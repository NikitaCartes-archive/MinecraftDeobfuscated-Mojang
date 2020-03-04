/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

public class Brain<E extends LivingEntity>
implements Serializable {
    private final Map<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> memories = Maps.newHashMap();
    private final Map<SensorType<? extends Sensor<? super E>>, Sensor<? super E>> sensors = Maps.newLinkedHashMap();
    private final Map<Integer, Map<Activity, Set<Behavior<? super E>>>> availableBehaviorsByPriority = Maps.newTreeMap();
    private Schedule schedule = Schedule.EMPTY;
    private final Map<Activity, Set<Pair<MemoryModuleType<?>, MemoryStatus>>> activityRequirements = Maps.newHashMap();
    private final Map<Activity, Set<MemoryModuleType<?>>> activityMemoriesToEraseWhenStopped = Maps.newHashMap();
    private Set<Activity> coreActivities = Sets.newHashSet();
    private final Set<Activity> activeActivities = Sets.newHashSet();
    private Activity defaultActivity = Activity.IDLE;
    private long lastScheduleUpdate = -9999L;

    public <T> Brain(Collection<MemoryModuleType<?>> collection, Collection<SensorType<? extends Sensor<? super E>>> collection2, Dynamic<T> dynamic) {
        collection.forEach(memoryModuleType -> this.memories.put((MemoryModuleType<?>)memoryModuleType, Optional.empty()));
        collection2.forEach(sensorType -> this.sensors.put((SensorType<Sensor<E>>)sensorType, (Sensor<E>)sensorType.create()));
        this.sensors.values().forEach(sensor -> {
            for (MemoryModuleType<?> memoryModuleType : sensor.requires()) {
                this.memories.put(memoryModuleType, Optional.empty());
            }
        });
        for (Map.Entry entry : dynamic.get("memories").asMap(Function.identity(), Function.identity()).entrySet()) {
            this.readMemory(Registry.MEMORY_MODULE_TYPE.get(new ResourceLocation(((Dynamic)entry.getKey()).asString(""))), (Dynamic)entry.getValue());
        }
    }

    public boolean hasMemoryValue(MemoryModuleType<?> memoryModuleType) {
        return this.checkMemory(memoryModuleType, MemoryStatus.VALUE_PRESENT);
    }

    private <T, U> void readMemory(MemoryModuleType<U> memoryModuleType, Dynamic<T> dynamic) {
        ExpirableValue<U> expirableValue = new ExpirableValue<U>(memoryModuleType.getDeserializer().orElseThrow(RuntimeException::new), dynamic);
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

    public <U> void setMemory(MemoryModuleType<U> memoryModuleType, Optional<U> optional) {
        this.setMemoryInternal(memoryModuleType, optional.map(ExpirableValue::of));
    }

    private <U> void setMemoryInternal(MemoryModuleType<U> memoryModuleType, Optional<? extends ExpirableValue<?>> optional) {
        if (this.memories.containsKey(memoryModuleType)) {
            if (optional.isPresent() && this.isEmptyCollection(optional.get().getValue())) {
                this.eraseMemory(memoryModuleType);
            } else {
                this.memories.put(memoryModuleType, optional);
            }
        }
    }

    public <U> Optional<U> getMemory(MemoryModuleType<U> memoryModuleType) {
        return this.memories.get(memoryModuleType).map(ExpirableValue::getValue);
    }

    public boolean checkMemory(MemoryModuleType<?> memoryModuleType, MemoryStatus memoryStatus) {
        Optional<ExpirableValue<?>> optional = this.memories.get(memoryModuleType);
        if (optional == null) {
            return false;
        }
        return memoryStatus == MemoryStatus.REGISTERED || memoryStatus == MemoryStatus.VALUE_PRESENT && optional.isPresent() || memoryStatus == MemoryStatus.VALUE_ABSENT && !optional.isPresent();
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
    public Stream<Behavior<? super E>> getRunningBehaviorsStream() {
        return this.availableBehaviorsByPriority.values().stream().flatMap(map -> map.values().stream()).flatMap(Collection::stream).filter(behavior -> behavior.getStatus() == Behavior.Status.RUNNING);
    }

    public void useDefaultActivity() {
        this.setActiveActivity(this.defaultActivity);
    }

    public Optional<Activity> getActiveNonCoreActivity() {
        return this.activeActivities.stream().filter(activity -> !this.coreActivities.contains(activity)).findFirst();
    }

    public void setActiveActivityIfPossible(Activity activity) {
        if (this.activityRequirementsAreMet(activity)) {
            this.setActiveActivity(activity);
        } else {
            this.useDefaultActivity();
        }
    }

    private void setActiveActivity(Activity activity) {
        if (this.isActive(activity)) {
            return;
        }
        this.eraseMemoriesForOtherActivitesThan(activity);
        this.activeActivities.clear();
        this.activeActivities.addAll(this.coreActivities);
        this.activeActivities.add(activity);
    }

    private void eraseMemoriesForOtherActivitesThan(Activity activity) {
        this.activeActivities.stream().filter(activity2 -> activity2 != activity).map(this.activityMemoriesToEraseWhenStopped::get).filter(Objects::nonNull).flatMap(Collection::stream).forEach(this::eraseMemory);
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
        list.stream().filter(this::activityRequirementsAreMet).findFirst().ifPresent(this::setActiveActivity);
    }

    public void setDefaultActivity(Activity activity) {
        this.defaultActivity = activity;
    }

    public void addActivity(Activity activity, int i, ImmutableList<? extends Behavior<? super E>> immutableList) {
        this.addActivity(activity, this.createPriorityPairs(i, immutableList));
    }

    public void addActivityAndRemoveMemoryWhenStopped(Activity activity, int i, ImmutableList<? extends Behavior<? super E>> immutableList, MemoryModuleType<?> memoryModuleType) {
        ImmutableSet<Pair<MemoryModuleType<?>, MemoryStatus>> set = ImmutableSet.of(Pair.of(memoryModuleType, MemoryStatus.VALUE_PRESENT));
        ImmutableSet<MemoryModuleType<?>> set2 = ImmutableSet.of(memoryModuleType);
        this.addActivityAndRemoveMemoriesWhenStopped(activity, this.createPriorityPairs(i, immutableList), set, set2);
    }

    public void addActivity(Activity activity, ImmutableList<? extends Pair<Integer, ? extends Behavior<? super E>>> immutableList) {
        this.addActivityAndRemoveMemoriesWhenStopped(activity, immutableList, ImmutableSet.of(), Sets.newHashSet());
    }

    public void addActivityWithConditions(Activity activity, ImmutableList<? extends Pair<Integer, ? extends Behavior<? super E>>> immutableList, Set<Pair<MemoryModuleType<?>, MemoryStatus>> set) {
        this.addActivityAndRemoveMemoriesWhenStopped(activity, immutableList, set, Sets.newHashSet());
    }

    private void addActivityAndRemoveMemoriesWhenStopped(Activity activity, ImmutableList<? extends Pair<Integer, ? extends Behavior<? super E>>> immutableList, Set<Pair<MemoryModuleType<?>, MemoryStatus>> set, Set<MemoryModuleType<?>> set2) {
        this.activityRequirements.put(activity, set);
        if (!set2.isEmpty()) {
            this.activityMemoriesToEraseWhenStopped.put(activity, set2);
        }
        immutableList.forEach((Consumer<Pair<Integer, Behavior<Pair>>>)((Consumer<Pair>)pair -> this.availableBehaviorsByPriority.computeIfAbsent((Integer)pair.getFirst(), (Function<Integer, Map<Activity, Set<Behavior<E>>>>)((Function<Integer, Map>)integer -> Maps.newHashMap())).computeIfAbsent(activity, activity -> Sets.newLinkedHashSet()).add(pair.getSecond())));
    }

    public boolean isActive(Activity activity) {
        return this.activeActivities.contains(activity);
    }

    public Brain<E> copyWithoutBehaviors() {
        Brain<E> brain = new Brain<E>(this.memories.keySet(), this.sensors.keySet(), new Dynamic<CompoundTag>(NbtOps.INSTANCE, new CompoundTag()));
        this.memories.forEach((memoryModuleType, optional) -> optional.ifPresent(expirableValue -> brain.memories.put((MemoryModuleType<?>)memoryModuleType, (Optional<ExpirableValue<?>>)Optional.of(expirableValue))));
        return brain;
    }

    public void tick(ServerLevel serverLevel, E livingEntity) {
        this.memories.forEach(this::tickMemoryAndRemoveIfExpired);
        this.sensors.values().forEach(sensor -> sensor.tick(serverLevel, livingEntity));
        this.startEachNonRunningBehavior(serverLevel, livingEntity);
        this.tickEachRunningBehavior(serverLevel, livingEntity);
    }

    public void stopAll(ServerLevel serverLevel, E livingEntity) {
        long l = ((LivingEntity)livingEntity).level.getGameTime();
        this.getRunningBehaviorsStream().forEach(behavior -> behavior.doStop(serverLevel, livingEntity, l));
    }

    @Override
    public <T> T serialize(DynamicOps<T> dynamicOps) {
        Object object = dynamicOps.createMap(this.memories.entrySet().stream().filter(entry -> ((MemoryModuleType)entry.getKey()).getDeserializer().isPresent() && ((Optional)entry.getValue()).isPresent()).map(entry -> Pair.of(dynamicOps.createString(Registry.MEMORY_MODULE_TYPE.getKey((MemoryModuleType<?>)entry.getKey()).toString()), ((ExpirableValue)((Optional)entry.getValue()).get()).serialize(dynamicOps))).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
        return (T)dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("memories"), object));
    }

    private void startEachNonRunningBehavior(ServerLevel serverLevel, E livingEntity) {
        long l = serverLevel.getGameTime();
        this.availableBehaviorsByPriority.values().stream().flatMap(map -> map.entrySet().stream()).filter(entry -> this.activeActivities.contains(entry.getKey())).map(Map.Entry::getValue).flatMap(Collection::stream).filter(behavior -> behavior.getStatus() == Behavior.Status.STOPPED).forEach(behavior -> behavior.tryStart(serverLevel, livingEntity, l));
    }

    private void tickEachRunningBehavior(ServerLevel serverLevel, E livingEntity) {
        long l = serverLevel.getGameTime();
        this.getRunningBehaviorsStream().forEach(behavior -> behavior.tickOrStop(serverLevel, livingEntity, l));
    }

    private void tickMemoryAndRemoveIfExpired(MemoryModuleType<?> memoryModuleType, Optional<? extends ExpirableValue<?>> optional) {
        optional.ifPresent(expirableValue -> {
            expirableValue.tick();
            if (expirableValue.hasExpired()) {
                this.eraseMemory(memoryModuleType);
            }
        });
    }

    private boolean activityRequirementsAreMet(Activity activity) {
        return this.activityRequirements.containsKey(activity) && this.activityRequirements.get(activity).stream().allMatch(pair -> {
            MemoryModuleType memoryModuleType = (MemoryModuleType)pair.getFirst();
            MemoryStatus memoryStatus = (MemoryStatus)((Object)((Object)pair.getSecond()));
            return this.checkMemory(memoryModuleType, memoryStatus);
        });
    }

    private boolean isEmptyCollection(Object object) {
        return object instanceof Collection && ((Collection)object).isEmpty();
    }

    private ImmutableList<? extends Pair<Integer, ? extends Behavior<? super E>>> createPriorityPairs(int i, ImmutableList<? extends Behavior<? super E>> immutableList) {
        MutableInt mutableInt = new MutableInt(i);
        return immutableList.stream().map(behavior -> Pair.of(mutableInt.incrementAndGet(), behavior)).collect(ImmutableList.toImmutableList());
    }
}

