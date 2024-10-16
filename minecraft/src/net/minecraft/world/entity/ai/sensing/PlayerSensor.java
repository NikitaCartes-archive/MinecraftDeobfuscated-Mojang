package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;

public class PlayerSensor extends Sensor<LivingEntity> {
	@Override
	public Set<MemoryModuleType<?>> requires() {
		return ImmutableSet.of(MemoryModuleType.NEAREST_PLAYERS, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER);
	}

	@Override
	protected void doTick(ServerLevel serverLevel, LivingEntity livingEntity) {
		List<Player> list = (List<Player>)serverLevel.players()
			.stream()
			.filter(EntitySelector.NO_SPECTATORS)
			.filter(serverPlayer -> livingEntity.closerThan(serverPlayer, this.getFollowDistance(livingEntity)))
			.sorted(Comparator.comparingDouble(livingEntity::distanceToSqr))
			.collect(Collectors.toList());
		Brain<?> brain = livingEntity.getBrain();
		brain.setMemory(MemoryModuleType.NEAREST_PLAYERS, list);
		List<Player> list2 = (List<Player>)list.stream().filter(player -> isEntityTargetable(serverLevel, livingEntity, player)).collect(Collectors.toList());
		brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER, list2.isEmpty() ? null : (Player)list2.get(0));
		Optional<Player> optional = list2.stream().filter(player -> isEntityAttackable(serverLevel, livingEntity, player)).findFirst();
		brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, optional);
	}

	protected double getFollowDistance(LivingEntity livingEntity) {
		return livingEntity.getAttributeValue(Attributes.FOLLOW_RANGE);
	}
}
