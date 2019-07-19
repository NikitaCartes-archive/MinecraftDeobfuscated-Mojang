package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec2;

public class WorldBorderCommand {
	private static final SimpleCommandExceptionType ERROR_SAME_CENTER = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.worldborder.center.failed")
	);
	private static final SimpleCommandExceptionType ERROR_SAME_SIZE = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.worldborder.set.failed.nochange")
	);
	private static final SimpleCommandExceptionType ERROR_TOO_SMALL = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.worldborder.set.failed.small.")
	);
	private static final SimpleCommandExceptionType ERROR_TOO_BIG = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.worldborder.set.failed.big.")
	);
	private static final SimpleCommandExceptionType ERROR_SAME_WARNING_TIME = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.worldborder.warning.time.failed")
	);
	private static final SimpleCommandExceptionType ERROR_SAME_WARNING_DISTANCE = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.worldborder.warning.distance.failed")
	);
	private static final SimpleCommandExceptionType ERROR_SAME_DAMAGE_BUFFER = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.worldborder.damage.buffer.failed")
	);
	private static final SimpleCommandExceptionType ERROR_SAME_DAMAGE_AMOUNT = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.worldborder.damage.amount.failed")
	);

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("worldborder")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.literal("add")
						.then(
							Commands.argument("distance", FloatArgumentType.floatArg(-6.0E7F, 6.0E7F))
								.executes(
									commandContext -> setSize(
											commandContext.getSource(),
											commandContext.getSource().getLevel().getWorldBorder().getSize() + (double)FloatArgumentType.getFloat(commandContext, "distance"),
											0L
										)
								)
								.then(
									Commands.argument("time", IntegerArgumentType.integer(0))
										.executes(
											commandContext -> setSize(
													commandContext.getSource(),
													commandContext.getSource().getLevel().getWorldBorder().getSize() + (double)FloatArgumentType.getFloat(commandContext, "distance"),
													commandContext.getSource().getLevel().getWorldBorder().getLerpRemainingTime()
														+ (long)IntegerArgumentType.getInteger(commandContext, "time") * 1000L
												)
										)
								)
						)
				)
				.then(
					Commands.literal("set")
						.then(
							Commands.argument("distance", FloatArgumentType.floatArg(-6.0E7F, 6.0E7F))
								.executes(commandContext -> setSize(commandContext.getSource(), (double)FloatArgumentType.getFloat(commandContext, "distance"), 0L))
								.then(
									Commands.argument("time", IntegerArgumentType.integer(0))
										.executes(
											commandContext -> setSize(
													commandContext.getSource(),
													(double)FloatArgumentType.getFloat(commandContext, "distance"),
													(long)IntegerArgumentType.getInteger(commandContext, "time") * 1000L
												)
										)
								)
						)
				)
				.then(
					Commands.literal("center")
						.then(
							Commands.argument("pos", Vec2Argument.vec2())
								.executes(commandContext -> setCenter(commandContext.getSource(), Vec2Argument.getVec2(commandContext, "pos")))
						)
				)
				.then(
					Commands.literal("damage")
						.then(
							Commands.literal("amount")
								.then(
									Commands.argument("damagePerBlock", FloatArgumentType.floatArg(0.0F))
										.executes(commandContext -> setDamageAmount(commandContext.getSource(), FloatArgumentType.getFloat(commandContext, "damagePerBlock")))
								)
						)
						.then(
							Commands.literal("buffer")
								.then(
									Commands.argument("distance", FloatArgumentType.floatArg(0.0F))
										.executes(commandContext -> setDamageBuffer(commandContext.getSource(), FloatArgumentType.getFloat(commandContext, "distance")))
								)
						)
				)
				.then(Commands.literal("get").executes(commandContext -> getSize(commandContext.getSource())))
				.then(
					Commands.literal("warning")
						.then(
							Commands.literal("distance")
								.then(
									Commands.argument("distance", IntegerArgumentType.integer(0))
										.executes(commandContext -> setWarningDistance(commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "distance")))
								)
						)
						.then(
							Commands.literal("time")
								.then(
									Commands.argument("time", IntegerArgumentType.integer(0))
										.executes(commandContext -> setWarningTime(commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "time")))
								)
						)
				)
		);
	}

	private static int setDamageBuffer(CommandSourceStack commandSourceStack, float f) throws CommandSyntaxException {
		WorldBorder worldBorder = commandSourceStack.getLevel().getWorldBorder();
		if (worldBorder.getDamageSafeZone() == (double)f) {
			throw ERROR_SAME_DAMAGE_BUFFER.create();
		} else {
			worldBorder.setDamageSafeZone((double)f);
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.worldborder.damage.buffer.success", String.format(Locale.ROOT, "%.2f", f)), true);
			return (int)f;
		}
	}

	private static int setDamageAmount(CommandSourceStack commandSourceStack, float f) throws CommandSyntaxException {
		WorldBorder worldBorder = commandSourceStack.getLevel().getWorldBorder();
		if (worldBorder.getDamagePerBlock() == (double)f) {
			throw ERROR_SAME_DAMAGE_AMOUNT.create();
		} else {
			worldBorder.setDamagePerBlock((double)f);
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.worldborder.damage.amount.success", String.format(Locale.ROOT, "%.2f", f)), true);
			return (int)f;
		}
	}

	private static int setWarningTime(CommandSourceStack commandSourceStack, int i) throws CommandSyntaxException {
		WorldBorder worldBorder = commandSourceStack.getLevel().getWorldBorder();
		if (worldBorder.getWarningTime() == i) {
			throw ERROR_SAME_WARNING_TIME.create();
		} else {
			worldBorder.setWarningTime(i);
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.worldborder.warning.time.success", i), true);
			return i;
		}
	}

	private static int setWarningDistance(CommandSourceStack commandSourceStack, int i) throws CommandSyntaxException {
		WorldBorder worldBorder = commandSourceStack.getLevel().getWorldBorder();
		if (worldBorder.getWarningBlocks() == i) {
			throw ERROR_SAME_WARNING_DISTANCE.create();
		} else {
			worldBorder.setWarningBlocks(i);
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.worldborder.warning.distance.success", i), true);
			return i;
		}
	}

	private static int getSize(CommandSourceStack commandSourceStack) {
		double d = commandSourceStack.getLevel().getWorldBorder().getSize();
		commandSourceStack.sendSuccess(new TranslatableComponent("commands.worldborder.get", String.format(Locale.ROOT, "%.0f", d)), false);
		return Mth.floor(d + 0.5);
	}

	private static int setCenter(CommandSourceStack commandSourceStack, Vec2 vec2) throws CommandSyntaxException {
		WorldBorder worldBorder = commandSourceStack.getLevel().getWorldBorder();
		if (worldBorder.getCenterX() == (double)vec2.x && worldBorder.getCenterZ() == (double)vec2.y) {
			throw ERROR_SAME_CENTER.create();
		} else {
			worldBorder.setCenter((double)vec2.x, (double)vec2.y);
			commandSourceStack.sendSuccess(
				new TranslatableComponent("commands.worldborder.center.success", String.format(Locale.ROOT, "%.2f", vec2.x), String.format("%.2f", vec2.y)), true
			);
			return 0;
		}
	}

	private static int setSize(CommandSourceStack commandSourceStack, double d, long l) throws CommandSyntaxException {
		WorldBorder worldBorder = commandSourceStack.getLevel().getWorldBorder();
		double e = worldBorder.getSize();
		if (e == d) {
			throw ERROR_SAME_SIZE.create();
		} else if (d < 1.0) {
			throw ERROR_TOO_SMALL.create();
		} else if (d > 6.0E7) {
			throw ERROR_TOO_BIG.create();
		} else {
			if (l > 0L) {
				worldBorder.lerpSizeBetween(e, d, l);
				if (d > e) {
					commandSourceStack.sendSuccess(
						new TranslatableComponent("commands.worldborder.set.grow", String.format(Locale.ROOT, "%.1f", d), Long.toString(l / 1000L)), true
					);
				} else {
					commandSourceStack.sendSuccess(
						new TranslatableComponent("commands.worldborder.set.shrink", String.format(Locale.ROOT, "%.1f", d), Long.toString(l / 1000L)), true
					);
				}
			} else {
				worldBorder.setSize(d);
				commandSourceStack.sendSuccess(new TranslatableComponent("commands.worldborder.set.immediate", String.format(Locale.ROOT, "%.1f", d)), true);
			}

			return (int)(d - e);
		}
	}
}
