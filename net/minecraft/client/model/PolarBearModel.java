/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.animal.PolarBear;

@Environment(value=EnvType.CLIENT)
public class PolarBearModel<T extends PolarBear>
extends QuadrupedModel<T> {
    public PolarBearModel() {
        super(12, 0.0f, false, 16.0f, 4.0f, 2.25f, 2.0f, 24);
        this.texWidth = 128;
        this.texHeight = 64;
        this.head = new ModelPart(this, 0, 0);
        this.head.addBox(-3.5f, -3.0f, -3.0f, 7.0f, 7.0f, 7.0f, 0.0f);
        this.head.setPos(0.0f, 10.0f, -16.0f);
        this.head.texOffs(0, 44).addBox(-2.5f, 1.0f, -6.0f, 5.0f, 3.0f, 3.0f, 0.0f);
        this.head.texOffs(26, 0).addBox(-4.5f, -4.0f, -1.0f, 2.0f, 2.0f, 1.0f, 0.0f);
        ModelPart modelPart = this.head.texOffs(26, 0);
        modelPart.mirror = true;
        modelPart.addBox(2.5f, -4.0f, -1.0f, 2.0f, 2.0f, 1.0f, 0.0f);
        this.body = new ModelPart(this);
        this.body.texOffs(0, 19).addBox(-5.0f, -13.0f, -7.0f, 14.0f, 14.0f, 11.0f, 0.0f);
        this.body.texOffs(39, 0).addBox(-4.0f, -25.0f, -7.0f, 12.0f, 12.0f, 10.0f, 0.0f);
        this.body.setPos(-2.0f, 9.0f, 12.0f);
        int i = 10;
        this.leg0 = new ModelPart(this, 50, 22);
        this.leg0.addBox(-2.0f, 0.0f, -2.0f, 4.0f, 10.0f, 8.0f, 0.0f);
        this.leg0.setPos(-3.5f, 14.0f, 6.0f);
        this.leg1 = new ModelPart(this, 50, 22);
        this.leg1.addBox(-2.0f, 0.0f, -2.0f, 4.0f, 10.0f, 8.0f, 0.0f);
        this.leg1.setPos(3.5f, 14.0f, 6.0f);
        this.leg2 = new ModelPart(this, 50, 40);
        this.leg2.addBox(-2.0f, 0.0f, -2.0f, 4.0f, 10.0f, 6.0f, 0.0f);
        this.leg2.setPos(-2.5f, 14.0f, -7.0f);
        this.leg3 = new ModelPart(this, 50, 40);
        this.leg3.addBox(-2.0f, 0.0f, -2.0f, 4.0f, 10.0f, 6.0f, 0.0f);
        this.leg3.setPos(2.5f, 14.0f, -7.0f);
        this.leg0.x -= 1.0f;
        this.leg1.x += 1.0f;
        this.leg0.z += 0.0f;
        this.leg1.z += 0.0f;
        this.leg2.x -= 1.0f;
        this.leg3.x += 1.0f;
        this.leg2.z -= 1.0f;
        this.leg3.z -= 1.0f;
    }

    @Override
    public void setupAnim(T polarBear, float f, float g, float h, float i, float j, float k) {
        super.setupAnim(polarBear, f, g, h, i, j, k);
        float l = h - (float)((PolarBear)polarBear).tickCount;
        float m = ((PolarBear)polarBear).getStandingAnimationScale(l);
        m *= m;
        float n = 1.0f - m;
        this.body.xRot = 1.5707964f - m * (float)Math.PI * 0.35f;
        this.body.y = 9.0f * n + 11.0f * m;
        this.leg2.y = 14.0f * n - 6.0f * m;
        this.leg2.z = -8.0f * n - 4.0f * m;
        this.leg2.xRot -= m * (float)Math.PI * 0.45f;
        this.leg3.y = this.leg2.y;
        this.leg3.z = this.leg2.z;
        this.leg3.xRot -= m * (float)Math.PI * 0.45f;
        if (this.young) {
            this.head.y = 10.0f * n - 9.0f * m;
            this.head.z = -16.0f * n - 7.0f * m;
        } else {
            this.head.y = 10.0f * n - 14.0f * m;
            this.head.z = -16.0f * n - 3.0f * m;
        }
        this.head.xRot += m * (float)Math.PI * 0.15f;
    }
}

