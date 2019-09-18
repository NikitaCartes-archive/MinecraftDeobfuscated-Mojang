package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class PigModel<T extends Entity> extends QuadrupedModel<T> {
	public PigModel() {
		this(0.0F);
	}

	public PigModel(float f) {
		super(6, f);
		this.head.texOffs(16, 16).addBox(-2.0F, 0.0F, -9.0F, 4.0F, 3.0F, 1.0F, f);
		this.yHeadOffs = 4.0F;
	}
}
