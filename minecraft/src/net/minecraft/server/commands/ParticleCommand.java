package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class ParticleCommand {
	private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.particle.failed"));

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("particle")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.argument("name", ParticleArgument.particle())
						.executes(
							commandContext -> sendParticles(
									commandContext.getSource(),
									ParticleArgument.getParticle(commandContext, "name"),
									commandContext.getSource().getPosition(),
									Vec3.ZERO,
									0.0F,
									0,
									false,
									commandContext.getSource().getServer().getPlayerList().getPlayers()
								)
						)
						.then(
							Commands.argument("pos", Vec3Argument.vec3())
								.executes(
									commandContext -> sendParticles(
											commandContext.getSource(),
											ParticleArgument.getParticle(commandContext, "name"),
											Vec3Argument.getVec3(commandContext, "pos"),
											Vec3.ZERO,
											0.0F,
											0,
											false,
											commandContext.getSource().getServer().getPlayerList().getPlayers()
										)
								)
								.then(
									Commands.argument("delta", Vec3Argument.vec3(false))
										.then(
											Commands.argument("speed", FloatArgumentType.floatArg(0.0F))
												.then(
													Commands.argument("count", IntegerArgumentType.integer(0))
														.executes(
															commandContext -> sendParticles(
																	commandContext.getSource(),
																	ParticleArgument.getParticle(commandContext, "name"),
																	Vec3Argument.getVec3(commandContext, "pos"),
																	Vec3Argument.getVec3(commandContext, "delta"),
																	FloatArgumentType.getFloat(commandContext, "speed"),
																	IntegerArgumentType.getInteger(commandContext, "count"),
																	false,
																	commandContext.getSource().getServer().getPlayerList().getPlayers()
																)
														)
														.then(
															Commands.literal("force")
																.executes(
																	commandContext -> sendParticles(
																			commandContext.getSource(),
																			ParticleArgument.getParticle(commandContext, "name"),
																			Vec3Argument.getVec3(commandContext, "pos"),
																			Vec3Argument.getVec3(commandContext, "delta"),
																			FloatArgumentType.getFloat(commandContext, "speed"),
																			IntegerArgumentType.getInteger(commandContext, "count"),
																			true,
																			commandContext.getSource().getServer().getPlayerList().getPlayers()
																		)
																)
																.then(
																	Commands.argument("viewers", EntityArgument.players())
																		.executes(
																			commandContext -> sendParticles(
																					commandContext.getSource(),
																					ParticleArgument.getParticle(commandContext, "name"),
																					Vec3Argument.getVec3(commandContext, "pos"),
																					Vec3Argument.getVec3(commandContext, "delta"),
																					FloatArgumentType.getFloat(commandContext, "speed"),
																					IntegerArgumentType.getInteger(commandContext, "count"),
																					true,
																					EntityArgument.getPlayers(commandContext, "viewers")
																				)
																		)
																)
														)
														.then(
															Commands.literal("normal")
																.executes(
																	commandContext -> sendParticles(
																			commandContext.getSource(),
																			ParticleArgument.getParticle(commandContext, "name"),
																			Vec3Argument.getVec3(commandContext, "pos"),
																			Vec3Argument.getVec3(commandContext, "delta"),
																			FloatArgumentType.getFloat(commandContext, "speed"),
																			IntegerArgumentType.getInteger(commandContext, "count"),
																			false,
																			commandContext.getSource().getServer().getPlayerList().getPlayers()
																		)
																)
																.then(
																	Commands.argument("viewers", EntityArgument.players())
																		.executes(
																			commandContext -> sendParticles(
																					commandContext.getSource(),
																					ParticleArgument.getParticle(commandContext, "name"),
																					Vec3Argument.getVec3(commandContext, "pos"),
																					Vec3Argument.getVec3(commandContext, "delta"),
																					FloatArgumentType.getFloat(commandContext, "speed"),
																					IntegerArgumentType.getInteger(commandContext, "count"),
																					false,
																					EntityArgument.getPlayers(commandContext, "viewers")
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

	private static int sendParticles(
		CommandSourceStack commandSourceStack,
		ParticleOptions particleOptions,
		Vec3 vec3,
		Vec3 vec32,
		float f,
		int i,
		boolean bl,
		Collection<ServerPlayer> collection
	) throws CommandSyntaxException {
		int j = 0;

		for (ServerPlayer serverPlayer : collection) {
			if (commandSourceStack.getLevel().sendParticles(serverPlayer, particleOptions, bl, vec3.x, vec3.y, vec3.z, i, vec32.x, vec32.y, vec32.z, (double)f)) {
				j++;
			}
		}

		if (j == 0) {
			throw ERROR_FAILED.create();
		} else {
			commandSourceStack.sendSuccess(
				new TranslatableComponent("commands.particle.success", Registry.PARTICLE_TYPE.getKey(particleOptions.getType()).toString()), true
			);
			return j;
		}
	}
}
