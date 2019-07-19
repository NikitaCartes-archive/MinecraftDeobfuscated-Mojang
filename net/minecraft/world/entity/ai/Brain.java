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
import java.util.Map;
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
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import org.jetbrains.annotations.Nullable;

public class Brain<E extends LivingEntity>
implements Serializable {
    private final Map<MemoryModuleType<?>, Optional<?>> memories = Maps.newHashMap();
    private final Map<SensorType<? extends Sensor<? super E>>, Sensor<? super E>> sensors = Maps.newLinkedHashMap();
    private final Map<Integer, Map<Activity, Set<Behavior<? super E>>>> availableGoalsByPriority = Maps.newTreeMap();
    private Schedule schedule = Schedule.EMPTY;
    private final Map<Activity, Set<Pair<MemoryModuleType<?>, MemoryStatus>>> activityRequirements = Maps.newHashMap();
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
        this.setMemory(memoryModuleType, memoryModuleType.getDeserializer().orElseThrow(RuntimeException::new).apply(dynamic));
    }

    public <U> void eraseMemory(MemoryModuleType<U> memoryModuleType) {
        this.setMemory(memoryModuleType, Optional.empty());
    }

    public <U> void setMemory(MemoryModuleType<U> memoryModuleType, @Nullable U object) {
        this.setMemory(memoryModuleType, Optional.ofNullable(object));
    }

    public <U> void setMemory(MemoryModuleType<U> memoryModuleType, Optional<U> optional) {
        if (this.memories.containsKey(memoryModuleType)) {
            if (optional.isPresent() && this.isEmptyCollection(optional.get())) {
                this.eraseMemory(memoryModuleType);
            } else {
                this.memories.put(memoryModuleType, optional);
            }
        }
    }

    public <U> Optional<U> getMemory(MemoryModuleType<U> memoryModuleType) {
        return this.memories.get(memoryModuleType);
    }

    public boolean checkMemory(MemoryModuleType<?> memoryModuleType, MemoryStatus memoryStatus) {
        Optional<?> optional = this.memories.get(memoryModuleType);
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
        return this.availableGoalsByPriority.values().stream().flatMap(map -> map.values().stream()).flatMap(Collection::stream).filter(behavior -> behavior.getStatus() == Behavior.Status.RUNNING);
    }

    public void setActivity(Activity activity) {
        this.activeActivities.clear();
        this.activeActivities.addAll(this.coreActivities);
        boolean bl = this.activityRequirements.keySet().contains(activity) && this.activityRequirementsAreMet(activity);
        this.activeActivities.add(bl ? activity : this.defaultActivity);
    }

    public void updateActivity(long l, long m) {
        if (m - this.lastScheduleUpdate > 20L) {
            this.lastScheduleUpdate = m;
            Activity activity = this.getSchedule().getActivityAt((int)(l % 24000L));
            if (!this.activeActivities.contains(activity)) {
                this.setActivity(activity);
            }
        }
    }

    public void setDefaultActivity(Activity activity) {
        this.defaultActivity = activity;
    }

    public void addActivity(Activity activity, ImmutableList<Pair<Integer, ? extends Behavior<? super E>>> immutableList) {
        this.addActivity(activity, immutableList, ImmutableSet.of());
    }

    public void addActivity(Activity activity, ImmutableList<Pair<Integer, ? extends Behavior<? super E>>> immutableList, Set<Pair<MemoryModuleType<?>, MemoryStatus>> set) {
        this.activityRequirements.put(activity, set);
        immutableList.forEach((Consumer<Pair<Integer, Behavior<Pair>>>)((Consumer<Pair>)pair -> this.availableGoalsByPriority.computeIfAbsent((Integer)pair.getFirst(), (Function<Integer, Map<Activity, Set<Behavior<E>>>>)((Function<Integer, Map>)integer -> Maps.newHashMap())).computeIfAbsent(activity, activity -> Sets.newLinkedHashSet()).add(pair.getSecond())));
    }

    public boolean isActive(Activity activity) {
        return this.activeActivities.contains(activity);
    }

    public Brain<E> copyWithoutGoals() {
        Brain<E> brain = new Brain<E>(this.memories.keySet(), this.sensors.keySet(), new Dynamic<CompoundTag>(NbtOps.INSTANCE, new CompoundTag()));
        this.memories.forEach((memoryModuleType, optional) -> optional.ifPresent(object -> brain.memories.put((MemoryModuleType<?>)memoryModuleType, Optional.of(object))));
        return brain;
    }

    public void tick(ServerLevel serverLevel, E livingEntity) {
        this.tickEachSensor(serverLevel, livingEntity);
        this.startEachNonRunningBehavior(serverLevel, livingEntity);
        this.tickEachRunningBehavior(serverLevel, livingEntity);
    }

    public void stopAll(ServerLevel serverLevel, E livingEntity) {
        long l = ((LivingEntity)livingEntity).level.getGameTime();
        this.getRunningBehaviorsStream().forEach(behavior -> behavior.doStop(serverLevel, livingEntity, l));
    }

    @Override
    public <T> T serialize(DynamicOps<T> dynamicOps) {
        Object object = dynamicOps.createMap(this.memories.entrySet().stream().filter(entry -> ((MemoryModuleType)entry.getKey()).getDeserializer().isPresent() && ((Optional)entry.getValue()).isPresent()).map(entry -> Pair.of(dynamicOps.createString(Registry.MEMORY_MODULE_TYPE.getKey((MemoryModuleType<?>)entry.getKey()).toString()), ((Serializable)((Optional)entry.getValue()).get()).serialize(dynamicOps))).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
        return (T)dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("memories"), object));
    }

    private void tickEachSensor(ServerLevel serverLevel, E livingEntity) {
        this.sensors.values().forEach(sensor -> sensor.tick(serverLevel, livingEntity));
    }

    private void startEachNonRunningBehavior(ServerLevel serverLevel, E livingEntity) {
        long l = serverLevel.getGameTime();
        this.availableGoalsByPriority.values().stream().flatMap(map -> map.entrySet().stream()).filter(entry -> this.activeActivities.contains(entry.getKey())).map(Map.Entry::getValue).flatMap(Collection::stream).filter(behavior -> behavior.getStatus() == Behavior.Status.STOPPED).forEach(behavior -> behavior.tryStart(serverLevel, livingEntity, l));
    }

    private void tickEachRunningBehavior(ServerLevel serverLevel, E livingEntity) {
        long l = serverLevel.getGameTime();
        this.getRunningBehaviorsStream().forEach(behavior -> behavior.tickOrStop(serverLevel, livingEntity, l));
    }

    private boolean activityRequirementsAreMet(Activity activity) {
        return this.activityRequirements.get(activity).stream().allMatch(pair -> {
            MemoryModuleType memoryModuleType = (MemoryModuleType)pair.getFirst();
            MemoryStatus memoryStatus = (MemoryStatus)((Object)((Object)pair.getSecond()));
            return this.checkMemory(memoryModuleType, memoryStatus);
        });
    }

    private boolean isEmptyCollection(Object object) {
        return object instanceof Collection && ((Collection)object).isEmpty();
    }
}

