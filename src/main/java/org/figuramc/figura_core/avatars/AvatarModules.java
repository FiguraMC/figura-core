package org.figuramc.figura_core.avatars;

import org.figuramc.figura_core.data.importer.v1.ModuleImporter;
import org.figuramc.figura_core.data.importer.ModuleImportingException;
import org.figuramc.figura_core.data.materials.ModuleMaterials;
import org.figuramc.figura_core.minecraft_interop.FiguraConnectionPoint;
import org.figuramc.figura_core.script_hooks.ScriptingLanguage;
import org.figuramc.figura_core.script_hooks.callback.CallbackType;
import org.figuramc.figura_core.script_hooks.callback.ScriptCallback;
import org.figuramc.figura_translations.TranslatableItems;
import org.figuramc.memory_tracker.AllocationTracker;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

// Tracks all modules in an avatar, including the main module and its dependencies.
// Also collects some other information about the collection of modules, such as which script languages they use.
public record AvatarModules(List<LoadTimeModule> loadTimeModules, Collection<AvatarComponent.Type<? extends ScriptRuntimeComponent<?>>> scriptRuntimeTypes) {

    // Return a list of load-time modules.
    // The last module in the list is the main module.
    // Dependencies always come before the dependent.
    public static AvatarModules loadModules(ModuleMaterials materials) throws ModuleImportingException, IOException {
        ArrayList<LoadTimeModule> loadTimeModules = new ArrayList<>();
        Set<AvatarComponent.Type<? extends ScriptRuntimeComponent<?>>> scriptRuntimeTypes = new HashSet<>();
        loadModule(loadTimeModules, scriptRuntimeTypes, materials, new HashMap<>());
        return new AvatarModules(loadTimeModules, scriptRuntimeTypes);
    }

    private static int loadModule(ArrayList<LoadTimeModule> loadTimeModules, Set<AvatarComponent.Type<? extends ScriptRuntimeComponent<?>>> scriptRuntimeTypes, ModuleMaterials materials, Map<String, Integer> alreadyImported) throws ModuleImportingException, IOException {
        Path commonModules = FiguraConnectionPoint.PATH_PROVIDER.getCommonModulesFolder().join();
        LinkedHashMap<String, Integer> dependencyIndices = new LinkedHashMap<>();
        for (var dep : materials.metadata().dependencies().entrySet()) {
            String givenName = dep.getKey();
            String dependency = dep.getValue();
            // Get dependency if not already imported
            if (!alreadyImported.containsKey(dependency)) {
                Path dependencyPath = commonModules.resolve(dependency);
                ModuleMaterials dependencyMats = ModuleImporter.importPath(dependencyPath);
                int index = loadModule(loadTimeModules, scriptRuntimeTypes, dependencyMats, alreadyImported);
                alreadyImported.put(dependency, index);
            }
            // Save index to map
            dependencyIndices.put(givenName, alreadyImported.get(dependency));
        }
        // Figure out the language
        AvatarComponent.Type<? extends ScriptRuntimeComponent<?>> runtimeType = null;
        if (materials.metadata().language() != null) {
            ScriptingLanguage lang = ScriptingLanguage.LANGUAGES.get(materials.metadata().language());
            if (lang == null) throw new ModuleImportingException(ScriptingLanguage.UNKNOWN_LANGUAGE, new TranslatableItems.Items1<>(materials.metadata().language()));
            runtimeType = lang.componentType;
            scriptRuntimeTypes.add(runtimeType);
        }
        // Now that dependencies are processed, add this module
        int index = loadTimeModules.size();
        LoadTimeModule module = new LoadTimeModule(index, materials, dependencyIndices, runtimeType);
        loadTimeModules.add(module);
        return index;
    }

    // "Module at load time" is different from "Module at runtime"!
    // After loading finishes, all LoadTimeModule should be deleted.
    // RuntimeModule will remain. RuntimeModule contains only information necessary after loading completes.

    // Representation of a singular module in the avatar, during load time.
    // May be updated and mutated by components it's used in
    public static class LoadTimeModule {

        public final int index; // Index of this module in the list
        public ModuleMaterials materials; // This will take up lots of memory, so we will null it out at a later stage once it's done being used.
        public final LinkedHashMap<String, Integer> dependencyIndices; // Indices of dependent modules in the list, by name

        // The runtime type used by this module, if any
        public final @Nullable AvatarComponent.Type<? extends ScriptRuntimeComponent<?>> runtimeType;

        private LoadTimeModule(int index, ModuleMaterials materials, LinkedHashMap<String, Integer> dependencyIndices, @Nullable AvatarComponent.Type<? extends ScriptRuntimeComponent<?>> runtimeType) {
            this.index = index;
            this.materials = materials;
            this.dependencyIndices = dependencyIndices;
            this.runtimeType = runtimeType;
        }
    }

    // A module in the avatar, represented at runtime.
    // Contains necessary info for runtime.
    public static class RuntimeModule {

        public final int index; // Index of this module

        private final int[] dependencyIndices; // Indices of this module's dependencies
        private boolean initialized = false;
        private final boolean autoInitializeDependencies; // Whether this module will automatically initialize its dependencies
        public final Map<String, CallbackType.Func<?, ?>> api; // The typed functions this module is expected to provide
        public final Map<String, ScriptCallback<?, ?>> callbacks; // The functions this module *did* provide (after initialization)

        private final @Nullable ScriptRuntimeComponent runtime; // The script runtime this module uses (if any)

        private static final int SIZE_ESTIMATE =
                AllocationTracker.OBJECT_SIZE * 4
                + AllocationTracker.REFERENCE_SIZE * 5
                + AllocationTracker.INT_SIZE
                + AllocationTracker.BOOLEAN_SIZE * 2;

        public RuntimeModule(Avatar<?> avatar, LoadTimeModule loadTime, @Nullable AllocationTracker<AvatarError> allocationTracker) throws AvatarError {
            this.index = loadTime.index;
            this.autoInitializeDependencies = loadTime.materials.metadata().autoRequireDependencies();
            this.api = new HashMap<>(loadTime.materials.metadata().api());
            this.callbacks = new HashMap<>();
            this.dependencyIndices = loadTime.dependencyIndices.values().stream().mapToInt(x -> x).toArray();
            this.runtime = loadTime.runtimeType == null ? null : avatar.<ScriptRuntimeComponent>getComponent(loadTime.runtimeType);
            // Track!
            if (allocationTracker != null) {
                int totalSize = SIZE_ESTIMATE;
                totalSize += dependencyIndices.length * AllocationTracker.INT_SIZE;
                totalSize += api.size() + AllocationTracker.REFERENCE_SIZE * 4;
                for (var entry : api.entrySet()) {
                    totalSize += entry.getKey().length() * AllocationTracker.CHAR_SIZE + AllocationTracker.OBJECT_SIZE;
                    allocationTracker.track(entry.getValue(), entry.getValue().getSize());
                }
                allocationTracker.track(this, totalSize);
            }
        }

        // Initialize this module, given the list of all RuntimeModules.
        // (TODO Should we have some kind of cycle detection? Yes, but it doesn't have to be here at runtime, it can be at import time)
        public void initialize(List<RuntimeModule> allRuntimeModules) throws AvatarError {
            // If already initialized, we're done.
            if (initialized) return;
            initialized = true;
            // If we auto-initialize dependencies, do so now:
            if (autoInitializeDependencies)
                for (int index : dependencyIndices)
                    allRuntimeModules.get(index).initialize(allRuntimeModules);
            // Then initialize this module.
            if (runtime != null) runtime.initModule(this);
        }

    }


}
