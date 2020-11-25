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
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
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
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class LivingEntityRenderer<T extends LivingEntity, M extends EntityModel<T>>
extends EntityRenderer<T>
implements RenderLayerParent<T, M> {
    private static final Logger LOGGER = LogManager.getLogger();
    protected M model;
    protected final List<RenderLayer<T, M>> layers = Lists.newArrayList();

    public LivingEntityRenderer(EntityRendererProvider.Context context, M entityModel, float f) {
        super(context);
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
    public void render(T livingEntity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        float n;
        Direction direction;
        poseStack.pushPose();
        ((EntityModel)this.model).attackTime = this.getAttackAnim(livingEntity, g);
        ((EntityModel)this.model).riding = ((Entity)livingEntity).isPassenger();
        ((EntityModel)this.model).young = ((LivingEntity)livingEntity).isBaby();
        float h = Mth.rotLerp(g, ((LivingEntity)livingEntity).yBodyRotO, ((LivingEntity)livingEntity).yBodyRot);
        float j = Mth.rotLerp(g, ((LivingEntity)livingEntity).yHeadRotO, ((LivingEntity)livingEntity).yHeadRot);
        float k = j - h;
        if (((Entity)livingEntity).isPassenger() && ((Entity)livingEntity).getVehicle() instanceof LivingEntity) {
            LivingEntity livingEntity2 = (LivingEntity)((Entity)livingEntity).getVehicle();
            h = Mth.rotLerp(g, livingEntity2.yBodyRotO, livingEntity2.yBodyRot);
            k = j - h;
            float l = Mth.wrapDegrees(k);
            if (l < -85.0f) {
                l = -85.0f;
            }
            if (l >= 85.0f) {
                l = 85.0f;
            }
            h = j - l;
            if (l * l > 2500.0f) {
                h += l * 0.2f;
            }
            k = j - h;
        }
        float m = Mth.lerp(g, ((LivingEntity)livingEntity).xRotO, ((LivingEntity)livingEntity).xRot);
        if (((Entity)livingEntity).getPose() == Pose.SLEEPING && (direction = ((LivingEntity)livingEntity).getBedOrientation()) != null) {
            n = ((Entity)livingEntity).getEyeHeight(Pose.STANDING) - 0.1f;
            poseStack.translate((float)(-direction.getStepX()) * n, 0.0, (float)(-direction.getStepZ()) * n);
        }
        float l = this.getBob(livingEntity, g);
        this.setupRotations(livingEntity, poseStack, l, h, g);
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        this.scale(livingEntity, poseStack, g);
        poseStack.translate(0.0, -1.501f, 0.0);
        n = 0.0f;
        float o = 0.0f;
        if (!((Entity)livingEntity).isPassenger() && ((LivingEntity)livingEntity).isAlive()) {
            n = Mth.lerp(g, ((LivingEntity)livingEntity).animationSpeedOld, ((LivingEntity)livingEntity).animationSpeed);
            o = ((LivingEntity)livingEntity).animationPosition - ((LivingEntity)livingEntity).animationSpeed * (1.0f - g);
            if (((LivingEntity)livingEntity).isBaby()) {
                o *= 3.0f;
            }
            if (n > 1.0f) {
                n = 1.0f;
            }
        }
        ((EntityModel)this.model).prepareMobModel(livingEntity, o, n, g);
        ((EntityModel)this.model).setupAnim(livingEntity, o, n, l, k, m);
        Minecraft minecraft = Minecraft.getInstance();
        boolean bl = this.isBodyVisible(livingEntity);
        boolean bl2 = !bl && !((Entity)livingEntity).isInvisibleTo(minecraft.player);
        boolean bl3 = minecraft.shouldEntityAppearGlowing((Entity)livingEntity);
        RenderType renderType = this.getRenderType(livingEntity, bl, bl2, bl3);
        if (renderType != null) {
            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(renderType);
            int p = LivingEntityRenderer.getOverlayCoords(livingEntity, this.getWhiteOverlayProgress(livingEntity, g));
            ((Model)this.model).renderToBuffer(poseStack, vertexConsumer, i, p, 1.0f, 1.0f, 1.0f, bl2 ? 0.15f : 1.0f);
        }
        if (!((Entity)livingEntity).isSpectator()) {
            for (RenderLayer<T, M> renderLayer : this.layers) {
                renderLayer.render(poseStack, multiBufferSource, i, livingEntity, o, n, g, l, k, m);
            }
        }
        poseStack.popPose();
        super.render(livingEntity, f, g, poseStack, multiBufferSource, i);
    }

    @Nullable
    protected RenderType getRenderType(T livingEntity, boolean bl, boolean bl2, boolean bl3) {
        ResourceLocation resourceLocation = this.getTextureLocation(livingEntity);
        if (bl2) {
            return RenderType.itemEntityTranslucentCull(resourceLocation);
        }
        if (bl) {
            return ((Model)this.model).renderType(resourceLocation);
        }
        if (bl3) {
            return RenderType.outline(resourceLocation);
        }
        return null;
    }

    public static int getOverlayCoords(LivingEntity livingEntity, float f) {
        return OverlayTexture.pack(OverlayTexture.u(f), OverlayTexture.v(livingEntity.hurtTime > 0 || livingEntity.deathTime > 0));
    }

    protected boolean isBodyVisible(T livingEntity) {
        return !((Entity)livingEntity).isInvisible();
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

    protected boolean isShaking(T livingEntity) {
        return false;
    }

    protected void setupRotations(T livingEntity, PoseStack poseStack, float f, float g, float h) {
        String string;
        Pose pose;
        if (this.isShaking(livingEntity)) {
            g += (float)(Math.cos((double)((LivingEntity)livingEntity).tickCount * 3.25) * Math.PI * (double)0.4f);
        }
        if ((pose = ((Entity)livingEntity).getPose()) != Pose.SLEEPING) {
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
}

