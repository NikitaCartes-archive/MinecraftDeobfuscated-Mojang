package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class StrollToPoi extends Behavior<PathfinderMob> {
	private final MemoryModuleType<GlobalPos> memoryType;
	private final int closeEnoughDist;
	private final int maxDistanceFromPoi;
	private final float speedModifier;
	private long nextOkStartTime;

	public StrollToPoi(MemoryModuleType<GlobalPos> memoryModuleType, float f, int i, int j) {
		super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED, memoryModuleType, MemoryStatus.VALUE_PRESENT));
		this.memoryType = memoryModuleType;
		this.speedModifier = f;
		this.closeEnoughDist = i;
		this.maxDistanceFromPoi = j;
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
		Optional<GlobalPos> optional = pathfinderMob.getBrain().getMemory(this.memoryType);
		return optional.isPresent()
			&& serverLevel.dimension() == ((GlobalPos)optional.get()).dimension()
			&& ((GlobalPos)optional.get()).pos().closerThan(pathfinderMob.position(), (double)this.maxDistanceFromPoi);
	}

	protected void start(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
		if (l > this.nextOkStartTime) {
			Brain<?> brain = pathfinderMob.getBrain();
			Optional<GlobalPos> optional = brain.getMemory(this.memoryType);
			optional.ifPresent(globalPos -> brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(globalPos.pos(), this.speedModifier, this.closeEnoughDist)));
			this.nextOkStartTime = l + 80L;
		}
	}
}
