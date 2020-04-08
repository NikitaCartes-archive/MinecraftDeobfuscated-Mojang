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
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
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
        for (MemoryModuleType<?> memoryModuleType : collection) {
            this.memories.put(memoryModuleType, Optional.empty());
        }
        for (SensorType sensorType : collection2) {
            this.sensors.put(sensorType, (Sensor<E>)sensorType.create());
        }
        for (Sensor sensor : this.sensors.values()) {
            for (MemoryModuleType<?> memoryModuleType2 : sensor.requires()) {
                this.memories.put(memoryModuleType2, Optional.empty());
            }
        }
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

    public <U> void setMemory(MemoryModuleType<U> memoryModuleType, Optional<? extends U> optional) {
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
    public List<Behavior<? super E>> getRunningBehaviors() {
        ObjectArrayList<Behavior<Behavior<E>>> list = new ObjectArrayList<Behavior<Behavior<E>>>();
        for (Map<Activity, Set<Behavior<E>>> map : this.availableBehaviorsByPriority.values()) {
            for (Set<Behavior<E>> set : map.values()) {
                for (Behavior<E> behavior : set) {
                    if (behavior.getStatus() != Behavior.Status.RUNNING) continue;
                    list.add(behavior);
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
            if (this.coreActivities.contains(activity)) continue;
            return Optional.of(activity);
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
        if (this.isActive(activity)) {
            return;
        }
        this.eraseMemoriesForOtherActivitesThan(activity);
        this.activeActivities.clear();
        this.activeActivities.addAll(this.coreActivities);
        this.activeActivities.add(activity);
    }

    private void eraseMemoriesForOtherActivitesThan(Activity activity) {
        for (Activity activity2 : this.activeActivities) {
            Set<MemoryModuleType<?>> set;
            if (activity2 == activity || (set = this.activityMemoriesToEraseWhenStopped.get(activity2)) == null) continue;
            for (MemoryModuleType<?> memoryModuleType : set) {
                this.eraseMemory(memoryModuleType);
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
            if (!this.activityRequirementsAreMet(activity)) continue;
            this.setActiveActivity(activity);
            break;
        }
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

    private void addActivityAndRemoveMemoriesWhenStopped(Activity activity2, ImmutableList<? extends Pair<Integer, ? extends Behavior<? super E>>> immutableList, Set<Pair<MemoryModuleType<?>, MemoryStatus>> set, Set<MemoryModuleType<?>> set2) {
        this.activityRequirements.put(activity2, set);
        if (!set2.isEmpty()) {
            this.activityMemoriesToEraseWhenStopped.put(activity2, set2);
        }
        for (Pair pair : immutableList) {
            this.availableBehaviorsByPriority.computeIfAbsent((Integer)pair.getFirst(), (Function<Integer, Map<Activity, Set<Behavior<E>>>>)((Function<Integer, Map>)integer -> Maps.newHashMap())).computeIfAbsent(activity2, activity -> Sets.newLinkedHashSet()).add(pair.getSecond());
        }
    }

    public boolean isActive(Activity activity) {
        return this.activeActivities.contains(activity);
    }

    public Brain<E> copyWithoutBehaviors() {
        Brain<E> brain = new Brain<E>(this.memories.keySet(), this.sensors.keySet(), new Dynamic<CompoundTag>(NbtOps.INSTANCE, new CompoundTag()));
        for (Map.Entry<MemoryModuleType<?>, Optional<ExpirableValue<?>>> entry : this.memories.entrySet()) {
            MemoryModuleType<?> memoryModuleType = entry.getKey();
            if (!entry.getValue().isPresent()) continue;
            brain.memories.put(memoryModuleType, entry.getValue());
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
        for (Sensor<E> sensor : this.sensors.values()) {
            sensor.tick(serverLevel, livingEntity);
        }
    }

    private void forgetOutdatedMemories() {
        for (Map.Entry<MemoryModuleType<?>, Optional<ExpirableValue<?>>> entry : this.memories.entrySet()) {
            if (!entry.getValue().isPresent()) continue;
            ExpirableValue<?> expirableValue = entry.getValue().get();
            expirableValue.tick();
            if (!expirableValue.hasExpired()) continue;
            this.eraseMemory(entry.getKey());
        }
    }

    public void stopAll(ServerLevel serverLevel, E livingEntity) {
        long l = ((LivingEntity)livingEntity).level.getGameTime();
        for (Behavior<E> behavior : this.getRunningBehaviors()) {
            behavior.doStop(serverLevel, livingEntity, l);
        }
    }

    @Override
    public <T> T serialize(DynamicOps<T> dynamicOps) {
        ImmutableMap.Builder<T, ?> builder = ImmutableMap.builder();
        for (Map.Entry<MemoryModuleType<?>, Optional<ExpirableValue<?>>> entry : this.memories.entrySet()) {
            MemoryModuleType<?> memoryModuleType = entry.getKey();
            if (!entry.getValue().isPresent() || !memoryModuleType.getDeserializer().isPresent()) continue;
            ExpirableValue<?> expirableValue = entry.getValue().get();
            T object = dynamicOps.createString(Registry.MEMORY_MODULE_TYPE.getKey(memoryModuleType).toString());
            Object object2 = expirableValue.serialize(dynamicOps);
            builder.put(object, object2);
        }
        return dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("memories"), dynamicOps.createMap(builder.build())));
    }

    private void startEachNonRunningBehavior(ServerLevel serverLevel, E livingEntity) {
        long l = serverLevel.getGameTime();
        for (Map<Activity, Set<Behavior<E>>> map : this.availableBehaviorsByPriority.values()) {
            for (Map.Entry<Activity, Set<Behavior<E>>> entry : map.entrySet()) {
                Activity activity = entry.getKey();
                if (!this.activeActivities.contains(activity)) continue;
                Set<Behavior<E>> set = entry.getValue();
                for (Behavior<E> behavior : set) {
                    if (behavior.getStatus() != Behavior.Status.STOPPED) continue;
                    behavior.tryStart(serverLevel, livingEntity, l);
                }
            }
        }
    }

    private void tickEachRunningBehavior(ServerLevel serverLevel, E livingEntity) {
        long l = serverLevel.getGameTime();
        for (Behavior<E> behavior : this.getRunningBehaviors()) {
            behavior.tickOrStop(serverLevel, livingEntity, l);
        }
    }

    private boolean activityRequirementsAreMet(Activity activity) {
        if (!this.activityRequirements.containsKey(activity)) {
            return false;
        }
        for (Pair<MemoryModuleType<?>, MemoryStatus> pair : this.activityRequirements.get(activity)) {
            MemoryStatus memoryStatus;
            MemoryModuleType<?> memoryModuleType = pair.getFirst();
            if (this.checkMemory(memoryModuleType, memoryStatus = pair.getSecond())) continue;
            return false;
        }
        return true;
    }

    private boolean isEmptyCollection(Object object) {
        return object instanceof Collection && ((Collection)object).isEmpty();
    }

    ImmutableList<? extends Pair<Integer, ? extends Behavior<? super E>>> createPriorityPairs(int i, ImmutableList<? extends Behavior<? super E>> immutableList) {
        int j = i;
        ImmutableList.Builder builder = ImmutableList.builder();
        for (Behavior behavior : immutableList) {
            builder.add(Pair.of(j++, behavior));
        }
        return builder.build();
    }
}

