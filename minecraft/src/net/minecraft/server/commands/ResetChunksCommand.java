package net.minecraft.server.commands;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.datafixers.util.Unit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ResetChunksCommand {
	private static final Logger LOGGER = LogManager.getLogger();

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("resetchunks")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.executes(commandContext -> resetChunks(commandContext.getSource(), 0))
				.then(
					Commands.argument("range", IntegerArgumentType.integer(0, 5))
						.executes(commandContext -> resetChunks(commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "range")))
				)
		);
	}

	private static int resetChunks(CommandSourceStack commandSourceStack, int i) {
		ServerLevel serverLevel = commandSourceStack.getLevel();
		ServerChunkCache serverChunkCache = serverLevel.getChunkSource();
		serverChunkCache.chunkMap.debugReloadGenerator();
		Vec3 vec3 = commandSourceStack.getPosition();
		ChunkPos chunkPos = new ChunkPos(new BlockPos(vec3));

		for (int j = chunkPos.z - i; j <= chunkPos.z + i; j++) {
			for (int k = chunkPos.x - i; k <= chunkPos.x + i; k++) {
				ChunkPos chunkPos2 = new ChunkPos(k, j);

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

		ProcessorMailbox<Runnable> processorMailbox = ProcessorMailbox.create(Util.backgroundExecutor(), "worldgen-resetchunks");
		long l = System.currentTimeMillis();
		int m = (i * 2 + 1) * (i * 2 + 1);

		for (ChunkStatus chunkStatus : ImmutableList.of(
			ChunkStatus.BIOMES, ChunkStatus.NOISE, ChunkStatus.SURFACE, ChunkStatus.CARVERS, ChunkStatus.LIQUID_CARVERS, ChunkStatus.FEATURES
		)) {
			long n = System.currentTimeMillis();
			CompletableFuture<Unit> completableFuture = CompletableFuture.supplyAsync(() -> Unit.INSTANCE, processorMailbox::tell);

			for (int o = chunkPos.z - i; o <= chunkPos.z + i; o++) {
				for (int p = chunkPos.x - i; p <= chunkPos.x + i; p++) {
					ChunkPos chunkPos3 = new ChunkPos(p, o);
					List<ChunkAccess> list = Lists.<ChunkAccess>newArrayList();
					int q = Math.max(1, chunkStatus.getRange());

					for (int r = chunkPos3.z - q; r <= chunkPos3.z + q; r++) {
						for (int s = chunkPos3.x - q; s <= chunkPos3.x + q; s++) {
							ChunkAccess chunkAccess = serverChunkCache.getChunk(s, r, chunkStatus.getParent(), true);
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
									serverLevel.getChunkSource().getGenerator(),
									serverLevel.getStructureManager(),
									serverChunkCache.getLightEngine(),
									chunkAccessx -> {
										throw new UnsupportedOperationException("Not creating full chunks here");
									},
									list,
									true
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

			commandSourceStack.getServer().managedBlock(completableFuture::isDone);
			LOGGER.debug(chunkStatus.getName() + " took " + (System.currentTimeMillis() - n) + " ms");
		}

		long t = System.currentTimeMillis();

		for (int u = chunkPos.z - i; u <= chunkPos.z + i; u++) {
			for (int v = chunkPos.x - i; v <= chunkPos.x + i; v++) {
				ChunkPos chunkPos4 = new ChunkPos(v, u);

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

		LOGGER.debug("blockChanged took " + (System.currentTimeMillis() - t) + " ms");
		long n = System.currentTimeMillis() - l;
		commandSourceStack.sendSuccess(
			new TextComponent(String.format("%d chunks have been reset. This took %d ms for %d chunks, or %02f ms per chunk", m, n, m, (float)n / (float)m)), true
		);
		return 1;
	}
}
