package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class CowModel<T extends Entity> extends QuadrupedModel<T> {
	public CowModel() {
		super(12, 0.0F);
		this.head = new ModelPart(this, 0, 0);
		this.head.addBox(-4.0F, -4.0F, -6.0F, 8, 8, 6, 0.0F);
		this.head.setPos(0.0F, 4.0F, -8.0F);
		this.head.texOffs(22, 0).addBox(-5.0F, -5.0F, -4.0F, 1, 3, 1, 0.0F);
		this.head.texOffs(22, 0).addBox(4.0F, -5.0F, -4.0F, 1, 3, 1, 0.0F);
		this.body = new ModelPart(this, 18, 4);
		this.body.addBox(-6.0F, -10.0F, -7.0F, 12, 18, 10, 0.0F);
		this.body.setPos(0.0F, 5.0F, 2.0F);
		this.body.texOffs(52, 0).addBox(-2.0F, 2.0F, -8.0F, 4, 6, 1);
		this.leg0.x--;
		this.leg1.x++;
		this.leg0.z += 0.0F;
		this.leg1.z += 0.0F;
		this.leg2.x--;
		this.leg3.x++;
		this.leg2.z--;
		this.leg3.z--;
		this.zHeadOffs += 2.0F;
	}

	public ModelPart getHead() {
		return this.head;
	}
}
