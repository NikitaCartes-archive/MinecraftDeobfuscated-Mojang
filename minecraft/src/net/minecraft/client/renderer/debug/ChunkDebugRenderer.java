package net.minecraft.client.renderer.debug;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.MultiPlayerLevel;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;

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
	public void render(long l) {
		double d = (double)Util.getNanos();
		if (d - this.lastUpdateTime > 3.0E9) {
			this.lastUpdateTime = d;
			IntegratedServer integratedServer = this.minecraft.getSingleplayerServer();
			if (integratedServer != null) {
				this.data = new ChunkDebugRenderer.ChunkData(integratedServer);
			} else {
				this.data = null;
			}
		}

		if (this.data != null) {
			RenderSystem.disableFog();
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(
				GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
			);
			RenderSystem.lineWidth(2.0F);
			RenderSystem.disableTexture();
			RenderSystem.depthMask(false);
			Map<ChunkPos, String> map = (Map<ChunkPos, String>)this.data.serverData.getNow(null);
			double e = this.minecraft.gameRenderer.getMainCamera().getPosition().y * 0.85;

			for (Entry<ChunkPos, String> entry : this.data.clientData.entrySet()) {
				ChunkPos chunkPos = (ChunkPos)entry.getKey();
				String string = (String)entry.getValue();
				if (map != null) {
					string = string + (String)map.get(chunkPos);
				}

				String[] strings = string.split("\n");
				int i = 0;

				for (String string2 : strings) {
					DebugRenderer.renderFloatingText(string2, (double)((chunkPos.x << 4) + 8), e + (double)i, (double)((chunkPos.z << 4) + 8), -1, 0.15F);
					i -= 2;
				}
			}

			RenderSystem.depthMask(true);
			RenderSystem.enableTexture();
			RenderSystem.disableBlend();
			RenderSystem.enableFog();
		}
	}

	@Environment(EnvType.CLIENT)
	final class ChunkData {
		private final Map<ChunkPos, String> clientData;
		private final CompletableFuture<Map<ChunkPos, String>> serverData;

		private ChunkData(IntegratedServer integratedServer) {
			MultiPlayerLevel multiPlayerLevel = ChunkDebugRenderer.this.minecraft.level;
			DimensionType dimensionType = ChunkDebugRenderer.this.minecraft.level.dimension.getType();
			ServerLevel serverLevel;
			if (integratedServer.getLevel(dimensionType) != null) {
				serverLevel = integratedServer.getLevel(dimensionType);
			} else {
				serverLevel = null;
			}

			Camera camera = ChunkDebugRenderer.this.minecraft.gameRenderer.getMainCamera();
			int i = (int)camera.getPosition().x >> 4;
			int j = (int)camera.getPosition().z >> 4;
			Builder<ChunkPos, String> builder = ImmutableMap.builder();
			ClientChunkCache clientChunkCache = multiPlayerLevel.getChunkSource();

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
				Builder<ChunkPos, String> builderx = ImmutableMap.builder();
				ServerChunkCache serverChunkCache = serverLevel.getChunkSource();

				for (int kx = i - 12; kx <= i + 12; kx++) {
					for (int lx = j - 12; lx <= j + 12; lx++) {
						ChunkPos chunkPosx = new ChunkPos(kx, lx);
						builderx.put(chunkPosx, "Server: " + serverChunkCache.getChunkDebugData(chunkPosx));
					}
				}

				return builderx.build();
			});
		}
	}
}
