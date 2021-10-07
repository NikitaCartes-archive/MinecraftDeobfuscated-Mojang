package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;

public class PiglinBruteSpecificSensor extends Sensor<LivingEntity> {
	@Override
	public Set<MemoryModuleType<?>> requires() {
		return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.NEARBY_ADULT_PIGLINS);
	}

	@Override
	protected void doTick(ServerLevel serverLevel, LivingEntity livingEntity) {
		Brain<?> brain = livingEntity.getBrain();
		List<AbstractPiglin> list = Lists.<AbstractPiglin>newArrayList();
		NearestVisibleLivingEntities nearestVisibleLivingEntities = (NearestVisibleLivingEntities)brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
			.orElse(NearestVisibleLivingEntities.empty());
		Optional<Mob> optional = nearestVisibleLivingEntities.findClosest(
				livingEntityx -> livingEntityx instanceof WitherSkeleton || livingEntityx instanceof WitherBoss
			)
			.map(Mob.class::cast);

		for (LivingEntity livingEntity2 : (List)brain.getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).orElse(ImmutableList.of())) {
			if (livingEntity2 instanceof AbstractPiglin && ((AbstractPiglin)livingEntity2).isAdult()) {
				list.add((AbstractPiglin)livingEntity2);
			}
		}

		brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS, optional);
		brain.setMemory(MemoryModuleType.NEARBY_ADULT_PIGLINS, list);
	}
}
