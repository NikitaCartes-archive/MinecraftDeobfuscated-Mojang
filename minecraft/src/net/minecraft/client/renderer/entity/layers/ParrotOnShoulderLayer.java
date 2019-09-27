package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ParrotModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ParrotRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

@Environment(EnvType.CLIENT)
public class ParrotOnShoulderLayer<T extends Player> extends RenderLayer<T, PlayerModel<T>> {
	private final ParrotModel model = new ParrotModel();

	public ParrotOnShoulderLayer(RenderLayerParent<T, PlayerModel<T>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T player, float f, float g, float h, float j, float k, float l, float m) {
		this.render(poseStack, multiBufferSource, i, player, f, g, h, k, l, m, true);
		this.render(poseStack, multiBufferSource, i, player, f, g, h, k, l, m, false);
	}

	private void render(
		PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T player, float f, float g, float h, float j, float k, float l, boolean bl
	) {
		CompoundTag compoundTag = bl ? player.getShoulderEntityLeft() : player.getShoulderEntityRight();
		EntityType.byString(compoundTag.getString("id")).filter(entityType -> entityType == EntityType.PARROT).ifPresent(entityType -> {
			poseStack.pushPose();
			poseStack.translate(bl ? 0.4F : -0.4F, player.isCrouching() ? -1.3F : -1.5, 0.0);
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.NEW_ENTITY(ParrotRenderer.PARROT_LOCATIONS[compoundTag.getInt("Variant")]));
			OverlayTexture.setDefault(vertexConsumer);
			this.model.renderOnShoulder(poseStack, vertexConsumer, i, f, g, j, k, l, player.tickCount);
			vertexConsumer.unsetDefaultOverlayCoords();
			poseStack.popPose();
		});
	}
}
