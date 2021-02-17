package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public interface BlockEntityRenderer<T extends BlockEntity> {
	void render(T blockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j);

	default boolean shouldRenderOffScreen(T blockEntity) {
		return false;
	}

	default int getViewDistance() {
		return 64;
	}

	default boolean shouldRender(T blockEntity, Vec3 vec3) {
		return Vec3.atCenterOf(blockEntity.getBlockPos()).closerThan(vec3, (double)this.getViewDistance());
	}
}
