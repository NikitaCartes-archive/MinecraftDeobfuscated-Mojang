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
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BellBlockEntity;

@Environment(EnvType.CLIENT)
public class BellModel extends Model {
	private static final String BELL_BODY = "bell_body";
	private final ModelPart root;
	private final ModelPart bellBody;

	public BellModel(ModelPart modelPart) {
		super(RenderType::entitySolid);
		this.root = modelPart;
		this.bellBody = modelPart.getChild("bell_body");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		PartDefinition partDefinition2 = partDefinition.addOrReplaceChild(
			"bell_body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -6.0F, -3.0F, 6.0F, 7.0F, 6.0F), PartPose.offset(8.0F, 12.0F, 8.0F)
		);
		partDefinition2.addOrReplaceChild(
			"bell_base", CubeListBuilder.create().texOffs(0, 13).addBox(4.0F, 4.0F, 4.0F, 8.0F, 2.0F, 8.0F), PartPose.offset(-8.0F, -12.0F, -8.0F)
		);
		return LayerDefinition.create(meshDefinition, 32, 32);
	}

	public void setupAnim(BellBlockEntity bellBlockEntity, float f) {
		float g = (float)bellBlockEntity.ticks + f;
		float h = 0.0F;
		float i = 0.0F;
		if (bellBlockEntity.shaking) {
			float j = Mth.sin(g / (float) Math.PI) / (4.0F + g / 3.0F);
			if (bellBlockEntity.clickDirection == Direction.NORTH) {
				h = -j;
			} else if (bellBlockEntity.clickDirection == Direction.SOUTH) {
				h = j;
			} else if (bellBlockEntity.clickDirection == Direction.EAST) {
				i = -j;
			} else if (bellBlockEntity.clickDirection == Direction.WEST) {
				i = j;
			}
		}

		this.bellBody.xRot = h;
		this.bellBody.zRot = i;
	}

	@Override
	public ModelPart root() {
		return this.root;
	}
}
