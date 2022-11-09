package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.apache.commons.lang3.mutable.MutableLong;

public class TryFindLand {
	private static final int COOLDOWN_TICKS = 60;

	public static BehaviorControl<PathfinderMob> create(int i, float f) {
		MutableLong mutableLong = new MutableLong(0L);
		return BehaviorBuilder.create(
			instance -> instance.group(
						instance.absent(MemoryModuleType.ATTACK_TARGET), instance.absent(MemoryModuleType.WALK_TARGET), instance.registered(MemoryModuleType.LOOK_TARGET)
					)
					.apply(
						instance,
						(memoryAccessor, memoryAccessor2, memoryAccessor3) -> (serverLevel, pathfinderMob, l) -> {
								if (!serverLevel.getFluidState(pathfinderMob.blockPosition()).is(FluidTags.WATER)) {
									return false;
								} else if (l < mutableLong.getValue()) {
									mutableLong.setValue(l + 60L);
									return true;
								} else {
									BlockPos blockPos = pathfinderMob.blockPosition();
									BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
									CollisionContext collisionContext = CollisionContext.of(pathfinderMob);

									for (BlockPos blockPos2 : BlockPos.withinManhattan(blockPos, i, i, i)) {
										if (blockPos2.getX() != blockPos.getX() || blockPos2.getZ() != blockPos.getZ()) {
											BlockState blockState = serverLevel.getBlockState(blockPos2);
											BlockState blockState2 = serverLevel.getBlockState(mutableBlockPos.setWithOffset(blockPos2, Direction.DOWN));
											if (!blockState.is(Blocks.WATER)
												&& serverLevel.getFluidState(blockPos2).isEmpty()
												&& blockState.getCollisionShape(serverLevel, blockPos2, collisionContext).isEmpty()
												&& blockState2.isFaceSturdy(serverLevel, mutableBlockPos, Direction.UP)) {
												BlockPos blockPos3 = blockPos2.immutable();
												memoryAccessor3.set(new BlockPosTracker(blockPos3));
												memoryAccessor2.set(new WalkTarget(new BlockPosTracker(blockPos3), f, 1));
												break;
											}
										}
									}

									mutableLong.setValue(l + 60L);
									return true;
								}
							}
					)
		);
	}
}
