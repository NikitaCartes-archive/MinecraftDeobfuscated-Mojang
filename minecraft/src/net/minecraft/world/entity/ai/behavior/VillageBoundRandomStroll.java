package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class VillageBoundRandomStroll extends Behavior<PathfinderMob> {
	private final float speedModifier;
	private final int maxXyDist;
	private final int maxYDist;

	public VillageBoundRandomStroll(float f) {
		this(f, 10, 7);
	}

	public VillageBoundRandomStroll(float f, int i, int j) {
		super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
		this.speedModifier = f;
		this.maxXyDist = i;
		this.maxYDist = j;
	}

	protected void start(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
		BlockPos blockPos = pathfinderMob.blockPosition();
		if (serverLevel.isVillage(blockPos)) {
			this.setRandomPos(pathfinderMob);
		} else {
			SectionPos sectionPos = SectionPos.of(blockPos);
			SectionPos sectionPos2 = BehaviorUtils.findSectionClosestToVillage(serverLevel, sectionPos, 2);
			if (sectionPos2 != sectionPos) {
				this.setTargetedPos(pathfinderMob, sectionPos2);
			} else {
				this.setRandomPos(pathfinderMob);
			}
		}
	}

	private void setTargetedPos(PathfinderMob pathfinderMob, SectionPos sectionPos) {
		Optional<Vec3> optional = Optional.ofNullable(
			DefaultRandomPos.getPosTowards(pathfinderMob, this.maxXyDist, this.maxYDist, Vec3.atBottomCenterOf(sectionPos.center()), (float) (Math.PI / 2))
		);
		pathfinderMob.getBrain().setMemory(MemoryModuleType.WALK_TARGET, optional.map(vec3 -> new WalkTarget(vec3, this.speedModifier, 0)));
	}

	private void setRandomPos(PathfinderMob pathfinderMob) {
		Optional<Vec3> optional = Optional.ofNullable(LandRandomPos.getPos(pathfinderMob, this.maxXyDist, this.maxYDist));
		pathfinderMob.getBrain().setMemory(MemoryModuleType.WALK_TARGET, optional.map(vec3 -> new WalkTarget(vec3, this.speedModifier, 0)));
	}
}
