package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.AllayRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

@Environment(EnvType.CLIENT)
public class AllayModel extends EntityModel<AllayRenderState> implements ArmedModel {
	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart body;
	private final ModelPart right_arm;
	private final ModelPart left_arm;
	private final ModelPart right_wing;
	private final ModelPart left_wing;
	private static final float FLYING_ANIMATION_X_ROT = (float) (Math.PI / 4);
	private static final float MAX_HAND_HOLDING_ITEM_X_ROT_RAD = -1.134464F;
	private static final float MIN_HAND_HOLDING_ITEM_X_ROT_RAD = (float) (-Math.PI / 3);

	public AllayModel(ModelPart modelPart) {
		super(RenderType::entityTranslucent);
		this.root = modelPart.getChild("root");
		this.head = this.root.getChild("head");
		this.body = this.root.getChild("body");
		this.right_arm = this.body.getChild("right_arm");
		this.left_arm = this.body.getChild("left_arm");
		this.right_wing = this.body.getChild("right_wing");
		this.left_wing = this.body.getChild("left_wing");
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 23.5F, 0.0F));
		partDefinition2.addOrReplaceChild(
			"head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -5.0F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.99F, 0.0F)
		);
		PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild(
			"body",
			CubeListBuilder.create()
				.texOffs(0, 10)
				.addBox(-1.5F, 0.0F, -1.0F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 16)
				.addBox(-1.5F, 0.0F, -1.0F, 3.0F, 5.0F, 2.0F, new CubeDeformation(-0.2F)),
			PartPose.offset(0.0F, -4.0F, 0.0F)
		);
		partDefinition3.addOrReplaceChild(
			"right_arm",
			CubeListBuilder.create().texOffs(23, 0).addBox(-0.75F, -0.5F, -1.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(-0.01F)),
			PartPose.offset(-1.75F, 0.5F, 0.0F)
		);
		partDefinition3.addOrReplaceChild(
			"left_arm",
			CubeListBuilder.create().texOffs(23, 6).addBox(-0.25F, -0.5F, -1.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(-0.01F)),
			PartPose.offset(1.75F, 0.5F, 0.0F)
		);
		partDefinition3.addOrReplaceChild(
			"right_wing",
			CubeListBuilder.create().texOffs(16, 14).addBox(0.0F, 1.0F, 0.0F, 0.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)),
			PartPose.offset(-0.5F, 0.0F, 0.6F)
		);
		partDefinition3.addOrReplaceChild(
			"left_wing",
			CubeListBuilder.create().texOffs(16, 14).addBox(0.0F, 1.0F, 0.0F, 0.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)),
			PartPose.offset(0.5F, 0.0F, 0.6F)
		);
		return LayerDefinition.create(meshDefinition, 32, 32);
	}

	public void setupAnim(AllayRenderState allayRenderState) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		float f = allayRenderState.walkAnimationSpeed;
		float g = allayRenderState.walkAnimationPos;
		float h = allayRenderState.ageInTicks * 20.0F * (float) (Math.PI / 180.0) + g;
		float i = Mth.cos(h) * (float) Math.PI * 0.15F + f;
		float j = allayRenderState.ageInTicks * 9.0F * (float) (Math.PI / 180.0);
		float k = Math.min(f / 0.3F, 1.0F);
		float l = 1.0F - k;
		float m = allayRenderState.holdingAnimationProgress;
		if (allayRenderState.isDancing) {
			float n = allayRenderState.ageInTicks * 8.0F * (float) (Math.PI / 180.0) + f;
			float o = Mth.cos(n) * 16.0F * (float) (Math.PI / 180.0);
			float p = allayRenderState.spinningProgress;
			float q = Mth.cos(n) * 14.0F * (float) (Math.PI / 180.0);
			float r = Mth.cos(n) * 30.0F * (float) (Math.PI / 180.0);
			this.root.yRot = allayRenderState.isSpinning ? (float) (Math.PI * 4) * p : this.root.yRot;
			this.root.zRot = o * (1.0F - p);
			this.head.yRot = r * (1.0F - p);
			this.head.zRot = q * (1.0F - p);
		} else {
			this.head.xRot = allayRenderState.xRot * (float) (Math.PI / 180.0);
			this.head.yRot = allayRenderState.yRot * (float) (Math.PI / 180.0);
		}

		this.right_wing.xRot = 0.43633232F * (1.0F - k);
		this.right_wing.yRot = (float) (-Math.PI / 4) + i;
		this.left_wing.xRot = 0.43633232F * (1.0F - k);
		this.left_wing.yRot = (float) (Math.PI / 4) - i;
		this.body.xRot = k * (float) (Math.PI / 4);
		float n = m * Mth.lerp(k, (float) (-Math.PI / 3), -1.134464F);
		this.root.y = this.root.y + (float)Math.cos((double)j) * 0.25F * l;
		this.right_arm.xRot = n;
		this.left_arm.xRot = n;
		float o = l * (1.0F - m);
		float p = 0.43633232F - Mth.cos(j + (float) (Math.PI * 3.0 / 2.0)) * (float) Math.PI * 0.075F * o;
		this.left_arm.zRot = -p;
		this.right_arm.zRot = p;
		this.right_arm.yRot = 0.27925268F * m;
		this.left_arm.yRot = -0.27925268F * m;
	}

	@Override
	public void translateToHand(HumanoidArm humanoidArm, PoseStack poseStack) {
		float f = 1.0F;
		float g = 3.0F;
		this.root.translateAndRotate(poseStack);
		this.body.translateAndRotate(poseStack);
		poseStack.translate(0.0F, 0.0625F, 0.1875F);
		poseStack.mulPose(Axis.XP.rotation(this.right_arm.xRot));
		poseStack.scale(0.7F, 0.7F, 0.7F);
		poseStack.translate(0.0625F, 0.0F, 0.0F);
	}
}
