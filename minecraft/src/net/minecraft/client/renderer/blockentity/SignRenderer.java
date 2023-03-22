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
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
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
import net.minecraft.util.FastColor;
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
	private static final String STICK = "stick";
	private static final int BLACK_TEXT_OUTLINE_COLOR = -988212;
	private static final int OUTLINE_RENDER_DISTANCE = Mth.square(16);
	private static final float SIZE = 0.6666667F;
	private final Map<WoodType, SignRenderer.SignModel> signModels;
	private final Font font;

	public SignRenderer(BlockEntityRendererProvider.Context context) {
		this.signModels = (Map<WoodType, SignRenderer.SignModel>)WoodType.values()
			.collect(
				ImmutableMap.toImmutableMap(woodType -> woodType, woodType -> new SignRenderer.SignModel(context.bakeLayer(ModelLayers.createSignModelName(woodType))))
			);
		this.font = context.getFont();
	}

	public void render(SignBlockEntity signBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
		BlockState blockState = signBlockEntity.getBlockState();
		SignBlock signBlock = (SignBlock)blockState.getBlock();
		WoodType woodType = SignBlock.getWoodType(signBlock);
		SignRenderer.SignModel signModel = (SignRenderer.SignModel)this.signModels.get(woodType);
		signModel.stick.visible = blockState.getBlock() instanceof StandingSignBlock;
		this.renderSignWithText(signBlockEntity, poseStack, multiBufferSource, i, j, blockState, signBlock, woodType, signModel, 0.6666667F);
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
		Model model,
		float f
	) {
		poseStack.pushPose();
		this.translateSign(poseStack, -signBlock.getYRotationDegrees(blockState), blockState);
		this.renderSign(poseStack, multiBufferSource, i, j, f, woodType, model);
		this.renderSignText(
			signBlockEntity.getBlockPos(),
			signBlockEntity.getFrontText(),
			poseStack,
			multiBufferSource,
			i,
			f,
			this.getTextOffset(f),
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
			f,
			this.getTextOffset(f),
			signBlockEntity.getTextLineHeight(),
			signBlockEntity.getMaxTextLineWidth(),
			false
		);
		poseStack.popPose();
	}

	void translateSign(PoseStack poseStack, float f, BlockState blockState) {
		poseStack.translate(0.5F, 0.5F, 0.5F);
		poseStack.mulPose(Axis.YP.rotationDegrees(f));
		if (!(blockState.getBlock() instanceof StandingSignBlock)) {
			poseStack.translate(0.0F, -0.3125F, -0.4375F);
		}
	}

	void renderSign(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, float f, WoodType woodType, Model model) {
		poseStack.pushPose();
		poseStack.scale(f, -f, -f);
		Material material = this.getSignMaterial(woodType);
		VertexConsumer vertexConsumer = material.buffer(multiBufferSource, model::renderType);
		this.renderSignModel(poseStack, i, j, model, vertexConsumer);
		poseStack.popPose();
	}

	void renderSignModel(PoseStack poseStack, int i, int j, Model model, VertexConsumer vertexConsumer) {
		SignRenderer.SignModel signModel = (SignRenderer.SignModel)model;
		signModel.root.render(poseStack, vertexConsumer, i, j);
	}

	Material getSignMaterial(WoodType woodType) {
		return Sheets.getSignMaterial(woodType);
	}

	void renderSignText(
		BlockPos blockPos, SignText signText, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, float f, Vec3 vec3, int j, int k, boolean bl
	) {
		poseStack.pushPose();
		this.translateSignText(f, poseStack, bl, vec3);
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
			float g = (float)(-this.font.width(formattedCharSequence) / 2);
			if (bl2) {
				this.font.drawInBatch8xOutline(formattedCharSequence, g, (float)(p * j - m), n, l, poseStack.last().pose(), multiBufferSource, o);
			} else {
				this.font
					.drawInBatch(formattedCharSequence, g, (float)(p * j - m), n, false, poseStack.last().pose(), multiBufferSource, Font.DisplayMode.POLYGON_OFFSET, 0, o);
			}
		}

		poseStack.popPose();
	}

	private void translateSignText(float f, PoseStack poseStack, boolean bl, Vec3 vec3) {
		if (!bl) {
			poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
		}

		float g = 0.015625F * f;
		poseStack.translate(vec3.x, vec3.y, vec3.z);
		poseStack.scale(g, -g, g);
	}

	Vec3 getTextOffset(float f) {
		return new Vec3(0.0, (double)(0.5F * f), (double)(0.07F * f));
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

	static int getDarkColor(SignText signText) {
		int i = signText.getColor().getTextColor();
		if (i == DyeColor.BLACK.getTextColor() && signText.hasGlowingText()) {
			return -988212;
		} else {
			double d = 0.4;
			int j = (int)((double)FastColor.ARGB32.red(i) * 0.4);
			int k = (int)((double)FastColor.ARGB32.green(i) * 0.4);
			int l = (int)((double)FastColor.ARGB32.blue(i) * 0.4);
			return FastColor.ARGB32.color(0, j, k, l);
		}
	}

	public static SignRenderer.SignModel createSignModel(EntityModelSet entityModelSet, WoodType woodType) {
		return new SignRenderer.SignModel(entityModelSet.bakeLayer(ModelLayers.createSignModelName(woodType)));
	}

	public static LayerDefinition createSignLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild("sign", CubeListBuilder.create().texOffs(0, 0).addBox(-12.0F, -14.0F, -1.0F, 24.0F, 12.0F, 2.0F), PartPose.ZERO);
		partDefinition.addOrReplaceChild("stick", CubeListBuilder.create().texOffs(0, 14).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 14.0F, 2.0F), PartPose.ZERO);
		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	@Environment(EnvType.CLIENT)
	public static final class SignModel extends Model {
		public final ModelPart root;
		public final ModelPart stick;

		public SignModel(ModelPart modelPart) {
			super(RenderType::entityCutoutNoCull);
			this.root = modelPart;
			this.stick = modelPart.getChild("stick");
		}

		@Override
		public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
			this.root.render(poseStack, vertexConsumer, i, j, f, g, h, k);
		}
	}
}
