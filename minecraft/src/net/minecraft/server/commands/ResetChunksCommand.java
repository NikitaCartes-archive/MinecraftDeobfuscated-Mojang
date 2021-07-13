package net.minecraft.server.commands;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
import net.minecraft.util.Mth;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.TerrainShaper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.RegenProtoChunk;
import net.minecraft.world.phys.Vec3;

public class ResetChunksCommand {
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("resetchunks")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.argument("radius", IntegerArgumentType.integer(0, 8))
						.executes(commandContext -> resetChunks(commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "radius")))
				)
		);
	}

	private static int resetChunks(CommandSourceStack commandSourceStack, int i) throws CommandSyntaxException {
		ServerLevel serverLevel = commandSourceStack.getLevel();
		ServerChunkCache serverChunkCache = serverLevel.getChunkSource();
		Vec3 vec3 = commandSourceStack.getPosition();
		TerrainShaper.init();
		ChunkPos chunkPos = new ChunkPos(Mth.floor(vec3.x() / 16.0), Mth.floor(vec3.z() / 16.0));

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

					for (int q = chunkPos3.z - 8; q <= chunkPos3.z + 8; q++) {
						for (int r = chunkPos3.x - 8; r <= chunkPos3.x + 8; r++) {
							ChunkAccess chunkAccess = serverChunkCache.getChunk(r, q, chunkStatus.getParent(), true);
							ChunkAccess chunkAccess2;
							if (chunkAccess instanceof ImposterProtoChunk) {
								chunkAccess2 = new RegenProtoChunk(((ImposterProtoChunk)chunkAccess).getWrapped());
							} else if (chunkAccess instanceof LevelChunk) {
								chunkAccess2 = new RegenProtoChunk((LevelChunk)chunkAccess);
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
								.thenApply(either -> Unit.INSTANCE),
						processorMailbox::tell
					);
				}
			}

			commandSourceStack.getServer().managedBlock(completableFuture::isDone);
			System.out.println(chunkStatus.getName() + " took " + (System.currentTimeMillis() - n) + " ms");
		}

		long s = System.currentTimeMillis();

		for (int t = chunkPos.z - i; t <= chunkPos.z + i; t++) {
			for (int u = chunkPos.x - i; u <= chunkPos.x + i; u++) {
				ChunkPos chunkPos4 = new ChunkPos(u, t);

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

		System.out.println("blockChanged took " + (System.currentTimeMillis() - s) + " ms");
		long n = System.currentTimeMillis() - l;
		commandSourceStack.sendSuccess(
			new TextComponent("Chunks have been reset. This took " + n + " ms for " + m + " chunks, or " + n / (long)m + " ms/chunk"), true
		);
		return 1;
	}
}
