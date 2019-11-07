package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Fox;

@Environment(EnvType.CLIENT)
public class FoxModel<T extends Fox> extends AgeableListModel<T> {
	public final ModelPart head;
	private final ModelPart earL;
	private final ModelPart earR;
	private final ModelPart nose;
	private final ModelPart body;
	private final ModelPart leg0;
	private final ModelPart leg1;
	private final ModelPart leg2;
	private final ModelPart leg3;
	private final ModelPart tail;
	private float legMotionPos;

	public FoxModel() {
		super(true, 8.0F, 3.35F);
		this.texWidth = 48;
		this.texHeight = 32;
		this.head = new ModelPart(this, 1, 5);
		this.head.addBox(-3.0F, -2.0F, -5.0F, 8.0F, 6.0F, 6.0F);
		this.head.setPos(-1.0F, 16.5F, -3.0F);
		this.earL = new ModelPart(this, 8, 1);
		this.earL.addBox(-3.0F, -4.0F, -4.0F, 2.0F, 2.0F, 1.0F);
		this.earR = new ModelPart(this, 15, 1);
		this.earR.addBox(3.0F, -4.0F, -4.0F, 2.0F, 2.0F, 1.0F);
		this.nose = new ModelPart(this, 6, 18);
		this.nose.addBox(-1.0F, 2.01F, -8.0F, 4.0F, 2.0F, 3.0F);
		this.head.addChild(this.earL);
		this.head.addChild(this.earR);
		this.head.addChild(this.nose);
		this.body = new ModelPart(this, 24, 15);
		this.body.addBox(-3.0F, 3.999F, -3.5F, 6.0F, 11.0F, 6.0F);
		this.body.setPos(0.0F, 16.0F, -6.0F);
		float f = 0.001F;
		this.leg0 = new ModelPart(this, 13, 24);
		this.leg0.addBox(2.0F, 0.5F, -1.0F, 2.0F, 6.0F, 2.0F, 0.001F);
		this.leg0.setPos(-5.0F, 17.5F, 7.0F);
		this.leg1 = new ModelPart(this, 4, 24);
		this.leg1.addBox(2.0F, 0.5F, -1.0F, 2.0F, 6.0F, 2.0F, 0.001F);
		this.leg1.setPos(-1.0F, 17.5F, 7.0F);
		this.leg2 = new ModelPart(this, 13, 24);
		this.leg2.addBox(2.0F, 0.5F, -1.0F, 2.0F, 6.0F, 2.0F, 0.001F);
		this.leg2.setPos(-5.0F, 17.5F, 0.0F);
		this.leg3 = new ModelPart(this, 4, 24);
		this.leg3.addBox(2.0F, 0.5F, -1.0F, 2.0F, 6.0F, 2.0F, 0.001F);
		this.leg3.setPos(-1.0F, 17.5F, 0.0F);
		this.tail = new ModelPart(this, 30, 0);
		this.tail.addBox(2.0F, 0.0F, -1.0F, 4.0F, 9.0F, 5.0F);
		this.tail.setPos(-4.0F, 15.0F, -1.0F);
		this.body.addChild(this.tail);
	}

	public void prepareMobModel(T fox, float f, float g, float h) {
		this.body.xRot = (float) (Math.PI / 2);
		this.tail.xRot = -0.05235988F;
		this.leg0.xRot = Mth.cos(f * 0.6662F) * 1.4F * g;
		this.leg1.xRot = Mth.cos(f * 0.6662F + (float) Math.PI) * 1.4F * g;
		this.leg2.xRot = Mth.cos(f * 0.6662F + (float) Math.PI) * 1.4F * g;
		this.leg3.xRot = Mth.cos(f * 0.6662F) * 1.4F * g;
		this.head.setPos(-1.0F, 16.5F, -3.0F);
		this.head.yRot = 0.0F;
		this.head.zRot = fox.getHeadRollAngle(h);
		this.leg0.visible = true;
		this.leg1.visible = true;
		this.leg2.visible = true;
		this.leg3.visible = true;
		this.body.setPos(0.0F, 16.0F, -6.0F);
		this.body.zRot = 0.0F;
		this.leg0.setPos(-5.0F, 17.5F, 7.0F);
		this.leg1.setPos(-1.0F, 17.5F, 7.0F);
		if (fox.isCrouching()) {
			this.body.xRot = 1.6755161F;
			float i = fox.getCrouchAmount(h);
			this.body.setPos(0.0F, 16.0F + fox.getCrouchAmount(h), -6.0F);
			this.head.setPos(-1.0F, 16.5F + i, -3.0F);
			this.head.yRot = 0.0F;
		} else if (fox.isSleeping()) {
			this.body.zRot = (float) (-Math.PI / 2);
			this.body.setPos(0.0F, 21.0F, -6.0F);
			this.tail.xRot = (float) (-Math.PI * 5.0 / 6.0);
			if (this.young) {
				this.tail.xRot = -2.1816616F;
				this.body.setPos(0.0F, 21.0F, -2.0F);
			}

			this.head.setPos(1.0F, 19.49F, -3.0F);
			this.head.xRot = 0.0F;
			this.head.yRot = (float) (-Math.PI * 2.0 / 3.0);
			this.head.zRot = 0.0F;
			this.leg0.visible = false;
			this.leg1.visible = false;
			this.leg2.visible = false;
			this.leg3.visible = false;
		} else if (fox.isSitting()) {
			this.body.xRot = (float) (Math.PI / 6);
			this.body.setPos(0.0F, 9.0F, -3.0F);
			this.tail.xRot = (float) (Math.PI / 4);
			this.tail.setPos(-4.0F, 15.0F, -2.0F);
			this.head.setPos(-1.0F, 10.0F, -0.25F);
			this.head.xRot = 0.0F;
			this.head.yRot = 0.0F;
			if (this.young) {
				this.head.setPos(-1.0F, 13.0F, -3.75F);
			}

			this.leg0.xRot = (float) (-Math.PI * 5.0 / 12.0);
			this.leg0.setPos(-5.0F, 21.5F, 6.75F);
			this.leg1.xRot = (float) (-Math.PI * 5.0 / 12.0);
			this.leg1.setPos(-1.0F, 21.5F, 6.75F);
			this.leg2.xRot = (float) (-Math.PI / 12);
			this.leg3.xRot = (float) (-Math.PI / 12);
		}
	}

	@Override
	protected Iterable<ModelPart> headParts() {
		return ImmutableList.<ModelPart>of(this.head);
	}

	@Override
	protected Iterable<ModelPart> bodyParts() {
		return ImmutableList.<ModelPart>of(this.body, this.leg0, this.leg1, this.leg2, this.leg3);
	}

	public void setupAnim(T fox, float f, float g, float h, float i, float j) {
		if (!fox.isSleeping() && !fox.isFaceplanted() && !fox.isCrouching()) {
			this.head.xRot = j * (float) (Math.PI / 180.0);
			this.head.yRot = i * (float) (Math.PI / 180.0);
		}

		if (fox.isSleeping()) {
			this.head.xRot = 0.0F;
			this.head.yRot = (float) (-Math.PI * 2.0 / 3.0);
			this.head.zRot = Mth.cos(h * 0.027F) / 22.0F;
		}

		if (fox.isCrouching()) {
			float k = Mth.cos(h) * 0.01F;
			this.body.yRot = k;
			this.leg0.zRot = k;
			this.leg1.zRot = k;
			this.leg2.zRot = k / 2.0F;
			this.leg3.zRot = k / 2.0F;
		}

		if (fox.isFaceplanted()) {
			float k = 0.1F;
			this.legMotionPos += 0.67F;
			this.leg0.xRot = Mth.cos(this.legMotionPos * 0.4662F) * 0.1F;
			this.leg1.xRot = Mth.cos(this.legMotionPos * 0.4662F + (float) Math.PI) * 0.1F;
			this.leg2.xRot = Mth.cos(this.legMotionPos * 0.4662F + (float) Math.PI) * 0.1F;
			this.leg3.xRot = Mth.cos(this.legMotionPos * 0.4662F) * 0.1F;
		}
	}
}
