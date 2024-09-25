package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;

public class NearestItemSensor extends Sensor<Mob> {
	private static final long XZ_RANGE = 32L;
	private static final long Y_RANGE = 16L;
	public static final int MAX_DISTANCE_TO_WANTED_ITEM = 32;

	@Override
	public Set<MemoryModuleType<?>> requires() {
		return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);
	}

	protected void doTick(ServerLevel serverLevel, Mob mob) {
		Brain<?> brain = mob.getBrain();
		List<ItemEntity> list = serverLevel.getEntitiesOfClass(ItemEntity.class, mob.getBoundingBox().inflate(32.0, 16.0, 32.0), itemEntity -> true);
		list.sort(Comparator.comparingDouble(mob::distanceToSqr));
		Optional<ItemEntity> optional = list.stream()
			.filter(itemEntity -> mob.wantsToPickUp(serverLevel, itemEntity.getItem()))
			.filter(itemEntity -> itemEntity.closerThan(mob, 32.0))
			.filter(mob::hasLineOfSight)
			.findFirst();
		brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, optional);
	}
}
