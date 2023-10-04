package net.minecraft.server.commands;

import com.google.common.base.Joiner;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class ForceLoadCommand {
	private static final int MAX_CHUNK_LIMIT = 256;
	private static final Dynamic2CommandExceptionType ERROR_TOO_MANY_CHUNKS = new Dynamic2CommandExceptionType(
		(object, object2) -> Component.translatableEscape("commands.forceload.toobig", object, object2)
	);
	private static final Dynamic2CommandExceptionType ERROR_NOT_TICKING = new Dynamic2CommandExceptionType(
		(object, object2) -> Component.translatableEscape("commands.forceload.query.failure", object, object2)
	);
	private static final SimpleCommandExceptionType ERROR_ALL_ADDED = new SimpleCommandExceptionType(Component.translatable("commands.forceload.added.failure"));
	private static final SimpleCommandExceptionType ERROR_NONE_REMOVED = new SimpleCommandExceptionType(
		Component.translatable("commands.forceload.removed.failure")
	);

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("forceload")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.literal("add")
						.then(
							Commands.argument("from", ColumnPosArgument.columnPos())
								.executes(
									commandContext -> changeForceLoad(
											commandContext.getSource(), ColumnPosArgument.getColumnPos(commandContext, "from"), ColumnPosArgument.getColumnPos(commandContext, "from"), true
										)
								)
								.then(
									Commands.argument("to", ColumnPosArgument.columnPos())
										.executes(
											commandContext -> changeForceLoad(
													commandContext.getSource(), ColumnPosArgument.getColumnPos(commandContext, "from"), ColumnPosArgument.getColumnPos(commandContext, "to"), true
												)
										)
								)
						)
				)
				.then(
					Commands.literal("remove")
						.then(
							Commands.argument("from", ColumnPosArgument.columnPos())
								.executes(
									commandContext -> changeForceLoad(
											commandContext.getSource(), ColumnPosArgument.getColumnPos(commandContext, "from"), ColumnPosArgument.getColumnPos(commandContext, "from"), false
										)
								)
								.then(
									Commands.argument("to", ColumnPosArgument.columnPos())
										.executes(
											commandContext -> changeForceLoad(
													commandContext.getSource(), ColumnPosArgument.getColumnPos(commandContext, "from"), ColumnPosArgument.getColumnPos(commandContext, "to"), false
												)
										)
								)
						)
						.then(Commands.literal("all").executes(commandContext -> removeAll(commandContext.getSource())))
				)
				.then(
					Commands.literal("query")
						.executes(commandContext -> listForceLoad(commandContext.getSource()))
						.then(
							Commands.argument("pos", ColumnPosArgument.columnPos())
								.executes(commandContext -> queryForceLoad(commandContext.getSource(), ColumnPosArgument.getColumnPos(commandContext, "pos")))
						)
				)
		);
	}

	private static int queryForceLoad(CommandSourceStack commandSourceStack, ColumnPos columnPos) throws CommandSyntaxException {
		ChunkPos chunkPos = columnPos.toChunkPos();
		ServerLevel serverLevel = commandSourceStack.getLevel();
		ResourceKey<Level> resourceKey = serverLevel.dimension();
		boolean bl = serverLevel.getForcedChunks().contains(chunkPos.toLong());
		if (bl) {
			commandSourceStack.sendSuccess(
				() -> Component.translatable("commands.forceload.query.success", Component.translationArg(chunkPos), Component.translationArg(resourceKey.location())),
				false
			);
			return 1;
		} else {
			throw ERROR_NOT_TICKING.create(chunkPos, resourceKey.location());
		}
	}

	private static int listForceLoad(CommandSourceStack commandSourceStack) {
		ServerLevel serverLevel = commandSourceStack.getLevel();
		ResourceKey<Level> resourceKey = serverLevel.dimension();
		LongSet longSet = serverLevel.getForcedChunks();
		int i = longSet.size();
		if (i > 0) {
			String string = Joiner.on(", ").join(longSet.stream().sorted().map(ChunkPos::new).map(ChunkPos::toString).iterator());
			if (i == 1) {
				commandSourceStack.sendSuccess(
					() -> Component.translatable("commands.forceload.list.single", Component.translationArg(resourceKey.location()), string), false
				);
			} else {
				commandSourceStack.sendSuccess(
					() -> Component.translatable("commands.forceload.list.multiple", i, Component.translationArg(resourceKey.location()), string), false
				);
			}
		} else {
			commandSourceStack.sendFailure(Component.translatable("commands.forceload.added.none", Component.translationArg(resourceKey.location())));
		}

		return i;
	}

	private static int removeAll(CommandSourceStack commandSourceStack) {
		ServerLevel serverLevel = commandSourceStack.getLevel();
		ResourceKey<Level> resourceKey = serverLevel.dimension();
		LongSet longSet = serverLevel.getForcedChunks();
		longSet.forEach(l -> serverLevel.setChunkForced(ChunkPos.getX(l), ChunkPos.getZ(l), false));
		commandSourceStack.sendSuccess(() -> Component.translatable("commands.forceload.removed.all", Component.translationArg(resourceKey.location())), true);
		return 0;
	}

	private static int changeForceLoad(CommandSourceStack commandSourceStack, ColumnPos columnPos, ColumnPos columnPos2, boolean bl) throws CommandSyntaxException {
		int i = Math.min(columnPos.x(), columnPos2.x());
		int j = Math.min(columnPos.z(), columnPos2.z());
		int k = Math.max(columnPos.x(), columnPos2.x());
		int l = Math.max(columnPos.z(), columnPos2.z());
		if (i >= -30000000 && j >= -30000000 && k < 30000000 && l < 30000000) {
			int m = SectionPos.blockToSectionCoord(i);
			int n = SectionPos.blockToSectionCoord(j);
			int o = SectionPos.blockToSectionCoord(k);
			int p = SectionPos.blockToSectionCoord(l);
			long q = ((long)(o - m) + 1L) * ((long)(p - n) + 1L);
			if (q > 256L) {
				throw ERROR_TOO_MANY_CHUNKS.create(256, q);
			} else {
				ServerLevel serverLevel = commandSourceStack.getLevel();
				ResourceKey<Level> resourceKey = serverLevel.dimension();
				ChunkPos chunkPos = null;
				int r = 0;

				for (int s = m; s <= o; s++) {
					for (int t = n; t <= p; t++) {
						boolean bl2 = serverLevel.setChunkForced(s, t, bl);
						if (bl2) {
							r++;
							if (chunkPos == null) {
								chunkPos = new ChunkPos(s, t);
							}
						}
					}
				}

				ChunkPos chunkPos2 = chunkPos;
				if (r == 0) {
					throw (bl ? ERROR_ALL_ADDED : ERROR_NONE_REMOVED).create();
				} else {
					if (r == 1) {
						commandSourceStack.sendSuccess(
							() -> Component.translatable(
									"commands.forceload." + (bl ? "added" : "removed") + ".single", Component.translationArg(chunkPos2), Component.translationArg(resourceKey.location())
								),
							true
						);
					} else {
						ChunkPos chunkPos3 = new ChunkPos(m, n);
						ChunkPos chunkPos4 = new ChunkPos(o, p);
						commandSourceStack.sendSuccess(
							() -> Component.translatable(
									"commands.forceload." + (bl ? "added" : "removed") + ".multiple",
									Component.translationArg(chunkPos2),
									Component.translationArg(resourceKey.location()),
									Component.translationArg(chunkPos3),
									Component.translationArg(chunkPos4)
								),
							true
						);
					}

					return r;
				}
			}
		} else {
			throw BlockPosArgument.ERROR_OUT_OF_WORLD.create();
		}
	}
}
