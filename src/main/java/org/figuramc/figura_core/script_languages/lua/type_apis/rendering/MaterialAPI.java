package org.figuramc.figura_core.script_languages.lua.type_apis.rendering;

import org.figuramc.figura_cobalt.LuaOOM;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.*;
import org.figuramc.figura_core.comptime.lua.annotations.LuaExpose;
import org.figuramc.figura_core.comptime.lua.annotations.LuaPassState;
import org.figuramc.figura_core.comptime.lua.annotations.LuaReturnSelf;
import org.figuramc.figura_core.comptime.lua.annotations.LuaTypeAPI;
import org.figuramc.figura_core.minecraft_interop.FiguraConnectionPoint;
import org.figuramc.figura_core.model.rendering.FiguraRenderType;
import org.figuramc.figura_core.model.rendering.shader.BuiltinShader;
import org.figuramc.figura_core.model.rendering.shader.FiguraShader;
import org.figuramc.figura_core.model.texture.AvatarTexture;
import org.figuramc.figura_core.script_hooks.flags.QueuedSetters;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector4d;
import org.joml.Vector4i;

import java.util.ArrayList;
import java.util.List;

@LuaTypeAPI(typeName = "Material", wrappedClass = FiguraRenderType.class)
public class MaterialAPI {

    public static LuaUserdata wrap(FiguraRenderType material, LuaRuntime state) {
        return new LuaUserdata(material, state.figuraMetatables.material);
    }

    // Parse the options table to create a render type.
    @LuaExpose(name = "new") @LuaPassState
    public static FiguraRenderType _new(LuaRuntime s, LuaTable options) throws LuaError, LuaOOM {
        // Priority
        int priority = options.rawget("priority") instanceof LuaNumber n ? n.toInteger() : 0;

        // Shader
        FiguraShader shader = switch (options.rawget("shader")) {
            case LuaString stringShader -> switch (stringShader.toJavaString(s.allocationTracker)) {
                case "albedo" -> BuiltinShader.ALBEDO;
                case "albedo_normal" -> BuiltinShader.ALBEDO_NORMAL;
                case "albedo_specular" -> BuiltinShader.ALBEDO_SPECULAR;
                case "albedo_normal_specular" -> BuiltinShader.ALBEDO_NORMAL_SPECULAR;
                case "end_portal" -> BuiltinShader.END_PORTAL;
                case "end_gateway" -> BuiltinShader.END_GATEWAY;
                default -> throw new LuaError("Unrecognized \"shader\" in Material.new: \"" + stringShader + "\"", s.allocationTracker);
            };
            default -> throw new LuaError("Expected key \"shader\" in Material.new options to be a string.", s.allocationTracker);
        };

        // Texture bindings
        List<FiguraRenderType.@NotNull TextureBinding> textureBindings = switch (options.rawget("textures")) {
            case LuaTable texturesTable -> {
                // Look for texture bindings by name
                List<FiguraRenderType.@NotNull TextureBinding> bindings = new ArrayList<>(shader.textureBindingPoints().size());
                for (int i = 0; i < shader.textureBindingPoints().size(); i++) {
                    String bindingPoint = shader.textureBindingPoints().get(i);
                    LuaValue texture = texturesTable.rawget(bindingPoint);
                    switch (texture) {
                        case LuaUserdata userdata when userdata.userdata() instanceof AvatarTexture avatarTexture -> {
                            bindings.add(new FiguraRenderType.TextureBinding(avatarTexture.getHandle(), avatarTexture.getUvValues()));
                        }
                        case LuaNil nil -> {
                            // Default binding
                            var defaultBinding = shader.defaultBindings().get(i);
                            bindings.add(FiguraConnectionPoint.TEXTURE_PROVIDER.getBuiltinTexture(defaultBinding));
                        }
                        default -> throw new LuaError("Expected texture in \"textures\" table to be a Texture object, instead found " + texture.typeName(), s.allocationTracker);
                    }
                }
                yield bindings;
            }
            case LuaNil nil -> {
                // All default values
                List<FiguraRenderType.@NotNull TextureBinding> bindings = new ArrayList<>(shader.textureBindingPoints().size());
                for (int i = 0; i < shader.textureBindingPoints().size(); i++) {
                    var defaultBinding = shader.defaultBindings().get(i);
                    bindings.add(FiguraConnectionPoint.TEXTURE_PROVIDER.getBuiltinTexture(defaultBinding));
                }
                yield bindings;
            }
            default -> throw new LuaError("Expected \"textures\" in Material.new options to be a table or nil, instead found " + options.rawget("textures").typeName(), s.allocationTracker);
        };

        // Scissor state
        FiguraRenderType.ScissorState scissorState = new FiguraRenderType.ScissorState();
        switch (options.rawget("scissorState")) {
            // Vec4 = set value
            case LuaUserdata userdata when userdata.userdata() instanceof Vector4d vec -> scissorState.set((int) vec.x, (int) vec.y, (int) vec.z, (int) vec.w);
            // Nil = default, do nothing (don't error)
            case LuaNil nil -> {}
            default -> throw new LuaError("Expected \"scissorState\" in Material.new options to be a vec4 or nil, instead found " + options.rawget("scissorState").typeName(), s.allocationTracker);
        }

        // Finally return
        return new FiguraRenderType(priority, shader, textureBindings, scissorState);
    }

    // Enable/disable for scissor state
    @LuaExpose @LuaReturnSelf public static void enableScissors(FiguraRenderType renderType, Vector4d vec) { enableScissors(renderType, (int) vec.x, (int) vec.y, (int) vec.z, (int) vec.w); }
    @LuaExpose @LuaReturnSelf public static void enableScissors(FiguraRenderType renderType, Vector2d pos, Vector2d size) { enableScissors(renderType, (int) pos.x, (int) pos.y, (int) size.x, (int) size.y); }
    @LuaExpose @LuaReturnSelf public static void enableScissors(FiguraRenderType renderType, int x, int y, int w, int h) { QueuedSetters.handle(() -> renderType.scissorState.set(x, y, w, h)); }
    @LuaExpose @LuaReturnSelf public static void disableScissors(FiguraRenderType renderType) { QueuedSetters.handle(() -> renderType.scissorState.set(-1, -1, -1, -1)); }

}
