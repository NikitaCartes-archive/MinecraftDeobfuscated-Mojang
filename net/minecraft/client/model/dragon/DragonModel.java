/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model.dragon;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

@Environment(value=EnvType.CLIENT)
public class DragonModel
extends EntityModel<EnderDragon> {
    private final ModelPart head;
    private final ModelPart neck;
    private final ModelPart jaw;
    private final ModelPart body;
    private final ModelPart rearLeg;
    private final ModelPart frontLeg;
    private final ModelPart rearLegTip;
    private final ModelPart frontLegTip;
    private final ModelPart rearFoot;
    private final ModelPart frontFoot;
    private final ModelPart wing;
    private final ModelPart wingTip;
    private float a;

    public DragonModel(float f) {
        this.texWidth = 256;
        this.texHeight = 256;
        float g = -16.0f;
        this.head = new ModelPart(this, "head");
        this.head.addBox("upperlip", -6.0f, -1.0f, -24.0f, 12, 5, 16, f, 176, 44);
        this.head.addBox("upperhead", -8.0f, -8.0f, -10.0f, 16, 16, 16, f, 112, 30);
        this.head.mirror = true;
        this.head.addBox("scale", -5.0f, -12.0f, -4.0f, 2, 4, 6, f, 0, 0);
        this.head.addBox("nostril", -5.0f, -3.0f, -22.0f, 2, 2, 4, f, 112, 0);
        this.head.mirror = false;
        this.head.addBox("scale", 3.0f, -12.0f, -4.0f, 2, 4, 6, f, 0, 0);
        this.head.addBox("nostril", 3.0f, -3.0f, -22.0f, 2, 2, 4, f, 112, 0);
        this.jaw = new ModelPart(this, "jaw");
        this.jaw.setPos(0.0f, 4.0f, -8.0f);
        this.jaw.addBox("jaw", -6.0f, 0.0f, -16.0f, 12, 4, 16, f, 176, 65);
        this.head.addChild(this.jaw);
        this.neck = new ModelPart(this, "neck");
        this.neck.addBox("box", -5.0f, -5.0f, -5.0f, 10, 10, 10, f, 192, 104);
        this.neck.addBox("scale", -1.0f, -9.0f, -3.0f, 2, 4, 6, f, 48, 0);
        this.body = new ModelPart(this, "body");
        this.body.setPos(0.0f, 4.0f, 8.0f);
        this.body.addBox("body", -12.0f, 0.0f, -16.0f, 24, 24, 64, f, 0, 0);
        this.body.addBox("scale", -1.0f, -6.0f, -10.0f, 2, 6, 12, f, 220, 53);
        this.body.addBox("scale", -1.0f, -6.0f, 10.0f, 2, 6, 12, f, 220, 53);
        this.body.addBox("scale", -1.0f, -6.0f, 30.0f, 2, 6, 12, f, 220, 53);
        this.wing = new ModelPart(this, "wing");
        this.wing.setPos(-12.0f, 5.0f, 2.0f);
        this.wing.addBox("bone", -56.0f, -4.0f, -4.0f, 56, 8, 8, f, 112, 88);
        this.wing.addBox("skin", -56.0f, 0.0f, 2.0f, 56, 0, 56, f, -56, 88);
        this.wingTip = new ModelPart(this, "wingtip");
        this.wingTip.setPos(-56.0f, 0.0f, 0.0f);
        this.wingTip.addBox("bone", -56.0f, -2.0f, -2.0f, 56, 4, 4, f, 112, 136);
        this.wingTip.addBox("skin", -56.0f, 0.0f, 2.0f, 56, 0, 56, f, -56, 144);
        this.wing.addChild(this.wingTip);
        this.frontLeg = new ModelPart(this, "frontleg");
        this.frontLeg.setPos(-12.0f, 20.0f, 2.0f);
        this.frontLeg.addBox("main", -4.0f, -4.0f, -4.0f, 8, 24, 8, f, 112, 104);
        this.frontLegTip = new ModelPart(this, "frontlegtip");
        this.frontLegTip.setPos(0.0f, 20.0f, -1.0f);
        this.frontLegTip.addBox("main", -3.0f, -1.0f, -3.0f, 6, 24, 6, f, 226, 138);
        this.frontLeg.addChild(this.frontLegTip);
        this.frontFoot = new ModelPart(this, "frontfoot");
        this.frontFoot.setPos(0.0f, 23.0f, 0.0f);
        this.frontFoot.addBox("main", -4.0f, 0.0f, -12.0f, 8, 4, 16, f, 144, 104);
        this.frontLegTip.addChild(this.frontFoot);
        this.rearLeg = new ModelPart(this, "rearleg");
        this.rearLeg.setPos(-16.0f, 16.0f, 42.0f);
        this.rearLeg.addBox("main", -8.0f, -4.0f, -8.0f, 16, 32, 16, f, 0, 0);
        this.rearLegTip = new ModelPart(this, "rearlegtip");
        this.rearLegTip.setPos(0.0f, 32.0f, -4.0f);
        this.rearLegTip.addBox("main", -6.0f, -2.0f, 0.0f, 12, 32, 12, f, 196, 0);
        this.rearLeg.addChild(this.rearLegTip);
        this.rearFoot = new ModelPart(this, "rearfoot");
        this.rearFoot.setPos(0.0f, 31.0f, 4.0f);
        this.rearFoot.addBox("main", -9.0f, 0.0f, -20.0f, 18, 6, 24, f, 112, 0);
        this.rearLegTip.addChild(this.rearFoot);
    }

    @Override
    public void prepareMobModel(EnderDragon enderDragon, float f, float g, float h) {
        this.a = h;
    }

    @Override
    public void render(EnderDragon enderDragon, float f, float g, float h, float i, float j, float k) {
        float v;
        RenderSystem.pushMatrix();
        float l = Mth.lerp(this.a, enderDragon.oFlapTime, enderDragon.flapTime);
        this.jaw.xRot = (float)(Math.sin(l * ((float)Math.PI * 2)) + 1.0) * 0.2f;
        float m = (float)(Math.sin(l * ((float)Math.PI * 2) - 1.0f) + 1.0);
        m = (m * m + m * 2.0f) * 0.05f;
        RenderSystem.translatef(0.0f, m - 2.0f, -3.0f);
        RenderSystem.rotatef(m * 2.0f, 1.0f, 0.0f, 0.0f);
        float n = 0.0f;
        float o = 20.0f;
        float p = -12.0f;
        float q = 1.5f;
        double[] ds = enderDragon.getLatencyPos(6, this.a);
        float r = this.rotWrap(enderDragon.getLatencyPos(5, this.a)[0] - enderDragon.getLatencyPos(10, this.a)[0]);
        float s = this.rotWrap(enderDragon.getLatencyPos(5, this.a)[0] + (double)(r / 2.0f));
        float t = l * ((float)Math.PI * 2);
        for (int u = 0; u < 5; ++u) {
            double[] es = enderDragon.getLatencyPos(5 - u, this.a);
            v = (float)Math.cos((float)u * 0.45f + t) * 0.15f;
            this.neck.yRot = this.rotWrap(es[0] - ds[0]) * ((float)Math.PI / 180) * 1.5f;
            this.neck.xRot = v + enderDragon.getHeadPartYOffset(u, ds, es) * ((float)Math.PI / 180) * 1.5f * 5.0f;
            this.neck.zRot = -this.rotWrap(es[0] - (double)s) * ((float)Math.PI / 180) * 1.5f;
            this.neck.y = o;
            this.neck.z = p;
            this.neck.x = n;
            o = (float)((double)o + Math.sin(this.neck.xRot) * 10.0);
            p = (float)((double)p - Math.cos(this.neck.yRot) * Math.cos(this.neck.xRot) * 10.0);
            n = (float)((double)n - Math.sin(this.neck.yRot) * Math.cos(this.neck.xRot) * 10.0);
            this.neck.render(k);
        }
        this.head.y = o;
        this.head.z = p;
        this.head.x = n;
        double[] fs = enderDragon.getLatencyPos(0, this.a);
        this.head.yRot = this.rotWrap(fs[0] - ds[0]) * ((float)Math.PI / 180);
        this.head.xRot = this.rotWrap(enderDragon.getHeadPartYOffset(6, ds, fs)) * ((float)Math.PI / 180) * 1.5f * 5.0f;
        this.head.zRot = -this.rotWrap(fs[0] - (double)s) * ((float)Math.PI / 180);
        this.head.render(k);
        RenderSystem.pushMatrix();
        RenderSystem.translatef(0.0f, 1.0f, 0.0f);
        RenderSystem.rotatef(-r * 1.5f, 0.0f, 0.0f, 1.0f);
        RenderSystem.translatef(0.0f, -1.0f, 0.0f);
        this.body.zRot = 0.0f;
        this.body.render(k);
        for (int w = 0; w < 2; ++w) {
            RenderSystem.enableCull();
            v = l * ((float)Math.PI * 2);
            this.wing.xRot = 0.125f - (float)Math.cos(v) * 0.2f;
            this.wing.yRot = 0.25f;
            this.wing.zRot = (float)(Math.sin(v) + 0.125) * 0.8f;
            this.wingTip.zRot = -((float)(Math.sin(v + 2.0f) + 0.5)) * 0.75f;
            this.rearLeg.xRot = 1.0f + m * 0.1f;
            this.rearLegTip.xRot = 0.5f + m * 0.1f;
            this.rearFoot.xRot = 0.75f + m * 0.1f;
            this.frontLeg.xRot = 1.3f + m * 0.1f;
            this.frontLegTip.xRot = -0.5f - m * 0.1f;
            this.frontFoot.xRot = 0.75f + m * 0.1f;
            this.wing.render(k);
            this.frontLeg.render(k);
            this.rearLeg.render(k);
            RenderSystem.scalef(-1.0f, 1.0f, 1.0f);
            if (w != 0) continue;
            RenderSystem.cullFace(GlStateManager.CullFace.FRONT);
        }
        RenderSystem.popMatrix();
        RenderSystem.cullFace(GlStateManager.CullFace.BACK);
        RenderSystem.disableCull();
        float x = -((float)Math.sin(l * ((float)Math.PI * 2))) * 0.0f;
        t = l * ((float)Math.PI * 2);
        o = 10.0f;
        p = 60.0f;
        n = 0.0f;
        ds = enderDragon.getLatencyPos(11, this.a);
        for (int y = 0; y < 12; ++y) {
            fs = enderDragon.getLatencyPos(12 + y, this.a);
            x = (float)((double)x + Math.sin((float)y * 0.45f + t) * (double)0.05f);
            this.neck.yRot = (this.rotWrap(fs[0] - ds[0]) * 1.5f + 180.0f) * ((float)Math.PI / 180);
            this.neck.xRot = x + (float)(fs[1] - ds[1]) * ((float)Math.PI / 180) * 1.5f * 5.0f;
            this.neck.zRot = this.rotWrap(fs[0] - (double)s) * ((float)Math.PI / 180) * 1.5f;
            this.neck.y = o;
            this.neck.z = p;
            this.neck.x = n;
            o = (float)((double)o + Math.sin(this.neck.xRot) * 10.0);
            p = (float)((double)p - Math.cos(this.neck.yRot) * Math.cos(this.neck.xRot) * 10.0);
            n = (float)((double)n - Math.sin(this.neck.yRot) * Math.cos(this.neck.xRot) * 10.0);
            this.neck.render(k);
        }
        RenderSystem.popMatrix();
    }

    private float rotWrap(double d) {
        while (d >= 180.0) {
            d -= 360.0;
        }
        while (d < -180.0) {
            d += 360.0;
        }
        return (float)d;
    }

    @Override
    public /* synthetic */ void render(Entity entity, float f, float g, float h, float i, float j, float k) {
        this.render((EnderDragon)entity, f, g, h, i, j, k);
    }
}

