package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Turtle;

@Environment(EnvType.CLIENT)
public class TurtleModel<T extends Turtle> extends QuadrupedModel<T> {
	private final ModelPart eggBelly;

	public TurtleModel(float f) {
		super(12, f, true, 120.0F, 0.0F, 9.0F, 6.0F, 120);
		this.texWidth = 128;
		this.texHeight = 64;
		this.head = new ModelPart(this, 3, 0);
		this.head.addBox(-3.0F, -1.0F, -3.0F, 6.0F, 5.0F, 6.0F, 0.0F);
		this.head.setPos(0.0F, 19.0F, -10.0F);
		this.body = new ModelPart(this);
		this.body.texOffs(7, 37).addBox(-9.5F, 3.0F, -10.0F, 19.0F, 20.0F, 6.0F, 0.0F);
		this.body.texOffs(31, 1).addBox(-5.5F, 3.0F, -13.0F, 11.0F, 18.0F, 3.0F, 0.0F);
		this.body.setPos(0.0F, 11.0F, -10.0F);
		this.eggBelly = new ModelPart(this);
		this.eggBelly.texOffs(70, 33).addBox(-4.5F, 3.0F, -14.0F, 9.0F, 18.0F, 1.0F, 0.0F);
		this.eggBelly.setPos(0.0F, 11.0F, -10.0F);
		int i = 1;
		this.leg0 = new ModelPart(this, 1, 23);
		this.leg0.addBox(-2.0F, 0.0F, 0.0F, 4.0F, 1.0F, 10.0F, 0.0F);
		this.leg0.setPos(-3.5F, 22.0F, 11.0F);
		this.leg1 = new ModelPart(this, 1, 12);
		this.leg1.addBox(-2.0F, 0.0F, 0.0F, 4.0F, 1.0F, 10.0F, 0.0F);
		this.leg1.setPos(3.5F, 22.0F, 11.0F);
		this.leg2 = new ModelPart(this, 27, 30);
		this.leg2.addBox(-13.0F, 0.0F, -2.0F, 13.0F, 1.0F, 5.0F, 0.0F);
		this.leg2.setPos(-5.0F, 21.0F, -4.0F);
		this.leg3 = new ModelPart(this, 27, 24);
		this.leg3.addBox(0.0F, 0.0F, -2.0F, 13.0F, 1.0F, 5.0F, 0.0F);
		this.leg3.setPos(5.0F, 21.0F, -4.0F);
	}

	@Override
	protected Iterable<ModelPart> bodyParts() {
		return Iterables.concat(super.bodyParts(), ImmutableList.of(this.eggBelly));
	}

	public void setupAnim(T turtle, float f, float g, float h, float i, float j) {
		super.setupAnim(turtle, f, g, h, i, j);
		this.leg0.xRot = Mth.cos(f * 0.6662F * 0.6F) * 0.5F * g;
		this.leg1.xRot = Mth.cos(f * 0.6662F * 0.6F + (float) Math.PI) * 0.5F * g;
		this.leg2.zRot = Mth.cos(f * 0.6662F * 0.6F + (float) Math.PI) * 0.5F * g;
		this.leg3.zRot = Mth.cos(f * 0.6662F * 0.6F) * 0.5F * g;
		this.leg2.xRot = 0.0F;
		this.leg3.xRot = 0.0F;
		this.leg2.yRot = 0.0F;
		this.leg3.yRot = 0.0F;
		this.leg0.yRot = 0.0F;
		this.leg1.yRot = 0.0F;
		this.eggBelly.xRot = (float) (Math.PI / 2);
		if (!turtle.isInWater() && turtle.onGround) {
			float k = turtle.isLayingEgg() ? 4.0F : 1.0F;
			float l = turtle.isLayingEgg() ? 2.0F : 1.0F;
			float m = 5.0F;
			this.leg2.yRot = Mth.cos(k * f * 5.0F + (float) Math.PI) * 8.0F * g * l;
			this.leg2.zRot = 0.0F;
			this.leg3.yRot = Mth.cos(k * f * 5.0F) * 8.0F * g * l;
			this.leg3.zRot = 0.0F;
			this.leg0.yRot = Mth.cos(f * 5.0F + (float) Math.PI) * 3.0F * g;
			this.leg0.xRot = 0.0F;
			this.leg1.yRot = Mth.cos(f * 5.0F) * 3.0F * g;
			this.leg1.xRot = 0.0F;
		}

		this.eggBelly.visible = !this.young && turtle.hasEgg();
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h) {
		boolean bl = this.eggBelly.visible;
		if (bl) {
			poseStack.pushPose();
			poseStack.translate(0.0, -0.08F, 0.0);
		}

		super.renderToBuffer(poseStack, vertexConsumer, i, j, f, g, h);
		if (bl) {
			poseStack.popPose();
		}
	}
}
