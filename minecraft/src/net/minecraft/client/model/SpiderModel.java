package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class SpiderModel<T extends Entity> extends ListModel<T> {
	private final ModelPart head;
	private final ModelPart body0;
	private final ModelPart body1;
	private final ModelPart leg0;
	private final ModelPart leg1;
	private final ModelPart leg2;
	private final ModelPart leg3;
	private final ModelPart leg4;
	private final ModelPart leg5;
	private final ModelPart leg6;
	private final ModelPart leg7;

	public SpiderModel() {
		float f = 0.0F;
		int i = 15;
		this.head = new ModelPart(this, 32, 4);
		this.head.addBox(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F, 0.0F);
		this.head.setPos(0.0F, 15.0F, -3.0F);
		this.body0 = new ModelPart(this, 0, 0);
		this.body0.addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, 0.0F);
		this.body0.setPos(0.0F, 15.0F, 0.0F);
		this.body1 = new ModelPart(this, 0, 12);
		this.body1.addBox(-5.0F, -4.0F, -6.0F, 10.0F, 8.0F, 12.0F, 0.0F);
		this.body1.setPos(0.0F, 15.0F, 9.0F);
		this.leg0 = new ModelPart(this, 18, 0);
		this.leg0.addBox(-15.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F, 0.0F);
		this.leg0.setPos(-4.0F, 15.0F, 2.0F);
		this.leg1 = new ModelPart(this, 18, 0);
		this.leg1.addBox(-1.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F, 0.0F);
		this.leg1.setPos(4.0F, 15.0F, 2.0F);
		this.leg2 = new ModelPart(this, 18, 0);
		this.leg2.addBox(-15.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F, 0.0F);
		this.leg2.setPos(-4.0F, 15.0F, 1.0F);
		this.leg3 = new ModelPart(this, 18, 0);
		this.leg3.addBox(-1.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F, 0.0F);
		this.leg3.setPos(4.0F, 15.0F, 1.0F);
		this.leg4 = new ModelPart(this, 18, 0);
		this.leg4.addBox(-15.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F, 0.0F);
		this.leg4.setPos(-4.0F, 15.0F, 0.0F);
		this.leg5 = new ModelPart(this, 18, 0);
		this.leg5.addBox(-1.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F, 0.0F);
		this.leg5.setPos(4.0F, 15.0F, 0.0F);
		this.leg6 = new ModelPart(this, 18, 0);
		this.leg6.addBox(-15.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F, 0.0F);
		this.leg6.setPos(-4.0F, 15.0F, -1.0F);
		this.leg7 = new ModelPart(this, 18, 0);
		this.leg7.addBox(-1.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F, 0.0F);
		this.leg7.setPos(4.0F, 15.0F, -1.0F);
	}

	@Override
	public Iterable<ModelPart> parts() {
		return ImmutableList.<ModelPart>of(this.head, this.body0, this.body1, this.leg0, this.leg1, this.leg2, this.leg3, this.leg4, this.leg5, this.leg6, this.leg7);
	}

	@Override
	public void setupAnim(T entity, float f, float g, float h, float i, float j, float k) {
		this.head.yRot = i * (float) (Math.PI / 180.0);
		this.head.xRot = j * (float) (Math.PI / 180.0);
		float l = (float) (Math.PI / 4);
		this.leg0.zRot = (float) (-Math.PI / 4);
		this.leg1.zRot = (float) (Math.PI / 4);
		this.leg2.zRot = -0.58119464F;
		this.leg3.zRot = 0.58119464F;
		this.leg4.zRot = -0.58119464F;
		this.leg5.zRot = 0.58119464F;
		this.leg6.zRot = (float) (-Math.PI / 4);
		this.leg7.zRot = (float) (Math.PI / 4);
		float m = -0.0F;
		float n = (float) (Math.PI / 8);
		this.leg0.yRot = (float) (Math.PI / 4);
		this.leg1.yRot = (float) (-Math.PI / 4);
		this.leg2.yRot = (float) (Math.PI / 8);
		this.leg3.yRot = (float) (-Math.PI / 8);
		this.leg4.yRot = (float) (-Math.PI / 8);
		this.leg5.yRot = (float) (Math.PI / 8);
		this.leg6.yRot = (float) (-Math.PI / 4);
		this.leg7.yRot = (float) (Math.PI / 4);
		float o = -(Mth.cos(f * 0.6662F * 2.0F + 0.0F) * 0.4F) * g;
		float p = -(Mth.cos(f * 0.6662F * 2.0F + (float) Math.PI) * 0.4F) * g;
		float q = -(Mth.cos(f * 0.6662F * 2.0F + (float) (Math.PI / 2)) * 0.4F) * g;
		float r = -(Mth.cos(f * 0.6662F * 2.0F + (float) (Math.PI * 3.0 / 2.0)) * 0.4F) * g;
		float s = Math.abs(Mth.sin(f * 0.6662F + 0.0F) * 0.4F) * g;
		float t = Math.abs(Mth.sin(f * 0.6662F + (float) Math.PI) * 0.4F) * g;
		float u = Math.abs(Mth.sin(f * 0.6662F + (float) (Math.PI / 2)) * 0.4F) * g;
		float v = Math.abs(Mth.sin(f * 0.6662F + (float) (Math.PI * 3.0 / 2.0)) * 0.4F) * g;
		this.leg0.yRot += o;
		this.leg1.yRot += -o;
		this.leg2.yRot += p;
		this.leg3.yRot += -p;
		this.leg4.yRot += q;
		this.leg5.yRot += -q;
		this.leg6.yRot += r;
		this.leg7.yRot += -r;
		this.leg0.zRot += s;
		this.leg1.zRot += -s;
		this.leg2.zRot += t;
		this.leg3.zRot += -t;
		this.leg4.zRot += u;
		this.leg5.zRot += -u;
		this.leg6.zRot += v;
		this.leg7.zRot += -v;
	}
}
