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
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;

public class MemoryModuleType<U> {
	public static final MemoryModuleType<Void> DUMMY = register("dummy");
	public static final MemoryModuleType<GlobalPos> HOME = register("home", Optional.of(GlobalPos::of));
	public static final MemoryModuleType<GlobalPos> JOB_SITE = register("job_site", Optional.of(GlobalPos::of));
	public static final MemoryModuleType<GlobalPos> MEETING_POINT = register("meeting_point", Optional.of(GlobalPos::of));
	public static final MemoryModuleType<List<GlobalPos>> SECONDARY_JOB_SITE = register("secondary_job_site");
	public static final MemoryModuleType<List<LivingEntity>> LIVING_ENTITIES = register("mobs");
	public static final MemoryModuleType<List<LivingEntity>> VISIBLE_LIVING_ENTITIES = register("visible_mobs");
	public static final MemoryModuleType<List<LivingEntity>> VISIBLE_VILLAGER_BABIES = register("visible_villager_babies");
	public static final MemoryModuleType<List<Player>> NEAREST_PLAYERS = register("nearest_players");
	public static final MemoryModuleType<Player> NEAREST_VISIBLE_PLAYER = register("nearest_visible_player");
	public static final MemoryModuleType<WalkTarget> WALK_TARGET = register("walk_target");
	public static final MemoryModuleType<PositionWrapper> LOOK_TARGET = register("look_target");
	public static final MemoryModuleType<LivingEntity> INTERACTION_TARGET = register("interaction_target");
	public static final MemoryModuleType<Villager> BREED_TARGET = register("breed_target");
	public static final MemoryModuleType<Path> PATH = register("path");
	public static final MemoryModuleType<List<GlobalPos>> INTERACTABLE_DOORS = register("interactable_doors");
	public static final MemoryModuleType<Set<GlobalPos>> OPENED_DOORS = register("opened_doors");
	public static final MemoryModuleType<BlockPos> NEAREST_BED = register("nearest_bed");
	public static final MemoryModuleType<DamageSource> HURT_BY = register("hurt_by");
	public static final MemoryModuleType<LivingEntity> HURT_BY_ENTITY = register("hurt_by_entity");
	public static final MemoryModuleType<LivingEntity> NEAREST_HOSTILE = register("nearest_hostile");
	public static final MemoryModuleType<GlobalPos> HIDING_PLACE = register("hiding_place");
	public static final MemoryModuleType<Long> HEARD_BELL_TIME = register("heard_bell_time");
	public static final MemoryModuleType<Long> CANT_REACH_WALK_TARGET_SINCE = register("cant_reach_walk_target_since");
	public static final MemoryModuleType<Long> GOLEM_LAST_SEEN_TIME = register("golem_last_seen_time");
	public static final MemoryModuleType<SerializableLong> LAST_SLEPT = register("last_slept", Optional.of(SerializableLong::of));
	public static final MemoryModuleType<SerializableLong> LAST_WORKED_AT_POI = register("last_worked_at_poi", Optional.of(SerializableLong::of));
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
		return Registry.register(Registry.MEMORY_MODULE_TYPE, new ResourceLocation(string), new MemoryModuleType<>(optional));
	}

	private static <U> MemoryModuleType<U> register(String string) {
		return Registry.register(Registry.MEMORY_MODULE_TYPE, new ResourceLocation(string), new MemoryModuleType<>(Optional.empty()));
	}
}
