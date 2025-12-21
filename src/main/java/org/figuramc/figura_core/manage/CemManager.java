package org.figuramc.figura_core.manage;

import org.figuramc.figura_core.avatars.AvatarModules;
import org.figuramc.figura_core.avatars.AvatarTemplates;
import org.figuramc.figura_core.data.importer.v1.ModuleImporter;
import org.figuramc.figura_core.data.materials.ModuleMaterials;
import org.figuramc.figura_core.minecraft_interop.FiguraConnectionPoint;
import org.figuramc.figura_core.minecraft_interop.game_data.MinecraftIdentifier;
import org.figuramc.figura_core.minecraft_interop.game_data.entity.MinecraftEntity;
import org.figuramc.figura_core.minecraft_interop.vanilla_parts.VanillaModel;
import org.figuramc.figura_core.util.exception.ExceptionUtils;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

public class CemManager {

    private static final ConcurrentHashMap<CemKey, CompletableFuture<@Nullable ModuleMaterials>> IMPORTED_MATERIALS = new ConcurrentHashMap<>();

    public static void clear() {
        IMPORTED_MATERIALS.clear();
    }

    // Tries to set up CEM for this entity.
    // Call this when we already know the entity doesn't have an avatar equipped.
    // - If the entity already has an avatar loading in progress, does nothing
    // - If this entity type has no CEM in the folder, does nothing
    // - Otherwise, will launch a task to give this entity its CEM avatar.
    public static void launchCemTask(MinecraftEntity entity) {
        UUID uuid = entity.getUUID();
        if (AvatarManagers.ENTITIES.isInProgress(uuid)) return;
        MinecraftIdentifier type = entity.getType();
        // Fetch the materials, or begin a task for them.
        CompletableFuture<@Nullable ModuleMaterials> materials = IMPORTED_MATERIALS.computeIfAbsent(new CemKey(type),
                cemKey -> CompletableFuture.supplyAsync(ExceptionUtils.wrapChecked(() -> {
                    // Try to load for this type
                    Path entityDir = FiguraConnectionPoint.PATH_PROVIDER.getCEMFolder().join().resolve(cemKey.entityType.namespace()).resolve(cemKey.entityType.name());
                    return Files.exists(entityDir) ? ModuleImporter.importPath(entityDir) : null;
                }, CompletionException::new)));
        // If the task isn't complete yet, just return out.
        if (!materials.isDone()) return;

        // Fetch the result. If this doesn't throw, then it completed without error.
        @Nullable ModuleMaterials result = materials.getNow(null);
        // If the result is null, this entity has no CEM, so just do nothing and return.
        if (result == null) return;
        // Otherwise, this entity has CEM, so launch a task to load it.
        VanillaModel model = entity.getModel();
        AvatarManagers.ENTITIES.load(uuid, () -> AvatarTemplates.cemAvatar(uuid, model, AvatarModules.loadModules(result)));
    }

    public record CemKey(MinecraftIdentifier entityType) {}

}
