package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class LeashKnotModel<T extends Entity> extends EntityModel<T> {
	private final ModelPart knot;

	public LeashKnotModel() {
		this(0, 0, 32, 32);
	}

	public LeashKnotModel(int i, int j, int k, int l) {
		this.texWidth = k;
		this.texHeight = l;
		this.knot = new ModelPart(this, i, j);
		this.knot.addBox(-3.0F, -6.0F, -3.0F, 6.0F, 8.0F, 6.0F, 0.0F);
		this.knot.setPos(0.0F, 0.0F, 0.0F);
	}

	@Override
	public void render(T entity, float f, float g, float h, float i, float j, float k) {
		this.setupAnim(entity, f, g, h, i, j, k);
		this.knot.render(k);
	}

	@Override
	public void setupAnim(T entity, float f, float g, float h, float i, float j, float k) {
		super.setupAnim(entity, f, g, h, i, j, k);
		this.knot.yRot = i * (float) (Math.PI / 180.0);
		this.knot.xRot = j * (float) (Math.PI / 180.0);
	}
}
