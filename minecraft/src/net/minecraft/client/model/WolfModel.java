package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Wolf;

@Environment(EnvType.CLIENT)
public class WolfModel<T extends Wolf> extends ColorableAgeableListModel<T> {
	private final ModelPart head;
	private final ModelPart realHead;
	private final ModelPart body;
	private final ModelPart leg0;
	private final ModelPart leg1;
	private final ModelPart leg2;
	private final ModelPart leg3;
	private final ModelPart tail;
	private final ModelPart realTail;
	private final ModelPart upperBody;

	public WolfModel() {
		float f = 0.0F;
		float g = 13.5F;
		this.head = new ModelPart(this, 0, 0);
		this.head.setPos(-1.0F, 13.5F, -7.0F);
		this.realHead = new ModelPart(this, 0, 0);
		this.realHead.addBox(-2.0F, -3.0F, -2.0F, 6.0F, 6.0F, 4.0F, 0.0F);
		this.head.addChild(this.realHead);
		this.body = new ModelPart(this, 18, 14);
		this.body.addBox(-3.0F, -2.0F, -3.0F, 6.0F, 9.0F, 6.0F, 0.0F);
		this.body.setPos(0.0F, 14.0F, 2.0F);
		this.upperBody = new ModelPart(this, 21, 0);
		this.upperBody.addBox(-3.0F, -3.0F, -3.0F, 8.0F, 6.0F, 7.0F, 0.0F);
		this.upperBody.setPos(-1.0F, 14.0F, 2.0F);
		this.leg0 = new ModelPart(this, 0, 18);
		this.leg0.addBox(0.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, 0.0F);
		this.leg0.setPos(-2.5F, 16.0F, 7.0F);
		this.leg1 = new ModelPart(this, 0, 18);
		this.leg1.addBox(0.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, 0.0F);
		this.leg1.setPos(0.5F, 16.0F, 7.0F);
		this.leg2 = new ModelPart(this, 0, 18);
		this.leg2.addBox(0.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, 0.0F);
		this.leg2.setPos(-2.5F, 16.0F, -4.0F);
		this.leg3 = new ModelPart(this, 0, 18);
		this.leg3.addBox(0.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, 0.0F);
		this.leg3.setPos(0.5F, 16.0F, -4.0F);
		this.tail = new ModelPart(this, 9, 18);
		this.tail.setPos(-1.0F, 12.0F, 8.0F);
		this.realTail = new ModelPart(this, 9, 18);
		this.realTail.addBox(0.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, 0.0F);
		this.tail.addChild(this.realTail);
		this.realHead.texOffs(16, 14).addBox(-2.0F, -5.0F, 0.0F, 2.0F, 2.0F, 1.0F, 0.0F);
		this.realHead.texOffs(16, 14).addBox(2.0F, -5.0F, 0.0F, 2.0F, 2.0F, 1.0F, 0.0F);
		this.realHead.texOffs(0, 10).addBox(-0.5F, 0.0F, -5.0F, 3.0F, 3.0F, 4.0F, 0.0F);
	}

	@Override
	protected Iterable<ModelPart> headParts() {
		return ImmutableList.<ModelPart>of(this.head);
	}

	@Override
	protected Iterable<ModelPart> bodyParts() {
		return ImmutableList.<ModelPart>of(this.body, this.leg0, this.leg1, this.leg2, this.leg3, this.tail, this.upperBody);
	}

	public void prepareMobModel(T wolf, float f, float g, float h) {
		if (wolf.isAngry()) {
			this.tail.yRot = 0.0F;
		} else {
			this.tail.yRot = Mth.cos(f * 0.6662F) * 1.4F * g;
		}

		if (wolf.isInSittingPose()) {
			this.upperBody.setPos(-1.0F, 16.0F, -3.0F);
			this.upperBody.xRot = (float) (Math.PI * 2.0 / 5.0);
			this.upperBody.yRot = 0.0F;
			this.body.setPos(0.0F, 18.0F, 0.0F);
			this.body.xRot = (float) (Math.PI / 4);
			this.tail.setPos(-1.0F, 21.0F, 6.0F);
			this.leg0.setPos(-2.5F, 22.7F, 2.0F);
			this.leg0.xRot = (float) (Math.PI * 3.0 / 2.0);
			this.leg1.setPos(0.5F, 22.7F, 2.0F);
			this.leg1.xRot = (float) (Math.PI * 3.0 / 2.0);
			this.leg2.xRot = 5.811947F;
			this.leg2.setPos(-2.49F, 17.0F, -4.0F);
			this.leg3.xRot = 5.811947F;
			this.leg3.setPos(0.51F, 17.0F, -4.0F);
		} else {
			this.body.setPos(0.0F, 14.0F, 2.0F);
			this.body.xRot = (float) (Math.PI / 2);
			this.upperBody.setPos(-1.0F, 14.0F, -3.0F);
			this.upperBody.xRot = this.body.xRot;
			this.tail.setPos(-1.0F, 12.0F, 8.0F);
			this.leg0.setPos(-2.5F, 16.0F, 7.0F);
			this.leg1.setPos(0.5F, 16.0F, 7.0F);
			this.leg2.setPos(-2.5F, 16.0F, -4.0F);
			this.leg3.setPos(0.5F, 16.0F, -4.0F);
			this.leg0.xRot = Mth.cos(f * 0.6662F) * 1.4F * g;
			this.leg1.xRot = Mth.cos(f * 0.6662F + (float) Math.PI) * 1.4F * g;
			this.leg2.xRot = Mth.cos(f * 0.6662F + (float) Math.PI) * 1.4F * g;
			this.leg3.xRot = Mth.cos(f * 0.6662F) * 1.4F * g;
		}

		this.realHead.zRot = wolf.getHeadRollAngle(h) + wolf.getBodyRollAngle(h, 0.0F);
		this.upperBody.zRot = wolf.getBodyRollAngle(h, -0.08F);
		this.body.zRot = wolf.getBodyRollAngle(h, -0.16F);
		this.realTail.zRot = wolf.getBodyRollAngle(h, -0.2F);
	}

	public void setupAnim(T wolf, float f, float g, float h, float i, float j) {
		this.head.xRot = j * (float) (Math.PI / 180.0);
		this.head.yRot = i * (float) (Math.PI / 180.0);
		this.tail.xRot = h;
	}
}
