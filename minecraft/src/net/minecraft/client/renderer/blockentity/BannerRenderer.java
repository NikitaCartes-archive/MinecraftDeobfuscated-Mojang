package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
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
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;

@Environment(EnvType.CLIENT)
public class BannerRenderer implements BlockEntityRenderer<BannerBlockEntity> {
	private static final int BANNER_WIDTH = 20;
	private static final int BANNER_HEIGHT = 40;
	private static final int MAX_PATTERNS = 16;
	public static final String FLAG = "flag";
	private static final String POLE = "pole";
	private static final String BAR = "bar";
	private final ModelPart flag;
	private final ModelPart pole;
	private final ModelPart bar;

	public BannerRenderer(BlockEntityRendererProvider.Context context) {
		ModelPart modelPart = context.bakeLayer(ModelLayers.BANNER);
		this.flag = modelPart.getChild("flag");
		this.pole = modelPart.getChild("pole");
		this.bar = modelPart.getChild("bar");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild("flag", CubeListBuilder.create().texOffs(0, 0).addBox(-10.0F, 0.0F, -2.0F, 20.0F, 40.0F, 1.0F), PartPose.ZERO);
		partDefinition.addOrReplaceChild("pole", CubeListBuilder.create().texOffs(44, 0).addBox(-1.0F, -30.0F, -1.0F, 2.0F, 42.0F, 2.0F), PartPose.ZERO);
		partDefinition.addOrReplaceChild("bar", CubeListBuilder.create().texOffs(0, 42).addBox(-10.0F, -32.0F, -1.0F, 20.0F, 2.0F, 2.0F), PartPose.ZERO);
		return LayerDefinition.create(meshDefinition, 64, 64);
	}

	public void render(BannerBlockEntity bannerBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
		float g = 0.6666667F;
		boolean bl = bannerBlockEntity.getLevel() == null;
		poseStack.pushPose();
		long l;
		if (bl) {
			l = 0L;
			poseStack.translate(0.5F, 0.5F, 0.5F);
			this.pole.visible = true;
		} else {
			l = bannerBlockEntity.getLevel().getGameTime();
			BlockState blockState = bannerBlockEntity.getBlockState();
			if (blockState.getBlock() instanceof BannerBlock) {
				poseStack.translate(0.5F, 0.5F, 0.5F);
				float h = -RotationSegment.convertToDegrees((Integer)blockState.getValue(BannerBlock.ROTATION));
				poseStack.mulPose(Axis.YP.rotationDegrees(h));
				this.pole.visible = true;
			} else {
				poseStack.translate(0.5F, -0.16666667F, 0.5F);
				float h = -((Direction)blockState.getValue(WallBannerBlock.FACING)).toYRot();
				poseStack.mulPose(Axis.YP.rotationDegrees(h));
				poseStack.translate(0.0F, -0.3125F, -0.4375F);
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
		renderPatterns(
			poseStack, multiBufferSource, i, j, this.flag, ModelBakery.BANNER_BASE, true, bannerBlockEntity.getBaseColor(), bannerBlockEntity.getPatterns()
		);
		poseStack.popPose();
		poseStack.popPose();
	}

	public static void renderPatterns(
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		int i,
		int j,
		ModelPart modelPart,
		Material material,
		boolean bl,
		DyeColor dyeColor,
		BannerPatternLayers bannerPatternLayers
	) {
		renderPatterns(poseStack, multiBufferSource, i, j, modelPart, material, bl, dyeColor, bannerPatternLayers, false);
	}

	public static void renderPatterns(
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		int i,
		int j,
		ModelPart modelPart,
		Material material,
		boolean bl,
		DyeColor dyeColor,
		BannerPatternLayers bannerPatternLayers,
		boolean bl2
	) {
		modelPart.render(poseStack, material.buffer(multiBufferSource, RenderType::entitySolid, bl2), i, j);
		renderPatternLayer(poseStack, multiBufferSource, i, j, modelPart, bl ? Sheets.BANNER_BASE : Sheets.SHIELD_BASE, dyeColor);

		for (int k = 0; k < 16 && k < bannerPatternLayers.layers().size(); k++) {
			BannerPatternLayers.Layer layer = (BannerPatternLayers.Layer)bannerPatternLayers.layers().get(k);
			Material material2 = bl ? Sheets.getBannerMaterial(layer.pattern()) : Sheets.getShieldMaterial(layer.pattern());
			renderPatternLayer(poseStack, multiBufferSource, i, j, modelPart, material2, layer.color());
		}
	}

	private static void renderPatternLayer(
		PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, ModelPart modelPart, Material material, DyeColor dyeColor
	) {
		float[] fs = dyeColor.getTextureDiffuseColors();
		modelPart.render(poseStack, material.buffer(multiBufferSource, RenderType::entityNoOutline), i, j, fs[0], fs[1], fs[2], 1.0F);
	}
}
