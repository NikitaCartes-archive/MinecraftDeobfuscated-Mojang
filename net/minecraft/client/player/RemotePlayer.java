/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.player;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;

@Environment(value=EnvType.CLIENT)
public class RemotePlayer
extends AbstractClientPlayer {
    public RemotePlayer(ClientLevel clientLevel, GameProfile gameProfile) {
        super(clientLevel, gameProfile);
        this.maxUpStep = 1.0f;
        this.noPhysics = true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        double e = this.getBoundingBox().getSize() * 10.0;
        if (Double.isNaN(e)) {
            e = 1.0;
        }
        return d < (e *= 64.0 * RemotePlayer.getViewScale()) * e;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        this.calculateEntityAnimation(this, false);
    }

    @Override
    public void aiStep() {
        if (this.lerpSteps > 0) {
            double d = this.getX() + (this.lerpX - this.getX()) / (double)this.lerpSteps;
            double e = this.getY() + (this.lerpY - this.getY()) / (double)this.lerpSteps;
            double f = this.getZ() + (this.lerpZ - this.getZ()) / (double)this.lerpSteps;
            this.yRot = (float)((double)this.yRot + Mth.wrapDegrees(this.lerpYRot - (double)this.yRot) / (double)this.lerpSteps);
            this.xRot = (float)((double)this.xRot + (this.lerpXRot - (double)this.xRot) / (double)this.lerpSteps);
            --this.lerpSteps;
            this.setPos(d, e, f);
            this.setRot(this.yRot, this.xRot);
        }
        if (this.lerpHeadSteps > 0) {
            this.yHeadRot = (float)((double)this.yHeadRot + Mth.wrapDegrees(this.lyHeadRot - (double)this.yHeadRot) / (double)this.lerpHeadSteps);
            --this.lerpHeadSteps;
        }
        this.oBob = this.bob;
        this.updateSwingTime();
        float g = !this.onGround || this.isDeadOrDying() ? 0.0f : Math.min(0.1f, Mth.sqrt(RemotePlayer.getHorizontalDistanceSqr(this.getDeltaMovement())));
        if (this.onGround || this.isDeadOrDying()) {
            float h = 0.0f;
        } else {
            float h = (float)Math.atan(-this.getDeltaMovement().y * (double)0.2f) * 15.0f;
        }
        this.bob += (g - this.bob) * 0.4f;
        this.level.getProfiler().push("push");
        this.pushEntities();
        this.level.getProfiler().pop();
    }

    @Override
    protected void updatePlayerPose() {
    }

    @Override
    public void sendMessage(Component component, UUID uUID) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!minecraft.isBlocked(uUID)) {
            minecraft.gui.getChat().addMessage(component);
        }
    }
}

