package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.model.SignModel;
import net.minecraft.client.renderer.RenderType;
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
	private static final ResourceLocation OAK_TEXTURE = new ResourceLocation("textures/entity/signs/oak.png");
	private static final ResourceLocation SPRUCE_TEXTURE = new ResourceLocation("textures/entity/signs/spruce.png");
	private static final ResourceLocation BIRCH_TEXTURE = new ResourceLocation("textures/entity/signs/birch.png");
	private static final ResourceLocation ACACIA_TEXTURE = new ResourceLocation("textures/entity/signs/acacia.png");
	private static final ResourceLocation JUNGLE_TEXTURE = new ResourceLocation("textures/entity/signs/jungle.png");
	private static final ResourceLocation DARK_OAK_TEXTURE = new ResourceLocation("textures/entity/signs/dark_oak.png");
	private final SignModel signModel = new SignModel();

	public void render(SignBlockEntity signBlockEntity, double d, double e, double f, float g, int i, RenderType renderType) {
		BlockState blockState = signBlockEntity.getBlockState();
		RenderSystem.pushMatrix();
		float h = 0.6666667F;
		if (blockState.getBlock() instanceof StandingSignBlock) {
			RenderSystem.translatef((float)d + 0.5F, (float)e + 0.5F, (float)f + 0.5F);
			RenderSystem.rotatef(-((float)((Integer)blockState.getValue(StandingSignBlock.ROTATION) * 360) / 16.0F), 0.0F, 1.0F, 0.0F);
			this.signModel.getStick().visible = true;
		} else {
			RenderSystem.translatef((float)d + 0.5F, (float)e + 0.5F, (float)f + 0.5F);
			RenderSystem.rotatef(-((Direction)blockState.getValue(WallSignBlock.FACING)).toYRot(), 0.0F, 1.0F, 0.0F);
			RenderSystem.translatef(0.0F, -0.3125F, -0.4375F);
			this.signModel.getStick().visible = false;
		}

		if (i >= 0) {
			this.bindTexture((ResourceLocation)BREAKING_LOCATIONS.get(i));
			RenderSystem.matrixMode(5890);
			RenderSystem.pushMatrix();
			RenderSystem.scalef(4.0F, 2.0F, 1.0F);
			RenderSystem.translatef(0.0625F, 0.0625F, 0.0625F);
			RenderSystem.matrixMode(5888);
		} else {
			this.bindTexture(this.getTexture(blockState.getBlock()));
		}

		RenderSystem.enableRescaleNormal();
		RenderSystem.pushMatrix();
		RenderSystem.scalef(0.6666667F, -0.6666667F, -0.6666667F);
		this.signModel.render();
		RenderSystem.popMatrix();
		Font font = this.getFont();
		float j = 0.010416667F;
		RenderSystem.translatef(0.0F, 0.33333334F, 0.046666667F);
		RenderSystem.scalef(0.010416667F, -0.010416667F, 0.010416667F);
		RenderSystem.normal3f(0.0F, 0.0F, -0.010416667F);
		RenderSystem.depthMask(false);
		int k = signBlockEntity.getColor().getTextColor();
		if (i < 0) {
			for (int l = 0; l < 4; l++) {
				String string = signBlockEntity.getRenderMessage(l, component -> {
					List<Component> list = ComponentRenderUtils.wrapComponents(component, 90, font, false, true);
					return list.isEmpty() ? "" : ((Component)list.get(0)).getColoredString();
				});
				if (string != null) {
					font.draw(string, (float)(-font.width(string) / 2), (float)(l * 10 - signBlockEntity.messages.length * 5), k);
					if (l == signBlockEntity.getSelectedLine() && signBlockEntity.getCursorPos() >= 0) {
						int m = font.width(string.substring(0, Math.max(Math.min(signBlockEntity.getCursorPos(), string.length()), 0)));
						int n = font.isBidirectional() ? -1 : 1;
						int o = (m - font.width(string) / 2) * n;
						int p = l * 10 - signBlockEntity.messages.length * 5;
						if (signBlockEntity.isShowCursor()) {
							if (signBlockEntity.getCursorPos() < string.length()) {
								GuiComponent.fill(o, p - 1, o + 1, p + 9, 0xFF000000 | k);
							} else {
								font.draw("_", (float)o, (float)p, k);
							}
						}

						if (signBlockEntity.getSelectionPos() != signBlockEntity.getCursorPos()) {
							int q = Math.min(signBlockEntity.getCursorPos(), signBlockEntity.getSelectionPos());
							int r = Math.max(signBlockEntity.getCursorPos(), signBlockEntity.getSelectionPos());
							int s = (font.width(string.substring(0, q)) - font.width(string) / 2) * n;
							int t = (font.width(string.substring(0, r)) - font.width(string) / 2) * n;
							this.renderHighlight(Math.min(s, t), p, Math.max(s, t), p + 9);
						}
					}
				}
			}
		}

		RenderSystem.depthMask(true);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.popMatrix();
		if (i >= 0) {
			RenderSystem.matrixMode(5890);
			RenderSystem.popMatrix();
			RenderSystem.matrixMode(5888);
		}
	}

	private ResourceLocation getTexture(Block block) {
		if (block == Blocks.OAK_SIGN || block == Blocks.OAK_WALL_SIGN) {
			return OAK_TEXTURE;
		} else if (block == Blocks.SPRUCE_SIGN || block == Blocks.SPRUCE_WALL_SIGN) {
			return SPRUCE_TEXTURE;
		} else if (block == Blocks.BIRCH_SIGN || block == Blocks.BIRCH_WALL_SIGN) {
			return BIRCH_TEXTURE;
		} else if (block == Blocks.ACACIA_SIGN || block == Blocks.ACACIA_WALL_SIGN) {
			return ACACIA_TEXTURE;
		} else if (block == Blocks.JUNGLE_SIGN || block == Blocks.JUNGLE_WALL_SIGN) {
			return JUNGLE_TEXTURE;
		} else {
			return block != Blocks.DARK_OAK_SIGN && block != Blocks.DARK_OAK_WALL_SIGN ? OAK_TEXTURE : DARK_OAK_TEXTURE;
		}
	}

	private void renderHighlight(int i, int j, int k, int l) {
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		RenderSystem.color4f(0.0F, 0.0F, 255.0F, 255.0F);
		RenderSystem.disableTexture();
		RenderSystem.enableColorLogicOp();
		RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION);
		bufferBuilder.vertex((double)i, (double)l, 0.0).endVertex();
		bufferBuilder.vertex((double)k, (double)l, 0.0).endVertex();
		bufferBuilder.vertex((double)k, (double)j, 0.0).endVertex();
		bufferBuilder.vertex((double)i, (double)j, 0.0).endVertex();
		tesselator.end();
		RenderSystem.disableColorLogicOp();
		RenderSystem.enableTexture();
	}
}
