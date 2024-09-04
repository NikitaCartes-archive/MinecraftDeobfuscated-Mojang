package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

public class PiglinSpecificSensor extends Sensor<LivingEntity> {
	@Override
	public Set<MemoryModuleType<?>> requires() {
		return ImmutableSet.of(
			MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
			MemoryModuleType.NEAREST_LIVING_ENTITIES,
			MemoryModuleType.NEAREST_VISIBLE_NEMESIS,
			MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD,
			MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM,
			MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN,
			MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN,
			MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS,
			MemoryModuleType.NEARBY_ADULT_PIGLINS,
			MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT,
			MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT,
			MemoryModuleType.NEAREST_REPELLENT
		);
	}

	@Override
	protected void doTick(ServerLevel serverLevel, LivingEntity livingEntity) {
		Brain<?> brain = livingEntity.getBrain();
		brain.setMemory(MemoryModuleType.NEAREST_REPELLENT, findNearestRepellent(serverLevel, livingEntity));
		Optional<Mob> optional = Optional.empty();
		Optional<Hoglin> optional2 = Optional.empty();
		Optional<Hoglin> optional3 = Optional.empty();
		Optional<Piglin> optional4 = Optional.empty();
		Optional<LivingEntity> optional5 = Optional.empty();
		Optional<Player> optional6 = Optional.empty();
		Optional<Player> optional7 = Optional.empty();
		int i = 0;
		List<AbstractPiglin> list = Lists.<AbstractPiglin>newArrayList();
		List<AbstractPiglin> list2 = Lists.<AbstractPiglin>newArrayList();
		NearestVisibleLivingEntities nearestVisibleLivingEntities = (NearestVisibleLivingEntities)brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
			.orElse(NearestVisibleLivingEntities.empty());

		for (LivingEntity livingEntity2 : nearestVisibleLivingEntities.findAll(livingEntityx -> true)) {
			if (livingEntity2 instanceof Hoglin) {
				Hoglin hoglin = (Hoglin)livingEntity2;
				if (hoglin.isBaby() && optional3.isEmpty()) {
					optional3 = Optional.of(hoglin);
				} else if (hoglin.isAdult()) {
					i++;
					if (optional2.isEmpty() && hoglin.canBeHunted()) {
						optional2 = Optional.of(hoglin);
					}
				}
			} else if (livingEntity2 instanceof PiglinBrute piglinBrute) {
				list.add(piglinBrute);
			} else if (livingEntity2 instanceof Piglin) {
				Piglin piglin = (Piglin)livingEntity2;
				if (piglin.isBaby() && optional4.isEmpty()) {
					optional4 = Optional.of(piglin);
				} else if (piglin.isAdult()) {
					list.add(piglin);
				}
			} else if (livingEntity2 instanceof Player) {
				Player player = (Player)livingEntity2;
				if (optional6.isEmpty() && !PiglinAi.isWearingSafeArmor(player) && livingEntity.canAttack(livingEntity2)) {
					optional6 = Optional.of(player);
				}

				if (optional7.isEmpty() && !player.isSpectator() && PiglinAi.isPlayerHoldingLovedItem(player)) {
					optional7 = Optional.of(player);
				}
			} else if (!optional.isEmpty() || !(livingEntity2 instanceof WitherSkeleton) && !(livingEntity2 instanceof WitherBoss)) {
				if (optional5.isEmpty() && PiglinAi.isZombified(livingEntity2.getType())) {
					optional5 = Optional.of(livingEntity2);
				}
			} else {
				optional = Optional.of((Mob)livingEntity2);
			}
		}

		for (LivingEntity livingEntity3 : (List)brain.getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).orElse(ImmutableList.of())) {
			if (livingEntity3 instanceof AbstractPiglin) {
				AbstractPiglin abstractPiglin = (AbstractPiglin)livingEntity3;
				if (abstractPiglin.isAdult()) {
					list2.add(abstractPiglin);
				}
			}
		}

		brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS, optional);
		brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, optional2);
		brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, optional3);
		brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, optional5);
		brain.setMemory(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, optional6);
		brain.setMemory(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, optional7);
		brain.setMemory(MemoryModuleType.NEARBY_ADULT_PIGLINS, list2);
		brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS, list);
		brain.setMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, list.size());
		brain.setMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, i);
	}

	private static Optional<BlockPos> findNearestRepellent(ServerLevel serverLevel, LivingEntity livingEntity) {
		return BlockPos.findClosestMatch(livingEntity.blockPosition(), 8, 4, blockPos -> isValidRepellent(serverLevel, blockPos));
	}

	private static boolean isValidRepellent(ServerLevel serverLevel, BlockPos blockPos) {
		BlockState blockState = serverLevel.getBlockState(blockPos);
		boolean bl = blockState.is(BlockTags.PIGLIN_REPELLENTS);
		return bl && blockState.is(Blocks.SOUL_CAMPFIRE) ? CampfireBlock.isLitCampfire(blockState) : bl;
	}
}
