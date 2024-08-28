package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.animation.definitions.BreezeAnimation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.BreezeRenderState;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class BreezeModel extends EntityModel<BreezeRenderState> {
	private static final float WIND_TOP_SPEED = 0.6F;
	private static final float WIND_MIDDLE_SPEED = 0.8F;
	private static final float WIND_BOTTOM_SPEED = 1.0F;
	private final ModelPart head;
	private final ModelPart eyes;
	private final ModelPart wind;
	private final ModelPart windTop;
	private final ModelPart windMid;
	private final ModelPart windBottom;
	private final ModelPart rods;

	public BreezeModel(ModelPart modelPart) {
		super(modelPart, RenderType::entityTranslucent);
		this.wind = modelPart.getChild("wind_body");
		this.windBottom = this.wind.getChild("wind_bottom");
		this.windMid = this.windBottom.getChild("wind_mid");
		this.windTop = this.windMid.getChild("wind_top");
		this.head = modelPart.getChild("body").getChild("head");
		this.eyes = this.head.getChild("eyes");
		this.rods = modelPart.getChild("body").getChild("rods");
	}

	public static LayerDefinition createBodyLayer(int i, int j) {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild("rods", CubeListBuilder.create(), PartPose.offset(0.0F, 8.0F, 0.0F));
		partDefinition3.addOrReplaceChild(
			"rod_1",
			CubeListBuilder.create().texOffs(0, 17).addBox(-1.0F, 0.0F, -3.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(2.5981F, -3.0F, 1.5F, -2.7489F, -1.0472F, 3.1416F)
		);
		partDefinition3.addOrReplaceChild(
			"rod_2",
			CubeListBuilder.create().texOffs(0, 17).addBox(-1.0F, 0.0F, -3.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(-2.5981F, -3.0F, 1.5F, -2.7489F, 1.0472F, 3.1416F)
		);
		partDefinition3.addOrReplaceChild(
			"rod_3",
			CubeListBuilder.create().texOffs(0, 17).addBox(-1.0F, 0.0F, -3.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(0.0F, -3.0F, -3.0F, 0.3927F, 0.0F, 0.0F)
		);
		PartDefinition partDefinition4 = partDefinition2.addOrReplaceChild(
			"head",
			CubeListBuilder.create()
				.texOffs(4, 24)
				.addBox(-5.0F, -5.0F, -4.2F, 10.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0)
				.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
			PartPose.offset(0.0F, 4.0F, 0.0F)
		);
		partDefinition4.addOrReplaceChild(
			"eyes",
			CubeListBuilder.create()
				.texOffs(4, 24)
				.addBox(-5.0F, -5.0F, -4.2F, 10.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0)
				.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
			PartPose.offset(0.0F, 0.0F, 0.0F)
		);
		PartDefinition partDefinition5 = partDefinition.addOrReplaceChild("wind_body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		PartDefinition partDefinition6 = partDefinition5.addOrReplaceChild(
			"wind_bottom",
			CubeListBuilder.create().texOffs(1, 83).addBox(-2.5F, -7.0F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)),
			PartPose.offset(0.0F, 24.0F, 0.0F)
		);
		PartDefinition partDefinition7 = partDefinition6.addOrReplaceChild(
			"wind_mid",
			CubeListBuilder.create()
				.texOffs(74, 28)
				.addBox(-6.0F, -6.0F, -6.0F, 12.0F, 6.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(78, 32)
				.addBox(-4.0F, -6.0F, -4.0F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(49, 71)
				.addBox(-2.5F, -6.0F, -2.5F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)),
			PartPose.offset(0.0F, -7.0F, 0.0F)
		);
		partDefinition7.addOrReplaceChild(
			"wind_top",
			CubeListBuilder.create()
				.texOffs(0, 0)
				.addBox(-9.0F, -8.0F, -9.0F, 18.0F, 8.0F, 18.0F, new CubeDeformation(0.0F))
				.texOffs(6, 6)
				.addBox(-6.0F, -8.0F, -6.0F, 12.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(105, 57)
				.addBox(-2.5F, -8.0F, -2.5F, 5.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)),
			PartPose.offset(0.0F, -6.0F, 0.0F)
		);
		return LayerDefinition.create(meshDefinition, i, j);
	}

	public void setupAnim(BreezeRenderState breezeRenderState) {
		super.setupAnim(breezeRenderState);
		float f = breezeRenderState.ageInTicks * (float) Math.PI * -0.1F;
		this.windTop.x = Mth.cos(f) * 1.0F * 0.6F;
		this.windTop.z = Mth.sin(f) * 1.0F * 0.6F;
		this.windMid.x = Mth.sin(f) * 0.5F * 0.8F;
		this.windMid.z = Mth.cos(f) * 0.8F;
		this.windBottom.x = Mth.cos(f) * -0.25F * 1.0F;
		this.windBottom.z = Mth.sin(f) * -0.25F * 1.0F;
		this.head.y = 4.0F + Mth.cos(f) / 4.0F;
		this.rods.yRot = breezeRenderState.ageInTicks * (float) Math.PI * 0.1F;
		this.animate(breezeRenderState.shoot, BreezeAnimation.SHOOT, breezeRenderState.ageInTicks);
		this.animate(breezeRenderState.slide, BreezeAnimation.SLIDE, breezeRenderState.ageInTicks);
		this.animate(breezeRenderState.slideBack, BreezeAnimation.SLIDE_BACK, breezeRenderState.ageInTicks);
		this.animate(breezeRenderState.inhale, BreezeAnimation.INHALE, breezeRenderState.ageInTicks);
		this.animate(breezeRenderState.longJump, BreezeAnimation.JUMP, breezeRenderState.ageInTicks);
	}

	public ModelPart head() {
		return this.head;
	}

	public ModelPart eyes() {
		return this.eyes;
	}

	public ModelPart rods() {
		return this.rods;
	}

	public ModelPart wind() {
		return this.wind;
	}
}
