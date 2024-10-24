package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class SignRenderer implements BlockEntityRenderer<SignBlockEntity> {
	private static final int BLACK_TEXT_OUTLINE_COLOR = -988212;
	private static final int OUTLINE_RENDER_DISTANCE = Mth.square(16);
	private static final float RENDER_SCALE = 0.6666667F;
	private static final Vec3 TEXT_OFFSET = new Vec3(0.0, 0.33333334F, 0.046666667F);
	private final Map<WoodType, SignRenderer.Models> signModels;
	private final Font font;

	public SignRenderer(BlockEntityRendererProvider.Context context) {
		this.signModels = (Map<WoodType, SignRenderer.Models>)WoodType.values()
			.collect(
				ImmutableMap.toImmutableMap(
					woodType -> woodType,
					woodType -> new SignRenderer.Models(createSignModel(context.getModelSet(), woodType, true), createSignModel(context.getModelSet(), woodType, false))
				)
			);
		this.font = context.getFont();
	}

	public void render(SignBlockEntity signBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
		BlockState blockState = signBlockEntity.getBlockState();
		SignBlock signBlock = (SignBlock)blockState.getBlock();
		WoodType woodType = SignBlock.getWoodType(signBlock);
		SignRenderer.Models models = (SignRenderer.Models)this.signModels.get(woodType);
		Model model = blockState.getBlock() instanceof StandingSignBlock ? models.standing() : models.wall();
		this.renderSignWithText(signBlockEntity, poseStack, multiBufferSource, i, j, blockState, signBlock, woodType, model);
	}

	public float getSignModelRenderScale() {
		return 0.6666667F;
	}

	public float getSignTextRenderScale() {
		return 0.6666667F;
	}

	void renderSignWithText(
		SignBlockEntity signBlockEntity,
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		int i,
		int j,
		BlockState blockState,
		SignBlock signBlock,
		WoodType woodType,
		Model model
	) {
		poseStack.pushPose();
		this.translateSign(poseStack, -signBlock.getYRotationDegrees(blockState), blockState);
		this.renderSign(poseStack, multiBufferSource, i, j, woodType, model);
		this.renderSignText(
			signBlockEntity.getBlockPos(),
			signBlockEntity.getFrontText(),
			poseStack,
			multiBufferSource,
			i,
			signBlockEntity.getTextLineHeight(),
			signBlockEntity.getMaxTextLineWidth(),
			true
		);
		this.renderSignText(
			signBlockEntity.getBlockPos(),
			signBlockEntity.getBackText(),
			poseStack,
			multiBufferSource,
			i,
			signBlockEntity.getTextLineHeight(),
			signBlockEntity.getMaxTextLineWidth(),
			false
		);
		poseStack.popPose();
	}

	void translateSign(PoseStack poseStack, float f, BlockState blockState) {
		poseStack.translate(0.5F, 0.75F * this.getSignModelRenderScale(), 0.5F);
		poseStack.mulPose(Axis.YP.rotationDegrees(f));
		if (!(blockState.getBlock() instanceof StandingSignBlock)) {
			poseStack.translate(0.0F, -0.3125F, -0.4375F);
		}
	}

	void renderSign(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, WoodType woodType, Model model) {
		poseStack.pushPose();
		float f = this.getSignModelRenderScale();
		poseStack.scale(f, -f, -f);
		Material material = this.getSignMaterial(woodType);
		VertexConsumer vertexConsumer = material.buffer(multiBufferSource, model::renderType);
		model.renderToBuffer(poseStack, vertexConsumer, i, j);
		poseStack.popPose();
	}

	Material getSignMaterial(WoodType woodType) {
		return Sheets.getSignMaterial(woodType);
	}

	void renderSignText(BlockPos blockPos, SignText signText, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, int k, boolean bl) {
		poseStack.pushPose();
		this.translateSignText(poseStack, bl, this.getTextOffset());
		int l = getDarkColor(signText);
		int m = 4 * j / 2;
		FormattedCharSequence[] formattedCharSequences = signText.getRenderMessages(Minecraft.getInstance().isTextFilteringEnabled(), component -> {
			List<FormattedCharSequence> list = this.font.split(component, k);
			return list.isEmpty() ? FormattedCharSequence.EMPTY : (FormattedCharSequence)list.get(0);
		});
		int n;
		boolean bl2;
		int o;
		if (signText.hasGlowingText()) {
			n = signText.getColor().getTextColor();
			bl2 = isOutlineVisible(blockPos, n);
			o = 15728880;
		} else {
			n = l;
			bl2 = false;
			o = i;
		}

		for (int p = 0; p < 4; p++) {
			FormattedCharSequence formattedCharSequence = formattedCharSequences[p];
			float f = (float)(-this.font.width(formattedCharSequence) / 2);
			if (bl2) {
				this.font.drawInBatch8xOutline(formattedCharSequence, f, (float)(p * j - m), n, l, poseStack.last().pose(), multiBufferSource, o);
			} else {
				this.font
					.drawInBatch(formattedCharSequence, f, (float)(p * j - m), n, false, poseStack.last().pose(), multiBufferSource, Font.DisplayMode.POLYGON_OFFSET, 0, o);
			}
		}

		poseStack.popPose();
	}

	private void translateSignText(PoseStack poseStack, boolean bl, Vec3 vec3) {
		if (!bl) {
			poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
		}

		float f = 0.015625F * this.getSignTextRenderScale();
		poseStack.translate(vec3);
		poseStack.scale(f, -f, f);
	}

	Vec3 getTextOffset() {
		return TEXT_OFFSET;
	}

	static boolean isOutlineVisible(BlockPos blockPos, int i) {
		if (i == DyeColor.BLACK.getTextColor()) {
			return true;
		} else {
			Minecraft minecraft = Minecraft.getInstance();
			LocalPlayer localPlayer = minecraft.player;
			if (localPlayer != null && minecraft.options.getCameraType().isFirstPerson() && localPlayer.isScoping()) {
				return true;
			} else {
				Entity entity = minecraft.getCameraEntity();
				return entity != null && entity.distanceToSqr(Vec3.atCenterOf(blockPos)) < (double)OUTLINE_RENDER_DISTANCE;
			}
		}
	}

	public static int getDarkColor(SignText signText) {
		int i = signText.getColor().getTextColor();
		if (i == DyeColor.BLACK.getTextColor() && signText.hasGlowingText()) {
			return -988212;
		} else {
			double d = 0.4;
			int j = (int)((double)ARGB.red(i) * 0.4);
			int k = (int)((double)ARGB.green(i) * 0.4);
			int l = (int)((double)ARGB.blue(i) * 0.4);
			return ARGB.color(0, j, k, l);
		}
	}

	public static Model createSignModel(EntityModelSet entityModelSet, WoodType woodType, boolean bl) {
		ModelLayerLocation modelLayerLocation = bl ? ModelLayers.createStandingSignModelName(woodType) : ModelLayers.createWallSignModelName(woodType);
		return new Model.Simple(entityModelSet.bakeLayer(modelLayerLocation), RenderType::entityCutoutNoCull);
	}

	public static LayerDefinition createSignLayer(boolean bl) {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild("sign", CubeListBuilder.create().texOffs(0, 0).addBox(-12.0F, -14.0F, -1.0F, 24.0F, 12.0F, 2.0F), PartPose.ZERO);
		if (bl) {
			partDefinition.addOrReplaceChild("stick", CubeListBuilder.create().texOffs(0, 14).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 14.0F, 2.0F), PartPose.ZERO);
		}

		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	@Environment(EnvType.CLIENT)
	static record Models(Model standing, Model wall) {
	}
}
