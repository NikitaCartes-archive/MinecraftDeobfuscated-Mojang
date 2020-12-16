package net.minecraft.world.entity.ai.sensing;

import java.util.Comparator;
import java.util.Optional;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class AxolotlHostileSensor extends HostilesSensor {
	@Override
	protected Optional<LivingEntity> getNearestHostile(LivingEntity livingEntity) {
		return this.getVisibleEntities(livingEntity)
			.flatMap(
				list -> list.stream()
						.filter(livingEntity2 -> this.shouldTarget(livingEntity, livingEntity2))
						.filter(livingEntity2 -> this.isClose(livingEntity, livingEntity2))
						.filter(Entity::isInWaterOrBubble)
						.min(Comparator.comparingDouble(livingEntity::distanceToSqr))
			);
	}

	private boolean shouldTarget(LivingEntity livingEntity, LivingEntity livingEntity2) {
		EntityType<?> entityType = livingEntity2.getType();
		if (EntityTypeTags.AXOLOTL_ALWAYS_HOSTILES.contains(entityType)) {
			return true;
		} else if (!EntityTypeTags.AXOLOTL_TEMPTED_HOSTILES.contains(entityType)) {
			return false;
		} else {
			Optional<Boolean> optional = livingEntity.getBrain().getMemory(MemoryModuleType.IS_TEMPTED);
			return optional.isPresent() && (Boolean)optional.get();
		}
	}

	@Override
	protected boolean isClose(LivingEntity livingEntity, LivingEntity livingEntity2) {
		return livingEntity2.distanceToSqr(livingEntity) <= 64.0;
	}
}
