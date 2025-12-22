package org.figuramc.figura_core.manage;

import org.figuramc.figura_core.avatars.Avatar;
import org.figuramc.figura_core.minecraft_interop.FiguraConnectionPoint;
import org.figuramc.figura_core.util.exception.ExceptionUtils;
import org.figuramc.figura_core.util.exception.FiguraException;
import org.figuramc.figura_core.util.functional.ThrowingConsumer;
import org.figuramc.figura_core.util.functional.ThrowingSupplier;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages a set of avatars, indexed by the given key.
 */
public class AvatarManager<K> {

    private final ConcurrentHashMap<K, Avatar<K>> loadedAvatars = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<K, CompletableFuture<@Nullable Avatar<K>>> inProgressAvatars = new ConcurrentHashMap<>();

    // See if any async tasks have completed; if they have, initialize them.
    public void poll() {
        // Iterate over in-progress Avatars, see if any are complete.
        // If they are, move them to the map of loaded Avatars.
        for (var entry : inProgressAvatars.entrySet()) {
            K key = entry.getKey();
            CompletableFuture<Avatar<K>> future = inProgressAvatars.get(key);
            if (future.isDone()) {
                // Fetch the result of the future:
                Avatar<K> result;
                try {
                    // If the following getNow() call doesn't throw, then the async task finished without throwing
                    result = future.getNow(null);
                } catch (CompletionException ex) {
                    // For now, we'll ALWAYS report to chat/console.
                    // Maybe later we'll disable this for multiplayer avatars (whenever we get to that lol)
                    if (ex.getCause() instanceof FiguraException figuraException) FiguraConnectionPoint.CONSOLE_OUTPUT.reportError(figuraException);
                    else FiguraConnectionPoint.CONSOLE_OUTPUT.reportUnexpectedError(ex.getCause());
                    // Cancel the in-progress Avatar, since it errored
                    cancelInProgress(key);
                    continue;
                } catch (Throwable unexpected) {
                    FiguraConnectionPoint.CONSOLE_OUTPUT.reportUnexpectedError(unexpected);
                    cancelInProgress(key);
                    continue;
                }

                // If no result, then just remove it from the map and end
                if (result == null) {
                    cancelInProgress(key);
                    continue;
                }

                // If it's not ready yet, continue and wait until it is ready
                if (!result.isReady()) continue;

                // Now that the result is ready, remove from the in-progress map and add to the loaded map
                inProgressAvatars.remove(key);
                loadedAvatars.put(key, result);
            }
        }
    }

    /**
     * Clear out the entire manager.
     */
    public void clear() {
        for (K key : inProgressAvatars.keySet())
            cancelInProgress(key);
        for (K key : loadedAvatars.keySet())
            unload(key);
        inProgressAvatars.clear();
        loadedAvatars.clear();
    }

    /**
     * Run the consumer on each loaded Avatar.
     */
    public <E extends Throwable> void forEach(ThrowingConsumer<? super Avatar<K>, E> consumer) throws E {
        for (Avatar<K> avatar : loadedAvatars.values())
            new AvatarView<>(avatar).use(consumer::accept);
    }

    /**
     * Get an Avatar if it's loaded. Return null if no Avatar exists for this key.
     */
    public @Nullable AvatarView<K> get(K key) {
        Avatar<K> avatar = loadedAvatars.get(key);
        if (avatar == null) return null;
        return new AvatarView<>(avatar);
    }

    /**
     * Check if there's an avatar in progress for this key:
     */
    public boolean isInProgress(K key) {
        return inProgressAvatars.containsKey(key);
    }

    /**
     * Unload an Avatar that is currently loaded, and destroy it.
     * If there is no Avatar for the given key, does nothing.
     */
    public void unload(K key) {
        Avatar<K> oldAvatar = loadedAvatars.remove(key);
        if (oldAvatar != null) oldAvatar.destroy();
    }

    /**
     * Cancel an Avatar in progress.
     * This removes it from the in-progress map, and queues up an action on the
     * future to destroy the Avatar once it's loaded.
     * Unfortunately we can't actually cancel a future in progress, so the best we can do
     * is (asynchronously) immediately destroy the Avatar once it's done.
     */
    public void cancelInProgress(K key) {
        CompletableFuture<Avatar<K>> oldTask = inProgressAvatars.remove(key);
        if (oldTask != null) oldTask.whenComplete((avatar, error) -> {
            if (avatar != null) avatar.destroy();
        });
    }

    /**
     * Launch the creation task on an off-thread and load the avatar.
     */
    public void load(K key, ThrowingSupplier<@Nullable Avatar<K>, Throwable> creationTask) {
        // Cancel any previous task, then launch the new task.
        cancelInProgress(key);
        inProgressAvatars.put(key, CompletableFuture.supplyAsync(ExceptionUtils.wrapChecked(creationTask, CompletionException::new)));
    }

}
