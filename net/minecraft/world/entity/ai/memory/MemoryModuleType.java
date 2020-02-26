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
import net.minecraft.core.SerializableUUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Serializable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.PositionWrapper;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
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
    public static final MemoryModuleType<Player> NEAREST_VISIBLE_TARGETABLE_PLAYER = MemoryModuleType.register("nearest_visible_targetable_player");
    public static final MemoryModuleType<WalkTarget> WALK_TARGET = MemoryModuleType.register("walk_target");
    public static final MemoryModuleType<PositionWrapper> LOOK_TARGET = MemoryModuleType.register("look_target");
    public static final MemoryModuleType<LivingEntity> ATTACK_TARGET = MemoryModuleType.register("attack_target");
    public static final MemoryModuleType<LivingEntity> INTERACTION_TARGET = MemoryModuleType.register("interaction_target");
    public static final MemoryModuleType<AgableMob> BREED_TARGET = MemoryModuleType.register("breed_target");
    public static final MemoryModuleType<Entity> RIDE_TARGET = MemoryModuleType.register("ride_target");
    public static final MemoryModuleType<Path> PATH = MemoryModuleType.register("path");
    public static final MemoryModuleType<List<GlobalPos>> INTERACTABLE_DOORS = MemoryModuleType.register("interactable_doors");
    public static final MemoryModuleType<Set<GlobalPos>> OPENED_DOORS = MemoryModuleType.register("opened_doors");
    public static final MemoryModuleType<BlockPos> NEAREST_BED = MemoryModuleType.register("nearest_bed");
    public static final MemoryModuleType<DamageSource> HURT_BY = MemoryModuleType.register("hurt_by");
    public static final MemoryModuleType<LivingEntity> HURT_BY_ENTITY = MemoryModuleType.register("hurt_by_entity");
    public static final MemoryModuleType<LivingEntity> AVOID_TARGET = MemoryModuleType.register("avoid_target");
    public static final MemoryModuleType<LivingEntity> NEAREST_HOSTILE = MemoryModuleType.register("nearest_hostile");
    public static final MemoryModuleType<GlobalPos> HIDING_PLACE = MemoryModuleType.register("hiding_place");
    public static final MemoryModuleType<Long> HEARD_BELL_TIME = MemoryModuleType.register("heard_bell_time");
    public static final MemoryModuleType<Long> CANT_REACH_WALK_TARGET_SINCE = MemoryModuleType.register("cant_reach_walk_target_since");
    public static final MemoryModuleType<Long> GOLEM_LAST_SEEN_TIME = MemoryModuleType.register("golem_last_seen_time");
    public static final MemoryModuleType<SerializableLong> LAST_SLEPT = MemoryModuleType.register("last_slept", Optional.of(SerializableLong::of));
    public static final MemoryModuleType<SerializableLong> LAST_WOKEN = MemoryModuleType.register("last_woken", Optional.of(SerializableLong::of));
    public static final MemoryModuleType<SerializableLong> LAST_WORKED_AT_POI = MemoryModuleType.register("last_worked_at_poi", Optional.of(SerializableLong::of));
    public static final MemoryModuleType<ItemEntity> NEAREST_VISIBLE_WANTED_ITEM = MemoryModuleType.register("nearest_visible_wanted_item");
    public static final MemoryModuleType<SerializableUUID> ANGRY_AT = MemoryModuleType.register("angry_at", Optional.of(SerializableUUID::of));
    public static final MemoryModuleType<Boolean> ADMIRING_ITEM = MemoryModuleType.register("admiring_item");
    public static final MemoryModuleType<Player> WAS_HIT_BY_PLAYER = MemoryModuleType.register("was_hit_by_player");
    public static final MemoryModuleType<Boolean> HUNTED_RECENTLY = MemoryModuleType.register("hunted_recently");
    public static final MemoryModuleType<BlockPos> CELEBRATE_LOCATION = MemoryModuleType.register("celebrate_location");
    public static final MemoryModuleType<WitherSkeleton> NEAREST_VISIBLE_WITHER_SKELETON = MemoryModuleType.register("nearest_visible_wither_skeleton");
    public static final MemoryModuleType<Hoglin> NEAREST_VISIBLE_ADULT_HOGLIN = MemoryModuleType.register("nearest_visible_adult_hoglin");
    public static final MemoryModuleType<Hoglin> NEAREST_VISIBLE_BABY_HOGLIN = MemoryModuleType.register("nearest_visible_baby_hoglin");
    public static final MemoryModuleType<Piglin> NEAREST_VISIBLE_BABY_PIGLIN = MemoryModuleType.register("nearest_visible_baby_piglin");
    public static final MemoryModuleType<Player> NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD = MemoryModuleType.register("nearest_targetable_player_not_wearing_gold");
    public static final MemoryModuleType<List<Piglin>> NEAREST_VISIBLE_ADULT_PIGLINS = MemoryModuleType.register("nearest_visible_adult_piglins");
    public static final MemoryModuleType<List<Hoglin>> NEAREST_VISIBLE_ADULT_HOGLINS = MemoryModuleType.register("nearest_visible_adult_hoglins");
    public static final MemoryModuleType<Piglin> NEAREST_VISIBLE_ADULT_PIGLIN = MemoryModuleType.register("nearest_visible_adult_piglin");
    public static final MemoryModuleType<ZombifiedPiglin> NEAREST_VISIBLE_ZOMBIFIED_PIGLIN = MemoryModuleType.register("nearest_visible_zombified_piglin");
    public static final MemoryModuleType<Integer> VISIBLE_ADULT_PIGLIN_COUNT = MemoryModuleType.register("visible_adult_piglin_count");
    public static final MemoryModuleType<Integer> VISIBLE_ADULT_HOGLIN_COUNT = MemoryModuleType.register("visible_adult_hoglin_count");
    public static final MemoryModuleType<Player> NEAREST_PLAYER_HOLDING_WANTED_ITEM = MemoryModuleType.register("nearest_player_holding_wanted_item");
    public static final MemoryModuleType<Boolean> ATE_RECENTLY = MemoryModuleType.register("ate_recently");
    public static final MemoryModuleType<BlockPos> NEAREST_WARPED_FUNGUS = MemoryModuleType.register("nearest_visible_warped_fungus");
    public static final MemoryModuleType<BlockPos> NEAREST_SOUL_FIRE = MemoryModuleType.register("nearest_visible_soul_fire_item");
    public static final MemoryModuleType<Boolean> PACIFIED = MemoryModuleType.register("pacified");
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

