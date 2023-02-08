package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import org.joml.Vector3f;

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
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.debugFilledBox());
		BlockPos blockPos = new BlockPos(d, 0.0, f);

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
								poseStack,
								vertexConsumer,
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
	}

	private Vector3f getColor(Heightmap.Types types) {
		return switch (types) {
			case WORLD_SURFACE_WG -> new Vector3f(1.0F, 1.0F, 0.0F);
			case OCEAN_FLOOR_WG -> new Vector3f(1.0F, 0.0F, 1.0F);
			case WORLD_SURFACE -> new Vector3f(0.0F, 0.7F, 0.0F);
			case OCEAN_FLOOR -> new Vector3f(0.0F, 0.0F, 0.5F);
			case MOTION_BLOCKING -> new Vector3f(0.0F, 0.3F, 0.3F);
			case MOTION_BLOCKING_NO_LEAVES -> new Vector3f(0.0F, 0.5F, 0.5F);
		};
	}
}
