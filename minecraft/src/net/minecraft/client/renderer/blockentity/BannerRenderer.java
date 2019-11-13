package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class BannerRenderer extends BlockEntityRenderer<BannerBlockEntity> {
	private final ModelPart flag = new ModelPart(64, 64, 0, 0);
	private final ModelPart pole;
	private final ModelPart bar;

	public BannerRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
		super(blockEntityRenderDispatcher);
		this.flag.addBox(-10.0F, 0.0F, -2.0F, 20.0F, 40.0F, 1.0F, 0.0F);
		this.pole = new ModelPart(64, 64, 44, 0);
		this.pole.addBox(-1.0F, -30.0F, -1.0F, 2.0F, 42.0F, 2.0F, 0.0F);
		this.bar = new ModelPart(64, 64, 0, 42);
		this.bar.addBox(-10.0F, -32.0F, -1.0F, 20.0F, 2.0F, 2.0F, 0.0F);
	}

	public void render(BannerBlockEntity bannerBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
		if (bannerBlockEntity.getPatterns() != null) {
			float g = 0.6666667F;
			boolean bl = bannerBlockEntity.getLevel() == null;
			poseStack.pushPose();
			long l;
			if (bl) {
				l = 0L;
				poseStack.translate(0.5, 0.5, 0.5);
				this.pole.visible = !bannerBlockEntity.onlyRenderPattern();
			} else {
				l = bannerBlockEntity.getLevel().getGameTime();
				BlockState blockState = bannerBlockEntity.getBlockState();
				if (blockState.getBlock() instanceof BannerBlock) {
					poseStack.translate(0.5, 0.5, 0.5);
					float h = (float)(-(Integer)blockState.getValue(BannerBlock.ROTATION) * 360) / 16.0F;
					poseStack.mulPose(Vector3f.YP.rotationDegrees(h));
					this.pole.visible = true;
				} else {
					poseStack.translate(0.5, -0.16666667F, 0.5);
					float h = -((Direction)blockState.getValue(WallBannerBlock.FACING)).toYRot();
					poseStack.mulPose(Vector3f.YP.rotationDegrees(h));
					poseStack.translate(0.0, -0.3125, -0.4375);
					this.pole.visible = false;
				}
			}

			TextureAtlasSprite textureAtlasSprite = this.getSprite(ModelBakery.BANNER_BASE);
			poseStack.pushPose();
			poseStack.scale(0.6666667F, -0.6666667F, -0.6666667F);
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.blockentitySolid());
			this.pole.render(poseStack, vertexConsumer, i, j, textureAtlasSprite);
			this.bar.render(poseStack, vertexConsumer, i, j, textureAtlasSprite);
			if (bannerBlockEntity.onlyRenderPattern()) {
				this.flag.xRot = 0.0F;
			} else {
				BlockPos blockPos = bannerBlockEntity.getBlockPos();
				float k = (float)((long)(blockPos.getX() * 7 + blockPos.getY() * 9 + blockPos.getZ() * 13) + l) + f;
				this.flag.xRot = (-0.0125F + 0.01F * Mth.cos(k * (float) Math.PI * 0.02F)) * (float) Math.PI;
			}

			this.flag.y = -32.0F;
			this.flag.render(poseStack, vertexConsumer, i, j, textureAtlasSprite);
			renderPatterns(bannerBlockEntity, poseStack, multiBufferSource, i, j, this.flag, true);
			poseStack.popPose();
			poseStack.popPose();
		}
	}

	public static void renderPatterns(
		BannerBlockEntity bannerBlockEntity, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, ModelPart modelPart, boolean bl
	) {
		List<BannerPattern> list = bannerBlockEntity.getPatterns();
		List<DyeColor> list2 = bannerBlockEntity.getColors();
		TextureAtlas textureAtlas = Minecraft.getInstance().getTextureAtlas();
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.blockentityNoOutline());

		for (int k = 0; k < 17 && k < list.size() && k < list2.size(); k++) {
			BannerPattern bannerPattern = (BannerPattern)list.get(k);
			DyeColor dyeColor = (DyeColor)list2.get(k);
			float[] fs = dyeColor.getTextureDiffuseColors();
			modelPart.render(poseStack, vertexConsumer, i, j, textureAtlas.getSprite(bannerPattern.location(bl)), fs[0], fs[1], fs[2]);
		}
	}
}
