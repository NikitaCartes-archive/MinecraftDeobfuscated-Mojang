/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Vector3f;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

@Environment(value=EnvType.CLIENT)
public class HeightMapRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;

    public HeightMapRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
        ClientLevel levelAccessor = this.minecraft.level;
        RenderSystem.pushMatrix();
        RenderSystem.disableBlend();
        RenderSystem.disableTexture();
        RenderSystem.enableDepthTest();
        BlockPos blockPos = new BlockPos(d, 0.0, f);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(5, DefaultVertexFormat.POSITION_COLOR);
        for (int i = -32; i <= 32; i += 16) {
            for (int j = -32; j <= 32; j += 16) {
                ChunkAccess chunkAccess = levelAccessor.getChunk(blockPos.offset(i, 0, j));
                for (Map.Entry<Heightmap.Types, Heightmap> entry : chunkAccess.getHeightmaps()) {
                    Heightmap.Types types = entry.getKey();
                    ChunkPos chunkPos = chunkAccess.getPos();
                    Vector3f vector3f = this.getColor(types);
                    for (int k = 0; k < 16; ++k) {
                        for (int l = 0; l < 16; ++l) {
                            int m = chunkPos.x * 16 + k;
                            int n = chunkPos.z * 16 + l;
                            float g = (float)((double)((float)levelAccessor.getHeight(types, m, n) + (float)types.ordinal() * 0.09375f) - e);
                            LevelRenderer.addChainedFilledBoxVertices(bufferBuilder, (double)((float)m + 0.25f) - d, g, (double)((float)n + 0.25f) - f, (double)((float)m + 0.75f) - d, g + 0.09375f, (double)((float)n + 0.75f) - f, vector3f.x(), vector3f.y(), vector3f.z(), 1.0f);
                        }
                    }
                }
            }
        }
        tesselator.end();
        RenderSystem.enableTexture();
        RenderSystem.popMatrix();
    }

    private Vector3f getColor(Heightmap.Types types) {
        switch (types) {
            case WORLD_SURFACE_WG: {
                return new Vector3f(1.0f, 1.0f, 0.0f);
            }
            case OCEAN_FLOOR_WG: {
                return new Vector3f(1.0f, 0.0f, 1.0f);
            }
            case WORLD_SURFACE: {
                return new Vector3f(0.0f, 0.7f, 0.0f);
            }
            case OCEAN_FLOOR: {
                return new Vector3f(0.0f, 0.0f, 0.5f);
            }
            case MOTION_BLOCKING: {
                return new Vector3f(0.0f, 0.3f, 0.3f);
            }
            case MOTION_BLOCKING_NO_LEAVES: {
                return new Vector3f(0.0f, 0.5f, 0.5f);
            }
        }
        return new Vector3f(0.0f, 0.0f, 0.0f);
    }
}

