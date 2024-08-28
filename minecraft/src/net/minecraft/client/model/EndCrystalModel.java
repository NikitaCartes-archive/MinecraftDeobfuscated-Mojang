package net.minecraft.client.model;

import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.client.renderer.entity.state.EndCrystalRenderState;
import org.joml.Quaternionf;

@Environment(EnvType.CLIENT)
public class EndCrystalModel extends EntityModel<EndCrystalRenderState> {
	private static final String OUTER_GLASS = "outer_glass";
	private static final String INNER_GLASS = "inner_glass";
	private static final String BASE = "base";
	private static final float SIN_45 = (float)Math.sin(Math.PI / 4);
	public final ModelPart base;
	public final ModelPart outerGlass;
	public final ModelPart innerGlass;
	public final ModelPart cube;

	public EndCrystalModel(ModelPart modelPart) {
		super(modelPart);
		this.base = modelPart.getChild("base");
		this.outerGlass = modelPart.getChild("outer_glass");
		this.innerGlass = this.outerGlass.getChild("inner_glass");
		this.cube = this.innerGlass.getChild("cube");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		float f = 0.875F;
		CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F);
		PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("outer_glass", cubeListBuilder, PartPose.offset(0.0F, 24.0F, 0.0F));
		PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild("inner_glass", cubeListBuilder, PartPose.ZERO.withScale(0.875F));
		partDefinition3.addOrReplaceChild(
			"cube", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO.withScale(0.765625F)
		);
		partDefinition.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 16).addBox(-6.0F, 0.0F, -6.0F, 12.0F, 4.0F, 12.0F), PartPose.ZERO);
		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	public void setupAnim(EndCrystalRenderState endCrystalRenderState) {
		super.setupAnim(endCrystalRenderState);
		this.base.visible = endCrystalRenderState.showsBottom;
		float f = endCrystalRenderState.ageInTicks * 3.0F;
		float g = EndCrystalRenderer.getY(endCrystalRenderState.ageInTicks) * 16.0F;
		this.outerGlass.y += g / 2.0F;
		this.outerGlass.rotateBy(Axis.YP.rotationDegrees(f).rotateAxis((float) (Math.PI / 3), SIN_45, 0.0F, SIN_45));
		this.innerGlass.rotateBy(new Quaternionf().setAngleAxis((float) (Math.PI / 3), SIN_45, 0.0F, SIN_45).rotateY(f * (float) (Math.PI / 180.0)));
		this.cube.rotateBy(new Quaternionf().setAngleAxis((float) (Math.PI / 3), SIN_45, 0.0F, SIN_45).rotateY(f * (float) (Math.PI / 180.0)));
	}
}
