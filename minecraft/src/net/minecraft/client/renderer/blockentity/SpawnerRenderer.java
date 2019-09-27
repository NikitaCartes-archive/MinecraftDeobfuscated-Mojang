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
public class SpawnerRenderer extends BlockEntityRenderer<SpawnerBlockEntity> {
	public SpawnerRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
		super(blockEntityRenderDispatcher);
	}

	public void render(
		SpawnerBlockEntity spawnerBlockEntity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i
	) {
		poseStack.pushPose();
		poseStack.translate(0.5, 0.0, 0.5);
		BaseSpawner baseSpawner = spawnerBlockEntity.getSpawner();
		Entity entity = baseSpawner.getOrCreateDisplayEntity();
		if (entity != null) {
			float h = 0.53125F;
			float j = Math.max(entity.getBbWidth(), entity.getBbHeight());
			if ((double)j > 1.0) {
				h /= j;
			}

			poseStack.translate(0.0, 0.4F, 0.0);
			poseStack.mulPose(Vector3f.YP.rotation((float)Mth.lerp((double)g, baseSpawner.getoSpin(), baseSpawner.getSpin()) * 10.0F, true));
			poseStack.translate(0.0, -0.2F, 0.0);
			poseStack.mulPose(Vector3f.XP.rotation(-30.0F, true));
			poseStack.scale(h, h, h);
			entity.moveTo(d, e, f, 0.0F, 0.0F);
			Minecraft.getInstance().getEntityRenderDispatcher().render(entity, 0.0, 0.0, 0.0, 0.0F, g, poseStack, multiBufferSource);
		}

		poseStack.popPose();
	}
}
