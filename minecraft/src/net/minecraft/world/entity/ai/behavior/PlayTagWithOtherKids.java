package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.phys.Vec3;

public class PlayTagWithOtherKids extends Behavior<PathfinderMob> {
	public PlayTagWithOtherKids() {
		super(
			ImmutableMap.of(
				MemoryModuleType.VISIBLE_VILLAGER_BABIES,
				MemoryStatus.VALUE_PRESENT,
				MemoryModuleType.WALK_TARGET,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.LOOK_TARGET,
				MemoryStatus.REGISTERED,
				MemoryModuleType.INTERACTION_TARGET,
				MemoryStatus.REGISTERED
			)
		);
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
		return serverLevel.getRandom().nextInt(10) == 0 && this.hasFriendsNearby(pathfinderMob);
	}

	protected void start(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
		LivingEntity livingEntity = this.seeIfSomeoneIsChasingMe(pathfinderMob);
		if (livingEntity != null) {
			this.fleeFromChaser(serverLevel, pathfinderMob, livingEntity);
		} else {
			Optional<LivingEntity> optional = this.findSomeoneBeingChased(pathfinderMob);
			if (optional.isPresent()) {
				chaseKid(pathfinderMob, (LivingEntity)optional.get());
			} else {
				this.findSomeoneToChase(pathfinderMob).ifPresent(livingEntityx -> chaseKid(pathfinderMob, livingEntityx));
			}
		}
	}

	private void fleeFromChaser(ServerLevel serverLevel, PathfinderMob pathfinderMob, LivingEntity livingEntity) {
		for (int i = 0; i < 10; i++) {
			Vec3 vec3 = RandomPos.getLandPos(pathfinderMob, 20, 8);
			if (vec3 != null && serverLevel.isVillage(new BlockPos(vec3))) {
				pathfinderMob.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(vec3, 0.6F, 0));
				return;
			}
		}
	}

	private static void chaseKid(PathfinderMob pathfinderMob, LivingEntity livingEntity) {
		Brain<?> brain = pathfinderMob.getBrain();
		brain.setMemory(MemoryModuleType.INTERACTION_TARGET, livingEntity);
		brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityPosWrapper(livingEntity));
		brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityPosWrapper(livingEntity), 0.6F, 1));
	}

	private Optional<LivingEntity> findSomeoneToChase(PathfinderMob pathfinderMob) {
		return this.getFriendsNearby(pathfinderMob).stream().findAny();
	}

	private Optional<LivingEntity> findSomeoneBeingChased(PathfinderMob pathfinderMob) {
		Map<LivingEntity, Integer> map = this.checkHowManyChasersEachFriendHas(pathfinderMob);
		return map.entrySet()
			.stream()
			.sorted(Comparator.comparingInt(Entry::getValue))
			.filter(entry -> (Integer)entry.getValue() > 0 && (Integer)entry.getValue() <= 5)
			.map(Entry::getKey)
			.findFirst();
	}

	private Map<LivingEntity, Integer> checkHowManyChasersEachFriendHas(PathfinderMob pathfinderMob) {
		Map<LivingEntity, Integer> map = Maps.<LivingEntity, Integer>newHashMap();
		this.getFriendsNearby(pathfinderMob).stream().filter(this::isChasingSomeone).forEach(livingEntity -> {
			Integer var10000 = (Integer)map.compute(this.whoAreYouChasing(livingEntity), (livingEntityx, integer) -> integer == null ? 1 : integer + 1);
		});
		return map;
	}

	private List<LivingEntity> getFriendsNearby(PathfinderMob pathfinderMob) {
		return (List<LivingEntity>)pathfinderMob.getBrain().getMemory(MemoryModuleType.VISIBLE_VILLAGER_BABIES).get();
	}

	private LivingEntity whoAreYouChasing(LivingEntity livingEntity) {
		return (LivingEntity)livingEntity.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get();
	}

	@Nullable
	private LivingEntity seeIfSomeoneIsChasingMe(LivingEntity livingEntity) {
		return (LivingEntity)((List)livingEntity.getBrain().getMemory(MemoryModuleType.VISIBLE_VILLAGER_BABIES).get())
			.stream()
			.filter(livingEntity2 -> this.isFriendChasingMe(livingEntity, livingEntity2))
			.findAny()
			.orElse(null);
	}

	private boolean isChasingSomeone(LivingEntity livingEntity) {
		return livingEntity.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).isPresent();
	}

	private boolean isFriendChasingMe(LivingEntity livingEntity, LivingEntity livingEntity2) {
		return livingEntity2.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).filter(livingEntity2x -> livingEntity2x == livingEntity).isPresent();
	}

	private boolean hasFriendsNearby(PathfinderMob pathfinderMob) {
		return pathfinderMob.getBrain().hasMemoryValue(MemoryModuleType.VISIBLE_VILLAGER_BABIES);
	}
}
