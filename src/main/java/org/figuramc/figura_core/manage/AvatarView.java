package org.figuramc.figura_core.manage;

import org.figuramc.figura_core.avatars.Avatar;
import org.figuramc.figura_core.util.functional.BiThrowingConsumer;
import org.figuramc.figura_core.util.functional.BiThrowingFunction;

public class AvatarView<Key> {

    private final Avatar<Key> avatar;

    public AvatarView(Avatar<Key> avatar) {
        this.avatar = avatar;
    }

    // Return whether this avatar is thread-safe.
    // If this is true, then use() will not block the caller.
    // If false, use() might block, if another thread is using the avatar.
    public boolean isThreadSafe() {
        return avatar.isThreadSafe;
    }

    // THIS SHOULDN'T DEADLOCK!
    // A constraint needs to be defined, which is that *thread-safe avatars cannot look at thread-unsafe avatars*.
    // This means a thread-safe avatar cannot invoke a thread-unsafe avatar's API functions.
    // A deadlock could occur if Avatars A and B are on different threads, A is looking at B, and B is looking at A.
    // However, if both avatars are looking at each other, that means both are thread-safe.
    // Therefore there is no `synchronized` block being triggered.

    // Use the Avatar in an operation.
    // Synchronizes only if the avatar is not thread-safe.
    public <E1 extends Throwable, E2 extends Throwable> void use(BiThrowingConsumer<Avatar<Key>, E1, E2> func) throws E1, E2 {
        if (avatar.isThreadSafe) {
            func.accept(avatar);
        } else {
            synchronized (avatar) {
                func.accept(avatar);
            }
        }
    }
    // Use the Avatar in an operation and return a result.
    public <R, E1 extends Throwable, E2 extends Throwable> R useFor(BiThrowingFunction<Avatar<Key>, R, E1, E2> func) throws E1, E2 {
        if (avatar.isThreadSafe) {
            return func.apply(avatar);
        } else {
            synchronized (avatar) {
                return func.apply(avatar);
            }
        }
    }
}
