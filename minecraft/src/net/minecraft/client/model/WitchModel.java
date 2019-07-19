package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class WitchModel<T extends Entity> extends VillagerModel<T> {
	private boolean holdingItem;
	private final ModelPart mole = new ModelPart(this).setTexSize(64, 128);

	public WitchModel(float f) {
		super(f, 64, 128);
		this.mole.setPos(0.0F, -2.0F, 0.0F);
		this.mole.texOffs(0, 0).addBox(0.0F, 3.0F, -6.75F, 1, 1, 1, -0.25F);
		this.nose.addChild(this.mole);
		this.head.removeChild(this.hat);
		this.hat = new ModelPart(this).setTexSize(64, 128);
		this.hat.setPos(-5.0F, -10.03125F, -5.0F);
		this.hat.texOffs(0, 64).addBox(0.0F, 0.0F, 0.0F, 10, 2, 10);
		this.head.addChild(this.hat);
		ModelPart modelPart = new ModelPart(this).setTexSize(64, 128);
		modelPart.setPos(1.75F, -4.0F, 2.0F);
		modelPart.texOffs(0, 76).addBox(0.0F, 0.0F, 0.0F, 7, 4, 7);
		modelPart.xRot = -0.05235988F;
		modelPart.zRot = 0.02617994F;
		this.hat.addChild(modelPart);
		ModelPart modelPart2 = new ModelPart(this).setTexSize(64, 128);
		modelPart2.setPos(1.75F, -4.0F, 2.0F);
		modelPart2.texOffs(0, 87).addBox(0.0F, 0.0F, 0.0F, 4, 4, 4);
		modelPart2.xRot = -0.10471976F;
		modelPart2.zRot = 0.05235988F;
		modelPart.addChild(modelPart2);
		ModelPart modelPart3 = new ModelPart(this).setTexSize(64, 128);
		modelPart3.setPos(1.75F, -2.0F, 2.0F);
		modelPart3.texOffs(0, 95).addBox(0.0F, 0.0F, 0.0F, 1, 2, 1, 0.25F);
		modelPart3.xRot = (float) (-Math.PI / 15);
		modelPart3.zRot = 0.10471976F;
		modelPart2.addChild(modelPart3);
	}

	@Override
	public void setupAnim(T entity, float f, float g, float h, float i, float j, float k) {
		super.setupAnim(entity, f, g, h, i, j, k);
		this.nose.translateX = 0.0F;
		this.nose.translateY = 0.0F;
		this.nose.translateZ = 0.0F;
		float l = 0.01F * (float)(entity.getId() % 10);
		this.nose.xRot = Mth.sin((float)entity.tickCount * l) * 4.5F * (float) (Math.PI / 180.0);
		this.nose.yRot = 0.0F;
		this.nose.zRot = Mth.cos((float)entity.tickCount * l) * 2.5F * (float) (Math.PI / 180.0);
		if (this.holdingItem) {
			this.nose.xRot = -0.9F;
			this.nose.translateZ = -0.09375F;
			this.nose.translateY = 0.1875F;
		}
	}

	public ModelPart getNose() {
		return this.nose;
	}

	public void setHoldingItem(boolean bl) {
		this.holdingItem = bl;
	}
}
