package net.minecraft.client.model;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.Boat;

@Environment(EnvType.CLIENT)
public class BoatModel extends EntityModel<Boat> {
	private final ModelPart[] cubes = new ModelPart[5];
	private final ModelPart[] paddles = new ModelPart[2];
	private final ModelPart waterPatch;

	public BoatModel() {
		this.cubes[0] = new ModelPart(this, 0, 0).setTexSize(128, 64);
		this.cubes[1] = new ModelPart(this, 0, 19).setTexSize(128, 64);
		this.cubes[2] = new ModelPart(this, 0, 27).setTexSize(128, 64);
		this.cubes[3] = new ModelPart(this, 0, 35).setTexSize(128, 64);
		this.cubes[4] = new ModelPart(this, 0, 43).setTexSize(128, 64);
		int i = 32;
		int j = 6;
		int k = 20;
		int l = 4;
		int m = 28;
		this.cubes[0].addBox(-14.0F, -9.0F, -3.0F, 28, 16, 3, 0.0F);
		this.cubes[0].setPos(0.0F, 3.0F, 1.0F);
		this.cubes[1].addBox(-13.0F, -7.0F, -1.0F, 18, 6, 2, 0.0F);
		this.cubes[1].setPos(-15.0F, 4.0F, 4.0F);
		this.cubes[2].addBox(-8.0F, -7.0F, -1.0F, 16, 6, 2, 0.0F);
		this.cubes[2].setPos(15.0F, 4.0F, 0.0F);
		this.cubes[3].addBox(-14.0F, -7.0F, -1.0F, 28, 6, 2, 0.0F);
		this.cubes[3].setPos(0.0F, 4.0F, -9.0F);
		this.cubes[4].addBox(-14.0F, -7.0F, -1.0F, 28, 6, 2, 0.0F);
		this.cubes[4].setPos(0.0F, 4.0F, 9.0F);
		this.cubes[0].xRot = (float) (Math.PI / 2);
		this.cubes[1].yRot = (float) (Math.PI * 3.0 / 2.0);
		this.cubes[2].yRot = (float) (Math.PI / 2);
		this.cubes[3].yRot = (float) Math.PI;
		this.paddles[0] = this.makePaddle(true);
		this.paddles[0].setPos(3.0F, -5.0F, 9.0F);
		this.paddles[1] = this.makePaddle(false);
		this.paddles[1].setPos(3.0F, -5.0F, -9.0F);
		this.paddles[1].yRot = (float) Math.PI;
		this.paddles[0].zRot = (float) (Math.PI / 16);
		this.paddles[1].zRot = (float) (Math.PI / 16);
		this.waterPatch = new ModelPart(this, 0, 0).setTexSize(128, 64);
		this.waterPatch.addBox(-14.0F, -9.0F, -3.0F, 28, 16, 3, 0.0F);
		this.waterPatch.setPos(0.0F, -3.0F, 1.0F);
		this.waterPatch.xRot = (float) (Math.PI / 2);
	}

	public void render(Boat boat, float f, float g, float h, float i, float j, float k) {
		GlStateManager.rotatef(90.0F, 0.0F, 1.0F, 0.0F);
		this.setupAnim(boat, f, g, h, i, j, k);

		for (int l = 0; l < 5; l++) {
			this.cubes[l].render(k);
		}

		this.animatePaddle(boat, 0, k, f);
		this.animatePaddle(boat, 1, k, f);
	}

	public void renderSecondPass(Entity entity, float f, float g, float h, float i, float j, float k) {
		GlStateManager.rotatef(90.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.colorMask(false, false, false, false);
		this.waterPatch.render(k);
		GlStateManager.colorMask(true, true, true, true);
	}

	protected ModelPart makePaddle(boolean bl) {
		ModelPart modelPart = new ModelPart(this, 62, bl ? 0 : 20).setTexSize(128, 64);
		int i = 20;
		int j = 7;
		int k = 6;
		float f = -5.0F;
		modelPart.addBox(-1.0F, 0.0F, -5.0F, 2, 2, 18);
		modelPart.addBox(bl ? -1.001F : 0.001F, -3.0F, 8.0F, 1, 6, 7);
		return modelPart;
	}

	protected void animatePaddle(Boat boat, int i, float f, float g) {
		float h = boat.getRowingTime(i, g);
		ModelPart modelPart = this.paddles[i];
		modelPart.xRot = (float)Mth.clampedLerp((float) (-Math.PI / 3), (float) (-Math.PI / 12), (double)((Mth.sin(-h) + 1.0F) / 2.0F));
		modelPart.yRot = (float)Mth.clampedLerp((float) (-Math.PI / 4), (float) (Math.PI / 4), (double)((Mth.sin(-h + 1.0F) + 1.0F) / 2.0F));
		if (i == 1) {
			modelPart.yRot = (float) Math.PI - modelPart.yRot;
		}

		modelPart.render(f);
	}
}
