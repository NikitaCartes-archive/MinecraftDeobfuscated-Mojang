package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.hoglin.Hoglin;

@Environment(EnvType.CLIENT)
public class HoglinModel extends AgeableListModel<Hoglin> {
	private final ModelPart head;
	private final ModelPart rightEar;
	private final ModelPart leftEar;
	private final ModelPart body;
	private final ModelPart frontRightLeg;
	private final ModelPart frontLeftLeg;
	private final ModelPart backRightLeg;
	private final ModelPart backLeftLeg;

	public HoglinModel() {
		super(true, 8.0F, 6.0F, 1.9F, 2.0F, 24.0F);
		this.texWidth = 128;
		this.texHeight = 128;
		this.body = new ModelPart(this);
		this.body.setPos(0.0F, 7.0F, 0.0F);
		this.body.texOffs(1, 1).addBox(-8.0F, -7.0F, -13.0F, 16.0F, 14.0F, 26.0F);
		ModelPart modelPart = new ModelPart(this);
		modelPart.setPos(0.0F, -14.0F, -7.0F);
		modelPart.texOffs(5, 67).addBox(0.0F, 0.0F, -9.0F, 0.0F, 10.0F, 19.0F, 0.001F);
		this.body.addChild(modelPart);
		this.head = new ModelPart(this);
		this.head.setPos(0.0F, 2.0F, -12.0F);
		this.head.texOffs(1, 42).addBox(-7.0F, -3.0F, -19.0F, 14.0F, 6.0F, 19.0F);
		this.rightEar = new ModelPart(this);
		this.rightEar.setPos(-6.0F, -2.0F, -3.0F);
		this.rightEar.texOffs(4, 16).addBox(-6.0F, -1.0F, -2.0F, 6.0F, 1.0F, 4.0F);
		this.rightEar.zRot = (float) (-Math.PI * 2.0 / 9.0);
		this.head.addChild(this.rightEar);
		this.leftEar = new ModelPart(this);
		this.leftEar.setPos(6.0F, -2.0F, -3.0F);
		this.leftEar.texOffs(4, 21).addBox(0.0F, -1.0F, -2.0F, 6.0F, 1.0F, 4.0F);
		this.leftEar.zRot = (float) (Math.PI * 2.0 / 9.0);
		this.head.addChild(this.leftEar);
		ModelPart modelPart2 = new ModelPart(this);
		modelPart2.setPos(-7.0F, 2.0F, -12.0F);
		modelPart2.texOffs(6, 45).addBox(-1.0F, -11.0F, -1.0F, 2.0F, 11.0F, 2.0F);
		this.head.addChild(modelPart2);
		ModelPart modelPart3 = new ModelPart(this);
		modelPart3.setPos(7.0F, 2.0F, -12.0F);
		modelPart3.texOffs(6, 45).addBox(-1.0F, -11.0F, -1.0F, 2.0F, 11.0F, 2.0F);
		this.head.addChild(modelPart3);
		this.head.xRot = 0.87266463F;
		int i = 14;
		int j = 11;
		this.frontRightLeg = new ModelPart(this);
		this.frontRightLeg.setPos(-4.0F, 10.0F, -8.5F);
		this.frontRightLeg.texOffs(46, 75).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 14.0F, 6.0F);
		this.frontLeftLeg = new ModelPart(this);
		this.frontLeftLeg.setPos(4.0F, 10.0F, -8.5F);
		this.frontLeftLeg.texOffs(71, 75).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 14.0F, 6.0F);
		this.backRightLeg = new ModelPart(this);
		this.backRightLeg.setPos(-5.0F, 13.0F, 10.0F);
		this.backRightLeg.texOffs(51, 43).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 11.0F, 5.0F);
		this.backLeftLeg = new ModelPart(this);
		this.backLeftLeg.setPos(5.0F, 13.0F, 10.0F);
		this.backLeftLeg.texOffs(72, 43).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 11.0F, 5.0F);
	}

	@Override
	protected Iterable<ModelPart> headParts() {
		return ImmutableList.<ModelPart>of(this.head);
	}

	@Override
	protected Iterable<ModelPart> bodyParts() {
		return ImmutableList.<ModelPart>of(this.body, this.frontRightLeg, this.frontLeftLeg, this.backRightLeg, this.backLeftLeg);
	}

	public void setupAnim(Hoglin hoglin, float f, float g, float h, float i, float j) {
		this.rightEar.zRot = (float) (-Math.PI * 2.0 / 9.0) - g * Mth.sin(f);
		this.leftEar.zRot = (float) (Math.PI * 2.0 / 9.0) + g * Mth.sin(f);
		this.head.yRot = i * (float) (Math.PI / 180.0);
		float k = 1.0F - (float)Mth.abs(10 - 2 * hoglin.getAttackAnimationRemainingTicks()) / 10.0F;
		this.head.xRot = Mth.lerp(k, 0.87266463F, (float) (-Math.PI / 9));
		float l = 1.2F;
		this.frontRightLeg.xRot = Mth.cos(f) * 1.2F * g;
		this.frontLeftLeg.xRot = Mth.cos(f + (float) Math.PI) * 1.2F * g;
		this.backRightLeg.xRot = this.frontLeftLeg.xRot;
		this.backLeftLeg.xRot = this.frontRightLeg.xRot;
	}
}
