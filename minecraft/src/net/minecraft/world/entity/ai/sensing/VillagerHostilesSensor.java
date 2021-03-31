package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableMap;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class VillagerHostilesSensor extends NearestVisibleLivingEntitySensor {
	private static final ImmutableMap<EntityType<?>, Float> ACCEPTABLE_DISTANCE_FROM_HOSTILES = ImmutableMap.<EntityType<?>, Float>builder()
		.put(EntityType.DROWNED, 8.0F)
		.put(EntityType.EVOKER, 12.0F)
		.put(EntityType.HUSK, 8.0F)
		.put(EntityType.ILLUSIONER, 12.0F)
		.put(EntityType.PILLAGER, 15.0F)
		.put(EntityType.RAVAGER, 12.0F)
		.put(EntityType.VEX, 8.0F)
		.put(EntityType.VINDICATOR, 10.0F)
		.put(EntityType.ZOGLIN, 10.0F)
		.put(EntityType.ZOMBIE, 8.0F)
		.put(EntityType.ZOMBIE_VILLAGER, 8.0F)
		.build();

	@Override
	protected boolean isMatchingEntity(LivingEntity livingEntity, LivingEntity livingEntity2) {
		return this.isHostile(livingEntity2) && this.isClose(livingEntity, livingEntity2);
	}

	private boolean isClose(LivingEntity livingEntity, LivingEntity livingEntity2) {
		float f = ACCEPTABLE_DISTANCE_FROM_HOSTILES.get(livingEntity2.getType());
		return livingEntity2.distanceToSqr(livingEntity) <= (double)(f * f);
	}

	@Override
	protected MemoryModuleType<LivingEntity> getMemory() {
		return MemoryModuleType.NEAREST_HOSTILE;
	}

	private boolean isHostile(LivingEntity livingEntity) {
		return ACCEPTABLE_DISTANCE_FROM_HOSTILES.containsKey(livingEntity.getType());
	}
}
