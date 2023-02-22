/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class HeightMapRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private static final int CHUNK_DIST = 2;
    private static final float BOX_HEIGHT = 0.09375f;

    public HeightMapRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
        ClientLevel levelAccessor = this.minecraft.level;
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.debugFilledBox());
        BlockPos blockPos = BlockPos.containing(d, 0.0, f);
        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                ChunkAccess chunkAccess = levelAccessor.getChunk(blockPos.offset(i * 16, 0, j * 16));
                for (Map.Entry<Heightmap.Types, Heightmap> entry : chunkAccess.getHeightmaps()) {
                    Heightmap.Types types = entry.getKey();
                    ChunkPos chunkPos = chunkAccess.getPos();
                    Vector3f vector3f = this.getColor(types);
                    for (int k = 0; k < 16; ++k) {
                        for (int l = 0; l < 16; ++l) {
                            int m = SectionPos.sectionToBlockCoord(chunkPos.x, k);
                            int n = SectionPos.sectionToBlockCoord(chunkPos.z, l);
                            float g = (float)((double)((float)levelAccessor.getHeight(types, m, n) + (float)types.ordinal() * 0.09375f) - e);
                            LevelRenderer.addChainedFilledBoxVertices(poseStack, vertexConsumer, (double)((float)m + 0.25f) - d, (double)g, (double)((float)n + 0.25f) - f, (double)((float)m + 0.75f) - d, (double)(g + 0.09375f), (double)((float)n + 0.75f) - f, vector3f.x(), vector3f.y(), vector3f.z(), 1.0f);
                        }
                    }
                }
            }
        }
    }

    private Vector3f getColor(Heightmap.Types types) {
        return switch (types) {
            default -> throw new IncompatibleClassChangeError();
            case Heightmap.Types.WORLD_SURFACE_WG -> new Vector3f(1.0f, 1.0f, 0.0f);
            case Heightmap.Types.OCEAN_FLOOR_WG -> new Vector3f(1.0f, 0.0f, 1.0f);
            case Heightmap.Types.WORLD_SURFACE -> new Vector3f(0.0f, 0.7f, 0.0f);
            case Heightmap.Types.OCEAN_FLOOR -> new Vector3f(0.0f, 0.0f, 0.5f);
            case Heightmap.Types.MOTION_BLOCKING -> new Vector3f(0.0f, 0.3f, 0.3f);
            case Heightmap.Types.MOTION_BLOCKING_NO_LEAVES -> new Vector3f(0.0f, 0.5f, 0.5f);
        };
    }
}

