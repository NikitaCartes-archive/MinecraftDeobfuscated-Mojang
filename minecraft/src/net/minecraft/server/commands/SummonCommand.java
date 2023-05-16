package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SummonCommand {
	private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.summon.failed"));
	private static final SimpleCommandExceptionType ERROR_DUPLICATE_UUID = new SimpleCommandExceptionType(Component.translatable("commands.summon.failed.uuid"));
	private static final SimpleCommandExceptionType INVALID_POSITION = new SimpleCommandExceptionType(Component.translatable("commands.summon.invalidPosition"));

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
		commandDispatcher.register(
			Commands.literal("summon")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.argument("entity", ResourceArgument.resource(commandBuildContext, Registries.ENTITY_TYPE))
						.suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
						.executes(
							commandContext -> spawnEntity(
									commandContext.getSource(),
									ResourceArgument.getSummonableEntityType(commandContext, "entity"),
									commandContext.getSource().getPosition(),
									new CompoundTag(),
									true
								)
						)
						.then(
							Commands.argument("pos", Vec3Argument.vec3())
								.executes(
									commandContext -> spawnEntity(
											commandContext.getSource(),
											ResourceArgument.getSummonableEntityType(commandContext, "entity"),
											Vec3Argument.getVec3(commandContext, "pos"),
											new CompoundTag(),
											true
										)
								)
								.then(
									Commands.argument("nbt", CompoundTagArgument.compoundTag())
										.executes(
											commandContext -> spawnEntity(
													commandContext.getSource(),
													ResourceArgument.getSummonableEntityType(commandContext, "entity"),
													Vec3Argument.getVec3(commandContext, "pos"),
													CompoundTagArgument.getCompoundTag(commandContext, "nbt"),
													false
												)
										)
								)
						)
				)
		);
	}

	public static Entity createEntity(
		CommandSourceStack commandSourceStack, Holder.Reference<EntityType<?>> reference, Vec3 vec3, CompoundTag compoundTag, boolean bl
	) throws CommandSyntaxException {
		BlockPos blockPos = BlockPos.containing(vec3);
		if (!Level.isInSpawnableBounds(blockPos)) {
			throw INVALID_POSITION.create();
		} else {
			CompoundTag compoundTag2 = compoundTag.copy();
			compoundTag2.putString("id", reference.key().location().toString());
			ServerLevel serverLevel = commandSourceStack.getLevel();
			Entity entity = EntityType.loadEntityRecursive(compoundTag2, serverLevel, entityx -> {
				entityx.moveTo(vec3.x, vec3.y, vec3.z, entityx.getYRot(), entityx.getXRot());
				return entityx;
			});
			if (entity == null) {
				throw ERROR_FAILED.create();
			} else {
				if (bl && entity instanceof Mob) {
					((Mob)entity)
						.finalizeSpawn(
							commandSourceStack.getLevel(), commandSourceStack.getLevel().getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.COMMAND, null, null
						);
				}

				if (!serverLevel.tryAddFreshEntityWithPassengers(entity)) {
					throw ERROR_DUPLICATE_UUID.create();
				} else {
					return entity;
				}
			}
		}
	}

	private static int spawnEntity(
		CommandSourceStack commandSourceStack, Holder.Reference<EntityType<?>> reference, Vec3 vec3, CompoundTag compoundTag, boolean bl
	) throws CommandSyntaxException {
		Entity entity = createEntity(commandSourceStack, reference, vec3, compoundTag, bl);
		commandSourceStack.sendSuccess(() -> Component.translatable("commands.summon.success", entity.getDisplayName()), true);
		return 1;
	}
}
