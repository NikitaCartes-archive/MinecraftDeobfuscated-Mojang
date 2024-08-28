package net.minecraft.client.model;

import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class BlazeModel extends EntityModel<LivingEntityRenderState> {
	private final ModelPart[] upperBodyParts;
	private final ModelPart head;

	public BlazeModel(ModelPart modelPart) {
		super(modelPart);
		this.head = modelPart.getChild("head");
		this.upperBodyParts = new ModelPart[12];
		Arrays.setAll(this.upperBodyParts, i -> modelPart.getChild(getPartName(i)));
	}

	private static String getPartName(int i) {
		return "part" + i;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
		float f = 0.0F;
		CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(0, 16).addBox(0.0F, 0.0F, 0.0F, 2.0F, 8.0F, 2.0F);

		for (int i = 0; i < 4; i++) {
			float g = Mth.cos(f) * 9.0F;
			float h = -2.0F + Mth.cos((float)(i * 2) * 0.25F);
			float j = Mth.sin(f) * 9.0F;
			partDefinition.addOrReplaceChild(getPartName(i), cubeListBuilder, PartPose.offset(g, h, j));
			f++;
		}

		f = (float) (Math.PI / 4);

		for (int i = 4; i < 8; i++) {
			float g = Mth.cos(f) * 7.0F;
			float h = 2.0F + Mth.cos((float)(i * 2) * 0.25F);
			float j = Mth.sin(f) * 7.0F;
			partDefinition.addOrReplaceChild(getPartName(i), cubeListBuilder, PartPose.offset(g, h, j));
			f++;
		}

		f = 0.47123894F;

		for (int i = 8; i < 12; i++) {
			float g = Mth.cos(f) * 5.0F;
			float h = 11.0F + Mth.cos((float)i * 1.5F * 0.5F);
			float j = Mth.sin(f) * 5.0F;
			partDefinition.addOrReplaceChild(getPartName(i), cubeListBuilder, PartPose.offset(g, h, j));
			f++;
		}

		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	public void setupAnim(LivingEntityRenderState livingEntityRenderState) {
		super.setupAnim(livingEntityRenderState);
		float f = livingEntityRenderState.ageInTicks * (float) Math.PI * -0.1F;

		for (int i = 0; i < 4; i++) {
			this.upperBodyParts[i].y = -2.0F + Mth.cos(((float)(i * 2) + livingEntityRenderState.ageInTicks) * 0.25F);
			this.upperBodyParts[i].x = Mth.cos(f) * 9.0F;
			this.upperBodyParts[i].z = Mth.sin(f) * 9.0F;
			f++;
		}

		f = (float) (Math.PI / 4) + livingEntityRenderState.ageInTicks * (float) Math.PI * 0.03F;

		for (int i = 4; i < 8; i++) {
			this.upperBodyParts[i].y = 2.0F + Mth.cos(((float)(i * 2) + livingEntityRenderState.ageInTicks) * 0.25F);
			this.upperBodyParts[i].x = Mth.cos(f) * 7.0F;
			this.upperBodyParts[i].z = Mth.sin(f) * 7.0F;
			f++;
		}

		f = 0.47123894F + livingEntityRenderState.ageInTicks * (float) Math.PI * -0.05F;

		for (int i = 8; i < 12; i++) {
			this.upperBodyParts[i].y = 11.0F + Mth.cos(((float)i * 1.5F + livingEntityRenderState.ageInTicks) * 0.5F);
			this.upperBodyParts[i].x = Mth.cos(f) * 5.0F;
			this.upperBodyParts[i].z = Mth.sin(f) * 5.0F;
			f++;
		}

		this.head.yRot = livingEntityRenderState.yRot * (float) (Math.PI / 180.0);
		this.head.xRot = livingEntityRenderState.xRot * (float) (Math.PI / 180.0);
	}
}
