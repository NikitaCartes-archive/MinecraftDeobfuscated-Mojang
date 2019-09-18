/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

@Environment(value=EnvType.CLIENT)
public class HorseModel<T extends AbstractHorse>
extends EntityModel<T> {
    protected final ModelPart body;
    protected final ModelPart headParts;
    private final ModelPart leg1A;
    private final ModelPart leg2A;
    private final ModelPart leg3A;
    private final ModelPart leg4A;
    private final ModelPart tail;
    private final ModelPart[] saddleParts;
    private final ModelPart[] ridingParts;

    public HorseModel(float f) {
        this.texWidth = 64;
        this.texHeight = 64;
        this.body = new ModelPart(this, 0, 32);
        this.body.addBox(-5.0f, -8.0f, -17.0f, 10.0f, 10.0f, 22.0f, 0.05f);
        this.body.setPos(0.0f, 11.0f, 5.0f);
        this.headParts = new ModelPart(this, 0, 35);
        this.headParts.addBox(-2.05f, -6.0f, -2.0f, 4.0f, 12.0f, 7.0f);
        this.headParts.xRot = 0.5235988f;
        ModelPart modelPart = new ModelPart(this, 0, 13);
        modelPart.addBox(-3.0f, -11.0f, -2.0f, 6.0f, 5.0f, 7.0f, f);
        ModelPart modelPart2 = new ModelPart(this, 56, 36);
        modelPart2.addBox(-1.0f, -11.0f, 5.01f, 2.0f, 16.0f, 2.0f, f);
        ModelPart modelPart3 = new ModelPart(this, 0, 25);
        modelPart3.addBox(-2.0f, -11.0f, -7.0f, 4.0f, 5.0f, 5.0f, f);
        this.headParts.addChild(modelPart);
        this.headParts.addChild(modelPart2);
        this.headParts.addChild(modelPart3);
        this.addEarModels(this.headParts);
        this.leg1A = new ModelPart(this, 48, 21);
        this.leg1A.mirror = true;
        this.leg1A.addBox(-3.0f, -1.01f, -1.0f, 4.0f, 11.0f, 4.0f, f);
        this.leg1A.setPos(4.0f, 14.0f, 7.0f);
        this.leg2A = new ModelPart(this, 48, 21);
        this.leg2A.addBox(-1.0f, -1.01f, -1.0f, 4.0f, 11.0f, 4.0f, f);
        this.leg2A.setPos(-4.0f, 14.0f, 7.0f);
        this.leg3A = new ModelPart(this, 48, 21);
        this.leg3A.mirror = true;
        this.leg3A.addBox(-3.0f, -1.01f, -1.9f, 4.0f, 11.0f, 4.0f, f);
        this.leg3A.setPos(4.0f, 6.0f, -12.0f);
        this.leg4A = new ModelPart(this, 48, 21);
        this.leg4A.addBox(-1.0f, -1.01f, -1.9f, 4.0f, 11.0f, 4.0f, f);
        this.leg4A.setPos(-4.0f, 6.0f, -12.0f);
        this.tail = new ModelPart(this, 42, 36);
        this.tail.addBox(-1.5f, 0.0f, 0.0f, 3.0f, 14.0f, 4.0f, f);
        this.tail.setPos(0.0f, -5.0f, 2.0f);
        this.tail.xRot = 0.5235988f;
        this.body.addChild(this.tail);
        ModelPart modelPart4 = new ModelPart(this, 26, 0);
        modelPart4.addBox(-5.0f, -8.0f, -9.0f, 10.0f, 9.0f, 9.0f, 0.5f);
        this.body.addChild(modelPart4);
        ModelPart modelPart5 = new ModelPart(this, 29, 5);
        modelPart5.addBox(2.0f, -9.0f, -6.0f, 1.0f, 2.0f, 2.0f, f);
        this.headParts.addChild(modelPart5);
        ModelPart modelPart6 = new ModelPart(this, 29, 5);
        modelPart6.addBox(-3.0f, -9.0f, -6.0f, 1.0f, 2.0f, 2.0f, f);
        this.headParts.addChild(modelPart6);
        ModelPart modelPart7 = new ModelPart(this, 32, 2);
        modelPart7.addBox(3.1f, -6.0f, -8.0f, 0.0f, 3.0f, 16.0f, f);
        modelPart7.xRot = -0.5235988f;
        this.headParts.addChild(modelPart7);
        ModelPart modelPart8 = new ModelPart(this, 32, 2);
        modelPart8.addBox(-3.1f, -6.0f, -8.0f, 0.0f, 3.0f, 16.0f, f);
        modelPart8.xRot = -0.5235988f;
        this.headParts.addChild(modelPart8);
        ModelPart modelPart9 = new ModelPart(this, 1, 1);
        modelPart9.addBox(-3.0f, -11.0f, -1.9f, 6.0f, 5.0f, 6.0f, 0.2f);
        this.headParts.addChild(modelPart9);
        ModelPart modelPart10 = new ModelPart(this, 19, 0);
        modelPart10.addBox(-2.0f, -11.0f, -4.0f, 4.0f, 5.0f, 2.0f, 0.2f);
        this.headParts.addChild(modelPart10);
        this.saddleParts = new ModelPart[]{modelPart4, modelPart5, modelPart6, modelPart9, modelPart10};
        this.ridingParts = new ModelPart[]{modelPart7, modelPart8};
    }

    protected void addEarModels(ModelPart modelPart) {
        ModelPart modelPart2 = new ModelPart(this, 19, 16);
        modelPart2.addBox(0.55f, -13.0f, 4.0f, 2.0f, 3.0f, 1.0f, -0.001f);
        ModelPart modelPart3 = new ModelPart(this, 19, 16);
        modelPart3.addBox(-2.55f, -13.0f, 4.0f, 2.0f, 3.0f, 1.0f, -0.001f);
        modelPart.addChild(modelPart2);
        modelPart.addChild(modelPart3);
    }

    @Override
    public void render(T abstractHorse, float f, float g, float h, float i, float j, float k) {
        boolean bl = ((AgableMob)abstractHorse).isBaby();
        float l = ((LivingEntity)abstractHorse).getScale();
        boolean bl2 = ((AbstractHorse)abstractHorse).isSaddled();
        boolean bl3 = ((Entity)abstractHorse).isVehicle();
        for (ModelPart modelPart : this.saddleParts) {
            modelPart.visible = bl2;
        }
        for (ModelPart modelPart : this.ridingParts) {
            modelPart.visible = bl3 && bl2;
        }
        if (bl) {
            RenderSystem.pushMatrix();
            RenderSystem.scalef(l, 0.5f + l * 0.5f, l);
            RenderSystem.translatef(0.0f, 0.95f * (1.0f - l), 0.0f);
        }
        this.leg1A.render(k);
        this.leg2A.render(k);
        this.leg3A.render(k);
        this.leg4A.render(k);
        if (bl) {
            RenderSystem.popMatrix();
            RenderSystem.pushMatrix();
            RenderSystem.scalef(l, l, l);
            RenderSystem.translatef(0.0f, 2.3f * (1.0f - l), 0.0f);
        }
        this.body.render(k);
        if (bl) {
            RenderSystem.popMatrix();
            RenderSystem.pushMatrix();
            float m = l + 0.1f * l;
            RenderSystem.scalef(m, m, m);
            RenderSystem.translatef(0.0f, 2.25f * (1.0f - m), 0.1f * (1.4f - m));
        }
        this.headParts.render(k);
        if (bl) {
            RenderSystem.popMatrix();
        }
    }

    @Override
    public void prepareMobModel(T abstractHorse, float f, float g, float h) {
        super.prepareMobModel(abstractHorse, f, g, h);
        float i = this.rotlerp(((AbstractHorse)abstractHorse).yBodyRotO, ((AbstractHorse)abstractHorse).yBodyRot, h);
        float j = this.rotlerp(((AbstractHorse)abstractHorse).yHeadRotO, ((AbstractHorse)abstractHorse).yHeadRot, h);
        float k = Mth.lerp(h, ((AbstractHorse)abstractHorse).xRotO, ((AbstractHorse)abstractHorse).xRot);
        float l = j - i;
        float m = k * ((float)Math.PI / 180);
        if (l > 20.0f) {
            l = 20.0f;
        }
        if (l < -20.0f) {
            l = -20.0f;
        }
        if (g > 0.2f) {
            m += Mth.cos(f * 0.4f) * 0.15f * g;
        }
        float n = ((AbstractHorse)abstractHorse).getEatAnim(h);
        float o = ((AbstractHorse)abstractHorse).getStandAnim(h);
        float p = 1.0f - o;
        float q = ((AbstractHorse)abstractHorse).getMouthAnim(h);
        boolean bl = ((AbstractHorse)abstractHorse).tailCounter != 0;
        float r = (float)((AbstractHorse)abstractHorse).tickCount + h;
        this.headParts.y = 4.0f;
        this.headParts.z = -12.0f;
        this.body.xRot = 0.0f;
        this.headParts.xRot = 0.5235988f + m;
        this.headParts.yRot = l * ((float)Math.PI / 180);
        float s = ((Entity)abstractHorse).isInWater() ? 0.2f : 1.0f;
        float t = Mth.cos(s * f * 0.6662f + (float)Math.PI);
        float u = t * 0.8f * g;
        float v = (1.0f - Math.max(o, n)) * (0.5235988f + m + q * Mth.sin(r) * 0.05f);
        this.headParts.xRot = o * (0.2617994f + m) + n * (2.1816616f + Mth.sin(r) * 0.05f) + v;
        this.headParts.yRot = o * l * ((float)Math.PI / 180) + (1.0f - Math.max(o, n)) * this.headParts.yRot;
        this.headParts.y = o * -4.0f + n * 11.0f + (1.0f - Math.max(o, n)) * this.headParts.y;
        this.headParts.z = o * -4.0f + n * -12.0f + (1.0f - Math.max(o, n)) * this.headParts.z;
        this.body.xRot = o * -0.7853982f + p * this.body.xRot;
        float w = 0.2617994f * o;
        float x = Mth.cos(r * 0.6f + (float)Math.PI);
        this.leg3A.y = 2.0f * o + 14.0f * p;
        this.leg3A.z = -6.0f * o - 10.0f * p;
        this.leg4A.y = this.leg3A.y;
        this.leg4A.z = this.leg3A.z;
        float y = (-1.0471976f + x) * o + u * p;
        float z = (-1.0471976f - x) * o - u * p;
        this.leg1A.xRot = w - t * 0.5f * g * p;
        this.leg2A.xRot = w + t * 0.5f * g * p;
        this.leg3A.xRot = y;
        this.leg4A.xRot = z;
        this.tail.xRot = 0.5235988f + g * 0.75f;
        this.tail.y = -5.0f + g;
        this.tail.z = 2.0f + g * 2.0f;
        this.tail.yRot = bl ? Mth.cos(r * 0.7f) : 0.0f;
    }

    private float rotlerp(float f, float g, float h) {
        float i;
        for (i = g - f; i < -180.0f; i += 360.0f) {
        }
        while (i >= 180.0f) {
            i -= 360.0f;
        }
        return f + h * i;
    }

    @Override
    public /* synthetic */ void render(Entity entity, float f, float g, float h, float i, float j, float k) {
        this.render((T)((AbstractHorse)entity), f, g, h, i, j, k);
    }
}

