package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

@Environment(EnvType.CLIENT)
public class LightDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
	private final Minecraft minecraft;
	private static final int MAX_RENDER_DIST = 10;

	public LightDebugRenderer(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
		Level level = this.minecraft.level;
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableTexture();
		BlockPos blockPos = new BlockPos(d, e, f);
		LongSet longSet = new LongOpenHashSet();

		for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-10, -10, -10), blockPos.offset(10, 10, 10))) {
			int i = level.getBrightness(LightLayer.SKY, blockPos2);
			float g = (float)(15 - i) / 15.0F * 0.5F + 0.16F;
			int j = Mth.hsvToRgb(g, 0.9F, 0.9F);
			long l = SectionPos.blockToSection(blockPos2.asLong());
			if (longSet.add(l)) {
				DebugRenderer.renderFloatingText(
					level.getChunkSource().getLightEngine().getDebugData(LightLayer.SKY, SectionPos.of(l)),
					(double)SectionPos.sectionToBlockCoord(SectionPos.x(l), 8),
					(double)SectionPos.sectionToBlockCoord(SectionPos.y(l), 8),
					(double)SectionPos.sectionToBlockCoord(SectionPos.z(l), 8),
					16711680,
					0.3F
				);
			}

			if (i != 15) {
				DebugRenderer.renderFloatingText(String.valueOf(i), (double)blockPos2.getX() + 0.5, (double)blockPos2.getY() + 0.25, (double)blockPos2.getZ() + 0.5, j);
			}
		}

		RenderSystem.enableTexture();
	}
}
