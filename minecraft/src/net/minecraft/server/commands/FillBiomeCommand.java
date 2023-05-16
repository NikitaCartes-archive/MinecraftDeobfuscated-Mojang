package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.apache.commons.lang3.mutable.MutableInt;

public class FillBiomeCommand {
	public static final SimpleCommandExceptionType ERROR_NOT_LOADED = new SimpleCommandExceptionType(Component.translatable("argument.pos.unloaded"));
	private static final Dynamic2CommandExceptionType ERROR_VOLUME_TOO_LARGE = new Dynamic2CommandExceptionType(
		(object, object2) -> Component.translatable("commands.fillbiome.toobig", object, object2)
	);

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
		commandDispatcher.register(
			Commands.literal("fillbiome")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.argument("from", BlockPosArgument.blockPos())
						.then(
							Commands.argument("to", BlockPosArgument.blockPos())
								.then(
									Commands.argument("biome", ResourceArgument.resource(commandBuildContext, Registries.BIOME))
										.executes(
											commandContext -> fill(
													commandContext.getSource(),
													BlockPosArgument.getLoadedBlockPos(commandContext, "from"),
													BlockPosArgument.getLoadedBlockPos(commandContext, "to"),
													ResourceArgument.getResource(commandContext, "biome", Registries.BIOME),
													holder -> true
												)
										)
										.then(
											Commands.literal("replace")
												.then(
													Commands.argument("filter", ResourceOrTagArgument.resourceOrTag(commandBuildContext, Registries.BIOME))
														.executes(
															commandContext -> fill(
																	commandContext.getSource(),
																	BlockPosArgument.getLoadedBlockPos(commandContext, "from"),
																	BlockPosArgument.getLoadedBlockPos(commandContext, "to"),
																	ResourceArgument.getResource(commandContext, "biome", Registries.BIOME),
																	ResourceOrTagArgument.getResourceOrTag(commandContext, "filter", Registries.BIOME)::test
																)
														)
												)
										)
								)
						)
				)
		);
	}

	private static int quantize(int i) {
		return QuartPos.toBlock(QuartPos.fromBlock(i));
	}

	private static BlockPos quantize(BlockPos blockPos) {
		return new BlockPos(quantize(blockPos.getX()), quantize(blockPos.getY()), quantize(blockPos.getZ()));
	}

	private static BiomeResolver makeResolver(
		MutableInt mutableInt, ChunkAccess chunkAccess, BoundingBox boundingBox, Holder<Biome> holder, Predicate<Holder<Biome>> predicate
	) {
		return (i, j, k, sampler) -> {
			int l = QuartPos.toBlock(i);
			int m = QuartPos.toBlock(j);
			int n = QuartPos.toBlock(k);
			Holder<Biome> holder2 = chunkAccess.getNoiseBiome(i, j, k);
			if (boundingBox.isInside(l, m, n) && predicate.test(holder2)) {
				mutableInt.increment();
				return holder;
			} else {
				return holder2;
			}
		};
	}

	private static int fill(
		CommandSourceStack commandSourceStack, BlockPos blockPos, BlockPos blockPos2, Holder.Reference<Biome> reference, Predicate<Holder<Biome>> predicate
	) throws CommandSyntaxException {
		BlockPos blockPos3 = quantize(blockPos);
		BlockPos blockPos4 = quantize(blockPos2);
		BoundingBox boundingBox = BoundingBox.fromCorners(blockPos3, blockPos4);
		int i = boundingBox.getXSpan() * boundingBox.getYSpan() * boundingBox.getZSpan();
		int j = commandSourceStack.getLevel().getGameRules().getInt(GameRules.RULE_COMMAND_MODIFICATION_BLOCK_LIMIT);
		if (i > j) {
			throw ERROR_VOLUME_TOO_LARGE.create(j, i);
		} else {
			ServerLevel serverLevel = commandSourceStack.getLevel();
			List<ChunkAccess> list = new ArrayList();

			for (int k = SectionPos.blockToSectionCoord(boundingBox.minZ()); k <= SectionPos.blockToSectionCoord(boundingBox.maxZ()); k++) {
				for (int l = SectionPos.blockToSectionCoord(boundingBox.minX()); l <= SectionPos.blockToSectionCoord(boundingBox.maxX()); l++) {
					ChunkAccess chunkAccess = serverLevel.getChunk(l, k, ChunkStatus.FULL, false);
					if (chunkAccess == null) {
						throw ERROR_NOT_LOADED.create();
					}

					list.add(chunkAccess);
				}
			}

			MutableInt mutableInt = new MutableInt(0);

			for (ChunkAccess chunkAccess : list) {
				chunkAccess.fillBiomesFromNoise(
					makeResolver(mutableInt, chunkAccess, boundingBox, reference, predicate), serverLevel.getChunkSource().randomState().sampler()
				);
				chunkAccess.setUnsaved(true);
			}

			serverLevel.getChunkSource().chunkMap.resendBiomesForChunks(list);
			commandSourceStack.sendSuccess(
				() -> Component.translatable(
						"commands.fillbiome.success.count",
						mutableInt.getValue(),
						boundingBox.minX(),
						boundingBox.minY(),
						boundingBox.minZ(),
						boundingBox.maxX(),
						boundingBox.maxY(),
						boundingBox.maxZ()
					),
				true
			);
			return mutableInt.getValue();
		}
	}
}
