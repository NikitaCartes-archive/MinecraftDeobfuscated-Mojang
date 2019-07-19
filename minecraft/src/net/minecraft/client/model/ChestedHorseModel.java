package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;

@Environment(EnvType.CLIENT)
public class ChestedHorseModel<T extends AbstractChestedHorse> extends HorseModel<T> {
	private final ModelPart boxL = new ModelPart(this, 26, 21);
	private final ModelPart boxR;

	public ChestedHorseModel(float f) {
		super(f);
		this.boxL.addBox(-4.0F, 0.0F, -2.0F, 8, 8, 3);
		this.boxR = new ModelPart(this, 26, 21);
		this.boxR.addBox(-4.0F, 0.0F, -2.0F, 8, 8, 3);
		this.boxL.yRot = (float) (-Math.PI / 2);
		this.boxR.yRot = (float) (Math.PI / 2);
		this.boxL.setPos(6.0F, -8.0F, 0.0F);
		this.boxR.setPos(-6.0F, -8.0F, 0.0F);
		this.body.addChild(this.boxL);
		this.body.addChild(this.boxR);
	}

	@Override
	protected void addEarModels(ModelPart modelPart) {
		ModelPart modelPart2 = new ModelPart(this, 0, 12);
		modelPart2.addBox(-1.0F, -7.0F, 0.0F, 2, 7, 1);
		modelPart2.setPos(1.25F, -10.0F, 4.0F);
		ModelPart modelPart3 = new ModelPart(this, 0, 12);
		modelPart3.addBox(-1.0F, -7.0F, 0.0F, 2, 7, 1);
		modelPart3.setPos(-1.25F, -10.0F, 4.0F);
		modelPart2.xRot = (float) (Math.PI / 12);
		modelPart2.zRot = (float) (Math.PI / 12);
		modelPart3.xRot = (float) (Math.PI / 12);
		modelPart3.zRot = (float) (-Math.PI / 12);
		modelPart.addChild(modelPart2);
		modelPart.addChild(modelPart3);
	}

	public void render(T abstractChestedHorse, float f, float g, float h, float i, float j, float k) {
		if (abstractChestedHorse.hasChest()) {
			this.boxL.visible = true;
			this.boxR.visible = true;
		} else {
			this.boxL.visible = false;
			this.boxR.visible = false;
		}

		super.render(abstractChestedHorse, f, g, h, i, j, k);
	}
}
