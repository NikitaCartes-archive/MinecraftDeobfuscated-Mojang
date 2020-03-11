package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;

public class PiglinSpecificSensor extends Sensor<LivingEntity> {
	@Override
	public Set<MemoryModuleType<?>> requires() {
		return ImmutableSet.of(
			MemoryModuleType.VISIBLE_LIVING_ENTITIES,
			MemoryModuleType.NEAREST_VISIBLE_WITHER_SKELETON,
			MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD,
			MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM,
			MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLIN,
			MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN,
			MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS,
			MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT,
			MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT,
			MemoryModuleType.NEAREST_REPELLENT
		);
	}

	@Override
	protected void doTick(ServerLevel serverLevel, LivingEntity livingEntity) {
		Brain<?> brain = livingEntity.getBrain();
		brain.setMemory(MemoryModuleType.NEAREST_REPELLENT, findNearestRepellent(serverLevel, livingEntity));
		Optional<WitherSkeleton> optional = Optional.empty();
		Optional<Hoglin> optional2 = Optional.empty();
		Optional<Hoglin> optional3 = Optional.empty();
		Optional<Piglin> optional4 = Optional.empty();
		Optional<ZombifiedPiglin> optional5 = Optional.empty();
		Optional<Player> optional6 = Optional.empty();
		Optional<Player> optional7 = Optional.empty();
		int i = 0;
		List<Piglin> list = Lists.<Piglin>newArrayList();

		for (LivingEntity livingEntity2 : (List)brain.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).orElse(Lists.newArrayList())) {
			if (livingEntity2 instanceof Hoglin) {
				Hoglin hoglin = (Hoglin)livingEntity2;
				if (hoglin.isBaby() && !optional3.isPresent()) {
					optional3 = Optional.of(hoglin);
				} else if (hoglin.isAdult()) {
					i++;
					if (!optional2.isPresent()) {
						optional2 = Optional.of(hoglin);
					}
				}
			} else if (livingEntity2 instanceof Piglin) {
				Piglin piglin = (Piglin)livingEntity2;
				if (piglin.isBaby() && !optional4.isPresent()) {
					optional4 = Optional.of(piglin);
				} else if (piglin.isAdult()) {
					list.add(piglin);
				}
			} else if (livingEntity2 instanceof Player) {
				Player player = (Player)livingEntity2;
				if (!optional6.isPresent() && EntitySelector.ATTACK_ALLOWED.test(livingEntity2) && !PiglinAi.isWearingGold(player)) {
					optional6 = Optional.of(player);
				}

				if (!optional7.isPresent() && !player.isSpectator() && PiglinAi.isPlayerHoldingLovedItem(player)) {
					optional7 = Optional.of(player);
				}
			} else if (!optional.isPresent() && livingEntity2 instanceof WitherSkeleton) {
				optional = Optional.of((WitherSkeleton)livingEntity2);
			} else if (!optional5.isPresent() && livingEntity2 instanceof ZombifiedPiglin) {
				optional5 = Optional.of((ZombifiedPiglin)livingEntity2);
			}
		}

		brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_WITHER_SKELETON, optional);
		brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLIN, optional2);
		brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, optional3);
		brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_BABY_PIGLIN, optional4);
		brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED_PIGLIN, optional5);
		brain.setMemory(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, optional6);
		brain.setMemory(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, optional7);
		brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS, list);
		brain.setMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, list.size());
		brain.setMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, i);
	}

	private static Optional<BlockPos> findNearestRepellent(ServerLevel serverLevel, LivingEntity livingEntity) {
		return BlockPos.findClosestMatch(livingEntity.blockPosition(), 8, 4, blockPos -> serverLevel.getBlockState(blockPos).is(BlockTags.PIGLIN_REPELLENTS));
	}
}
