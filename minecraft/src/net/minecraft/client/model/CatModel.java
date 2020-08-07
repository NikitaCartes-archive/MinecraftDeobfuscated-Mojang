package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.animal.Cat;

@Environment(EnvType.CLIENT)
public class CatModel<T extends Cat> extends OcelotModel<T> {
	private float lieDownAmount;
	private float lieDownAmountTail;
	private float relaxStateOneAmount;

	public CatModel(float f) {
		super(f);
	}

	public void prepareMobModel(T cat, float f, float g, float h) {
		this.lieDownAmount = cat.getLieDownAmount(h);
		this.lieDownAmountTail = cat.getLieDownAmountTail(h);
		this.relaxStateOneAmount = cat.getRelaxStateOneAmount(h);
		if (this.lieDownAmount <= 0.0F) {
			this.head.xRot = 0.0F;
			this.head.zRot = 0.0F;
			this.frontLegL.xRot = 0.0F;
			this.frontLegL.zRot = 0.0F;
			this.frontLegR.xRot = 0.0F;
			this.frontLegR.zRot = 0.0F;
			this.frontLegR.x = -1.2F;
			this.backLegL.xRot = 0.0F;
			this.backLegR.xRot = 0.0F;
			this.backLegR.zRot = 0.0F;
			this.backLegR.x = -1.1F;
			this.backLegR.y = 18.0F;
		}

		super.prepareMobModel(cat, f, g, h);
		if (cat.isInSittingPose()) {
			this.body.xRot = (float) (Math.PI / 4);
			this.body.y += -4.0F;
			this.body.z += 5.0F;
			this.head.y += -3.3F;
			this.head.z++;
			this.tail1.y += 8.0F;
			this.tail1.z += -2.0F;
			this.tail2.y += 2.0F;
			this.tail2.z += -0.8F;
			this.tail1.xRot = 1.7278761F;
			this.tail2.xRot = 2.670354F;
			this.frontLegL.xRot = (float) (-Math.PI / 20);
			this.frontLegL.y = 16.1F;
			this.frontLegL.z = -7.0F;
			this.frontLegR.xRot = (float) (-Math.PI / 20);
			this.frontLegR.y = 16.1F;
			this.frontLegR.z = -7.0F;
			this.backLegL.xRot = (float) (-Math.PI / 2);
			this.backLegL.y = 21.0F;
			this.backLegL.z = 1.0F;
			this.backLegR.xRot = (float) (-Math.PI / 2);
			this.backLegR.y = 21.0F;
			this.backLegR.z = 1.0F;
			this.state = 3;
		}
	}

	public void setupAnim(T cat, float f, float g, float h, float i, float j) {
		super.setupAnim(cat, f, g, h, i, j);
		if (this.lieDownAmount > 0.0F) {
			this.head.zRot = ModelUtils.rotlerpRad(this.head.zRot, -1.2707963F, this.lieDownAmount);
			this.head.yRot = ModelUtils.rotlerpRad(this.head.yRot, 1.2707963F, this.lieDownAmount);
			this.frontLegL.xRot = -1.2707963F;
			this.frontLegR.xRot = -0.47079635F;
			this.frontLegR.zRot = -0.2F;
			this.frontLegR.x = -0.2F;
			this.backLegL.xRot = -0.4F;
			this.backLegR.xRot = 0.5F;
			this.backLegR.zRot = -0.5F;
			this.backLegR.x = -0.3F;
			this.backLegR.y = 20.0F;
			this.tail1.xRot = ModelUtils.rotlerpRad(this.tail1.xRot, 0.8F, this.lieDownAmountTail);
			this.tail2.xRot = ModelUtils.rotlerpRad(this.tail2.xRot, -0.4F, this.lieDownAmountTail);
		}

		if (this.relaxStateOneAmount > 0.0F) {
			this.head.xRot = ModelUtils.rotlerpRad(this.head.xRot, -0.58177644F, this.relaxStateOneAmount);
		}
	}
}
