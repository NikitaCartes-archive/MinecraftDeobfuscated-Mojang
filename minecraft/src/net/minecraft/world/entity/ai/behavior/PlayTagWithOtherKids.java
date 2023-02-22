package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.Maps;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class PlayTagWithOtherKids {
	private static final int MAX_FLEE_XZ_DIST = 20;
	private static final int MAX_FLEE_Y_DIST = 8;
	private static final float FLEE_SPEED_MODIFIER = 0.6F;
	private static final float CHASE_SPEED_MODIFIER = 0.6F;
	private static final int MAX_CHASERS_PER_TARGET = 5;
	private static final int AVERAGE_WAIT_TIME_BETWEEN_RUNS = 10;

	public static BehaviorControl<PathfinderMob> create() {
		return BehaviorBuilder.create(
			instance -> instance.group(
						instance.present(MemoryModuleType.VISIBLE_VILLAGER_BABIES),
						instance.absent(MemoryModuleType.WALK_TARGET),
						instance.registered(MemoryModuleType.LOOK_TARGET),
						instance.registered(MemoryModuleType.INTERACTION_TARGET)
					)
					.apply(instance, (memoryAccessor, memoryAccessor2, memoryAccessor3, memoryAccessor4) -> (serverLevel, pathfinderMob, l) -> {
							if (serverLevel.getRandom().nextInt(10) != 0) {
								return false;
							} else {
								List<LivingEntity> list = instance.get(memoryAccessor);
								Optional<LivingEntity> optional = list.stream().filter(livingEntity -> isFriendChasingMe(pathfinderMob, livingEntity)).findAny();
								if (!optional.isPresent()) {
									Optional<LivingEntity> optional2 = findSomeoneBeingChased(list);
									if (optional2.isPresent()) {
										chaseKid(memoryAccessor4, memoryAccessor3, memoryAccessor2, (LivingEntity)optional2.get());
										return true;
									} else {
										list.stream().findAny().ifPresent(livingEntity -> chaseKid(memoryAccessor4, memoryAccessor3, memoryAccessor2, livingEntity));
										return true;
									}
								} else {
									for (int i = 0; i < 10; i++) {
										Vec3 vec3 = LandRandomPos.getPos(pathfinderMob, 20, 8);
										if (vec3 != null && serverLevel.isVillage(BlockPos.containing(vec3))) {
											memoryAccessor2.set(new WalkTarget(vec3, 0.6F, 0));
											break;
										}
									}

									return true;
								}
							}
						})
		);
	}

	private static void chaseKid(
		MemoryAccessor<?, LivingEntity> memoryAccessor,
		MemoryAccessor<?, PositionTracker> memoryAccessor2,
		MemoryAccessor<?, WalkTarget> memoryAccessor3,
		LivingEntity livingEntity
	) {
		memoryAccessor.set(livingEntity);
		memoryAccessor2.set(new EntityTracker(livingEntity, true));
		memoryAccessor3.set(new WalkTarget(new EntityTracker(livingEntity, false), 0.6F, 1));
	}

	private static Optional<LivingEntity> findSomeoneBeingChased(List<LivingEntity> list) {
		Map<LivingEntity, Integer> map = checkHowManyChasersEachFriendHas(list);
		return map.entrySet()
			.stream()
			.sorted(Comparator.comparingInt(Entry::getValue))
			.filter(entry -> (Integer)entry.getValue() > 0 && (Integer)entry.getValue() <= 5)
			.map(Entry::getKey)
			.findFirst();
	}

	private static Map<LivingEntity, Integer> checkHowManyChasersEachFriendHas(List<LivingEntity> list) {
		Map<LivingEntity, Integer> map = Maps.<LivingEntity, Integer>newHashMap();
		list.stream()
			.filter(PlayTagWithOtherKids::isChasingSomeone)
			.forEach(livingEntity -> map.compute(whoAreYouChasing(livingEntity), (livingEntityx, integer) -> integer == null ? 1 : integer + 1));
		return map;
	}

	private static LivingEntity whoAreYouChasing(LivingEntity livingEntity) {
		return (LivingEntity)livingEntity.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get();
	}

	private static boolean isChasingSomeone(LivingEntity livingEntity) {
		return livingEntity.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).isPresent();
	}

	private static boolean isFriendChasingMe(LivingEntity livingEntity, LivingEntity livingEntity2) {
		return livingEntity2.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).filter(livingEntity2x -> livingEntity2x == livingEntity).isPresent();
	}
}
