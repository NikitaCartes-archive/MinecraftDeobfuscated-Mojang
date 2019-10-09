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
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.phys.Vec3;

public class VillageBoundRandomStroll extends Behavior<PathfinderMob> {
	private final float speed;
	private final int maxXyDist;
	private final int maxYDist;

	public VillageBoundRandomStroll(float f) {
		this(f, 10, 7);
	}

	public VillageBoundRandomStroll(float f, int i, int j) {
		super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
		this.speed = f;
		this.maxXyDist = i;
		this.maxYDist = j;
	}

	protected void start(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
		BlockPos blockPos = new BlockPos(pathfinderMob);
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
		Optional<Vec3> optional = Optional.ofNullable(RandomPos.getPosTowards(pathfinderMob, this.maxXyDist, this.maxYDist, new Vec3(sectionPos.center())));
		pathfinderMob.getBrain().setMemory(MemoryModuleType.WALK_TARGET, optional.map(vec3 -> new WalkTarget(vec3, this.speed, 0)));
	}

	private void setRandomPos(PathfinderMob pathfinderMob) {
		Optional<Vec3> optional = Optional.ofNullable(RandomPos.getLandPos(pathfinderMob, this.maxXyDist, this.maxYDist));
		pathfinderMob.getBrain().setMemory(MemoryModuleType.WALK_TARGET, optional.map(vec3 -> new WalkTarget(vec3, this.speed, 0)));
	}
}
