package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class MinecartModel<T extends Entity> extends EntityModel<T> {
	private final ModelPart[] cubes = new ModelPart[7];

	public MinecartModel() {
		this.cubes[0] = new ModelPart(this, 0, 10);
		this.cubes[1] = new ModelPart(this, 0, 0);
		this.cubes[2] = new ModelPart(this, 0, 0);
		this.cubes[3] = new ModelPart(this, 0, 0);
		this.cubes[4] = new ModelPart(this, 0, 0);
		this.cubes[5] = new ModelPart(this, 44, 10);
		int i = 20;
		int j = 8;
		int k = 16;
		int l = 4;
		this.cubes[0].addBox(-10.0F, -8.0F, -1.0F, 20, 16, 2, 0.0F);
		this.cubes[0].setPos(0.0F, 4.0F, 0.0F);
		this.cubes[5].addBox(-9.0F, -7.0F, -1.0F, 18, 14, 1, 0.0F);
		this.cubes[5].setPos(0.0F, 4.0F, 0.0F);
		this.cubes[1].addBox(-8.0F, -9.0F, -1.0F, 16, 8, 2, 0.0F);
		this.cubes[1].setPos(-9.0F, 4.0F, 0.0F);
		this.cubes[2].addBox(-8.0F, -9.0F, -1.0F, 16, 8, 2, 0.0F);
		this.cubes[2].setPos(9.0F, 4.0F, 0.0F);
		this.cubes[3].addBox(-8.0F, -9.0F, -1.0F, 16, 8, 2, 0.0F);
		this.cubes[3].setPos(0.0F, 4.0F, -7.0F);
		this.cubes[4].addBox(-8.0F, -9.0F, -1.0F, 16, 8, 2, 0.0F);
		this.cubes[4].setPos(0.0F, 4.0F, 7.0F);
		this.cubes[0].xRot = (float) (Math.PI / 2);
		this.cubes[1].yRot = (float) (Math.PI * 3.0 / 2.0);
		this.cubes[2].yRot = (float) (Math.PI / 2);
		this.cubes[3].yRot = (float) Math.PI;
		this.cubes[5].xRot = (float) (-Math.PI / 2);
	}

	@Override
	public void render(T entity, float f, float g, float h, float i, float j, float k) {
		this.cubes[5].y = 4.0F - h;

		for (int l = 0; l < 6; l++) {
			this.cubes[l].render(k);
		}
	}
}
