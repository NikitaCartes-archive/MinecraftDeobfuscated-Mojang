package net.minecraft.client.renderer.debug;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

@Environment(EnvType.CLIENT)
public class ChunkDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
	private final Minecraft minecraft;
	private double lastUpdateTime = Double.MIN_VALUE;
	private final int radius = 12;
	@Nullable
	private ChunkDebugRenderer.ChunkData data;

	public ChunkDebugRenderer(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
		double g = (double)Util.getNanos();
		if (g - this.lastUpdateTime > 3.0E9) {
			this.lastUpdateTime = g;
			IntegratedServer integratedServer = this.minecraft.getSingleplayerServer();
			if (integratedServer != null) {
				this.data = new ChunkDebugRenderer.ChunkData(integratedServer, d, f);
			} else {
				this.data = null;
			}
		}

		if (this.data != null) {
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.lineWidth(2.0F);
			RenderSystem.disableTexture();
			RenderSystem.depthMask(false);
			Map<ChunkPos, String> map = (Map<ChunkPos, String>)this.data.serverData.getNow(null);
			double h = this.minecraft.gameRenderer.getMainCamera().getPosition().y * 0.85;

			for (Entry<ChunkPos, String> entry : this.data.clientData.entrySet()) {
				ChunkPos chunkPos = (ChunkPos)entry.getKey();
				String string = (String)entry.getValue();
				if (map != null) {
					string = string + (String)map.get(chunkPos);
				}

				String[] strings = string.split("\n");
				int i = 0;

				for (String string2 : strings) {
					DebugRenderer.renderFloatingText(
						string2, (double)SectionPos.sectionToBlockCoord(chunkPos.x, 8), h + (double)i, (double)SectionPos.sectionToBlockCoord(chunkPos.z, 8), -1, 0.15F
					);
					i -= 2;
				}
			}

			RenderSystem.depthMask(true);
			RenderSystem.enableTexture();
			RenderSystem.disableBlend();
		}
	}

	@Environment(EnvType.CLIENT)
	final class ChunkData {
		private final Map<ChunkPos, String> clientData;
		private final CompletableFuture<Map<ChunkPos, String>> serverData;

		private ChunkData(IntegratedServer integratedServer, double d, double e) {
			ClientLevel clientLevel = ChunkDebugRenderer.this.minecraft.level;
			ResourceKey<Level> resourceKey = clientLevel.dimension();
			int i = SectionPos.posToSectionCoord(d);
			int j = SectionPos.posToSectionCoord(e);
			Builder<ChunkPos, String> builder = ImmutableMap.builder();
			ClientChunkCache clientChunkCache = clientLevel.getChunkSource();

			for (int k = i - 12; k <= i + 12; k++) {
				for (int l = j - 12; l <= j + 12; l++) {
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
				ServerLevel serverLevel = integratedServer.getLevel(resourceKey);
				if (serverLevel == null) {
					return ImmutableMap.of();
				} else {
					Builder<ChunkPos, String> builderx = ImmutableMap.builder();
					ServerChunkCache serverChunkCache = serverLevel.getChunkSource();

					for (int kx = i - 12; kx <= i + 12; kx++) {
						for (int lx = j - 12; lx <= j + 12; lx++) {
							ChunkPos chunkPosx = new ChunkPos(kx, lx);
							builderx.put(chunkPosx, "Server: " + serverChunkCache.getChunkDebugData(chunkPosx));
						}
					}

					return builderx.build();
				}
			});
		}
	}
}
