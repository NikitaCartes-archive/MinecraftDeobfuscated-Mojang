package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class SquidModel<T extends Entity> extends EntityModel<T> {
	private final ModelPart body;
	private final ModelPart[] tentacles = new ModelPart[8];

	public SquidModel() {
		int i = -16;
		this.body = new ModelPart(this, 0, 0);
		this.body.addBox(-6.0F, -8.0F, -6.0F, 12, 16, 12);
		this.body.y += 8.0F;

		for (int j = 0; j < this.tentacles.length; j++) {
			this.tentacles[j] = new ModelPart(this, 48, 0);
			double d = (double)j * Math.PI * 2.0 / (double)this.tentacles.length;
			float f = (float)Math.cos(d) * 5.0F;
			float g = (float)Math.sin(d) * 5.0F;
			this.tentacles[j].addBox(-1.0F, 0.0F, -1.0F, 2, 18, 2);
			this.tentacles[j].x = f;
			this.tentacles[j].z = g;
			this.tentacles[j].y = 15.0F;
			d = (double)j * Math.PI * -2.0 / (double)this.tentacles.length + (Math.PI / 2);
			this.tentacles[j].yRot = (float)d;
		}
	}

	@Override
	public void setupAnim(T entity, float f, float g, float h, float i, float j, float k) {
		for (ModelPart modelPart : this.tentacles) {
			modelPart.xRot = h;
		}
	}

	@Override
	public void render(T entity, float f, float g, float h, float i, float j, float k) {
		this.setupAnim(entity, f, g, h, i, j, k);
		this.body.render(k);

		for (ModelPart modelPart : this.tentacles) {
			modelPart.render(k);
		}
	}
}
