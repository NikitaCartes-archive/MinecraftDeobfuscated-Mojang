package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;

@Environment(EnvType.CLIENT)
public class SpawnerRenderer implements BlockEntityRenderer<SpawnerBlockEntity> {
	private final EntityRenderDispatcher entityRenderer;

	public SpawnerRenderer(BlockEntityRendererProvider.Context context) {
		this.entityRenderer = context.getEntityRenderer();
	}

	public void render(SpawnerBlockEntity spawnerBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
		Level level = spawnerBlockEntity.getLevel();
		if (level != null) {
			BaseSpawner baseSpawner = spawnerBlockEntity.getSpawner();
			Entity entity = baseSpawner.getOrCreateDisplayEntity(level, spawnerBlockEntity.getBlockPos());
			if (entity != null) {
				renderEntityInSpawner(f, poseStack, multiBufferSource, i, entity, this.entityRenderer, baseSpawner.getoSpin(), baseSpawner.getSpin());
			}
		}
	}

	public static void renderEntityInSpawner(
		float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Entity entity, EntityRenderDispatcher entityRenderDispatcher, double d, double e
	) {
		poseStack.pushPose();
		poseStack.translate(0.5F, 0.0F, 0.5F);
		float g = 0.53125F;
		float h = Math.max(entity.getBbWidth(), entity.getBbHeight());
		if ((double)h > 1.0) {
			g /= h;
		}

		poseStack.translate(0.0F, 0.4F, 0.0F);
		poseStack.mulPose(Axis.YP.rotationDegrees((float)Mth.lerp((double)f, d, e) * 10.0F));
		poseStack.translate(0.0F, -0.2F, 0.0F);
		poseStack.mulPose(Axis.XP.rotationDegrees(-30.0F));
		poseStack.scale(g, g, g);
		entityRenderDispatcher.render(entity, 0.0, 0.0, 0.0, 0.0F, f, poseStack, multiBufferSource, i);
		poseStack.popPose();
	}
}
