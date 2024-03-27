package net.minecraft.world.entity.ai.behavior;

import java.util.Collections;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class InsideBrownianWalk {
	public static BehaviorControl<PathfinderMob> create(float f) {
		return BehaviorBuilder.create(
			instance -> instance.group(instance.absent(MemoryModuleType.WALK_TARGET))
					.apply(
						instance,
						memoryAccessor -> (serverLevel, pathfinderMob, l) -> {
								if (serverLevel.canSeeSky(pathfinderMob.blockPosition())) {
									return false;
								} else {
									BlockPos blockPos = pathfinderMob.blockPosition();
									List<BlockPos> list = (List<BlockPos>)BlockPos.betweenClosedStream(blockPos.offset(-1, -1, -1), blockPos.offset(1, 1, 1))
										.map(BlockPos::immutable)
										.collect(Util.toMutableList());
									Collections.shuffle(list);
									list.stream()
										.filter(blockPosx -> !serverLevel.canSeeSky(blockPosx))
										.filter(blockPosx -> serverLevel.loadedAndEntityCanStandOn(blockPosx, pathfinderMob))
										.filter(blockPosx -> serverLevel.noCollision(pathfinderMob))
										.findFirst()
										.ifPresent(blockPosx -> memoryAccessor.set(new WalkTarget(blockPosx, f, 0)));
									return true;
								}
							}
					)
		);
	}
}
