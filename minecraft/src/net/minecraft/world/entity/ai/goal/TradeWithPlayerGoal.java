package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;

public class TradeWithPlayerGoal extends Goal {
	private final AbstractVillager mob;

	public TradeWithPlayerGoal(AbstractVillager abstractVillager) {
		this.mob = abstractVillager;
		this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		if (!this.mob.isAlive()) {
			return false;
		} else if (this.mob.isInWater()) {
			return false;
		} else if (!this.mob.onGround()) {
			return false;
		} else if (this.mob.hurtMarked) {
			return false;
		} else {
			Player player = this.mob.getTradingPlayer();
			if (player == null) {
				return false;
			} else {
				return this.mob.distanceToSqr(player) > 16.0 ? false : player.containerMenu != null;
			}
		}
	}

	@Override
	public void start() {
		this.mob.getNavigation().stop();
	}

	@Override
	public void stop() {
		this.mob.setTradingPlayer(null);
	}
}
