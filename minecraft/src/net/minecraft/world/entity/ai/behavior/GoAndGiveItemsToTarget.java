package net.minecraft.world.entity.ai.behavior;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class GoAndGiveItemsToTarget<E extends LivingEntity & InventoryCarrier> extends Behavior<E> {
	private static final int CLOSE_ENOUGH_DISTANCE_TO_TARGET = 3;
	private static final int ITEM_PICKUP_COOLDOWN_AFTER_THROWING = 100;
	private final Function<LivingEntity, Optional<PositionTracker>> targetPositionGetter;
	private final float speedModifier;

	public GoAndGiveItemsToTarget(Function<LivingEntity, Optional<PositionTracker>> function, float f) {
		super(
			Map.of(
				MemoryModuleType.LOOK_TARGET,
				MemoryStatus.REGISTERED,
				MemoryModuleType.WALK_TARGET,
				MemoryStatus.REGISTERED,
				MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS,
				MemoryStatus.REGISTERED
			)
		);
		this.targetPositionGetter = function;
		this.speedModifier = f;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel serverLevel, E livingEntity) {
		return this.canThrowItemToTarget(livingEntity);
	}

	@Override
	protected boolean canStillUse(ServerLevel serverLevel, E livingEntity, long l) {
		return this.canThrowItemToTarget(livingEntity);
	}

	@Override
	protected void start(ServerLevel serverLevel, E livingEntity, long l) {
		((Optional)this.targetPositionGetter.apply(livingEntity))
			.ifPresent(positionTracker -> BehaviorUtils.setWalkAndLookTargetMemories(livingEntity, positionTracker, this.speedModifier, 3));
	}

	@Override
	protected void tick(ServerLevel serverLevel, E livingEntity, long l) {
		Optional<PositionTracker> optional = (Optional<PositionTracker>)this.targetPositionGetter.apply(livingEntity);
		if (!optional.isEmpty()) {
			PositionTracker positionTracker = (PositionTracker)optional.get();
			double d = positionTracker.currentPosition().distanceTo(livingEntity.getEyePosition());
			if (d < 3.0) {
				ItemStack itemStack = livingEntity.getInventory().removeItem(0, 1);
				if (!itemStack.isEmpty()) {
					BehaviorUtils.throwItem(livingEntity, itemStack, getThrowPosition(positionTracker));
					livingEntity.getBrain().setMemory(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, 100);
				}
			}
		}
	}

	private boolean canThrowItemToTarget(E livingEntity) {
		return !livingEntity.getInventory().isEmpty() && ((Optional)this.targetPositionGetter.apply(livingEntity)).isPresent();
	}

	private static Vec3 getThrowPosition(PositionTracker positionTracker) {
		return positionTracker.currentPosition().add(0.0, 1.0, 0.0);
	}
}
