package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.apache.commons.lang3.mutable.MutableLong;

public class TryFindLandNearWater {
	public static BehaviorControl<PathfinderMob> create(int i, float f) {
		MutableLong mutableLong = new MutableLong(0L);
		return BehaviorBuilder.create(
			instance -> instance.group(
						instance.absent(MemoryModuleType.ATTACK_TARGET), instance.absent(MemoryModuleType.WALK_TARGET), instance.registered(MemoryModuleType.LOOK_TARGET)
					)
					.apply(
						instance,
						(memoryAccessor, memoryAccessor2, memoryAccessor3) -> (serverLevel, pathfinderMob, l) -> {
								if (serverLevel.getFluidState(pathfinderMob.blockPosition()).is(FluidTags.WATER)) {
									return false;
								} else if (l < mutableLong.getValue()) {
									mutableLong.setValue(l + 40L);
									return true;
								} else {
									CollisionContext collisionContext = CollisionContext.of(pathfinderMob);
									BlockPos blockPos = pathfinderMob.blockPosition();
									BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

									label45:
									for (BlockPos blockPos2 : BlockPos.withinManhattan(blockPos, i, i, i)) {
										if ((blockPos2.getX() != blockPos.getX() || blockPos2.getZ() != blockPos.getZ())
											&& serverLevel.getBlockState(blockPos2).getCollisionShape(serverLevel, blockPos2, collisionContext).isEmpty()
											&& !serverLevel.getBlockState(mutableBlockPos.setWithOffset(blockPos2, Direction.DOWN))
												.getCollisionShape(serverLevel, blockPos2, collisionContext)
												.isEmpty()) {
											for (Direction direction : Direction.Plane.HORIZONTAL) {
												mutableBlockPos.setWithOffset(blockPos2, direction);
												if (serverLevel.getBlockState(mutableBlockPos).isAir() && serverLevel.getBlockState(mutableBlockPos.move(Direction.DOWN)).is(Blocks.WATER)) {
													memoryAccessor3.set(new BlockPosTracker(blockPos2));
													memoryAccessor2.set(new WalkTarget(new BlockPosTracker(blockPos2), f, 0));
													break label45;
												}
											}
										}
									}

									mutableLong.setValue(l + 40L);
									return true;
								}
							}
					)
		);
	}
}
