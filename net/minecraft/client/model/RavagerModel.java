/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Ravager;

@Environment(value=EnvType.CLIENT)
public class RavagerModel
extends EntityModel<Ravager> {
    private final ModelPart head;
    private final ModelPart mouth;
    private final ModelPart body;
    private final ModelPart leg0;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart neck;

    public RavagerModel() {
        this.texWidth = 128;
        this.texHeight = 128;
        int i = 16;
        float f = 0.0f;
        this.neck = new ModelPart(this);
        this.neck.setPos(0.0f, -7.0f, -1.5f);
        this.neck.texOffs(68, 73).addBox(-5.0f, -1.0f, -18.0f, 10.0f, 10.0f, 18.0f, 0.0f);
        this.head = new ModelPart(this);
        this.head.setPos(0.0f, 16.0f, -17.0f);
        this.head.texOffs(0, 0).addBox(-8.0f, -20.0f, -14.0f, 16.0f, 20.0f, 16.0f, 0.0f);
        this.head.texOffs(0, 0).addBox(-2.0f, -6.0f, -18.0f, 4.0f, 8.0f, 4.0f, 0.0f);
        ModelPart modelPart = new ModelPart(this);
        modelPart.setPos(-10.0f, -14.0f, -8.0f);
        modelPart.texOffs(74, 55).addBox(0.0f, -14.0f, -2.0f, 2.0f, 14.0f, 4.0f, 0.0f);
        modelPart.xRot = 1.0995574f;
        this.head.addChild(modelPart);
        ModelPart modelPart2 = new ModelPart(this);
        modelPart2.mirror = true;
        modelPart2.setPos(8.0f, -14.0f, -8.0f);
        modelPart2.texOffs(74, 55).addBox(0.0f, -14.0f, -2.0f, 2.0f, 14.0f, 4.0f, 0.0f);
        modelPart2.xRot = 1.0995574f;
        this.head.addChild(modelPart2);
        this.mouth = new ModelPart(this);
        this.mouth.setPos(0.0f, -2.0f, 2.0f);
        this.mouth.texOffs(0, 36).addBox(-8.0f, 0.0f, -16.0f, 16.0f, 3.0f, 16.0f, 0.0f);
        this.head.addChild(this.mouth);
        this.neck.addChild(this.head);
        this.body = new ModelPart(this);
        this.body.texOffs(0, 55).addBox(-7.0f, -10.0f, -7.0f, 14.0f, 16.0f, 20.0f, 0.0f);
        this.body.texOffs(0, 91).addBox(-6.0f, 6.0f, -7.0f, 12.0f, 13.0f, 18.0f, 0.0f);
        this.body.setPos(0.0f, 1.0f, 2.0f);
        this.leg0 = new ModelPart(this, 96, 0);
        this.leg0.addBox(-4.0f, 0.0f, -4.0f, 8.0f, 37.0f, 8.0f, 0.0f);
        this.leg0.setPos(-8.0f, -13.0f, 18.0f);
        this.leg1 = new ModelPart(this, 96, 0);
        this.leg1.mirror = true;
        this.leg1.addBox(-4.0f, 0.0f, -4.0f, 8.0f, 37.0f, 8.0f, 0.0f);
        this.leg1.setPos(8.0f, -13.0f, 18.0f);
        this.leg2 = new ModelPart(this, 64, 0);
        this.leg2.addBox(-4.0f, 0.0f, -4.0f, 8.0f, 37.0f, 8.0f, 0.0f);
        this.leg2.setPos(-8.0f, -13.0f, -5.0f);
        this.leg3 = new ModelPart(this, 64, 0);
        this.leg3.mirror = true;
        this.leg3.addBox(-4.0f, 0.0f, -4.0f, 8.0f, 37.0f, 8.0f, 0.0f);
        this.leg3.setPos(8.0f, -13.0f, -5.0f);
    }

    @Override
    public void render(Ravager ravager, float f, float g, float h, float i, float j, float k) {
        this.setupAnim(ravager, f, g, h, i, j, k);
        this.neck.render(k);
        this.body.render(k);
        this.leg0.render(k);
        this.leg1.render(k);
        this.leg2.render(k);
        this.leg3.render(k);
    }

    @Override
    public void setupAnim(Ravager ravager, float f, float g, float h, float i, float j, float k) {
        this.head.xRot = j * ((float)Math.PI / 180);
        this.head.yRot = i * ((float)Math.PI / 180);
        this.body.xRot = 1.5707964f;
        float l = 0.4f * g;
        this.leg0.xRot = Mth.cos(f * 0.6662f) * l;
        this.leg1.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * l;
        this.leg2.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * l;
        this.leg3.xRot = Mth.cos(f * 0.6662f) * l;
    }

    @Override
    public void prepareMobModel(Ravager ravager, float f, float g, float h) {
        super.prepareMobModel(ravager, f, g, h);
        int i = ravager.getStunnedTick();
        int j = ravager.getRoarTick();
        int k = 20;
        int l = ravager.getAttackTick();
        int m = 10;
        if (l > 0) {
            float n = this.triangleWave((float)l - h, 10.0f);
            float o = (1.0f + n) * 0.5f;
            float p = o * o * o * 12.0f;
            float q = p * Mth.sin(this.neck.xRot);
            this.neck.z = -6.5f + p;
            this.neck.y = -7.0f - q;
            float r = Mth.sin(((float)l - h) / 10.0f * (float)Math.PI * 0.25f);
            this.mouth.xRot = 1.5707964f * r;
            this.mouth.xRot = l > 5 ? Mth.sin(((float)(-4 + l) - h) / 4.0f) * (float)Math.PI * 0.4f : 0.15707964f * Mth.sin((float)Math.PI * ((float)l - h) / 10.0f);
        } else {
            float n = -1.0f;
            float o = -1.0f * Mth.sin(this.neck.xRot);
            this.neck.x = 0.0f;
            this.neck.y = -7.0f - o;
            this.neck.z = 5.5f;
            boolean bl = i > 0;
            this.neck.xRot = bl ? 0.21991149f : 0.0f;
            this.mouth.xRot = (float)Math.PI * (bl ? 0.05f : 0.01f);
            if (bl) {
                double d = (double)i / 40.0;
                this.neck.x = (float)Math.sin(d * 10.0) * 3.0f;
            } else if (j > 0) {
                float q = Mth.sin(((float)(20 - j) - h) / 20.0f * (float)Math.PI * 0.25f);
                this.mouth.xRot = 1.5707964f * q;
            }
        }
    }

    private float triangleWave(float f, float g) {
        return (Math.abs(f % g - g * 0.5f) - g * 0.25f) / (g * 0.25f);
    }

    @Override
    public /* synthetic */ void setupAnim(Entity entity, float f, float g, float h, float i, float j, float k) {
        this.setupAnim((Ravager)entity, f, g, h, i, j, k);
    }

    @Override
    public /* synthetic */ void render(Entity entity, float f, float g, float h, float i, float j, float k) {
        this.render((Ravager)entity, f, g, h, i, j, k);
    }
}

