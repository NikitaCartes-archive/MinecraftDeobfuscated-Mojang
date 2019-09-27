package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.animal.Sheep;

@Environment(EnvType.CLIENT)
public class SheepFurModel<T extends Sheep> extends QuadrupedModel<T> {
	private float headXRot;

	public SheepFurModel() {
		super(12, 0.0F, false, 8.0F, 4.0F, 2.0F, 2.0F, 24);
		this.head = new ModelPart(this, 0, 0);
		this.head.addBox(-3.0F, -4.0F, -4.0F, 6.0F, 6.0F, 6.0F, 0.6F);
		this.head.setPos(0.0F, 6.0F, -8.0F);
		this.body = new ModelPart(this, 28, 8);
		this.body.addBox(-4.0F, -10.0F, -7.0F, 8.0F, 16.0F, 6.0F, 1.75F);
		this.body.setPos(0.0F, 5.0F, 2.0F);
		float f = 0.5F;
		this.leg0 = new ModelPart(this, 0, 16);
		this.leg0.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.5F);
		this.leg0.setPos(-3.0F, 12.0F, 7.0F);
		this.leg1 = new ModelPart(this, 0, 16);
		this.leg1.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.5F);
		this.leg1.setPos(3.0F, 12.0F, 7.0F);
		this.leg2 = new ModelPart(this, 0, 16);
		this.leg2.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.5F);
		this.leg2.setPos(-3.0F, 12.0F, -5.0F);
		this.leg3 = new ModelPart(this, 0, 16);
		this.leg3.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.5F);
		this.leg3.setPos(3.0F, 12.0F, -5.0F);
	}

	public void prepareMobModel(T sheep, float f, float g, float h) {
		super.prepareMobModel(sheep, f, g, h);
		this.head.y = 6.0F + sheep.getHeadEatPositionScale(h) * 9.0F;
		this.headXRot = sheep.getHeadEatAngleScale(h);
	}

	public void setupAnim(T sheep, float f, float g, float h, float i, float j, float k) {
		super.setupAnim(sheep, f, g, h, i, j, k);
		this.head.xRot = this.headXRot;
	}
}
