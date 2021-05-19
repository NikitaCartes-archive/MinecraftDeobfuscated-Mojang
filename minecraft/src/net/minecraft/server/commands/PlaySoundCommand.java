package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundCustomSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public class PlaySoundCommand {
	private static final SimpleCommandExceptionType ERROR_TOO_FAR = new SimpleCommandExceptionType(new TranslatableComponent("commands.playsound.failed"));

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> requiredArgumentBuilder = Commands.argument("sound", ResourceLocationArgument.id())
			.suggests(SuggestionProviders.AVAILABLE_SOUNDS);

		for (SoundSource soundSource : SoundSource.values()) {
			requiredArgumentBuilder.then(source(soundSource));
		}

		commandDispatcher.register(Commands.literal("playsound").requires(commandSourceStack -> commandSourceStack.hasPermission(2)).then(requiredArgumentBuilder));
	}

	private static LiteralArgumentBuilder<CommandSourceStack> source(SoundSource soundSource) {
		return Commands.literal(soundSource.getName())
			.then(
				Commands.argument("targets", EntityArgument.players())
					.executes(
						commandContext -> playSound(
								commandContext.getSource(),
								EntityArgument.getPlayers(commandContext, "targets"),
								ResourceLocationArgument.getId(commandContext, "sound"),
								soundSource,
								commandContext.getSource().getPosition(),
								1.0F,
								1.0F,
								0.0F
							)
					)
					.then(
						Commands.argument("pos", Vec3Argument.vec3())
							.executes(
								commandContext -> playSound(
										commandContext.getSource(),
										EntityArgument.getPlayers(commandContext, "targets"),
										ResourceLocationArgument.getId(commandContext, "sound"),
										soundSource,
										Vec3Argument.getVec3(commandContext, "pos"),
										1.0F,
										1.0F,
										0.0F
									)
							)
							.then(
								Commands.argument("volume", FloatArgumentType.floatArg(0.0F))
									.executes(
										commandContext -> playSound(
												commandContext.getSource(),
												EntityArgument.getPlayers(commandContext, "targets"),
												ResourceLocationArgument.getId(commandContext, "sound"),
												soundSource,
												Vec3Argument.getVec3(commandContext, "pos"),
												commandContext.<Float>getArgument("volume", Float.class),
												1.0F,
												0.0F
											)
									)
									.then(
										Commands.argument("pitch", FloatArgumentType.floatArg(0.0F, 2.0F))
											.executes(
												commandContext -> playSound(
														commandContext.getSource(),
														EntityArgument.getPlayers(commandContext, "targets"),
														ResourceLocationArgument.getId(commandContext, "sound"),
														soundSource,
														Vec3Argument.getVec3(commandContext, "pos"),
														commandContext.<Float>getArgument("volume", Float.class),
														commandContext.<Float>getArgument("pitch", Float.class),
														0.0F
													)
											)
											.then(
												Commands.argument("minVolume", FloatArgumentType.floatArg(0.0F, 1.0F))
													.executes(
														commandContext -> playSound(
																commandContext.getSource(),
																EntityArgument.getPlayers(commandContext, "targets"),
																ResourceLocationArgument.getId(commandContext, "sound"),
																soundSource,
																Vec3Argument.getVec3(commandContext, "pos"),
																commandContext.<Float>getArgument("volume", Float.class),
																commandContext.<Float>getArgument("pitch", Float.class),
																commandContext.<Float>getArgument("minVolume", Float.class)
															)
													)
											)
									)
							)
					)
			);
	}

	private static int playSound(
		CommandSourceStack commandSourceStack,
		Collection<ServerPlayer> collection,
		ResourceLocation resourceLocation,
		SoundSource soundSource,
		Vec3 vec3,
		float f,
		float g,
		float h
	) throws CommandSyntaxException {
		double d = Math.pow(f > 1.0F ? (double)(f * 16.0F) : 16.0, 2.0);
		int i = 0;

		for (ServerPlayer serverPlayer : collection) {
			double e = vec3.x - serverPlayer.getX();
			double j = vec3.y - serverPlayer.getY();
			double k = vec3.z - serverPlayer.getZ();
			double l = e * e + j * j + k * k;
			Vec3 vec32 = vec3;
			float m = f;
			if (l > d) {
				if (h <= 0.0F) {
					continue;
				}

				double n = Math.sqrt(l);
				vec32 = new Vec3(serverPlayer.getX() + e / n * 2.0, serverPlayer.getY() + j / n * 2.0, serverPlayer.getZ() + k / n * 2.0);
				m = h;
			}

			serverPlayer.connection.send(new ClientboundCustomSoundPacket(resourceLocation, soundSource, vec32, m, g));
			i++;
		}

		if (i == 0) {
			throw ERROR_TOO_FAR.create();
		} else {
			if (collection.size() == 1) {
				commandSourceStack.sendSuccess(
					new TranslatableComponent("commands.playsound.success.single", resourceLocation, ((ServerPlayer)collection.iterator().next()).getDisplayName()), true
				);
			} else {
				commandSourceStack.sendSuccess(new TranslatableComponent("commands.playsound.success.multiple", resourceLocation, collection.size()), true);
			}

			return i;
		}
	}
}
