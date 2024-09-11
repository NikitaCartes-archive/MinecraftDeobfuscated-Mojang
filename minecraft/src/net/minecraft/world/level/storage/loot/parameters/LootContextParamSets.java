package net.minecraft.world.level.storage.loot.parameters;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;

public class LootContextParamSets {
	private static final BiMap<ResourceLocation, LootContextParamSet> REGISTRY = HashBiMap.create();
	public static final Codec<LootContextParamSet> CODEC = ResourceLocation.CODEC
		.comapFlatMap(
			resourceLocation -> (DataResult)Optional.ofNullable((LootContextParamSet)REGISTRY.get(resourceLocation))
					.map(DataResult::success)
					.orElseGet(() -> DataResult.error(() -> "No parameter set exists with id: '" + resourceLocation + "'")),
			REGISTRY.inverse()::get
		);
	public static final LootContextParamSet EMPTY = register("empty", builder -> {
	});
	public static final LootContextParamSet CHEST = register(
		"chest", builder -> builder.required(LootContextParams.ORIGIN).optional(LootContextParams.THIS_ENTITY)
	);
	public static final LootContextParamSet COMMAND = register(
		"command", builder -> builder.required(LootContextParams.ORIGIN).optional(LootContextParams.THIS_ENTITY)
	);
	public static final LootContextParamSet SELECTOR = register(
		"selector", builder -> builder.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY)
	);
	public static final LootContextParamSet FISHING = register(
		"fishing", builder -> builder.required(LootContextParams.ORIGIN).required(LootContextParams.TOOL).optional(LootContextParams.THIS_ENTITY)
	);
	public static final LootContextParamSet ENTITY = register(
		"entity",
		builder -> builder.required(LootContextParams.THIS_ENTITY)
				.required(LootContextParams.ORIGIN)
				.required(LootContextParams.DAMAGE_SOURCE)
				.optional(LootContextParams.ATTACKING_ENTITY)
				.optional(LootContextParams.DIRECT_ATTACKING_ENTITY)
				.optional(LootContextParams.LAST_DAMAGE_PLAYER)
	);
	public static final LootContextParamSet EQUIPMENT = register(
		"equipment", builder -> builder.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY)
	);
	public static final LootContextParamSet ARCHAEOLOGY = register(
		"archaeology", builder -> builder.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY).required(LootContextParams.TOOL)
	);
	public static final LootContextParamSet GIFT = register("gift", builder -> builder.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY));
	public static final LootContextParamSet PIGLIN_BARTER = register("barter", builder -> builder.required(LootContextParams.THIS_ENTITY));
	public static final LootContextParamSet VAULT = register(
		"vault", builder -> builder.required(LootContextParams.ORIGIN).optional(LootContextParams.THIS_ENTITY).optional(LootContextParams.TOOL)
	);
	public static final LootContextParamSet ADVANCEMENT_REWARD = register(
		"advancement_reward", builder -> builder.required(LootContextParams.THIS_ENTITY).required(LootContextParams.ORIGIN)
	);
	public static final LootContextParamSet ADVANCEMENT_ENTITY = register(
		"advancement_entity", builder -> builder.required(LootContextParams.THIS_ENTITY).required(LootContextParams.ORIGIN)
	);
	public static final LootContextParamSet ADVANCEMENT_LOCATION = register(
		"advancement_location",
		builder -> builder.required(LootContextParams.THIS_ENTITY)
				.required(LootContextParams.ORIGIN)
				.required(LootContextParams.TOOL)
				.required(LootContextParams.BLOCK_STATE)
	);
	public static final LootContextParamSet BLOCK_USE = register(
		"block_use", builder -> builder.required(LootContextParams.THIS_ENTITY).required(LootContextParams.ORIGIN).required(LootContextParams.BLOCK_STATE)
	);
	public static final LootContextParamSet ALL_PARAMS = register(
		"generic",
		builder -> builder.required(LootContextParams.THIS_ENTITY)
				.required(LootContextParams.LAST_DAMAGE_PLAYER)
				.required(LootContextParams.DAMAGE_SOURCE)
				.required(LootContextParams.ATTACKING_ENTITY)
				.required(LootContextParams.DIRECT_ATTACKING_ENTITY)
				.required(LootContextParams.ORIGIN)
				.required(LootContextParams.BLOCK_STATE)
				.required(LootContextParams.BLOCK_ENTITY)
				.required(LootContextParams.TOOL)
				.required(LootContextParams.EXPLOSION_RADIUS)
	);
	public static final LootContextParamSet BLOCK = register(
		"block",
		builder -> builder.required(LootContextParams.BLOCK_STATE)
				.required(LootContextParams.ORIGIN)
				.required(LootContextParams.TOOL)
				.optional(LootContextParams.THIS_ENTITY)
				.optional(LootContextParams.BLOCK_ENTITY)
				.optional(LootContextParams.EXPLOSION_RADIUS)
	);
	public static final LootContextParamSet SHEARING = register(
		"shearing", builder -> builder.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY).required(LootContextParams.TOOL)
	);
	public static final LootContextParamSet ENCHANTED_DAMAGE = register(
		"enchanted_damage",
		builder -> builder.required(LootContextParams.THIS_ENTITY)
				.required(LootContextParams.ENCHANTMENT_LEVEL)
				.required(LootContextParams.ORIGIN)
				.required(LootContextParams.DAMAGE_SOURCE)
				.optional(LootContextParams.DIRECT_ATTACKING_ENTITY)
				.optional(LootContextParams.ATTACKING_ENTITY)
	);
	public static final LootContextParamSet ENCHANTED_ITEM = register(
		"enchanted_item", builder -> builder.required(LootContextParams.TOOL).required(LootContextParams.ENCHANTMENT_LEVEL)
	);
	public static final LootContextParamSet ENCHANTED_LOCATION = register(
		"enchanted_location",
		builder -> builder.required(LootContextParams.THIS_ENTITY)
				.required(LootContextParams.ENCHANTMENT_LEVEL)
				.required(LootContextParams.ORIGIN)
				.required(LootContextParams.ENCHANTMENT_ACTIVE)
	);
	public static final LootContextParamSet ENCHANTED_ENTITY = register(
		"enchanted_entity",
		builder -> builder.required(LootContextParams.THIS_ENTITY).required(LootContextParams.ENCHANTMENT_LEVEL).required(LootContextParams.ORIGIN)
	);
	public static final LootContextParamSet HIT_BLOCK = register(
		"hit_block",
		builder -> builder.required(LootContextParams.THIS_ENTITY)
				.required(LootContextParams.ENCHANTMENT_LEVEL)
				.required(LootContextParams.ORIGIN)
				.required(LootContextParams.BLOCK_STATE)
	);

	private static LootContextParamSet register(String string, Consumer<LootContextParamSet.Builder> consumer) {
		LootContextParamSet.Builder builder = new LootContextParamSet.Builder();
		consumer.accept(builder);
		LootContextParamSet lootContextParamSet = builder.build();
		ResourceLocation resourceLocation = ResourceLocation.withDefaultNamespace(string);
		LootContextParamSet lootContextParamSet2 = REGISTRY.put(resourceLocation, lootContextParamSet);
		if (lootContextParamSet2 != null) {
			throw new IllegalStateException("Loot table parameter set " + resourceLocation + " is already registered");
		} else {
			return lootContextParamSet;
		}
	}
}
