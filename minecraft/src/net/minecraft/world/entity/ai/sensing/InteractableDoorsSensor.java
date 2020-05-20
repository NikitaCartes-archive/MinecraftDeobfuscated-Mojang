package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.dimension.DimensionType;

public class InteractableDoorsSensor extends Sensor<LivingEntity> {
	@Override
	protected void doTick(ServerLevel serverLevel, LivingEntity livingEntity) {
		ResourceKey<DimensionType> resourceKey = serverLevel.dimension();
		BlockPos blockPos = livingEntity.blockPosition();
		List<GlobalPos> list = Lists.<GlobalPos>newArrayList();

		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				for (int k = -1; k <= 1; k++) {
					BlockPos blockPos2 = blockPos.offset(i, j, k);
					if (serverLevel.getBlockState(blockPos2).is(BlockTags.WOODEN_DOORS)) {
						list.add(GlobalPos.of(resourceKey, blockPos2));
					}
				}
			}
		}

		Brain<?> brain = livingEntity.getBrain();
		if (!list.isEmpty()) {
			brain.setMemory(MemoryModuleType.INTERACTABLE_DOORS, list);
		} else {
			brain.eraseMemory(MemoryModuleType.INTERACTABLE_DOORS);
		}
	}

	@Override
	public Set<MemoryModuleType<?>> requires() {
		return ImmutableSet.of(MemoryModuleType.INTERACTABLE_DOORS);
	}
}
