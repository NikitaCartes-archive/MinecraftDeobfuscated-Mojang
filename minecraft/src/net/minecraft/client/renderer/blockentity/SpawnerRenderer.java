package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;

@Environment(EnvType.CLIENT)
public class SpawnerRenderer implements BlockEntityRenderer<SpawnerBlockEntity> {
	public SpawnerRenderer(BlockEntityRendererProvider.Context context) {
	}

	public void render(SpawnerBlockEntity spawnerBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
		poseStack.pushPose();
		poseStack.translate(0.5, 0.0, 0.5);
		BaseSpawner baseSpawner = spawnerBlockEntity.getSpawner();
		Entity entity = baseSpawner.getOrCreateDisplayEntity(spawnerBlockEntity.getLevel());
		if (entity != null) {
			float g = 0.53125F;
			float h = Math.max(entity.getBbWidth(), entity.getBbHeight());
			if ((double)h > 1.0) {
				g /= h;
			}

			poseStack.translate(0.0, 0.4F, 0.0);
			poseStack.mulPose(Vector3f.YP.rotationDegrees((float)Mth.lerp((double)f, baseSpawner.getoSpin(), baseSpawner.getSpin()) * 10.0F));
			poseStack.translate(0.0, -0.2F, 0.0);
			poseStack.mulPose(Vector3f.XP.rotationDegrees(-30.0F));
			poseStack.scale(g, g, g);
			Minecraft.getInstance().getEntityRenderDispatcher().render(entity, 0.0, 0.0, 0.0, 0.0F, f, poseStack, multiBufferSource, i);
		}

		poseStack.popPose();
	}
}
