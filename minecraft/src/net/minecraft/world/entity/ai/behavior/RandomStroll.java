package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class RandomStroll extends Behavior<PathfinderMob> {
	private static final int MAX_XZ_DIST = 10;
	private static final int MAX_Y_DIST = 7;
	private final float speedModifier;
	protected final int maxHorizontalDistance;
	protected final int maxVerticalDistance;
	private final boolean mayStrollFromWater;

	public RandomStroll(float f) {
		this(f, true);
	}

	public RandomStroll(float f, boolean bl) {
		this(f, 10, 7, bl);
	}

	public RandomStroll(float f, int i, int j) {
		this(f, i, j, true);
	}

	public RandomStroll(float f, int i, int j, boolean bl) {
		super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
		this.speedModifier = f;
		this.maxHorizontalDistance = i;
		this.maxVerticalDistance = j;
		this.mayStrollFromWater = bl;
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
		return this.mayStrollFromWater || !pathfinderMob.isInWaterOrBubble();
	}

	protected void start(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
		Optional<Vec3> optional = Optional.ofNullable(this.getTargetPos(pathfinderMob));
		pathfinderMob.getBrain().setMemory(MemoryModuleType.WALK_TARGET, optional.map(vec3 -> new WalkTarget(vec3, this.speedModifier, 0)));
	}

	@Nullable
	protected Vec3 getTargetPos(PathfinderMob pathfinderMob) {
		return LandRandomPos.getPos(pathfinderMob, this.maxHorizontalDistance, this.maxVerticalDistance);
	}
}
