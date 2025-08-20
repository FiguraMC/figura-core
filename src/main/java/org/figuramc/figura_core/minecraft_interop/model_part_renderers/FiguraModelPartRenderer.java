package org.figuramc.figura_core.minecraft_interop.model_part_renderers;

/**
 * Something that can render a particular FiguraModelPart, given inputs.
 * - It should be memory-countable, because it's stored by avatars
 * - It can be destroyable, to clean up any native resources such as VBOs
 */
public interface FiguraModelPartRenderer {

    /**
     * Render the model part. Any parameters should be stored as fields on the class implementing this,
     * and the fields should be set by the implementor before this is to be called.
     */
    void render() throws Throwable;

    /**
     * May be called by a script when it's time for a rebuild. Not all renderers need to do anything when this happens,
     * but for VBO-based renderers, it lets us know when to rebuild the VBO after a change is made.
     * It's similar to Texture:upload() in that it communicates changes to the GPU, and is invoked manually to allow performance.
     */
    void rebuild() throws Throwable;

    /**
     * Destroy this renderer, cleaning up any native resources it uses.
     */
    void destroy();

}
