package org.figuramc.figura_core.avatars;

import org.figuramc.figura_core.manage.AvatarManager;
import org.figuramc.figura_core.minecraft_interop.FiguraConnectionPoint;
import org.figuramc.figura_core.minecraft_interop.vanilla_parts.VanillaModel;
import org.figuramc.figura_core.script_hooks.Event;
import org.figuramc.figura_core.script_hooks.EventListener;
import org.figuramc.figura_core.script_hooks.callback.items.CallbackItem;
import org.figuramc.figura_core.util.ListUtils;
import org.figuramc.figura_core.util.enumlike.IdMap;
import org.figuramc.figura_core.util.functional.ThrowingRunnable;
import org.figuramc.figura_translations.Translatable;
import org.figuramc.figura_translations.TranslatableItems;
import org.figuramc.memory_tracker.AllocationTracker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public final class Avatar<K> {

    public final AvatarManager<K> manager;
    public final K key; // The key which accesses this Avatar in its corresponding AvatarSubManager<K>
    public final @Nullable AllocationTracker<AvatarError> allocationTracker;
    public final List<AvatarModules.RuntimeModule> modules; // Runtime modules.

    // Components. Keep an IdMap to fetch nullable components, and an array to iterate only present components.
    private final IdMap<AvatarComponent.Type, AvatarComponent<?>> components; // Components, where ID -> component if present, null if not. Requires some unchecked sillies because of generics.
    private final @NotNull AvatarComponent<?>[] presentComponents; // Only the non-null components, used for iteration

    // Event listeners
    // Listeners for built-in events, indexed using the event's ID.
    public final IdMap<Event<?>, EventListener<?>> eventListeners;
    // Type-safe EventListener<T> getter from Event<T>
    @SuppressWarnings("unchecked") public <T extends CallbackItem> EventListener<T> getEventListener(Event<T> event) { return (EventListener<T>) eventListeners.get(event); }

    // Any error that's occurred in this avatar
    private @Nullable Throwable error;

    // Extra items which are helpful for initializing some types.
    // All of these items are optional.
    // TODO maybe try to figure out a cleaner way of providing this to VanillaRendering...
    public @Nullable VanillaModel vanillaModel;

    // Constructor without the extra types
    public Avatar(AvatarManager<K> manager, K key, AvatarModules loadTimeModules, @Nullable AllocationTracker<AvatarError> allocationTracker, Collection<AvatarComponent.Type<?>> componentTypes) throws AvatarError {
        this(manager, key, loadTimeModules, allocationTracker, componentTypes, null);
    }

    public Avatar(
            // Identification
            AvatarManager<K> manager, K key,
            // Modules and tracking
            AvatarModules loadTimeModules, @Nullable AllocationTracker<AvatarError> allocationTracker,
            // Set of component types
            Collection<AvatarComponent.Type<?>> componentTypes,
            // Various other info which may be used by some component types... not sure of a better way to handle this yet :(
            @Nullable VanillaModel vanillaModel
    ) throws AvatarError {
        // Set basic fields
        this.manager = manager;
        this.key = key;
        this.allocationTracker = allocationTracker;
        this.eventListeners = new IdMap<>(Event.class, e -> new EventListener<>(e.type));
        // Add script runtimes to the set of components
        componentTypes = new HashSet<>(componentTypes);
        componentTypes.addAll(loadTimeModules.scriptRuntimeTypes());
        // Set the extra helper fields
        this.vanillaModel = vanillaModel;
        // Construct all the provided component types
        this.components = new IdMap<>(AvatarComponent.Type.class);
        for (var ty : componentTypes) components.put(ty, ty.factory.apply(this, loadTimeModules));
        // Create presentComponents array by removing null elements for faster iteration.
        this.presentComponents = this.components.values().stream().filter(Objects::nonNull).toArray(AvatarComponent[]::new);
        // Create runtime modules
        this.modules = ListUtils.map(loadTimeModules.loadTimeModules(), loadTime -> new AvatarModules.RuntimeModule(this, loadTime, allocationTracker));
        // Init the main module. This should be okay to run on an off-thread.
        this.modules.getLast().initialize(this.modules);
    }

    // If onPoll() returns true, the avatar's loading will be canceled.
    public boolean onPoll() {
        for (AvatarComponent<?> component : presentComponents)
            if (component.onPoll()) return true;
        return false;
    }

    public boolean isReady() {
        for (AvatarComponent<?> component : presentComponents)
            if (!component.isReady()) return false;
        return true;
    }

    private static final Translatable<TranslatableItems.Items0> UNLOADED = Translatable.create("figura_core.avatar.unloaded");

    public void unload() {
        this.error(new AvatarError(UNLOADED, TranslatableItems.Items0.INSTANCE));
        this.manager.unload(this.key);
    }

    // Access this using the static field <subclass of AvatarComponent>.TYPE.
    // This field should exist if they followed the implementation instructions in AvatarComponent correctly.
    @SuppressWarnings("unchecked")
    public <T extends AvatarComponent<T>> @Nullable T getComponent(AvatarComponent.Type<? extends T> type) {
        // Errored avatars act like they have no components.
        // All mixins and such will look for a component on a given avatar and try to use it;
        // but they will be unable to get this component if the avatar is errored.
        if (isErrored()) return null;
        return (T) components.get(type);
    }

    public <T extends AvatarComponent<T>> @NotNull T assertComponent(AvatarComponent.Type<T> type) {
        return Objects.requireNonNull(getComponent(type), "Asserted component was not present!");
    }

    public void error(AvatarError reason) {
        // Mark as errored
        this.error = reason;
        // Report the error to user(?) (Todo improve)
        FiguraConnectionPoint.ERROR_REPORTER.report(reason);
    }

    public void unexpectedError(Throwable reason) {
        this.error = reason;
        FiguraConnectionPoint.ERROR_REPORTER.reportUnexpected(reason);
    }

    // We want to use this function only when strictly necessary; for most usages, the fact
    // that an errored avatar acts like it has no components is good enough.
    // An example where this is needed is during cross-avatar (or potentially cross-avatar) scripting calls.
    public boolean isErrored() {
        return error != null;
    }

    // Run on cleanup. Should be used to prevent memory leaks.
    public void destroy() {
        for (AvatarComponent<?> component : presentComponents)
            component.destroy();
    }

    // Should be run on the main thread, which is why it's not part of the constructor!
    public void mainThreadInitialize() {
        // Run the components' main thread init functions.
        for (AvatarComponent<?> component : presentComponents) {
            try {
                component.mainThreadInitialize();
            } catch (AvatarError e) {
                error(e);
            } catch (Throwable unexpected) {
                unexpectedError(unexpected);
            }
            if (isErrored()) break;
        }
    }

    // Run at the end of each client tick.
    // It just ticks each component in the order they were added to the Avatar.
    public void tick() {
        if (isErrored()) return; // Don't tick if errored
        for (AvatarComponent<?> component : presentComponents) {
            try {
                component.tick();
            } catch (AvatarError err) {
                error(err);
            } catch (Throwable unexpected) {
                unexpectedError(unexpected);
            }
            if (isErrored()) break;
        }
    }

    // Various helper methods

    // Run an event

    // Run the given event, with its args.
    // If it errors, the avatar will error out.
    public <Args extends CallbackItem> void runEvent(Event<Args> event, Args args) {
        if (isErrored()) return;
        try {
            // Fetch the event listener and invoke it.
            getEventListener(event).invoke(args);
        } catch (Throwable ex) {
            unexpectedError(ex);
        }
    }

    // Attempt to run the given lambda (which renders the model part) and error the avatar appropriately if it fails.
    public void tryRenderModelPart(ThrowingRunnable<Throwable> renderer) {
        if (isErrored()) return;
        try {
            renderer.run();
        } catch (StackOverflowError ex) {
            error(new AvatarError(STACK_OVERFLOW_DURING_RENDERING, TranslatableItems.Items0.INSTANCE));
        } catch (AvatarError avatarError) {
            error(avatarError);
        } catch (Throwable unexpected) {
            unexpectedError(unexpected);
        }
    }

    private static final Translatable<TranslatableItems.Items0> STACK_OVERFLOW_DURING_RENDERING
            = Translatable.create("figura_core.error.rendering.stack_overflow");

}
