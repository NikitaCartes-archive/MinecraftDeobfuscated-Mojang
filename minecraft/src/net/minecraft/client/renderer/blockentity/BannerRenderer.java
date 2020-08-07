package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Vector3f;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
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
	private final ModelPart flag = makeFlag();
	private final ModelPart pole = new ModelPart(64, 64, 44, 0);
	private final ModelPart bar;

	public BannerRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
		super(blockEntityRenderDispatcher);
		this.pole.addBox(-1.0F, -30.0F, -1.0F, 2.0F, 42.0F, 2.0F, 0.0F);
		this.bar = new ModelPart(64, 64, 0, 42);
		this.bar.addBox(-10.0F, -32.0F, -1.0F, 20.0F, 2.0F, 2.0F, 0.0F);
	}

	public static ModelPart makeFlag() {
		ModelPart modelPart = new ModelPart(64, 64, 0, 0);
		modelPart.addBox(-10.0F, 0.0F, -2.0F, 20.0F, 40.0F, 1.0F, 0.0F);
		return modelPart;
	}

	public void render(BannerBlockEntity bannerBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
		List<Pair<BannerPattern, DyeColor>> list = bannerBlockEntity.getPatterns();
		if (list != null) {
			float g = 0.6666667F;
			boolean bl = bannerBlockEntity.getLevel() == null;
			poseStack.pushPose();
			long l;
			if (bl) {
				l = 0L;
				poseStack.translate(0.5, 0.5, 0.5);
				this.pole.visible = true;
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

			poseStack.pushPose();
			poseStack.scale(0.6666667F, -0.6666667F, -0.6666667F);
			VertexConsumer vertexConsumer = ModelBakery.BANNER_BASE.buffer(multiBufferSource, RenderType::entitySolid);
			this.pole.render(poseStack, vertexConsumer, i, j);
			this.bar.render(poseStack, vertexConsumer, i, j);
			BlockPos blockPos = bannerBlockEntity.getBlockPos();
			float k = ((float)Math.floorMod((long)(blockPos.getX() * 7 + blockPos.getY() * 9 + blockPos.getZ() * 13) + l, 100L) + f) / 100.0F;
			this.flag.xRot = (-0.0125F + 0.01F * Mth.cos((float) (Math.PI * 2) * k)) * (float) Math.PI;
			this.flag.y = -32.0F;
			renderPatterns(poseStack, multiBufferSource, i, j, this.flag, ModelBakery.BANNER_BASE, true, list);
			poseStack.popPose();
			poseStack.popPose();
		}
	}

	public static void renderPatterns(
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		int i,
		int j,
		ModelPart modelPart,
		Material material,
		boolean bl,
		List<Pair<BannerPattern, DyeColor>> list
	) {
		renderPatterns(poseStack, multiBufferSource, i, j, modelPart, material, bl, list, false);
	}

	public static void renderPatterns(
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		int i,
		int j,
		ModelPart modelPart,
		Material material,
		boolean bl,
		List<Pair<BannerPattern, DyeColor>> list,
		boolean bl2
	) {
		modelPart.render(poseStack, material.buffer(multiBufferSource, RenderType::entitySolid, bl2), i, j);

		for (int k = 0; k < 17 && k < list.size(); k++) {
			Pair<BannerPattern, DyeColor> pair = (Pair<BannerPattern, DyeColor>)list.get(k);
			float[] fs = pair.getSecond().getTextureDiffuseColors();
			Material material2 = new Material(bl ? Sheets.BANNER_SHEET : Sheets.SHIELD_SHEET, pair.getFirst().location(bl));
			modelPart.render(poseStack, material2.buffer(multiBufferSource, RenderType::entityNoOutline), i, j, fs[0], fs[1], fs[2], 1.0F);
		}
	}
}
