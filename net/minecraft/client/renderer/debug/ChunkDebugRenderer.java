/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.debug;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.platform.GlStateManager;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.MultiPlayerLevel;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ChunkDebugRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private double lastUpdateTime = Double.MIN_VALUE;
    private final int radius = 12;
    @Nullable
    private ChunkData data;

    public ChunkDebugRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void render(long l) {
        double d = Util.getNanos();
        if (d - this.lastUpdateTime > 3.0E9) {
            this.lastUpdateTime = d;
            IntegratedServer integratedServer = this.minecraft.getSingleplayerServer();
            this.data = integratedServer != null ? new ChunkData(integratedServer) : null;
        }
        if (this.data != null) {
            GlStateManager.disableFog();
            GlStateManager.enableBlend();
            GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.lineWidth(2.0f);
            GlStateManager.disableTexture();
            GlStateManager.depthMask(false);
            Map map = this.data.serverData.getNow(null);
            double e = this.minecraft.gameRenderer.getMainCamera().getPosition().y * 0.85;
            for (Map.Entry entry : this.data.clientData.entrySet()) {
                ChunkPos chunkPos = (ChunkPos)entry.getKey();
                String string = (String)entry.getValue();
                if (map != null) {
                    string = string + (String)map.get(chunkPos);
                }
                String[] strings = string.split("\n");
                int i = 0;
                for (String string2 : strings) {
                    DebugRenderer.renderFloatingText(string2, (chunkPos.x << 4) + 8, e + (double)i, (chunkPos.z << 4) + 8, -1, 0.15f);
                    i -= 2;
                }
            }
            GlStateManager.depthMask(true);
            GlStateManager.enableTexture();
            GlStateManager.disableBlend();
            GlStateManager.enableFog();
        }
    }

    @Environment(value=EnvType.CLIENT)
    final class ChunkData {
        private final Map<ChunkPos, String> clientData;
        private final CompletableFuture<Map<ChunkPos, String>> serverData;

        private ChunkData(IntegratedServer integratedServer) {
            MultiPlayerLevel multiPlayerLevel = ((ChunkDebugRenderer)ChunkDebugRenderer.this).minecraft.level;
            DimensionType dimensionType = ((ChunkDebugRenderer)ChunkDebugRenderer.this).minecraft.level.dimension.getType();
            ServerLevel serverLevel = integratedServer.getLevel(dimensionType) != null ? integratedServer.getLevel(dimensionType) : null;
            Camera camera = ((ChunkDebugRenderer)ChunkDebugRenderer.this).minecraft.gameRenderer.getMainCamera();
            int i = (int)camera.getPosition().x >> 4;
            int j = (int)camera.getPosition().z >> 4;
            ImmutableMap.Builder<ChunkPos, String> builder = ImmutableMap.builder();
            ClientChunkCache clientChunkCache = multiPlayerLevel.getChunkSource();
            for (int k = i - 12; k <= i + 12; ++k) {
                for (int l = j - 12; l <= j + 12; ++l) {
                    ChunkPos chunkPos = new ChunkPos(k, l);
                    String string = "";
                    LevelChunk levelChunk = clientChunkCache.getChunk(k, l, false);
                    string = string + "Client: ";
                    if (levelChunk == null) {
                        string = string + "0n/a\n";
                    } else {
                        string = string + (levelChunk.isEmpty() ? " E" : "");
                        string = string + "\n";
                    }
                    builder.put(chunkPos, string);
                }
            }
            this.clientData = builder.build();
            this.serverData = integratedServer.submit(() -> {
                ImmutableMap.Builder<ChunkPos, String> builder = ImmutableMap.builder();
                ServerChunkCache serverChunkCache = serverLevel.getChunkSource();
                for (int k = i - 12; k <= i + 12; ++k) {
                    for (int l = j - 12; l <= j + 12; ++l) {
                        ChunkPos chunkPos = new ChunkPos(k, l);
                        builder.put(chunkPos, "Server: " + serverChunkCache.getChunkDebugData(chunkPos));
                    }
                }
                return builder.build();
            });
        }
    }
}

