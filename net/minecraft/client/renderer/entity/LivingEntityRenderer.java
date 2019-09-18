/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.scores.Team;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public abstract class LivingEntityRenderer<T extends LivingEntity, M extends EntityModel<T>>
extends EntityRenderer<T>
implements RenderLayerParent<T, M> {
    private static final Logger LOGGER = LogManager.getLogger();
    protected M model;
    protected final List<RenderLayer<T, M>> layers = Lists.newArrayList();
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

    @Override
    public void render(T livingEntity, double d, double e, double f, float g, float h) {
        RenderSystem.pushMatrix();
        RenderSystem.disableCull();
        ((EntityModel)this.model).attackTime = this.getAttackAnim(livingEntity, h);
        ((EntityModel)this.model).riding = ((Entity)livingEntity).isPassenger();
        ((EntityModel)this.model).young = ((LivingEntity)livingEntity).isBaby();
        try {
            float l;
            float i = Mth.rotLerp(h, ((LivingEntity)livingEntity).yBodyRotO, ((LivingEntity)livingEntity).yBodyRot);
            float j = Mth.rotLerp(h, ((LivingEntity)livingEntity).yHeadRotO, ((LivingEntity)livingEntity).yHeadRot);
            float k = j - i;
            if (((Entity)livingEntity).isPassenger() && ((Entity)livingEntity).getVehicle() instanceof LivingEntity) {
                LivingEntity livingEntity2 = (LivingEntity)((Entity)livingEntity).getVehicle();
                i = Mth.rotLerp(h, livingEntity2.yBodyRotO, livingEntity2.yBodyRot);
                k = j - i;
                l = Mth.wrapDegrees(k);
                if (l < -85.0f) {
                    l = -85.0f;
                }
                if (l >= 85.0f) {
                    l = 85.0f;
                }
                i = j - l;
                if (l * l > 2500.0f) {
                    i += l * 0.2f;
                }
                k = j - i;
            }
            float m = Mth.lerp(h, ((LivingEntity)livingEntity).xRotO, ((LivingEntity)livingEntity).xRot);
            this.setupPosition(livingEntity, d, e, f);
            l = this.getBob(livingEntity, h);
            this.setupRotations(livingEntity, l, i, h);
            float n = this.setupScale(livingEntity, h);
            float o = 0.0f;
            float p = 0.0f;
            if (!((Entity)livingEntity).isPassenger() && ((LivingEntity)livingEntity).isAlive()) {
                o = Mth.lerp(h, ((LivingEntity)livingEntity).animationSpeedOld, ((LivingEntity)livingEntity).animationSpeed);
                p = ((LivingEntity)livingEntity).animationPosition - ((LivingEntity)livingEntity).animationSpeed * (1.0f - h);
                if (((LivingEntity)livingEntity).isBaby()) {
                    p *= 3.0f;
                }
                if (o > 1.0f) {
                    o = 1.0f;
                }
            }
            RenderSystem.enableAlphaTest();
            ((EntityModel)this.model).prepareMobModel(livingEntity, p, o, h);
            ((EntityModel)this.model).setupAnim(livingEntity, p, o, l, k, m, n);
            if (this.solidRender) {
                boolean bl = this.setupSolidState(livingEntity);
                RenderSystem.enableColorMaterial();
                RenderSystem.setupSolidRenderingTextureCombine(this.getTeamColor(livingEntity));
                if (!this.onlySolidLayers) {
                    this.renderModel(livingEntity, p, o, l, k, m, n);
                }
                if (!((Entity)livingEntity).isSpectator()) {
                    this.renderLayers(livingEntity, p, o, h, l, k, m, n);
                }
                RenderSystem.tearDownSolidRenderingTextureCombine();
                RenderSystem.disableColorMaterial();
                if (bl) {
                    this.tearDownSolidState();
                }
            } else {
                boolean bl = this.setupOverlayColor(livingEntity, h);
                this.renderModel(livingEntity, p, o, l, k, m, n);
                if (bl) {
                    RenderSystem.teardownOverlayColor();
                }
                RenderSystem.depthMask(true);
                if (!((Entity)livingEntity).isSpectator()) {
                    this.renderLayers(livingEntity, p, o, h, l, k, m, n);
                }
            }
            RenderSystem.disableRescaleNormal();
        } catch (Exception exception) {
            LOGGER.error("Couldn't render entity", (Throwable)exception);
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
        RenderSystem.scalef(-1.0f, -1.0f, 1.0f);
        this.scale(livingEntity, f);
        float g = 0.0625f;
        RenderSystem.translatef(0.0f, -1.501f, 0.0f);
        return 0.0625f;
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
        boolean bl2;
        boolean bl = this.isVisible(livingEntity);
        boolean bl3 = bl2 = !bl && !((Entity)livingEntity).isInvisibleTo(Minecraft.getInstance().player);
        if (bl || bl2) {
            if (!this.bindTexture(livingEntity)) {
                return;
            }
            if (bl2) {
                RenderSystem.setProfile(RenderSystem.Profile.TRANSPARENT_MODEL);
            }
            ((EntityModel)this.model).render(livingEntity, f, g, h, i, j, k);
            if (bl2) {
                RenderSystem.unsetProfile(RenderSystem.Profile.TRANSPARENT_MODEL);
            }
        }
    }

    protected boolean isVisible(T livingEntity) {
        return !((Entity)livingEntity).isInvisible() || this.solidRender;
    }

    protected boolean setupOverlayColor(T livingEntity, float f) {
        return this.setupOverlayColor(livingEntity, f, true);
    }

    private boolean setupOverlayColor(T livingEntity, float f, boolean bl) {
        boolean bl3;
        int i = this.getOverlayColor(livingEntity, ((Entity)livingEntity).getBrightness(), f);
        boolean bl2 = (i >> 24 & 0xFF) > 0;
        boolean bl4 = bl3 = ((LivingEntity)livingEntity).hurtTime > 0 || ((LivingEntity)livingEntity).deathTime > 0;
        if (!(bl2 || bl3 && bl)) {
            return false;
        }
        RenderSystem.setupOverlayColor(i, bl3);
        return true;
    }

    protected void setupPosition(T livingEntity, double d, double e, double f) {
        Direction direction;
        if (((Entity)livingEntity).getPose() == Pose.SLEEPING && (direction = ((LivingEntity)livingEntity).getBedOrientation()) != null) {
            float g = ((Entity)livingEntity).getEyeHeight(Pose.STANDING) - 0.1f;
            RenderSystem.translatef((float)d - (float)direction.getStepX() * g, (float)e, (float)f - (float)direction.getStepZ() * g);
            return;
        }
        RenderSystem.translatef((float)d, (float)e, (float)f);
    }

    private static float sleepDirectionToRotation(Direction direction) {
        switch (direction) {
            case SOUTH: {
                return 90.0f;
            }
            case WEST: {
                return 0.0f;
            }
            case NORTH: {
                return 270.0f;
            }
            case EAST: {
                return 180.0f;
            }
        }
        return 0.0f;
    }

    protected void setupRotations(T livingEntity, float f, float g, float h) {
        String string;
        Pose pose = ((Entity)livingEntity).getPose();
        if (pose != Pose.SLEEPING) {
            RenderSystem.rotatef(180.0f - g, 0.0f, 1.0f, 0.0f);
        }
        if (((LivingEntity)livingEntity).deathTime > 0) {
            float i = ((float)((LivingEntity)livingEntity).deathTime + h - 1.0f) / 20.0f * 1.6f;
            if ((i = Mth.sqrt(i)) > 1.0f) {
                i = 1.0f;
            }
            RenderSystem.rotatef(i * this.getFlipDegrees(livingEntity), 0.0f, 0.0f, 1.0f);
        } else if (((LivingEntity)livingEntity).isAutoSpinAttack()) {
            RenderSystem.rotatef(-90.0f - ((LivingEntity)livingEntity).xRot, 1.0f, 0.0f, 0.0f);
            RenderSystem.rotatef(((float)((LivingEntity)livingEntity).tickCount + h) * -75.0f, 0.0f, 1.0f, 0.0f);
        } else if (pose == Pose.SLEEPING) {
            Direction direction = ((LivingEntity)livingEntity).getBedOrientation();
            RenderSystem.rotatef(direction != null ? LivingEntityRenderer.sleepDirectionToRotation(direction) : g, 0.0f, 1.0f, 0.0f);
            RenderSystem.rotatef(this.getFlipDegrees(livingEntity), 0.0f, 0.0f, 1.0f);
            RenderSystem.rotatef(270.0f, 0.0f, 1.0f, 0.0f);
        } else if ((((Entity)livingEntity).hasCustomName() || livingEntity instanceof Player) && (string = ChatFormatting.stripFormatting(((Entity)livingEntity).getName().getString())) != null && ("Dinnerbone".equals(string) || "Grumm".equals(string)) && (!(livingEntity instanceof Player) || ((Player)livingEntity).isModelPartShown(PlayerModelPart.CAPE))) {
            RenderSystem.translatef(0.0f, ((Entity)livingEntity).getBbHeight() + 0.1f, 0.0f);
            RenderSystem.rotatef(180.0f, 0.0f, 0.0f, 1.0f);
        }
    }

    protected float getAttackAnim(T livingEntity, float f) {
        return ((LivingEntity)livingEntity).getAttackAnim(f);
    }

    protected float getBob(T livingEntity, float f) {
        return (float)((LivingEntity)livingEntity).tickCount + f;
    }

    protected void renderLayers(T livingEntity, float f, float g, float h, float i, float j, float k, float l) {
        for (RenderLayer<T, M> renderLayer : this.layers) {
            boolean bl = this.setupOverlayColor(livingEntity, h, renderLayer.colorsOnDamage());
            renderLayer.render(livingEntity, f, g, h, i, j, k, l);
            if (!bl) continue;
            RenderSystem.teardownOverlayColor();
        }
    }

    protected float getFlipDegrees(T livingEntity) {
        return 90.0f;
    }

    protected int getOverlayColor(T livingEntity, float f, float g) {
        return 0;
    }

    protected void scale(T livingEntity, float f) {
    }

    @Override
    public void renderName(T livingEntity, double d, double e, double f) {
        float h;
        if (!this.shouldShowName(livingEntity)) {
            return;
        }
        double g = ((Entity)livingEntity).distanceToSqr(this.entityRenderDispatcher.camera.getPosition());
        float f2 = h = ((Entity)livingEntity).isDiscrete() ? 32.0f : 64.0f;
        if (g >= (double)(h * h)) {
            return;
        }
        String string = ((Entity)livingEntity).getDisplayName().getColoredString();
        RenderSystem.defaultAlphaFunc();
        this.renderNameTags(livingEntity, d, e, f, string, g);
    }

    @Override
    protected boolean shouldShowName(T livingEntity) {
        boolean bl;
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        boolean bl2 = bl = !((Entity)livingEntity).isInvisibleTo(localPlayer);
        if (livingEntity != localPlayer) {
            Team team = ((Entity)livingEntity).getTeam();
            Team team2 = localPlayer.getTeam();
            if (team != null) {
                Team.Visibility visibility = team.getNameTagVisibility();
                switch (visibility) {
                    case ALWAYS: {
                        return bl;
                    }
                    case NEVER: {
                        return false;
                    }
                    case HIDE_FOR_OTHER_TEAMS: {
                        return team2 == null ? bl : team.isAlliedTo(team2) && (team.canSeeFriendlyInvisibles() || bl);
                    }
                    case HIDE_FOR_OWN_TEAM: {
                        return team2 == null ? bl : !team.isAlliedTo(team2) && bl;
                    }
                }
                return true;
            }
        }
        return Minecraft.renderNames() && livingEntity != this.entityRenderDispatcher.camera.getEntity() && bl && !((Entity)livingEntity).isVehicle();
    }

    @Override
    protected /* synthetic */ boolean shouldShowName(Entity entity) {
        return this.shouldShowName((T)((LivingEntity)entity));
    }

    @Override
    public /* synthetic */ void renderName(Entity entity, double d, double e, double f) {
        this.renderName((T)((LivingEntity)entity), d, e, f);
    }
}

