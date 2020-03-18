package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.item.ItemEntity;

public class GoToWantedItem<E extends LivingEntity> extends Behavior<E> {
	private final Predicate<E> predicate;
	private final int maxDistToWalk;
	private final float speedModifier;

	public GoToWantedItem(float f, boolean bl, int i) {
		this(livingEntity -> true, f, bl, i);
	}

	public GoToWantedItem(Predicate<E> predicate, float f, boolean bl, int i) {
		super(
			ImmutableMap.of(
				MemoryModuleType.LOOK_TARGET,
				MemoryStatus.REGISTERED,
				MemoryModuleType.WALK_TARGET,
				bl ? MemoryStatus.REGISTERED : MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM,
				MemoryStatus.VALUE_PRESENT
			)
		);
		this.predicate = predicate;
		this.maxDistToWalk = i;
		this.speedModifier = f;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel serverLevel, E livingEntity) {
		return this.predicate.test(livingEntity) && this.getClosestLovedItem(livingEntity).closerThan(livingEntity, (double)this.maxDistToWalk);
	}

	@Override
	protected void start(ServerLevel serverLevel, E livingEntity, long l) {
		BehaviorUtils.setWalkAndLookTargetMemories(livingEntity, this.getClosestLovedItem(livingEntity), this.speedModifier, 0);
	}

	private ItemEntity getClosestLovedItem(E livingEntity) {
		return (ItemEntity)livingEntity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM).get();
	}
}
