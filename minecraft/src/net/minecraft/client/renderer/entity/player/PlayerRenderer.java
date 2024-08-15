package net.minecraft.client.renderer.entity.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.BeeStingerLayer;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.Deadmau5EarsLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ParrotOnShoulderLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.SpinAttackEffectLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.Scoreboard;

@Environment(EnvType.CLIENT)
public class PlayerRenderer extends LivingEntityRenderer<AbstractClientPlayer, PlayerRenderState, PlayerModel> {
	public PlayerRenderer(EntityRendererProvider.Context context, boolean bl) {
		super(context, new PlayerModel(context.bakeLayer(bl ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), bl), 0.5F);
		this.addLayer(
			new HumanoidArmorLayer<>(
				this,
				new HumanoidArmorModel(context.bakeLayer(bl ? ModelLayers.PLAYER_SLIM_INNER_ARMOR : ModelLayers.PLAYER_INNER_ARMOR)),
				new HumanoidArmorModel(context.bakeLayer(bl ? ModelLayers.PLAYER_SLIM_OUTER_ARMOR : ModelLayers.PLAYER_OUTER_ARMOR)),
				context.getModelManager()
			)
		);
		this.addLayer(new PlayerItemInHandLayer<>(this, context.getItemRenderer()));
		this.addLayer(new ArrowLayer<>(this, context));
		this.addLayer(new Deadmau5EarsLayer(this, context.getModelSet()));
		this.addLayer(new CapeLayer(this, context.getModelSet()));
		this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getItemRenderer()));
		this.addLayer(new ElytraLayer<>(this, context.getModelSet()));
		this.addLayer(new ParrotOnShoulderLayer(this, context.getModelSet()));
		this.addLayer(new SpinAttackEffectLayer(this, context.getModelSet()));
		this.addLayer(new BeeStingerLayer<>(this, context));
	}

	protected boolean shouldRenderLayers(PlayerRenderState playerRenderState) {
		return !playerRenderState.isSpectator;
	}

	public Vec3 getRenderOffset(PlayerRenderState playerRenderState) {
		Vec3 vec3 = super.getRenderOffset(playerRenderState);
		return playerRenderState.isCrouching ? vec3.add(0.0, (double)(playerRenderState.scale * -2.0F) / 16.0, 0.0) : vec3;
	}

	public static HumanoidModel.ArmPose getArmPose(PlayerRenderState playerRenderState, PlayerRenderState.HandState handState, InteractionHand interactionHand) {
		if (handState.isEmpty) {
			return HumanoidModel.ArmPose.EMPTY;
		} else {
			if (playerRenderState.useItemHand == interactionHand && playerRenderState.useItemRemainingTicks > 0) {
				UseAnim useAnim = handState.useAnimation;
				if (useAnim == UseAnim.BLOCK) {
					return HumanoidModel.ArmPose.BLOCK;
				}

				if (useAnim == UseAnim.BOW) {
					return HumanoidModel.ArmPose.BOW_AND_ARROW;
				}

				if (useAnim == UseAnim.SPEAR) {
					return HumanoidModel.ArmPose.THROW_SPEAR;
				}

				if (useAnim == UseAnim.CROSSBOW) {
					return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
				}

				if (useAnim == UseAnim.SPYGLASS) {
					return HumanoidModel.ArmPose.SPYGLASS;
				}

				if (useAnim == UseAnim.TOOT_HORN) {
					return HumanoidModel.ArmPose.TOOT_HORN;
				}

				if (useAnim == UseAnim.BRUSH) {
					return HumanoidModel.ArmPose.BRUSH;
				}
			} else if (!playerRenderState.swinging && handState.holdsChargedCrossbow) {
				return HumanoidModel.ArmPose.CROSSBOW_HOLD;
			}

			return HumanoidModel.ArmPose.ITEM;
		}
	}

	public ResourceLocation getTextureLocation(PlayerRenderState playerRenderState) {
		return playerRenderState.skin.texture();
	}

	protected void scale(PlayerRenderState playerRenderState, PoseStack poseStack) {
		float f = 0.9375F;
		poseStack.scale(0.9375F, 0.9375F, 0.9375F);
	}

	protected void renderNameTag(PlayerRenderState playerRenderState, Component component, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		if (playerRenderState.scoreText != null) {
			poseStack.pushPose();
			super.renderNameTag(playerRenderState, playerRenderState.scoreText, poseStack, multiBufferSource, i);
			poseStack.translate(0.0F, 9.0F * 1.15F * 0.025F, 0.0F);
			poseStack.popPose();
		}

		super.renderNameTag(playerRenderState, component, poseStack, multiBufferSource, i);
	}

	public PlayerRenderState createRenderState() {
		return new PlayerRenderState();
	}

	public void extractRenderState(AbstractClientPlayer abstractClientPlayer, PlayerRenderState playerRenderState, float f) {
		super.extractRenderState(abstractClientPlayer, playerRenderState, f);
		HumanoidMobRenderer.extractHumanoidRenderState(abstractClientPlayer, playerRenderState, f);
		playerRenderState.skin = abstractClientPlayer.getSkin();
		playerRenderState.arrowCount = abstractClientPlayer.getArrowCount();
		playerRenderState.stingerCount = abstractClientPlayer.getStingerCount();
		playerRenderState.useItemRemainingTicks = abstractClientPlayer.getUseItemRemainingTicks();
		playerRenderState.swinging = abstractClientPlayer.swinging;
		playerRenderState.isVisuallySwimming = abstractClientPlayer.isVisuallySwimming();
		playerRenderState.isSpectator = abstractClientPlayer.isSpectator();
		playerRenderState.showHat = abstractClientPlayer.isModelPartShown(PlayerModelPart.HAT);
		playerRenderState.showJacket = abstractClientPlayer.isModelPartShown(PlayerModelPart.JACKET);
		playerRenderState.showLeftPants = abstractClientPlayer.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
		playerRenderState.showRightPants = abstractClientPlayer.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
		playerRenderState.showLeftSleeve = abstractClientPlayer.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
		playerRenderState.showRightSleeve = abstractClientPlayer.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
		playerRenderState.showCape = abstractClientPlayer.isModelPartShown(PlayerModelPart.CAPE);
		extractFlightData(abstractClientPlayer, playerRenderState, f);
		this.extractHandState(abstractClientPlayer, playerRenderState.mainHandState, InteractionHand.MAIN_HAND);
		this.extractHandState(abstractClientPlayer, playerRenderState.offhandState, InteractionHand.OFF_HAND);
		extractCapeState(abstractClientPlayer, playerRenderState, f);
		if (playerRenderState.distanceToCameraSq < 100.0) {
			Scoreboard scoreboard = abstractClientPlayer.getScoreboard();
			Objective objective = scoreboard.getDisplayObjective(DisplaySlot.BELOW_NAME);
			if (objective != null) {
				ReadOnlyScoreInfo readOnlyScoreInfo = scoreboard.getPlayerScoreInfo(abstractClientPlayer, objective);
				Component component = ReadOnlyScoreInfo.safeFormatValue(readOnlyScoreInfo, objective.numberFormatOrDefault(StyledFormat.NO_STYLE));
				playerRenderState.scoreText = Component.empty().append(component).append(CommonComponents.SPACE).append(objective.getDisplayName());
			} else {
				playerRenderState.scoreText = null;
			}
		} else {
			playerRenderState.scoreText = null;
		}

		playerRenderState.parrotOnLeftShoulder = getParrotOnShoulder(abstractClientPlayer, true);
		playerRenderState.parrotOnRightShoulder = getParrotOnShoulder(abstractClientPlayer, false);
		playerRenderState.id = abstractClientPlayer.getId();
		playerRenderState.name = abstractClientPlayer.getGameProfile().getName();
	}

	private static void extractFlightData(AbstractClientPlayer abstractClientPlayer, PlayerRenderState playerRenderState, float f) {
		playerRenderState.fallFlyingTimeInTicks = (float)abstractClientPlayer.getFallFlyingTicks() + f;
		Vec3 vec3 = abstractClientPlayer.getViewVector(f);
		Vec3 vec32 = abstractClientPlayer.getDeltaMovementLerped(f);
		double d = vec32.horizontalDistanceSqr();
		double e = vec3.horizontalDistanceSqr();
		if (d > 0.0 && e > 0.0) {
			playerRenderState.shouldApplyFlyingYRot = true;
			double g = (vec32.x * vec3.x + vec32.z * vec3.z) / Math.sqrt(d * e);
			double h = vec32.x * vec3.z - vec32.z * vec3.x;
			playerRenderState.flyingYRot = (float)(Math.signum(h) * Math.acos(g));
		} else {
			playerRenderState.shouldApplyFlyingYRot = false;
			playerRenderState.flyingYRot = 0.0F;
		}
	}

	private void extractHandState(AbstractClientPlayer abstractClientPlayer, PlayerRenderState.HandState handState, InteractionHand interactionHand) {
		ItemStack itemStack = abstractClientPlayer.getItemInHand(interactionHand);
		handState.isEmpty = itemStack.isEmpty();
		handState.useAnimation = !itemStack.isEmpty() ? itemStack.getUseAnimation() : null;
		handState.holdsChargedCrossbow = itemStack.is(Items.CROSSBOW) && CrossbowItem.isCharged(itemStack);
	}

	private static void extractCapeState(AbstractClientPlayer abstractClientPlayer, PlayerRenderState playerRenderState, float f) {
		double d = Mth.lerp((double)f, abstractClientPlayer.xCloakO, abstractClientPlayer.xCloak)
			- Mth.lerp((double)f, abstractClientPlayer.xo, abstractClientPlayer.getX());
		double e = Mth.lerp((double)f, abstractClientPlayer.yCloakO, abstractClientPlayer.yCloak)
			- Mth.lerp((double)f, abstractClientPlayer.yo, abstractClientPlayer.getY());
		double g = Mth.lerp((double)f, abstractClientPlayer.zCloakO, abstractClientPlayer.zCloak)
			- Mth.lerp((double)f, abstractClientPlayer.zo, abstractClientPlayer.getZ());
		float h = Mth.rotLerp(f, abstractClientPlayer.yBodyRotO, abstractClientPlayer.yBodyRot);
		double i = (double)Mth.sin(h * (float) (Math.PI / 180.0));
		double j = (double)(-Mth.cos(h * (float) (Math.PI / 180.0)));
		playerRenderState.capeFlap = (float)e * 10.0F;
		playerRenderState.capeFlap = Mth.clamp(playerRenderState.capeFlap, -6.0F, 32.0F);
		playerRenderState.capeLean = (float)(d * i + g * j) * 100.0F;
		playerRenderState.capeLean = Mth.clamp(playerRenderState.capeLean, 0.0F, 150.0F);
		playerRenderState.capeLean2 = (float)(d * j - g * i) * 100.0F;
		playerRenderState.capeLean2 = Mth.clamp(playerRenderState.capeLean2, -20.0F, 20.0F);
		float k = Mth.lerp(f, abstractClientPlayer.oBob, abstractClientPlayer.bob);
		float l = Mth.lerp(f, abstractClientPlayer.walkDistO, abstractClientPlayer.walkDist);
		playerRenderState.capeFlap = playerRenderState.capeFlap + Mth.sin(l * 6.0F) * 32.0F * k;
	}

	@Nullable
	private static Parrot.Variant getParrotOnShoulder(AbstractClientPlayer abstractClientPlayer, boolean bl) {
		CompoundTag compoundTag = bl ? abstractClientPlayer.getShoulderEntityLeft() : abstractClientPlayer.getShoulderEntityRight();
		return EntityType.byString(compoundTag.getString("id")).filter(entityType -> entityType == EntityType.PARROT).isPresent()
			? Parrot.Variant.byId(compoundTag.getInt("Variant"))
			: null;
	}

	public void renderRightHand(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, ResourceLocation resourceLocation) {
		this.renderHand(poseStack, multiBufferSource, i, resourceLocation, this.model.rightArm);
	}

	public void renderLeftHand(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, ResourceLocation resourceLocation) {
		this.renderHand(poseStack, multiBufferSource, i, resourceLocation, this.model.leftArm);
	}

	private void renderHand(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, ResourceLocation resourceLocation, ModelPart modelPart) {
		PlayerModel playerModel = this.getModel();
		playerModel.leftArm.resetPose();
		playerModel.rightArm.resetPose();
		playerModel.leftArm.zRot = -0.1F;
		playerModel.rightArm.zRot = 0.1F;
		modelPart.render(poseStack, multiBufferSource.getBuffer(RenderType.entityTranslucent(resourceLocation)), i, OverlayTexture.NO_OVERLAY);
	}

	protected void setupRotations(PlayerRenderState playerRenderState, PoseStack poseStack, float f, float g) {
		float h = playerRenderState.swimAmount;
		float i = playerRenderState.xRot;
		if (playerRenderState.isFallFlying) {
			super.setupRotations(playerRenderState, poseStack, f, g);
			float j = Mth.clamp(playerRenderState.fallFlyingTimeInTicks * playerRenderState.fallFlyingTimeInTicks / 100.0F, 0.0F, 1.0F);
			if (!playerRenderState.isAutoSpinAttack) {
				poseStack.mulPose(Axis.XP.rotationDegrees(j * (-90.0F - i)));
			}

			if (playerRenderState.shouldApplyFlyingYRot) {
				poseStack.mulPose(Axis.YP.rotation(playerRenderState.flyingYRot));
			}
		} else if (h > 0.0F) {
			super.setupRotations(playerRenderState, poseStack, f, g);
			float jx = playerRenderState.isInWater ? -90.0F - i : -90.0F;
			float k = Mth.lerp(h, 0.0F, jx);
			poseStack.mulPose(Axis.XP.rotationDegrees(k));
			if (playerRenderState.isVisuallySwimming) {
				poseStack.translate(0.0F, -1.0F, 0.3F);
			}
		} else {
			super.setupRotations(playerRenderState, poseStack, f, g);
		}
	}
}
