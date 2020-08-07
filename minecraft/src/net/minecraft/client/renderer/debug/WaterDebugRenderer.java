package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;

@Environment(EnvType.CLIENT)
public class WaterDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
	private final Minecraft minecraft;

	public WaterDebugRenderer(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
		BlockPos blockPos = this.minecraft.player.blockPosition();
		LevelReader levelReader = this.minecraft.player.level;
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.color4f(0.0F, 1.0F, 0.0F, 0.75F);
		RenderSystem.disableTexture();
		RenderSystem.lineWidth(6.0F);

		for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-10, -10, -10), blockPos.offset(10, 10, 10))) {
			FluidState fluidState = levelReader.getFluidState(blockPos2);
			if (fluidState.is(FluidTags.WATER)) {
				double g = (double)((float)blockPos2.getY() + fluidState.getHeight(levelReader, blockPos2));
				DebugRenderer.renderFilledBox(
					new AABB(
							(double)((float)blockPos2.getX() + 0.01F),
							(double)((float)blockPos2.getY() + 0.01F),
							(double)((float)blockPos2.getZ() + 0.01F),
							(double)((float)blockPos2.getX() + 0.99F),
							g,
							(double)((float)blockPos2.getZ() + 0.99F)
						)
						.move(-d, -e, -f),
					1.0F,
					1.0F,
					1.0F,
					0.2F
				);
			}
		}

		for (BlockPos blockPos2x : BlockPos.betweenClosed(blockPos.offset(-10, -10, -10), blockPos.offset(10, 10, 10))) {
			FluidState fluidState = levelReader.getFluidState(blockPos2x);
			if (fluidState.is(FluidTags.WATER)) {
				DebugRenderer.renderFloatingText(
					String.valueOf(fluidState.getAmount()),
					(double)blockPos2x.getX() + 0.5,
					(double)((float)blockPos2x.getY() + fluidState.getHeight(levelReader, blockPos2x)),
					(double)blockPos2x.getZ() + 0.5,
					-16777216
				);
			}
		}

		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
	}
}
