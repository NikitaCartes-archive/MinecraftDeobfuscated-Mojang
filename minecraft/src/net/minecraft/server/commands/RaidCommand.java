package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
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

public class RaidCommand {
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
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
							Commands.argument("type", ComponentArgument.textComponent())
								.executes(commandContext -> playSound(commandContext.getSource(), ComponentArgument.getComponent(commandContext, "type")))
						)
				)
				.then(Commands.literal("spawnleader").executes(commandContext -> spawnLeader(commandContext.getSource())))
				.then(
					Commands.literal("setomen")
						.then(
							Commands.argument("level", IntegerArgumentType.integer(0))
								.executes(commandContext -> setBadOmenLevel(commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "level")))
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

	private static int setBadOmenLevel(CommandSourceStack commandSourceStack, int i) throws CommandSyntaxException {
		Raid raid = getRaid(commandSourceStack.getPlayerOrException());
		if (raid != null) {
			int j = raid.getMaxBadOmenLevel();
			if (i > j) {
				commandSourceStack.sendFailure(new TextComponent("Sorry, the max bad omen level you can set is " + j));
			} else {
				int k = raid.getBadOmenLevel();
				raid.setBadOmenLevel(i);
				commandSourceStack.sendSuccess(new TextComponent("Changed village's bad omen level from " + k + " to " + i), false);
			}
		} else {
			commandSourceStack.sendFailure(new TextComponent("No raid found here"));
		}

		return 1;
	}

	private static int spawnLeader(CommandSourceStack commandSourceStack) {
		commandSourceStack.sendSuccess(new TextComponent("Spawned a raid captain"), false);
		Raider raider = EntityType.PILLAGER.create(commandSourceStack.getLevel());
		raider.setPatrolLeader(true);
		raider.setItemSlot(EquipmentSlot.HEAD, Raid.getLeaderBannerInstance());
		raider.setPos(commandSourceStack.getPosition().x, commandSourceStack.getPosition().y, commandSourceStack.getPosition().z);
		raider.finalizeSpawn(
			commandSourceStack.getLevel(),
			commandSourceStack.getLevel().getCurrentDifficultyAt(new BlockPos(commandSourceStack.getPosition())),
			MobSpawnType.COMMAND,
			null,
			null
		);
		commandSourceStack.getLevel().addFreshEntityWithPassengers(raider);
		return 1;
	}

	private static int playSound(CommandSourceStack commandSourceStack, Component component) {
		if (component != null && component.getString().equals("local")) {
			commandSourceStack.getLevel()
				.playSound(null, new BlockPos(commandSourceStack.getPosition().add(5.0, 0.0, 0.0)), SoundEvents.RAID_HORN, SoundSource.NEUTRAL, 2.0F, 1.0F);
		}

		return 1;
	}

	private static int start(CommandSourceStack commandSourceStack, int i) throws CommandSyntaxException {
		ServerPlayer serverPlayer = commandSourceStack.getPlayerOrException();
		BlockPos blockPos = serverPlayer.blockPosition();
		if (serverPlayer.getLevel().isRaided(blockPos)) {
			commandSourceStack.sendFailure(new TextComponent("Raid already started close by"));
			return -1;
		} else {
			Raids raids = serverPlayer.getLevel().getRaids();
			Raid raid = raids.createOrExtendRaid(serverPlayer);
			if (raid != null) {
				raid.setBadOmenLevel(i);
				raids.setDirty();
				commandSourceStack.sendSuccess(new TextComponent("Created a raid in your local village"), false);
			} else {
				commandSourceStack.sendFailure(new TextComponent("Failed to create a raid in your local village"));
			}

			return 1;
		}
	}

	private static int stop(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
		ServerPlayer serverPlayer = commandSourceStack.getPlayerOrException();
		BlockPos blockPos = serverPlayer.blockPosition();
		Raid raid = serverPlayer.getLevel().getRaidAt(blockPos);
		if (raid != null) {
			raid.stop();
			commandSourceStack.sendSuccess(new TextComponent("Stopped raid"), false);
			return 1;
		} else {
			commandSourceStack.sendFailure(new TextComponent("No raid here"));
			return -1;
		}
	}

	private static int check(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
		Raid raid = getRaid(commandSourceStack.getPlayerOrException());
		if (raid != null) {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("Found a started raid! ");
			commandSourceStack.sendSuccess(new TextComponent(stringBuilder.toString()), false);
			stringBuilder = new StringBuilder();
			stringBuilder.append("Num groups spawned: ");
			stringBuilder.append(raid.getGroupsSpawned());
			stringBuilder.append(" Bad omen level: ");
			stringBuilder.append(raid.getBadOmenLevel());
			stringBuilder.append(" Num mobs: ");
			stringBuilder.append(raid.getTotalRaidersAlive());
			stringBuilder.append(" Raid health: ");
			stringBuilder.append(raid.getHealthOfLivingRaiders());
			stringBuilder.append(" / ");
			stringBuilder.append(raid.getTotalHealth());
			commandSourceStack.sendSuccess(new TextComponent(stringBuilder.toString()), false);
			return 1;
		} else {
			commandSourceStack.sendFailure(new TextComponent("Found no started raids"));
			return 0;
		}
	}

	@Nullable
	private static Raid getRaid(ServerPlayer serverPlayer) {
		return serverPlayer.getLevel().getRaidAt(serverPlayer.blockPosition());
	}
}