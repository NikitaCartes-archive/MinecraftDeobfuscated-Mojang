package net.minecraft.world.entity.ai.goal;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.phys.Vec3;

public class MoveBackToVillage extends RandomStrollGoal {
	public MoveBackToVillage(PathfinderMob pathfinderMob, double d) {
		super(pathfinderMob, d, 10);
	}

	@Override
	public boolean canUse() {
		ServerLevel serverLevel = (ServerLevel)this.mob.level;
		BlockPos blockPos = new BlockPos(this.mob);
		return serverLevel.isVillage(blockPos) ? false : super.canUse();
	}

	@Nullable
	@Override
	protected Vec3 getPosition() {
		ServerLevel serverLevel = (ServerLevel)this.mob.level;
		BlockPos blockPos = new BlockPos(this.mob);
		SectionPos sectionPos = SectionPos.of(blockPos);
		SectionPos sectionPos2 = BehaviorUtils.findSectionClosestToVillage(serverLevel, sectionPos, 2);
		if (sectionPos2 != sectionPos) {
			BlockPos blockPos2 = sectionPos2.center();
			return RandomPos.getPosTowards(this.mob, 10, 7, new Vec3((double)blockPos2.getX(), (double)blockPos2.getY(), (double)blockPos2.getZ()));
		} else {
			return null;
		}
	}
}
