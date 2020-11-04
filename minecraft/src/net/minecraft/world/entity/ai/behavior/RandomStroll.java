package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class RandomStroll extends Behavior<PathfinderMob> {
	private final float speedModifier;
	private final int maxHorizontalDistance;
	private final int maxVerticalDistance;

	public RandomStroll(float f) {
		this(f, 10, 7);
	}

	public RandomStroll(float f, int i, int j) {
		super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
		this.speedModifier = f;
		this.maxHorizontalDistance = i;
		this.maxVerticalDistance = j;
	}

	protected void start(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
		Optional<Vec3> optional = Optional.ofNullable(LandRandomPos.getPos(pathfinderMob, this.maxHorizontalDistance, this.maxVerticalDistance));
		pathfinderMob.getBrain().setMemory(MemoryModuleType.WALK_TARGET, optional.map(vec3 -> new WalkTarget(vec3, this.speedModifier, 0)));
	}
}
