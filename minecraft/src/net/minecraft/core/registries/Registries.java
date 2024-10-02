package net.minecraft.core.registries;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.numbers.NumberFormatType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.StatType;
import net.minecraft.util.valueproviders.FloatProviderType;
import net.minecraft.util.valueproviders.IntProviderType;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.entity.animal.WolfVariant;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentLocationBasedEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.DecoratedPotPattern;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerConfig;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.PositionSourceType;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSizeType;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.rootplacers.RootPlacerType;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBinding;
import net.minecraft.world.level.levelgen.structure.templatesystem.PosRuleTestType;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity.RuleBlockEntityModifierType;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.providers.nbt.LootNbtProviderType;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.score.LootScoreProviderType;

public class Registries {
	public static final ResourceLocation ROOT_REGISTRY_NAME = ResourceLocation.withDefaultNamespace("root");
	public static final ResourceKey<Registry<Activity>> ACTIVITY = createRegistryKey("activity");
	public static final ResourceKey<Registry<Attribute>> ATTRIBUTE = createRegistryKey("attribute");
	public static final ResourceKey<Registry<BannerPattern>> BANNER_PATTERN = createRegistryKey("banner_pattern");
	public static final ResourceKey<Registry<MapCodec<? extends BiomeSource>>> BIOME_SOURCE = createRegistryKey("worldgen/biome_source");
	public static final ResourceKey<Registry<Block>> BLOCK = createRegistryKey("block");
	public static final ResourceKey<Registry<MapCodec<? extends Block>>> BLOCK_TYPE = createRegistryKey("block_type");
	public static final ResourceKey<Registry<BlockEntityType<?>>> BLOCK_ENTITY_TYPE = createRegistryKey("block_entity_type");
	public static final ResourceKey<Registry<BlockPredicateType<?>>> BLOCK_PREDICATE_TYPE = createRegistryKey("block_predicate_type");
	public static final ResourceKey<Registry<BlockStateProviderType<?>>> BLOCK_STATE_PROVIDER_TYPE = createRegistryKey("worldgen/block_state_provider_type");
	public static final ResourceKey<Registry<WorldCarver<?>>> CARVER = createRegistryKey("worldgen/carver");
	public static final ResourceKey<Registry<CatVariant>> CAT_VARIANT = createRegistryKey("cat_variant");
	public static final ResourceKey<Registry<WolfVariant>> WOLF_VARIANT = createRegistryKey("wolf_variant");
	public static final ResourceKey<Registry<MapCodec<? extends ChunkGenerator>>> CHUNK_GENERATOR = createRegistryKey("worldgen/chunk_generator");
	public static final ResourceKey<Registry<ChunkStatus>> CHUNK_STATUS = createRegistryKey("chunk_status");
	public static final ResourceKey<Registry<ArgumentTypeInfo<?, ?>>> COMMAND_ARGUMENT_TYPE = createRegistryKey("command_argument_type");
	public static final ResourceKey<Registry<CreativeModeTab>> CREATIVE_MODE_TAB = createRegistryKey("creative_mode_tab");
	public static final ResourceKey<Registry<ResourceLocation>> CUSTOM_STAT = createRegistryKey("custom_stat");
	public static final ResourceKey<Registry<DamageType>> DAMAGE_TYPE = createRegistryKey("damage_type");
	public static final ResourceKey<Registry<MapCodec<? extends DensityFunction>>> DENSITY_FUNCTION_TYPE = createRegistryKey("worldgen/density_function_type");
	public static final ResourceKey<Registry<MapCodec<? extends EnchantmentEntityEffect>>> ENCHANTMENT_ENTITY_EFFECT_TYPE = createRegistryKey(
		"enchantment_entity_effect_type"
	);
	public static final ResourceKey<Registry<MapCodec<? extends LevelBasedValue>>> ENCHANTMENT_LEVEL_BASED_VALUE_TYPE = createRegistryKey(
		"enchantment_level_based_value_type"
	);
	public static final ResourceKey<Registry<MapCodec<? extends EnchantmentLocationBasedEffect>>> ENCHANTMENT_LOCATION_BASED_EFFECT_TYPE = createRegistryKey(
		"enchantment_location_based_effect_type"
	);
	public static final ResourceKey<Registry<MapCodec<? extends EnchantmentProvider>>> ENCHANTMENT_PROVIDER_TYPE = createRegistryKey("enchantment_provider_type");
	public static final ResourceKey<Registry<MapCodec<? extends EnchantmentValueEffect>>> ENCHANTMENT_VALUE_EFFECT_TYPE = createRegistryKey(
		"enchantment_value_effect_type"
	);
	public static final ResourceKey<Registry<EntityType<?>>> ENTITY_TYPE = createRegistryKey("entity_type");
	public static final ResourceKey<Registry<Feature<?>>> FEATURE = createRegistryKey("worldgen/feature");
	public static final ResourceKey<Registry<FeatureSizeType<?>>> FEATURE_SIZE_TYPE = createRegistryKey("worldgen/feature_size_type");
	public static final ResourceKey<Registry<FloatProviderType<?>>> FLOAT_PROVIDER_TYPE = createRegistryKey("float_provider_type");
	public static final ResourceKey<Registry<Fluid>> FLUID = createRegistryKey("fluid");
	public static final ResourceKey<Registry<FoliagePlacerType<?>>> FOLIAGE_PLACER_TYPE = createRegistryKey("worldgen/foliage_placer_type");
	public static final ResourceKey<Registry<FrogVariant>> FROG_VARIANT = createRegistryKey("frog_variant");
	public static final ResourceKey<Registry<GameEvent>> GAME_EVENT = createRegistryKey("game_event");
	public static final ResourceKey<Registry<HeightProviderType<?>>> HEIGHT_PROVIDER_TYPE = createRegistryKey("height_provider_type");
	public static final ResourceKey<Registry<Instrument>> INSTRUMENT = createRegistryKey("instrument");
	public static final ResourceKey<Registry<IntProviderType<?>>> INT_PROVIDER_TYPE = createRegistryKey("int_provider_type");
	public static final ResourceKey<Registry<Item>> ITEM = createRegistryKey("item");
	public static final ResourceKey<Registry<JukeboxSong>> JUKEBOX_SONG = createRegistryKey("jukebox_song");
	public static final ResourceKey<Registry<LootItemConditionType>> LOOT_CONDITION_TYPE = createRegistryKey("loot_condition_type");
	public static final ResourceKey<Registry<LootItemFunctionType<?>>> LOOT_FUNCTION_TYPE = createRegistryKey("loot_function_type");
	public static final ResourceKey<Registry<LootNbtProviderType>> LOOT_NBT_PROVIDER_TYPE = createRegistryKey("loot_nbt_provider_type");
	public static final ResourceKey<Registry<LootNumberProviderType>> LOOT_NUMBER_PROVIDER_TYPE = createRegistryKey("loot_number_provider_type");
	public static final ResourceKey<Registry<LootPoolEntryType>> LOOT_POOL_ENTRY_TYPE = createRegistryKey("loot_pool_entry_type");
	public static final ResourceKey<Registry<LootScoreProviderType>> LOOT_SCORE_PROVIDER_TYPE = createRegistryKey("loot_score_provider_type");
	public static final ResourceKey<Registry<MapCodec<? extends SurfaceRules.ConditionSource>>> MATERIAL_CONDITION = createRegistryKey(
		"worldgen/material_condition"
	);
	public static final ResourceKey<Registry<MapCodec<? extends SurfaceRules.RuleSource>>> MATERIAL_RULE = createRegistryKey("worldgen/material_rule");
	public static final ResourceKey<Registry<MemoryModuleType<?>>> MEMORY_MODULE_TYPE = createRegistryKey("memory_module_type");
	public static final ResourceKey<Registry<MenuType<?>>> MENU = createRegistryKey("menu");
	public static final ResourceKey<Registry<MobEffect>> MOB_EFFECT = createRegistryKey("mob_effect");
	public static final ResourceKey<Registry<PaintingVariant>> PAINTING_VARIANT = createRegistryKey("painting_variant");
	public static final ResourceKey<Registry<ParticleType<?>>> PARTICLE_TYPE = createRegistryKey("particle_type");
	public static final ResourceKey<Registry<PlacementModifierType<?>>> PLACEMENT_MODIFIER_TYPE = createRegistryKey("worldgen/placement_modifier_type");
	public static final ResourceKey<Registry<PoiType>> POINT_OF_INTEREST_TYPE = createRegistryKey("point_of_interest_type");
	public static final ResourceKey<Registry<PositionSourceType<?>>> POSITION_SOURCE_TYPE = createRegistryKey("position_source_type");
	public static final ResourceKey<Registry<PosRuleTestType<?>>> POS_RULE_TEST = createRegistryKey("pos_rule_test");
	public static final ResourceKey<Registry<Potion>> POTION = createRegistryKey("potion");
	public static final ResourceKey<Registry<RecipeSerializer<?>>> RECIPE_SERIALIZER = createRegistryKey("recipe_serializer");
	public static final ResourceKey<Registry<RecipeType<?>>> RECIPE_TYPE = createRegistryKey("recipe_type");
	public static final ResourceKey<Registry<RootPlacerType<?>>> ROOT_PLACER_TYPE = createRegistryKey("worldgen/root_placer_type");
	public static final ResourceKey<Registry<RuleTestType<?>>> RULE_TEST = createRegistryKey("rule_test");
	public static final ResourceKey<Registry<RuleBlockEntityModifierType<?>>> RULE_BLOCK_ENTITY_MODIFIER = createRegistryKey("rule_block_entity_modifier");
	public static final ResourceKey<Registry<Schedule>> SCHEDULE = createRegistryKey("schedule");
	public static final ResourceKey<Registry<SensorType<?>>> SENSOR_TYPE = createRegistryKey("sensor_type");
	public static final ResourceKey<Registry<SoundEvent>> SOUND_EVENT = createRegistryKey("sound_event");
	public static final ResourceKey<Registry<StatType<?>>> STAT_TYPE = createRegistryKey("stat_type");
	public static final ResourceKey<Registry<StructurePieceType>> STRUCTURE_PIECE = createRegistryKey("worldgen/structure_piece");
	public static final ResourceKey<Registry<StructurePlacementType<?>>> STRUCTURE_PLACEMENT = createRegistryKey("worldgen/structure_placement");
	public static final ResourceKey<Registry<StructurePoolElementType<?>>> STRUCTURE_POOL_ELEMENT = createRegistryKey("worldgen/structure_pool_element");
	public static final ResourceKey<Registry<MapCodec<? extends PoolAliasBinding>>> POOL_ALIAS_BINDING = createRegistryKey("worldgen/pool_alias_binding");
	public static final ResourceKey<Registry<StructureProcessorType<?>>> STRUCTURE_PROCESSOR = createRegistryKey("worldgen/structure_processor");
	public static final ResourceKey<Registry<StructureType<?>>> STRUCTURE_TYPE = createRegistryKey("worldgen/structure_type");
	public static final ResourceKey<Registry<TreeDecoratorType<?>>> TREE_DECORATOR_TYPE = createRegistryKey("worldgen/tree_decorator_type");
	public static final ResourceKey<Registry<TrunkPlacerType<?>>> TRUNK_PLACER_TYPE = createRegistryKey("worldgen/trunk_placer_type");
	public static final ResourceKey<Registry<VillagerProfession>> VILLAGER_PROFESSION = createRegistryKey("villager_profession");
	public static final ResourceKey<Registry<VillagerType>> VILLAGER_TYPE = createRegistryKey("villager_type");
	public static final ResourceKey<Registry<DecoratedPotPattern>> DECORATED_POT_PATTERN = createRegistryKey("decorated_pot_pattern");
	public static final ResourceKey<Registry<NumberFormatType<?>>> NUMBER_FORMAT_TYPE = createRegistryKey("number_format_type");
	public static final ResourceKey<Registry<DataComponentType<?>>> DATA_COMPONENT_TYPE = createRegistryKey("data_component_type");
	public static final ResourceKey<Registry<MapCodec<? extends EntitySubPredicate>>> ENTITY_SUB_PREDICATE_TYPE = createRegistryKey("entity_sub_predicate_type");
	public static final ResourceKey<Registry<ItemSubPredicate.Type<?>>> ITEM_SUB_PREDICATE_TYPE = createRegistryKey("item_sub_predicate_type");
	public static final ResourceKey<Registry<MapDecorationType>> MAP_DECORATION_TYPE = createRegistryKey("map_decoration_type");
	public static final ResourceKey<Registry<DataComponentType<?>>> ENCHANTMENT_EFFECT_COMPONENT_TYPE = createRegistryKey("enchantment_effect_component_type");
	public static final ResourceKey<Registry<ConsumeEffect.Type<?>>> CONSUME_EFFECT_TYPE = createRegistryKey("consume_effect_type");
	public static final ResourceKey<Registry<RecipeDisplay.Type<?>>> RECIPE_DISPLAY = createRegistryKey("recipe_display");
	public static final ResourceKey<Registry<SlotDisplay.Type<?>>> SLOT_DISPLAY = createRegistryKey("slot_display");
	public static final ResourceKey<Registry<Biome>> BIOME = createRegistryKey("worldgen/biome");
	public static final ResourceKey<Registry<ChatType>> CHAT_TYPE = createRegistryKey("chat_type");
	public static final ResourceKey<Registry<ConfiguredWorldCarver<?>>> CONFIGURED_CARVER = createRegistryKey("worldgen/configured_carver");
	public static final ResourceKey<Registry<ConfiguredFeature<?, ?>>> CONFIGURED_FEATURE = createRegistryKey("worldgen/configured_feature");
	public static final ResourceKey<Registry<DensityFunction>> DENSITY_FUNCTION = createRegistryKey("worldgen/density_function");
	public static final ResourceKey<Registry<DimensionType>> DIMENSION_TYPE = createRegistryKey("dimension_type");
	public static final ResourceKey<Registry<Enchantment>> ENCHANTMENT = createRegistryKey("enchantment");
	public static final ResourceKey<Registry<EnchantmentProvider>> ENCHANTMENT_PROVIDER = createRegistryKey("enchantment_provider");
	public static final ResourceKey<Registry<FlatLevelGeneratorPreset>> FLAT_LEVEL_GENERATOR_PRESET = createRegistryKey("worldgen/flat_level_generator_preset");
	public static final ResourceKey<Registry<NoiseGeneratorSettings>> NOISE_SETTINGS = createRegistryKey("worldgen/noise_settings");
	public static final ResourceKey<Registry<NormalNoise.NoiseParameters>> NOISE = createRegistryKey("worldgen/noise");
	public static final ResourceKey<Registry<PlacedFeature>> PLACED_FEATURE = createRegistryKey("worldgen/placed_feature");
	public static final ResourceKey<Registry<Structure>> STRUCTURE = createRegistryKey("worldgen/structure");
	public static final ResourceKey<Registry<StructureProcessorList>> PROCESSOR_LIST = createRegistryKey("worldgen/processor_list");
	public static final ResourceKey<Registry<StructureSet>> STRUCTURE_SET = createRegistryKey("worldgen/structure_set");
	public static final ResourceKey<Registry<StructureTemplatePool>> TEMPLATE_POOL = createRegistryKey("worldgen/template_pool");
	public static final ResourceKey<Registry<CriterionTrigger<?>>> TRIGGER_TYPE = createRegistryKey("trigger_type");
	public static final ResourceKey<Registry<TrimMaterial>> TRIM_MATERIAL = createRegistryKey("trim_material");
	public static final ResourceKey<Registry<TrimPattern>> TRIM_PATTERN = createRegistryKey("trim_pattern");
	public static final ResourceKey<Registry<WorldPreset>> WORLD_PRESET = createRegistryKey("worldgen/world_preset");
	public static final ResourceKey<Registry<MultiNoiseBiomeSourceParameterList>> MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST = createRegistryKey(
		"worldgen/multi_noise_biome_source_parameter_list"
	);
	public static final ResourceKey<Registry<TrialSpawnerConfig>> TRIAL_SPAWNER_CONFIG = createRegistryKey("trial_spawner");
	public static final ResourceKey<Registry<Level>> DIMENSION = createRegistryKey("dimension");
	public static final ResourceKey<Registry<LevelStem>> LEVEL_STEM = createRegistryKey("dimension");
	public static final ResourceKey<Registry<LootTable>> LOOT_TABLE = createRegistryKey("loot_table");
	public static final ResourceKey<Registry<LootItemFunction>> ITEM_MODIFIER = createRegistryKey("item_modifier");
	public static final ResourceKey<Registry<LootItemCondition>> PREDICATE = createRegistryKey("predicate");
	public static final ResourceKey<Registry<Advancement>> ADVANCEMENT = createRegistryKey("advancement");
	public static final ResourceKey<Registry<Recipe<?>>> RECIPE = createRegistryKey("recipe");

	public static ResourceKey<Level> levelStemToLevel(ResourceKey<LevelStem> resourceKey) {
		return ResourceKey.create(DIMENSION, resourceKey.location());
	}

	public static ResourceKey<LevelStem> levelToLevelStem(ResourceKey<Level> resourceKey) {
		return ResourceKey.create(LEVEL_STEM, resourceKey.location());
	}

	private static <T> ResourceKey<Registry<T>> createRegistryKey(String string) {
		return ResourceKey.createRegistryKey(ResourceLocation.withDefaultNamespace(string));
	}

	public static String elementsDirPath(ResourceKey<? extends Registry<?>> resourceKey) {
		return resourceKey.location().getPath();
	}

	public static String tagsDirPath(ResourceKey<? extends Registry<?>> resourceKey) {
		return "tags/" + resourceKey.location().getPath();
	}
}
