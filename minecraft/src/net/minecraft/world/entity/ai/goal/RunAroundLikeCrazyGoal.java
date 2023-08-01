package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class RunAroundLikeCrazyGoal extends Goal {
	private final AbstractHorse horse;
	private final double speedModifier;
	private double posX;
	private double posY;
	private double posZ;

	public RunAroundLikeCrazyGoal(AbstractHorse abstractHorse, double d) {
		this.horse = abstractHorse;
		this.speedModifier = d;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		if (!this.horse.isTamed() && this.horse.isVehicle()) {
			Vec3 vec3 = DefaultRandomPos.getPos(this.horse, 5, 4);
			if (vec3 == null) {
				return false;
			} else {
				this.posX = vec3.x;
				this.posY = vec3.y;
				this.posZ = vec3.z;
				return true;
			}
		} else {
			return false;
		}
	}

	@Override
	public void start() {
		this.horse.getNavigation().moveTo(this.posX, this.posY, this.posZ, this.speedModifier);
	}

	@Override
	public boolean canContinueToUse() {
		return !this.horse.isTamed() && !this.horse.getNavigation().isDone() && this.horse.isVehicle();
	}

	@Override
	public void tick() {
		if (!this.horse.isTamed() && this.horse.getRandom().nextInt(this.adjustedTickDelay(50)) == 0) {
			Entity entity = this.horse.getFirstPassenger();
			if (entity == null) {
				return;
			}

			if (entity instanceof Player player) {
				int i = this.horse.getTemper();
				int j = this.horse.getMaxTemper();
				if (j > 0 && this.horse.getRandom().nextInt(j) < i) {
					this.horse.tameWithName(player);
					return;
				}

				this.horse.modifyTemper(5);
			}

			this.horse.ejectPassengers();
			this.horse.makeMad();
			this.horse.level().broadcastEntityEvent(this.horse, (byte)6);
		}
	}
}
