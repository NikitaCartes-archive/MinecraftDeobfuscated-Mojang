/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import net.minecraft.client.model.Model;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
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
    public void render(T livingEntity, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        float n;
        Direction direction;
        poseStack.pushPose();
        ((EntityModel)this.model).attackTime = this.getAttackAnim(livingEntity, h);
        ((EntityModel)this.model).riding = ((Entity)livingEntity).isPassenger();
        ((EntityModel)this.model).young = ((LivingEntity)livingEntity).isBaby();
        float i = Mth.rotLerp(h, ((LivingEntity)livingEntity).yBodyRotO, ((LivingEntity)livingEntity).yBodyRot);
        float j = Mth.rotLerp(h, ((LivingEntity)livingEntity).yHeadRotO, ((LivingEntity)livingEntity).yHeadRot);
        float k = j - i;
        if (((Entity)livingEntity).isPassenger() && ((Entity)livingEntity).getVehicle() instanceof LivingEntity) {
            LivingEntity livingEntity2 = (LivingEntity)((Entity)livingEntity).getVehicle();
            i = Mth.rotLerp(h, livingEntity2.yBodyRotO, livingEntity2.yBodyRot);
            k = j - i;
            float l = Mth.wrapDegrees(k);
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
        if (((Entity)livingEntity).getPose() == Pose.SLEEPING && (direction = ((LivingEntity)livingEntity).getBedOrientation()) != null) {
            n = ((Entity)livingEntity).getEyeHeight(Pose.STANDING) - 0.1f;
            poseStack.translate((float)(-direction.getStepX()) * n, 0.0, (float)(-direction.getStepZ()) * n);
        }
        float l = this.getBob(livingEntity, h);
        this.setupRotations(livingEntity, poseStack, l, i, h);
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        this.scale(livingEntity, poseStack, h);
        n = 0.0625f;
        poseStack.translate(0.0, -1.501f, 0.0);
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
        ((EntityModel)this.model).prepareMobModel(livingEntity, p, o, h);
        boolean bl = this.isVisible(livingEntity, false);
        boolean bl2 = !bl && !((Entity)livingEntity).isInvisibleTo(Minecraft.getInstance().player);
        int q = ((Entity)livingEntity).getLightColor();
        ((EntityModel)this.model).setupAnim(livingEntity, p, o, l, k, m, 0.0625f);
        if (bl || bl2) {
            ResourceLocation resourceLocation = this.getTextureLocation(livingEntity);
            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(bl2 ? RenderType.entityForceTranslucent(resourceLocation) : ((Model)this.model).renderType(resourceLocation));
            ((Model)this.model).renderToBuffer(poseStack, vertexConsumer, q, LivingEntityRenderer.getOverlayCoords(livingEntity, this.getWhiteOverlayProgress(livingEntity, h)), 1.0f, 1.0f, 1.0f);
        }
        if (!((Entity)livingEntity).isSpectator()) {
            for (RenderLayer<T, M> renderLayer : this.layers) {
                renderLayer.render(poseStack, multiBufferSource, q, livingEntity, p, o, h, l, k, m, 0.0625f);
            }
        }
        poseStack.popPose();
        super.render(livingEntity, d, e, f, g, h, poseStack, multiBufferSource);
    }

    public static int getOverlayCoords(LivingEntity livingEntity, float f) {
        return OverlayTexture.pack(OverlayTexture.u(f), OverlayTexture.v(livingEntity.hurtTime > 0 || livingEntity.deathTime > 0));
    }

    protected boolean isVisible(T livingEntity, boolean bl) {
        return !((Entity)livingEntity).isInvisible() || bl;
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

    protected void setupRotations(T livingEntity, PoseStack poseStack, float f, float g, float h) {
        String string;
        Pose pose = ((Entity)livingEntity).getPose();
        if (pose != Pose.SLEEPING) {
            poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0f - g));
        }
        if (((LivingEntity)livingEntity).deathTime > 0) {
            float i = ((float)((LivingEntity)livingEntity).deathTime + h - 1.0f) / 20.0f * 1.6f;
            if ((i = Mth.sqrt(i)) > 1.0f) {
                i = 1.0f;
            }
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(i * this.getFlipDegrees(livingEntity)));
        } else if (((LivingEntity)livingEntity).isAutoSpinAttack()) {
            poseStack.mulPose(Vector3f.XP.rotationDegrees(-90.0f - ((LivingEntity)livingEntity).xRot));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(((float)((LivingEntity)livingEntity).tickCount + h) * -75.0f));
        } else if (pose == Pose.SLEEPING) {
            Direction direction = ((LivingEntity)livingEntity).getBedOrientation();
            float j = direction != null ? LivingEntityRenderer.sleepDirectionToRotation(direction) : g;
            poseStack.mulPose(Vector3f.YP.rotationDegrees(j));
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(this.getFlipDegrees(livingEntity)));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(270.0f));
        } else if ((((Entity)livingEntity).hasCustomName() || livingEntity instanceof Player) && ("Dinnerbone".equals(string = ChatFormatting.stripFormatting(((Entity)livingEntity).getName().getString())) || "Grumm".equals(string)) && (!(livingEntity instanceof Player) || ((Player)livingEntity).isModelPartShown(PlayerModelPart.CAPE))) {
            poseStack.translate(0.0, ((Entity)livingEntity).getBbHeight() + 0.1f, 0.0);
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0f));
        }
    }

    protected float getAttackAnim(T livingEntity, float f) {
        return ((LivingEntity)livingEntity).getAttackAnim(f);
    }

    protected float getBob(T livingEntity, float f) {
        return (float)((LivingEntity)livingEntity).tickCount + f;
    }

    protected float getFlipDegrees(T livingEntity) {
        return 90.0f;
    }

    protected float getWhiteOverlayProgress(T livingEntity, float f) {
        return 0.0f;
    }

    protected void scale(T livingEntity, PoseStack poseStack, float f) {
    }

    @Override
    protected boolean shouldShowName(T livingEntity) {
        boolean bl;
        float f;
        double d = this.entityRenderDispatcher.distanceToSqr((Entity)livingEntity);
        float f2 = f = ((Entity)livingEntity).isDiscrete() ? 32.0f : 64.0f;
        if (d >= (double)(f * f)) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer localPlayer = minecraft.player;
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
        return Minecraft.renderNames() && livingEntity != minecraft.getCameraEntity() && bl && !((Entity)livingEntity).isVehicle();
    }

    @Override
    protected /* synthetic */ boolean shouldShowName(Entity entity) {
        return this.shouldShowName((T)((LivingEntity)entity));
    }
}

