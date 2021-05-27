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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class SignRenderer implements BlockEntityRenderer<SignBlockEntity> {
	public static final int MAX_LINE_WIDTH = 90;
	private static final int LINE_HEIGHT = 10;
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
		WoodType woodType = getWoodType(blockState.getBlock());
		SignRenderer.SignModel signModel = (SignRenderer.SignModel)this.signModels.get(woodType);
		if (blockState.getBlock() instanceof StandingSignBlock) {
			poseStack.translate(0.5, 0.5, 0.5);
			float h = -((float)((Integer)blockState.getValue(StandingSignBlock.ROTATION) * 360) / 16.0F);
			poseStack.mulPose(Vector3f.YP.rotationDegrees(h));
			signModel.stick.visible = true;
		} else {
			poseStack.translate(0.5, 0.5, 0.5);
			float h = -((Direction)blockState.getValue(WallSignBlock.FACING)).toYRot();
			poseStack.mulPose(Vector3f.YP.rotationDegrees(h));
			poseStack.translate(0.0, -0.3125, -0.4375);
			signModel.stick.visible = false;
		}

		poseStack.pushPose();
		poseStack.scale(0.6666667F, -0.6666667F, -0.6666667F);
		Material material = Sheets.getSignMaterial(woodType);
		VertexConsumer vertexConsumer = material.buffer(multiBufferSource, signModel::renderType);
		signModel.root.render(poseStack, vertexConsumer, i, j);
		poseStack.popPose();
		float k = 0.010416667F;
		poseStack.translate(0.0, 0.33333334F, 0.046666667F);
		poseStack.scale(0.010416667F, -0.010416667F, 0.010416667F);
		int l = getDarkColor(signBlockEntity);
		int m = 20;
		FormattedCharSequence[] formattedCharSequences = signBlockEntity.getRenderMessages(Minecraft.getInstance().isTextFilteringEnabled(), component -> {
			List<FormattedCharSequence> list = this.font.split(component, 90);
			return list.isEmpty() ? FormattedCharSequence.EMPTY : (FormattedCharSequence)list.get(0);
		});
		int n;
		boolean bl;
		int o;
		if (signBlockEntity.hasGlowingText()) {
			n = signBlockEntity.getColor().getTextColor();
			bl = isOutlineVisible(signBlockEntity, n);
			o = 15728880;
		} else {
			n = l;
			bl = false;
			o = i;
		}

		for (int p = 0; p < 4; p++) {
			FormattedCharSequence formattedCharSequence = formattedCharSequences[p];
			float q = (float)(-this.font.width(formattedCharSequence) / 2);
			if (bl) {
				this.font.drawInBatch8xOutline(formattedCharSequence, q, (float)(p * 10 - 20), n, l, poseStack.last().pose(), multiBufferSource, o);
			} else {
				this.font.drawInBatch(formattedCharSequence, q, (float)(p * 10 - 20), n, false, poseStack.last().pose(), multiBufferSource, false, 0, o);
			}
		}

		poseStack.popPose();
	}

	private static boolean isOutlineVisible(SignBlockEntity signBlockEntity, int i) {
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

	private static int getDarkColor(SignBlockEntity signBlockEntity) {
		int i = signBlockEntity.getColor().getTextColor();
		double d = 0.4;
		int j = (int)((double)NativeImage.getR(i) * 0.4);
		int k = (int)((double)NativeImage.getG(i) * 0.4);
		int l = (int)((double)NativeImage.getB(i) * 0.4);
		return i == DyeColor.BLACK.getTextColor() && signBlockEntity.hasGlowingText() ? -988212 : NativeImage.combine(0, l, k, j);
	}

	public static WoodType getWoodType(Block block) {
		WoodType woodType;
		if (block instanceof SignBlock) {
			woodType = ((SignBlock)block).type();
		} else {
			woodType = WoodType.OAK;
		}

		return woodType;
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
