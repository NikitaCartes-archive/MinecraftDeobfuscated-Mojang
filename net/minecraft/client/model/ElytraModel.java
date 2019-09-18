/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

@Environment(value=EnvType.CLIENT)
public class ElytraModel<T extends LivingEntity>
extends EntityModel<T> {
    private final ModelPart rightWing;
    private final ModelPart leftWing = new ModelPart(this, 22, 0);

    public ElytraModel() {
        this.leftWing.addBox(-10.0f, 0.0f, 0.0f, 10.0f, 20.0f, 2.0f, 1.0f);
        this.rightWing = new ModelPart(this, 22, 0);
        this.rightWing.mirror = true;
        this.rightWing.addBox(0.0f, 0.0f, 0.0f, 10.0f, 20.0f, 2.0f, 1.0f);
    }

    @Override
    public void render(T livingEntity, float f, float g, float h, float i, float j, float k) {
        RenderSystem.disableRescaleNormal();
        RenderSystem.disableCull();
        if (((LivingEntity)livingEntity).isBaby()) {
            RenderSystem.pushMatrix();
            RenderSystem.scalef(0.5f, 0.5f, 0.5f);
            RenderSystem.translatef(0.0f, 1.5f, -0.1f);
            this.leftWing.render(k);
            this.rightWing.render(k);
            RenderSystem.popMatrix();
        } else {
            this.leftWing.render(k);
            this.rightWing.render(k);
        }
    }

    @Override
    public void setupAnim(T livingEntity, float f, float g, float h, float i, float j, float k) {
        super.setupAnim(livingEntity, f, g, h, i, j, k);
        float l = 0.2617994f;
        float m = -0.2617994f;
        float n = 0.0f;
        float o = 0.0f;
        if (((LivingEntity)livingEntity).isFallFlying()) {
            float p = 1.0f;
            Vec3 vec3 = ((Entity)livingEntity).getDeltaMovement();
            if (vec3.y < 0.0) {
                Vec3 vec32 = vec3.normalize();
                p = 1.0f - (float)Math.pow(-vec32.y, 1.5);
            }
            l = p * 0.34906584f + (1.0f - p) * l;
            m = p * -1.5707964f + (1.0f - p) * m;
        } else if (((Entity)livingEntity).isCrouching()) {
            l = 0.6981317f;
            m = -0.7853982f;
            n = 3.0f;
            o = 0.08726646f;
        }
        this.leftWing.x = 5.0f;
        this.leftWing.y = n;
        if (livingEntity instanceof AbstractClientPlayer) {
            AbstractClientPlayer abstractClientPlayer = (AbstractClientPlayer)livingEntity;
            abstractClientPlayer.elytraRotX = (float)((double)abstractClientPlayer.elytraRotX + (double)(l - abstractClientPlayer.elytraRotX) * 0.1);
            abstractClientPlayer.elytraRotY = (float)((double)abstractClientPlayer.elytraRotY + (double)(o - abstractClientPlayer.elytraRotY) * 0.1);
            abstractClientPlayer.elytraRotZ = (float)((double)abstractClientPlayer.elytraRotZ + (double)(m - abstractClientPlayer.elytraRotZ) * 0.1);
            this.leftWing.xRot = abstractClientPlayer.elytraRotX;
            this.leftWing.yRot = abstractClientPlayer.elytraRotY;
            this.leftWing.zRot = abstractClientPlayer.elytraRotZ;
        } else {
            this.leftWing.xRot = l;
            this.leftWing.zRot = m;
            this.leftWing.yRot = o;
        }
        this.rightWing.x = -this.leftWing.x;
        this.rightWing.yRot = -this.leftWing.yRot;
        this.rightWing.y = this.leftWing.y;
        this.rightWing.xRot = this.leftWing.xRot;
        this.rightWing.zRot = -this.leftWing.zRot;
    }

    @Override
    public /* synthetic */ void setupAnim(Entity entity, float f, float g, float h, float i, float j, float k) {
        this.setupAnim((T)((LivingEntity)entity), f, g, h, i, j, k);
    }

    @Override
    public /* synthetic */ void render(Entity entity, float f, float g, float h, float i, float j, float k) {
        this.render((T)((LivingEntity)entity), f, g, h, i, j, k);
    }
}

