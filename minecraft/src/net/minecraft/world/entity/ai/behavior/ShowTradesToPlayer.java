package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.CarryableTrade;
import net.minecraft.world.item.trading.MerchantOffer;

public class ShowTradesToPlayer extends Behavior<Villager> {
	private static final int MAX_LOOK_TIME = 900;
	private static final int STARTING_LOOK_TIME = 40;
	public static final int CYCLE_INTERVAL_TICKS = 100;
	@Nullable
	private CarryableTrade playerTradeProposition;
	private final List<MerchantOffer> availableOffers = Lists.<MerchantOffer>newArrayList();
	private int cycleCounter;
	private int displayIndex;
	private int lookTime;

	public ShowTradesToPlayer(int i, int j) {
		super(ImmutableMap.of(MemoryModuleType.NEAREST_PLAYERS, MemoryStatus.VALUE_PRESENT), i, j);
	}

	public boolean checkExtraStartConditions(ServerLevel serverLevel, Villager villager) {
		Brain<?> brain = villager.getBrain();
		if (!brain.getMemory(MemoryModuleType.NEAREST_PLAYERS).isPresent()) {
			return false;
		} else if (villager.isAlive() && !villager.isBaby()) {
			for (Player player : (List)brain.getMemory(MemoryModuleType.NEAREST_PLAYERS).get()) {
				if (player.isAlive() && player.position().closerThan(villager.position(), 6.0)) {
					CarryableTrade carryableTrade = player.getTradeProposition();
					if (carryableTrade != null && this.hasMatchingOffers(villager, carryableTrade)) {
						villager.getBrain().setMemory(MemoryModuleType.INTERACTION_TARGET, player);
						return true;
					}
				}
			}

			return false;
		} else {
			return false;
		}
	}

	public boolean canStillUse(ServerLevel serverLevel, Villager villager, long l) {
		return this.checkExtraStartConditions(serverLevel, villager)
			&& this.lookTime > 0
			&& villager.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).isPresent();
	}

	public void start(ServerLevel serverLevel, Villager villager, long l) {
		super.start(serverLevel, villager, l);
		this.lookAtTarget(villager);
		this.cycleCounter = 0;
		this.displayIndex = 0;
		this.lookTime = 40;
	}

	public void tick(ServerLevel serverLevel, Villager villager, long l) {
		LivingEntity livingEntity = this.lookAtTarget(villager);
		this.findItemsToDisplay(livingEntity, villager);
		if (!this.availableOffers.isEmpty()) {
			this.displayCyclingItems(villager);
		} else {
			villager.setCurrentOffer(null);
			this.lookTime = Math.min(this.lookTime, 40);
		}

		this.lookTime--;
	}

	public void stop(ServerLevel serverLevel, Villager villager, long l) {
		super.stop(serverLevel, villager, l);
		villager.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
		villager.setCurrentOffer(null);
		this.playerTradeProposition = null;
	}

	private void findItemsToDisplay(LivingEntity livingEntity, Villager villager) {
		boolean bl = false;
		CarryableTrade carryableTrade = livingEntity.getTradeProposition();
		if (this.playerTradeProposition == null || carryableTrade == null || !carryableTrade.matches(this.playerTradeProposition)) {
			this.playerTradeProposition = carryableTrade;
			bl = true;
			this.availableOffers.clear();
		}

		if (bl && this.playerTradeProposition != null) {
			this.updateDisplayItems(villager, this.playerTradeProposition);
			if (!this.availableOffers.isEmpty()) {
				this.lookTime = 900;
				this.displayFirstItem(villager);
			}
		}
	}

	private void displayFirstItem(Villager villager) {
		MerchantOffer merchantOffer = (MerchantOffer)this.availableOffers.get(0);
		villager.setCurrentOffer(merchantOffer);
	}

	private void updateDisplayItems(Villager villager, CarryableTrade carryableTrade) {
		getMatchingOffers(villager, carryableTrade).forEach(this.availableOffers::add);
		Collections.shuffle(this.availableOffers);
	}

	private boolean hasMatchingOffers(Villager villager, CarryableTrade carryableTrade) {
		return !Iterables.isEmpty(getMatchingOffers(villager, carryableTrade));
	}

	private static Iterable<MerchantOffer> getMatchingOffers(Villager villager, CarryableTrade carryableTrade) {
		return Iterables.filter(villager.getOffers(), merchantOffer -> !merchantOffer.isOutOfStock() && merchantOffer.accepts(carryableTrade));
	}

	private LivingEntity lookAtTarget(Villager villager) {
		Brain<?> brain = villager.getBrain();
		LivingEntity livingEntity = (LivingEntity)brain.getMemory(MemoryModuleType.INTERACTION_TARGET).get();
		brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(livingEntity, true));
		return livingEntity;
	}

	private void displayCyclingItems(Villager villager) {
		if (this.availableOffers.size() >= 2 && ++this.cycleCounter >= 100) {
			this.displayIndex++;
			this.cycleCounter = 0;
			if (this.displayIndex > this.availableOffers.size() - 1) {
				this.displayIndex = 0;
			}

			villager.setCurrentOffer((MerchantOffer)this.availableOffers.get(this.displayIndex));
		}
	}
}
