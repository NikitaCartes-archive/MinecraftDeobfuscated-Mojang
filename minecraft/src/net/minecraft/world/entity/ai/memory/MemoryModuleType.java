package net.minecraft.world.entity.ai.memory;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SerializableLong;
import net.minecraft.core.SerializableUUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;

public class MemoryModuleType<U> {
	public static final MemoryModuleType<Void> DUMMY = register("dummy");
	public static final MemoryModuleType<GlobalPos> HOME = register("home", GlobalPos.CODEC);
	public static final MemoryModuleType<GlobalPos> JOB_SITE = register("job_site", GlobalPos.CODEC);
	public static final MemoryModuleType<GlobalPos> POTENTIAL_JOB_SITE = register("potential_job_site", GlobalPos.CODEC);
	public static final MemoryModuleType<GlobalPos> MEETING_POINT = register("meeting_point", GlobalPos.CODEC);
	public static final MemoryModuleType<List<GlobalPos>> SECONDARY_JOB_SITE = register("secondary_job_site");
	public static final MemoryModuleType<List<LivingEntity>> LIVING_ENTITIES = register("mobs");
	public static final MemoryModuleType<List<LivingEntity>> VISIBLE_LIVING_ENTITIES = register("visible_mobs");
	public static final MemoryModuleType<List<LivingEntity>> VISIBLE_VILLAGER_BABIES = register("visible_villager_babies");
	public static final MemoryModuleType<List<Player>> NEAREST_PLAYERS = register("nearest_players");
	public static final MemoryModuleType<Player> NEAREST_VISIBLE_PLAYER = register("nearest_visible_player");
	public static final MemoryModuleType<Player> NEAREST_VISIBLE_TARGETABLE_PLAYER = register("nearest_visible_targetable_player");
	public static final MemoryModuleType<WalkTarget> WALK_TARGET = register("walk_target");
	public static final MemoryModuleType<PositionTracker> LOOK_TARGET = register("look_target");
	public static final MemoryModuleType<LivingEntity> ATTACK_TARGET = register("attack_target");
	public static final MemoryModuleType<Boolean> ATTACK_COOLING_DOWN = register("attack_cooling_down");
	public static final MemoryModuleType<LivingEntity> INTERACTION_TARGET = register("interaction_target");
	public static final MemoryModuleType<AgableMob> BREED_TARGET = register("breed_target");
	public static final MemoryModuleType<Entity> RIDE_TARGET = register("ride_target");
	public static final MemoryModuleType<Path> PATH = register("path");
	public static final MemoryModuleType<List<GlobalPos>> INTERACTABLE_DOORS = register("interactable_doors");
	public static final MemoryModuleType<Set<GlobalPos>> OPENED_DOORS = register("opened_doors");
	public static final MemoryModuleType<BlockPos> NEAREST_BED = register("nearest_bed");
	public static final MemoryModuleType<DamageSource> HURT_BY = register("hurt_by");
	public static final MemoryModuleType<LivingEntity> HURT_BY_ENTITY = register("hurt_by_entity");
	public static final MemoryModuleType<LivingEntity> AVOID_TARGET = register("avoid_target");
	public static final MemoryModuleType<LivingEntity> NEAREST_HOSTILE = register("nearest_hostile");
	public static final MemoryModuleType<GlobalPos> HIDING_PLACE = register("hiding_place");
	public static final MemoryModuleType<Long> HEARD_BELL_TIME = register("heard_bell_time");
	public static final MemoryModuleType<Long> CANT_REACH_WALK_TARGET_SINCE = register("cant_reach_walk_target_since");
	public static final MemoryModuleType<Long> GOLEM_LAST_SEEN_TIME = register("golem_last_seen_time");
	public static final MemoryModuleType<SerializableLong> LAST_SLEPT = register("last_slept", SerializableLong.CODEC);
	public static final MemoryModuleType<SerializableLong> LAST_WOKEN = register("last_woken", SerializableLong.CODEC);
	public static final MemoryModuleType<SerializableLong> LAST_WORKED_AT_POI = register("last_worked_at_poi", SerializableLong.CODEC);
	public static final MemoryModuleType<ItemEntity> NEAREST_VISIBLE_WANTED_ITEM = register("nearest_visible_wanted_item");
	public static final MemoryModuleType<SerializableUUID> ANGRY_AT = register("angry_at", SerializableUUID.CODEC);
	public static final MemoryModuleType<Boolean> ADMIRING_ITEM = register("admiring_item", Codec.BOOL);
	public static final MemoryModuleType<Boolean> ADMIRING_DISABLED = register("admiring_disabled", Codec.BOOL);
	public static final MemoryModuleType<Boolean> HUNTED_RECENTLY = register("hunted_recently", Codec.BOOL);
	public static final MemoryModuleType<BlockPos> CELEBRATE_LOCATION = register("celebrate_location");
	public static final MemoryModuleType<Boolean> DANCING = register("dancing");
	public static final MemoryModuleType<WitherSkeleton> NEAREST_VISIBLE_WITHER_SKELETON = register("nearest_visible_wither_skeleton");
	public static final MemoryModuleType<Hoglin> NEAREST_VISIBLE_HUNTABLE_HOGLIN = register("nearest_visible_huntable_hoglin");
	public static final MemoryModuleType<Hoglin> NEAREST_VISIBLE_BABY_HOGLIN = register("nearest_visible_baby_hoglin");
	public static final MemoryModuleType<Piglin> NEAREST_VISIBLE_BABY_PIGLIN = register("nearest_visible_baby_piglin");
	public static final MemoryModuleType<Player> NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD = register("nearest_targetable_player_not_wearing_gold");
	public static final MemoryModuleType<List<Piglin>> NEAREST_ADULT_PIGLINS = register("nearest_adult_piglins");
	public static final MemoryModuleType<List<Piglin>> NEAREST_VISIBLE_ADULT_PIGLINS = register("nearest_visible_adult_piglins");
	public static final MemoryModuleType<List<Hoglin>> NEAREST_VISIBLE_ADULT_HOGLINS = register("nearest_visible_adult_hoglins");
	public static final MemoryModuleType<Piglin> NEAREST_VISIBLE_ADULT_PIGLIN = register("nearest_visible_adult_piglin");
	public static final MemoryModuleType<LivingEntity> NEAREST_VISIBLE_ZOMBIFIED = register("nearest_visible_zombified");
	public static final MemoryModuleType<Integer> VISIBLE_ADULT_PIGLIN_COUNT = register("visible_adult_piglin_count");
	public static final MemoryModuleType<Integer> VISIBLE_ADULT_HOGLIN_COUNT = register("visible_adult_hoglin_count");
	public static final MemoryModuleType<Player> NEAREST_PLAYER_HOLDING_WANTED_ITEM = register("nearest_player_holding_wanted_item");
	public static final MemoryModuleType<Boolean> ATE_RECENTLY = register("ate_recently");
	public static final MemoryModuleType<BlockPos> NEAREST_REPELLENT = register("nearest_repellent");
	public static final MemoryModuleType<Boolean> PACIFIED = register("pacified");
	private final Optional<Codec<ExpirableValue<U>>> codec;

	private MemoryModuleType(Optional<Codec<U>> optional) {
		this.codec = optional.map(ExpirableValue::codec);
	}

	public String toString() {
		return Registry.MEMORY_MODULE_TYPE.getKey(this).toString();
	}

	public Optional<Codec<ExpirableValue<U>>> getCodec() {
		return this.codec;
	}

	private static <U> MemoryModuleType<U> register(String string, Codec<U> codec) {
		return Registry.register(Registry.MEMORY_MODULE_TYPE, new ResourceLocation(string), new MemoryModuleType<>(Optional.of(codec)));
	}

	private static <U> MemoryModuleType<U> register(String string) {
		return Registry.register(Registry.MEMORY_MODULE_TYPE, new ResourceLocation(string), new MemoryModuleType<>(Optional.empty()));
	}
}
