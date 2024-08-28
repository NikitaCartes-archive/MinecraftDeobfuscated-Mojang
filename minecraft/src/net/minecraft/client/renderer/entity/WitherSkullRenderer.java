package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.WitherSkullRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.WitherSkull;

@Environment(EnvType.CLIENT)
public class WitherSkullRenderer extends EntityRenderer<WitherSkull, WitherSkullRenderState> {
	private static final ResourceLocation WITHER_INVULNERABLE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/wither/wither_invulnerable.png");
	private static final ResourceLocation WITHER_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/wither/wither.png");
	private final SkullModel model;

	public WitherSkullRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.model = new SkullModel(context.bakeLayer(ModelLayers.WITHER_SKULL));
	}

	public static LayerDefinition createSkullLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 35).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
		return LayerDefinition.create(meshDefinition, 64, 64);
	}

	protected int getBlockLightLevel(WitherSkull witherSkull, BlockPos blockPos) {
		return 15;
	}

	public void render(WitherSkullRenderState witherSkullRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		poseStack.pushPose();
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(this.model.renderType(this.getTextureLocation(witherSkullRenderState)));
		this.model.setupAnim(0.0F, witherSkullRenderState.yRot, witherSkullRenderState.xRot);
		this.model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY);
		poseStack.popPose();
		super.render(witherSkullRenderState, poseStack, multiBufferSource, i);
	}

	private ResourceLocation getTextureLocation(WitherSkullRenderState witherSkullRenderState) {
		return witherSkullRenderState.isDangerous ? WITHER_INVULNERABLE_LOCATION : WITHER_LOCATION;
	}

	public WitherSkullRenderState createRenderState() {
		return new WitherSkullRenderState();
	}

	public void extractRenderState(WitherSkull witherSkull, WitherSkullRenderState witherSkullRenderState, float f) {
		super.extractRenderState(witherSkull, witherSkullRenderState, f);
		witherSkullRenderState.isDangerous = witherSkull.isDangerous();
		witherSkullRenderState.yRot = witherSkull.getYRot(f);
		witherSkullRenderState.xRot = witherSkull.getXRot(f);
	}
}
