package net.minecraft.server.commands;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic4CommandExceptionType;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.scores.Team;

public class SpreadPlayersCommand {
	private static final int MAX_ITERATION_COUNT = 10000;
	private static final Dynamic4CommandExceptionType ERROR_FAILED_TO_SPREAD_TEAMS = new Dynamic4CommandExceptionType(
		(object, object2, object3, object4) -> Component.translatableEscape("commands.spreadplayers.failed.teams", object, object2, object3, object4)
	);
	private static final Dynamic4CommandExceptionType ERROR_FAILED_TO_SPREAD_ENTITIES = new Dynamic4CommandExceptionType(
		(object, object2, object3, object4) -> Component.translatableEscape("commands.spreadplayers.failed.entities", object, object2, object3, object4)
	);
	private static final Dynamic2CommandExceptionType ERROR_INVALID_MAX_HEIGHT = new Dynamic2CommandExceptionType(
		(object, object2) -> Component.translatableEscape("commands.spreadplayers.failed.invalid.height", object, object2)
	);

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("spreadplayers")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.argument("center", Vec2Argument.vec2())
						.then(
							Commands.argument("spreadDistance", FloatArgumentType.floatArg(0.0F))
								.then(
									Commands.argument("maxRange", FloatArgumentType.floatArg(1.0F))
										.then(
											Commands.argument("respectTeams", BoolArgumentType.bool())
												.then(
													Commands.argument("targets", EntityArgument.entities())
														.executes(
															commandContext -> spreadPlayers(
																	commandContext.getSource(),
																	Vec2Argument.getVec2(commandContext, "center"),
																	FloatArgumentType.getFloat(commandContext, "spreadDistance"),
																	FloatArgumentType.getFloat(commandContext, "maxRange"),
																	commandContext.getSource().getLevel().getMaxY() + 1,
																	BoolArgumentType.getBool(commandContext, "respectTeams"),
																	EntityArgument.getEntities(commandContext, "targets")
																)
														)
												)
										)
										.then(
											Commands.literal("under")
												.then(
													Commands.argument("maxHeight", IntegerArgumentType.integer())
														.then(
															Commands.argument("respectTeams", BoolArgumentType.bool())
																.then(
																	Commands.argument("targets", EntityArgument.entities())
																		.executes(
																			commandContext -> spreadPlayers(
																					commandContext.getSource(),
																					Vec2Argument.getVec2(commandContext, "center"),
																					FloatArgumentType.getFloat(commandContext, "spreadDistance"),
																					FloatArgumentType.getFloat(commandContext, "maxRange"),
																					IntegerArgumentType.getInteger(commandContext, "maxHeight"),
																					BoolArgumentType.getBool(commandContext, "respectTeams"),
																					EntityArgument.getEntities(commandContext, "targets")
																				)
																		)
																)
														)
												)
										)
								)
						)
				)
		);
	}

	private static int spreadPlayers(
		CommandSourceStack commandSourceStack, Vec2 vec2, float f, float g, int i, boolean bl, Collection<? extends Entity> collection
	) throws CommandSyntaxException {
		ServerLevel serverLevel = commandSourceStack.getLevel();
		int j = serverLevel.getMinY();
		if (i < j) {
			throw ERROR_INVALID_MAX_HEIGHT.create(i, j);
		} else {
			RandomSource randomSource = RandomSource.create();
			double d = (double)(vec2.x - g);
			double e = (double)(vec2.y - g);
			double h = (double)(vec2.x + g);
			double k = (double)(vec2.y + g);
			SpreadPlayersCommand.Position[] positions = createInitialPositions(randomSource, bl ? getNumberOfTeams(collection) : collection.size(), d, e, h, k);
			spreadPositions(vec2, (double)f, serverLevel, randomSource, d, e, h, k, i, positions, bl);
			double l = setPlayerPositions(collection, serverLevel, positions, i, bl);
			commandSourceStack.sendSuccess(
				() -> Component.translatable(
						"commands.spreadplayers.success." + (bl ? "teams" : "entities"), positions.length, vec2.x, vec2.y, String.format(Locale.ROOT, "%.2f", l)
					),
				true
			);
			return positions.length;
		}
	}

	private static int getNumberOfTeams(Collection<? extends Entity> collection) {
		Set<Team> set = Sets.<Team>newHashSet();

		for (Entity entity : collection) {
			if (entity instanceof Player) {
				set.add(entity.getTeam());
			} else {
				set.add(null);
			}
		}

		return set.size();
	}

	private static void spreadPositions(
		Vec2 vec2,
		double d,
		ServerLevel serverLevel,
		RandomSource randomSource,
		double e,
		double f,
		double g,
		double h,
		int i,
		SpreadPlayersCommand.Position[] positions,
		boolean bl
	) throws CommandSyntaxException {
		boolean bl2 = true;
		double j = Float.MAX_VALUE;

		int k;
		for (k = 0; k < 10000 && bl2; k++) {
			bl2 = false;
			j = Float.MAX_VALUE;

			for (int l = 0; l < positions.length; l++) {
				SpreadPlayersCommand.Position position = positions[l];
				int m = 0;
				SpreadPlayersCommand.Position position2 = new SpreadPlayersCommand.Position();

				for (int n = 0; n < positions.length; n++) {
					if (l != n) {
						SpreadPlayersCommand.Position position3 = positions[n];
						double o = position.dist(position3);
						j = Math.min(o, j);
						if (o < d) {
							m++;
							position2.x = position2.x + (position3.x - position.x);
							position2.z = position2.z + (position3.z - position.z);
						}
					}
				}

				if (m > 0) {
					position2.x /= (double)m;
					position2.z /= (double)m;
					double p = position2.getLength();
					if (p > 0.0) {
						position2.normalize();
						position.moveAway(position2);
					} else {
						position.randomize(randomSource, e, f, g, h);
					}

					bl2 = true;
				}

				if (position.clamp(e, f, g, h)) {
					bl2 = true;
				}
			}

			if (!bl2) {
				for (SpreadPlayersCommand.Position position2 : positions) {
					if (!position2.isSafe(serverLevel, i)) {
						position2.randomize(randomSource, e, f, g, h);
						bl2 = true;
					}
				}
			}
		}

		if (j == Float.MAX_VALUE) {
			j = 0.0;
		}

		if (k >= 10000) {
			if (bl) {
				throw ERROR_FAILED_TO_SPREAD_TEAMS.create(positions.length, vec2.x, vec2.y, String.format(Locale.ROOT, "%.2f", j));
			} else {
				throw ERROR_FAILED_TO_SPREAD_ENTITIES.create(positions.length, vec2.x, vec2.y, String.format(Locale.ROOT, "%.2f", j));
			}
		}
	}

	private static double setPlayerPositions(
		Collection<? extends Entity> collection, ServerLevel serverLevel, SpreadPlayersCommand.Position[] positions, int i, boolean bl
	) {
		double d = 0.0;
		int j = 0;
		Map<Team, SpreadPlayersCommand.Position> map = Maps.<Team, SpreadPlayersCommand.Position>newHashMap();

		for (Entity entity : collection) {
			SpreadPlayersCommand.Position position;
			if (bl) {
				Team team = entity instanceof Player ? entity.getTeam() : null;
				if (!map.containsKey(team)) {
					map.put(team, positions[j++]);
				}

				position = (SpreadPlayersCommand.Position)map.get(team);
			} else {
				position = positions[j++];
			}

			entity.teleportTo(
				serverLevel,
				(double)Mth.floor(position.x) + 0.5,
				(double)position.getSpawnY(serverLevel, i),
				(double)Mth.floor(position.z) + 0.5,
				Set.of(),
				entity.getYRot(),
				entity.getXRot(),
				true
			);
			double e = Double.MAX_VALUE;

			for (SpreadPlayersCommand.Position position2 : positions) {
				if (position != position2) {
					double f = position.dist(position2);
					e = Math.min(f, e);
				}
			}

			d += e;
		}

		return collection.size() < 2 ? 0.0 : d / (double)collection.size();
	}

	private static SpreadPlayersCommand.Position[] createInitialPositions(RandomSource randomSource, int i, double d, double e, double f, double g) {
		SpreadPlayersCommand.Position[] positions = new SpreadPlayersCommand.Position[i];

		for (int j = 0; j < positions.length; j++) {
			SpreadPlayersCommand.Position position = new SpreadPlayersCommand.Position();
			position.randomize(randomSource, d, e, f, g);
			positions[j] = position;
		}

		return positions;
	}

	static class Position {
		double x;
		double z;

		double dist(SpreadPlayersCommand.Position position) {
			double d = this.x - position.x;
			double e = this.z - position.z;
			return Math.sqrt(d * d + e * e);
		}

		void normalize() {
			double d = this.getLength();
			this.x /= d;
			this.z /= d;
		}

		double getLength() {
			return Math.sqrt(this.x * this.x + this.z * this.z);
		}

		public void moveAway(SpreadPlayersCommand.Position position) {
			this.x = this.x - position.x;
			this.z = this.z - position.z;
		}

		public boolean clamp(double d, double e, double f, double g) {
			boolean bl = false;
			if (this.x < d) {
				this.x = d;
				bl = true;
			} else if (this.x > f) {
				this.x = f;
				bl = true;
			}

			if (this.z < e) {
				this.z = e;
				bl = true;
			} else if (this.z > g) {
				this.z = g;
				bl = true;
			}

			return bl;
		}

		public int getSpawnY(BlockGetter blockGetter, int i) {
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(this.x, (double)(i + 1), this.z);
			boolean bl = blockGetter.getBlockState(mutableBlockPos).isAir();
			mutableBlockPos.move(Direction.DOWN);
			boolean bl2 = blockGetter.getBlockState(mutableBlockPos).isAir();

			while (mutableBlockPos.getY() > blockGetter.getMinY()) {
				mutableBlockPos.move(Direction.DOWN);
				boolean bl3 = blockGetter.getBlockState(mutableBlockPos).isAir();
				if (!bl3 && bl2 && bl) {
					return mutableBlockPos.getY() + 1;
				}

				bl = bl2;
				bl2 = bl3;
			}

			return i + 1;
		}

		public boolean isSafe(BlockGetter blockGetter, int i) {
			BlockPos blockPos = BlockPos.containing(this.x, (double)(this.getSpawnY(blockGetter, i) - 1), this.z);
			BlockState blockState = blockGetter.getBlockState(blockPos);
			return blockPos.getY() < i && !blockState.liquid() && !blockState.is(BlockTags.FIRE);
		}

		public void randomize(RandomSource randomSource, double d, double e, double f, double g) {
			this.x = Mth.nextDouble(randomSource, d, f);
			this.z = Mth.nextDouble(randomSource, e, g);
		}
	}
}
