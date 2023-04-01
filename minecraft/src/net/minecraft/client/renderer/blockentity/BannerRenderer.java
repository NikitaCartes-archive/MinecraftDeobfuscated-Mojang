package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import java.util.List;
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
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.voting.rules.Rules;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

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
	private final ModelPart bedFoot;
	private final ModelPart bedHead;

	public BannerRenderer(BlockEntityRendererProvider.Context context) {
		ModelPart modelPart = context.bakeLayer(ModelLayers.BANNER);
		this.flag = modelPart.getChild("flag");
		this.pole = modelPart.getChild("pole");
		this.bar = modelPart.getChild("bar");
		this.bedFoot = context.bakeLayer(ModelLayers.BED_FOOT);
		this.bedHead = context.bakeLayer(ModelLayers.BED_HEAD);
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
		List<Pair<Holder<BannerPattern>, DyeColor>> list = bannerBlockEntity.getPatterns();
		float g = 0.6666667F;
		boolean bl = bannerBlockEntity.getLevel() == null;
		poseStack.pushPose();
		Material material = Sheets.BED_TEXTURES[bannerBlockEntity.getBaseColor().getId()];
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
		float m = (-0.0125F + 0.01F * Mth.cos((float) (Math.PI * 2) * k)) * (float) Math.PI;
		this.flag.xRot = m;
		this.flag.y = -32.0F;
		renderPatterns(poseStack, multiBufferSource, i, j, this.flag, ModelBakery.BANNER_BASE, true, list);
		if (Rules.BEDS_ON_BANNERS.get()) {
			poseStack.pushPose();
			VertexConsumer vertexConsumer2 = material.buffer(multiBufferSource, RenderType::entitySolid);
			poseStack.translate(0.0F, -2.0F, 0.01F);
			poseStack.mulPose(new Quaternionf(new AxisAngle4f(m, 1.0F, 0.0F, 0.0F)));
			poseStack.scale(1.2F, 1.25F, 1.0F);
			poseStack.translate(-0.5, 0.0, -0.125);
			this.bedHead.render(poseStack, vertexConsumer2, i, j);
			poseStack.translate(0.0F, 1.0F, 0.0F);
			this.bedFoot.render(poseStack, vertexConsumer2, i, j);
			poseStack.popPose();
		}

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
		List<Pair<Holder<BannerPattern>, DyeColor>> list
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
		List<Pair<Holder<BannerPattern>, DyeColor>> list,
		boolean bl2
	) {
		boolean bl3 = bl && Rules.BEDS_ON_BANNERS.get();
		if (!bl3) {
			modelPart.render(poseStack, material.buffer(multiBufferSource, RenderType::entitySolid, bl2), i, j);
		}

		for (int k = 0; k < 17 && k < list.size(); k++) {
			Pair<Holder<BannerPattern>, DyeColor> pair = (Pair<Holder<BannerPattern>, DyeColor>)list.get(k);
			float[] fs = pair.getSecond().getTextureDiffuseColors();
			if (!bl3 || !pair.getFirst().is(BannerPatterns.BASE)) {
				pair.getFirst()
					.unwrapKey()
					.map(resourceKey -> bl ? Sheets.getBannerMaterial(resourceKey) : Sheets.getShieldMaterial(resourceKey))
					.ifPresent(materialx -> modelPart.render(poseStack, materialx.buffer(multiBufferSource, RenderType::entityNoOutline), i, j, fs[0], fs[1], fs[2], 1.0F));
			}
		}
	}
}
