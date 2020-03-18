package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class InsideBrownianWalk extends Behavior<PathfinderMob> {
	private final float speedModifier;

	public InsideBrownianWalk(float f) {
		super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
		this.speedModifier = f;
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
		return !serverLevel.canSeeSky(pathfinderMob.blockPosition());
	}

	protected void start(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
		BlockPos blockPos = pathfinderMob.blockPosition();
		List<BlockPos> list = (List<BlockPos>)BlockPos.betweenClosedStream(blockPos.offset(-1, -1, -1), blockPos.offset(1, 1, 1))
			.map(BlockPos::immutable)
			.collect(Collectors.toList());
		Collections.shuffle(list);
		Optional<BlockPos> optional = list.stream()
			.filter(blockPosx -> !serverLevel.canSeeSky(blockPosx))
			.filter(blockPosx -> serverLevel.loadedAndEntityCanStandOn(blockPosx, pathfinderMob))
			.filter(blockPosx -> serverLevel.noCollision(pathfinderMob))
			.findFirst();
		optional.ifPresent(blockPosx -> pathfinderMob.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(blockPosx, this.speedModifier, 0)));
	}
}
