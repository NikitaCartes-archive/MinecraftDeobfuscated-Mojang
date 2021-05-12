/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.debug;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.lang.invoke.CallSite;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ChunkDebugRenderer
implements DebugRenderer.SimpleDebugRenderer {
    final Minecraft minecraft;
    private double lastUpdateTime = Double.MIN_VALUE;
    private final int radius = 12;
    @Nullable
    private ChunkData data;

    public ChunkDebugRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
        double g = Util.getNanos();
        if (g - this.lastUpdateTime > 3.0E9) {
            this.lastUpdateTime = g;
            IntegratedServer integratedServer = this.minecraft.getSingleplayerServer();
            this.data = integratedServer != null ? new ChunkData(integratedServer, d, f) : null;
        }
        if (this.data != null) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.lineWidth(2.0f);
            RenderSystem.disableTexture();
            RenderSystem.depthMask(false);
            Map map = this.data.serverData.getNow(null);
            double h = this.minecraft.gameRenderer.getMainCamera().getPosition().y * 0.85;
            for (Map.Entry<ChunkPos, String> entry : this.data.clientData.entrySet()) {
                ChunkPos chunkPos = entry.getKey();
                Object string = entry.getValue();
                if (map != null) {
                    string = (String)string + (String)map.get(chunkPos);
                }
                String[] strings = ((String)string).split("\n");
                int i = 0;
                for (String string2 : strings) {
                    DebugRenderer.renderFloatingText(string2, SectionPos.sectionToBlockCoord(chunkPos.x, 8), h + (double)i, SectionPos.sectionToBlockCoord(chunkPos.z, 8), -1, 0.15f);
                    i -= 2;
                }
            }
            RenderSystem.depthMask(true);
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
        }
    }

    @Environment(value=EnvType.CLIENT)
    final class ChunkData {
        final Map<ChunkPos, String> clientData;
        final CompletableFuture<Map<ChunkPos, String>> serverData;

        ChunkData(IntegratedServer integratedServer, double d, double e) {
            ClientLevel clientLevel = ChunkDebugRenderer.this.minecraft.level;
            ResourceKey<Level> resourceKey = clientLevel.dimension();
            int i = SectionPos.posToSectionCoord(d);
            int j = SectionPos.posToSectionCoord(e);
            ImmutableMap.Builder<ChunkPos, Object> builder = ImmutableMap.builder();
            ClientChunkCache clientChunkCache = clientLevel.getChunkSource();
            for (int k = i - 12; k <= i + 12; ++k) {
                for (int l = j - 12; l <= j + 12; ++l) {
                    ChunkPos chunkPos = new ChunkPos(k, l);
                    Object string = "";
                    LevelChunk levelChunk = clientChunkCache.getChunk(k, l, false);
                    string = (String)string + "Client: ";
                    if (levelChunk == null) {
                        string = (String)string + "0n/a\n";
                    } else {
                        string = (String)string + (levelChunk.isEmpty() ? " E" : "");
                        string = (String)string + "\n";
                    }
                    builder.put(chunkPos, string);
                }
            }
            this.clientData = builder.build();
            this.serverData = integratedServer.submit(() -> {
                ServerLevel serverLevel = integratedServer.getLevel(resourceKey);
                if (serverLevel == null) {
                    return ImmutableMap.of();
                }
                ImmutableMap.Builder<ChunkPos, CallSite> builder = ImmutableMap.builder();
                ServerChunkCache serverChunkCache = serverLevel.getChunkSource();
                for (int k = i - 12; k <= i + 12; ++k) {
                    for (int l = j - 12; l <= j + 12; ++l) {
                        ChunkPos chunkPos = new ChunkPos(k, l);
                        builder.put(chunkPos, (CallSite)((Object)("Server: " + serverChunkCache.getChunkDebugData(chunkPos))));
                    }
                }
                return builder.build();
            });
        }
    }
}

