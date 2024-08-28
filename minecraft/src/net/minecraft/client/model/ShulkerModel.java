package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.ShulkerRenderState;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class ShulkerModel extends EntityModel<ShulkerRenderState> {
	public static final String LID = "lid";
	private static final String BASE = "base";
	private final ModelPart lid;
	private final ModelPart head;

	public ShulkerModel(ModelPart modelPart) {
		super(modelPart, RenderType::entityCutoutNoCullZOffset);
		this.lid = modelPart.getChild("lid");
		this.head = modelPart.getChild("head");
	}

	private static MeshDefinition createShellMesh() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild(
			"lid", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -16.0F, -8.0F, 16.0F, 12.0F, 16.0F), PartPose.offset(0.0F, 24.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"base", CubeListBuilder.create().texOffs(0, 28).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 8.0F, 16.0F), PartPose.offset(0.0F, 24.0F, 0.0F)
		);
		return meshDefinition;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = createShellMesh();
		meshDefinition.getRoot()
			.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 52).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.offset(0.0F, 12.0F, 0.0F));
		return LayerDefinition.create(meshDefinition, 64, 64);
	}

	public static LayerDefinition createBoxLayer() {
		MeshDefinition meshDefinition = createShellMesh();
		return LayerDefinition.create(meshDefinition, 64, 64);
	}

	public void setupAnim(ShulkerRenderState shulkerRenderState) {
		super.setupAnim(shulkerRenderState);
		float f = (0.5F + shulkerRenderState.peekAmount) * (float) Math.PI;
		float g = -1.0F + Mth.sin(f);
		float h = 0.0F;
		if (f > (float) Math.PI) {
			h = Mth.sin(shulkerRenderState.ageInTicks * 0.1F) * 0.7F;
		}

		this.lid.setPos(0.0F, 16.0F + Mth.sin(f) * 8.0F + h, 0.0F);
		if (shulkerRenderState.peekAmount > 0.3F) {
			this.lid.yRot = g * g * g * g * (float) Math.PI * 0.125F;
		} else {
			this.lid.yRot = 0.0F;
		}

		this.head.xRot = shulkerRenderState.xRot * (float) (Math.PI / 180.0);
		this.head.yRot = (shulkerRenderState.yHeadRot - 180.0F - shulkerRenderState.yBodyRot) * (float) (Math.PI / 180.0);
	}
}
