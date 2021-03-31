package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class StrollAroundPoi extends Behavior<PathfinderMob> {
	private static final int MIN_TIME_BETWEEN_STROLLS = 180;
	private static final int STROLL_MAX_XZ_DIST = 8;
	private static final int STROLL_MAX_Y_DIST = 6;
	private final MemoryModuleType<GlobalPos> memoryType;
	private long nextOkStartTime;
	private final int maxDistanceFromPoi;
	private final float speedModifier;

	public StrollAroundPoi(MemoryModuleType<GlobalPos> memoryModuleType, float f, int i) {
		super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED, memoryModuleType, MemoryStatus.VALUE_PRESENT));
		this.memoryType = memoryModuleType;
		this.speedModifier = f;
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
			Optional<Vec3> optional = Optional.ofNullable(LandRandomPos.getPos(pathfinderMob, 8, 6));
			pathfinderMob.getBrain().setMemory(MemoryModuleType.WALK_TARGET, optional.map(vec3 -> new WalkTarget(vec3, this.speedModifier, 1)));
			this.nextOkStartTime = l + 180L;
		}
	}
}
