package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.phys.Vec3;

public class StrollAroundPoi extends Behavior<PathfinderMob> {
	private final MemoryModuleType<GlobalPos> memoryType;
	private long nextOkStartTime;
	private final int maxDistanceFromPoi;

	public StrollAroundPoi(MemoryModuleType<GlobalPos> memoryModuleType, int i) {
		super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED, memoryModuleType, MemoryStatus.VALUE_PRESENT));
		this.memoryType = memoryModuleType;
		this.maxDistanceFromPoi = i;
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
		Optional<GlobalPos> optional = pathfinderMob.getBrain().getMemory(this.memoryType);
		return optional.isPresent()
			&& serverLevel.dimension() == ((GlobalPos)optional.get()).dimension()
			&& ((GlobalPos)optional.get()).pos().closerThan(pathfinderMob.position(), (double)this.maxDistanceFromPoi);
	}

	protected void start(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
		if (l > this.nextOkStartTime) {
			Optional<Vec3> optional = Optional.ofNullable(RandomPos.getLandPos(pathfinderMob, 8, 6));
			pathfinderMob.getBrain().setMemory(MemoryModuleType.WALK_TARGET, optional.map(vec3 -> new WalkTarget(vec3, 0.4F, 1)));
			this.nextOkStartTime = l + 180L;
		}
	}
}
