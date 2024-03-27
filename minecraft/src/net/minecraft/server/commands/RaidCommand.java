package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.raid.Raids;
import net.minecraft.world.phys.Vec3;

public class RaidCommand {
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
		commandDispatcher.register(
			Commands.literal("raid")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(3))
				.then(
					Commands.literal("start")
						.then(
							Commands.argument("omenlvl", IntegerArgumentType.integer(0))
								.executes(commandContext -> start(commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "omenlvl")))
						)
				)
				.then(Commands.literal("stop").executes(commandContext -> stop(commandContext.getSource())))
				.then(Commands.literal("check").executes(commandContext -> check(commandContext.getSource())))
				.then(
					Commands.literal("sound")
						.then(
							Commands.argument("type", ComponentArgument.textComponent(commandBuildContext))
								.executes(commandContext -> playSound(commandContext.getSource(), ComponentArgument.getComponent(commandContext, "type")))
						)
				)
				.then(Commands.literal("spawnleader").executes(commandContext -> spawnLeader(commandContext.getSource())))
				.then(
					Commands.literal("setomen")
						.then(
							Commands.argument("level", IntegerArgumentType.integer(0))
								.executes(commandContext -> setRaidOmenLevel(commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "level")))
						)
				)
				.then(Commands.literal("glow").executes(commandContext -> glow(commandContext.getSource())))
		);
	}

	private static int glow(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
		Raid raid = getRaid(commandSourceStack.getPlayerOrException());
		if (raid != null) {
			for (Raider raider : raid.getAllRaiders()) {
				raider.addEffect(new MobEffectInstance(MobEffects.GLOWING, 1000, 1));
			}
		}

		return 1;
	}

	private static int setRaidOmenLevel(CommandSourceStack commandSourceStack, int i) throws CommandSyntaxException {
		Raid raid = getRaid(commandSourceStack.getPlayerOrException());
		if (raid != null) {
			int j = raid.getMaxRaidOmenLevel();
			if (i > j) {
				commandSourceStack.sendFailure(Component.literal("Sorry, the max raid omen level you can set is " + j));
			} else {
				int k = raid.getRaidOmenLevel();
				raid.setRaidOmenLevel(i);
				commandSourceStack.sendSuccess(() -> Component.literal("Changed village's raid omen level from " + k + " to " + i), false);
			}
		} else {
			commandSourceStack.sendFailure(Component.literal("No raid found here"));
		}

		return 1;
	}

	private static int spawnLeader(CommandSourceStack commandSourceStack) {
		commandSourceStack.sendSuccess(() -> Component.literal("Spawned a raid captain"), false);
		Raider raider = EntityType.PILLAGER.create(commandSourceStack.getLevel());
		if (raider == null) {
			commandSourceStack.sendFailure(Component.literal("Pillager failed to spawn"));
			return 0;
		} else {
			raider.setPatrolLeader(true);
			raider.setItemSlot(EquipmentSlot.HEAD, Raid.getLeaderBannerInstance(commandSourceStack.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN)));
			raider.setPos(commandSourceStack.getPosition().x, commandSourceStack.getPosition().y, commandSourceStack.getPosition().z);
			raider.finalizeSpawn(
				commandSourceStack.getLevel(),
				commandSourceStack.getLevel().getCurrentDifficultyAt(BlockPos.containing(commandSourceStack.getPosition())),
				MobSpawnType.COMMAND,
				null
			);
			commandSourceStack.getLevel().addFreshEntityWithPassengers(raider);
			return 1;
		}
	}

	private static int playSound(CommandSourceStack commandSourceStack, @Nullable Component component) {
		if (component != null && component.getString().equals("local")) {
			ServerLevel serverLevel = commandSourceStack.getLevel();
			Vec3 vec3 = commandSourceStack.getPosition().add(5.0, 0.0, 0.0);
			serverLevel.playSeededSound(null, vec3.x, vec3.y, vec3.z, SoundEvents.RAID_HORN, SoundSource.NEUTRAL, 2.0F, 1.0F, serverLevel.random.nextLong());
		}

		return 1;
	}

	private static int start(CommandSourceStack commandSourceStack, int i) throws CommandSyntaxException {
		ServerPlayer serverPlayer = commandSourceStack.getPlayerOrException();
		BlockPos blockPos = serverPlayer.blockPosition();
		if (serverPlayer.serverLevel().isRaided(blockPos)) {
			commandSourceStack.sendFailure(Component.literal("Raid already started close by"));
			return -1;
		} else {
			Raids raids = serverPlayer.serverLevel().getRaids();
			Raid raid = raids.createOrExtendRaid(serverPlayer, serverPlayer.blockPosition());
			if (raid != null) {
				raid.setRaidOmenLevel(i);
				raids.setDirty();
				commandSourceStack.sendSuccess(() -> Component.literal("Created a raid in your local village"), false);
			} else {
				commandSourceStack.sendFailure(Component.literal("Failed to create a raid in your local village"));
			}

			return 1;
		}
	}

	private static int stop(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
		ServerPlayer serverPlayer = commandSourceStack.getPlayerOrException();
		BlockPos blockPos = serverPlayer.blockPosition();
		Raid raid = serverPlayer.serverLevel().getRaidAt(blockPos);
		if (raid != null) {
			raid.stop();
			commandSourceStack.sendSuccess(() -> Component.literal("Stopped raid"), false);
			return 1;
		} else {
			commandSourceStack.sendFailure(Component.literal("No raid here"));
			return -1;
		}
	}

	private static int check(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
		Raid raid = getRaid(commandSourceStack.getPlayerOrException());
		if (raid != null) {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("Found a started raid! ");
			commandSourceStack.sendSuccess(() -> Component.literal(stringBuilder.toString()), false);
			StringBuilder stringBuilder2 = new StringBuilder();
			stringBuilder2.append("Num groups spawned: ");
			stringBuilder2.append(raid.getGroupsSpawned());
			stringBuilder2.append(" Raid omen level: ");
			stringBuilder2.append(raid.getRaidOmenLevel());
			stringBuilder2.append(" Num mobs: ");
			stringBuilder2.append(raid.getTotalRaidersAlive());
			stringBuilder2.append(" Raid health: ");
			stringBuilder2.append(raid.getHealthOfLivingRaiders());
			stringBuilder2.append(" / ");
			stringBuilder2.append(raid.getTotalHealth());
			commandSourceStack.sendSuccess(() -> Component.literal(stringBuilder2.toString()), false);
			return 1;
		} else {
			commandSourceStack.sendFailure(Component.literal("Found no started raids"));
			return 0;
		}
	}

	@Nullable
	private static Raid getRaid(ServerPlayer serverPlayer) {
		return serverPlayer.serverLevel().getRaidAt(serverPlayer.blockPosition());
	}
}
