package net.minecraft.world.entity.ai.goal.target;

import java.util.EnumSet;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class DefendVillageTargetGoal extends TargetGoal {
	private final IronGolem golem;
	private LivingEntity potentialTarget;
	private final TargetingConditions attackTargeting = new TargetingConditions().range(64.0);

	public DefendVillageTargetGoal(IronGolem ironGolem) {
		super(ironGolem, false, true);
		this.golem = ironGolem;
		this.setFlags(EnumSet.of(Goal.Flag.TARGET));
	}

	@Override
	public boolean canUse() {
		AABB aABB = this.golem.getBoundingBox().inflate(10.0, 8.0, 10.0);
		List<LivingEntity> list = this.golem.level.getNearbyEntities(Villager.class, this.attackTargeting, this.golem, aABB);
		List<Player> list2 = this.golem.level.getNearbyPlayers(this.attackTargeting, this.golem, aABB);

		for (LivingEntity livingEntity : list) {
			Villager villager = (Villager)livingEntity;

			for (Player player : list2) {
				int i = villager.getPlayerReputation(player);
				if (i <= -100) {
					this.potentialTarget = player;
				}
			}
		}

		return this.potentialTarget == null
			? false
			: !(this.potentialTarget instanceof Player) || !this.potentialTarget.isSpectator() && !((Player)this.potentialTarget).isCreative();
	}

	@Override
	public void start() {
		this.golem.setTarget(this.potentialTarget);
		super.start();
	}
}
