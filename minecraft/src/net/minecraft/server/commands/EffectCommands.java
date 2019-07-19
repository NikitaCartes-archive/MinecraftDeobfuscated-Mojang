package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MobEffectArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class EffectCommands {
	private static final SimpleCommandExceptionType ERROR_GIVE_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.effect.give.failed"));
	private static final SimpleCommandExceptionType ERROR_CLEAR_EVERYTHING_FAILED = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.effect.clear.everything.failed")
	);
	private static final SimpleCommandExceptionType ERROR_CLEAR_SPECIFIC_FAILED = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.effect.clear.specific.failed")
	);

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("effect")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.literal("clear")
						.then(
							Commands.argument("targets", EntityArgument.entities())
								.executes(commandContext -> clearEffects(commandContext.getSource(), EntityArgument.getEntities(commandContext, "targets")))
								.then(
									Commands.argument("effect", MobEffectArgument.effect())
										.executes(
											commandContext -> clearEffect(
													commandContext.getSource(), EntityArgument.getEntities(commandContext, "targets"), MobEffectArgument.getEffect(commandContext, "effect")
												)
										)
								)
						)
				)
				.then(
					Commands.literal("give")
						.then(
							Commands.argument("targets", EntityArgument.entities())
								.then(
									Commands.argument("effect", MobEffectArgument.effect())
										.executes(
											commandContext -> giveEffect(
													commandContext.getSource(),
													EntityArgument.getEntities(commandContext, "targets"),
													MobEffectArgument.getEffect(commandContext, "effect"),
													null,
													0,
													true
												)
										)
										.then(
											Commands.argument("seconds", IntegerArgumentType.integer(1, 1000000))
												.executes(
													commandContext -> giveEffect(
															commandContext.getSource(),
															EntityArgument.getEntities(commandContext, "targets"),
															MobEffectArgument.getEffect(commandContext, "effect"),
															IntegerArgumentType.getInteger(commandContext, "seconds"),
															0,
															true
														)
												)
												.then(
													Commands.argument("amplifier", IntegerArgumentType.integer(0, 255))
														.executes(
															commandContext -> giveEffect(
																	commandContext.getSource(),
																	EntityArgument.getEntities(commandContext, "targets"),
																	MobEffectArgument.getEffect(commandContext, "effect"),
																	IntegerArgumentType.getInteger(commandContext, "seconds"),
																	IntegerArgumentType.getInteger(commandContext, "amplifier"),
																	true
																)
														)
														.then(
															Commands.argument("hideParticles", BoolArgumentType.bool())
																.executes(
																	commandContext -> giveEffect(
																			commandContext.getSource(),
																			EntityArgument.getEntities(commandContext, "targets"),
																			MobEffectArgument.getEffect(commandContext, "effect"),
																			IntegerArgumentType.getInteger(commandContext, "seconds"),
																			IntegerArgumentType.getInteger(commandContext, "amplifier"),
																			!BoolArgumentType.getBool(commandContext, "hideParticles")
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

	private static int giveEffect(
		CommandSourceStack commandSourceStack, Collection<? extends Entity> collection, MobEffect mobEffect, @Nullable Integer integer, int i, boolean bl
	) throws CommandSyntaxException {
		int j = 0;
		int k;
		if (integer != null) {
			if (mobEffect.isInstantenous()) {
				k = integer;
			} else {
				k = integer * 20;
			}
		} else if (mobEffect.isInstantenous()) {
			k = 1;
		} else {
			k = 600;
		}

		for (Entity entity : collection) {
			if (entity instanceof LivingEntity) {
				MobEffectInstance mobEffectInstance = new MobEffectInstance(mobEffect, k, i, false, bl);
				if (((LivingEntity)entity).addEffect(mobEffectInstance)) {
					j++;
				}
			}
		}

		if (j == 0) {
			throw ERROR_GIVE_FAILED.create();
		} else {
			if (collection.size() == 1) {
				commandSourceStack.sendSuccess(
					new TranslatableComponent(
						"commands.effect.give.success.single", mobEffect.getDisplayName(), ((Entity)collection.iterator().next()).getDisplayName(), k / 20
					),
					true
				);
			} else {
				commandSourceStack.sendSuccess(
					new TranslatableComponent("commands.effect.give.success.multiple", mobEffect.getDisplayName(), collection.size(), k / 20), true
				);
			}

			return j;
		}
	}

	private static int clearEffects(CommandSourceStack commandSourceStack, Collection<? extends Entity> collection) throws CommandSyntaxException {
		int i = 0;

		for (Entity entity : collection) {
			if (entity instanceof LivingEntity && ((LivingEntity)entity).removeAllEffects()) {
				i++;
			}
		}

		if (i == 0) {
			throw ERROR_CLEAR_EVERYTHING_FAILED.create();
		} else {
			if (collection.size() == 1) {
				commandSourceStack.sendSuccess(
					new TranslatableComponent("commands.effect.clear.everything.success.single", ((Entity)collection.iterator().next()).getDisplayName()), true
				);
			} else {
				commandSourceStack.sendSuccess(new TranslatableComponent("commands.effect.clear.everything.success.multiple", collection.size()), true);
			}

			return i;
		}
	}

	private static int clearEffect(CommandSourceStack commandSourceStack, Collection<? extends Entity> collection, MobEffect mobEffect) throws CommandSyntaxException {
		int i = 0;

		for (Entity entity : collection) {
			if (entity instanceof LivingEntity && ((LivingEntity)entity).removeEffect(mobEffect)) {
				i++;
			}
		}

		if (i == 0) {
			throw ERROR_CLEAR_SPECIFIC_FAILED.create();
		} else {
			if (collection.size() == 1) {
				commandSourceStack.sendSuccess(
					new TranslatableComponent(
						"commands.effect.clear.specific.success.single", mobEffect.getDisplayName(), ((Entity)collection.iterator().next()).getDisplayName()
					),
					true
				);
			} else {
				commandSourceStack.sendSuccess(
					new TranslatableComponent("commands.effect.clear.specific.success.multiple", mobEffect.getDisplayName(), collection.size()), true
				);
			}

			return i;
		}
	}
}
