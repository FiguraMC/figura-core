package org.figuramc.figura_core.script_hooks.callback.items;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract View.
 * Views are Closeable, and can be parented to other Views.
 * When a view is closed, all child views derived from it are also closed.
 */
public abstract class AbstractView implements AutoCloseable {

    private List<AbstractView> children; // TODO: Should we be worried about size of this list and its memory usage?

    public AbstractView() {}

    // If we constructed this View with a parent, we need to call this at the END of the SUBCLASS'S constructor!
    // It will check if the parent has already been revoked; if it has, then we close the child immediately as well.
    protected synchronized void registerToParent(AbstractView parent) {
        if (!parent.addChild(this))
            this.close();
    }

    // Return true if the child was successfully added.
    // Return false if this has been revoked.
    private synchronized boolean addChild(AbstractView child) {
        if (isRevoked()) return false;
        if (children == null) children = new ArrayList<>();
        children.add(child);
        return true;
    }


    /**
     * DO NOT use isRevoked as a check for whether an operation is safe to run!
     * Another thread might revoke the item after you call this!
     * Use it only as a means for diagnosing a sentinel-return in the actual methods!
     */
    public abstract /* not synchronized */ boolean isRevoked();

    @Override
    public synchronized void close() {
        if (children != null) {
            for (AbstractView child : children)
                child.close();
            children = null;
        }
    }

}
