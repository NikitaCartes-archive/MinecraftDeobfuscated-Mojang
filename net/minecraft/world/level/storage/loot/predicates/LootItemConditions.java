/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.predicates;

import java.util.function.Predicate;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.GsonAdapterFactory;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.predicates.AlternativeLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.ConditionReference;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;
import net.minecraft.world.level.storage.loot.predicates.EntityHasScoreCondition;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithLootingCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.predicates.TimeCheck;
import net.minecraft.world.level.storage.loot.predicates.ValueCheckCondition;
import net.minecraft.world.level.storage.loot.predicates.WeatherCheck;

public class LootItemConditions {
    public static final LootItemConditionType INVERTED = LootItemConditions.register("inverted", new InvertedLootItemCondition.Serializer());
    public static final LootItemConditionType ALTERNATIVE = LootItemConditions.register("alternative", new AlternativeLootItemCondition.Serializer());
    public static final LootItemConditionType RANDOM_CHANCE = LootItemConditions.register("random_chance", new LootItemRandomChanceCondition.Serializer());
    public static final LootItemConditionType RANDOM_CHANCE_WITH_LOOTING = LootItemConditions.register("random_chance_with_looting", new LootItemRandomChanceWithLootingCondition.Serializer());
    public static final LootItemConditionType ENTITY_PROPERTIES = LootItemConditions.register("entity_properties", new LootItemEntityPropertyCondition.Serializer());
    public static final LootItemConditionType KILLED_BY_PLAYER = LootItemConditions.register("killed_by_player", new LootItemKilledByPlayerCondition.Serializer());
    public static final LootItemConditionType ENTITY_SCORES = LootItemConditions.register("entity_scores", new EntityHasScoreCondition.Serializer());
    public static final LootItemConditionType BLOCK_STATE_PROPERTY = LootItemConditions.register("block_state_property", new LootItemBlockStatePropertyCondition.Serializer());
    public static final LootItemConditionType MATCH_TOOL = LootItemConditions.register("match_tool", new MatchTool.Serializer());
    public static final LootItemConditionType TABLE_BONUS = LootItemConditions.register("table_bonus", new BonusLevelTableCondition.Serializer());
    public static final LootItemConditionType SURVIVES_EXPLOSION = LootItemConditions.register("survives_explosion", new ExplosionCondition.Serializer());
    public static final LootItemConditionType DAMAGE_SOURCE_PROPERTIES = LootItemConditions.register("damage_source_properties", new DamageSourceCondition.Serializer());
    public static final LootItemConditionType LOCATION_CHECK = LootItemConditions.register("location_check", new LocationCheck.Serializer());
    public static final LootItemConditionType WEATHER_CHECK = LootItemConditions.register("weather_check", new WeatherCheck.Serializer());
    public static final LootItemConditionType REFERENCE = LootItemConditions.register("reference", new ConditionReference.Serializer());
    public static final LootItemConditionType TIME_CHECK = LootItemConditions.register("time_check", new TimeCheck.Serializer());
    public static final LootItemConditionType VALUE_CHECK = LootItemConditions.register("value_check", new ValueCheckCondition.Serializer());

    private static LootItemConditionType register(String string, Serializer<? extends LootItemCondition> serializer) {
        return Registry.register(Registry.LOOT_CONDITION_TYPE, new ResourceLocation(string), new LootItemConditionType(serializer));
    }

    public static Object createGsonAdapter() {
        return GsonAdapterFactory.builder(Registry.LOOT_CONDITION_TYPE, "condition", "condition", LootItemCondition::getType).build();
    }

    public static <T> Predicate<T> andConditions(Predicate<T>[] predicates) {
        switch (predicates.length) {
            case 0: {
                return object -> true;
            }
            case 1: {
                return predicates[0];
            }
            case 2: {
                return predicates[0].and(predicates[1]);
            }
        }
        return object -> {
            for (Predicate predicate : predicates) {
                if (predicate.test(object)) continue;
                return false;
            }
            return true;
        };
    }

    public static <T> Predicate<T> orConditions(Predicate<T>[] predicates) {
        switch (predicates.length) {
            case 0: {
                return object -> false;
            }
            case 1: {
                return predicates[0];
            }
            case 2: {
                return predicates[0].or(predicates[1]);
            }
        }
        return object -> {
            for (Predicate predicate : predicates) {
                if (!predicate.test(object)) continue;
                return true;
            }
            return false;
        };
    }
}

