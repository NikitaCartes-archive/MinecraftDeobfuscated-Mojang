package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.function.BiPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class DismountOrSkipMounting<E extends LivingEntity, T extends Entity> extends Behavior<E> {
	private final int maxWalkDistToRideTarget;
	private final BiPredicate<E, Entity> dontRideIf;

	public DismountOrSkipMounting(int i, BiPredicate<E, Entity> biPredicate) {
		super(ImmutableMap.of(MemoryModuleType.RIDE_TARGET, MemoryStatus.REGISTERED));
		this.maxWalkDistToRideTarget = i;
		this.dontRideIf = biPredicate;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel serverLevel, E livingEntity) {
		Entity entity = livingEntity.getVehicle();
		Entity entity2 = (Entity)livingEntity.getBrain().getMemory(MemoryModuleType.RIDE_TARGET).orElse(null);
		if (entity == null && entity2 == null) {
			return false;
		} else {
			Entity entity3 = entity == null ? entity2 : entity;
			return !this.isVehicleValid(livingEntity, entity3) || this.dontRideIf.test(livingEntity, entity3);
		}
	}

	private boolean isVehicleValid(E livingEntity, Entity entity) {
		return entity.isAlive() && entity.closerThan(livingEntity, (double)this.maxWalkDistToRideTarget) && entity.level == livingEntity.level;
	}

	@Override
	protected void start(ServerLevel serverLevel, E livingEntity, long l) {
		livingEntity.stopRiding();
		livingEntity.getBrain().eraseMemory(MemoryModuleType.RIDE_TARGET);
	}
}
