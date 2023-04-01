package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BeretModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.voting.rules.Rules;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;

@Environment(EnvType.CLIENT)
public class BeretLayer<T extends LivingEntity, M extends EntityModel<T> & HeadedModel> extends RenderLayer<T, M> {
	private static final ResourceLocation BERET_LOCATION = new ResourceLocation("textures/models/armor/beret.png");
	private final BeretModel<T> model;

	public BeretLayer(RenderLayerParent<T, M> renderLayerParent, EntityModelSet entityModelSet) {
		super(renderLayerParent);
		this.model = new BeretModel<>(entityModelSet.bakeLayer(ModelLayers.BERET));
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l) {
		if (Rules.FRENCH_MODE.get()) {
			if (livingEntity instanceof Villager villager) {
				VillagerData villagerData = villager.getVillagerData();
				if (villagerData.getProfession().equals(VillagerProfession.FARMER) || villagerData.getProfession().equals(VillagerProfession.LIBRARIAN)) {
					return;
				}

				if (villagerData.getType().equals(VillagerType.SNOW)) {
					return;
				}
			}

			poseStack.pushPose();
			if (livingEntity.isBaby() && !(livingEntity instanceof Villager)) {
				float m = 2.0F;
				float n = 1.4F;
				poseStack.translate(0.0F, 0.03125F, 0.0F);
				poseStack.scale(0.7F, 0.7F, 0.7F);
				poseStack.translate(0.0F, 1.0F, 0.0F);
			}

			this.getParentModel().getHead().translateAndRotate(poseStack);
			if (livingEntity instanceof Villager) {
				poseStack.translate(0.0F, -0.125F, 0.0F);
			}

			this.model.setupAnim(livingEntity, f, g, j, k, l);
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(this.model.renderType(BERET_LOCATION));
			this.model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
			poseStack.popPose();
		}
	}
}
