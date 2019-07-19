package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;

@Environment(EnvType.CLIENT)
public class LargeChestModel extends ChestModel {
	public LargeChestModel() {
		this.lid = new ModelPart(this, 0, 0).setTexSize(128, 64);
		this.lid.addBox(0.0F, -5.0F, -14.0F, 30, 5, 14, 0.0F);
		this.lid.x = 1.0F;
		this.lid.y = 7.0F;
		this.lid.z = 15.0F;
		this.lock = new ModelPart(this, 0, 0).setTexSize(128, 64);
		this.lock.addBox(-1.0F, -2.0F, -15.0F, 2, 4, 1, 0.0F);
		this.lock.x = 16.0F;
		this.lock.y = 7.0F;
		this.lock.z = 15.0F;
		this.bottom = new ModelPart(this, 0, 19).setTexSize(128, 64);
		this.bottom.addBox(0.0F, 0.0F, 0.0F, 30, 10, 14, 0.0F);
		this.bottom.x = 1.0F;
		this.bottom.y = 6.0F;
		this.bottom.z = 1.0F;
	}
}
