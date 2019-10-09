package net.minecraft.client.gui.components;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.DataFixUtils;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

@Environment(EnvType.CLIENT)
public class DebugScreenOverlay extends GuiComponent {
	private static final Map<Heightmap.Types, String> HEIGHTMAP_NAMES = Util.make(new EnumMap(Heightmap.Types.class), enumMap -> {
		enumMap.put(Heightmap.Types.WORLD_SURFACE_WG, "SW");
		enumMap.put(Heightmap.Types.WORLD_SURFACE, "S");
		enumMap.put(Heightmap.Types.OCEAN_FLOOR_WG, "OW");
		enumMap.put(Heightmap.Types.OCEAN_FLOOR, "O");
		enumMap.put(Heightmap.Types.MOTION_BLOCKING, "M");
		enumMap.put(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, "ML");
	});
	private final Minecraft minecraft;
	private final Font font;
	private HitResult block;
	private HitResult liquid;
	@Nullable
	private ChunkPos lastPos;
	@Nullable
	private LevelChunk clientChunk;
	@Nullable
	private CompletableFuture<LevelChunk> serverChunk;

	public DebugScreenOverlay(Minecraft minecraft) {
		this.minecraft = minecraft;
		this.font = minecraft.font;
	}

	public void clearChunkCache() {
		this.serverChunk = null;
		this.clientChunk = null;
	}

	public void render() {
		this.minecraft.getProfiler().push("debug");
		RenderSystem.pushMatrix();
		Entity entity = this.minecraft.getCameraEntity();
		this.block = entity.pick(20.0, 0.0F, false);
		this.liquid = entity.pick(20.0, 0.0F, true);
		this.drawGameInformation();
		this.drawSystemInformation();
		RenderSystem.popMatrix();
		if (this.minecraft.options.renderFpsChart) {
			int i = this.minecraft.getWindow().getGuiScaledWidth();
			this.drawChart(this.minecraft.getFrameTimer(), 0, i / 2, true);
			IntegratedServer integratedServer = this.minecraft.getSingleplayerServer();
			if (integratedServer != null) {
				this.drawChart(integratedServer.getFrameTimer(), i - Math.min(i / 2, 240), i / 2, false);
			}
		}

		this.minecraft.getProfiler().pop();
	}

	protected void drawGameInformation() {
		List<String> list = this.getGameInformation();
		list.add("");
		boolean bl = this.minecraft.getSingleplayerServer() != null;
		list.add(
			"Debug: Pie [shift]: "
				+ (this.minecraft.options.renderDebugCharts ? "visible" : "hidden")
				+ (bl ? " FPS + TPS" : " FPS")
				+ " [alt]: "
				+ (this.minecraft.options.renderFpsChart ? "visible" : "hidden")
		);
		list.add("For help: press F3 + Q");

		for (int i = 0; i < list.size(); i++) {
			String string = (String)list.get(i);
			if (!Strings.isNullOrEmpty(string)) {
				int j = 9;
				int k = this.font.width(string);
				int l = 2;
				int m = 2 + j * i;
				fill(1, m - 1, 2 + k + 1, m + j - 1, -1873784752);
				this.font.draw(string, 2.0F, (float)m, 14737632);
			}
		}
	}

	protected void drawSystemInformation() {
		List<String> list = this.getSystemInformation();

		for (int i = 0; i < list.size(); i++) {
			String string = (String)list.get(i);
			if (!Strings.isNullOrEmpty(string)) {
				int j = 9;
				int k = this.font.width(string);
				int l = this.minecraft.getWindow().getGuiScaledWidth() - 2 - k;
				int m = 2 + j * i;
				fill(l - 1, m - 1, l + k + 1, m + j - 1, -1873784752);
				this.font.draw(string, (float)l, (float)m, 14737632);
			}
		}
	}

	protected List<String> getGameInformation() {
		IntegratedServer integratedServer = this.minecraft.getSingleplayerServer();
		Connection connection = this.minecraft.getConnection().getConnection();
		float f = connection.getAverageSentPackets();
		float g = connection.getAverageReceivedPackets();
		String string;
		if (integratedServer != null) {
			string = String.format("Integrated server @ %.0f ms ticks, %.0f tx, %.0f rx", integratedServer.getAverageTickTime(), f, g);
		} else {
			string = String.format("\"%s\" server, %.0f tx, %.0f rx", this.minecraft.player.getServerBrand(), f, g);
		}

		BlockPos blockPos = new BlockPos(this.minecraft.getCameraEntity());
		if (this.minecraft.showOnlyReducedInfo()) {
			return Lists.<String>newArrayList(
				"Minecraft "
					+ SharedConstants.getCurrentVersion().getName()
					+ " ("
					+ this.minecraft.getLaunchedVersion()
					+ "/"
					+ ClientBrandRetriever.getClientModName()
					+ ")",
				this.minecraft.fpsString,
				string,
				this.minecraft.levelRenderer.getChunkStatistics(),
				this.minecraft.levelRenderer.getEntityStatistics(),
				"P: " + this.minecraft.particleEngine.countParticles() + ". T: " + this.minecraft.level.getEntityCount(),
				this.minecraft.level.gatherChunkSourceStats(),
				"",
				String.format("Chunk-relative: %d %d %d", blockPos.getX() & 15, blockPos.getY() & 15, blockPos.getZ() & 15)
			);
		} else {
			Entity entity = this.minecraft.getCameraEntity();
			Direction direction = entity.getDirection();
			String string2;
			switch (direction) {
				case NORTH:
					string2 = "Towards negative Z";
					break;
				case SOUTH:
					string2 = "Towards positive Z";
					break;
				case WEST:
					string2 = "Towards negative X";
					break;
				case EAST:
					string2 = "Towards positive X";
					break;
				default:
					string2 = "Invalid";
			}

			ChunkPos chunkPos = new ChunkPos(blockPos);
			if (!Objects.equals(this.lastPos, chunkPos)) {
				this.lastPos = chunkPos;
				this.clearChunkCache();
			}

			Level level = this.getLevel();
			LongSet longSet = (LongSet)(level instanceof ServerLevel ? ((ServerLevel)level).getForcedChunks() : LongSets.EMPTY_SET);
			List<String> list = Lists.<String>newArrayList(
				"Minecraft "
					+ SharedConstants.getCurrentVersion().getName()
					+ " ("
					+ this.minecraft.getLaunchedVersion()
					+ "/"
					+ ClientBrandRetriever.getClientModName()
					+ ("release".equalsIgnoreCase(this.minecraft.getVersionType()) ? "" : "/" + this.minecraft.getVersionType())
					+ ")",
				this.minecraft.fpsString,
				string,
				this.minecraft.levelRenderer.getChunkStatistics(),
				this.minecraft.levelRenderer.getEntityStatistics(),
				"P: " + this.minecraft.particleEngine.countParticles() + ". T: " + this.minecraft.level.getEntityCount(),
				this.minecraft.level.gatherChunkSourceStats()
			);
			String string3 = this.getServerChunkStats();
			if (string3 != null) {
				list.add(string3);
			}

			list.add(DimensionType.getName(this.minecraft.level.dimension.getType()).toString() + " FC: " + Integer.toString(longSet.size()));
			list.add("");
			list.add(
				String.format(
					Locale.ROOT,
					"XYZ: %.3f / %.5f / %.3f",
					this.minecraft.getCameraEntity().getX(),
					this.minecraft.getCameraEntity().getY(),
					this.minecraft.getCameraEntity().getZ()
				)
			);
			list.add(String.format("Block: %d %d %d", blockPos.getX(), blockPos.getY(), blockPos.getZ()));
			list.add(
				String.format(
					"Chunk: %d %d %d in %d %d %d",
					blockPos.getX() & 15,
					blockPos.getY() & 15,
					blockPos.getZ() & 15,
					blockPos.getX() >> 4,
					blockPos.getY() >> 4,
					blockPos.getZ() >> 4
				)
			);
			list.add(String.format(Locale.ROOT, "Facing: %s (%s) (%.1f / %.1f)", direction, string2, Mth.wrapDegrees(entity.yRot), Mth.wrapDegrees(entity.xRot)));
			if (this.minecraft.level != null) {
				if (this.minecraft.level.hasChunkAt(blockPos)) {
					LevelChunk levelChunk = this.getClientChunk();
					if (levelChunk.isEmpty()) {
						list.add("Waiting for chunk...");
					} else {
						int i = this.minecraft.level.getChunkSource().getLightEngine().getRawBrightness(blockPos, 0);
						int j = this.minecraft.level.getBrightness(LightLayer.SKY, blockPos);
						int k = this.minecraft.level.getBrightness(LightLayer.BLOCK, blockPos);
						list.add("Client Light: " + i + " (" + j + " sky, " + k + " block)");
						LevelChunk levelChunk2 = this.getServerChunk();
						if (levelChunk2 != null) {
							LevelLightEngine levelLightEngine = level.getChunkSource().getLightEngine();
							list.add(
								"Server Light: ("
									+ levelLightEngine.getLayerListener(LightLayer.SKY).getLightValue(blockPos)
									+ " sky, "
									+ levelLightEngine.getLayerListener(LightLayer.BLOCK).getLightValue(blockPos)
									+ " block)"
							);
						}

						StringBuilder stringBuilder = new StringBuilder("CH");

						for (Heightmap.Types types : Heightmap.Types.values()) {
							if (types.sendToClient()) {
								stringBuilder.append(" ").append((String)HEIGHTMAP_NAMES.get(types)).append(": ").append(levelChunk.getHeight(types, blockPos.getX(), blockPos.getZ()));
							}
						}

						list.add(stringBuilder.toString());
						if (levelChunk2 != null) {
							stringBuilder.setLength(0);
							stringBuilder.append("SH");

							for (Heightmap.Types typesx : Heightmap.Types.values()) {
								if (typesx.keepAfterWorldgen()) {
									stringBuilder.append(" ")
										.append((String)HEIGHTMAP_NAMES.get(typesx))
										.append(": ")
										.append(levelChunk2.getHeight(typesx, blockPos.getX(), blockPos.getZ()));
								}
							}

							list.add(stringBuilder.toString());
						}

						if (blockPos.getY() >= 0 && blockPos.getY() < 256) {
							list.add("Biome: " + Registry.BIOME.getKey(this.minecraft.level.getBiome(blockPos)));
							long l = 0L;
							float h = 0.0F;
							if (levelChunk2 != null) {
								h = level.getMoonBrightness();
								l = levelChunk2.getInhabitedTime();
							}

							DifficultyInstance difficultyInstance = new DifficultyInstance(level.getDifficulty(), level.getDayTime(), l, h);
							list.add(
								String.format(
									Locale.ROOT,
									"Local Difficulty: %.2f // %.2f (Day %d)",
									difficultyInstance.getEffectiveDifficulty(),
									difficultyInstance.getSpecialMultiplier(),
									this.minecraft.level.getDayTime() / 24000L
								)
							);
						}
					}
				} else {
					list.add("Outside of world...");
				}
			} else {
				list.add("Outside of world...");
			}

			PostChain postChain = this.minecraft.gameRenderer.currentEffect();
			if (postChain != null) {
				list.add("Shader: " + postChain.getName());
			}

			if (this.block.getType() == HitResult.Type.BLOCK) {
				BlockPos blockPos2 = ((BlockHitResult)this.block).getBlockPos();
				list.add(String.format("Looking at block: %d %d %d", blockPos2.getX(), blockPos2.getY(), blockPos2.getZ()));
			}

			if (this.liquid.getType() == HitResult.Type.BLOCK) {
				BlockPos blockPos2 = ((BlockHitResult)this.liquid).getBlockPos();
				list.add(String.format("Looking at liquid: %d %d %d", blockPos2.getX(), blockPos2.getY(), blockPos2.getZ()));
			}

			list.add(this.minecraft.getSoundManager().getDebugString());
			return list;
		}
	}

	@Nullable
	private String getServerChunkStats() {
		IntegratedServer integratedServer = this.minecraft.getSingleplayerServer();
		if (integratedServer != null) {
			ServerLevel serverLevel = integratedServer.getLevel(this.minecraft.level.getDimension().getType());
			if (serverLevel != null) {
				return serverLevel.gatherChunkSourceStats();
			}
		}

		return null;
	}

	private Level getLevel() {
		return DataFixUtils.orElse(
			Optional.ofNullable(this.minecraft.getSingleplayerServer()).map(integratedServer -> integratedServer.getLevel(this.minecraft.level.dimension.getType())),
			this.minecraft.level
		);
	}

	@Nullable
	private LevelChunk getServerChunk() {
		if (this.serverChunk == null) {
			IntegratedServer integratedServer = this.minecraft.getSingleplayerServer();
			if (integratedServer != null) {
				ServerLevel serverLevel = integratedServer.getLevel(this.minecraft.level.dimension.getType());
				if (serverLevel != null) {
					this.serverChunk = serverLevel.getChunkSource()
						.getChunkFuture(this.lastPos.x, this.lastPos.z, ChunkStatus.FULL, false)
						.thenApply(either -> either.map(chunkAccess -> (LevelChunk)chunkAccess, chunkLoadingFailure -> null));
				}
			}

			if (this.serverChunk == null) {
				this.serverChunk = CompletableFuture.completedFuture(this.getClientChunk());
			}
		}

		return (LevelChunk)this.serverChunk.getNow(null);
	}

	private LevelChunk getClientChunk() {
		if (this.clientChunk == null) {
			this.clientChunk = this.minecraft.level.getChunk(this.lastPos.x, this.lastPos.z);
		}

		return this.clientChunk;
	}

	protected List<String> getSystemInformation() {
		long l = Runtime.getRuntime().maxMemory();
		long m = Runtime.getRuntime().totalMemory();
		long n = Runtime.getRuntime().freeMemory();
		long o = m - n;
		List<String> list = Lists.<String>newArrayList(
			String.format("Java: %s %dbit", System.getProperty("java.version"), this.minecraft.is64Bit() ? 64 : 32),
			String.format("Mem: % 2d%% %03d/%03dMB", o * 100L / l, bytesToMegabytes(o), bytesToMegabytes(l)),
			String.format("Allocated: % 2d%% %03dMB", m * 100L / l, bytesToMegabytes(m)),
			"",
			String.format("CPU: %s", GlUtil.getCpuInfo()),
			"",
			String.format("Display: %dx%d (%s)", Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight(), GlUtil.getVendor()),
			GlUtil.getRenderer(),
			GlUtil.getOpenGLVersion()
		);
		if (this.minecraft.showOnlyReducedInfo()) {
			return list;
		} else {
			if (this.block.getType() == HitResult.Type.BLOCK) {
				BlockPos blockPos = ((BlockHitResult)this.block).getBlockPos();
				BlockState blockState = this.minecraft.level.getBlockState(blockPos);
				list.add("");
				list.add(ChatFormatting.UNDERLINE + "Targeted Block");
				list.add(String.valueOf(Registry.BLOCK.getKey(blockState.getBlock())));

				for (Entry<Property<?>, Comparable<?>> entry : blockState.getValues().entrySet()) {
					list.add(this.getPropertyValueString(entry));
				}

				for (ResourceLocation resourceLocation : this.minecraft.getConnection().getTags().getBlocks().getMatchingTags(blockState.getBlock())) {
					list.add("#" + resourceLocation);
				}
			}

			if (this.liquid.getType() == HitResult.Type.BLOCK) {
				BlockPos blockPos = ((BlockHitResult)this.liquid).getBlockPos();
				FluidState fluidState = this.minecraft.level.getFluidState(blockPos);
				list.add("");
				list.add(ChatFormatting.UNDERLINE + "Targeted Fluid");
				list.add(String.valueOf(Registry.FLUID.getKey(fluidState.getType())));

				for (Entry<Property<?>, Comparable<?>> entry : fluidState.getValues().entrySet()) {
					list.add(this.getPropertyValueString(entry));
				}

				for (ResourceLocation resourceLocation : this.minecraft.getConnection().getTags().getFluids().getMatchingTags(fluidState.getType())) {
					list.add("#" + resourceLocation);
				}
			}

			Entity entity = this.minecraft.crosshairPickEntity;
			if (entity != null) {
				list.add("");
				list.add(ChatFormatting.UNDERLINE + "Targeted Entity");
				list.add(String.valueOf(Registry.ENTITY_TYPE.getKey(entity.getType())));
			}

			return list;
		}
	}

	private String getPropertyValueString(Entry<Property<?>, Comparable<?>> entry) {
		Property<?> property = (Property<?>)entry.getKey();
		Comparable<?> comparable = (Comparable<?>)entry.getValue();
		String string = Util.getPropertyName(property, comparable);
		if (Boolean.TRUE.equals(comparable)) {
			string = ChatFormatting.GREEN + string;
		} else if (Boolean.FALSE.equals(comparable)) {
			string = ChatFormatting.RED + string;
		}

		return property.getName() + ": " + string;
	}

	private void drawChart(FrameTimer frameTimer, int i, int j, boolean bl) {
		RenderSystem.disableDepthTest();
		int k = frameTimer.getLogStart();
		int l = frameTimer.getLogEnd();
		long[] ls = frameTimer.getLog();
		int n = i;
		int o = Math.max(0, ls.length - j);
		int p = ls.length - o;
		int m = frameTimer.wrapIndex(k + o);
		long q = 0L;
		int r = Integer.MAX_VALUE;
		int s = Integer.MIN_VALUE;

		for (int t = 0; t < p; t++) {
			int u = (int)(ls[frameTimer.wrapIndex(m + t)] / 1000000L);
			r = Math.min(r, u);
			s = Math.max(s, u);
			q += (long)u;
		}

		int t = this.minecraft.getWindow().getGuiScaledHeight();
		fill(i, t - 60, i + p, t, -1873784752);

		while (m != l) {
			int u = frameTimer.scaleSampleTo(ls[m], bl ? 30 : 60, bl ? 60 : 20);
			int v = bl ? 100 : 60;
			int w = this.getSampleColor(Mth.clamp(u, 0, v), 0, v / 2, v);
			this.vLine(n, t, t - u, w);
			n++;
			m = frameTimer.wrapIndex(m + 1);
		}

		if (bl) {
			fill(i + 1, t - 30 + 1, i + 14, t - 30 + 10, -1873784752);
			this.font.draw("60 FPS", (float)(i + 2), (float)(t - 30 + 2), 14737632);
			this.hLine(i, i + p - 1, t - 30, -1);
			fill(i + 1, t - 60 + 1, i + 14, t - 60 + 10, -1873784752);
			this.font.draw("30 FPS", (float)(i + 2), (float)(t - 60 + 2), 14737632);
			this.hLine(i, i + p - 1, t - 60, -1);
		} else {
			fill(i + 1, t - 60 + 1, i + 14, t - 60 + 10, -1873784752);
			this.font.draw("20 TPS", (float)(i + 2), (float)(t - 60 + 2), 14737632);
			this.hLine(i, i + p - 1, t - 60, -1);
		}

		this.hLine(i, i + p - 1, t - 1, -1);
		this.vLine(i, t - 60, t, -1);
		this.vLine(i + p - 1, t - 60, t, -1);
		if (bl && this.minecraft.options.framerateLimit > 0 && this.minecraft.options.framerateLimit <= 250) {
			this.hLine(i, i + p - 1, t - 1 - (int)(1800.0 / (double)this.minecraft.options.framerateLimit), -16711681);
		}

		String string = r + " ms min";
		String string2 = q / (long)p + " ms avg";
		String string3 = s + " ms max";
		this.font.drawShadow(string, (float)(i + 2), (float)(t - 60 - 9), 14737632);
		this.font.drawShadow(string2, (float)(i + p / 2 - this.font.width(string2) / 2), (float)(t - 60 - 9), 14737632);
		this.font.drawShadow(string3, (float)(i + p - this.font.width(string3)), (float)(t - 60 - 9), 14737632);
		RenderSystem.enableDepthTest();
	}

	private int getSampleColor(int i, int j, int k, int l) {
		return i < k ? this.colorLerp(-16711936, -256, (float)i / (float)k) : this.colorLerp(-256, -65536, (float)(i - k) / (float)(l - k));
	}

	private int colorLerp(int i, int j, float f) {
		int k = i >> 24 & 0xFF;
		int l = i >> 16 & 0xFF;
		int m = i >> 8 & 0xFF;
		int n = i & 0xFF;
		int o = j >> 24 & 0xFF;
		int p = j >> 16 & 0xFF;
		int q = j >> 8 & 0xFF;
		int r = j & 0xFF;
		int s = Mth.clamp((int)Mth.lerp(f, (float)k, (float)o), 0, 255);
		int t = Mth.clamp((int)Mth.lerp(f, (float)l, (float)p), 0, 255);
		int u = Mth.clamp((int)Mth.lerp(f, (float)m, (float)q), 0, 255);
		int v = Mth.clamp((int)Mth.lerp(f, (float)n, (float)r), 0, 255);
		return s << 24 | t << 16 | u << 8 | v;
	}

	private static long bytesToMegabytes(long l) {
		return l / 1024L / 1024L;
	}
}
