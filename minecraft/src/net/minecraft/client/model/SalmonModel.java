package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class SalmonModel<T extends Entity> extends EntityModel<T> {
	private final ModelPart bodyFront;
	private final ModelPart bodyBack;
	private final ModelPart head;
	private final ModelPart topFin0;
	private final ModelPart topFin1;
	private final ModelPart backFin;
	private final ModelPart sideFin0;
	private final ModelPart sideFin1;

	public SalmonModel() {
		this.texWidth = 32;
		this.texHeight = 32;
		int i = 20;
		this.bodyFront = new ModelPart(this, 0, 0);
		this.bodyFront.addBox(-1.5F, -2.5F, 0.0F, 3.0F, 5.0F, 8.0F);
		this.bodyFront.setPos(0.0F, 20.0F, 0.0F);
		this.bodyBack = new ModelPart(this, 0, 13);
		this.bodyBack.addBox(-1.5F, -2.5F, 0.0F, 3.0F, 5.0F, 8.0F);
		this.bodyBack.setPos(0.0F, 20.0F, 8.0F);
		this.head = new ModelPart(this, 22, 0);
		this.head.addBox(-1.0F, -2.0F, -3.0F, 2.0F, 4.0F, 3.0F);
		this.head.setPos(0.0F, 20.0F, 0.0F);
		this.backFin = new ModelPart(this, 20, 10);
		this.backFin.addBox(0.0F, -2.5F, 0.0F, 0.0F, 5.0F, 6.0F);
		this.backFin.setPos(0.0F, 0.0F, 8.0F);
		this.bodyBack.addChild(this.backFin);
		this.topFin0 = new ModelPart(this, 2, 1);
		this.topFin0.addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 3.0F);
		this.topFin0.setPos(0.0F, -4.5F, 5.0F);
		this.bodyFront.addChild(this.topFin0);
		this.topFin1 = new ModelPart(this, 0, 2);
		this.topFin1.addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 4.0F);
		this.topFin1.setPos(0.0F, -4.5F, -1.0F);
		this.bodyBack.addChild(this.topFin1);
		this.sideFin0 = new ModelPart(this, -4, 0);
		this.sideFin0.addBox(-2.0F, 0.0F, 0.0F, 2.0F, 0.0F, 2.0F);
		this.sideFin0.setPos(-1.5F, 21.5F, 0.0F);
		this.sideFin0.zRot = (float) (-Math.PI / 4);
		this.sideFin1 = new ModelPart(this, 0, 0);
		this.sideFin1.addBox(0.0F, 0.0F, 0.0F, 2.0F, 0.0F, 2.0F);
		this.sideFin1.setPos(1.5F, 21.5F, 0.0F);
		this.sideFin1.zRot = (float) (Math.PI / 4);
	}

	@Override
	public void render(T entity, float f, float g, float h, float i, float j, float k) {
		this.setupAnim(entity, f, g, h, i, j, k);
		this.bodyFront.render(k);
		this.bodyBack.render(k);
		this.head.render(k);
		this.sideFin0.render(k);
		this.sideFin1.render(k);
	}

	@Override
	public void setupAnim(T entity, float f, float g, float h, float i, float j, float k) {
		float l = 1.0F;
		float m = 1.0F;
		if (!entity.isInWater()) {
			l = 1.3F;
			m = 1.7F;
		}

		this.bodyBack.yRot = -l * 0.25F * Mth.sin(m * 0.6F * h);
	}
}
