package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Bee;

@Environment(EnvType.CLIENT)
public class BeeModel<T extends Bee> extends AgeableListModel<T> {
	private final ModelPart bone;
	private final ModelPart body;
	private final ModelPart rightWing;
	private final ModelPart leftWing;
	private final ModelPart frontLeg;
	private final ModelPart midLeg;
	private final ModelPart backLeg;
	private final ModelPart stinger;
	private final ModelPart leftAntenna;
	private final ModelPart rightAntenna;
	private float rollAmount;

	public BeeModel() {
		super(RenderType::entityCutoutNoCull, false, 24.0F, 0.0F, 2.0F, 2.0F, 24.0F);
		this.texWidth = 64;
		this.texHeight = 64;
		this.bone = new ModelPart(this);
		this.bone.setPos(0.0F, 19.0F, 0.0F);
		this.body = new ModelPart(this, 0, 0);
		this.body.setPos(0.0F, 0.0F, 0.0F);
		this.bone.addChild(this.body);
		this.body.addBox(-3.5F, -4.0F, -5.0F, 7.0F, 7.0F, 10.0F, 0.0F);
		this.stinger = new ModelPart(this, 26, 7);
		this.stinger.addBox(0.0F, -1.0F, 5.0F, 0.0F, 1.0F, 2.0F, 0.0F);
		this.body.addChild(this.stinger);
		this.leftAntenna = new ModelPart(this, 2, 0);
		this.leftAntenna.setPos(0.0F, -2.0F, -5.0F);
		this.leftAntenna.addBox(1.5F, -2.0F, -3.0F, 1.0F, 2.0F, 3.0F, 0.0F);
		this.rightAntenna = new ModelPart(this, 2, 3);
		this.rightAntenna.setPos(0.0F, -2.0F, -5.0F);
		this.rightAntenna.addBox(-2.5F, -2.0F, -3.0F, 1.0F, 2.0F, 3.0F, 0.0F);
		this.body.addChild(this.leftAntenna);
		this.body.addChild(this.rightAntenna);
		this.rightWing = new ModelPart(this, 0, 18);
		this.rightWing.setPos(-1.5F, -4.0F, -3.0F);
		this.rightWing.xRot = 0.0F;
		this.rightWing.yRot = -0.2618F;
		this.rightWing.zRot = 0.0F;
		this.bone.addChild(this.rightWing);
		this.rightWing.addBox(-9.0F, 0.0F, 0.0F, 9.0F, 0.0F, 6.0F, 0.001F);
		this.leftWing = new ModelPart(this, 0, 18);
		this.leftWing.setPos(1.5F, -4.0F, -3.0F);
		this.leftWing.xRot = 0.0F;
		this.leftWing.yRot = 0.2618F;
		this.leftWing.zRot = 0.0F;
		this.leftWing.mirror = true;
		this.bone.addChild(this.leftWing);
		this.leftWing.addBox(0.0F, 0.0F, 0.0F, 9.0F, 0.0F, 6.0F, 0.001F);
		this.frontLeg = new ModelPart(this);
		this.frontLeg.setPos(1.5F, 3.0F, -2.0F);
		this.bone.addChild(this.frontLeg);
		this.frontLeg.addBox("frontLegBox", -5.0F, 0.0F, 0.0F, 7, 2, 0, 0.0F, 26, 1);
		this.midLeg = new ModelPart(this);
		this.midLeg.setPos(1.5F, 3.0F, 0.0F);
		this.bone.addChild(this.midLeg);
		this.midLeg.addBox("midLegBox", -5.0F, 0.0F, 0.0F, 7, 2, 0, 0.0F, 26, 3);
		this.backLeg = new ModelPart(this);
		this.backLeg.setPos(1.5F, 3.0F, 2.0F);
		this.bone.addChild(this.backLeg);
		this.backLeg.addBox("backLegBox", -5.0F, 0.0F, 0.0F, 7, 2, 0, 0.0F, 26, 5);
	}

	public void prepareMobModel(T bee, float f, float g, float h) {
		super.prepareMobModel(bee, f, g, h);
		this.rollAmount = bee.isBaby() ? 0.0F : bee.getRollAmount(h);
		this.stinger.visible = !bee.hasStung();
	}

	public void setupAnim(T bee, float f, float g, float h, float i, float j, float k) {
		this.rightWing.xRot = 0.0F;
		this.leftAntenna.xRot = 0.0F;
		this.rightAntenna.xRot = 0.0F;
		this.bone.xRot = 0.0F;
		this.bone.y = 19.0F;
		boolean bl = bee.onGround && bee.getDeltaMovement().lengthSqr() < 1.0E-7;
		if (bl) {
			this.rightWing.yRot = -0.2618F;
			this.rightWing.zRot = 0.0F;
			this.leftWing.xRot = 0.0F;
			this.leftWing.yRot = 0.2618F;
			this.leftWing.zRot = 0.0F;
			this.frontLeg.xRot = 0.0F;
			this.midLeg.xRot = 0.0F;
			this.backLeg.xRot = 0.0F;
		} else {
			float l = h * 2.1F;
			this.rightWing.yRot = 0.0F;
			this.rightWing.zRot = Mth.cos(l) * (float) Math.PI * 0.15F;
			this.leftWing.xRot = this.rightWing.xRot;
			this.leftWing.yRot = this.rightWing.yRot;
			this.leftWing.zRot = -this.rightWing.zRot;
			this.frontLeg.xRot = (float) (Math.PI / 4);
			this.midLeg.xRot = (float) (Math.PI / 4);
			this.backLeg.xRot = (float) (Math.PI / 4);
			this.bone.xRot = 0.0F;
			this.bone.yRot = 0.0F;
			this.bone.zRot = 0.0F;
		}

		if (!bee.isAngry()) {
			this.bone.xRot = 0.0F;
			this.bone.yRot = 0.0F;
			this.bone.zRot = 0.0F;
			if (!bl) {
				float l = Mth.cos(h * 0.18F);
				this.bone.xRot = 0.1F + l * (float) Math.PI * 0.025F;
				this.leftAntenna.xRot = l * (float) Math.PI * 0.03F;
				this.rightAntenna.xRot = l * (float) Math.PI * 0.03F;
				this.frontLeg.xRot = -l * (float) Math.PI * 0.1F + (float) (Math.PI / 8);
				this.backLeg.xRot = -l * (float) Math.PI * 0.05F + (float) (Math.PI / 4);
				this.bone.y = 19.0F - Mth.cos(h * 0.18F) * 0.9F;
			}
		}

		if (this.rollAmount > 0.0F) {
			this.bone.xRot = ModelUtils.rotlerpRad(this.bone.xRot, 3.0915928F, this.rollAmount);
		}
	}

	@Override
	protected Iterable<ModelPart> headParts() {
		return ImmutableList.<ModelPart>of();
	}

	@Override
	protected Iterable<ModelPart> bodyParts() {
		return ImmutableList.<ModelPart>of(this.bone);
	}
}
