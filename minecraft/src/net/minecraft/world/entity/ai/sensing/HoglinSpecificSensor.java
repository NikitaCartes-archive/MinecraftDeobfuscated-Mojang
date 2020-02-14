package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.BlockFinder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.level.block.Blocks;

public class HoglinSpecificSensor extends Sensor<Hoglin> {
	@Override
	public Set<MemoryModuleType<?>> requires() {
		return ImmutableSet.of(
			MemoryModuleType.VISIBLE_LIVING_ENTITIES,
			MemoryModuleType.NEAREST_WARPED_FUNGI,
			MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN,
			MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS,
			MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT,
			MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT
		);
	}

	protected void doTick(ServerLevel serverLevel, Hoglin hoglin) {
		Brain<?> brain = hoglin.getBrain();
		brain.setMemory(MemoryModuleType.NEAREST_WARPED_FUNGI, this.findNearestWarpedFungi(serverLevel, hoglin));
		Optional<Piglin> optional = Optional.empty();
		int i = 0;
		List<Hoglin> list = Lists.<Hoglin>newArrayList();

		for (LivingEntity livingEntity : (List)brain.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).orElse(Lists.newArrayList())) {
			if (livingEntity instanceof Piglin && ((Piglin)livingEntity).isAdult()) {
				i++;
			}

			if (!optional.isPresent() && livingEntity instanceof Piglin && !livingEntity.isBaby() && livingEntity.closerThan(hoglin, 15.0)) {
				optional = Optional.of((Piglin)livingEntity);
			}

			if (livingEntity instanceof Hoglin && !livingEntity.isBaby()) {
				list.add((Hoglin)livingEntity);
			}
		}

		brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, optional);
		brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS, list);
		brain.setMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, i);
		brain.setMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, list.size());
	}

	private Optional<BlockPos> findNearestWarpedFungi(ServerLevel serverLevel, Hoglin hoglin) {
		return BlockFinder.findClosestMatchingBlockPos(hoglin.getBlockPos(), 8, 4, blockPos -> serverLevel.getBlockState(blockPos).getBlock() == Blocks.WARPED_FUNGI);
	}
}
