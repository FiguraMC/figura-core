package org.figuramc.figura_core.avatars.components;

import org.figuramc.figura_core.avatars.Avatar;
import org.figuramc.figura_core.avatars.AvatarComponent;
import org.figuramc.figura_core.avatars.AvatarModules;
import org.figuramc.figura_core.model.rendering.RenderData;

import java.util.HashSet;
import java.util.Set;

/**
 * Holds on to built rendering data for native resources, and cleans them up at the end of avatar life.
 */
public class RenderDataHolder implements AvatarComponent<RenderDataHolder> {

    public static final Type<RenderDataHolder> TYPE = new Type<>("RENDER_DATA_HOLDER", RenderDataHolder::new);
    public Type<RenderDataHolder> getType() { return TYPE; }

    private final Set<RenderData> allRenderData = new HashSet<>();

    public RenderDataHolder(Avatar<?> self, AvatarModules modules) {

    }

    // Register some render data to ensure we close later
    public void register(RenderData toCloseLater) {
        this.allRenderData.add(toCloseLater);
    }

    // Deregister some data after it's already been closed
    public void deregister(RenderData alreadyClosed) {
        assert alreadyClosed.clientPartRenderer == null;
        this.allRenderData.remove(alreadyClosed);
    }

    // On destruction, destroy all render data
    @Override
    public void destroy() {
        allRenderData.forEach(RenderData::close);
    }
}
