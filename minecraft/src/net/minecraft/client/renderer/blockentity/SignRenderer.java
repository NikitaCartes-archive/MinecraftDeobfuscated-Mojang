package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
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
import net.minecraft.core.Direction;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class SignRenderer implements BlockEntityRenderer<SignBlockEntity> {
	private static final String STICK = "stick";
	private static final int BLACK_TEXT_OUTLINE_COLOR = -988212;
	private static final int OUTLINE_RENDER_DISTANCE = Mth.square(16);
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
		poseStack.pushPose();
		float g = 0.6666667F;
		WoodType woodType = SignBlock.getWoodType(blockState.getBlock());
		SignRenderer.SignModel signModel = (SignRenderer.SignModel)this.signModels.get(woodType);
		if (blockState.getBlock() instanceof StandingSignBlock) {
			poseStack.translate(0.5, 0.5, 0.5);
			float h = -RotationSegment.convertToDegrees((Integer)blockState.getValue(StandingSignBlock.ROTATION));
			poseStack.mulPose(Vector3f.YP.rotationDegrees(h));
			signModel.stick.visible = true;
		} else {
			poseStack.translate(0.5, 0.5, 0.5);
			float h = -((Direction)blockState.getValue(WallSignBlock.FACING)).toYRot();
			poseStack.mulPose(Vector3f.YP.rotationDegrees(h));
			poseStack.translate(0.0, -0.3125, -0.4375);
			signModel.stick.visible = false;
		}

		this.renderSign(poseStack, multiBufferSource, i, j, 0.6666667F, woodType, signModel);
		this.renderSignText(signBlockEntity, poseStack, multiBufferSource, i, 0.6666667F);
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

	void renderSignText(SignBlockEntity signBlockEntity, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, float f) {
		float g = 0.015625F * f;
		Vec3 vec3 = this.getTextOffset(f);
		poseStack.translate(vec3.x, vec3.y, vec3.z);
		poseStack.scale(g, -g, g);
		int j = getDarkColor(signBlockEntity);
		int k = 4 * signBlockEntity.getTextLineHeight() / 2;
		FormattedCharSequence[] formattedCharSequences = signBlockEntity.getRenderMessages(Minecraft.getInstance().isTextFilteringEnabled(), component -> {
			List<FormattedCharSequence> list = this.font.split(component, signBlockEntity.getMaxTextLineWidth());
			return list.isEmpty() ? FormattedCharSequence.EMPTY : (FormattedCharSequence)list.get(0);
		});
		int l;
		boolean bl;
		int m;
		if (signBlockEntity.hasGlowingText()) {
			l = signBlockEntity.getColor().getTextColor();
			bl = isOutlineVisible(signBlockEntity, l);
			m = 15728880;
		} else {
			l = j;
			bl = false;
			m = i;
		}

		for (int n = 0; n < 4; n++) {
			FormattedCharSequence formattedCharSequence = formattedCharSequences[n];
			float h = (float)(-this.font.width(formattedCharSequence) / 2);
			if (bl) {
				this.font
					.drawInBatch8xOutline(formattedCharSequence, h, (float)(n * signBlockEntity.getTextLineHeight() - k), l, j, poseStack.last().pose(), multiBufferSource, m);
			} else {
				this.font
					.drawInBatch(
						formattedCharSequence, h, (float)(n * signBlockEntity.getTextLineHeight() - k), l, false, poseStack.last().pose(), multiBufferSource, false, 0, m
					);
			}
		}

		poseStack.popPose();
	}

	Vec3 getTextOffset(float f) {
		return new Vec3(0.0, (double)(0.5F * f), (double)(0.07F * f));
	}

	static boolean isOutlineVisible(SignBlockEntity signBlockEntity, int i) {
		if (i == DyeColor.BLACK.getTextColor()) {
			return true;
		} else {
			Minecraft minecraft = Minecraft.getInstance();
			LocalPlayer localPlayer = minecraft.player;
			if (localPlayer != null && minecraft.options.getCameraType().isFirstPerson() && localPlayer.isScoping()) {
				return true;
			} else {
				Entity entity = minecraft.getCameraEntity();
				return entity != null && entity.distanceToSqr(Vec3.atCenterOf(signBlockEntity.getBlockPos())) < (double)OUTLINE_RENDER_DISTANCE;
			}
		}
	}

	static int getDarkColor(SignBlockEntity signBlockEntity) {
		int i = signBlockEntity.getColor().getTextColor();
		double d = 0.4;
		int j = (int)((double)NativeImage.getR(i) * 0.4);
		int k = (int)((double)NativeImage.getG(i) * 0.4);
		int l = (int)((double)NativeImage.getB(i) * 0.4);
		return i == DyeColor.BLACK.getTextColor() && signBlockEntity.hasGlowingText() ? -988212 : NativeImage.combine(0, l, k, j);
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
