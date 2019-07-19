package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.platform.GlStateManager;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

@Environment(EnvType.CLIENT)
public class LightDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
	private final Minecraft minecraft;

	public LightDebugRenderer(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	@Override
	public void render(long l) {
		Camera camera = this.minecraft.gameRenderer.getMainCamera();
		Level level = this.minecraft.level;
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.blendFuncSeparate(
			GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
		);
		GlStateManager.disableTexture();
		BlockPos blockPos = new BlockPos(camera.getPosition());
		LongSet longSet = new LongOpenHashSet();

		for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-10, -10, -10), blockPos.offset(10, 10, 10))) {
			int i = level.getBrightness(LightLayer.SKY, blockPos2);
			float f = (float)(15 - i) / 15.0F * 0.5F + 0.16F;
			int j = Mth.hsvToRgb(f, 0.9F, 0.9F);
			long m = SectionPos.blockToSection(blockPos2.asLong());
			if (longSet.add(m)) {
				DebugRenderer.renderFloatingText(
					level.getChunkSource().getLightEngine().getDebugData(LightLayer.SKY, SectionPos.of(m)),
					(double)(SectionPos.x(m) * 16 + 8),
					(double)(SectionPos.y(m) * 16 + 8),
					(double)(SectionPos.z(m) * 16 + 8),
					16711680,
					0.3F
				);
			}

			if (i != 15) {
				DebugRenderer.renderFloatingText(String.valueOf(i), (double)blockPos2.getX() + 0.5, (double)blockPos2.getY() + 0.25, (double)blockPos2.getZ() + 0.5, j);
			}
		}

		GlStateManager.enableTexture();
		GlStateManager.popMatrix();
	}
}
