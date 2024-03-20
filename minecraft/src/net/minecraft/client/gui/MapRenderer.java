package net.minecraft.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.MapDecorationTextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class MapRenderer implements AutoCloseable {
	private static final int WIDTH = 128;
	private static final int HEIGHT = 128;
	final TextureManager textureManager;
	final MapDecorationTextureManager decorationTextures;
	private final Int2ObjectMap<MapRenderer.MapInstance> maps = new Int2ObjectOpenHashMap<>();

	public MapRenderer(TextureManager textureManager, MapDecorationTextureManager mapDecorationTextureManager) {
		this.textureManager = textureManager;
		this.decorationTextures = mapDecorationTextureManager;
	}

	public void update(MapId mapId, MapItemSavedData mapItemSavedData) {
		this.getOrCreateMapInstance(mapId, mapItemSavedData).forceUpload();
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, MapId mapId, MapItemSavedData mapItemSavedData, boolean bl, int i) {
		this.getOrCreateMapInstance(mapId, mapItemSavedData).draw(poseStack, multiBufferSource, bl, i);
	}

	private MapRenderer.MapInstance getOrCreateMapInstance(MapId mapId, MapItemSavedData mapItemSavedData) {
		return this.maps.compute(mapId.id(), (integer, mapInstance) -> {
			if (mapInstance == null) {
				return new MapRenderer.MapInstance(integer, mapItemSavedData);
			} else {
				mapInstance.replaceMapData(mapItemSavedData);
				return mapInstance;
			}
		});
	}

	public void resetData() {
		for (MapRenderer.MapInstance mapInstance : this.maps.values()) {
			mapInstance.close();
		}

		this.maps.clear();
	}

	public void close() {
		this.resetData();
	}

	@Environment(EnvType.CLIENT)
	class MapInstance implements AutoCloseable {
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
			for (int i = 0; i < 128; i++) {
				for (int j = 0; j < 128; j++) {
					int k = j + i * 128;
					this.texture.getPixels().setPixelRGBA(j, i, MapColor.getColorFromPackedId(this.data.colors[k]));
				}
			}

			this.texture.upload();
		}

		void draw(PoseStack poseStack, MultiBufferSource multiBufferSource, boolean bl, int i) {
			if (this.requiresUpload) {
				this.updateTexture();
				this.requiresUpload = false;
			}

			int j = 0;
			int k = 0;
			float f = 0.0F;
			Matrix4f matrix4f = poseStack.last().pose();
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(this.renderType);
			vertexConsumer.vertex(matrix4f, 0.0F, 128.0F, -0.01F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(i).endVertex();
			vertexConsumer.vertex(matrix4f, 128.0F, 128.0F, -0.01F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(i).endVertex();
			vertexConsumer.vertex(matrix4f, 128.0F, 0.0F, -0.01F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(i).endVertex();
			vertexConsumer.vertex(matrix4f, 0.0F, 0.0F, -0.01F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(i).endVertex();
			int l = 0;

			for (MapDecoration mapDecoration : this.data.getDecorations()) {
				if (!bl || mapDecoration.renderOnFrame()) {
					poseStack.pushPose();
					poseStack.translate(0.0F + (float)mapDecoration.x() / 2.0F + 64.0F, 0.0F + (float)mapDecoration.y() / 2.0F + 64.0F, -0.02F);
					poseStack.mulPose(Axis.ZP.rotationDegrees((float)(mapDecoration.rot() * 360) / 16.0F));
					poseStack.scale(4.0F, 4.0F, 3.0F);
					poseStack.translate(-0.125F, 0.125F, 0.0F);
					Matrix4f matrix4f2 = poseStack.last().pose();
					float g = -0.001F;
					TextureAtlasSprite textureAtlasSprite = MapRenderer.this.decorationTextures.get(mapDecoration);
					float h = textureAtlasSprite.getU0();
					float m = textureAtlasSprite.getV0();
					float n = textureAtlasSprite.getU1();
					float o = textureAtlasSprite.getV1();
					VertexConsumer vertexConsumer2 = multiBufferSource.getBuffer(RenderType.text(textureAtlasSprite.atlasLocation()));
					vertexConsumer2.vertex(matrix4f2, -1.0F, 1.0F, (float)l * -0.001F).color(255, 255, 255, 255).uv(h, m).uv2(i).endVertex();
					vertexConsumer2.vertex(matrix4f2, 1.0F, 1.0F, (float)l * -0.001F).color(255, 255, 255, 255).uv(n, m).uv2(i).endVertex();
					vertexConsumer2.vertex(matrix4f2, 1.0F, -1.0F, (float)l * -0.001F).color(255, 255, 255, 255).uv(n, o).uv2(i).endVertex();
					vertexConsumer2.vertex(matrix4f2, -1.0F, -1.0F, (float)l * -0.001F).color(255, 255, 255, 255).uv(h, o).uv2(i).endVertex();
					poseStack.popPose();
					if (mapDecoration.name().isPresent()) {
						Font font = Minecraft.getInstance().font;
						Component component = (Component)mapDecoration.name().get();
						float p = (float)font.width(component);
						float q = Mth.clamp(25.0F / p, 0.0F, 6.0F / 9.0F);
						poseStack.pushPose();
						poseStack.translate(0.0F + (float)mapDecoration.x() / 2.0F + 64.0F - p * q / 2.0F, 0.0F + (float)mapDecoration.y() / 2.0F + 64.0F + 4.0F, -0.025F);
						poseStack.scale(q, q, 1.0F);
						poseStack.translate(0.0F, 0.0F, -0.1F);
						font.drawInBatch(component, 0.0F, 0.0F, -1, false, poseStack.last().pose(), multiBufferSource, Font.DisplayMode.NORMAL, Integer.MIN_VALUE, i);
						poseStack.popPose();
					}

					l++;
				}
			}
		}

		public void close() {
			this.texture.close();
		}
	}
}
