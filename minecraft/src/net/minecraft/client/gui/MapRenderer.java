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
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

@Environment(EnvType.CLIENT)
public class MapRenderer implements AutoCloseable {
	private static final ResourceLocation MAP_ICONS_LOCATION = new ResourceLocation("textures/map/map_icons.png");
	private final TextureManager textureManager;
	private final Map<String, MapRenderer.MapInstance> maps = Maps.<String, MapRenderer.MapInstance>newHashMap();

	public MapRenderer(TextureManager textureManager) {
		this.textureManager = textureManager;
	}

	public void update(MapItemSavedData mapItemSavedData) {
		this.getMapInstance(mapItemSavedData).updateTexture();
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, MapItemSavedData mapItemSavedData, boolean bl) {
		this.getMapInstance(mapItemSavedData).draw(poseStack, multiBufferSource, bl);
	}

	private MapRenderer.MapInstance getMapInstance(MapItemSavedData mapItemSavedData) {
		MapRenderer.MapInstance mapInstance = (MapRenderer.MapInstance)this.maps.get(mapItemSavedData.getId());
		if (mapInstance == null) {
			mapInstance = new MapRenderer.MapInstance(mapItemSavedData);
			this.maps.put(mapItemSavedData.getId(), mapInstance);
		}

		return mapInstance;
	}

	@Nullable
	public MapRenderer.MapInstance getMapInstanceIfExists(String string) {
		return (MapRenderer.MapInstance)this.maps.get(string);
	}

	public void resetData() {
		for (MapRenderer.MapInstance mapInstance : this.maps.values()) {
			mapInstance.close();
		}

		this.maps.clear();
	}

	@Nullable
	public MapItemSavedData getData(@Nullable MapRenderer.MapInstance mapInstance) {
		return mapInstance != null ? mapInstance.data : null;
	}

	public void close() {
		this.resetData();
	}

	@Environment(EnvType.CLIENT)
	class MapInstance implements AutoCloseable {
		private final MapItemSavedData data;
		private final DynamicTexture texture;
		private final ResourceLocation location;

		private MapInstance(MapItemSavedData mapItemSavedData) {
			this.data = mapItemSavedData;
			this.texture = new DynamicTexture(128, 128, true);
			this.location = MapRenderer.this.textureManager.register("map/" + mapItemSavedData.getId(), this.texture);
		}

		private void updateTexture() {
			for (int i = 0; i < 128; i++) {
				for (int j = 0; j < 128; j++) {
					int k = j + i * 128;
					int l = this.data.colors[k] & 255;
					if (l / 4 == 0) {
						this.texture.getPixels().setPixelRGBA(j, i, (k + k / 128 & 1) * 8 + 16 << 24);
					} else {
						this.texture.getPixels().setPixelRGBA(j, i, MaterialColor.MATERIAL_COLORS[l / 4].calculateRGBColor(l & 3));
					}
				}
			}

			this.texture.upload();
		}

		private void draw(PoseStack poseStack, MultiBufferSource multiBufferSource, boolean bl) {
			int i = 0;
			int j = 0;
			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder bufferBuilder = tesselator.getBuilder();
			float f = 0.0F;
			Matrix4f matrix4f = poseStack.getPose();
			MapRenderer.this.textureManager.bind(this.location);
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(
				GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE
			);
			RenderSystem.disableAlphaTest();
			bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
			bufferBuilder.vertex(matrix4f, 0.0F, 128.0F, -0.01F).uv(0.0F, 1.0F).endVertex();
			bufferBuilder.vertex(matrix4f, 128.0F, 128.0F, -0.01F).uv(1.0F, 1.0F).endVertex();
			bufferBuilder.vertex(matrix4f, 128.0F, 0.0F, -0.01F).uv(1.0F, 0.0F).endVertex();
			bufferBuilder.vertex(matrix4f, 0.0F, 0.0F, -0.01F).uv(0.0F, 0.0F).endVertex();
			tesselator.end();
			RenderSystem.enableAlphaTest();
			RenderSystem.disableBlend();
			int k = 0;

			for (MapDecoration mapDecoration : this.data.decorations.values()) {
				if (!bl || mapDecoration.renderOnFrame()) {
					MapRenderer.this.textureManager.bind(MapRenderer.MAP_ICONS_LOCATION);
					poseStack.pushPose();
					poseStack.translate((double)(0.0F + (float)mapDecoration.getX() / 2.0F + 64.0F), (double)(0.0F + (float)mapDecoration.getY() / 2.0F + 64.0F), -0.02F);
					poseStack.mulPose(Vector3f.ZP.rotation((float)(mapDecoration.getRot() * 360) / 16.0F, true));
					poseStack.scale(4.0F, 4.0F, 3.0F);
					poseStack.translate(-0.125, 0.125, 0.0);
					byte b = mapDecoration.getImage();
					float g = (float)(b % 16 + 0) / 16.0F;
					float h = (float)(b / 16 + 0) / 16.0F;
					float l = (float)(b % 16 + 1) / 16.0F;
					float m = (float)(b / 16 + 1) / 16.0F;
					Matrix4f matrix4f2 = poseStack.getPose();
					bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
					float n = -0.001F;
					bufferBuilder.vertex(matrix4f2, -1.0F, 1.0F, (float)k * -0.001F).uv(g, h).color(255, 255, 255, 255).endVertex();
					bufferBuilder.vertex(matrix4f2, 1.0F, 1.0F, (float)k * -0.001F).uv(l, h).color(255, 255, 255, 255).endVertex();
					bufferBuilder.vertex(matrix4f2, 1.0F, -1.0F, (float)k * -0.001F).uv(l, m).color(255, 255, 255, 255).endVertex();
					bufferBuilder.vertex(matrix4f2, -1.0F, -1.0F, (float)k * -0.001F).uv(g, m).color(255, 255, 255, 255).endVertex();
					bufferBuilder.end();
					BufferUploader.end(bufferBuilder);
					poseStack.popPose();
					if (mapDecoration.getName() != null) {
						Font font = Minecraft.getInstance().font;
						String string = mapDecoration.getName().getColoredString();
						float o = (float)font.width(string);
						float p = Mth.clamp(25.0F / o, 0.0F, 6.0F / 9.0F);
						poseStack.pushPose();
						poseStack.translate(
							(double)(0.0F + (float)mapDecoration.getX() / 2.0F + 64.0F - o * p / 2.0F), (double)(0.0F + (float)mapDecoration.getY() / 2.0F + 64.0F + 4.0F), -0.025F
						);
						poseStack.scale(p, p, 1.0F);
						GuiComponent.fill(poseStack.getPose(), -1, -1, (int)o, 9 - 1, Integer.MIN_VALUE);
						poseStack.translate(0.0, 0.0, -0.1F);
						RenderSystem.enableAlphaTest();
						font.drawInBatch(string, 0.0F, 0.0F, -1, false, poseStack.getPose(), multiBufferSource, false, 0, 15728880);
						poseStack.popPose();
					}

					k++;
				}
			}
		}

		public void close() {
			this.texture.close();
		}
	}
}
