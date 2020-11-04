package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.entity.BlockEntity;

@Environment(EnvType.CLIENT)
public interface BlockEntityRenderer<T extends BlockEntity> {
	void render(T blockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j);

	default boolean shouldRenderOffScreen(T blockEntity) {
		return false;
	}
}
