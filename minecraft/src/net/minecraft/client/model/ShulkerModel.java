package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Shulker;

@Environment(EnvType.CLIENT)
public class ShulkerModel<T extends Shulker> extends ListModel<T> {
	private static final String LID = "lid";
	private static final String BASE = "base";
	private final ModelPart base;
	private final ModelPart lid;
	private final ModelPart head;

	public ShulkerModel(ModelPart modelPart) {
		super(RenderType::entityCutoutNoCullZOffset);
		this.lid = modelPart.getChild("lid");
		this.base = modelPart.getChild("base");
		this.head = modelPart.getChild("head");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild(
			"lid", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -16.0F, -8.0F, 16.0F, 12.0F, 16.0F), PartPose.offset(0.0F, 24.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"base", CubeListBuilder.create().texOffs(0, 28).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 8.0F, 16.0F), PartPose.offset(0.0F, 24.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"head", CubeListBuilder.create().texOffs(0, 52).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.offset(0.0F, 12.0F, 0.0F)
		);
		return LayerDefinition.create(meshDefinition, 64, 64);
	}

	public void setupAnim(T shulker, float f, float g, float h, float i, float j) {
		float k = h - (float)shulker.tickCount;
		float l = (0.5F + shulker.getClientPeekAmount(k)) * (float) Math.PI;
		float m = -1.0F + Mth.sin(l);
		float n = 0.0F;
		if (l > (float) Math.PI) {
			n = Mth.sin(h * 0.1F) * 0.7F;
		}

		this.lid.setPos(0.0F, 16.0F + Mth.sin(l) * 8.0F + n, 0.0F);
		if (shulker.getClientPeekAmount(k) > 0.3F) {
			this.lid.yRot = m * m * m * m * (float) Math.PI * 0.125F;
		} else {
			this.lid.yRot = 0.0F;
		}

		this.head.xRot = j * (float) (Math.PI / 180.0);
		this.head.yRot = (shulker.yHeadRot - 180.0F - shulker.yBodyRot) * (float) (Math.PI / 180.0);
	}

	@Override
	public Iterable<ModelPart> parts() {
		return ImmutableList.<ModelPart>of(this.base, this.lid);
	}

	public ModelPart getLid() {
		return this.lid;
	}

	public ModelPart getHead() {
		return this.head;
	}
}
