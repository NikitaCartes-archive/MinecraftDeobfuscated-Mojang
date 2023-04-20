package net.minecraft.server.commands;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.datafixers.util.Unit;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class ResetChunksCommand {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("resetchunks")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.executes(commandContext -> resetChunks(commandContext.getSource(), 0, true))
				.then(
					Commands.argument("range", IntegerArgumentType.integer(0, 5))
						.executes(commandContext -> resetChunks(commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "range"), true))
						.then(
							Commands.argument("skipOldChunks", BoolArgumentType.bool())
								.executes(
									commandContext -> resetChunks(
											commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "range"), BoolArgumentType.getBool(commandContext, "skipOldChunks")
										)
								)
						)
				)
		);
	}

	private static int resetChunks(CommandSourceStack commandSourceStack, int i, boolean bl) {
		ServerLevel serverLevel = commandSourceStack.getLevel();
		ServerChunkCache serverChunkCache = serverLevel.getChunkSource();
		serverChunkCache.chunkMap.debugReloadGenerator();
		Vec3 vec3 = commandSourceStack.getPosition();
		ChunkPos chunkPos = new ChunkPos(BlockPos.containing(vec3));
		int j = chunkPos.z - i;
		int k = chunkPos.z + i;
		int l = chunkPos.x - i;
		int m = chunkPos.x + i;

		for (int n = j; n <= k; n++) {
			for (int o = l; o <= m; o++) {
				ChunkPos chunkPos2 = new ChunkPos(o, n);
				LevelChunk levelChunk = serverChunkCache.getChunk(o, n, false);
				if (levelChunk != null && (!bl || !levelChunk.isOldNoiseGeneration())) {
					for (BlockPos blockPos : BlockPos.betweenClosed(
						chunkPos2.getMinBlockX(),
						serverLevel.getMinBuildHeight(),
						chunkPos2.getMinBlockZ(),
						chunkPos2.getMaxBlockX(),
						serverLevel.getMaxBuildHeight() - 1,
						chunkPos2.getMaxBlockZ()
					)) {
						serverLevel.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 16);
					}
				}
			}
		}

		ProcessorMailbox<Runnable> processorMailbox = ProcessorMailbox.create(Util.backgroundExecutor(), "worldgen-resetchunks");
		long p = System.currentTimeMillis();
		int q = (i * 2 + 1) * (i * 2 + 1);

		for (ChunkStatus chunkStatus : ImmutableList.of(
			ChunkStatus.BIOMES, ChunkStatus.NOISE, ChunkStatus.SURFACE, ChunkStatus.CARVERS, ChunkStatus.FEATURES, ChunkStatus.INITIALIZE_LIGHT
		)) {
			long r = System.currentTimeMillis();
			CompletableFuture<Unit> completableFuture = CompletableFuture.supplyAsync(() -> Unit.INSTANCE, processorMailbox::tell);

			for (int s = chunkPos.z - i; s <= chunkPos.z + i; s++) {
				for (int t = chunkPos.x - i; t <= chunkPos.x + i; t++) {
					ChunkPos chunkPos3 = new ChunkPos(t, s);
					LevelChunk levelChunk2 = serverChunkCache.getChunk(t, s, false);
					if (levelChunk2 != null && (!bl || !levelChunk2.isOldNoiseGeneration())) {
						List<ChunkAccess> list = Lists.<ChunkAccess>newArrayList();
						int u = Math.max(1, chunkStatus.getRange());

						for (int v = chunkPos3.z - u; v <= chunkPos3.z + u; v++) {
							for (int w = chunkPos3.x - u; w <= chunkPos3.x + u; w++) {
								ChunkAccess chunkAccess = serverChunkCache.getChunk(w, v, chunkStatus.getParent(), true);
								ChunkAccess chunkAccess2;
								if (chunkAccess instanceof ImposterProtoChunk) {
									chunkAccess2 = new ImposterProtoChunk(((ImposterProtoChunk)chunkAccess).getWrapped(), true);
								} else if (chunkAccess instanceof LevelChunk) {
									chunkAccess2 = new ImposterProtoChunk((LevelChunk)chunkAccess, true);
								} else {
									chunkAccess2 = chunkAccess;
								}

								list.add(chunkAccess2);
							}
						}

						completableFuture = completableFuture.thenComposeAsync(
							unit -> chunkStatus.generate(
										processorMailbox::tell,
										serverLevel,
										serverChunkCache.getGenerator(),
										serverLevel.getStructureManager(),
										serverChunkCache.getLightEngine(),
										chunkAccessx -> {
											throw new UnsupportedOperationException("Not creating full chunks here");
										},
										list
									)
									.thenApply(either -> {
										if (chunkStatus == ChunkStatus.NOISE) {
											either.left().ifPresent(chunkAccessx -> Heightmap.primeHeightmaps(chunkAccessx, ChunkStatus.POST_FEATURES));
										}

										return Unit.INSTANCE;
									}),
							processorMailbox::tell
						);
					}
				}
			}

			commandSourceStack.getServer().managedBlock(completableFuture::isDone);
			LOGGER.debug(chunkStatus.getName() + " took " + (System.currentTimeMillis() - r) + " ms");
		}

		long x = System.currentTimeMillis();

		for (int y = chunkPos.z - i; y <= chunkPos.z + i; y++) {
			for (int z = chunkPos.x - i; z <= chunkPos.x + i; z++) {
				ChunkPos chunkPos4 = new ChunkPos(z, y);
				LevelChunk levelChunk3 = serverChunkCache.getChunk(z, y, false);
				if (levelChunk3 != null && (!bl || !levelChunk3.isOldNoiseGeneration())) {
					for (BlockPos blockPos2 : BlockPos.betweenClosed(
						chunkPos4.getMinBlockX(),
						serverLevel.getMinBuildHeight(),
						chunkPos4.getMinBlockZ(),
						chunkPos4.getMaxBlockX(),
						serverLevel.getMaxBuildHeight() - 1,
						chunkPos4.getMaxBlockZ()
					)) {
						serverChunkCache.blockChanged(blockPos2);
					}
				}
			}
		}

		LOGGER.debug("blockChanged took " + (System.currentTimeMillis() - x) + " ms");
		long r = System.currentTimeMillis() - p;
		commandSourceStack.sendSuccess(
			Component.literal(String.format(Locale.ROOT, "%d chunks have been reset. This took %d ms for %d chunks, or %02f ms per chunk", q, r, q, (float)r / (float)q)),
			true
		);
		return 1;
	}
}
