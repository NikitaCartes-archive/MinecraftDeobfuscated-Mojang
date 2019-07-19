package net.minecraft.client.gui;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.Map;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
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

	public void render(MapItemSavedData mapItemSavedData, boolean bl) {
		this.getMapInstance(mapItemSavedData).draw(bl);
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

		private void draw(boolean bl) {
			int i = 0;
			int j = 0;
			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder bufferBuilder = tesselator.getBuilder();
			float f = 0.0F;
			MapRenderer.this.textureManager.bind(this.location);
			GlStateManager.enableBlend();
			GlStateManager.blendFuncSeparate(
				GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE
			);
			GlStateManager.disableAlphaTest();
			bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
			bufferBuilder.vertex(0.0, 128.0, -0.01F).uv(0.0, 1.0).endVertex();
			bufferBuilder.vertex(128.0, 128.0, -0.01F).uv(1.0, 1.0).endVertex();
			bufferBuilder.vertex(128.0, 0.0, -0.01F).uv(1.0, 0.0).endVertex();
			bufferBuilder.vertex(0.0, 0.0, -0.01F).uv(0.0, 0.0).endVertex();
			tesselator.end();
			GlStateManager.enableAlphaTest();
			GlStateManager.disableBlend();
			int k = 0;

			for (MapDecoration mapDecoration : this.data.decorations.values()) {
				if (!bl || mapDecoration.renderOnFrame()) {
					MapRenderer.this.textureManager.bind(MapRenderer.MAP_ICONS_LOCATION);
					GlStateManager.pushMatrix();
					GlStateManager.translatef(0.0F + (float)mapDecoration.getX() / 2.0F + 64.0F, 0.0F + (float)mapDecoration.getY() / 2.0F + 64.0F, -0.02F);
					GlStateManager.rotatef((float)(mapDecoration.getRot() * 360) / 16.0F, 0.0F, 0.0F, 1.0F);
					GlStateManager.scalef(4.0F, 4.0F, 3.0F);
					GlStateManager.translatef(-0.125F, 0.125F, 0.0F);
					byte b = mapDecoration.getImage();
					float g = (float)(b % 16 + 0) / 16.0F;
					float h = (float)(b / 16 + 0) / 16.0F;
					float l = (float)(b % 16 + 1) / 16.0F;
					float m = (float)(b / 16 + 1) / 16.0F;
					bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
					GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
					float n = -0.001F;
					bufferBuilder.vertex(-1.0, 1.0, (double)((float)k * -0.001F)).uv((double)g, (double)h).endVertex();
					bufferBuilder.vertex(1.0, 1.0, (double)((float)k * -0.001F)).uv((double)l, (double)h).endVertex();
					bufferBuilder.vertex(1.0, -1.0, (double)((float)k * -0.001F)).uv((double)l, (double)m).endVertex();
					bufferBuilder.vertex(-1.0, -1.0, (double)((float)k * -0.001F)).uv((double)g, (double)m).endVertex();
					tesselator.end();
					GlStateManager.popMatrix();
					if (mapDecoration.getName() != null) {
						Font font = Minecraft.getInstance().font;
						String string = mapDecoration.getName().getColoredString();
						float o = (float)font.width(string);
						float p = Mth.clamp(25.0F / o, 0.0F, 6.0F / 9.0F);
						GlStateManager.pushMatrix();
						GlStateManager.translatef(
							0.0F + (float)mapDecoration.getX() / 2.0F + 64.0F - o * p / 2.0F, 0.0F + (float)mapDecoration.getY() / 2.0F + 64.0F + 4.0F, -0.025F
						);
						GlStateManager.scalef(p, p, 1.0F);
						GuiComponent.fill(-1, -1, (int)o, 9 - 1, Integer.MIN_VALUE);
						GlStateManager.translatef(0.0F, 0.0F, -0.1F);
						font.draw(string, 0.0F, 0.0F, -1);
						GlStateManager.popMatrix();
					}

					k++;
				}
			}

			GlStateManager.pushMatrix();
			GlStateManager.translatef(0.0F, 0.0F, -0.04F);
			GlStateManager.scalef(1.0F, 1.0F, 1.0F);
			GlStateManager.popMatrix();
		}

		public void close() {
			this.texture.close();
		}
	}
}
