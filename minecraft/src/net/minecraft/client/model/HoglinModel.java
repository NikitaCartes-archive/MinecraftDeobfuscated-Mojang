package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.hoglin.HoglinBase;

@Environment(EnvType.CLIENT)
public class HoglinModel<T extends Mob & HoglinBase> extends AgeableListModel<T> {
	private final ModelPart head;
	private final ModelPart rightEar;
	private final ModelPart leftEar;
	private final ModelPart body;
	private final ModelPart frontRightLeg;
	private final ModelPart frontLeftLeg;
	private final ModelPart backRightLeg;
	private final ModelPart backLeftLeg;
	private final ModelPart mane;

	public HoglinModel() {
		super(true, 8.0F, 6.0F, 1.9F, 2.0F, 24.0F);
		this.texWidth = 128;
		this.texHeight = 64;
		this.body = new ModelPart(this);
		this.body.setPos(0.0F, 7.0F, 0.0F);
		this.body.texOffs(1, 1).addBox(-8.0F, -7.0F, -13.0F, 16.0F, 14.0F, 26.0F);
		this.mane = new ModelPart(this);
		this.mane.setPos(0.0F, -14.0F, -5.0F);
		this.mane.texOffs(90, 33).addBox(0.0F, 0.0F, -9.0F, 0.0F, 10.0F, 19.0F, 0.001F);
		this.body.addChild(this.mane);
		this.head = new ModelPart(this);
		this.head.setPos(0.0F, 2.0F, -12.0F);
		this.head.texOffs(61, 1).addBox(-7.0F, -3.0F, -19.0F, 14.0F, 6.0F, 19.0F);
		this.rightEar = new ModelPart(this);
		this.rightEar.setPos(-6.0F, -2.0F, -3.0F);
		this.rightEar.texOffs(1, 1).addBox(-6.0F, -1.0F, -2.0F, 6.0F, 1.0F, 4.0F);
		this.rightEar.zRot = (float) (-Math.PI * 2.0 / 9.0);
		this.head.addChild(this.rightEar);
		this.leftEar = new ModelPart(this);
		this.leftEar.setPos(6.0F, -2.0F, -3.0F);
		this.leftEar.texOffs(1, 6).addBox(0.0F, -1.0F, -2.0F, 6.0F, 1.0F, 4.0F);
		this.leftEar.zRot = (float) (Math.PI * 2.0 / 9.0);
		this.head.addChild(this.leftEar);
		ModelPart modelPart = new ModelPart(this);
		modelPart.setPos(-7.0F, 2.0F, -12.0F);
		modelPart.texOffs(1, 13).addBox(-1.0F, -11.0F, -1.0F, 2.0F, 11.0F, 2.0F);
		this.head.addChild(modelPart);
		ModelPart modelPart2 = new ModelPart(this);
		modelPart2.setPos(7.0F, 2.0F, -12.0F);
		modelPart2.texOffs(1, 13).addBox(-1.0F, -11.0F, -1.0F, 2.0F, 11.0F, 2.0F);
		this.head.addChild(modelPart2);
		this.head.xRot = 0.87266463F;
		int i = 14;
		int j = 11;
		this.frontRightLeg = new ModelPart(this);
		this.frontRightLeg.setPos(-4.0F, 10.0F, -8.5F);
		this.frontRightLeg.texOffs(66, 42).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 14.0F, 6.0F);
		this.frontLeftLeg = new ModelPart(this);
		this.frontLeftLeg.setPos(4.0F, 10.0F, -8.5F);
		this.frontLeftLeg.texOffs(41, 42).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 14.0F, 6.0F);
		this.backRightLeg = new ModelPart(this);
		this.backRightLeg.setPos(-5.0F, 13.0F, 10.0F);
		this.backRightLeg.texOffs(21, 45).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 11.0F, 5.0F);
		this.backLeftLeg = new ModelPart(this);
		this.backLeftLeg.setPos(5.0F, 13.0F, 10.0F);
		this.backLeftLeg.texOffs(0, 45).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 11.0F, 5.0F);
	}

	@Override
	protected Iterable<ModelPart> headParts() {
		return ImmutableList.<ModelPart>of(this.head);
	}

	@Override
	protected Iterable<ModelPart> bodyParts() {
		return ImmutableList.<ModelPart>of(this.body, this.frontRightLeg, this.frontLeftLeg, this.backRightLeg, this.backLeftLeg);
	}

	public void setupAnim(T mob, float f, float g, float h, float i, float j) {
		this.rightEar.zRot = (float) (-Math.PI * 2.0 / 9.0) - g * Mth.sin(f);
		this.leftEar.zRot = (float) (Math.PI * 2.0 / 9.0) + g * Mth.sin(f);
		this.head.yRot = i * (float) (Math.PI / 180.0);
		int k = mob.getAttackAnimationRemainingTicks();
		float l = 1.0F - (float)Mth.abs(10 - 2 * k) / 10.0F;
		this.head.xRot = Mth.lerp(l, 0.87266463F, (float) (-Math.PI / 9));
		if (mob.isBaby()) {
			this.head.y = Mth.lerp(l, 2.0F, 5.0F);
			this.mane.z = -3.0F;
		} else {
			this.head.y = 2.0F;
			this.mane.z = -7.0F;
		}

		float m = 1.2F;
		this.frontRightLeg.xRot = Mth.cos(f) * 1.2F * g;
		this.frontLeftLeg.xRot = Mth.cos(f + (float) Math.PI) * 1.2F * g;
		this.backRightLeg.xRot = this.frontLeftLeg.xRot;
		this.backLeftLeg.xRot = this.frontRightLeg.xRot;
	}
}
