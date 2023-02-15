package net.minecraft.world.level.storage.loot.parameters;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;

public class LootContextParamSets {
	private static final BiMap<ResourceLocation, LootContextParamSet> REGISTRY = HashBiMap.create();
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
				.optional(LootContextParams.KILLER_ENTITY)
				.optional(LootContextParams.DIRECT_KILLER_ENTITY)
				.optional(LootContextParams.LAST_DAMAGE_PLAYER)
	);
	public static final LootContextParamSet ARCHAEOLOGY = register(
		"archaeology", builder -> builder.required(LootContextParams.ORIGIN).optional(LootContextParams.THIS_ENTITY)
	);
	public static final LootContextParamSet GIFT = register("gift", builder -> builder.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY));
	public static final LootContextParamSet PIGLIN_BARTER = register("barter", builder -> builder.required(LootContextParams.THIS_ENTITY));
	public static final LootContextParamSet ADVANCEMENT_REWARD = register(
		"advancement_reward", builder -> builder.required(LootContextParams.THIS_ENTITY).required(LootContextParams.ORIGIN)
	);
	public static final LootContextParamSet ADVANCEMENT_ENTITY = register(
		"advancement_entity", builder -> builder.required(LootContextParams.THIS_ENTITY).required(LootContextParams.ORIGIN)
	);
	public static final LootContextParamSet ALL_PARAMS = register(
		"generic",
		builder -> builder.required(LootContextParams.THIS_ENTITY)
				.required(LootContextParams.LAST_DAMAGE_PLAYER)
				.required(LootContextParams.DAMAGE_SOURCE)
				.required(LootContextParams.KILLER_ENTITY)
				.required(LootContextParams.DIRECT_KILLER_ENTITY)
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

	private static LootContextParamSet register(String string, Consumer<LootContextParamSet.Builder> consumer) {
		LootContextParamSet.Builder builder = new LootContextParamSet.Builder();
		consumer.accept(builder);
		LootContextParamSet lootContextParamSet = builder.build();
		ResourceLocation resourceLocation = new ResourceLocation(string);
		LootContextParamSet lootContextParamSet2 = REGISTRY.put(resourceLocation, lootContextParamSet);
		if (lootContextParamSet2 != null) {
			throw new IllegalStateException("Loot table parameter set " + resourceLocation + " is already registered");
		} else {
			return lootContextParamSet;
		}
	}

	@Nullable
	public static LootContextParamSet get(ResourceLocation resourceLocation) {
		return (LootContextParamSet)REGISTRY.get(resourceLocation);
	}

	@Nullable
	public static ResourceLocation getKey(LootContextParamSet lootContextParamSet) {
		return (ResourceLocation)REGISTRY.inverse().get(lootContextParamSet);
	}
}
