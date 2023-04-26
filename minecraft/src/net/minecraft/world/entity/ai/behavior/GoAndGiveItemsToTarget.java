package net.minecraft.world.entity.ai.behavior;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.allay.AllayAi;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class GoAndGiveItemsToTarget<E extends LivingEntity & InventoryCarrier> extends Behavior<E> {
	private static final int CLOSE_ENOUGH_DISTANCE_TO_TARGET = 3;
	private static final int ITEM_PICKUP_COOLDOWN_AFTER_THROWING = 60;
	private final Function<LivingEntity, Optional<PositionTracker>> targetPositionGetter;
	private final float speedModifier;

	public GoAndGiveItemsToTarget(Function<LivingEntity, Optional<PositionTracker>> function, float f, int i) {
		super(
			Map.of(
				MemoryModuleType.LOOK_TARGET,
				MemoryStatus.REGISTERED,
				MemoryModuleType.WALK_TARGET,
				MemoryStatus.REGISTERED,
				MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS,
				MemoryStatus.REGISTERED
			),
			i
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
					throwItem(livingEntity, itemStack, getThrowPosition(positionTracker));
					if (livingEntity instanceof Allay allay) {
						AllayAi.getLikedPlayer(allay).ifPresent(serverPlayer -> this.triggerDropItemOnBlock(positionTracker, itemStack, serverPlayer));
					}

					livingEntity.getBrain().setMemory(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, 60);
				}
			}
		}
	}

	private void triggerDropItemOnBlock(PositionTracker positionTracker, ItemStack itemStack, ServerPlayer serverPlayer) {
		BlockPos blockPos = positionTracker.currentBlockPosition().below();
		CriteriaTriggers.ALLAY_DROP_ITEM_ON_BLOCK.trigger(serverPlayer, blockPos, itemStack);
	}

	private boolean canThrowItemToTarget(E livingEntity) {
		if (livingEntity.getInventory().isEmpty()) {
			return false;
		} else {
			Optional<PositionTracker> optional = (Optional<PositionTracker>)this.targetPositionGetter.apply(livingEntity);
			return optional.isPresent();
		}
	}

	private static Vec3 getThrowPosition(PositionTracker positionTracker) {
		return positionTracker.currentPosition().add(0.0, 1.0, 0.0);
	}

	public static void throwItem(LivingEntity livingEntity, ItemStack itemStack, Vec3 vec3) {
		Vec3 vec32 = new Vec3(0.2F, 0.3F, 0.2F);
		BehaviorUtils.throwItem(livingEntity, itemStack, vec3, vec32, 0.2F);
		Level level = livingEntity.level();
		if (level.getGameTime() % 7L == 0L && level.random.nextDouble() < 0.9) {
			float f = Util.<Float>getRandom(Allay.THROW_SOUND_PITCHES, level.getRandom());
			level.playSound(null, livingEntity, SoundEvents.ALLAY_THROW, SoundSource.NEUTRAL, 1.0F, f);
		}
	}
}
