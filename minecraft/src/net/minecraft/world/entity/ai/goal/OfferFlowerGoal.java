package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class OfferFlowerGoal extends Goal {
	private static final TargetingConditions OFFER_TARGER_CONTEXT = TargetingConditions.forNonCombat().range(6.0);
	public static final int OFFER_TICKS = 400;
	private static final List<String> LOVED_PLAYERS = List.of("maria", "alva", "neo", "hidetaka", "miyazaki");
	private final IronGolem golem;
	private LivingEntity flowerReceiver;
	private int tick;

	public OfferFlowerGoal(IronGolem ironGolem) {
		this.golem = ironGolem;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		if (!this.golem.level.isDay()) {
			return false;
		} else if (this.golem.isJolly()) {
			if (this.golem.getRandom().nextInt(300) != 0) {
				return false;
			} else {
				AABB aABB = this.golem.getBoundingBox().inflate(6.0, 2.0, 6.0);
				List<Player> list = this.golem.level.getEntities(EntityType.PLAYER, aABB, entity -> {
					if (entity instanceof Player player && this.isLoved(player)) {
						return true;
					}

					return false;
				});
				if (list.isEmpty()) {
					return false;
				} else {
					this.flowerReceiver = (LivingEntity)list.get(0);
					return true;
				}
			}
		} else if (this.golem.getRandom().nextInt(8000) != 0) {
			return false;
		} else {
			this.flowerReceiver = this.golem
				.level
				.getNearestEntity(
					Villager.class,
					OFFER_TARGER_CONTEXT,
					this.golem,
					this.golem.getX(),
					this.golem.getY(),
					this.golem.getZ(),
					this.golem.getBoundingBox().inflate(6.0, 2.0, 6.0)
				);
			return this.flowerReceiver != null;
		}
	}

	private boolean isLoved(Player player) {
		return LOVED_PLAYERS.contains(player.getName().getString().toLowerCase(Locale.ROOT));
	}

	@Override
	public boolean canContinueToUse() {
		return this.tick > 0;
	}

	@Override
	public void start() {
		this.tick = this.adjustedTickDelay(400);
		this.golem.offerFlower(true);
	}

	@Override
	public void stop() {
		this.golem.offerFlower(false);
		this.flowerReceiver = null;
	}

	@Override
	public void tick() {
		this.golem.getLookControl().setLookAt(this.flowerReceiver, 30.0F, 30.0F);
		this.tick--;
	}
}
