package net.minecraft.server.commands;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic4CommandExceptionType;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.scores.Team;

public class SpreadPlayersCommand {
	private static final Dynamic4CommandExceptionType ERROR_FAILED_TO_SPREAD_TEAMS = new Dynamic4CommandExceptionType(
		(object, object2, object3, object4) -> new TranslatableComponent("commands.spreadplayers.failed.teams", object, object2, object3, object4)
	);
	private static final Dynamic4CommandExceptionType ERROR_FAILED_TO_SPREAD_ENTITIES = new Dynamic4CommandExceptionType(
		(object, object2, object3, object4) -> new TranslatableComponent("commands.spreadplayers.failed.entities", object, object2, object3, object4)
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
																	BoolArgumentType.getBool(commandContext, "respectTeams"),
																	EntityArgument.getEntities(commandContext, "targets")
																)
														)
												)
										)
								)
						)
				)
		);
	}

	private static int spreadPlayers(CommandSourceStack commandSourceStack, Vec2 vec2, float f, float g, boolean bl, Collection<? extends Entity> collection) throws CommandSyntaxException {
		Random random = new Random();
		double d = (double)(vec2.x - g);
		double e = (double)(vec2.y - g);
		double h = (double)(vec2.x + g);
		double i = (double)(vec2.y + g);
		SpreadPlayersCommand.Position[] positions = createInitialPositions(random, bl ? getNumberOfTeams(collection) : collection.size(), d, e, h, i);
		spreadPositions(vec2, (double)f, commandSourceStack.getLevel(), random, d, e, h, i, positions, bl);
		double j = setPlayerPositions(collection, commandSourceStack.getLevel(), positions, bl);
		commandSourceStack.sendSuccess(
			new TranslatableComponent(
				"commands.spreadplayers.success." + (bl ? "teams" : "entities"), positions.length, vec2.x, vec2.y, String.format(Locale.ROOT, "%.2f", j)
			),
			true
		);
		return positions.length;
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
		Vec2 vec2, double d, ServerLevel serverLevel, Random random, double e, double f, double g, double h, SpreadPlayersCommand.Position[] positions, boolean bl
	) throws CommandSyntaxException {
		boolean bl2 = true;
		double i = Float.MAX_VALUE;

		int j;
		for (j = 0; j < 10000 && bl2; j++) {
			bl2 = false;
			i = Float.MAX_VALUE;

			for (int k = 0; k < positions.length; k++) {
				SpreadPlayersCommand.Position position = positions[k];
				int l = 0;
				SpreadPlayersCommand.Position position2 = new SpreadPlayersCommand.Position();

				for (int m = 0; m < positions.length; m++) {
					if (k != m) {
						SpreadPlayersCommand.Position position3 = positions[m];
						double n = position.dist(position3);
						i = Math.min(n, i);
						if (n < d) {
							l++;
							position2.x = position2.x + (position3.x - position.x);
							position2.z = position2.z + (position3.z - position.z);
						}
					}
				}

				if (l > 0) {
					position2.x = position2.x / (double)l;
					position2.z = position2.z / (double)l;
					double o = (double)position2.getLength();
					if (o > 0.0) {
						position2.normalize();
						position.moveAway(position2);
					} else {
						position.randomize(random, e, f, g, h);
					}

					bl2 = true;
				}

				if (position.clamp(e, f, g, h)) {
					bl2 = true;
				}
			}

			if (!bl2) {
				for (SpreadPlayersCommand.Position position2 : positions) {
					if (!position2.isSafe(serverLevel)) {
						position2.randomize(random, e, f, g, h);
						bl2 = true;
					}
				}
			}
		}

		if (i == Float.MAX_VALUE) {
			i = 0.0;
		}

		if (j >= 10000) {
			if (bl) {
				throw ERROR_FAILED_TO_SPREAD_TEAMS.create(positions.length, vec2.x, vec2.y, String.format(Locale.ROOT, "%.2f", i));
			} else {
				throw ERROR_FAILED_TO_SPREAD_ENTITIES.create(positions.length, vec2.x, vec2.y, String.format(Locale.ROOT, "%.2f", i));
			}
		}
	}

	private static double setPlayerPositions(
		Collection<? extends Entity> collection, ServerLevel serverLevel, SpreadPlayersCommand.Position[] positions, boolean bl
	) {
		double d = 0.0;
		int i = 0;
		Map<Team, SpreadPlayersCommand.Position> map = Maps.<Team, SpreadPlayersCommand.Position>newHashMap();

		for (Entity entity : collection) {
			SpreadPlayersCommand.Position position;
			if (bl) {
				Team team = entity instanceof Player ? entity.getTeam() : null;
				if (!map.containsKey(team)) {
					map.put(team, positions[i++]);
				}

				position = (SpreadPlayersCommand.Position)map.get(team);
			} else {
				position = positions[i++];
			}

			entity.teleportToWithTicket((double)((float)Mth.floor(position.x) + 0.5F), (double)position.getSpawnY(serverLevel), (double)Mth.floor(position.z) + 0.5);
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

	private static SpreadPlayersCommand.Position[] createInitialPositions(Random random, int i, double d, double e, double f, double g) {
		SpreadPlayersCommand.Position[] positions = new SpreadPlayersCommand.Position[i];

		for (int j = 0; j < positions.length; j++) {
			SpreadPlayersCommand.Position position = new SpreadPlayersCommand.Position();
			position.randomize(random, d, e, f, g);
			positions[j] = position;
		}

		return positions;
	}

	static class Position {
		private double x;
		private double z;

		double dist(SpreadPlayersCommand.Position position) {
			double d = this.x - position.x;
			double e = this.z - position.z;
			return Math.sqrt(d * d + e * e);
		}

		void normalize() {
			double d = (double)this.getLength();
			this.x /= d;
			this.z /= d;
		}

		float getLength() {
			return Mth.sqrt(this.x * this.x + this.z * this.z);
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

		public int getSpawnY(BlockGetter blockGetter) {
			BlockPos blockPos = new BlockPos(this.x, 256.0, this.z);

			while (blockPos.getY() > 0) {
				blockPos = blockPos.below();
				if (!blockGetter.getBlockState(blockPos).isAir()) {
					return blockPos.getY() + 1;
				}
			}

			return 257;
		}

		public boolean isSafe(BlockGetter blockGetter) {
			BlockPos blockPos = new BlockPos(this.x, 256.0, this.z);

			while (blockPos.getY() > 0) {
				blockPos = blockPos.below();
				BlockState blockState = blockGetter.getBlockState(blockPos);
				if (!blockState.isAir()) {
					Material material = blockState.getMaterial();
					return !material.isLiquid() && material != Material.FIRE;
				}
			}

			return false;
		}

		public void randomize(Random random, double d, double e, double f, double g) {
			this.x = Mth.nextDouble(random, d, f);
			this.z = Mth.nextDouble(random, e, g);
		}
	}
}
