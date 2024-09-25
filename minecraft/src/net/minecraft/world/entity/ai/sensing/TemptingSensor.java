package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TemptingSensor extends Sensor<PathfinderMob> {
	private static final TargetingConditions TEMPT_TARGETING = TargetingConditions.forNonCombat().ignoreLineOfSight();
	private final Predicate<ItemStack> temptations;

	public TemptingSensor(Predicate<ItemStack> predicate) {
		this.temptations = predicate;
	}

	protected void doTick(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
		Brain<?> brain = pathfinderMob.getBrain();
		TargetingConditions targetingConditions = TEMPT_TARGETING.copy().range((double)((float)pathfinderMob.getAttributeValue(Attributes.TEMPT_RANGE)));
		List<Player> list = (List<Player>)serverLevel.players()
			.stream()
			.filter(EntitySelector.NO_SPECTATORS)
			.filter(serverPlayer -> targetingConditions.test(serverLevel, pathfinderMob, serverPlayer))
			.filter(this::playerHoldingTemptation)
			.filter(serverPlayer -> !pathfinderMob.hasPassenger(serverPlayer))
			.sorted(Comparator.comparingDouble(pathfinderMob::distanceToSqr))
			.collect(Collectors.toList());
		if (!list.isEmpty()) {
			Player player = (Player)list.get(0);
			brain.setMemory(MemoryModuleType.TEMPTING_PLAYER, player);
		} else {
			brain.eraseMemory(MemoryModuleType.TEMPTING_PLAYER);
		}
	}

	private boolean playerHoldingTemptation(Player player) {
		return this.isTemptation(player.getMainHandItem()) || this.isTemptation(player.getOffhandItem());
	}

	private boolean isTemptation(ItemStack itemStack) {
		return this.temptations.test(itemStack);
	}

	@Override
	public Set<MemoryModuleType<?>> requires() {
		return ImmutableSet.of(MemoryModuleType.TEMPTING_PLAYER);
	}
}
