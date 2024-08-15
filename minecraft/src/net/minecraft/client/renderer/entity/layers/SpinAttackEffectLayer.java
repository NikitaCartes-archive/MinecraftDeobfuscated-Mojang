package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class SpinAttackEffectLayer extends RenderLayer<PlayerRenderState, PlayerModel> {
	public static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/trident_riptide.png");
	private static final int BOX_COUNT = 2;
	private final Model model;
	private final ModelPart[] boxes = new ModelPart[2];

	public SpinAttackEffectLayer(RenderLayerParent<PlayerRenderState, PlayerModel> renderLayerParent, EntityModelSet entityModelSet) {
		super(renderLayerParent);
		ModelPart modelPart = entityModelSet.bakeLayer(ModelLayers.PLAYER_SPIN_ATTACK);
		this.model = new Model.Simple(modelPart, RenderType::entityCutoutNoCull);

		for (int i = 0; i < 2; i++) {
			this.boxes[i] = modelPart.getChild(boxName(i));
		}
	}

	private static String boxName(int i) {
		return "box" + i;
	}

	public static LayerDefinition createLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();

		for (int i = 0; i < 2; i++) {
			float f = -3.2F + 9.6F * (float)(i + 1);
			float g = 0.75F * (float)(i + 1);
			partDefinition.addOrReplaceChild(
				boxName(i), CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -16.0F + f, -8.0F, 16.0F, 32.0F, 16.0F), PartPose.ZERO.withScale(g)
			);
		}

		return LayerDefinition.create(meshDefinition, 64, 64);
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, PlayerRenderState playerRenderState, float f, float g) {
		if (playerRenderState.isAutoSpinAttack) {
			for (int j = 0; j < this.boxes.length; j++) {
				float h = playerRenderState.ageInTicks * (float)(-(45 + (j + 1) * 5));
				this.boxes[j].yRot = Mth.wrapDegrees(h) * (float) (Math.PI / 180.0);
			}

			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(this.model.renderType(TEXTURE));
			this.model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY);
		}
	}
}
