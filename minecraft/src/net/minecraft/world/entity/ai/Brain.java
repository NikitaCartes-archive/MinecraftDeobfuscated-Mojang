package net.minecraft.world.entity.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Serializable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;

public class Brain<E extends LivingEntity> implements Serializable {
	private final Map<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> memories = Maps.<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>>newHashMap();
	private final Map<SensorType<? extends Sensor<? super E>>, Sensor<? super E>> sensors = Maps.<SensorType<? extends Sensor<? super E>>, Sensor<? super E>>newLinkedHashMap();
	private final Map<Integer, Map<Activity, Set<Behavior<? super E>>>> availableBehaviorsByPriority = Maps.newTreeMap();
	private Schedule schedule = Schedule.EMPTY;
	private final Map<Activity, Set<Pair<MemoryModuleType<?>, MemoryStatus>>> activityRequirements = Maps.<Activity, Set<Pair<MemoryModuleType<?>, MemoryStatus>>>newHashMap();
	private final Map<Activity, Set<MemoryModuleType<?>>> activityMemoriesToEraseWhenStopped = Maps.<Activity, Set<MemoryModuleType<?>>>newHashMap();
	private Set<Activity> coreActivities = Sets.<Activity>newHashSet();
	private final Set<Activity> activeActivities = Sets.<Activity>newHashSet();
	private Activity defaultActivity = Activity.IDLE;
	private long lastScheduleUpdate = -9999L;

	public <T> Brain(Collection<MemoryModuleType<?>> collection, Collection<SensorType<? extends Sensor<? super E>>> collection2, Dynamic<T> dynamic) {
		for (MemoryModuleType<?> memoryModuleType : collection) {
			this.memories.put(memoryModuleType, Optional.empty());
		}

		for (SensorType<? extends Sensor<? super E>> sensorType : collection2) {
			this.sensors.put(sensorType, sensorType.create());
		}

		for (Sensor<? super E> sensor : this.sensors.values()) {
			for (MemoryModuleType<?> memoryModuleType2 : sensor.requires()) {
				this.memories.put(memoryModuleType2, Optional.empty());
			}
		}

		for (Entry<Dynamic<T>, Dynamic<T>> entry : dynamic.get("memories").asMap(Function.identity(), Function.identity()).entrySet()) {
			this.readMemory(Registry.MEMORY_MODULE_TYPE.get(new ResourceLocation(((Dynamic)entry.getKey()).asString(""))), (Dynamic<T>)entry.getValue());
		}
	}

	public boolean hasMemoryValue(MemoryModuleType<?> memoryModuleType) {
		return this.checkMemory(memoryModuleType, MemoryStatus.VALUE_PRESENT);
	}

	private <T, U> void readMemory(MemoryModuleType<U> memoryModuleType, Dynamic<T> dynamic) {
		ExpirableValue<U> expirableValue = new ExpirableValue((Function<Dynamic<?>, T>)memoryModuleType.getDeserializer().orElseThrow(RuntimeException::new), dynamic);
		this.setMemoryInternal(memoryModuleType, Optional.of(expirableValue));
	}

	public <U> void eraseMemory(MemoryModuleType<U> memoryModuleType) {
		this.setMemory(memoryModuleType, Optional.empty());
	}

	public <U> void setMemory(MemoryModuleType<U> memoryModuleType, @Nullable U object) {
		this.setMemory(memoryModuleType, Optional.ofNullable(object));
	}

	public <U> void setMemoryWithExpiry(MemoryModuleType<U> memoryModuleType, U object, long l) {
		this.setMemoryInternal(memoryModuleType, Optional.of(ExpirableValue.of(object, l)));
	}

	public <U> void setMemory(MemoryModuleType<U> memoryModuleType, Optional<? extends U> optional) {
		this.setMemoryInternal(memoryModuleType, optional.map(ExpirableValue::of));
	}

	private <U> void setMemoryInternal(MemoryModuleType<U> memoryModuleType, Optional<? extends ExpirableValue<?>> optional) {
		if (this.memories.containsKey(memoryModuleType)) {
			if (optional.isPresent() && this.isEmptyCollection(((ExpirableValue)optional.get()).getValue())) {
				this.eraseMemory(memoryModuleType);
			} else {
				this.memories.put(memoryModuleType, optional);
			}
		}
	}

	public <U> Optional<U> getMemory(MemoryModuleType<U> memoryModuleType) {
		return ((Optional)this.memories.get(memoryModuleType)).map(ExpirableValue::getValue);
	}

	public boolean checkMemory(MemoryModuleType<?> memoryModuleType, MemoryStatus memoryStatus) {
		Optional<? extends ExpirableValue<?>> optional = (Optional<? extends ExpirableValue<?>>)this.memories.get(memoryModuleType);
		return optional == null
			? false
			: memoryStatus == MemoryStatus.REGISTERED
				|| memoryStatus == MemoryStatus.VALUE_PRESENT && optional.isPresent()
				|| memoryStatus == MemoryStatus.VALUE_ABSENT && !optional.isPresent();
	}

	public Schedule getSchedule() {
		return this.schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	public void setCoreActivities(Set<Activity> set) {
		this.coreActivities = set;
	}

	@Deprecated
	public List<Behavior<? super E>> getRunningBehaviors() {
		List<Behavior<? super E>> list = new ObjectArrayList<>();

		for (Map<Activity, Set<Behavior<? super E>>> map : this.availableBehaviorsByPriority.values()) {
			for (Set<Behavior<? super E>> set : map.values()) {
				for (Behavior<? super E> behavior : set) {
					if (behavior.getStatus() == Behavior.Status.RUNNING) {
						list.add(behavior);
					}
				}
			}
		}

		return list;
	}

	public void useDefaultActivity() {
		this.setActiveActivity(this.defaultActivity);
	}

	public Optional<Activity> getActiveNonCoreActivity() {
		for (Activity activity : this.activeActivities) {
			if (!this.coreActivities.contains(activity)) {
				return Optional.of(activity);
			}
		}

		return Optional.empty();
	}

	public void setActiveActivityIfPossible(Activity activity) {
		if (this.activityRequirementsAreMet(activity)) {
			this.setActiveActivity(activity);
		} else {
			this.useDefaultActivity();
		}
	}

	private void setActiveActivity(Activity activity) {
		if (!this.isActive(activity)) {
			this.eraseMemoriesForOtherActivitesThan(activity);
			this.activeActivities.clear();
			this.activeActivities.addAll(this.coreActivities);
			this.activeActivities.add(activity);
		}
	}

	private void eraseMemoriesForOtherActivitesThan(Activity activity) {
		for (Activity activity2 : this.activeActivities) {
			if (activity2 != activity) {
				Set<MemoryModuleType<?>> set = (Set<MemoryModuleType<?>>)this.activityMemoriesToEraseWhenStopped.get(activity2);
				if (set != null) {
					for (MemoryModuleType<?> memoryModuleType : set) {
						this.eraseMemory(memoryModuleType);
					}
				}
			}
		}
	}

	public void updateActivityFromSchedule(long l, long m) {
		if (m - this.lastScheduleUpdate > 20L) {
			this.lastScheduleUpdate = m;
			Activity activity = this.getSchedule().getActivityAt((int)(l % 24000L));
			if (!this.activeActivities.contains(activity)) {
				this.setActiveActivityIfPossible(activity);
			}
		}
	}

	public void setActiveActivityToFirstValid(List<Activity> list) {
		for (Activity activity : list) {
			if (this.activityRequirementsAreMet(activity)) {
				this.setActiveActivity(activity);
				break;
			}
		}
	}

	public void setDefaultActivity(Activity activity) {
		this.defaultActivity = activity;
	}

	public void addActivity(Activity activity, int i, ImmutableList<? extends Behavior<? super E>> immutableList) {
		this.addActivity(activity, this.createPriorityPairs(i, immutableList));
	}

	public void addActivityAndRemoveMemoryWhenStopped(
		Activity activity, int i, ImmutableList<? extends Behavior<? super E>> immutableList, MemoryModuleType<?> memoryModuleType
	) {
		Set<Pair<MemoryModuleType<?>, MemoryStatus>> set = ImmutableSet.of(Pair.of(memoryModuleType, MemoryStatus.VALUE_PRESENT));
		Set<MemoryModuleType<?>> set2 = ImmutableSet.of(memoryModuleType);
		this.addActivityAndRemoveMemoriesWhenStopped(activity, this.createPriorityPairs(i, immutableList), set, set2);
	}

	public void addActivity(Activity activity, ImmutableList<? extends Pair<Integer, ? extends Behavior<? super E>>> immutableList) {
		this.addActivityAndRemoveMemoriesWhenStopped(activity, immutableList, ImmutableSet.of(), Sets.<MemoryModuleType<?>>newHashSet());
	}

	public void addActivityWithConditions(
		Activity activity, ImmutableList<? extends Pair<Integer, ? extends Behavior<? super E>>> immutableList, Set<Pair<MemoryModuleType<?>, MemoryStatus>> set
	) {
		this.addActivityAndRemoveMemoriesWhenStopped(activity, immutableList, set, Sets.<MemoryModuleType<?>>newHashSet());
	}

	private void addActivityAndRemoveMemoriesWhenStopped(
		Activity activity,
		ImmutableList<? extends Pair<Integer, ? extends Behavior<? super E>>> immutableList,
		Set<Pair<MemoryModuleType<?>, MemoryStatus>> set,
		Set<MemoryModuleType<?>> set2
	) {
		this.activityRequirements.put(activity, set);
		if (!set2.isEmpty()) {
			this.activityMemoriesToEraseWhenStopped.put(activity, set2);
		}

		for (Pair<Integer, ? extends Behavior<? super E>> pair : immutableList) {
			((Set)((Map)this.availableBehaviorsByPriority.computeIfAbsent(pair.getFirst(), integer -> Maps.newHashMap()))
					.computeIfAbsent(activity, activityx -> Sets.newLinkedHashSet()))
				.add(pair.getSecond());
		}
	}

	public boolean isActive(Activity activity) {
		return this.activeActivities.contains(activity);
	}

	public Brain<E> copyWithoutBehaviors() {
		Brain<E> brain = new Brain<>(this.memories.keySet(), this.sensors.keySet(), new Dynamic<>(NbtOps.INSTANCE, new CompoundTag()));

		for (Entry<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> entry : this.memories.entrySet()) {
			MemoryModuleType<?> memoryModuleType = (MemoryModuleType<?>)entry.getKey();
			if (((Optional)entry.getValue()).isPresent()) {
				brain.memories.put(memoryModuleType, entry.getValue());
			}
		}

		return brain;
	}

	public void tick(ServerLevel serverLevel, E livingEntity) {
		this.forgetOutdatedMemories();
		this.tickSensors(serverLevel, livingEntity);
		this.startEachNonRunningBehavior(serverLevel, livingEntity);
		this.tickEachRunningBehavior(serverLevel, livingEntity);
	}

	private void tickSensors(ServerLevel serverLevel, E livingEntity) {
		for (Sensor<? super E> sensor : this.sensors.values()) {
			sensor.tick(serverLevel, livingEntity);
		}
	}

	private void forgetOutdatedMemories() {
		for (Entry<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> entry : this.memories.entrySet()) {
			if (((Optional)entry.getValue()).isPresent()) {
				ExpirableValue<?> expirableValue = (ExpirableValue<?>)((Optional)entry.getValue()).get();
				expirableValue.tick();
				if (expirableValue.hasExpired()) {
					this.eraseMemory((MemoryModuleType)entry.getKey());
				}
			}
		}
	}

	public void stopAll(ServerLevel serverLevel, E livingEntity) {
		long l = livingEntity.level.getGameTime();

		for (Behavior<? super E> behavior : this.getRunningBehaviors()) {
			behavior.doStop(serverLevel, livingEntity, l);
		}
	}

	@Override
	public <T> T serialize(DynamicOps<T> dynamicOps) {
		Builder<T, T> builder = ImmutableMap.builder();

		for (Entry<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> entry : this.memories.entrySet()) {
			MemoryModuleType<?> memoryModuleType = (MemoryModuleType<?>)entry.getKey();
			if (((Optional)entry.getValue()).isPresent() && memoryModuleType.getDeserializer().isPresent()) {
				ExpirableValue<?> expirableValue = (ExpirableValue<?>)((Optional)entry.getValue()).get();
				T object = dynamicOps.createString(Registry.MEMORY_MODULE_TYPE.getKey(memoryModuleType).toString());
				T object2 = expirableValue.serialize(dynamicOps);
				builder.put(object, object2);
			}
		}

		return dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("memories"), dynamicOps.createMap(builder.build())));
	}

	private void startEachNonRunningBehavior(ServerLevel serverLevel, E livingEntity) {
		long l = serverLevel.getGameTime();

		for (Map<Activity, Set<Behavior<? super E>>> map : this.availableBehaviorsByPriority.values()) {
			for (Entry<Activity, Set<Behavior<? super E>>> entry : map.entrySet()) {
				Activity activity = (Activity)entry.getKey();
				if (this.activeActivities.contains(activity)) {
					for (Behavior<? super E> behavior : (Set)entry.getValue()) {
						if (behavior.getStatus() == Behavior.Status.STOPPED) {
							behavior.tryStart(serverLevel, livingEntity, l);
						}
					}
				}
			}
		}
	}

	private void tickEachRunningBehavior(ServerLevel serverLevel, E livingEntity) {
		long l = serverLevel.getGameTime();

		for (Behavior<? super E> behavior : this.getRunningBehaviors()) {
			behavior.tickOrStop(serverLevel, livingEntity, l);
		}
	}

	private boolean activityRequirementsAreMet(Activity activity) {
		if (!this.activityRequirements.containsKey(activity)) {
			return false;
		} else {
			for (Pair<MemoryModuleType<?>, MemoryStatus> pair : (Set)this.activityRequirements.get(activity)) {
				MemoryModuleType<?> memoryModuleType = pair.getFirst();
				MemoryStatus memoryStatus = pair.getSecond();
				if (!this.checkMemory(memoryModuleType, memoryStatus)) {
					return false;
				}
			}

			return true;
		}
	}

	private boolean isEmptyCollection(Object object) {
		return object instanceof Collection && ((Collection)object).isEmpty();
	}

	ImmutableList<? extends Pair<Integer, ? extends Behavior<? super E>>> createPriorityPairs(int i, ImmutableList<? extends Behavior<? super E>> immutableList) {
		int j = i;
		com.google.common.collect.ImmutableList.Builder<Pair<Integer, ? extends Behavior<? super E>>> builder = ImmutableList.builder();

		for (Behavior<? super E> behavior : immutableList) {
			builder.add(Pair.of(j++, behavior));
		}

		return builder.build();
	}
}
