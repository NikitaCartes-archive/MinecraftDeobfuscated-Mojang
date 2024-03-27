package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;

public class LootItemConditions {
	private static final Codec<LootItemCondition> TYPED_CODEC = BuiltInRegistries.LOOT_CONDITION_TYPE
		.byNameCodec()
		.dispatch("condition", LootItemCondition::getType, LootItemConditionType::codec);
	public static final Codec<LootItemCondition> DIRECT_CODEC = Codec.lazyInitialized(() -> Codec.withAlternative(TYPED_CODEC, AllOfCondition.INLINE_CODEC));
	public static final Codec<Holder<LootItemCondition>> CODEC = RegistryFileCodec.create(Registries.PREDICATE, DIRECT_CODEC);
	public static final LootItemConditionType INVERTED = register("inverted", InvertedLootItemCondition.CODEC);
	public static final LootItemConditionType ANY_OF = register("any_of", AnyOfCondition.CODEC);
	public static final LootItemConditionType ALL_OF = register("all_of", AllOfCondition.CODEC);
	public static final LootItemConditionType RANDOM_CHANCE = register("random_chance", LootItemRandomChanceCondition.CODEC);
	public static final LootItemConditionType RANDOM_CHANCE_WITH_LOOTING = register("random_chance_with_looting", LootItemRandomChanceWithLootingCondition.CODEC);
	public static final LootItemConditionType ENTITY_PROPERTIES = register("entity_properties", LootItemEntityPropertyCondition.CODEC);
	public static final LootItemConditionType KILLED_BY_PLAYER = register("killed_by_player", LootItemKilledByPlayerCondition.CODEC);
	public static final LootItemConditionType ENTITY_SCORES = register("entity_scores", EntityHasScoreCondition.CODEC);
	public static final LootItemConditionType BLOCK_STATE_PROPERTY = register("block_state_property", LootItemBlockStatePropertyCondition.CODEC);
	public static final LootItemConditionType MATCH_TOOL = register("match_tool", MatchTool.CODEC);
	public static final LootItemConditionType TABLE_BONUS = register("table_bonus", BonusLevelTableCondition.CODEC);
	public static final LootItemConditionType SURVIVES_EXPLOSION = register("survives_explosion", ExplosionCondition.CODEC);
	public static final LootItemConditionType DAMAGE_SOURCE_PROPERTIES = register("damage_source_properties", DamageSourceCondition.CODEC);
	public static final LootItemConditionType LOCATION_CHECK = register("location_check", LocationCheck.CODEC);
	public static final LootItemConditionType WEATHER_CHECK = register("weather_check", WeatherCheck.CODEC);
	public static final LootItemConditionType REFERENCE = register("reference", ConditionReference.CODEC);
	public static final LootItemConditionType TIME_CHECK = register("time_check", TimeCheck.CODEC);
	public static final LootItemConditionType VALUE_CHECK = register("value_check", ValueCheckCondition.CODEC);

	private static LootItemConditionType register(String string, MapCodec<? extends LootItemCondition> mapCodec) {
		return Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, new ResourceLocation(string), new LootItemConditionType(mapCodec));
	}
}
