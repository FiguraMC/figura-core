package org.figuramc.figura_core.model.rendering;

import org.figuramc.figura_core.avatars.components.RenderDataHolder;
import org.figuramc.figura_core.avatars.errors.AvatarError;
import org.figuramc.figura_core.avatars.errors.AvatarOutOfMemoryError;
import org.figuramc.figura_core.minecraft_interop.FiguraConnectionPoint;
import org.figuramc.figura_core.minecraft_interop.render.ClientPartRenderer;
import org.figuramc.figura_core.minecraft_interop.text.MinecraftGlyphProvider;
import org.figuramc.figura_core.minecraft_interop.texture.MinecraftTexture;
import org.figuramc.figura_core.model.rendering.vertex.FiguraVertexElem;
import org.figuramc.figura_core.model.rendering.vertex.FiguraVertexFormat;
import org.figuramc.figura_core.model.rendering.vertex.PartVertexData;
import org.figuramc.figura_core.text.FormattedText;
import org.figuramc.figura_core.text.TextStyle;
import org.figuramc.figura_core.util.ListUtils;
import org.figuramc.figura_core.util.MapUtils;
import org.figuramc.figura_core.util.data_structures.ByteBufferBuilder;
import org.figuramc.figura_core.util.data_structures.Pair;
import org.figuramc.figura_translations.Translatable;
import org.figuramc.figura_translations.TranslatableItems;
import org.figuramc.memory_tracker.AllocationTracker;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Optionally stored in a FiguraModelPart.
 * Holds data needed for rendering, like built vertex data and a list of drawcalls.
 */
public class RenderData implements AutoCloseable {

    // Built data, by format, in native-order byte buffers.
    public final Map<FiguraVertexFormat, ByteBuffer> builtData;
    // The list of draw calls
    public final List<DrawCall> drawCalls;
    // Array holding part info, populated each render pass
    public final PartDataStruct[] partData;

    // The RenderDataHolder this is known by, if any
    private @Nullable RenderDataHolder holder;

    // Render state for this, owned by the client
    public final ClientPartRenderer clientPartRenderer;

    // Holds a slice of built vertex data with the given draw call info, starting point, and length
    public record DrawCall(FiguraRenderType.DrawCallInfo drawCallInfo, int start, int length) {}

    // Finalize the builder and merge constructed data
    // If a holder is passed, store it there.
    private RenderData(Builder built, @Nullable RenderDataHolder holder, @Nullable AllocationTracker<AvatarOutOfMemoryError> allocationTracker) throws AvatarOutOfMemoryError {
        // Group draw call infos by priority and shader
        var byPriority = built.buildingBuffers.entrySet().stream()
                .filter(e -> e.getValue().size() != 0)
                .collect(Collectors.groupingBy(e -> e.getKey().priority(), TreeMap::new, Collectors.toList()));
        var byShaderByPriority = MapUtils.mapValues(byPriority, samePriority -> samePriority.stream()
                .collect(Collectors.groupingBy(e -> e.getKey().shader(), HashMap::new, Collectors.toList())));

        // Coalesce to a sorted list of pairs (DrawCallInfo, ByteBufferBuilder)
        var drawCallInfosAndBufferBuilders = byShaderByPriority.values().stream()
                .flatMap(map -> map.values().stream())
                .flatMap(List::stream)
                .map(Pair::new)
                .toList();

        // Create map of built data
        var groupedByFormat = drawCallInfosAndBufferBuilders.stream().collect(Collectors.groupingBy(p -> p.a().shader().vertexFormat()));
        this.builtData = MapUtils.mapValues(groupedByFormat, pair -> {
            int size = pair.stream().map(Pair::b).mapToInt(ByteBufferBuilder::size).sum();
            // TODO: Reuse buffers? This could get laggy if we're constructing too many
            //       Would also affect allocation tracker
            ByteBuffer buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
            if (allocationTracker != null) allocationTracker.track(buffer, size);
            return buffer;
        });

        // Initialize buffers and draw calls
        this.drawCalls = ListUtils.map(drawCallInfosAndBufferBuilders, pair -> {
            // Extract pair
            FiguraRenderType.DrawCallInfo drawCallInfo = pair.a();
            ByteBufferBuilder bufferBuilder = pair.b();
            // Get buffer and add data
            ByteBuffer buf = builtData.get(drawCallInfo.shader().vertexFormat());
            int pos = buf.position();
            bufferBuilder.writeTo(buf);
            return new DrawCall(drawCallInfo, pos, bufferBuilder.size());
        });

        // Create part infos array
        partData = new PartDataStruct[built.currentPartID];
        for (int i = 0; i < partData.length; i++)
            partData[i] = new PartDataStruct();
        if (allocationTracker != null)
            allocationTracker.track(partData, AllocationTracker.OBJECT_SIZE + partData.length * (AllocationTracker.REFERENCE_SIZE + PartDataStruct.CPU_SIZE));

        // Set up client state
        clientPartRenderer = FiguraConnectionPoint.PART_RENDERER_FACTORY.apply(this, allocationTracker);

        // Save this instance in the holder, if one was given
        this.holder = holder;
        if (holder != null) {
            holder.register(this);
        }
    }

    // Constructed through builder pattern
    public static Builder builder() { return new Builder(); }
    public static class Builder {

        private int currentPartID; // ID of the current part
        private Map<FiguraRenderType.DrawCallInfo, ByteBufferBuilder> buildingBuffers = new HashMap<>();

        /**
         * If this builder has no parts in it, then we just return null here.
         * Otherwise, construct a RenderData and return it.
         */
        public @Nullable RenderData build(@Nullable RenderDataHolder renderDataHolder, @Nullable AllocationTracker<AvatarOutOfMemoryError> allocationTracker) throws AvatarOutOfMemoryError {
            if (currentPartID == 0) return null;
            return new RenderData(this, renderDataHolder, allocationTracker);
        }

        /**
         * Build this into a text-based render data.
         * Assumes addText() was called at least once.
         * Also returns the shared scissor state between all used materials in this text rendering.
         */
        public Pair<RenderData, FiguraRenderType.ScissorState> buildText(@Nullable RenderDataHolder renderDataHolder, @Nullable AllocationTracker<AvatarOutOfMemoryError> allocationTracker) throws AvatarOutOfMemoryError {
            assert currentPartID > 0; // At least one thing was added
            return new Pair<>(new RenderData(this, renderDataHolder, allocationTracker), this.scissorState);
        }


        /**
         * Add the given vertex data into this builder, with the given render type
         */
        public void addModelPart(PartVertexData vertexData, FiguraRenderType renderType) throws AvatarError {
            // Get the byte buffer builder
            ByteBufferBuilder buffer = buildingBuffers.computeIfAbsent(renderType.drawCallInfo(), x -> new ByteBufferBuilder());
            // Fetch unique ID and increment
            final int partID = currentPartID++;
            // Local vars avoiding field fetches (probably doesn't matter)
            final var vertexCount = vertexData.vertexCount;
            final var positions = vertexData.positions;
            final var riggingWeights = vertexData.riggingWeights;
            final var riggingOffsets = vertexData.riggingOffsets;
            final var uvs = vertexData.uvs;
            final var normals = vertexData.normals;
            final var tangents = vertexData.tangents;
            final var vertexFormat = renderType.drawCallInfo().shader().vertexFormat();
            final var elements = vertexFormat.elements;
            final var offsets = vertexFormat.offsets;
            final var textureBindings = renderType.textureBindings;
            // Iterate vertices:
            for (int i = 0; i < vertexCount; i++) {
                int start = buffer.size();
                // Iterate vertex elements
                for (int j = 0; j < elements.length; j++) {
                    // Align by padding with 0s:
                    while (buffer.size() < start + offsets[j])
                        buffer.push((byte) 0);
                    // Insert data
                    FiguraVertexElem elem = elements[j];
                    switch (elem.id) {
                        case POSITION -> {
                            int k = i * 3;
                            buffer.pushFloat(positions[k]);
                            buffer.pushFloat(positions[k + 1]);
                            buffer.pushFloat(positions[k + 2]);
                        }
                        case RIGGING_WEIGHTS -> {
                            int k = i * 4;
                            buffer.push(riggingWeights[k]);
                            buffer.push(riggingWeights[k + 1]);
                            buffer.push(riggingWeights[k + 2]);
                            buffer.push(riggingWeights[k + 3]);
                        }
                        case RIGGING_INDICES -> {
                            if (partID > 0xFF00) throw new AvatarError(TOO_MANY_GROUPS, TranslatableItems.Items0.INSTANCE);
                            int k = i * 4;
                            buffer.pushUnsignedShort((char) (partID + Byte.toUnsignedInt(riggingOffsets[k])));
                            buffer.pushUnsignedShort((char) (partID + Byte.toUnsignedInt(riggingOffsets[k + 1])));
                            buffer.pushUnsignedShort((char) (partID + Byte.toUnsignedInt(riggingOffsets[k + 2])));
                            buffer.pushUnsignedShort((char) (partID + Byte.toUnsignedInt(riggingOffsets[k + 3])));
                        }
                        case UV0, UV1, UV2, UV3 -> {
                            int k = i * 2;
                            float u = uvs[k];
                            float v = uvs[k + 1];
                            int uvIndex = elem.id.ordinal() - FiguraVertexElem.ID.UV0.ordinal(); // Offset from UV0
                            Vector4f uvModifier = textureBindings.get(uvIndex).uvModifier();
                            u = u * uvModifier.z + uvModifier.x;
                            v = v * uvModifier.w + uvModifier.y;
                            buffer.pushFloat(u).pushFloat(v);
                        }
                        case NORMAL -> {
                            int k = i * 3;
                            buffer.push(normals[k]);
                            buffer.push(normals[k + 1]);
                            buffer.push(normals[k + 2]);
                        }
                        case TANGENT -> {
                            int k = i * 3;
                            buffer.push(tangents[k]);
                            buffer.push(tangents[k + 1]);
                            buffer.push(tangents[k + 2]);
                        }
                        // Vertex color is just always 255 for regular model parts, it's unused in most shaders
                        case COLOR -> buffer.push((byte) -1).push((byte) -1).push((byte) -1).push((byte) -1);
                        case OTHER -> {
                            // Custom data
                            byte @Nullable [] data = vertexData.dataByElement.get(elem);
                            if (data == null) {
                                // Zeroes by default.
                                for (int k = 0; k < elem.type.size; k++)
                                    buffer.push((byte) 0);
                            } else {
                                buffer.pushArr(data, i * elem.type.size, elem.type.size);
                            }
                        }
                    }
                }
                // Align whole vertex by padding with zeros
                while (buffer.size() < start + vertexFormat.vertexSize)
                    buffer.push((byte) 0);
            }
        }

        // (Not done, temporarily removed ahead of commit)
        // Text rendering state variables
        private float curX, curY, curLineHeight;
        private List<PendingChar> curLine;
        private TextStyle curStyle;
        private float defaultLineHeight;
        private FiguraRenderType.ScissorState scissorState; // Shared scissor state between all
        // Cache text render types
        // This is a bit cursed since we ignore the UV modifier in this mapping.
        // The UV modifier is handled manually and strictly internally on a per-char basis, and is not relevant during actual rendering.
        private Map<MinecraftTexture, FiguraRenderType> textRenderTypeCache;

        // Add the given text.
        // This is intended to be called every frame for dynamic text, so this should perform well!
        public void addText(FormattedText text) {
            // Set up initial variables
            defaultLineHeight = FiguraConnectionPoint.GLYPH_PROVIDER.getDefaultLineHeight();
            curX = 0; curY = 0; curLineHeight = 0;
            curLine = new ArrayList<>();
            scissorState = new FiguraRenderType.ScissorState();
            textRenderTypeCache = new HashMap<>();
            processText(text, 0); // Add all the text
            flushLine(); // Flush final line
            currentPartID++; // Increment part ID since we just added a text part
        }

        // Add text recursively and return the new char index
        private int processText(FormattedText text, int charIndex) {
            curStyle = text.style;
            for (int codepoint : text.codepoints)
                processChar(codepoint, charIndex++);
            for (FormattedText child : text.children)
                charIndex = processText(child, charIndex);
            return charIndex;
        }

        private void processChar(int codepoint, int charIndex) {
            Vector2f scale = curStyle.scale.value(charIndex);
            float glyphHeight = defaultLineHeight * scale.y;
            curLineHeight = Math.max(curLineHeight, glyphHeight);
            // If we have a newline, flush this line. Otherwise, append this char to the current line.
            if (codepoint == '\n') {
                flushLine();
            } else {
                boolean obfuscated = curStyle.obfuscated.value(charIndex);
                MinecraftGlyphProvider.GlyphInfo info = FiguraConnectionPoint.GLYPH_PROVIDER.getGlyphInfo(codepoint, obfuscated);
                // Calculate advance
                boolean bold = curStyle.bold.value(charIndex);
                float advance = info.advance();
                if (bold) advance += info.boldOffset();
                advance *= scale.x;
                // Add it in
                curLine.add(new PendingChar(info, bold, scale.x, scale.y, advance, glyphHeight, curStyle, charIndex));
            }
        }

        private void flushLine() {
            // Go through the pending chars and add their vertices
            boolean empty = curLine.isEmpty();
            curX = 0;
            for (PendingChar pending : curLine) {
                addChar(pending);
                curX += pending.advance;
            }
            curY += empty ? defaultLineHeight : curLineHeight;
            curLine.clear();
            curLineHeight = 0;
        }

        // Add the given char at the current coords
        private void addChar(PendingChar pendingChar) {
            // Local vars
            MinecraftGlyphProvider.GlyphInfo info = pendingChar.info;
            FiguraRenderType.TextureBinding textureBinding = info.textureBinding();
            Vector4f uvModifier = textureBinding.uvModifier();
            float x = curX;
            float y = curY;
            float height = pendingChar.height;
            float scaleX = pendingChar.scaleX;
            float scaleY = pendingChar.scaleY;
            TextStyle style = pendingChar.style;
            float charIndex = pendingChar.charIndex;

            // Fetch or create a render type and buffer
            FiguraRenderType renderType = textRenderTypeCache.computeIfAbsent(textureBinding.handle(), handle -> FiguraRenderType.text(0, new FiguraRenderType.TextureBinding(handle, null), scissorState));
            ByteBufferBuilder buffer = buildingBuffers.computeIfAbsent(renderType.drawCallInfo(), __ -> new ByteBufferBuilder());

            // Colors
            Vector4f color = style.color.value(charIndex);
            Vector4f backgroundColor = style.backgroundColor.value(charIndex);
            Vector4f shadowColor = style.shadowColor.value(charIndex);
            Vector4f outlineColor = style.outlineColor.value(charIndex);
            Vector4f strikethroughColor = style.strikethroughColor.value(charIndex);
            Vector4f underlineColor = style.underlineColor.value(charIndex);

            // Offset (does not change location of any extra effects)
            Vector2f offset = style.offset.value(charIndex);
            x += offset.x; y += offset.y;

            // Vertical alignment
            y += (curLineHeight - height) * style.verticalAlignment.value(charIndex);

            // Skew
            float skewX = style.italic.value(charIndex) ? scaleX : 0;
            Vector2f skew = style.skew.value(charIndex);
            skewX += skew.x;
            float skewY = skew.y;

            // Get drawing numbers
            float x0 = x + info.left();
            float x1 = x + info.right();
            float y0 = y + info.top();
            float y1 = y + info.bottom();
            float u0 = uvModifier.x;
            float u1 = u0 + uvModifier.z;
            float v0 = uvModifier.y;
            float v1 = v0 + uvModifier.w;

            // Shadow
            if (shadowColor.w != 0) {
                Vector2f shadowOffset = style.shadowOffset.value(charIndex);
                shadowOffset.mul(scaleX, scaleY);
            }

            // Outline effect
            if (outlineColor.w != 0) {
                Vector2f outlineScale = style.outlineScale.value(charIndex);
                for (int oY = -1; oY <= 1; oY++) {
                    for (int oX = -1; oX <= 1; oX++) {
                        // Skip drawing unneeded chars
                        if (oX == 0 && oY == 0) continue;
                        if (outlineScale.x == 0 && oX != 0) continue;
                        if (outlineScale.y == 0 && oY != 0) continue;
                        // Calculate where to draw
                        float cX = oX * outlineScale.x;
                        float cY = oY * outlineScale.y;
                        // Draw it
                        addCharVertices(buffer, renderType.shader.vertexFormat(), cX + x0, cY + y0, cX + x1, cY + y1, u0, v0, u1, v1, skewX, skewY, outlineColor);
                    }
                }
            }

            // Output text vertices
            addCharVertices(buffer, renderType.shader.vertexFormat(), x0, y0, x1, y1, u0, v0, u1, v1, skewX, skewY, color);
        }

        private void addCharVertices(ByteBufferBuilder buffer, FiguraVertexFormat format, float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1, float skewX, float skewY, Vector4f color) {
            addTextVertex(buffer, format, x0 + skewX, y0 - skewY, u0, v0, color);
            addTextVertex(buffer, format, x0 - skewX, y1 - skewY, u0, v1, color);
            addTextVertex(buffer, format, x1 - skewX, y1 + skewY, u1, v1, color);
            addTextVertex(buffer, format, x1 + skewX, y0 + skewY, u1, v0, color);
        }

        // Add a text vertex
        private void addTextVertex(ByteBufferBuilder buffer, FiguraVertexFormat format, float x, float y, float u, float v, Vector4f color) {
            int start = buffer.size();
            for (int i = 0; i < format.elements.length; i++) {
                // Align by padding with 0s:
                while (buffer.size() < start + format.offsets[i])
                    buffer.push((byte) 0);
                // Add data
                switch (format.elements[i].id) {
                    case POSITION -> buffer.pushFloat(x).pushFloat(y).pushFloat(0);
                    case RIGGING_WEIGHTS -> buffer.pushNormalizedUnsignedByte(1.0f).pushNormalizedUnsignedByte(0.0f).pushNormalizedUnsignedByte(0.0f).pushNormalizedUnsignedByte(0.0f);
                    case RIGGING_INDICES -> buffer.pushUnsignedShort((char) this.currentPartID).pushUnsignedShort((char) 0).pushUnsignedShort((char) 0).pushUnsignedShort((char) 0);
                    case UV0 -> buffer.pushFloat(u).pushFloat(v);
                    case COLOR -> buffer.pushNormalizedUnsignedByte(color.x).pushNormalizedUnsignedByte(color.y).pushNormalizedUnsignedByte(color.z).pushNormalizedUnsignedByte(color.w);
                }
            }
            // Align whole vertex by padding with zeros
            while (buffer.size() < start + format.vertexSize)
                buffer.push((byte) 0);
        }

        private record PendingChar(MinecraftGlyphProvider.GlyphInfo info, boolean bold, float scaleX, float scaleY, float advance, float height, TextStyle style, int charIndex) {}


        private static final Translatable<TranslatableItems.Items0> TOO_MANY_GROUPS = Translatable.create("figura_core.error.rendering.too_many_groups");
    }

    @Override
    public synchronized void close() {
        // Close the client state!
        if (this.clientPartRenderer != null) {
            this.clientPartRenderer.close();
        }
        // If there's a holder, this is already closed, so remove ourselves from the holder to avoid memory leaks
        if (this.holder != null) {
            this.holder.deregister(this);
            this.holder = null;
        }
    }
}
