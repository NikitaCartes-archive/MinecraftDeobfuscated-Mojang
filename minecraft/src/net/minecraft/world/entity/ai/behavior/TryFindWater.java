package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.mutable.MutableLong;

public class TryFindWater {
	public static BehaviorControl<PathfinderMob> create(int i, float f) {
		MutableLong mutableLong = new MutableLong(0L);
		return BehaviorBuilder.create(
			instance -> instance.group(
						instance.absent(MemoryModuleType.ATTACK_TARGET), instance.absent(MemoryModuleType.WALK_TARGET), instance.registered(MemoryModuleType.LOOK_TARGET)
					)
					.apply(instance, (memoryAccessor, memoryAccessor2, memoryAccessor3) -> (serverLevel, pathfinderMob, l) -> {
							if (serverLevel.getFluidState(pathfinderMob.blockPosition()).is(FluidTags.WATER)) {
								return false;
							} else if (l < mutableLong.getValue()) {
								mutableLong.setValue(l + 20L + 2L);
								return true;
							} else {
								BlockPos blockPos = null;
								BlockPos blockPos2 = null;
								BlockPos blockPos3 = pathfinderMob.blockPosition();

								for (BlockPos blockPos4 : BlockPos.withinManhattan(blockPos3, i, i, i)) {
									if (blockPos4.getX() != blockPos3.getX() || blockPos4.getZ() != blockPos3.getZ()) {
										BlockState blockState = pathfinderMob.level().getBlockState(blockPos4.above());
										BlockState blockState2 = pathfinderMob.level().getBlockState(blockPos4);
										if (blockState2.is(Blocks.WATER)) {
											if (blockState.isAir()) {
												blockPos = blockPos4.immutable();
												break;
											}

											if (blockPos2 == null && !blockPos4.closerToCenterThan(pathfinderMob.position(), 1.5)) {
												blockPos2 = blockPos4.immutable();
											}
										}
									}
								}

								if (blockPos == null) {
									blockPos = blockPos2;
								}

								if (blockPos != null) {
									memoryAccessor3.set(new BlockPosTracker(blockPos));
									memoryAccessor2.set(new WalkTarget(new BlockPosTracker(blockPos), f, 0));
								}

								mutableLong.setValue(l + 40L);
								return true;
							}
						})
		);
	}
}
