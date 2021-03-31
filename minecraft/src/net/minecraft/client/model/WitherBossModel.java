package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.wither.WitherBoss;

@Environment(EnvType.CLIENT)
public class WitherBossModel<T extends WitherBoss> extends HierarchicalModel<T> {
	private static final String RIBCAGE = "ribcage";
	private static final String CENTER_HEAD = "center_head";
	private static final String RIGHT_HEAD = "right_head";
	private static final String LEFT_HEAD = "left_head";
	private static final float RIBCAGE_X_ROT_OFFSET = 0.065F;
	private static final float TAIL_X_ROT_OFFSET = 0.265F;
	private final ModelPart root;
	private final ModelPart centerHead;
	private final ModelPart rightHead;
	private final ModelPart leftHead;
	private final ModelPart ribcage;
	private final ModelPart tail;

	public WitherBossModel(ModelPart modelPart) {
		this.root = modelPart;
		this.ribcage = modelPart.getChild("ribcage");
		this.tail = modelPart.getChild("tail");
		this.centerHead = modelPart.getChild("center_head");
		this.rightHead = modelPart.getChild("right_head");
		this.leftHead = modelPart.getChild("left_head");
	}

	public static LayerDefinition createBodyLayer(CubeDeformation cubeDeformation) {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild(
			"shoulders", CubeListBuilder.create().texOffs(0, 16).addBox(-10.0F, 3.9F, -0.5F, 20.0F, 3.0F, 3.0F, cubeDeformation), PartPose.ZERO
		);
		float f = 0.20420352F;
		partDefinition.addOrReplaceChild(
			"ribcage",
			CubeListBuilder.create()
				.texOffs(0, 22)
				.addBox(0.0F, 0.0F, 0.0F, 3.0F, 10.0F, 3.0F, cubeDeformation)
				.texOffs(24, 22)
				.addBox(-4.0F, 1.5F, 0.5F, 11.0F, 2.0F, 2.0F, cubeDeformation)
				.texOffs(24, 22)
				.addBox(-4.0F, 4.0F, 0.5F, 11.0F, 2.0F, 2.0F, cubeDeformation)
				.texOffs(24, 22)
				.addBox(-4.0F, 6.5F, 0.5F, 11.0F, 2.0F, 2.0F, cubeDeformation),
			PartPose.offsetAndRotation(-2.0F, 6.9F, -0.5F, 0.20420352F, 0.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"tail",
			CubeListBuilder.create().texOffs(12, 22).addBox(0.0F, 0.0F, 0.0F, 3.0F, 6.0F, 3.0F, cubeDeformation),
			PartPose.offsetAndRotation(-2.0F, 6.9F + Mth.cos(0.20420352F) * 10.0F, -0.5F + Mth.sin(0.20420352F) * 10.0F, 0.83252203F, 0.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"center_head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, cubeDeformation), PartPose.ZERO
		);
		CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -4.0F, -4.0F, 6.0F, 6.0F, 6.0F, cubeDeformation);
		partDefinition.addOrReplaceChild("right_head", cubeListBuilder, PartPose.offset(-8.0F, 4.0F, 0.0F));
		partDefinition.addOrReplaceChild("left_head", cubeListBuilder, PartPose.offset(10.0F, 4.0F, 0.0F));
		return LayerDefinition.create(meshDefinition, 64, 64);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	public void setupAnim(T witherBoss, float f, float g, float h, float i, float j) {
		float k = Mth.cos(h * 0.1F);
		this.ribcage.xRot = (0.065F + 0.05F * k) * (float) Math.PI;
		this.tail.setPos(-2.0F, 6.9F + Mth.cos(this.ribcage.xRot) * 10.0F, -0.5F + Mth.sin(this.ribcage.xRot) * 10.0F);
		this.tail.xRot = (0.265F + 0.1F * k) * (float) Math.PI;
		this.centerHead.yRot = i * (float) (Math.PI / 180.0);
		this.centerHead.xRot = j * (float) (Math.PI / 180.0);
	}

	public void prepareMobModel(T witherBoss, float f, float g, float h) {
		setupHeadRotation(witherBoss, this.rightHead, 0);
		setupHeadRotation(witherBoss, this.leftHead, 1);
	}

	private static <T extends WitherBoss> void setupHeadRotation(T witherBoss, ModelPart modelPart, int i) {
		modelPart.yRot = (witherBoss.getHeadYRot(i) - witherBoss.yBodyRot) * (float) (Math.PI / 180.0);
		modelPart.xRot = witherBoss.getHeadXRot(i) * (float) (Math.PI / 180.0);
	}
}
