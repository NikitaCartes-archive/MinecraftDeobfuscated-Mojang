/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;

@Environment(value=EnvType.CLIENT)
public class LlamaModel<T extends AbstractChestedHorse>
extends QuadrupedModel<T> {
    private final ModelPart chest1;
    private final ModelPart chest2;

    public LlamaModel(float f) {
        super(15, f);
        this.texWidth = 128;
        this.texHeight = 64;
        this.head = new ModelPart(this, 0, 0);
        this.head.addBox(-2.0f, -14.0f, -10.0f, 4, 4, 9, f);
        this.head.setPos(0.0f, 7.0f, -6.0f);
        this.head.texOffs(0, 14).addBox(-4.0f, -16.0f, -6.0f, 8, 18, 6, f);
        this.head.texOffs(17, 0).addBox(-4.0f, -19.0f, -4.0f, 3, 3, 2, f);
        this.head.texOffs(17, 0).addBox(1.0f, -19.0f, -4.0f, 3, 3, 2, f);
        this.body = new ModelPart(this, 29, 0);
        this.body.addBox(-6.0f, -10.0f, -7.0f, 12, 18, 10, f);
        this.body.setPos(0.0f, 5.0f, 2.0f);
        this.chest1 = new ModelPart(this, 45, 28);
        this.chest1.addBox(-3.0f, 0.0f, 0.0f, 8, 8, 3, f);
        this.chest1.setPos(-8.5f, 3.0f, 3.0f);
        this.chest1.yRot = 1.5707964f;
        this.chest2 = new ModelPart(this, 45, 41);
        this.chest2.addBox(-3.0f, 0.0f, 0.0f, 8, 8, 3, f);
        this.chest2.setPos(5.5f, 3.0f, 3.0f);
        this.chest2.yRot = 1.5707964f;
        int i = 4;
        int j = 14;
        this.leg0 = new ModelPart(this, 29, 29);
        this.leg0.addBox(-2.0f, 0.0f, -2.0f, 4, 14, 4, f);
        this.leg0.setPos(-2.5f, 10.0f, 6.0f);
        this.leg1 = new ModelPart(this, 29, 29);
        this.leg1.addBox(-2.0f, 0.0f, -2.0f, 4, 14, 4, f);
        this.leg1.setPos(2.5f, 10.0f, 6.0f);
        this.leg2 = new ModelPart(this, 29, 29);
        this.leg2.addBox(-2.0f, 0.0f, -2.0f, 4, 14, 4, f);
        this.leg2.setPos(-2.5f, 10.0f, -4.0f);
        this.leg3 = new ModelPart(this, 29, 29);
        this.leg3.addBox(-2.0f, 0.0f, -2.0f, 4, 14, 4, f);
        this.leg3.setPos(2.5f, 10.0f, -4.0f);
        this.leg0.x -= 1.0f;
        this.leg1.x += 1.0f;
        this.leg0.z += 0.0f;
        this.leg1.z += 0.0f;
        this.leg2.x -= 1.0f;
        this.leg3.x += 1.0f;
        this.leg2.z -= 1.0f;
        this.leg3.z -= 1.0f;
        this.zHeadOffs += 2.0f;
    }

    @Override
    public void render(T abstractChestedHorse, float f, float g, float h, float i, float j, float k) {
        boolean bl = !((AgableMob)abstractChestedHorse).isBaby() && ((AbstractChestedHorse)abstractChestedHorse).hasChest();
        this.setupAnim(abstractChestedHorse, f, g, h, i, j, k);
        if (this.young) {
            float l = 2.0f;
            GlStateManager.pushMatrix();
            GlStateManager.translatef(0.0f, this.yHeadOffs * k, this.zHeadOffs * k);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            float m = 0.7f;
            GlStateManager.scalef(0.71428573f, 0.64935064f, 0.7936508f);
            GlStateManager.translatef(0.0f, 21.0f * k, 0.22f);
            this.head.render(k);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            float n = 1.1f;
            GlStateManager.scalef(0.625f, 0.45454544f, 0.45454544f);
            GlStateManager.translatef(0.0f, 33.0f * k, 0.0f);
            this.body.render(k);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.scalef(0.45454544f, 0.41322312f, 0.45454544f);
            GlStateManager.translatef(0.0f, 33.0f * k, 0.0f);
            this.leg0.render(k);
            this.leg1.render(k);
            this.leg2.render(k);
            this.leg3.render(k);
            GlStateManager.popMatrix();
        } else {
            this.head.render(k);
            this.body.render(k);
            this.leg0.render(k);
            this.leg1.render(k);
            this.leg2.render(k);
            this.leg3.render(k);
        }
        if (bl) {
            this.chest1.render(k);
            this.chest2.render(k);
        }
    }

    @Override
    public /* synthetic */ void render(Entity entity, float f, float g, float h, float i, float j, float k) {
        this.render((T)((AbstractChestedHorse)entity), f, g, h, i, j, k);
    }
}

