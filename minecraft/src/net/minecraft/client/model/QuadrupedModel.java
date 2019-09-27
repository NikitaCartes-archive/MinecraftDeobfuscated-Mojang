package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class QuadrupedModel<T extends Entity> extends AgeableListModel<T> {
	protected ModelPart head = new ModelPart(this, 0, 0);
	protected ModelPart body;
	protected ModelPart leg0;
	protected ModelPart leg1;
	protected ModelPart leg2;
	protected ModelPart leg3;

	public QuadrupedModel(int i, float f, boolean bl, float g, float h, float j, float k, int l) {
		super(bl, g, h, j, k, (float)l);
		this.head.addBox(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F, f);
		this.head.setPos(0.0F, (float)(18 - i), -6.0F);
		this.body = new ModelPart(this, 28, 8);
		this.body.addBox(-5.0F, -10.0F, -7.0F, 10.0F, 16.0F, 8.0F, f);
		this.body.setPos(0.0F, (float)(17 - i), 2.0F);
		this.leg0 = new ModelPart(this, 0, 16);
		this.leg0.addBox(-2.0F, 0.0F, -2.0F, 4.0F, (float)i, 4.0F, f);
		this.leg0.setPos(-3.0F, (float)(24 - i), 7.0F);
		this.leg1 = new ModelPart(this, 0, 16);
		this.leg1.addBox(-2.0F, 0.0F, -2.0F, 4.0F, (float)i, 4.0F, f);
		this.leg1.setPos(3.0F, (float)(24 - i), 7.0F);
		this.leg2 = new ModelPart(this, 0, 16);
		this.leg2.addBox(-2.0F, 0.0F, -2.0F, 4.0F, (float)i, 4.0F, f);
		this.leg2.setPos(-3.0F, (float)(24 - i), -5.0F);
		this.leg3 = new ModelPart(this, 0, 16);
		this.leg3.addBox(-2.0F, 0.0F, -2.0F, 4.0F, (float)i, 4.0F, f);
		this.leg3.setPos(3.0F, (float)(24 - i), -5.0F);
	}

	@Override
	protected Iterable<ModelPart> headParts() {
		return ImmutableList.<ModelPart>of(this.head);
	}

	@Override
	protected Iterable<ModelPart> bodyParts() {
		return ImmutableList.<ModelPart>of(this.body, this.leg0, this.leg1, this.leg2, this.leg3);
	}

	@Override
	public void setupAnim(T entity, float f, float g, float h, float i, float j, float k) {
		this.head.xRot = j * (float) (Math.PI / 180.0);
		this.head.yRot = i * (float) (Math.PI / 180.0);
		this.body.xRot = (float) (Math.PI / 2);
		this.leg0.xRot = Mth.cos(f * 0.6662F) * 1.4F * g;
		this.leg1.xRot = Mth.cos(f * 0.6662F + (float) Math.PI) * 1.4F * g;
		this.leg2.xRot = Mth.cos(f * 0.6662F + (float) Math.PI) * 1.4F * g;
		this.leg3.xRot = Mth.cos(f * 0.6662F) * 1.4F * g;
	}
}
