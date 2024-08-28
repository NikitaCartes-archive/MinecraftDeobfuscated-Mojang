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
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class BookModel extends Model {
	private static final String LEFT_PAGES = "left_pages";
	private static final String RIGHT_PAGES = "right_pages";
	private static final String FLIP_PAGE_1 = "flip_page1";
	private static final String FLIP_PAGE_2 = "flip_page2";
	private final ModelPart leftLid;
	private final ModelPart rightLid;
	private final ModelPart leftPages;
	private final ModelPart rightPages;
	private final ModelPart flipPage1;
	private final ModelPart flipPage2;

	public BookModel(ModelPart modelPart) {
		super(modelPart, RenderType::entitySolid);
		this.leftLid = modelPart.getChild("left_lid");
		this.rightLid = modelPart.getChild("right_lid");
		this.leftPages = modelPart.getChild("left_pages");
		this.rightPages = modelPart.getChild("right_pages");
		this.flipPage1 = modelPart.getChild("flip_page1");
		this.flipPage2 = modelPart.getChild("flip_page2");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild(
			"left_lid", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -5.0F, -0.005F, 6.0F, 10.0F, 0.005F), PartPose.offset(0.0F, 0.0F, -1.0F)
		);
		partDefinition.addOrReplaceChild(
			"right_lid", CubeListBuilder.create().texOffs(16, 0).addBox(0.0F, -5.0F, -0.005F, 6.0F, 10.0F, 0.005F), PartPose.offset(0.0F, 0.0F, 1.0F)
		);
		partDefinition.addOrReplaceChild(
			"seam", CubeListBuilder.create().texOffs(12, 0).addBox(-1.0F, -5.0F, 0.0F, 2.0F, 10.0F, 0.005F), PartPose.rotation(0.0F, (float) (Math.PI / 2), 0.0F)
		);
		partDefinition.addOrReplaceChild("left_pages", CubeListBuilder.create().texOffs(0, 10).addBox(0.0F, -4.0F, -0.99F, 5.0F, 8.0F, 1.0F), PartPose.ZERO);
		partDefinition.addOrReplaceChild("right_pages", CubeListBuilder.create().texOffs(12, 10).addBox(0.0F, -4.0F, -0.01F, 5.0F, 8.0F, 1.0F), PartPose.ZERO);
		CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(24, 10).addBox(0.0F, -4.0F, 0.0F, 5.0F, 8.0F, 0.005F);
		partDefinition.addOrReplaceChild("flip_page1", cubeListBuilder, PartPose.ZERO);
		partDefinition.addOrReplaceChild("flip_page2", cubeListBuilder, PartPose.ZERO);
		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	public void setupAnim(float f, float g, float h, float i) {
		float j = (Mth.sin(f * 0.02F) * 0.1F + 1.25F) * i;
		this.leftLid.yRot = (float) Math.PI + j;
		this.rightLid.yRot = -j;
		this.leftPages.yRot = j;
		this.rightPages.yRot = -j;
		this.flipPage1.yRot = j - j * 2.0F * g;
		this.flipPage2.yRot = j - j * 2.0F * h;
		this.leftPages.x = Mth.sin(j);
		this.rightPages.x = Mth.sin(j);
		this.flipPage1.x = Mth.sin(j);
		this.flipPage2.x = Mth.sin(j);
	}
}
