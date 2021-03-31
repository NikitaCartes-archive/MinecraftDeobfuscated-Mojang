package net.minecraft.world.entity.ai.goal;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

public class MoveBackToVillageGoal extends RandomStrollGoal {
	private static final int MAX_XZ_DIST = 10;
	private static final int MAX_Y_DIST = 7;

	public MoveBackToVillageGoal(PathfinderMob pathfinderMob, double d, boolean bl) {
		super(pathfinderMob, d, 10, bl);
	}

	@Override
	public boolean canUse() {
		ServerLevel serverLevel = (ServerLevel)this.mob.level;
		BlockPos blockPos = this.mob.blockPosition();
		return serverLevel.isVillage(blockPos) ? false : super.canUse();
	}

	@Nullable
	@Override
	protected Vec3 getPosition() {
		ServerLevel serverLevel = (ServerLevel)this.mob.level;
		BlockPos blockPos = this.mob.blockPosition();
		SectionPos sectionPos = SectionPos.of(blockPos);
		SectionPos sectionPos2 = BehaviorUtils.findSectionClosestToVillage(serverLevel, sectionPos, 2);
		return sectionPos2 != sectionPos ? DefaultRandomPos.getPosTowards(this.mob, 10, 7, Vec3.atBottomCenterOf(sectionPos2.center()), (float) (Math.PI / 2)) : null;
	}
}
