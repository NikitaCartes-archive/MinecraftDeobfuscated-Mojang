package net.minecraft.client.model.dragon;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

@Environment(EnvType.CLIENT)
public class DragonModel extends EntityModel<EnderDragon> {
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
		float g = -16.0F;
		this.head = new ModelPart(this);
		this.head.addBox("upperlip", -6.0F, -1.0F, -24.0F, 12, 5, 16, f, 176, 44);
		this.head.addBox("upperhead", -8.0F, -8.0F, -10.0F, 16, 16, 16, f, 112, 30);
		this.head.mirror = true;
		this.head.addBox("scale", -5.0F, -12.0F, -4.0F, 2, 4, 6, f, 0, 0);
		this.head.addBox("nostril", -5.0F, -3.0F, -22.0F, 2, 2, 4, f, 112, 0);
		this.head.mirror = false;
		this.head.addBox("scale", 3.0F, -12.0F, -4.0F, 2, 4, 6, f, 0, 0);
		this.head.addBox("nostril", 3.0F, -3.0F, -22.0F, 2, 2, 4, f, 112, 0);
		this.jaw = new ModelPart(this);
		this.jaw.setPos(0.0F, 4.0F, -8.0F);
		this.jaw.addBox("jaw", -6.0F, 0.0F, -16.0F, 12, 4, 16, f, 176, 65);
		this.head.addChild(this.jaw);
		this.neck = new ModelPart(this);
		this.neck.addBox("box", -5.0F, -5.0F, -5.0F, 10, 10, 10, f, 192, 104);
		this.neck.addBox("scale", -1.0F, -9.0F, -3.0F, 2, 4, 6, f, 48, 0);
		this.body = new ModelPart(this);
		this.body.setPos(0.0F, 4.0F, 8.0F);
		this.body.addBox("body", -12.0F, 0.0F, -16.0F, 24, 24, 64, f, 0, 0);
		this.body.addBox("scale", -1.0F, -6.0F, -10.0F, 2, 6, 12, f, 220, 53);
		this.body.addBox("scale", -1.0F, -6.0F, 10.0F, 2, 6, 12, f, 220, 53);
		this.body.addBox("scale", -1.0F, -6.0F, 30.0F, 2, 6, 12, f, 220, 53);
		this.wing = new ModelPart(this);
		this.wing.setPos(-12.0F, 5.0F, 2.0F);
		this.wing.addBox("bone", -56.0F, -4.0F, -4.0F, 56, 8, 8, f, 112, 88);
		this.wing.addBox("skin", -56.0F, 0.0F, 2.0F, 56, 0, 56, f, -56, 88);
		this.wingTip = new ModelPart(this);
		this.wingTip.setPos(-56.0F, 0.0F, 0.0F);
		this.wingTip.addBox("bone", -56.0F, -2.0F, -2.0F, 56, 4, 4, f, 112, 136);
		this.wingTip.addBox("skin", -56.0F, 0.0F, 2.0F, 56, 0, 56, f, -56, 144);
		this.wing.addChild(this.wingTip);
		this.frontLeg = new ModelPart(this);
		this.frontLeg.setPos(-12.0F, 20.0F, 2.0F);
		this.frontLeg.addBox("main", -4.0F, -4.0F, -4.0F, 8, 24, 8, f, 112, 104);
		this.frontLegTip = new ModelPart(this);
		this.frontLegTip.setPos(0.0F, 20.0F, -1.0F);
		this.frontLegTip.addBox("main", -3.0F, -1.0F, -3.0F, 6, 24, 6, f, 226, 138);
		this.frontLeg.addChild(this.frontLegTip);
		this.frontFoot = new ModelPart(this);
		this.frontFoot.setPos(0.0F, 23.0F, 0.0F);
		this.frontFoot.addBox("main", -4.0F, 0.0F, -12.0F, 8, 4, 16, f, 144, 104);
		this.frontLegTip.addChild(this.frontFoot);
		this.rearLeg = new ModelPart(this);
		this.rearLeg.setPos(-16.0F, 16.0F, 42.0F);
		this.rearLeg.addBox("main", -8.0F, -4.0F, -8.0F, 16, 32, 16, f, 0, 0);
		this.rearLegTip = new ModelPart(this);
		this.rearLegTip.setPos(0.0F, 32.0F, -4.0F);
		this.rearLegTip.addBox("main", -6.0F, -2.0F, 0.0F, 12, 32, 12, f, 196, 0);
		this.rearLeg.addChild(this.rearLegTip);
		this.rearFoot = new ModelPart(this);
		this.rearFoot.setPos(0.0F, 31.0F, 4.0F);
		this.rearFoot.addBox("main", -9.0F, 0.0F, -20.0F, 18, 6, 24, f, 112, 0);
		this.rearLegTip.addChild(this.rearFoot);
	}

	public void prepareMobModel(EnderDragon enderDragon, float f, float g, float h) {
		this.a = h;
	}

	public void render(EnderDragon enderDragon, float f, float g, float h, float i, float j, float k) {
		RenderSystem.pushMatrix();
		float l = Mth.lerp(this.a, enderDragon.oFlapTime, enderDragon.flapTime);
		this.jaw.xRot = (float)(Math.sin((double)(l * (float) (Math.PI * 2))) + 1.0) * 0.2F;
		float m = (float)(Math.sin((double)(l * (float) (Math.PI * 2) - 1.0F)) + 1.0);
		m = (m * m + m * 2.0F) * 0.05F;
		RenderSystem.translatef(0.0F, m - 2.0F, -3.0F);
		RenderSystem.rotatef(m * 2.0F, 1.0F, 0.0F, 0.0F);
		float n = 0.0F;
		float o = 20.0F;
		float p = -12.0F;
		float q = 1.5F;
		double[] ds = enderDragon.getLatencyPos(6, this.a);
		float r = this.rotWrap(enderDragon.getLatencyPos(5, this.a)[0] - enderDragon.getLatencyPos(10, this.a)[0]);
		float s = this.rotWrap(enderDragon.getLatencyPos(5, this.a)[0] + (double)(r / 2.0F));
		float t = l * (float) (Math.PI * 2);

		for (int u = 0; u < 5; u++) {
			double[] es = enderDragon.getLatencyPos(5 - u, this.a);
			float v = (float)Math.cos((double)((float)u * 0.45F + t)) * 0.15F;
			this.neck.yRot = this.rotWrap(es[0] - ds[0]) * (float) (Math.PI / 180.0) * 1.5F;
			this.neck.xRot = v + enderDragon.getHeadPartYOffset(u, ds, es) * (float) (Math.PI / 180.0) * 1.5F * 5.0F;
			this.neck.zRot = -this.rotWrap(es[0] - (double)s) * (float) (Math.PI / 180.0) * 1.5F;
			this.neck.y = o;
			this.neck.z = p;
			this.neck.x = n;
			o = (float)((double)o + Math.sin((double)this.neck.xRot) * 10.0);
			p = (float)((double)p - Math.cos((double)this.neck.yRot) * Math.cos((double)this.neck.xRot) * 10.0);
			n = (float)((double)n - Math.sin((double)this.neck.yRot) * Math.cos((double)this.neck.xRot) * 10.0);
			this.neck.render(k);
		}

		this.head.y = o;
		this.head.z = p;
		this.head.x = n;
		double[] fs = enderDragon.getLatencyPos(0, this.a);
		this.head.yRot = this.rotWrap(fs[0] - ds[0]) * (float) (Math.PI / 180.0);
		this.head.xRot = this.rotWrap((double)enderDragon.getHeadPartYOffset(6, ds, fs)) * (float) (Math.PI / 180.0) * 1.5F * 5.0F;
		this.head.zRot = -this.rotWrap(fs[0] - (double)s) * (float) (Math.PI / 180.0);
		this.head.render(k);
		RenderSystem.pushMatrix();
		RenderSystem.translatef(0.0F, 1.0F, 0.0F);
		RenderSystem.rotatef(-r * 1.5F, 0.0F, 0.0F, 1.0F);
		RenderSystem.translatef(0.0F, -1.0F, 0.0F);
		this.body.zRot = 0.0F;
		this.body.render(k);

		for (int w = 0; w < 2; w++) {
			RenderSystem.enableCull();
			float v = l * (float) (Math.PI * 2);
			this.wing.xRot = 0.125F - (float)Math.cos((double)v) * 0.2F;
			this.wing.yRot = 0.25F;
			this.wing.zRot = (float)(Math.sin((double)v) + 0.125) * 0.8F;
			this.wingTip.zRot = -((float)(Math.sin((double)(v + 2.0F)) + 0.5)) * 0.75F;
			this.rearLeg.xRot = 1.0F + m * 0.1F;
			this.rearLegTip.xRot = 0.5F + m * 0.1F;
			this.rearFoot.xRot = 0.75F + m * 0.1F;
			this.frontLeg.xRot = 1.3F + m * 0.1F;
			this.frontLegTip.xRot = -0.5F - m * 0.1F;
			this.frontFoot.xRot = 0.75F + m * 0.1F;
			this.wing.render(k);
			this.frontLeg.render(k);
			this.rearLeg.render(k);
			RenderSystem.scalef(-1.0F, 1.0F, 1.0F);
			if (w == 0) {
				RenderSystem.cullFace(GlStateManager.CullFace.FRONT);
			}
		}

		RenderSystem.popMatrix();
		RenderSystem.cullFace(GlStateManager.CullFace.BACK);
		RenderSystem.disableCull();
		float x = -((float)Math.sin((double)(l * (float) (Math.PI * 2)))) * 0.0F;
		t = l * (float) (Math.PI * 2);
		o = 10.0F;
		p = 60.0F;
		n = 0.0F;
		ds = enderDragon.getLatencyPos(11, this.a);

		for (int y = 0; y < 12; y++) {
			fs = enderDragon.getLatencyPos(12 + y, this.a);
			x = (float)((double)x + Math.sin((double)((float)y * 0.45F + t)) * 0.05F);
			this.neck.yRot = (this.rotWrap(fs[0] - ds[0]) * 1.5F + 180.0F) * (float) (Math.PI / 180.0);
			this.neck.xRot = x + (float)(fs[1] - ds[1]) * (float) (Math.PI / 180.0) * 1.5F * 5.0F;
			this.neck.zRot = this.rotWrap(fs[0] - (double)s) * (float) (Math.PI / 180.0) * 1.5F;
			this.neck.y = o;
			this.neck.z = p;
			this.neck.x = n;
			o = (float)((double)o + Math.sin((double)this.neck.xRot) * 10.0);
			p = (float)((double)p - Math.cos((double)this.neck.yRot) * Math.cos((double)this.neck.xRot) * 10.0);
			n = (float)((double)n - Math.sin((double)this.neck.yRot) * Math.cos((double)this.neck.xRot) * 10.0);
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
}
