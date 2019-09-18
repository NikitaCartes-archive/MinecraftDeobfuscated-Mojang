package net.minecraft.client.renderer.entity;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.Direction;
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
	protected boolean onlySolidLayers;

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

	public void render(T livingEntity, double d, double e, double f, float g, float h) {
		RenderSystem.pushMatrix();
		RenderSystem.disableCull();
		this.model.attackTime = this.getAttackAnim(livingEntity, h);
		this.model.riding = livingEntity.isPassenger();
		this.model.young = livingEntity.isBaby();

		try {
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
			this.setupPosition(livingEntity, d, e, f);
			float lx = this.getBob(livingEntity, h);
			this.setupRotations(livingEntity, lx, i, h);
			float n = this.setupScale(livingEntity, h);
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

			RenderSystem.enableAlphaTest();
			this.model.prepareMobModel(livingEntity, p, o, h);
			this.model.setupAnim(livingEntity, p, o, lx, k, m, n);
			if (this.solidRender) {
				boolean bl = this.setupSolidState(livingEntity);
				RenderSystem.enableColorMaterial();
				RenderSystem.setupSolidRenderingTextureCombine(this.getTeamColor(livingEntity));
				if (!this.onlySolidLayers) {
					this.renderModel(livingEntity, p, o, lx, k, m, n);
				}

				if (!livingEntity.isSpectator()) {
					this.renderLayers(livingEntity, p, o, h, lx, k, m, n);
				}

				RenderSystem.tearDownSolidRenderingTextureCombine();
				RenderSystem.disableColorMaterial();
				if (bl) {
					this.tearDownSolidState();
				}
			} else {
				boolean blx = this.setupOverlayColor(livingEntity, h);
				this.renderModel(livingEntity, p, o, lx, k, m, n);
				if (blx) {
					RenderSystem.teardownOverlayColor();
				}

				RenderSystem.depthMask(true);
				if (!livingEntity.isSpectator()) {
					this.renderLayers(livingEntity, p, o, h, lx, k, m, n);
				}
			}

			RenderSystem.disableRescaleNormal();
		} catch (Exception var19) {
			LOGGER.error("Couldn't render entity", (Throwable)var19);
		}

		RenderSystem.activeTexture(33985);
		RenderSystem.enableTexture();
		RenderSystem.activeTexture(33984);
		RenderSystem.enableCull();
		RenderSystem.popMatrix();
		super.render(livingEntity, d, e, f, g, h);
	}

	public float setupScale(T livingEntity, float f) {
		RenderSystem.enableRescaleNormal();
		RenderSystem.scalef(-1.0F, -1.0F, 1.0F);
		this.scale(livingEntity, f);
		float g = 0.0625F;
		RenderSystem.translatef(0.0F, -1.501F, 0.0F);
		return 0.0625F;
	}

	protected boolean setupSolidState(T livingEntity) {
		RenderSystem.disableLighting();
		RenderSystem.activeTexture(33985);
		RenderSystem.disableTexture();
		RenderSystem.activeTexture(33984);
		return true;
	}

	protected void tearDownSolidState() {
		RenderSystem.enableLighting();
		RenderSystem.activeTexture(33985);
		RenderSystem.enableTexture();
		RenderSystem.activeTexture(33984);
	}

	protected void renderModel(T livingEntity, float f, float g, float h, float i, float j, float k) {
		boolean bl = this.isVisible(livingEntity);
		boolean bl2 = !bl && !livingEntity.isInvisibleTo(Minecraft.getInstance().player);
		if (bl || bl2) {
			if (!this.bindTexture(livingEntity)) {
				return;
			}

			if (bl2) {
				RenderSystem.setProfile(RenderSystem.Profile.TRANSPARENT_MODEL);
			}

			this.model.render(livingEntity, f, g, h, i, j, k);
			if (bl2) {
				RenderSystem.unsetProfile(RenderSystem.Profile.TRANSPARENT_MODEL);
			}
		}
	}

	protected boolean isVisible(T livingEntity) {
		return !livingEntity.isInvisible() || this.solidRender;
	}

	protected boolean setupOverlayColor(T livingEntity, float f) {
		return this.setupOverlayColor(livingEntity, f, true);
	}

	private boolean setupOverlayColor(T livingEntity, float f, boolean bl) {
		int i = this.getOverlayColor(livingEntity, livingEntity.getBrightness(), f);
		boolean bl2 = (i >> 24 & 0xFF) > 0;
		boolean bl3 = livingEntity.hurtTime > 0 || livingEntity.deathTime > 0;
		if (bl2 || bl3 && bl) {
			RenderSystem.setupOverlayColor(i, bl3);
			return true;
		} else {
			return false;
		}
	}

	protected void setupPosition(T livingEntity, double d, double e, double f) {
		if (livingEntity.getPose() == Pose.SLEEPING) {
			Direction direction = livingEntity.getBedOrientation();
			if (direction != null) {
				float g = livingEntity.getEyeHeight(Pose.STANDING) - 0.1F;
				RenderSystem.translatef((float)d - (float)direction.getStepX() * g, (float)e, (float)f - (float)direction.getStepZ() * g);
				return;
			}
		}

		RenderSystem.translatef((float)d, (float)e, (float)f);
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

	protected void setupRotations(T livingEntity, float f, float g, float h) {
		Pose pose = livingEntity.getPose();
		if (pose != Pose.SLEEPING) {
			RenderSystem.rotatef(180.0F - g, 0.0F, 1.0F, 0.0F);
		}

		if (livingEntity.deathTime > 0) {
			float i = ((float)livingEntity.deathTime + h - 1.0F) / 20.0F * 1.6F;
			i = Mth.sqrt(i);
			if (i > 1.0F) {
				i = 1.0F;
			}

			RenderSystem.rotatef(i * this.getFlipDegrees(livingEntity), 0.0F, 0.0F, 1.0F);
		} else if (livingEntity.isAutoSpinAttack()) {
			RenderSystem.rotatef(-90.0F - livingEntity.xRot, 1.0F, 0.0F, 0.0F);
			RenderSystem.rotatef(((float)livingEntity.tickCount + h) * -75.0F, 0.0F, 1.0F, 0.0F);
		} else if (pose == Pose.SLEEPING) {
			Direction direction = livingEntity.getBedOrientation();
			RenderSystem.rotatef(direction != null ? sleepDirectionToRotation(direction) : g, 0.0F, 1.0F, 0.0F);
			RenderSystem.rotatef(this.getFlipDegrees(livingEntity), 0.0F, 0.0F, 1.0F);
			RenderSystem.rotatef(270.0F, 0.0F, 1.0F, 0.0F);
		} else if (livingEntity.hasCustomName() || livingEntity instanceof Player) {
			String string = ChatFormatting.stripFormatting(livingEntity.getName().getString());
			if (string != null
				&& ("Dinnerbone".equals(string) || "Grumm".equals(string))
				&& (!(livingEntity instanceof Player) || ((Player)livingEntity).isModelPartShown(PlayerModelPart.CAPE))) {
				RenderSystem.translatef(0.0F, livingEntity.getBbHeight() + 0.1F, 0.0F);
				RenderSystem.rotatef(180.0F, 0.0F, 0.0F, 1.0F);
			}
		}
	}

	protected float getAttackAnim(T livingEntity, float f) {
		return livingEntity.getAttackAnim(f);
	}

	protected float getBob(T livingEntity, float f) {
		return (float)livingEntity.tickCount + f;
	}

	protected void renderLayers(T livingEntity, float f, float g, float h, float i, float j, float k, float l) {
		for (RenderLayer<T, M> renderLayer : this.layers) {
			boolean bl = this.setupOverlayColor(livingEntity, h, renderLayer.colorsOnDamage());
			renderLayer.render(livingEntity, f, g, h, i, j, k, l);
			if (bl) {
				RenderSystem.teardownOverlayColor();
			}
		}
	}

	protected float getFlipDegrees(T livingEntity) {
		return 90.0F;
	}

	protected int getOverlayColor(T livingEntity, float f, float g) {
		return 0;
	}

	protected void scale(T livingEntity, float f) {
	}

	public void renderName(T livingEntity, double d, double e, double f) {
		if (this.shouldShowName(livingEntity)) {
			double g = livingEntity.distanceToSqr(this.entityRenderDispatcher.camera.getPosition());
			float h = livingEntity.isDiscrete() ? 32.0F : 64.0F;
			if (!(g >= (double)(h * h))) {
				String string = livingEntity.getDisplayName().getColoredString();
				RenderSystem.defaultAlphaFunc();
				this.renderNameTags(livingEntity, d, e, f, string, g);
			}
		}
	}

	protected boolean shouldShowName(T livingEntity) {
		LocalPlayer localPlayer = Minecraft.getInstance().player;
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

		return Minecraft.renderNames() && livingEntity != this.entityRenderDispatcher.camera.getEntity() && bl && !livingEntity.isVehicle();
	}
}
