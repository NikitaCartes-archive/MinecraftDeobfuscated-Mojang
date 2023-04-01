package net.minecraft.client.renderer.entity.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.math.Axis;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.BeeStingerLayer;
import net.minecraft.client.renderer.entity.layers.BeretLayer;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.Deadmau5EarsLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.MustacheLayer;
import net.minecraft.client.renderer.entity.layers.ParrotOnShoulderLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.SpinAttackEffectLayer;
import net.minecraft.client.renderer.entity.layers.TailLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.voting.rules.Rules;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;

@Environment(EnvType.CLIENT)
public class PlayerRenderer extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
	public PlayerRenderer(EntityRendererProvider.Context context, boolean bl) {
		super(context, new PlayerModel<>(context.bakeLayer(bl ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), bl), 0.5F);
		this.addLayer(
			new HumanoidArmorLayer<>(
				this,
				new PlayerRenderer.PlayerArmorModel(context.bakeLayer(bl ? ModelLayers.PLAYER_SLIM_INNER_ARMOR : ModelLayers.PLAYER_INNER_ARMOR)),
				new PlayerRenderer.PlayerArmorModel(context.bakeLayer(bl ? ModelLayers.PLAYER_SLIM_OUTER_ARMOR : ModelLayers.PLAYER_OUTER_ARMOR)),
				context.getModelManager()
			)
		);
		this.addLayer(new PlayerItemInHandLayer<>(this, context.getItemInHandRenderer()));
		this.addLayer(new ArrowLayer<>(context, this));
		this.addLayer(new Deadmau5EarsLayer(this));
		this.addLayer(new CapeLayer(this));
		this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getItemInHandRenderer()));
		this.addLayer(new ElytraLayer<>(this, context.getModelSet()));
		this.addLayer(new ParrotOnShoulderLayer<>(this, context.getModelSet()));
		this.addLayer(new SpinAttackEffectLayer<>(this, context.getModelSet()));
		this.addLayer(new BeeStingerLayer<>(this));
		this.addLayer(new BeretLayer<>(this, context.getModelSet()));
		this.addLayer(new MustacheLayer<>(this, context.getModelSet()));
		this.addLayer(new TailLayer<>(this, context.getModelSet()));
	}

	public void render(AbstractClientPlayer abstractClientPlayer, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		this.setModelProperties(abstractClientPlayer);
		super.render(abstractClientPlayer, f, g, poseStack, multiBufferSource, i);
	}

	public Vec3 getRenderOffset(AbstractClientPlayer abstractClientPlayer, float f) {
		return abstractClientPlayer.isCrouching() ? new Vec3(0.0, -0.125, 0.0) : super.getRenderOffset(abstractClientPlayer, f);
	}

	private void setModelProperties(AbstractClientPlayer abstractClientPlayer) {
		PlayerModel<AbstractClientPlayer> playerModel = this.getModel();
		playerModel.setModelProperties(abstractClientPlayer);
		if (!abstractClientPlayer.isSpectator()) {
			playerModel.setAllVisible(true);
			playerModel.hat.visible = abstractClientPlayer.isModelPartShown(PlayerModelPart.HAT);
			playerModel.jacket.visible = abstractClientPlayer.isModelPartShown(PlayerModelPart.JACKET);
			playerModel.leftPants.visible = abstractClientPlayer.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
			playerModel.rightPants.visible = abstractClientPlayer.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
			playerModel.leftSleeve.visible = abstractClientPlayer.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
			playerModel.rightSleeve.visible = abstractClientPlayer.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
		}
	}

	public ResourceLocation getTextureLocation(AbstractClientPlayer abstractClientPlayer) {
		return abstractClientPlayer.getSkinTextureLocation();
	}

	protected void scale(AbstractClientPlayer abstractClientPlayer, PoseStack poseStack, float f) {
		float g = 0.9375F;
		poseStack.scale(0.9375F, 0.9375F, 0.9375F);
	}

	protected void renderNameTag(AbstractClientPlayer abstractClientPlayer, Component component, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		double d = this.entityRenderDispatcher.distanceToSqr(abstractClientPlayer);
		if (Rules.PRESIDENT.contains(abstractClientPlayer.getUUID())) {
			component = Component.translatable("rule.president.tag", abstractClientPlayer.getDisplayName());
		}

		poseStack.pushPose();
		if (d < 100.0) {
			Scoreboard scoreboard = abstractClientPlayer.getScoreboard();
			Objective objective = scoreboard.getDisplayObjective(2);
			if (objective != null) {
				Score score = scoreboard.getOrCreatePlayerScore(abstractClientPlayer.getScoreboardName(), objective);
				super.renderNameTag(
					abstractClientPlayer,
					Component.literal(Integer.toString(score.getScore())).append(CommonComponents.SPACE).append(objective.getDisplayName()),
					poseStack,
					multiBufferSource,
					i
				);
				poseStack.translate(0.0F, 9.0F * 1.15F * 0.025F, 0.0F);
			}
		}

		super.renderNameTag(abstractClientPlayer, component, poseStack, multiBufferSource, i);
		poseStack.popPose();
	}

	public void renderRightHand(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, AbstractClientPlayer abstractClientPlayer) {
		this.renderHand(poseStack, multiBufferSource, i, abstractClientPlayer, this.model.rightArm, this.model.rightSleeve);
	}

	public void renderLeftHand(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, AbstractClientPlayer abstractClientPlayer) {
		this.renderHand(poseStack, multiBufferSource, i, abstractClientPlayer, this.model.leftArm, this.model.leftSleeve);
	}

	private void renderHand(
		PoseStack poseStack, MultiBufferSource multiBufferSource, int i, AbstractClientPlayer abstractClientPlayer, ModelPart modelPart, ModelPart modelPart2
	) {
		PlayerModel<AbstractClientPlayer> playerModel = this.getModel();
		this.setModelProperties(abstractClientPlayer);
		playerModel.attackTime = 0.0F;
		playerModel.crouching = false;
		playerModel.swimAmount = 0.0F;
		playerModel.setupAnim(abstractClientPlayer, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
		modelPart.xRot = 0.0F;
		modelPart.render(poseStack, multiBufferSource.getBuffer(RenderType.entitySolid(abstractClientPlayer.getSkinTextureLocation())), i, OverlayTexture.NO_OVERLAY);
		modelPart2.xRot = 0.0F;
		modelPart2.render(
			poseStack, multiBufferSource.getBuffer(RenderType.entityTranslucent(abstractClientPlayer.getSkinTextureLocation())), i, OverlayTexture.NO_OVERLAY
		);
	}

	protected void setupRotations(AbstractClientPlayer abstractClientPlayer, PoseStack poseStack, float f, float g, float h) {
		float i = abstractClientPlayer.getSwimAmount(h);
		if (abstractClientPlayer.isFallFlying()) {
			super.setupRotations(abstractClientPlayer, poseStack, f, g, h);
			float j = (float)abstractClientPlayer.getFallFlyingTicks() + h;
			float k = Mth.clamp(j * j / 100.0F, 0.0F, 1.0F);
			if (!abstractClientPlayer.isAutoSpinAttack()) {
				poseStack.mulPose(Axis.XP.rotationDegrees(k * (-90.0F - abstractClientPlayer.getXRot())));
			}

			Vec3 vec3 = abstractClientPlayer.getViewVector(h);
			Vec3 vec32 = abstractClientPlayer.getDeltaMovementLerped(h);
			double d = vec32.horizontalDistanceSqr();
			double e = vec3.horizontalDistanceSqr();
			if (d > 0.0 && e > 0.0) {
				double l = (vec32.x * vec3.x + vec32.z * vec3.z) / Math.sqrt(d * e);
				double m = vec32.x * vec3.z - vec32.z * vec3.x;
				poseStack.mulPose(Axis.YP.rotation((float)(Math.signum(m) * Math.acos(l))));
			}
		} else if (i > 0.0F) {
			super.setupRotations(abstractClientPlayer, poseStack, f, g, h);
			float jx = abstractClientPlayer.isInWater() ? -90.0F - abstractClientPlayer.getXRot() : -90.0F;
			float kx = Mth.lerp(i, 0.0F, jx);
			poseStack.mulPose(Axis.XP.rotationDegrees(kx));
			if (abstractClientPlayer.isVisuallySwimming()) {
				poseStack.translate(0.0F, -1.0F, 0.3F);
			}
		} else {
			super.setupRotations(abstractClientPlayer, poseStack, f, g, h);
		}
	}

	@Nullable
	protected VertexConsumer getVertexConsumer(
		AbstractClientPlayer abstractClientPlayer, PoseStack poseStack, MultiBufferSource multiBufferSource, boolean bl, boolean bl2, boolean bl3
	) {
		if (Rules.MIDAS_TOUCH.get()) {
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.goldEntityGlint());
			VertexConsumer vertexConsumer2 = super.getVertexConsumer(abstractClientPlayer, poseStack, multiBufferSource, bl, bl2, bl3);
			return vertexConsumer2 == null ? null : VertexMultiConsumer.create(vertexConsumer, vertexConsumer2);
		} else {
			return super.getVertexConsumer(abstractClientPlayer, poseStack, multiBufferSource, bl, bl2, bl3);
		}
	}

	@Environment(EnvType.CLIENT)
	static class PlayerArmorModel<T extends LivingEntity> extends HumanoidArmorModel<T> {
		public PlayerArmorModel(ModelPart modelPart) {
			super(modelPart);
		}

		@Override
		public boolean miniMe() {
			return Rules.MINI_ME_MODE.get();
		}
	}
}
