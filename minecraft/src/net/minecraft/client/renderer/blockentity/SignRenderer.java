package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class SignRenderer extends BlockEntityRenderer<SignBlockEntity> {
	private final ModelPart sign = new ModelPart(64, 32, 0, 0);
	private final ModelPart stick;

	public SignRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
		super(blockEntityRenderDispatcher);
		this.sign.addBox(-12.0F, -14.0F, -1.0F, 24.0F, 12.0F, 2.0F, 0.0F);
		this.stick = new ModelPart(64, 32, 0, 14);
		this.stick.addBox(-1.0F, -2.0F, -1.0F, 2.0F, 14.0F, 2.0F, 0.0F);
	}

	public void render(
		SignBlockEntity signBlockEntity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j
	) {
		BlockState blockState = signBlockEntity.getBlockState();
		poseStack.pushPose();
		float h = 0.6666667F;
		if (blockState.getBlock() instanceof StandingSignBlock) {
			poseStack.translate(0.5, 0.5, 0.5);
			float k = -((float)((Integer)blockState.getValue(StandingSignBlock.ROTATION) * 360) / 16.0F);
			poseStack.mulPose(Vector3f.YP.rotationDegrees(k));
			this.stick.visible = true;
		} else {
			poseStack.translate(0.5, 0.5, 0.5);
			float k = -((Direction)blockState.getValue(WallSignBlock.FACING)).toYRot();
			poseStack.mulPose(Vector3f.YP.rotationDegrees(k));
			poseStack.translate(0.0, -0.3125, -0.4375);
			this.stick.visible = false;
		}

		TextureAtlasSprite textureAtlasSprite = this.getSprite(this.getTexture(blockState.getBlock()));
		poseStack.pushPose();
		poseStack.scale(0.6666667F, -0.6666667F, -0.6666667F);
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
		this.sign.render(poseStack, vertexConsumer, 0.0625F, i, j, textureAtlasSprite);
		this.stick.render(poseStack, vertexConsumer, 0.0625F, i, j, textureAtlasSprite);
		poseStack.popPose();
		Font font = this.renderer.getFont();
		float l = 0.010416667F;
		poseStack.translate(0.0, 0.33333334F, 0.046666667F);
		poseStack.scale(0.010416667F, -0.010416667F, 0.010416667F);
		int m = signBlockEntity.getColor().getTextColor();

		for (int n = 0; n < 4; n++) {
			String string = signBlockEntity.getRenderMessage(n, component -> {
				List<Component> list = ComponentRenderUtils.wrapComponents(component, 90, font, false, true);
				return list.isEmpty() ? "" : ((Component)list.get(0)).getColoredString();
			});
			if (string != null) {
				float o = (float)(-font.width(string) / 2);
				font.drawInBatch(string, o, (float)(n * 10 - signBlockEntity.messages.length * 5), m, false, poseStack.getPose(), multiBufferSource, false, 0, i);
				if (n == signBlockEntity.getSelectedLine() && signBlockEntity.getCursorPos() >= 0) {
					int p = font.width(string.substring(0, Math.max(Math.min(signBlockEntity.getCursorPos(), string.length()), 0)));
					int q = font.isBidirectional() ? -1 : 1;
					int r = (p - font.width(string) / 2) * q;
					int s = n * 10 - signBlockEntity.messages.length * 5;
					if (signBlockEntity.isShowCursor()) {
						if (signBlockEntity.getCursorPos() < string.length()) {
							GuiComponent.fill(r, s - 1, r + 1, s + 9, 0xFF000000 | m);
						} else {
							font.drawInBatch("_", (float)r, (float)s, m, false, poseStack.getPose(), multiBufferSource, false, 0, i);
						}
					}

					if (signBlockEntity.getSelectionPos() != signBlockEntity.getCursorPos()) {
						int t = Math.min(signBlockEntity.getCursorPos(), signBlockEntity.getSelectionPos());
						int u = Math.max(signBlockEntity.getCursorPos(), signBlockEntity.getSelectionPos());
						int v = (font.width(string.substring(0, t)) - font.width(string) / 2) * q;
						int w = (font.width(string.substring(0, u)) - font.width(string) / 2) * q;
						RenderSystem.pushMatrix();
						RenderSystem.multMatrix(poseStack.getPose());
						this.renderHighlight(Math.min(v, w), s, Math.max(v, w), s + 9);
						RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
						RenderSystem.popMatrix();
					}
				}
			}
		}

		poseStack.popPose();
	}

	private ResourceLocation getTexture(Block block) {
		if (block == Blocks.OAK_SIGN || block == Blocks.OAK_WALL_SIGN) {
			return ModelBakery.OAK_SIGN_TEXTURE;
		} else if (block == Blocks.SPRUCE_SIGN || block == Blocks.SPRUCE_WALL_SIGN) {
			return ModelBakery.SPRUCE_SIGN_TEXTURE;
		} else if (block == Blocks.BIRCH_SIGN || block == Blocks.BIRCH_WALL_SIGN) {
			return ModelBakery.BIRCH_SIGN_TEXTURE;
		} else if (block == Blocks.ACACIA_SIGN || block == Blocks.ACACIA_WALL_SIGN) {
			return ModelBakery.ACACIA_SIGN_TEXTURE;
		} else if (block == Blocks.JUNGLE_SIGN || block == Blocks.JUNGLE_WALL_SIGN) {
			return ModelBakery.JUNGLE_SIGN_TEXTURE;
		} else {
			return block != Blocks.DARK_OAK_SIGN && block != Blocks.DARK_OAK_WALL_SIGN ? ModelBakery.OAK_SIGN_TEXTURE : ModelBakery.DARK_OAK_SIGN_TEXTURE;
		}
	}

	private void renderHighlight(int i, int j, int k, int l) {
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		RenderSystem.color4f(0.0F, 0.0F, 1.0F, 1.0F);
		RenderSystem.disableTexture();
		RenderSystem.enableColorLogicOp();
		RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION);
		bufferBuilder.vertex((double)i, (double)l, 0.0).endVertex();
		bufferBuilder.vertex((double)k, (double)l, 0.0).endVertex();
		bufferBuilder.vertex((double)k, (double)j, 0.0).endVertex();
		bufferBuilder.vertex((double)i, (double)j, 0.0).endVertex();
		bufferBuilder.end();
		BufferUploader.end(bufferBuilder);
		RenderSystem.disableColorLogicOp();
		RenderSystem.enableTexture();
	}
}
