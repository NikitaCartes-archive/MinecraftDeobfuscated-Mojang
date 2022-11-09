package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;

public class SleepInBed extends Behavior<LivingEntity> {
	public static final int COOLDOWN_AFTER_BEING_WOKEN = 100;
	private long nextOkStartTime;

	public SleepInBed() {
		super(ImmutableMap.of(MemoryModuleType.HOME, MemoryStatus.VALUE_PRESENT, MemoryModuleType.LAST_WOKEN, MemoryStatus.REGISTERED));
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel serverLevel, LivingEntity livingEntity) {
		if (livingEntity.isPassenger()) {
			return false;
		} else {
			Brain<?> brain = livingEntity.getBrain();
			GlobalPos globalPos = (GlobalPos)brain.getMemory(MemoryModuleType.HOME).get();
			if (serverLevel.dimension() != globalPos.dimension()) {
				return false;
			} else {
				Optional<Long> optional = brain.getMemory(MemoryModuleType.LAST_WOKEN);
				if (optional.isPresent()) {
					long l = serverLevel.getGameTime() - (Long)optional.get();
					if (l > 0L && l < 100L) {
						return false;
					}
				}

				BlockState blockState = serverLevel.getBlockState(globalPos.pos());
				return globalPos.pos().closerToCenterThan(livingEntity.position(), 2.0)
					&& blockState.is(BlockTags.BEDS)
					&& !(Boolean)blockState.getValue(BedBlock.OCCUPIED);
			}
		}
	}

	@Override
	protected boolean canStillUse(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
		Optional<GlobalPos> optional = livingEntity.getBrain().getMemory(MemoryModuleType.HOME);
		if (!optional.isPresent()) {
			return false;
		} else {
			BlockPos blockPos = ((GlobalPos)optional.get()).pos();
			return livingEntity.getBrain().isActive(Activity.REST)
				&& livingEntity.getY() > (double)blockPos.getY() + 0.4
				&& blockPos.closerToCenterThan(livingEntity.position(), 1.14);
		}
	}

	@Override
	protected void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
		if (l > this.nextOkStartTime) {
			Brain<?> brain = livingEntity.getBrain();
			if (brain.hasMemoryValue(MemoryModuleType.DOORS_TO_CLOSE)) {
				Set<GlobalPos> set = (Set<GlobalPos>)brain.getMemory(MemoryModuleType.DOORS_TO_CLOSE).get();
				Optional<List<LivingEntity>> optional;
				if (brain.hasMemoryValue(MemoryModuleType.NEAREST_LIVING_ENTITIES)) {
					optional = brain.getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES);
				} else {
					optional = Optional.empty();
				}

				InteractWithDoor.closeDoorsThatIHaveOpenedOrPassedThrough(serverLevel, livingEntity, null, null, set, optional);
			}

			livingEntity.startSleeping(((GlobalPos)livingEntity.getBrain().getMemory(MemoryModuleType.HOME).get()).pos());
		}
	}

	@Override
	protected boolean timedOut(long l) {
		return false;
	}

	@Override
	protected void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
		if (livingEntity.isSleeping()) {
			livingEntity.stopSleeping();
			this.nextOkStartTime = l + 40L;
		}
	}
}
