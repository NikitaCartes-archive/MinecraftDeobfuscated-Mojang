package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Parrot;

@Environment(EnvType.CLIENT)
public class ParrotModel extends HierarchicalModel<Parrot> {
	private static final String FEATHER = "feather";
	private final ModelPart root;
	private final ModelPart body;
	private final ModelPart tail;
	private final ModelPart leftWing;
	private final ModelPart rightWing;
	private final ModelPart head;
	private final ModelPart feather;
	private final ModelPart leftLeg;
	private final ModelPart rightLeg;

	public ParrotModel(ModelPart modelPart) {
		this.root = modelPart;
		this.body = modelPart.getChild("body");
		this.tail = modelPart.getChild("tail");
		this.leftWing = modelPart.getChild("left_wing");
		this.rightWing = modelPart.getChild("right_wing");
		this.head = modelPart.getChild("head");
		this.feather = this.head.getChild("feather");
		this.leftLeg = modelPart.getChild("left_leg");
		this.rightLeg = modelPart.getChild("right_leg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild(
			"body", CubeListBuilder.create().texOffs(2, 8).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F), PartPose.offset(0.0F, 16.5F, -3.0F)
		);
		partDefinition.addOrReplaceChild(
			"tail", CubeListBuilder.create().texOffs(22, 1).addBox(-1.5F, -1.0F, -1.0F, 3.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 21.07F, 1.16F)
		);
		partDefinition.addOrReplaceChild(
			"left_wing", CubeListBuilder.create().texOffs(19, 8).addBox(-0.5F, 0.0F, -1.5F, 1.0F, 5.0F, 3.0F), PartPose.offset(1.5F, 16.94F, -2.76F)
		);
		partDefinition.addOrReplaceChild(
			"right_wing", CubeListBuilder.create().texOffs(19, 8).addBox(-0.5F, 0.0F, -1.5F, 1.0F, 5.0F, 3.0F), PartPose.offset(-1.5F, 16.94F, -2.76F)
		);
		PartDefinition partDefinition2 = partDefinition.addOrReplaceChild(
			"head", CubeListBuilder.create().texOffs(2, 2).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offset(0.0F, 15.69F, -2.76F)
		);
		partDefinition2.addOrReplaceChild(
			"head2", CubeListBuilder.create().texOffs(10, 0).addBox(-1.0F, -0.5F, -2.0F, 2.0F, 1.0F, 4.0F), PartPose.offset(0.0F, -2.0F, -1.0F)
		);
		partDefinition2.addOrReplaceChild(
			"beak1", CubeListBuilder.create().texOffs(11, 7).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offset(0.0F, -0.5F, -1.5F)
		);
		partDefinition2.addOrReplaceChild(
			"beak2", CubeListBuilder.create().texOffs(16, 7).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offset(0.0F, -1.75F, -2.45F)
		);
		partDefinition2.addOrReplaceChild(
			"feather", CubeListBuilder.create().texOffs(2, 18).addBox(0.0F, -4.0F, -2.0F, 0.0F, 5.0F, 4.0F), PartPose.offset(0.0F, -2.15F, 0.15F)
		);
		CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(14, 18).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F);
		partDefinition.addOrReplaceChild("left_leg", cubeListBuilder, PartPose.offset(1.0F, 22.0F, -1.05F));
		partDefinition.addOrReplaceChild("right_leg", cubeListBuilder, PartPose.offset(-1.0F, 22.0F, -1.05F));
		return LayerDefinition.create(meshDefinition, 32, 32);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	public void setupAnim(Parrot parrot, float f, float g, float h, float i, float j) {
		this.setupAnim(getState(parrot), parrot.tickCount, f, g, h, i, j);
	}

	public void prepareMobModel(Parrot parrot, float f, float g, float h) {
		this.prepare(getState(parrot));
	}

	public void renderOnShoulder(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k, int l) {
		this.prepare(ParrotModel.State.ON_SHOULDER);
		this.setupAnim(ParrotModel.State.ON_SHOULDER, l, f, g, 0.0F, h, k);
		this.root.render(poseStack, vertexConsumer, i, j);
	}

	private void setupAnim(ParrotModel.State state, int i, float f, float g, float h, float j, float k) {
		this.head.xRot = k * (float) (Math.PI / 180.0);
		this.head.yRot = j * (float) (Math.PI / 180.0);
		this.head.zRot = 0.0F;
		this.head.x = 0.0F;
		this.body.x = 0.0F;
		this.tail.x = 0.0F;
		this.rightWing.x = -1.5F;
		this.leftWing.x = 1.5F;
		switch (state) {
			case SITTING:
				break;
			case PARTY:
				float l = Mth.cos((float)i);
				float m = Mth.sin((float)i);
				this.head.x = l;
				this.head.y = 15.69F + m;
				this.head.xRot = 0.0F;
				this.head.yRot = 0.0F;
				this.head.zRot = Mth.sin((float)i) * 0.4F;
				this.body.x = l;
				this.body.y = 16.5F + m;
				this.leftWing.zRot = -0.0873F - h;
				this.leftWing.x = 1.5F + l;
				this.leftWing.y = 16.94F + m;
				this.rightWing.zRot = 0.0873F + h;
				this.rightWing.x = -1.5F + l;
				this.rightWing.y = 16.94F + m;
				this.tail.x = l;
				this.tail.y = 21.07F + m;
				break;
			case STANDING:
				this.leftLeg.xRot = this.leftLeg.xRot + Mth.cos(f * 0.6662F) * 1.4F * g;
				this.rightLeg.xRot = this.rightLeg.xRot + Mth.cos(f * 0.6662F + (float) Math.PI) * 1.4F * g;
			case FLYING:
			case ON_SHOULDER:
			default:
				float n = h * 0.3F;
				this.head.y = 15.69F + n;
				this.tail.xRot = 1.015F + Mth.cos(f * 0.6662F) * 0.3F * g;
				this.tail.y = 21.07F + n;
				this.body.y = 16.5F + n;
				this.leftWing.zRot = -0.0873F - h;
				this.leftWing.y = 16.94F + n;
				this.rightWing.zRot = 0.0873F + h;
				this.rightWing.y = 16.94F + n;
				this.leftLeg.y = 22.0F + n;
				this.rightLeg.y = 22.0F + n;
		}
	}

	private void prepare(ParrotModel.State state) {
		this.feather.xRot = -0.2214F;
		this.body.xRot = 0.4937F;
		this.leftWing.xRot = -0.6981F;
		this.leftWing.yRot = (float) -Math.PI;
		this.rightWing.xRot = -0.6981F;
		this.rightWing.yRot = (float) -Math.PI;
		this.leftLeg.xRot = -0.0299F;
		this.rightLeg.xRot = -0.0299F;
		this.leftLeg.y = 22.0F;
		this.rightLeg.y = 22.0F;
		this.leftLeg.zRot = 0.0F;
		this.rightLeg.zRot = 0.0F;
		switch (state) {
			case SITTING:
				float f = 1.9F;
				this.head.y = 17.59F;
				this.tail.xRot = 1.5388988F;
				this.tail.y = 22.97F;
				this.body.y = 18.4F;
				this.leftWing.zRot = -0.0873F;
				this.leftWing.y = 18.84F;
				this.rightWing.zRot = 0.0873F;
				this.rightWing.y = 18.84F;
				this.leftLeg.y++;
				this.rightLeg.y++;
				this.leftLeg.xRot++;
				this.rightLeg.xRot++;
				break;
			case PARTY:
				this.leftLeg.zRot = (float) (-Math.PI / 9);
				this.rightLeg.zRot = (float) (Math.PI / 9);
			case STANDING:
			case ON_SHOULDER:
			default:
				break;
			case FLYING:
				this.leftLeg.xRot += (float) (Math.PI * 2.0 / 9.0);
				this.rightLeg.xRot += (float) (Math.PI * 2.0 / 9.0);
		}
	}

	private static ParrotModel.State getState(Parrot parrot) {
		if (parrot.isPartyParrot()) {
			return ParrotModel.State.PARTY;
		} else if (parrot.isInSittingPose()) {
			return ParrotModel.State.SITTING;
		} else {
			return parrot.isFlying() ? ParrotModel.State.FLYING : ParrotModel.State.STANDING;
		}
	}

	@Environment(EnvType.CLIENT)
	public static enum State {
		FLYING,
		STANDING,
		SITTING,
		PARTY,
		ON_SHOULDER;
	}
}
