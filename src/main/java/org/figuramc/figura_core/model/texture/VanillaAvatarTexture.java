package org.figuramc.figura_core.model.texture;

// TODO: Make it possible to create these by fetching from Minecraft, through a script API (not part of avatar loading)
public class VanillaAvatarTexture /*extends AvatarTexture*/ {

//    // Resource "%s" could not be found!
//    public static final Translatable<TranslatableItems.Items1<String>> RESOURCE_NOT_FOUND
//            = Translatable.create("figura_core.error.loading.texture.resource_not_found", String.class);
//
//    private MinecraftTexture backing;
//    private Vector4f uvValues;
//
//    protected VanillaAvatarTexture(ModuleMaterials.TextureMaterials.VanillaTexture vanilla) throws AvatarInitError {
//        var pair = FiguraConnectionPoint.TEXTURE_PROVIDER.getVanillaTexture(vanilla.resourceLocation());
//        if (pair == null) throw new CompletionException(new AvatarInitError(RESOURCE_NOT_FOUND, new TranslatableItems.Items1<>(vanilla.resourceLocation().toString())));
//        this.backing = pair.a();
//        this.uvValues = pair.b();
//    }
//
//    @Override
//    public CompletableFuture<Void> ready() {
//        // Ready when the backing texture is ready
//        return backing.readyToUse();
//    }
//
//    @Override
//    public CompletableFuture<Void> commit() {
//        // Not mutable, so no commit work needs to happen
//        return CompletableFuture.completedFuture(null);
//    }
//
//    @Override
//    public void destroy() {
//        backing = null;
//        uvValues = null;
//    }
//
//    @Override
//    public MinecraftTexture getHandle() {
//        return backing;
//    }
//
//    @Override
//    public Vector4f getUvValues() {
//        return uvValues;
//    }
//
//    @Override
//    public int getWidth() {
//        return backing.width();
//    }
//
//    @Override
//    public int getHeight() {
//        return backing.height();
//    }
//
//    @Override
//    public int getPixel(int x, int y) {
//        throw new UnsupportedOperationException("TODO: MinecraftTexture not necessarily readable");
//    }
}
