package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FleeSunGoal extends Goal {
	protected final PathfinderMob mob;
	private double wantedX;
	private double wantedY;
	private double wantedZ;
	private final double speedModifier;
	private final Level level;

	public FleeSunGoal(PathfinderMob pathfinderMob, double d) {
		this.mob = pathfinderMob;
		this.speedModifier = d;
		this.level = pathfinderMob.level;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		if (this.mob.getTarget() != null) {
			return false;
		} else if (!this.level.isDay()) {
			return false;
		} else if (!this.mob.isOnFire()) {
			return false;
		} else if (!this.level.canSeeSky(this.mob.blockPosition())) {
			return false;
		} else {
			return !this.mob.getItemBySlot(EquipmentSlot.HEAD).isEmpty() ? false : this.setWantedPos();
		}
	}

	protected boolean setWantedPos() {
		Vec3 vec3 = this.getHidePos();
		if (vec3 == null) {
			return false;
		} else {
			this.wantedX = vec3.x;
			this.wantedY = vec3.y;
			this.wantedZ = vec3.z;
			return true;
		}
	}

	@Override
	public boolean canContinueToUse() {
		return !this.mob.getNavigation().isDone();
	}

	@Override
	public void start() {
		this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
	}

	@Nullable
	protected Vec3 getHidePos() {
		RandomSource randomSource = this.mob.getRandom();
		BlockPos blockPos = this.mob.blockPosition();

		for (int i = 0; i < 10; i++) {
			BlockPos blockPos2 = blockPos.offset(randomSource.nextInt(20) - 10, randomSource.nextInt(6) - 3, randomSource.nextInt(20) - 10);
			if (!this.level.canSeeSky(blockPos2) && this.mob.getWalkTargetValue(blockPos2) < 0.0F) {
				return Vec3.atBottomCenterOf(blockPos2);
			}
		}

		return null;
	}
}
