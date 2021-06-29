/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

@Environment(value=EnvType.CLIENT)
public class MapRenderer
implements AutoCloseable {
    private static final ResourceLocation MAP_ICONS_LOCATION = new ResourceLocation("textures/map/map_icons.png");
    static final RenderType MAP_ICONS = RenderType.text(MAP_ICONS_LOCATION);
    private static final int WIDTH = 128;
    private static final int HEIGHT = 128;
    final TextureManager textureManager;
    private final Int2ObjectMap<MapInstance> maps = new Int2ObjectOpenHashMap<MapInstance>();

    public MapRenderer(TextureManager textureManager) {
        this.textureManager = textureManager;
    }

    public void update(int i, MapItemSavedData mapItemSavedData) {
        this.getOrCreateMapInstance(i, mapItemSavedData).forceUpload();
    }

    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, MapItemSavedData mapItemSavedData, boolean bl, int j) {
        this.getOrCreateMapInstance(i, mapItemSavedData).draw(poseStack, multiBufferSource, bl, j);
    }

    private MapInstance getOrCreateMapInstance(int i, MapItemSavedData mapItemSavedData) {
        return this.maps.compute(i, (integer, mapInstance) -> {
            if (mapInstance == null) {
                return new MapInstance((int)integer, mapItemSavedData);
            }
            mapInstance.replaceMapData(mapItemSavedData);
            return mapInstance;
        });
    }

    public void resetData() {
        for (MapInstance mapInstance : this.maps.values()) {
            mapInstance.close();
        }
        this.maps.clear();
    }

    @Override
    public void close() {
        this.resetData();
    }

    @Environment(value=EnvType.CLIENT)
    class MapInstance
    implements AutoCloseable {
        private MapItemSavedData data;
        private final DynamicTexture texture;
        private final RenderType renderType;
        private boolean requiresUpload = true;

        MapInstance(int i, MapItemSavedData mapItemSavedData) {
            this.data = mapItemSavedData;
            this.texture = new DynamicTexture(128, 128, true);
            ResourceLocation resourceLocation = MapRenderer.this.textureManager.register("map/" + i, this.texture);
            this.renderType = RenderType.text(resourceLocation);
        }

        void replaceMapData(MapItemSavedData mapItemSavedData) {
            boolean bl = this.data != mapItemSavedData;
            this.data = mapItemSavedData;
            this.requiresUpload |= bl;
        }

        public void forceUpload() {
            this.requiresUpload = true;
        }

        private void updateTexture() {
            for (int i = 0; i < 128; ++i) {
                for (int j = 0; j < 128; ++j) {
                    int k = j + i * 128;
                    int l = this.data.colors[k] & 0xFF;
                    if (l / 4 == 0) {
                        this.texture.getPixels().setPixelRGBA(j, i, 0);
                        continue;
                    }
                    this.texture.getPixels().setPixelRGBA(j, i, MaterialColor.MATERIAL_COLORS[l / 4].calculateRGBColor(l & 3));
                }
            }
            this.texture.upload();
        }

        void draw(PoseStack poseStack, MultiBufferSource multiBufferSource, boolean bl, int i) {
            if (this.requiresUpload) {
                this.updateTexture();
                this.requiresUpload = false;
            }
            boolean j = false;
            boolean k = false;
            float f = 0.0f;
            Matrix4f matrix4f = poseStack.last().pose();
            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(this.renderType);
            vertexConsumer.vertex(matrix4f, 0.0f, 128.0f, -0.01f).color(255, 255, 255, 255).uv(0.0f, 1.0f).uv2(i).endVertex();
            vertexConsumer.vertex(matrix4f, 128.0f, 128.0f, -0.01f).color(255, 255, 255, 255).uv(1.0f, 1.0f).uv2(i).endVertex();
            vertexConsumer.vertex(matrix4f, 128.0f, 0.0f, -0.01f).color(255, 255, 255, 255).uv(1.0f, 0.0f).uv2(i).endVertex();
            vertexConsumer.vertex(matrix4f, 0.0f, 0.0f, -0.01f).color(255, 255, 255, 255).uv(0.0f, 0.0f).uv2(i).endVertex();
            int l = 0;
            for (MapDecoration mapDecoration : this.data.getDecorations()) {
                if (bl && !mapDecoration.renderOnFrame()) continue;
                poseStack.pushPose();
                poseStack.translate(0.0f + (float)mapDecoration.getX() / 2.0f + 64.0f, 0.0f + (float)mapDecoration.getY() / 2.0f + 64.0f, -0.02f);
                poseStack.mulPose(Vector3f.ZP.rotationDegrees((float)(mapDecoration.getRot() * 360) / 16.0f));
                poseStack.scale(4.0f, 4.0f, 3.0f);
                poseStack.translate(-0.125, 0.125, 0.0);
                byte b = mapDecoration.getImage();
                float g = (float)(b % 16 + 0) / 16.0f;
                float h = (float)(b / 16 + 0) / 16.0f;
                float m = (float)(b % 16 + 1) / 16.0f;
                float n = (float)(b / 16 + 1) / 16.0f;
                Matrix4f matrix4f2 = poseStack.last().pose();
                float o = -0.001f;
                VertexConsumer vertexConsumer2 = multiBufferSource.getBuffer(MAP_ICONS);
                vertexConsumer2.vertex(matrix4f2, -1.0f, 1.0f, (float)l * -0.001f).color(255, 255, 255, 255).uv(g, h).uv2(i).endVertex();
                vertexConsumer2.vertex(matrix4f2, 1.0f, 1.0f, (float)l * -0.001f).color(255, 255, 255, 255).uv(m, h).uv2(i).endVertex();
                vertexConsumer2.vertex(matrix4f2, 1.0f, -1.0f, (float)l * -0.001f).color(255, 255, 255, 255).uv(m, n).uv2(i).endVertex();
                vertexConsumer2.vertex(matrix4f2, -1.0f, -1.0f, (float)l * -0.001f).color(255, 255, 255, 255).uv(g, n).uv2(i).endVertex();
                poseStack.popPose();
                if (mapDecoration.getName() != null) {
                    Font font = Minecraft.getInstance().font;
                    Component component = mapDecoration.getName();
                    float p = font.width(component);
                    float f2 = 25.0f / p;
                    Objects.requireNonNull(font);
                    float q = Mth.clamp(f2, 0.0f, 6.0f / 9.0f);
                    poseStack.pushPose();
                    poseStack.translate(0.0f + (float)mapDecoration.getX() / 2.0f + 64.0f - p * q / 2.0f, 0.0f + (float)mapDecoration.getY() / 2.0f + 64.0f + 4.0f, -0.025f);
                    poseStack.scale(q, q, 1.0f);
                    poseStack.translate(0.0, 0.0, -0.1f);
                    font.drawInBatch(component, 0.0f, 0.0f, -1, false, poseStack.last().pose(), multiBufferSource, false, Integer.MIN_VALUE, i);
                    poseStack.popPose();
                }
                ++l;
            }
        }

        @Override
        public void close() {
            this.texture.close();
        }
    }
}

