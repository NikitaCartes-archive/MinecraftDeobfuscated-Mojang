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

	public void render(T livingEntity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		poseStack.pushPose();
		this.model.attackTime = this.getAttackAnim(livingEntity, g);
		this.model.riding = livingEntity.isPassenger();
		this.model.young = livingEntity.isBaby();
		float h = Mth.rotLerp(g, livingEntity.yBodyRotO, livingEntity.yBodyRot);
		float j = Mth.rotLerp(g, livingEntity.yHeadRotO, livingEntity.yHeadRot);
		float k = j - h;
		if (livingEntity.isPassenger() && livingEntity.getVehicle() instanceof LivingEntity) {
			LivingEntity livingEntity2 = (LivingEntity)livingEntity.getVehicle();
			h = Mth.rotLerp(g, livingEntity2.yBodyRotO, livingEntity2.yBodyRot);
			k = j - h;
			float l = Mth.wrapDegrees(k);
			if (l < -85.0F) {
				l = -85.0F;
			}

			if (l >= 85.0F) {
				l = 85.0F;
			}

			h = j - l;
			if (l * l > 2500.0F) {
				h += l * 0.2F;
			}

			k = j - h;
		}

		float m = Mth.lerp(g, livingEntity.xRotO, livingEntity.xRot);
		if (livingEntity.getPose() == Pose.SLEEPING) {
			Direction direction = livingEntity.getBedOrientation();
			if (direction != null) {
				float n = livingEntity.getEyeHeight(Pose.STANDING) - 0.1F;
				poseStack.translate((double)((float)(-direction.getStepX()) * n), 0.0, (double)((float)(-direction.getStepZ()) * n));
			}
		}

		float lx = this.getBob(livingEntity, g);
		this.setupRotations(livingEntity, poseStack, lx, h, g);
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		this.scale(livingEntity, poseStack, g);
		poseStack.translate(0.0, -1.501F, 0.0);
		float n = 0.0F;
		float o = 0.0F;
		if (!livingEntity.isPassenger() && livingEntity.isAlive()) {
			n = Mth.lerp(g, livingEntity.animationSpeedOld, livingEntity.animationSpeed);
			o = livingEntity.animationPosition - livingEntity.animationSpeed * (1.0F - g);
			if (livingEntity.isBaby()) {
				o *= 3.0F;
			}

			if (n > 1.0F) {
				n = 1.0F;
			}
		}

		this.model.prepareMobModel(livingEntity, o, n, g);
		boolean bl = livingEntity.isGlowing();
		boolean bl2 = this.isVisible(livingEntity, false);
		boolean bl3 = !bl2 && !livingEntity.isInvisibleTo(Minecraft.getInstance().player);
		this.model.setupAnim(livingEntity, o, n, lx, k, m);
		ResourceLocation resourceLocation = this.getTextureLocation(livingEntity);
		RenderType renderType;
		if (bl3) {
			renderType = RenderType.entityTranslucent(resourceLocation);
		} else if (bl2) {
			renderType = this.model.renderType(resourceLocation);
		} else {
			renderType = RenderType.outline(resourceLocation);
		}

		if (bl2 || bl3 || bl) {
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(renderType);
			int p = getOverlayCoords(livingEntity, this.getWhiteOverlayProgress(livingEntity, g));
			this.model.renderToBuffer(poseStack, vertexConsumer, i, p, 1.0F, 1.0F, 1.0F, bl3 ? 0.15F : 1.0F);
		}

		if (!livingEntity.isSpectator()) {
			for (RenderLayer<T, M> renderLayer : this.layers) {
				renderLayer.render(poseStack, multiBufferSource, i, livingEntity, o, n, g, lx, k, m);
			}
		}

		poseStack.popPose();
		super.render(livingEntity, f, g, poseStack, multiBufferSource, i);
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
