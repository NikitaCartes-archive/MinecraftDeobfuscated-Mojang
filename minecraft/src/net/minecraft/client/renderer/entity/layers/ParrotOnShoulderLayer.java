package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ParrotModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ParrotRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.player.Player;

@Environment(EnvType.CLIENT)
public class ParrotOnShoulderLayer<T extends Player> extends RenderLayer<T, PlayerModel<T>> {
	private final ParrotModel model;

	public ParrotOnShoulderLayer(RenderLayerParent<T, PlayerModel<T>> renderLayerParent, EntityModelSet entityModelSet) {
		super(renderLayerParent);
		this.model = new ParrotModel(entityModelSet.bakeLayer(ModelLayers.PARROT));
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T player, float f, float g, float h, float j, float k, float l) {
		this.render(poseStack, multiBufferSource, i, player, f, g, k, l, true);
		this.render(poseStack, multiBufferSource, i, player, f, g, k, l, false);
	}

	private void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T player, float f, float g, float h, float j, boolean bl) {
		CompoundTag compoundTag = bl ? player.getShoulderEntityLeft() : player.getShoulderEntityRight();
		EntityType.byString(compoundTag.getString("id")).filter(entityType -> entityType == EntityType.PARROT).ifPresent(entityType -> {
			poseStack.pushPose();
			poseStack.translate(bl ? 0.4F : -0.4F, player.isCrouching() ? -1.3F : -1.5F, 0.0F);
			Parrot.Variant variant = Parrot.Variant.byId(compoundTag.getInt("Variant"));
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(this.model.renderType(ParrotRenderer.getVariantTexture(variant)));
			this.model.renderOnShoulder(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, f, g, h, j, player.tickCount);
			poseStack.popPose();
		});
	}
}
