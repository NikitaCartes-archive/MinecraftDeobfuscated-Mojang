package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Vector3f;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

@Environment(EnvType.CLIENT)
public class HeightMapRenderer implements DebugRenderer.SimpleDebugRenderer {
	private final Minecraft minecraft;
	private static final int CHUNK_DIST = 2;
	private static final float BOX_HEIGHT = 0.09375F;

	public HeightMapRenderer(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
		LevelAccessor levelAccessor = this.minecraft.level;
		RenderSystem.disableBlend();
		RenderSystem.disableTexture();
		RenderSystem.enableDepthTest();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		BlockPos blockPos = new BlockPos(d, 0.0, f);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

		for (int i = -2; i <= 2; i++) {
			for (int j = -2; j <= 2; j++) {
				ChunkAccess chunkAccess = levelAccessor.getChunk(blockPos.offset(i * 16, 0, j * 16));

				for (Entry<Heightmap.Types, Heightmap> entry : chunkAccess.getHeightmaps()) {
					Heightmap.Types types = (Heightmap.Types)entry.getKey();
					ChunkPos chunkPos = chunkAccess.getPos();
					Vector3f vector3f = this.getColor(types);

					for (int k = 0; k < 16; k++) {
						for (int l = 0; l < 16; l++) {
							int m = SectionPos.sectionToBlockCoord(chunkPos.x, k);
							int n = SectionPos.sectionToBlockCoord(chunkPos.z, l);
							float g = (float)((double)((float)levelAccessor.getHeight(types, m, n) + (float)types.ordinal() * 0.09375F) - e);
							LevelRenderer.addChainedFilledBoxVertices(
								bufferBuilder,
								(double)((float)m + 0.25F) - d,
								(double)g,
								(double)((float)n + 0.25F) - f,
								(double)((float)m + 0.75F) - d,
								(double)(g + 0.09375F),
								(double)((float)n + 0.75F) - f,
								vector3f.x(),
								vector3f.y(),
								vector3f.z(),
								1.0F
							);
						}
					}
				}
			}
		}

		tesselator.end();
		RenderSystem.enableTexture();
	}

	private Vector3f getColor(Heightmap.Types types) {
		switch (types) {
			case WORLD_SURFACE_WG:
				return new Vector3f(1.0F, 1.0F, 0.0F);
			case OCEAN_FLOOR_WG:
				return new Vector3f(1.0F, 0.0F, 1.0F);
			case WORLD_SURFACE:
				return new Vector3f(0.0F, 0.7F, 0.0F);
			case OCEAN_FLOOR:
				return new Vector3f(0.0F, 0.0F, 0.5F);
			case MOTION_BLOCKING:
				return new Vector3f(0.0F, 0.3F, 0.3F);
			case MOTION_BLOCKING_NO_LEAVES:
				return new Vector3f(0.0F, 0.5F, 0.5F);
			default:
				return new Vector3f(0.0F, 0.0F, 0.0F);
		}
	}
}
