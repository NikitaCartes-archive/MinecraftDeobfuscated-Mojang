package net.minecraft.client.renderer.entity;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.scores.Team;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public abstract class LivingEntityRenderer<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements RenderLayerParent<T, M> {
	private static final Logger LOGGER = LogManager.getLogger();
	protected M model;
	protected final List<RenderLayer<T, M>> layers = Lists.<RenderLayer<T, M>>newArrayList();

	public LivingEntityRenderer(EntityRenderDispatcher entityRenderDispatcher, M entityModel, float f) {
		super(entityRenderDispatcher);
		this.model = entityModel;
		this.shadowRadius = f;
	}

	protected final boolean addLayer(RenderLayer<T, M> renderLayer) {
		return this.layers.add(renderLayer);
	}

	@Override
	public M getModel() {
		return this.model;
	}

	public void render(T livingEntity, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource) {
		poseStack.pushPose();
		this.model.attackTime = this.getAttackAnim(livingEntity, h);
		this.model.riding = livingEntity.isPassenger();
		this.model.young = livingEntity.isBaby();
		float i = Mth.rotLerp(h, livingEntity.yBodyRotO, livingEntity.yBodyRot);
		float j = Mth.rotLerp(h, livingEntity.yHeadRotO, livingEntity.yHeadRot);
		float k = j - i;
		if (livingEntity.isPassenger() && livingEntity.getVehicle() instanceof LivingEntity) {
			LivingEntity livingEntity2 = (LivingEntity)livingEntity.getVehicle();
			i = Mth.rotLerp(h, livingEntity2.yBodyRotO, livingEntity2.yBodyRot);
			k = j - i;
			float l = Mth.wrapDegrees(k);
			if (l < -85.0F) {
				l = -85.0F;
			}

			if (l >= 85.0F) {
				l = 85.0F;
			}

			i = j - l;
			if (l * l > 2500.0F) {
				i += l * 0.2F;
			}

			k = j - i;
		}

		float m = Mth.lerp(h, livingEntity.xRotO, livingEntity.xRot);
		if (livingEntity.getPose() == Pose.SLEEPING) {
			Direction direction = livingEntity.getBedOrientation();
			if (direction != null) {
				float n = livingEntity.getEyeHeight(Pose.STANDING) - 0.1F;
				poseStack.translate((double)((float)(-direction.getStepX()) * n), 0.0, (double)((float)(-direction.getStepZ()) * n));
			}
		}

		float lx = this.getBob(livingEntity, h);
		this.setupRotations(livingEntity, poseStack, lx, i, h);
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		this.scale(livingEntity, poseStack, h);
		float n = 0.0625F;
		poseStack.translate(0.0, -1.501F, 0.0);
		float o = 0.0F;
		float p = 0.0F;
		if (!livingEntity.isPassenger() && livingEntity.isAlive()) {
			o = Mth.lerp(h, livingEntity.animationSpeedOld, livingEntity.animationSpeed);
			p = livingEntity.animationPosition - livingEntity.animationSpeed * (1.0F - h);
			if (livingEntity.isBaby()) {
				p *= 3.0F;
			}

			if (o > 1.0F) {
				o = 1.0F;
			}
		}

		this.model.prepareMobModel(livingEntity, p, o, h);
		boolean bl = this.isVisible(livingEntity, false);
		boolean bl2 = !bl && !livingEntity.isInvisibleTo(Minecraft.getInstance().player);
		int q = livingEntity.getLightColor();
		this.model.setupAnim(livingEntity, p, o, lx, k, m, 0.0625F);
		if (bl || bl2) {
			ResourceLocation resourceLocation = this.getTextureLocation(livingEntity);
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(
				bl2 ? RenderType.entityForceTranslucent(resourceLocation) : this.model.renderType(resourceLocation)
			);
			this.model.renderToBuffer(poseStack, vertexConsumer, q, getOverlayCoords(livingEntity, this.getWhiteOverlayProgress(livingEntity, h)), 1.0F, 1.0F, 1.0F);
		}

		if (!livingEntity.isSpectator()) {
			for (RenderLayer<T, M> renderLayer : this.layers) {
				renderLayer.render(poseStack, multiBufferSource, q, livingEntity, p, o, h, lx, k, m, 0.0625F);
			}
		}

		poseStack.popPose();
		super.render(livingEntity, d, e, f, g, h, poseStack, multiBufferSource);
	}

	public static int getOverlayCoords(LivingEntity livingEntity, float f) {
		return OverlayTexture.pack(OverlayTexture.u(f), OverlayTexture.v(livingEntity.hurtTime > 0 || livingEntity.deathTime > 0));
	}

	protected boolean isVisible(T livingEntity, boolean bl) {
		return !livingEntity.isInvisible() || bl;
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

	protected void setupRotations(T livingEntity, PoseStack poseStack, float f, float g, float h) {
		Pose pose = livingEntity.getPose();
		if (pose != Pose.SLEEPING) {
			poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - g));
		}

		if (livingEntity.deathTime > 0) {
			float i = ((float)livingEntity.deathTime + h - 1.0F) / 20.0F * 1.6F;
			i = Mth.sqrt(i);
			if (i > 1.0F) {
				i = 1.0F;
			}

			poseStack.mulPose(Vector3f.ZP.rotationDegrees(i * this.getFlipDegrees(livingEntity)));
		} else if (livingEntity.isAutoSpinAttack()) {
			poseStack.mulPose(Vector3f.XP.rotationDegrees(-90.0F - livingEntity.xRot));
			poseStack.mulPose(Vector3f.YP.rotationDegrees(((float)livingEntity.tickCount + h) * -75.0F));
		} else if (pose == Pose.SLEEPING) {
			Direction direction = livingEntity.getBedOrientation();
			float j = direction != null ? sleepDirectionToRotation(direction) : g;
			poseStack.mulPose(Vector3f.YP.rotationDegrees(j));
			poseStack.mulPose(Vector3f.ZP.rotationDegrees(this.getFlipDegrees(livingEntity)));
			poseStack.mulPose(Vector3f.YP.rotationDegrees(270.0F));
		} else if (livingEntity.hasCustomName() || livingEntity instanceof Player) {
			String string = ChatFormatting.stripFormatting(livingEntity.getName().getString());
			if (("Dinnerbone".equals(string) || "Grumm".equals(string))
				&& (!(livingEntity instanceof Player) || ((Player)livingEntity).isModelPartShown(PlayerModelPart.CAPE))) {
				poseStack.translate(0.0, (double)(livingEntity.getBbHeight() + 0.1F), 0.0);
				poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
			}
		}
	}

	protected float getAttackAnim(T livingEntity, float f) {
		return livingEntity.getAttackAnim(f);
	}

	protected float getBob(T livingEntity, float f) {
		return (float)livingEntity.tickCount + f;
	}

	protected float getFlipDegrees(T livingEntity) {
		return 90.0F;
	}

	protected float getWhiteOverlayProgress(T livingEntity, float f) {
		return 0.0F;
	}

	protected void scale(T livingEntity, PoseStack poseStack, float f) {
	}

	protected boolean shouldShowName(T livingEntity) {
		double d = this.entityRenderDispatcher.distanceToSqr(livingEntity);
		float f = livingEntity.isDiscrete() ? 32.0F : 64.0F;
		if (d >= (double)(f * f)) {
			return false;
		} else {
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
	}
}
