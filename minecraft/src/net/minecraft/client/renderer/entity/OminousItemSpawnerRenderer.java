package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.OminousItemSpawner;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Environment(EnvType.CLIENT)
public class OminousItemSpawnerRenderer extends EntityRenderer<OminousItemSpawner> {
	private static final float ROTATION_SPEED = 40.0F;
	private static final int TICKS_SCALING = 50;
	private final ItemRenderer itemRenderer;

	protected OminousItemSpawnerRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.itemRenderer = context.getItemRenderer();
	}

	public ResourceLocation getTextureLocation(OminousItemSpawner ominousItemSpawner) {
		return TextureAtlas.LOCATION_BLOCKS;
	}

	public void render(OminousItemSpawner ominousItemSpawner, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		ItemStack itemStack = ominousItemSpawner.getItem();
		if (!itemStack.isEmpty()) {
			poseStack.pushPose();
			if (ominousItemSpawner.tickCount <= 50) {
				float h = Math.min((float)ominousItemSpawner.tickCount + g, 50.0F) / 50.0F;
				poseStack.scale(h, h, h);
			}

			Level level = ominousItemSpawner.level();
			float j = Mth.wrapDegrees((float)(level.getGameTime() - 1L)) * 40.0F;
			float k = Mth.wrapDegrees((float)level.getGameTime()) * 40.0F;
			poseStack.mulPose(Axis.YP.rotationDegrees(Mth.rotLerp(g, j, k)));
			ItemEntityRenderer.renderMultipleFromCount(this.itemRenderer, poseStack, multiBufferSource, 15728880, itemStack, level.random, level);
			poseStack.popPose();
		}
	}
}
