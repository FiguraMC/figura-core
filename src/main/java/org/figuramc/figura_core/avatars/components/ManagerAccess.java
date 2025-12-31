package org.figuramc.figura_core.avatars.components;

import org.figuramc.figura_core.avatars.Avatar;
import org.figuramc.figura_core.avatars.AvatarComponent;
import org.figuramc.figura_core.avatars.AvatarModules;
import org.figuramc.figura_core.data.importer.v1.ModuleImporter;
import org.figuramc.figura_core.data.importer.ModuleImportingException;
import org.figuramc.figura_core.data.materials.ModuleMaterials;
import org.figuramc.figura_core.minecraft_interop.FiguraConnectionPoint;
import org.figuramc.figura_core.util.IOUtils;
import org.figuramc.figura_core.util.data_structures.DataTree;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This component gives an Avatar access to very powerful management functions.
 * This should only be given to GUI avatars, which have the highest authority, even higher than host avatars.
 */
public class ManagerAccess implements AvatarComponent<ManagerAccess> {

    public static final Type<ManagerAccess> TYPE = new Type<>(ManagerAccess::new);

    public ManagerAccess(Avatar<?> avatar, AvatarModules modules) {}

    /**
     * Get all Avatars in <figura folder>/avatars.
     * Returns null if the user hasn't set a Figura folder yet,
     * or if an error otherwise occurs while accessing the avatars folder.
     */
    public @Nullable DataTree<String, AvatarListEntry> getAvatars() {
        Path avatars = FiguraConnectionPoint.PATH_PROVIDER.getAvatarsFolder().getNow(null);
        if (avatars == null) return null;
        try {
            return IOUtils.recursiveProcess(avatars, path -> {
                try {
                    if (!Files.isDirectory(path)) return null;
                    ModuleMaterials.MetadataMaterials materials = ModuleImporter.checkMetadata(path);
                    if (materials == null) return null;
                    return new AvatarListEntry.ValidEntry(path, materials);
                } catch (ModuleImportingException error) {
                    return new AvatarListEntry.ImportError(path, error);
                }
            }, null, false, true, null);
        } catch (Throwable unexpectedError) {
            return null;
        }
    }

    /**
     * Outputs for getAvatars() function
     */
    public sealed interface AvatarListEntry {
        Path path();
        record ImportError(Path path, ModuleImportingException error) implements AvatarListEntry {}
        record ValidEntry(Path path, ModuleMaterials.MetadataMaterials metadata) implements AvatarListEntry {}
    }

}
