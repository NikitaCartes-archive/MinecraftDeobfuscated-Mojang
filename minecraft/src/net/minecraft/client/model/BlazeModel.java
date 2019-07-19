package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class BlazeModel<T extends Entity> extends EntityModel<T> {
	private final ModelPart[] upperBodyParts = new ModelPart[12];
	private final ModelPart head;

	public BlazeModel() {
		for (int i = 0; i < this.upperBodyParts.length; i++) {
			this.upperBodyParts[i] = new ModelPart(this, 0, 16);
			this.upperBodyParts[i].addBox(0.0F, 0.0F, 0.0F, 2, 8, 2);
		}

		this.head = new ModelPart(this, 0, 0);
		this.head.addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8);
	}

	@Override
	public void render(T entity, float f, float g, float h, float i, float j, float k) {
		this.setupAnim(entity, f, g, h, i, j, k);
		this.head.render(k);

		for (ModelPart modelPart : this.upperBodyParts) {
			modelPart.render(k);
		}
	}

	@Override
	public void setupAnim(T entity, float f, float g, float h, float i, float j, float k) {
		float l = h * (float) Math.PI * -0.1F;

		for (int m = 0; m < 4; m++) {
			this.upperBodyParts[m].y = -2.0F + Mth.cos(((float)(m * 2) + h) * 0.25F);
			this.upperBodyParts[m].x = Mth.cos(l) * 9.0F;
			this.upperBodyParts[m].z = Mth.sin(l) * 9.0F;
			l++;
		}

		l = (float) (Math.PI / 4) + h * (float) Math.PI * 0.03F;

		for (int m = 4; m < 8; m++) {
			this.upperBodyParts[m].y = 2.0F + Mth.cos(((float)(m * 2) + h) * 0.25F);
			this.upperBodyParts[m].x = Mth.cos(l) * 7.0F;
			this.upperBodyParts[m].z = Mth.sin(l) * 7.0F;
			l++;
		}

		l = 0.47123894F + h * (float) Math.PI * -0.05F;

		for (int m = 8; m < 12; m++) {
			this.upperBodyParts[m].y = 11.0F + Mth.cos(((float)m * 1.5F + h) * 0.5F);
			this.upperBodyParts[m].x = Mth.cos(l) * 5.0F;
			this.upperBodyParts[m].z = Mth.sin(l) * 5.0F;
			l++;
		}

		this.head.yRot = i * (float) (Math.PI / 180.0);
		this.head.xRot = j * (float) (Math.PI / 180.0);
	}
}
