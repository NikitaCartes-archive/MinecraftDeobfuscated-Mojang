package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.monster.AbstractIllager;

@Environment(EnvType.CLIENT)
public class PillagerModel<T extends AbstractIllager> extends IllagerModel<T> {
	public PillagerModel(float f, float g, int i, int j) {
		super(f, g, i, j);
	}

	@Override
	public void render(T abstractIllager, float f, float g, float h, float i, float j, float k) {
		this.setupAnim(abstractIllager, f, g, h, i, j, k);
		this.head.render(k);
		this.body.render(k);
		this.leftLeg.render(k);
		this.rightLeg.render(k);
		this.rightArm.render(k);
		this.leftArm.render(k);
	}
}
