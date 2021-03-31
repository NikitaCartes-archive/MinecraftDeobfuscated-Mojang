package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class OcelotModel<T extends Entity> extends AgeableListModel<T> {
	private static final int CROUCH_STATE = 0;
	private static final int WALK_STATE = 1;
	private static final int SPRINT_STATE = 2;
	protected static final int SITTING_STATE = 3;
	private static final float XO = 0.0F;
	private static final float YO = 16.0F;
	private static final float ZO = -9.0F;
	private static final float HEAD_WALK_Y = 15.0F;
	private static final float HEAD_WALK_Z = -9.0F;
	private static final float BODY_WALK_Y = 12.0F;
	private static final float BODY_WALK_Z = -10.0F;
	private static final float TAIL_1_WALK_Y = 15.0F;
	private static final float TAIL_1_WALK_Z = 8.0F;
	private static final float TAIL_2_WALK_Y = 20.0F;
	private static final float TAIL_2_WALK_Z = 14.0F;
	protected static final float BACK_LEG_Y = 18.0F;
	protected static final float BACK_LEG_Z = 5.0F;
	protected static final float FRONT_LEG_Y = 14.1F;
	private static final float FRONT_LEG_Z = -5.0F;
	private static final String TAIL_1 = "tail1";
	private static final String TAIL_2 = "tail2";
	protected final ModelPart leftHindLeg;
	protected final ModelPart rightHindLeg;
	protected final ModelPart leftFrontLeg;
	protected final ModelPart rightFrontLeg;
	protected final ModelPart tail1;
	protected final ModelPart tail2;
	protected final ModelPart head;
	protected final ModelPart body;
	protected int state = 1;

	public OcelotModel(ModelPart modelPart) {
		super(true, 10.0F, 4.0F);
		this.head = modelPart.getChild("head");
		this.body = modelPart.getChild("body");
		this.tail1 = modelPart.getChild("tail1");
		this.tail2 = modelPart.getChild("tail2");
		this.leftHindLeg = modelPart.getChild("left_hind_leg");
		this.rightHindLeg = modelPart.getChild("right_hind_leg");
		this.leftFrontLeg = modelPart.getChild("left_front_leg");
		this.rightFrontLeg = modelPart.getChild("right_front_leg");
	}

	public static MeshDefinition createBodyMesh(CubeDeformation cubeDeformation) {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild(
			"head",
			CubeListBuilder.create()
				.addBox("main", -2.5F, -2.0F, -3.0F, 5.0F, 4.0F, 5.0F, cubeDeformation)
				.addBox("nose", -1.5F, 0.0F, -4.0F, 3, 2, 2, cubeDeformation, 0, 24)
				.addBox("ear1", -2.0F, -3.0F, 0.0F, 1, 1, 2, cubeDeformation, 0, 10)
				.addBox("ear2", 1.0F, -3.0F, 0.0F, 1, 1, 2, cubeDeformation, 6, 10),
			PartPose.offset(0.0F, 15.0F, -9.0F)
		);
		partDefinition.addOrReplaceChild(
			"body",
			CubeListBuilder.create().texOffs(20, 0).addBox(-2.0F, 3.0F, -8.0F, 4.0F, 16.0F, 6.0F, cubeDeformation),
			PartPose.offsetAndRotation(0.0F, 12.0F, -10.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"tail1",
			CubeListBuilder.create().texOffs(0, 15).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 8.0F, 1.0F, cubeDeformation),
			PartPose.offsetAndRotation(0.0F, 15.0F, 8.0F, 0.9F, 0.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"tail2", CubeListBuilder.create().texOffs(4, 15).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 8.0F, 1.0F, cubeDeformation), PartPose.offset(0.0F, 20.0F, 14.0F)
		);
		CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(8, 13).addBox(-1.0F, 0.0F, 1.0F, 2.0F, 6.0F, 2.0F, cubeDeformation);
		partDefinition.addOrReplaceChild("left_hind_leg", cubeListBuilder, PartPose.offset(1.1F, 18.0F, 5.0F));
		partDefinition.addOrReplaceChild("right_hind_leg", cubeListBuilder, PartPose.offset(-1.1F, 18.0F, 5.0F));
		CubeListBuilder cubeListBuilder2 = CubeListBuilder.create().texOffs(40, 0).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 10.0F, 2.0F, cubeDeformation);
		partDefinition.addOrReplaceChild("left_front_leg", cubeListBuilder2, PartPose.offset(1.2F, 14.1F, -5.0F));
		partDefinition.addOrReplaceChild("right_front_leg", cubeListBuilder2, PartPose.offset(-1.2F, 14.1F, -5.0F));
		return meshDefinition;
	}

	@Override
	protected Iterable<ModelPart> headParts() {
		return ImmutableList.<ModelPart>of(this.head);
	}

	@Override
	protected Iterable<ModelPart> bodyParts() {
		return ImmutableList.<ModelPart>of(this.body, this.leftHindLeg, this.rightHindLeg, this.leftFrontLeg, this.rightFrontLeg, this.tail1, this.tail2);
	}

	@Override
	public void setupAnim(T entity, float f, float g, float h, float i, float j) {
		this.head.xRot = j * (float) (Math.PI / 180.0);
		this.head.yRot = i * (float) (Math.PI / 180.0);
		if (this.state != 3) {
			this.body.xRot = (float) (Math.PI / 2);
			if (this.state == 2) {
				this.leftHindLeg.xRot = Mth.cos(f * 0.6662F) * g;
				this.rightHindLeg.xRot = Mth.cos(f * 0.6662F + 0.3F) * g;
				this.leftFrontLeg.xRot = Mth.cos(f * 0.6662F + (float) Math.PI + 0.3F) * g;
				this.rightFrontLeg.xRot = Mth.cos(f * 0.6662F + (float) Math.PI) * g;
				this.tail2.xRot = 1.7278761F + (float) (Math.PI / 10) * Mth.cos(f) * g;
			} else {
				this.leftHindLeg.xRot = Mth.cos(f * 0.6662F) * g;
				this.rightHindLeg.xRot = Mth.cos(f * 0.6662F + (float) Math.PI) * g;
				this.leftFrontLeg.xRot = Mth.cos(f * 0.6662F + (float) Math.PI) * g;
				this.rightFrontLeg.xRot = Mth.cos(f * 0.6662F) * g;
				if (this.state == 1) {
					this.tail2.xRot = 1.7278761F + (float) (Math.PI / 4) * Mth.cos(f) * g;
				} else {
					this.tail2.xRot = 1.7278761F + 0.47123894F * Mth.cos(f) * g;
				}
			}
		}
	}

	@Override
	public void prepareMobModel(T entity, float f, float g, float h) {
		this.body.y = 12.0F;
		this.body.z = -10.0F;
		this.head.y = 15.0F;
		this.head.z = -9.0F;
		this.tail1.y = 15.0F;
		this.tail1.z = 8.0F;
		this.tail2.y = 20.0F;
		this.tail2.z = 14.0F;
		this.leftFrontLeg.y = 14.1F;
		this.leftFrontLeg.z = -5.0F;
		this.rightFrontLeg.y = 14.1F;
		this.rightFrontLeg.z = -5.0F;
		this.leftHindLeg.y = 18.0F;
		this.leftHindLeg.z = 5.0F;
		this.rightHindLeg.y = 18.0F;
		this.rightHindLeg.z = 5.0F;
		this.tail1.xRot = 0.9F;
		if (entity.isCrouching()) {
			this.body.y++;
			this.head.y += 2.0F;
			this.tail1.y++;
			this.tail2.y += -4.0F;
			this.tail2.z += 2.0F;
			this.tail1.xRot = (float) (Math.PI / 2);
			this.tail2.xRot = (float) (Math.PI / 2);
			this.state = 0;
		} else if (entity.isSprinting()) {
			this.tail2.y = this.tail1.y;
			this.tail2.z += 2.0F;
			this.tail1.xRot = (float) (Math.PI / 2);
			this.tail2.xRot = (float) (Math.PI / 2);
			this.state = 2;
		} else {
			this.state = 1;
		}
	}
}
