package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.animal.allay.Allay;

@Environment(EnvType.CLIENT)
public class AllayModel extends HierarchicalModel<Allay> implements ArmedModel {
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

	public void setupAnim(Allay allay, float f, float g, float h, float i, float j) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		float k = h * 20.0F * (float) (Math.PI / 180.0) + f;
		float l = Mth.cos(k) * (float) Math.PI * 0.15F + g;
		float m = h - (float)allay.tickCount;
		float n = h * 9.0F * (float) (Math.PI / 180.0);
		float o = Math.min(g / 0.3F, 1.0F);
		float p = 1.0F - o;
		float q = allay.getHoldingItemAnimationProgress(m);
		if (allay.isDancing()) {
			float r = h * 8.0F * (float) (Math.PI / 180.0) + g;
			float s = Mth.cos(r) * 16.0F * (float) (Math.PI / 180.0);
			float t = allay.getSpinningProgress(m);
			float u = Mth.cos(r) * 14.0F * (float) (Math.PI / 180.0);
			float v = Mth.cos(r) * 30.0F * (float) (Math.PI / 180.0);
			this.root.yRot = allay.isSpinning() ? (float) (Math.PI * 4) * t : this.root.yRot;
			this.root.zRot = s * (1.0F - t);
			this.head.yRot = v * (1.0F - t);
			this.head.zRot = u * (1.0F - t);
		} else {
			this.head.xRot = j * (float) (Math.PI / 180.0);
			this.head.yRot = i * (float) (Math.PI / 180.0);
		}

		this.right_wing.xRot = 0.43633232F * (1.0F - o);
		this.right_wing.yRot = (float) (-Math.PI / 4) + l;
		this.left_wing.xRot = 0.43633232F * (1.0F - o);
		this.left_wing.yRot = (float) (Math.PI / 4) - l;
		this.body.xRot = o * (float) (Math.PI / 4);
		float r = q * Mth.lerp(o, (float) (-Math.PI / 3), -1.134464F);
		this.root.y = this.root.y + (float)Math.cos((double)n) * 0.25F * p;
		this.right_arm.xRot = r;
		this.left_arm.xRot = r;
		float s = p * (1.0F - q);
		float t = 0.43633232F - Mth.cos(n + (float) (Math.PI * 3.0 / 2.0)) * (float) Math.PI * 0.075F * s;
		this.left_arm.zRot = -t;
		this.right_arm.zRot = t;
		this.right_arm.yRot = 0.27925268F * q;
		this.left_arm.yRot = -0.27925268F * q;
	}

	@Override
	public void translateToHand(HumanoidArm humanoidArm, PoseStack poseStack) {
		float f = 1.0F;
		float g = 3.0F;
		this.root.translateAndRotate(poseStack);
		this.body.translateAndRotate(poseStack);
		poseStack.translate(0.0, 0.0625, 0.1875);
		poseStack.mulPose(Vector3f.XP.rotation(this.right_arm.xRot));
		poseStack.scale(0.7F, 0.7F, 0.7F);
		poseStack.translate(0.0625, 0.0, 0.0);
	}
}
