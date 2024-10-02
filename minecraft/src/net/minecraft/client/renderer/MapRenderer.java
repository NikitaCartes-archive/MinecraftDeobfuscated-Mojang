package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MapDecorationTextureManager;
import net.minecraft.client.resources.MapTextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class MapRenderer {
	private static final float MAP_Z_OFFSET = -0.01F;
	private static final float DECORATION_Z_OFFSET = -0.001F;
	private static final int WIDTH = 128;
	private static final int HEIGHT = 128;
	private final MapTextureManager mapTextureManager;
	private final MapDecorationTextureManager decorationTextures;

	public MapRenderer(MapDecorationTextureManager mapDecorationTextureManager, MapTextureManager mapTextureManager) {
		this.decorationTextures = mapDecorationTextureManager;
		this.mapTextureManager = mapTextureManager;
	}

	public void render(MapRenderState mapRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, boolean bl, int i) {
		Matrix4f matrix4f = poseStack.last().pose();
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.text(mapRenderState.texture));
		vertexConsumer.addVertex(matrix4f, 0.0F, 128.0F, -0.01F).setColor(-1).setUv(0.0F, 1.0F).setLight(i);
		vertexConsumer.addVertex(matrix4f, 128.0F, 128.0F, -0.01F).setColor(-1).setUv(1.0F, 1.0F).setLight(i);
		vertexConsumer.addVertex(matrix4f, 128.0F, 0.0F, -0.01F).setColor(-1).setUv(1.0F, 0.0F).setLight(i);
		vertexConsumer.addVertex(matrix4f, 0.0F, 0.0F, -0.01F).setColor(-1).setUv(0.0F, 0.0F).setLight(i);
		int j = 0;

		for (MapRenderState.MapDecorationRenderState mapDecorationRenderState : mapRenderState.decorations) {
			if (!bl || mapDecorationRenderState.renderOnFrame) {
				poseStack.pushPose();
				poseStack.translate((float)mapDecorationRenderState.x / 2.0F + 64.0F, (float)mapDecorationRenderState.y / 2.0F + 64.0F, -0.02F);
				poseStack.mulPose(Axis.ZP.rotationDegrees((float)(mapDecorationRenderState.rot * 360) / 16.0F));
				poseStack.scale(4.0F, 4.0F, 3.0F);
				poseStack.translate(-0.125F, 0.125F, 0.0F);
				Matrix4f matrix4f2 = poseStack.last().pose();
				TextureAtlasSprite textureAtlasSprite = mapDecorationRenderState.atlasSprite;
				if (textureAtlasSprite != null) {
					VertexConsumer vertexConsumer2 = multiBufferSource.getBuffer(RenderType.text(textureAtlasSprite.atlasLocation()));
					vertexConsumer2.addVertex(matrix4f2, -1.0F, 1.0F, (float)j * -0.001F)
						.setColor(-1)
						.setUv(textureAtlasSprite.getU0(), textureAtlasSprite.getV0())
						.setLight(i);
					vertexConsumer2.addVertex(matrix4f2, 1.0F, 1.0F, (float)j * -0.001F)
						.setColor(-1)
						.setUv(textureAtlasSprite.getU1(), textureAtlasSprite.getV0())
						.setLight(i);
					vertexConsumer2.addVertex(matrix4f2, 1.0F, -1.0F, (float)j * -0.001F)
						.setColor(-1)
						.setUv(textureAtlasSprite.getU1(), textureAtlasSprite.getV1())
						.setLight(i);
					vertexConsumer2.addVertex(matrix4f2, -1.0F, -1.0F, (float)j * -0.001F)
						.setColor(-1)
						.setUv(textureAtlasSprite.getU0(), textureAtlasSprite.getV1())
						.setLight(i);
					poseStack.popPose();
				}

				if (mapDecorationRenderState.name != null) {
					Font font = Minecraft.getInstance().font;
					float f = (float)font.width(mapDecorationRenderState.name);
					float g = Mth.clamp(25.0F / f, 0.0F, 6.0F / 9.0F);
					poseStack.pushPose();
					poseStack.translate((float)mapDecorationRenderState.x / 2.0F + 64.0F - f * g / 2.0F, (float)mapDecorationRenderState.y / 2.0F + 64.0F + 4.0F, -0.025F);
					poseStack.scale(g, g, 1.0F);
					poseStack.translate(0.0F, 0.0F, -0.1F);
					font.drawInBatch(
						mapDecorationRenderState.name, 0.0F, 0.0F, -1, false, poseStack.last().pose(), multiBufferSource, Font.DisplayMode.SEE_THROUGH, Integer.MIN_VALUE, i
					);
					poseStack.popPose();
				}

				j++;
			}
		}
	}

	public void extractRenderState(MapId mapId, MapItemSavedData mapItemSavedData, MapRenderState mapRenderState) {
		mapRenderState.texture = this.mapTextureManager.prepareMapTexture(mapId, mapItemSavedData);
		mapRenderState.decorations.clear();

		for (MapDecoration mapDecoration : mapItemSavedData.getDecorations()) {
			mapRenderState.decorations.add(this.extractDecorationRenderState(mapDecoration));
		}
	}

	private MapRenderState.MapDecorationRenderState extractDecorationRenderState(MapDecoration mapDecoration) {
		MapRenderState.MapDecorationRenderState mapDecorationRenderState = new MapRenderState.MapDecorationRenderState();
		mapDecorationRenderState.atlasSprite = this.decorationTextures.get(mapDecoration);
		mapDecorationRenderState.x = mapDecoration.x();
		mapDecorationRenderState.y = mapDecoration.y();
		mapDecorationRenderState.rot = mapDecoration.rot();
		mapDecorationRenderState.name = (Component)mapDecoration.name().orElse(null);
		mapDecorationRenderState.renderOnFrame = mapDecoration.renderOnFrame();
		return mapDecorationRenderState;
	}
}
