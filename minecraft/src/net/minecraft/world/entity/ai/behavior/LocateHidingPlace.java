package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;

public class LocateHidingPlace extends Behavior<LivingEntity> {
	private final float speedModifier;
	private final int radius;
	private final int closeEnoughDist;
	private Optional<BlockPos> currentPos = Optional.empty();

	public LocateHidingPlace(int i, float f, int j) {
		super(
			ImmutableMap.of(
				MemoryModuleType.WALK_TARGET,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.HOME,
				MemoryStatus.REGISTERED,
				MemoryModuleType.HIDING_PLACE,
				MemoryStatus.REGISTERED
			)
		);
		this.radius = i;
		this.speedModifier = f;
		this.closeEnoughDist = j;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel serverLevel, LivingEntity livingEntity) {
		Optional<BlockPos> optional = serverLevel.getPoiManager()
			.find(poiType -> poiType == PoiType.HOME, blockPos -> true, livingEntity.blockPosition(), this.closeEnoughDist + 1, PoiManager.Occupancy.ANY);
		if (optional.isPresent() && ((BlockPos)optional.get()).closerToCenterThan(livingEntity.position(), (double)this.closeEnoughDist)) {
			this.currentPos = optional;
		} else {
			this.currentPos = Optional.empty();
		}

		return true;
	}

	@Override
	protected void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
		Brain<?> brain = livingEntity.getBrain();
		Optional<BlockPos> optional = this.currentPos;
		if (!optional.isPresent()) {
			optional = serverLevel.getPoiManager()
				.getRandom(
					poiType -> poiType == PoiType.HOME, blockPos -> true, PoiManager.Occupancy.ANY, livingEntity.blockPosition(), this.radius, livingEntity.getRandom()
				);
			if (!optional.isPresent()) {
				Optional<GlobalPos> optional2 = brain.getMemory(MemoryModuleType.HOME);
				if (optional2.isPresent()) {
					optional = Optional.of(((GlobalPos)optional2.get()).pos());
				}
			}
		}

		if (optional.isPresent()) {
			brain.eraseMemory(MemoryModuleType.PATH);
			brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
			brain.eraseMemory(MemoryModuleType.BREED_TARGET);
			brain.eraseMemory(MemoryModuleType.INTERACTION_TARGET);
			brain.setMemory(MemoryModuleType.HIDING_PLACE, GlobalPos.of(serverLevel.dimension(), (BlockPos)optional.get()));
			if (!((BlockPos)optional.get()).closerToCenterThan(livingEntity.position(), (double)this.closeEnoughDist)) {
				brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget((BlockPos)optional.get(), this.speedModifier, this.closeEnoughDist));
			}
		}
	}
}
