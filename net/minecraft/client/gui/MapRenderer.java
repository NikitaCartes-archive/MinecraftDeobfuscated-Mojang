/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class MapRenderer
implements AutoCloseable {
    private static final ResourceLocation MAP_ICONS_LOCATION = new ResourceLocation("textures/map/map_icons.png");
    private final TextureManager textureManager;
    private final Map<String, MapInstance> maps = Maps.newHashMap();

    public MapRenderer(TextureManager textureManager) {
        this.textureManager = textureManager;
    }

    public void update(MapItemSavedData mapItemSavedData) {
        this.getMapInstance(mapItemSavedData).updateTexture();
    }

    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, MapItemSavedData mapItemSavedData, boolean bl) {
        this.getMapInstance(mapItemSavedData).draw(poseStack, multiBufferSource, bl);
    }

    private MapInstance getMapInstance(MapItemSavedData mapItemSavedData) {
        MapInstance mapInstance = this.maps.get(mapItemSavedData.getId());
        if (mapInstance == null) {
            mapInstance = new MapInstance(mapItemSavedData);
            this.maps.put(mapItemSavedData.getId(), mapInstance);
        }
        return mapInstance;
    }

    @Nullable
    public MapInstance getMapInstanceIfExists(String string) {
        return this.maps.get(string);
    }

    public void resetData() {
        for (MapInstance mapInstance : this.maps.values()) {
            mapInstance.close();
        }
        this.maps.clear();
    }

    @Nullable
    public MapItemSavedData getData(@Nullable MapInstance mapInstance) {
        if (mapInstance != null) {
            return mapInstance.data;
        }
        return null;
    }

    @Override
    public void close() {
        this.resetData();
    }

    @Environment(value=EnvType.CLIENT)
    class MapInstance
    implements AutoCloseable {
        private final MapItemSavedData data;
        private final DynamicTexture texture;
        private final ResourceLocation location;

        private MapInstance(MapItemSavedData mapItemSavedData) {
            this.data = mapItemSavedData;
            this.texture = new DynamicTexture(128, 128, true);
            this.location = MapRenderer.this.textureManager.register("map/" + mapItemSavedData.getId(), this.texture);
        }

        private void updateTexture() {
            for (int i = 0; i < 128; ++i) {
                for (int j = 0; j < 128; ++j) {
                    int k = j + i * 128;
                    int l = this.data.colors[k] & 0xFF;
                    if (l / 4 == 0) {
                        this.texture.getPixels().setPixelRGBA(j, i, (k + k / 128 & 1) * 8 + 16 << 24);
                        continue;
                    }
                    this.texture.getPixels().setPixelRGBA(j, i, MaterialColor.MATERIAL_COLORS[l / 4].calculateRGBColor(l & 3));
                }
            }
            this.texture.upload();
        }

        private void draw(PoseStack poseStack, MultiBufferSource multiBufferSource, boolean bl) {
            boolean i = false;
            boolean j = false;
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferBuilder = tesselator.getBuilder();
            float f = 0.0f;
            Matrix4f matrix4f = poseStack.getPose();
            MapRenderer.this.textureManager.bind(this.location);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
            RenderSystem.disableAlphaTest();
            bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
            bufferBuilder.vertex(matrix4f, 0.0f, 128.0f, -0.01f).uv(0.0f, 1.0f).endVertex();
            bufferBuilder.vertex(matrix4f, 128.0f, 128.0f, -0.01f).uv(1.0f, 1.0f).endVertex();
            bufferBuilder.vertex(matrix4f, 128.0f, 0.0f, -0.01f).uv(1.0f, 0.0f).endVertex();
            bufferBuilder.vertex(matrix4f, 0.0f, 0.0f, -0.01f).uv(0.0f, 0.0f).endVertex();
            tesselator.end();
            RenderSystem.enableAlphaTest();
            RenderSystem.disableBlend();
            int k = 0;
            for (MapDecoration mapDecoration : this.data.decorations.values()) {
                if (bl && !mapDecoration.renderOnFrame()) continue;
                MapRenderer.this.textureManager.bind(MAP_ICONS_LOCATION);
                poseStack.pushPose();
                poseStack.translate(0.0f + (float)mapDecoration.getX() / 2.0f + 64.0f, 0.0f + (float)mapDecoration.getY() / 2.0f + 64.0f, -0.02f);
                poseStack.mulPose(Vector3f.ZP.rotation((float)(mapDecoration.getRot() * 360) / 16.0f, true));
                poseStack.scale(4.0f, 4.0f, 3.0f);
                poseStack.translate(-0.125, 0.125, 0.0);
                byte b = mapDecoration.getImage();
                float g = (float)(b % 16 + 0) / 16.0f;
                float h = (float)(b / 16 + 0) / 16.0f;
                float l = (float)(b % 16 + 1) / 16.0f;
                float m = (float)(b / 16 + 1) / 16.0f;
                Matrix4f matrix4f2 = poseStack.getPose();
                bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
                float n = -0.001f;
                bufferBuilder.vertex(matrix4f2, -1.0f, 1.0f, (float)k * -0.001f).uv(g, h).color(255, 255, 255, 255).endVertex();
                bufferBuilder.vertex(matrix4f2, 1.0f, 1.0f, (float)k * -0.001f).uv(l, h).color(255, 255, 255, 255).endVertex();
                bufferBuilder.vertex(matrix4f2, 1.0f, -1.0f, (float)k * -0.001f).uv(l, m).color(255, 255, 255, 255).endVertex();
                bufferBuilder.vertex(matrix4f2, -1.0f, -1.0f, (float)k * -0.001f).uv(g, m).color(255, 255, 255, 255).endVertex();
                bufferBuilder.end();
                BufferUploader.end(bufferBuilder);
                poseStack.popPose();
                if (mapDecoration.getName() != null) {
                    Font font = Minecraft.getInstance().font;
                    String string = mapDecoration.getName().getColoredString();
                    float o = font.width(string);
                    float f2 = 25.0f / o;
                    font.getClass();
                    float p = Mth.clamp(f2, 0.0f, 6.0f / 9.0f);
                    poseStack.pushPose();
                    poseStack.translate(0.0f + (float)mapDecoration.getX() / 2.0f + 64.0f - o * p / 2.0f, 0.0f + (float)mapDecoration.getY() / 2.0f + 64.0f + 4.0f, -0.025f);
                    poseStack.scale(p, p, 1.0f);
                    GuiComponent.fill(poseStack.getPose(), -1, -1, (int)o, font.lineHeight - 1, Integer.MIN_VALUE);
                    poseStack.translate(0.0, 0.0, -0.1f);
                    RenderSystem.enableAlphaTest();
                    font.drawInBatch(string, 0.0f, 0.0f, -1, false, poseStack.getPose(), multiBufferSource, false, 0, 0xF000F0);
                    poseStack.popPose();
                }
                ++k;
            }
        }

        @Override
        public void close() {
            this.texture.close();
        }
    }
}

