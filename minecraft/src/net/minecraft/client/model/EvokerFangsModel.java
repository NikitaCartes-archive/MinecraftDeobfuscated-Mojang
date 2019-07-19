package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class EvokerFangsModel<T extends Entity> extends EntityModel<T> {
	private final ModelPart base = new ModelPart(this, 0, 0);
	private final ModelPart upperJaw;
	private final ModelPart lowerJaw;

	public EvokerFangsModel() {
		this.base.setPos(-5.0F, 22.0F, -5.0F);
		this.base.addBox(0.0F, 0.0F, 0.0F, 10, 12, 10);
		this.upperJaw = new ModelPart(this, 40, 0);
		this.upperJaw.setPos(1.5F, 22.0F, -4.0F);
		this.upperJaw.addBox(0.0F, 0.0F, 0.0F, 4, 14, 8);
		this.lowerJaw = new ModelPart(this, 40, 0);
		this.lowerJaw.setPos(-1.5F, 22.0F, 4.0F);
		this.lowerJaw.addBox(0.0F, 0.0F, 0.0F, 4, 14, 8);
	}

	@Override
	public void render(T entity, float f, float g, float h, float i, float j, float k) {
		float l = f * 2.0F;
		if (l > 1.0F) {
			l = 1.0F;
		}

		l = 1.0F - l * l * l;
		this.upperJaw.zRot = (float) Math.PI - l * 0.35F * (float) Math.PI;
		this.lowerJaw.zRot = (float) Math.PI + l * 0.35F * (float) Math.PI;
		this.lowerJaw.yRot = (float) Math.PI;
		float m = (f + Mth.sin(f * 2.7F)) * 0.6F * 12.0F;
		this.upperJaw.y = 24.0F - m;
		this.lowerJaw.y = this.upperJaw.y;
		this.base.y = this.upperJaw.y;
		this.base.render(k);
		this.upperJaw.render(k);
		this.lowerJaw.render(k);
	}
}
