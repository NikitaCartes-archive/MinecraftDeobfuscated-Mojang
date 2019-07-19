/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.memory;

import com.mojang.datafixers.Dynamic;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SerializableLong;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Serializable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.PositionWrapper;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;

public class MemoryModuleType<U> {
    public static final MemoryModuleType<Void> DUMMY = MemoryModuleType.register("dummy");
    public static final MemoryModuleType<GlobalPos> HOME = MemoryModuleType.register("home", Optional.of(GlobalPos::of));
    public static final MemoryModuleType<GlobalPos> JOB_SITE = MemoryModuleType.register("job_site", Optional.of(GlobalPos::of));
    public static final MemoryModuleType<GlobalPos> MEETING_POINT = MemoryModuleType.register("meeting_point", Optional.of(GlobalPos::of));
    public static final MemoryModuleType<List<GlobalPos>> SECONDARY_JOB_SITE = MemoryModuleType.register("secondary_job_site");
    public static final MemoryModuleType<List<LivingEntity>> LIVING_ENTITIES = MemoryModuleType.register("mobs");
    public static final MemoryModuleType<List<LivingEntity>> VISIBLE_LIVING_ENTITIES = MemoryModuleType.register("visible_mobs");
    public static final MemoryModuleType<List<LivingEntity>> VISIBLE_VILLAGER_BABIES = MemoryModuleType.register("visible_villager_babies");
    public static final MemoryModuleType<List<Player>> NEAREST_PLAYERS = MemoryModuleType.register("nearest_players");
    public static final MemoryModuleType<Player> NEAREST_VISIBLE_PLAYER = MemoryModuleType.register("nearest_visible_player");
    public static final MemoryModuleType<WalkTarget> WALK_TARGET = MemoryModuleType.register("walk_target");
    public static final MemoryModuleType<PositionWrapper> LOOK_TARGET = MemoryModuleType.register("look_target");
    public static final MemoryModuleType<LivingEntity> INTERACTION_TARGET = MemoryModuleType.register("interaction_target");
    public static final MemoryModuleType<Villager> BREED_TARGET = MemoryModuleType.register("breed_target");
    public static final MemoryModuleType<Path> PATH = MemoryModuleType.register("path");
    public static final MemoryModuleType<List<GlobalPos>> INTERACTABLE_DOORS = MemoryModuleType.register("interactable_doors");
    public static final MemoryModuleType<Set<GlobalPos>> OPENED_DOORS = MemoryModuleType.register("opened_doors");
    public static final MemoryModuleType<BlockPos> NEAREST_BED = MemoryModuleType.register("nearest_bed");
    public static final MemoryModuleType<DamageSource> HURT_BY = MemoryModuleType.register("hurt_by");
    public static final MemoryModuleType<LivingEntity> HURT_BY_ENTITY = MemoryModuleType.register("hurt_by_entity");
    public static final MemoryModuleType<LivingEntity> NEAREST_HOSTILE = MemoryModuleType.register("nearest_hostile");
    public static final MemoryModuleType<GlobalPos> HIDING_PLACE = MemoryModuleType.register("hiding_place");
    public static final MemoryModuleType<Long> HEARD_BELL_TIME = MemoryModuleType.register("heard_bell_time");
    public static final MemoryModuleType<Long> CANT_REACH_WALK_TARGET_SINCE = MemoryModuleType.register("cant_reach_walk_target_since");
    public static final MemoryModuleType<Long> GOLEM_LAST_SEEN_TIME = MemoryModuleType.register("golem_last_seen_time");
    public static final MemoryModuleType<SerializableLong> LAST_SLEPT = MemoryModuleType.register("last_slept", Optional.of(SerializableLong::of));
    public static final MemoryModuleType<SerializableLong> LAST_WORKED_AT_POI = MemoryModuleType.register("last_worked_at_poi", Optional.of(SerializableLong::of));
    private final Optional<Function<Dynamic<?>, U>> deserializer;

    private MemoryModuleType(Optional<Function<Dynamic<?>, U>> optional) {
        this.deserializer = optional;
    }

    public String toString() {
        return Registry.MEMORY_MODULE_TYPE.getKey(this).toString();
    }

    public Optional<Function<Dynamic<?>, U>> getDeserializer() {
        return this.deserializer;
    }

    private static <U extends Serializable> MemoryModuleType<U> register(String string, Optional<Function<Dynamic<?>, U>> optional) {
        return Registry.register(Registry.MEMORY_MODULE_TYPE, new ResourceLocation(string), new MemoryModuleType<U>(optional));
    }

    private static <U> MemoryModuleType<U> register(String string) {
        return Registry.register(Registry.MEMORY_MODULE_TYPE, new ResourceLocation(string), new MemoryModuleType<U>(Optional.empty()));
    }
}

