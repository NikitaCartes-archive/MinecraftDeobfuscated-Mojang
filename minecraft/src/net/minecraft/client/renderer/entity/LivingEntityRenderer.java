package net.minecraft.client.renderer.entity;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.Team;

@Environment(EnvType.CLIENT)
public abstract class LivingEntityRenderer<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>>
	extends EntityRenderer<T, S>
	implements RenderLayerParent<S, M> {
	private static final float EYE_BED_OFFSET = 0.1F;
	protected M model;
	protected final ItemRenderer itemRenderer;
	protected final List<RenderLayer<S, M>> layers = Lists.<RenderLayer<S, M>>newArrayList();

	public LivingEntityRenderer(EntityRendererProvider.Context context, M entityModel, float f) {
		super(context);
		this.itemRenderer = context.getItemRenderer();
		this.model = entityModel;
		this.shadowRadius = f;
	}

	protected final boolean addLayer(RenderLayer<S, M> renderLayer) {
		return this.layers.add(renderLayer);
	}

	@Override
	public M getModel() {
		return this.model;
	}

	protected AABB getBoundingBoxForCulling(T livingEntity) {
		AABB aABB = super.getBoundingBoxForCulling(livingEntity);
		if (livingEntity.getItemBySlot(EquipmentSlot.HEAD).is(Items.DRAGON_HEAD)) {
			float f = 0.5F;
			return aABB.inflate(0.5, 0.5, 0.5);
		} else {
			return aABB;
		}
	}

	public void render(S livingEntityRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		poseStack.pushPose();
		if (livingEntityRenderState.hasPose(Pose.SLEEPING)) {
			Direction direction = livingEntityRenderState.bedOrientation;
			if (direction != null) {
				float f = livingEntityRenderState.eyeHeight - 0.1F;
				poseStack.translate((float)(-direction.getStepX()) * f, 0.0F, (float)(-direction.getStepZ()) * f);
			}
		}

		float g = livingEntityRenderState.scale;
		poseStack.scale(g, g, g);
		this.setupRotations(livingEntityRenderState, poseStack, livingEntityRenderState.bodyRot, g);
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		this.scale(livingEntityRenderState, poseStack);
		poseStack.translate(0.0F, -1.501F, 0.0F);
		this.model.setupAnim(livingEntityRenderState);
		boolean bl = this.isBodyVisible(livingEntityRenderState);
		boolean bl2 = !bl && !livingEntityRenderState.isInvisibleToPlayer;
		RenderType renderType = this.getRenderType(livingEntityRenderState, bl, bl2, livingEntityRenderState.appearsGlowing);
		if (renderType != null) {
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(renderType);
			int j = getOverlayCoords(livingEntityRenderState, this.getWhiteOverlayProgress(livingEntityRenderState));
			int k = bl2 ? 654311423 : -1;
			int l = ARGB.multiply(k, this.getModelTint(livingEntityRenderState));
			this.model.renderToBuffer(poseStack, vertexConsumer, i, j, l);
		}

		if (this.shouldRenderLayers(livingEntityRenderState)) {
			for (RenderLayer<S, M> renderLayer : this.layers) {
				renderLayer.render(poseStack, multiBufferSource, i, livingEntityRenderState, livingEntityRenderState.yRot, livingEntityRenderState.xRot);
			}
		}

		poseStack.popPose();
		super.render(livingEntityRenderState, poseStack, multiBufferSource, i);
	}

	protected boolean shouldRenderLayers(S livingEntityRenderState) {
		return true;
	}

	protected int getModelTint(S livingEntityRenderState) {
		return -1;
	}

	@Nullable
	protected RenderType getRenderType(S livingEntityRenderState, boolean bl, boolean bl2, boolean bl3) {
		ResourceLocation resourceLocation = this.getTextureLocation(livingEntityRenderState);
		if (bl2) {
			return RenderType.itemEntityTranslucentCull(resourceLocation);
		} else if (bl) {
			return this.model.renderType(resourceLocation);
		} else {
			return bl3 ? RenderType.outline(resourceLocation) : null;
		}
	}

	public static int getOverlayCoords(LivingEntityRenderState livingEntityRenderState, float f) {
		return OverlayTexture.pack(OverlayTexture.u(f), OverlayTexture.v(livingEntityRenderState.hasRedOverlay));
	}

	protected boolean isBodyVisible(S livingEntityRenderState) {
		return !livingEntityRenderState.isInvisible;
	}

	private static float sleepDirectionToRotation(Direction direction) {
		switch (direction) {
			case SOUTH:
				return 90.0F;
			case WEST:
				return 0.0F;
			case NORTH:
				return 270.0F;
			case EAST:
				return 180.0F;
			default:
				return 0.0F;
		}
	}

	protected boolean isShaking(S livingEntityRenderState) {
		return livingEntityRenderState.isFullyFrozen;
	}

	protected void setupRotations(S livingEntityRenderState, PoseStack poseStack, float f, float g) {
		if (this.isShaking(livingEntityRenderState)) {
			f += (float)(Math.cos((double)((float)Mth.floor(livingEntityRenderState.ageInTicks) * 3.25F)) * Math.PI * 0.4F);
		}

		if (!livingEntityRenderState.hasPose(Pose.SLEEPING)) {
			poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - f));
		}

		if (livingEntityRenderState.deathTime > 0.0F) {
			float h = (livingEntityRenderState.deathTime - 1.0F) / 20.0F * 1.6F;
			h = Mth.sqrt(h);
			if (h > 1.0F) {
				h = 1.0F;
			}

			poseStack.mulPose(Axis.ZP.rotationDegrees(h * this.getFlipDegrees()));
		} else if (livingEntityRenderState.isAutoSpinAttack) {
			poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F - livingEntityRenderState.xRot));
			poseStack.mulPose(Axis.YP.rotationDegrees(livingEntityRenderState.ageInTicks * -75.0F));
		} else if (livingEntityRenderState.hasPose(Pose.SLEEPING)) {
			Direction direction = livingEntityRenderState.bedOrientation;
			float i = direction != null ? sleepDirectionToRotation(direction) : f;
			poseStack.mulPose(Axis.YP.rotationDegrees(i));
			poseStack.mulPose(Axis.ZP.rotationDegrees(this.getFlipDegrees()));
			poseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
		} else if (livingEntityRenderState.isUpsideDown) {
			poseStack.translate(0.0F, (livingEntityRenderState.boundingBoxHeight + 0.1F) / g, 0.0F);
			poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
		}
	}

	protected float getFlipDegrees() {
		return 90.0F;
	}

	protected float getWhiteOverlayProgress(S livingEntityRenderState) {
		return 0.0F;
	}

	protected void scale(S livingEntityRenderState, PoseStack poseStack) {
	}

	protected boolean shouldShowName(T livingEntity, double d) {
		if (livingEntity.isDiscrete()) {
			float f = 32.0F;
			if (d >= 1024.0) {
				return false;
			}
		}

		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer localPlayer = minecraft.player;
		boolean bl = !livingEntity.isInvisibleTo(localPlayer);
		if (livingEntity != localPlayer) {
			Team team = livingEntity.getTeam();
			Team team2 = localPlayer.getTeam();
			if (team != null) {
				Team.Visibility visibility = team.getNameTagVisibility();
				switch (visibility) {
					case ALWAYS:
						return bl;
					case NEVER:
						return false;
					case HIDE_FOR_OTHER_TEAMS:
						return team2 == null ? bl : team.isAlliedTo(team2) && (team.canSeeFriendlyInvisibles() || bl);
					case HIDE_FOR_OWN_TEAM:
						return team2 == null ? bl : !team.isAlliedTo(team2) && bl;
					default:
						return true;
				}
			}
		}

		return Minecraft.renderNames() && livingEntity != minecraft.getCameraEntity() && bl && !livingEntity.isVehicle();
	}

	public static boolean isEntityUpsideDown(LivingEntity livingEntity) {
		if (livingEntity instanceof Player || livingEntity.hasCustomName()) {
			String string = ChatFormatting.stripFormatting(livingEntity.getName().getString());
			if ("Dinnerbone".equals(string) || "Grumm".equals(string)) {
				return !(livingEntity instanceof Player) || ((Player)livingEntity).isModelPartShown(PlayerModelPart.CAPE);
			}
		}

		return false;
	}

	protected float getShadowRadius(S livingEntityRenderState) {
		return super.getShadowRadius(livingEntityRenderState) * livingEntityRenderState.scale;
	}

	public void extractRenderState(T livingEntity, S livingEntityRenderState, float f) {
		super.extractRenderState(livingEntity, livingEntityRenderState, f);
		float g = Mth.rotLerp(f, livingEntity.yHeadRotO, livingEntity.yHeadRot);
		livingEntityRenderState.bodyRot = solveBodyRot(livingEntity, g, f);
		livingEntityRenderState.yRot = Mth.wrapDegrees(g - livingEntityRenderState.bodyRot);
		livingEntityRenderState.xRot = livingEntity.getXRot(f);
		livingEntityRenderState.customName = livingEntity.getCustomName();
		livingEntityRenderState.isUpsideDown = isEntityUpsideDown(livingEntity);
		if (livingEntityRenderState.isUpsideDown) {
			livingEntityRenderState.xRot *= -1.0F;
			livingEntityRenderState.yRot *= -1.0F;
		}

		livingEntityRenderState.walkAnimationPos = livingEntity.walkAnimation.position(f);
		livingEntityRenderState.walkAnimationSpeed = livingEntity.walkAnimation.speed(f);
		if (livingEntity.getVehicle() instanceof LivingEntity livingEntity2) {
			livingEntityRenderState.wornHeadAnimationPos = livingEntity2.walkAnimation.position(f);
		} else {
			livingEntityRenderState.wornHeadAnimationPos = livingEntityRenderState.walkAnimationPos;
		}

		livingEntityRenderState.scale = livingEntity.getScale();
		livingEntityRenderState.ageScale = livingEntity.getAgeScale();
		livingEntityRenderState.pose = livingEntity.getPose();
		livingEntityRenderState.bedOrientation = livingEntity.getBedOrientation();
		if (livingEntityRenderState.bedOrientation != null) {
			livingEntityRenderState.eyeHeight = livingEntity.getEyeHeight(Pose.STANDING);
		}

		livingEntityRenderState.isFullyFrozen = livingEntity.isFullyFrozen();
		livingEntityRenderState.isBaby = livingEntity.isBaby();
		livingEntityRenderState.isInWater = livingEntity.isInWater();
		livingEntityRenderState.isAutoSpinAttack = livingEntity.isAutoSpinAttack();
		livingEntityRenderState.hasRedOverlay = livingEntity.hurtTime > 0 || livingEntity.deathTime > 0;
		livingEntityRenderState.headItem = livingEntity.getItemBySlot(EquipmentSlot.HEAD).copy();
		livingEntityRenderState.headItemModel = this.itemRenderer.resolveItemModel(livingEntityRenderState.headItem, livingEntity, ItemDisplayContext.HEAD);
		livingEntityRenderState.mainArm = livingEntity.getMainArm();
		ItemStack itemStack = livingEntity.getItemHeldByArm(HumanoidArm.RIGHT);
		ItemStack itemStack2 = livingEntity.getItemHeldByArm(HumanoidArm.LEFT);
		livingEntityRenderState.rightHandItem = itemStack.copy();
		livingEntityRenderState.leftHandItem = itemStack2.copy();
		livingEntityRenderState.rightHandItemModel = this.itemRenderer.resolveItemModel(itemStack, livingEntity, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND);
		livingEntityRenderState.leftHandItemModel = this.itemRenderer.resolveItemModel(itemStack2, livingEntity, ItemDisplayContext.THIRD_PERSON_LEFT_HAND);
		livingEntityRenderState.deathTime = livingEntity.deathTime > 0 ? (float)livingEntity.deathTime + f : 0.0F;
		Minecraft minecraft = Minecraft.getInstance();
		livingEntityRenderState.isInvisibleToPlayer = livingEntityRenderState.isInvisible && livingEntity.isInvisibleTo(minecraft.player);
		livingEntityRenderState.appearsGlowing = minecraft.shouldEntityAppearGlowing(livingEntity);
	}

	private static float solveBodyRot(LivingEntity livingEntity, float f, float g) {
		if (livingEntity.getVehicle() instanceof LivingEntity livingEntity2) {
			float h = Mth.rotLerp(g, livingEntity2.yBodyRotO, livingEntity2.yBodyRot);
			float i = 85.0F;
			float j = Mth.clamp(Mth.wrapDegrees(f - h), -85.0F, 85.0F);
			h = f - j;
			if (Math.abs(j) > 50.0F) {
				h += j * 0.2F;
			}

			return h;
		} else {
			return Mth.rotLerp(g, livingEntity.yBodyRotO, livingEntity.yBodyRot);
		}
	}
}
