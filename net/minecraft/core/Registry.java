/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.Lifecycle;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.Util;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.IdMap;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.FloatProviderType;
import net.minecraft.util.valueproviders.IntProviderType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.decoration.PaintingVariants;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.Instruments;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.PositionSourceType;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
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
import net.minecraft.world.level.levelgen.structure.templatesystem.PosRuleTestType;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraft.world.level.storage.loot.providers.nbt.LootNbtProviderType;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProviders;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import net.minecraft.world.level.storage.loot.providers.score.LootScoreProviderType;
import net.minecraft.world.level.storage.loot.providers.score.ScoreboardNameProviders;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class Registry<T>
implements Keyable,
IdMap<T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<ResourceLocation, Supplier<?>> LOADERS = Maps.newLinkedHashMap();
    public static final ResourceLocation ROOT_REGISTRY_NAME = new ResourceLocation("root");
    protected static final WritableRegistry<WritableRegistry<?>> WRITABLE_REGISTRY = new MappedRegistry(Registry.createRegistryKey("root"), Lifecycle.experimental(), null);
    public static final Registry<? extends Registry<?>> REGISTRY = WRITABLE_REGISTRY;
    public static final ResourceKey<Registry<SoundEvent>> SOUND_EVENT_REGISTRY = Registry.createRegistryKey("sound_event");
    public static final ResourceKey<Registry<Fluid>> FLUID_REGISTRY = Registry.createRegistryKey("fluid");
    public static final ResourceKey<Registry<MobEffect>> MOB_EFFECT_REGISTRY = Registry.createRegistryKey("mob_effect");
    public static final ResourceKey<Registry<Block>> BLOCK_REGISTRY = Registry.createRegistryKey("block");
    public static final ResourceKey<Registry<Enchantment>> ENCHANTMENT_REGISTRY = Registry.createRegistryKey("enchantment");
    public static final ResourceKey<Registry<EntityType<?>>> ENTITY_TYPE_REGISTRY = Registry.createRegistryKey("entity_type");
    public static final ResourceKey<Registry<Item>> ITEM_REGISTRY = Registry.createRegistryKey("item");
    public static final ResourceKey<Registry<Potion>> POTION_REGISTRY = Registry.createRegistryKey("potion");
    public static final ResourceKey<Registry<ParticleType<?>>> PARTICLE_TYPE_REGISTRY = Registry.createRegistryKey("particle_type");
    public static final ResourceKey<Registry<BlockEntityType<?>>> BLOCK_ENTITY_TYPE_REGISTRY = Registry.createRegistryKey("block_entity_type");
    public static final ResourceKey<Registry<PaintingVariant>> PAINTING_VARIANT_REGISTRY = Registry.createRegistryKey("painting_variant");
    public static final ResourceKey<Registry<ResourceLocation>> CUSTOM_STAT_REGISTRY = Registry.createRegistryKey("custom_stat");
    public static final ResourceKey<Registry<ChunkStatus>> CHUNK_STATUS_REGISTRY = Registry.createRegistryKey("chunk_status");
    public static final ResourceKey<Registry<RuleTestType<?>>> RULE_TEST_REGISTRY = Registry.createRegistryKey("rule_test");
    public static final ResourceKey<Registry<PosRuleTestType<?>>> POS_RULE_TEST_REGISTRY = Registry.createRegistryKey("pos_rule_test");
    public static final ResourceKey<Registry<MenuType<?>>> MENU_REGISTRY = Registry.createRegistryKey("menu");
    public static final ResourceKey<Registry<RecipeType<?>>> RECIPE_TYPE_REGISTRY = Registry.createRegistryKey("recipe_type");
    public static final ResourceKey<Registry<RecipeSerializer<?>>> RECIPE_SERIALIZER_REGISTRY = Registry.createRegistryKey("recipe_serializer");
    public static final ResourceKey<Registry<Attribute>> ATTRIBUTE_REGISTRY = Registry.createRegistryKey("attribute");
    public static final ResourceKey<Registry<GameEvent>> GAME_EVENT_REGISTRY = Registry.createRegistryKey("game_event");
    public static final ResourceKey<Registry<PositionSourceType<?>>> POSITION_SOURCE_TYPE_REGISTRY = Registry.createRegistryKey("position_source_type");
    public static final ResourceKey<Registry<StatType<?>>> STAT_TYPE_REGISTRY = Registry.createRegistryKey("stat_type");
    public static final ResourceKey<Registry<VillagerType>> VILLAGER_TYPE_REGISTRY = Registry.createRegistryKey("villager_type");
    public static final ResourceKey<Registry<VillagerProfession>> VILLAGER_PROFESSION_REGISTRY = Registry.createRegistryKey("villager_profession");
    public static final ResourceKey<Registry<PoiType>> POINT_OF_INTEREST_TYPE_REGISTRY = Registry.createRegistryKey("point_of_interest_type");
    public static final ResourceKey<Registry<MemoryModuleType<?>>> MEMORY_MODULE_TYPE_REGISTRY = Registry.createRegistryKey("memory_module_type");
    public static final ResourceKey<Registry<SensorType<?>>> SENSOR_TYPE_REGISTRY = Registry.createRegistryKey("sensor_type");
    public static final ResourceKey<Registry<Schedule>> SCHEDULE_REGISTRY = Registry.createRegistryKey("schedule");
    public static final ResourceKey<Registry<Activity>> ACTIVITY_REGISTRY = Registry.createRegistryKey("activity");
    public static final ResourceKey<Registry<LootPoolEntryType>> LOOT_ENTRY_REGISTRY = Registry.createRegistryKey("loot_pool_entry_type");
    public static final ResourceKey<Registry<LootItemFunctionType>> LOOT_FUNCTION_REGISTRY = Registry.createRegistryKey("loot_function_type");
    public static final ResourceKey<Registry<LootItemConditionType>> LOOT_ITEM_REGISTRY = Registry.createRegistryKey("loot_condition_type");
    public static final ResourceKey<Registry<LootNumberProviderType>> LOOT_NUMBER_PROVIDER_REGISTRY = Registry.createRegistryKey("loot_number_provider_type");
    public static final ResourceKey<Registry<LootNbtProviderType>> LOOT_NBT_PROVIDER_REGISTRY = Registry.createRegistryKey("loot_nbt_provider_type");
    public static final ResourceKey<Registry<LootScoreProviderType>> LOOT_SCORE_PROVIDER_REGISTRY = Registry.createRegistryKey("loot_score_provider_type");
    public static final ResourceKey<Registry<ArgumentTypeInfo<?, ?>>> COMMAND_ARGUMENT_TYPE_REGISTRY = Registry.createRegistryKey("command_argument_type");
    public static final ResourceKey<Registry<DimensionType>> DIMENSION_TYPE_REGISTRY = Registry.createRegistryKey("dimension_type");
    public static final ResourceKey<Registry<Level>> DIMENSION_REGISTRY = Registry.createRegistryKey("dimension");
    public static final ResourceKey<Registry<LevelStem>> LEVEL_STEM_REGISTRY = Registry.createRegistryKey("dimension");
    public static final DefaultedRegistry<GameEvent> GAME_EVENT = Registry.registerDefaulted(GAME_EVENT_REGISTRY, "step", GameEvent::builtInRegistryHolder, (Registry<T> registry) -> GameEvent.STEP);
    public static final Registry<SoundEvent> SOUND_EVENT = Registry.registerSimple(SOUND_EVENT_REGISTRY, registry -> SoundEvents.ITEM_PICKUP);
    public static final DefaultedRegistry<Fluid> FLUID = Registry.registerDefaulted(FLUID_REGISTRY, "empty", Fluid::builtInRegistryHolder, (Registry<T> registry) -> Fluids.EMPTY);
    public static final Registry<MobEffect> MOB_EFFECT = Registry.registerSimple(MOB_EFFECT_REGISTRY, registry -> MobEffects.LUCK);
    public static final DefaultedRegistry<Block> BLOCK = Registry.registerDefaulted(BLOCK_REGISTRY, "air", Block::builtInRegistryHolder, (Registry<T> registry) -> Blocks.AIR);
    public static final Registry<Enchantment> ENCHANTMENT = Registry.registerSimple(ENCHANTMENT_REGISTRY, registry -> Enchantments.BLOCK_FORTUNE);
    public static final DefaultedRegistry<EntityType<?>> ENTITY_TYPE = Registry.registerDefaulted(ENTITY_TYPE_REGISTRY, "pig", EntityType::builtInRegistryHolder, (Registry<T> registry) -> EntityType.PIG);
    public static final DefaultedRegistry<Item> ITEM = Registry.registerDefaulted(ITEM_REGISTRY, "air", Item::builtInRegistryHolder, (Registry<T> registry) -> Items.AIR);
    public static final DefaultedRegistry<Potion> POTION = Registry.registerDefaulted(POTION_REGISTRY, "empty", registry -> Potions.EMPTY);
    public static final Registry<ParticleType<?>> PARTICLE_TYPE = Registry.registerSimple(PARTICLE_TYPE_REGISTRY, registry -> ParticleTypes.BLOCK);
    public static final Registry<BlockEntityType<?>> BLOCK_ENTITY_TYPE = Registry.registerSimple(BLOCK_ENTITY_TYPE_REGISTRY, registry -> BlockEntityType.FURNACE);
    public static final DefaultedRegistry<PaintingVariant> PAINTING_VARIANT = Registry.registerDefaulted(PAINTING_VARIANT_REGISTRY, "kebab", PaintingVariants::bootstrap);
    public static final Registry<ResourceLocation> CUSTOM_STAT = Registry.registerSimple(CUSTOM_STAT_REGISTRY, registry -> Stats.JUMP);
    public static final DefaultedRegistry<ChunkStatus> CHUNK_STATUS = Registry.registerDefaulted(CHUNK_STATUS_REGISTRY, "empty", registry -> ChunkStatus.EMPTY);
    public static final Registry<RuleTestType<?>> RULE_TEST = Registry.registerSimple(RULE_TEST_REGISTRY, registry -> RuleTestType.ALWAYS_TRUE_TEST);
    public static final Registry<PosRuleTestType<?>> POS_RULE_TEST = Registry.registerSimple(POS_RULE_TEST_REGISTRY, registry -> PosRuleTestType.ALWAYS_TRUE_TEST);
    public static final Registry<MenuType<?>> MENU = Registry.registerSimple(MENU_REGISTRY, registry -> MenuType.ANVIL);
    public static final Registry<RecipeType<?>> RECIPE_TYPE = Registry.registerSimple(RECIPE_TYPE_REGISTRY, registry -> RecipeType.CRAFTING);
    public static final Registry<RecipeSerializer<?>> RECIPE_SERIALIZER = Registry.registerSimple(RECIPE_SERIALIZER_REGISTRY, registry -> RecipeSerializer.SHAPELESS_RECIPE);
    public static final Registry<Attribute> ATTRIBUTE = Registry.registerSimple(ATTRIBUTE_REGISTRY, registry -> Attributes.LUCK);
    public static final Registry<PositionSourceType<?>> POSITION_SOURCE_TYPE = Registry.registerSimple(POSITION_SOURCE_TYPE_REGISTRY, registry -> PositionSourceType.BLOCK);
    public static final Registry<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPE = Registry.registerSimple(COMMAND_ARGUMENT_TYPE_REGISTRY, ArgumentTypeInfos::bootstrap);
    public static final Registry<StatType<?>> STAT_TYPE = Registry.registerSimple(STAT_TYPE_REGISTRY, registry -> Stats.ITEM_USED);
    public static final DefaultedRegistry<VillagerType> VILLAGER_TYPE = Registry.registerDefaulted(VILLAGER_TYPE_REGISTRY, "plains", registry -> VillagerType.PLAINS);
    public static final DefaultedRegistry<VillagerProfession> VILLAGER_PROFESSION = Registry.registerDefaulted(VILLAGER_PROFESSION_REGISTRY, "none", registry -> VillagerProfession.NONE);
    public static final DefaultedRegistry<PoiType> POINT_OF_INTEREST_TYPE = Registry.registerDefaulted(POINT_OF_INTEREST_TYPE_REGISTRY, "unemployed", registry -> PoiType.UNEMPLOYED);
    public static final DefaultedRegistry<MemoryModuleType<?>> MEMORY_MODULE_TYPE = Registry.registerDefaulted(MEMORY_MODULE_TYPE_REGISTRY, "dummy", registry -> MemoryModuleType.DUMMY);
    public static final DefaultedRegistry<SensorType<?>> SENSOR_TYPE = Registry.registerDefaulted(SENSOR_TYPE_REGISTRY, "dummy", registry -> SensorType.DUMMY);
    public static final Registry<Schedule> SCHEDULE = Registry.registerSimple(SCHEDULE_REGISTRY, registry -> Schedule.EMPTY);
    public static final Registry<Activity> ACTIVITY = Registry.registerSimple(ACTIVITY_REGISTRY, registry -> Activity.IDLE);
    public static final Registry<LootPoolEntryType> LOOT_POOL_ENTRY_TYPE = Registry.registerSimple(LOOT_ENTRY_REGISTRY, registry -> LootPoolEntries.EMPTY);
    public static final Registry<LootItemFunctionType> LOOT_FUNCTION_TYPE = Registry.registerSimple(LOOT_FUNCTION_REGISTRY, registry -> LootItemFunctions.SET_COUNT);
    public static final Registry<LootItemConditionType> LOOT_CONDITION_TYPE = Registry.registerSimple(LOOT_ITEM_REGISTRY, registry -> LootItemConditions.INVERTED);
    public static final Registry<LootNumberProviderType> LOOT_NUMBER_PROVIDER_TYPE = Registry.registerSimple(LOOT_NUMBER_PROVIDER_REGISTRY, registry -> NumberProviders.CONSTANT);
    public static final Registry<LootNbtProviderType> LOOT_NBT_PROVIDER_TYPE = Registry.registerSimple(LOOT_NBT_PROVIDER_REGISTRY, registry -> NbtProviders.CONTEXT);
    public static final Registry<LootScoreProviderType> LOOT_SCORE_PROVIDER_TYPE = Registry.registerSimple(LOOT_SCORE_PROVIDER_REGISTRY, registry -> ScoreboardNameProviders.CONTEXT);
    public static final ResourceKey<Registry<FloatProviderType<?>>> FLOAT_PROVIDER_TYPE_REGISTRY = Registry.createRegistryKey("float_provider_type");
    public static final Registry<FloatProviderType<?>> FLOAT_PROVIDER_TYPES = Registry.registerSimple(FLOAT_PROVIDER_TYPE_REGISTRY, registry -> FloatProviderType.CONSTANT);
    public static final ResourceKey<Registry<IntProviderType<?>>> INT_PROVIDER_TYPE_REGISTRY = Registry.createRegistryKey("int_provider_type");
    public static final Registry<IntProviderType<?>> INT_PROVIDER_TYPES = Registry.registerSimple(INT_PROVIDER_TYPE_REGISTRY, registry -> IntProviderType.CONSTANT);
    public static final ResourceKey<Registry<HeightProviderType<?>>> HEIGHT_PROVIDER_TYPE_REGISTRY = Registry.createRegistryKey("height_provider_type");
    public static final Registry<HeightProviderType<?>> HEIGHT_PROVIDER_TYPES = Registry.registerSimple(HEIGHT_PROVIDER_TYPE_REGISTRY, registry -> HeightProviderType.CONSTANT);
    public static final ResourceKey<Registry<BlockPredicateType<?>>> BLOCK_PREDICATE_TYPE_REGISTRY = Registry.createRegistryKey("block_predicate_type");
    public static final Registry<BlockPredicateType<?>> BLOCK_PREDICATE_TYPES = Registry.registerSimple(BLOCK_PREDICATE_TYPE_REGISTRY, registry -> BlockPredicateType.NOT);
    public static final ResourceKey<Registry<NoiseGeneratorSettings>> NOISE_GENERATOR_SETTINGS_REGISTRY = Registry.createRegistryKey("worldgen/noise_settings");
    public static final ResourceKey<Registry<ConfiguredWorldCarver<?>>> CONFIGURED_CARVER_REGISTRY = Registry.createRegistryKey("worldgen/configured_carver");
    public static final ResourceKey<Registry<ConfiguredFeature<?, ?>>> CONFIGURED_FEATURE_REGISTRY = Registry.createRegistryKey("worldgen/configured_feature");
    public static final ResourceKey<Registry<PlacedFeature>> PLACED_FEATURE_REGISTRY = Registry.createRegistryKey("worldgen/placed_feature");
    public static final ResourceKey<Registry<Structure>> STRUCTURE_REGISTRY = Registry.createRegistryKey("worldgen/structure");
    public static final ResourceKey<Registry<StructureSet>> STRUCTURE_SET_REGISTRY = Registry.createRegistryKey("worldgen/structure_set");
    public static final ResourceKey<Registry<StructureProcessorList>> PROCESSOR_LIST_REGISTRY = Registry.createRegistryKey("worldgen/processor_list");
    public static final ResourceKey<Registry<StructureTemplatePool>> TEMPLATE_POOL_REGISTRY = Registry.createRegistryKey("worldgen/template_pool");
    public static final ResourceKey<Registry<Biome>> BIOME_REGISTRY = Registry.createRegistryKey("worldgen/biome");
    public static final ResourceKey<Registry<NormalNoise.NoiseParameters>> NOISE_REGISTRY = Registry.createRegistryKey("worldgen/noise");
    public static final ResourceKey<Registry<DensityFunction>> DENSITY_FUNCTION_REGISTRY = Registry.createRegistryKey("worldgen/density_function");
    public static final ResourceKey<Registry<WorldPreset>> WORLD_PRESET_REGISTRY = Registry.createRegistryKey("worldgen/world_preset");
    public static final ResourceKey<Registry<FlatLevelGeneratorPreset>> FLAT_LEVEL_GENERATOR_PRESET_REGISTRY = Registry.createRegistryKey("worldgen/flat_level_generator_preset");
    public static final ResourceKey<Registry<WorldCarver<?>>> CARVER_REGISTRY = Registry.createRegistryKey("worldgen/carver");
    public static final Registry<WorldCarver<?>> CARVER = Registry.registerSimple(CARVER_REGISTRY, registry -> WorldCarver.CAVE);
    public static final ResourceKey<Registry<Feature<?>>> FEATURE_REGISTRY = Registry.createRegistryKey("worldgen/feature");
    public static final Registry<Feature<?>> FEATURE = Registry.registerSimple(FEATURE_REGISTRY, registry -> Feature.ORE);
    public static final ResourceKey<Registry<StructurePlacementType<?>>> STRUCTURE_PLACEMENT_TYPE_REGISTRY = Registry.createRegistryKey("worldgen/structure_placement");
    public static final Registry<StructurePlacementType<?>> STRUCTURE_PLACEMENT_TYPE = Registry.registerSimple(STRUCTURE_PLACEMENT_TYPE_REGISTRY, registry -> StructurePlacementType.RANDOM_SPREAD);
    public static final ResourceKey<Registry<StructurePieceType>> STRUCTURE_PIECE_REGISTRY = Registry.createRegistryKey("worldgen/structure_piece");
    public static final Registry<StructurePieceType> STRUCTURE_PIECE = Registry.registerSimple(STRUCTURE_PIECE_REGISTRY, registry -> StructurePieceType.MINE_SHAFT_ROOM);
    public static final ResourceKey<Registry<StructureType<?>>> STRUCTURE_TYPE_REGISTRY = Registry.createRegistryKey("worldgen/structure_type");
    public static final Registry<StructureType<?>> STRUCTURE_TYPES = Registry.registerSimple(STRUCTURE_TYPE_REGISTRY, registry -> StructureType.JIGSAW);
    public static final ResourceKey<Registry<PlacementModifierType<?>>> PLACEMENT_MODIFIER_REGISTRY = Registry.createRegistryKey("worldgen/placement_modifier_type");
    public static final Registry<PlacementModifierType<?>> PLACEMENT_MODIFIERS = Registry.registerSimple(PLACEMENT_MODIFIER_REGISTRY, registry -> PlacementModifierType.COUNT);
    public static final ResourceKey<Registry<BlockStateProviderType<?>>> BLOCK_STATE_PROVIDER_TYPE_REGISTRY = Registry.createRegistryKey("worldgen/block_state_provider_type");
    public static final ResourceKey<Registry<FoliagePlacerType<?>>> FOLIAGE_PLACER_TYPE_REGISTRY = Registry.createRegistryKey("worldgen/foliage_placer_type");
    public static final ResourceKey<Registry<TrunkPlacerType<?>>> TRUNK_PLACER_TYPE_REGISTRY = Registry.createRegistryKey("worldgen/trunk_placer_type");
    public static final ResourceKey<Registry<TreeDecoratorType<?>>> TREE_DECORATOR_TYPE_REGISTRY = Registry.createRegistryKey("worldgen/tree_decorator_type");
    public static final ResourceKey<Registry<RootPlacerType<?>>> ROOT_PLACER_TYPE_REGISTRY = Registry.createRegistryKey("worldgen/root_placer_type");
    public static final ResourceKey<Registry<FeatureSizeType<?>>> FEATURE_SIZE_TYPE_REGISTRY = Registry.createRegistryKey("worldgen/feature_size_type");
    public static final ResourceKey<Registry<Codec<? extends BiomeSource>>> BIOME_SOURCE_REGISTRY = Registry.createRegistryKey("worldgen/biome_source");
    public static final ResourceKey<Registry<Codec<? extends ChunkGenerator>>> CHUNK_GENERATOR_REGISTRY = Registry.createRegistryKey("worldgen/chunk_generator");
    public static final ResourceKey<Registry<Codec<? extends SurfaceRules.ConditionSource>>> CONDITION_REGISTRY = Registry.createRegistryKey("worldgen/material_condition");
    public static final ResourceKey<Registry<Codec<? extends SurfaceRules.RuleSource>>> RULE_REGISTRY = Registry.createRegistryKey("worldgen/material_rule");
    public static final ResourceKey<Registry<Codec<? extends DensityFunction>>> DENSITY_FUNCTION_TYPE_REGISTRY = Registry.createRegistryKey("worldgen/density_function_type");
    public static final ResourceKey<Registry<StructureProcessorType<?>>> STRUCTURE_PROCESSOR_REGISTRY = Registry.createRegistryKey("worldgen/structure_processor");
    public static final ResourceKey<Registry<StructurePoolElementType<?>>> STRUCTURE_POOL_ELEMENT_REGISTRY = Registry.createRegistryKey("worldgen/structure_pool_element");
    public static final Registry<BlockStateProviderType<?>> BLOCKSTATE_PROVIDER_TYPES = Registry.registerSimple(BLOCK_STATE_PROVIDER_TYPE_REGISTRY, registry -> BlockStateProviderType.SIMPLE_STATE_PROVIDER);
    public static final Registry<FoliagePlacerType<?>> FOLIAGE_PLACER_TYPES = Registry.registerSimple(FOLIAGE_PLACER_TYPE_REGISTRY, registry -> FoliagePlacerType.BLOB_FOLIAGE_PLACER);
    public static final Registry<TrunkPlacerType<?>> TRUNK_PLACER_TYPES = Registry.registerSimple(TRUNK_PLACER_TYPE_REGISTRY, registry -> TrunkPlacerType.STRAIGHT_TRUNK_PLACER);
    public static final Registry<RootPlacerType<?>> ROOT_PLACER_TYPES = Registry.registerSimple(ROOT_PLACER_TYPE_REGISTRY, registry -> RootPlacerType.MANGROVE_ROOT_PLACER);
    public static final Registry<TreeDecoratorType<?>> TREE_DECORATOR_TYPES = Registry.registerSimple(TREE_DECORATOR_TYPE_REGISTRY, registry -> TreeDecoratorType.LEAVE_VINE);
    public static final Registry<FeatureSizeType<?>> FEATURE_SIZE_TYPES = Registry.registerSimple(FEATURE_SIZE_TYPE_REGISTRY, registry -> FeatureSizeType.TWO_LAYERS_FEATURE_SIZE);
    public static final Registry<Codec<? extends BiomeSource>> BIOME_SOURCE = Registry.registerSimple(BIOME_SOURCE_REGISTRY, Lifecycle.stable(), registry -> BiomeSource.CODEC);
    public static final Registry<Codec<? extends ChunkGenerator>> CHUNK_GENERATOR = Registry.registerSimple(CHUNK_GENERATOR_REGISTRY, Lifecycle.stable(), registry -> ChunkGenerator.CODEC);
    public static final Registry<Codec<? extends SurfaceRules.ConditionSource>> CONDITION = Registry.registerSimple(CONDITION_REGISTRY, SurfaceRules.ConditionSource::bootstrap);
    public static final Registry<Codec<? extends SurfaceRules.RuleSource>> RULE = Registry.registerSimple(RULE_REGISTRY, SurfaceRules.RuleSource::bootstrap);
    public static final Registry<Codec<? extends DensityFunction>> DENSITY_FUNCTION_TYPES = Registry.registerSimple(DENSITY_FUNCTION_TYPE_REGISTRY, DensityFunctions::bootstrap);
    public static final Registry<StructureProcessorType<?>> STRUCTURE_PROCESSOR = Registry.registerSimple(STRUCTURE_PROCESSOR_REGISTRY, registry -> StructureProcessorType.BLOCK_IGNORE);
    public static final Registry<StructurePoolElementType<?>> STRUCTURE_POOL_ELEMENT = Registry.registerSimple(STRUCTURE_POOL_ELEMENT_REGISTRY, registry -> StructurePoolElementType.EMPTY);
    public static final ResourceKey<Registry<ChatType>> CHAT_TYPE_REGISTRY = Registry.createRegistryKey("chat_type");
    public static final Registry<ChatType> CHAT_TYPE = Registry.registerSimple(CHAT_TYPE_REGISTRY, ChatType::bootstrap);
    public static final ResourceKey<Registry<CatVariant>> CAT_VARIANT_REGISTRY = Registry.createRegistryKey("cat_variant");
    public static final Registry<CatVariant> CAT_VARIANT = Registry.registerSimple(CAT_VARIANT_REGISTRY, registry -> CatVariant.BLACK);
    public static final ResourceKey<Registry<FrogVariant>> FROG_VARIANT_REGISTRY = Registry.createRegistryKey("frog_variant");
    public static final Registry<FrogVariant> FROG_VARIANT = Registry.registerSimple(FROG_VARIANT_REGISTRY, registry -> FrogVariant.TEMPERATE);
    public static final ResourceKey<Registry<BannerPattern>> BANNER_PATTERN_REGISTRY = Registry.createRegistryKey("banner_pattern");
    public static final Registry<BannerPattern> BANNER_PATTERN = Registry.registerSimple(BANNER_PATTERN_REGISTRY, BannerPatterns::bootstrap);
    public static final ResourceKey<Registry<Instrument>> INSTRUMENT_REGISTRY = Registry.createRegistryKey("instrument");
    public static final Registry<Instrument> INSTRUMENT = Registry.registerSimple(INSTRUMENT_REGISTRY, Instruments::bootstrap);
    private final ResourceKey<? extends Registry<T>> key;
    private final Lifecycle lifecycle;

    private static <T> ResourceKey<Registry<T>> createRegistryKey(String string) {
        return ResourceKey.createRegistryKey(new ResourceLocation(string));
    }

    public static <T extends Registry<?>> void checkRegistry(Registry<T> registry) {
        registry.forEach(registry2 -> {
            if (registry2.keySet().isEmpty()) {
                Util.logAndPauseIfInIde("Registry '" + registry.getKey((Registry)registry2) + "' was empty after loading");
            }
            if (registry2 instanceof DefaultedRegistry) {
                ResourceLocation resourceLocation = ((DefaultedRegistry)registry2).getDefaultKey();
                Validate.notNull(registry2.get(resourceLocation), "Missing default of DefaultedMappedRegistry: " + resourceLocation, new Object[0]);
            }
        });
    }

    private static <T> Registry<T> registerSimple(ResourceKey<? extends Registry<T>> resourceKey, RegistryBootstrap<T> registryBootstrap) {
        return Registry.registerSimple(resourceKey, Lifecycle.experimental(), registryBootstrap);
    }

    private static <T> DefaultedRegistry<T> registerDefaulted(ResourceKey<? extends Registry<T>> resourceKey, String string, RegistryBootstrap<T> registryBootstrap) {
        return Registry.registerDefaulted(resourceKey, string, Lifecycle.experimental(), registryBootstrap);
    }

    private static <T> DefaultedRegistry<T> registerDefaulted(ResourceKey<? extends Registry<T>> resourceKey, String string, Function<T, Holder.Reference<T>> function, RegistryBootstrap<T> registryBootstrap) {
        return Registry.registerDefaulted(resourceKey, string, Lifecycle.experimental(), function, registryBootstrap);
    }

    private static <T> Registry<T> registerSimple(ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle, RegistryBootstrap<T> registryBootstrap) {
        return Registry.internalRegister(resourceKey, new MappedRegistry(resourceKey, lifecycle, null), registryBootstrap, lifecycle);
    }

    private static <T> Registry<T> registerSimple(ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle, Function<T, Holder.Reference<T>> function, RegistryBootstrap<T> registryBootstrap) {
        return Registry.internalRegister(resourceKey, new MappedRegistry<T>(resourceKey, lifecycle, function), registryBootstrap, lifecycle);
    }

    private static <T> DefaultedRegistry<T> registerDefaulted(ResourceKey<? extends Registry<T>> resourceKey, String string, Lifecycle lifecycle, RegistryBootstrap<T> registryBootstrap) {
        return Registry.internalRegister(resourceKey, new DefaultedRegistry(string, resourceKey, lifecycle, null), registryBootstrap, lifecycle);
    }

    private static <T> DefaultedRegistry<T> registerDefaulted(ResourceKey<? extends Registry<T>> resourceKey, String string, Lifecycle lifecycle, Function<T, Holder.Reference<T>> function, RegistryBootstrap<T> registryBootstrap) {
        return Registry.internalRegister(resourceKey, new DefaultedRegistry<T>(string, resourceKey, lifecycle, function), registryBootstrap, lifecycle);
    }

    private static <T, R extends WritableRegistry<T>> R internalRegister(ResourceKey<? extends Registry<T>> resourceKey, R writableRegistry, RegistryBootstrap<T> registryBootstrap, Lifecycle lifecycle) {
        ResourceLocation resourceLocation = resourceKey.location();
        LOADERS.put(resourceLocation, () -> registryBootstrap.run(writableRegistry));
        WRITABLE_REGISTRY.register(resourceKey, writableRegistry, lifecycle);
        return writableRegistry;
    }

    protected Registry(ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle) {
        Bootstrap.checkBootstrapCalled(() -> "registry " + resourceKey);
        this.key = resourceKey;
        this.lifecycle = lifecycle;
    }

    public static void freezeBuiltins() {
        for (Registry registry : REGISTRY) {
            registry.freeze();
        }
    }

    public ResourceKey<? extends Registry<T>> key() {
        return this.key;
    }

    public Lifecycle lifecycle() {
        return this.lifecycle;
    }

    public String toString() {
        return "Registry[" + this.key + " (" + this.lifecycle + ")]";
    }

    public Codec<T> byNameCodec() {
        Codec<Object> codec = ResourceLocation.CODEC.flatXmap(resourceLocation -> Optional.ofNullable(this.get((ResourceLocation)resourceLocation)).map(DataResult::success).orElseGet(() -> DataResult.error("Unknown registry key in " + this.key + ": " + resourceLocation)), object -> this.getResourceKey(object).map(ResourceKey::location).map(DataResult::success).orElseGet(() -> DataResult.error("Unknown registry element in " + this.key + ":" + object)));
        Codec<Object> codec2 = ExtraCodecs.idResolverCodec(object -> this.getResourceKey(object).isPresent() ? this.getId(object) : -1, this::byId, -1);
        return ExtraCodecs.overrideLifecycle(ExtraCodecs.orCompressed(codec, codec2), this::lifecycle, object -> this.lifecycle);
    }

    public Codec<Holder<T>> holderByNameCodec() {
        Codec<Holder> codec = ResourceLocation.CODEC.flatXmap(resourceLocation -> this.getHolder(ResourceKey.create(this.key, resourceLocation)).map(DataResult::success).orElseGet(() -> DataResult.error("Unknown registry key in " + this.key + ": " + resourceLocation)), holder -> holder.unwrapKey().map(ResourceKey::location).map(DataResult::success).orElseGet(() -> DataResult.error("Unknown registry element in " + this.key + ":" + holder)));
        return ExtraCodecs.overrideLifecycle(codec, holder -> this.lifecycle(holder.value()), holder -> this.lifecycle);
    }

    public <U> Stream<U> keys(DynamicOps<U> dynamicOps) {
        return this.keySet().stream().map(resourceLocation -> dynamicOps.createString(resourceLocation.toString()));
    }

    @Nullable
    public abstract ResourceLocation getKey(T var1);

    public abstract Optional<ResourceKey<T>> getResourceKey(T var1);

    @Override
    public abstract int getId(@Nullable T var1);

    @Nullable
    public abstract T get(@Nullable ResourceKey<T> var1);

    @Nullable
    public abstract T get(@Nullable ResourceLocation var1);

    public abstract Lifecycle lifecycle(T var1);

    public abstract Lifecycle elementsLifecycle();

    public Optional<T> getOptional(@Nullable ResourceLocation resourceLocation) {
        return Optional.ofNullable(this.get(resourceLocation));
    }

    public Optional<T> getOptional(@Nullable ResourceKey<T> resourceKey) {
        return Optional.ofNullable(this.get(resourceKey));
    }

    public T getOrThrow(ResourceKey<T> resourceKey) {
        T object = this.get(resourceKey);
        if (object == null) {
            throw new IllegalStateException("Missing key in " + this.key + ": " + resourceKey);
        }
        return object;
    }

    public abstract Set<ResourceLocation> keySet();

    public abstract Set<Map.Entry<ResourceKey<T>, T>> entrySet();

    public abstract Set<ResourceKey<T>> registryKeySet();

    public abstract Optional<Holder<T>> getRandom(RandomSource var1);

    public Stream<T> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    public abstract boolean containsKey(ResourceLocation var1);

    public abstract boolean containsKey(ResourceKey<T> var1);

    public static <T> T register(Registry<? super T> registry, String string, T object) {
        return Registry.register(registry, new ResourceLocation(string), object);
    }

    public static <V, T extends V> T register(Registry<V> registry, ResourceLocation resourceLocation, T object) {
        return Registry.register(registry, ResourceKey.create(registry.key, resourceLocation), object);
    }

    public static <V, T extends V> T register(Registry<V> registry, ResourceKey<V> resourceKey, T object) {
        ((WritableRegistry)registry).register(resourceKey, object, Lifecycle.stable());
        return object;
    }

    public static <V, T extends V> T registerMapping(Registry<V> registry, int i, String string, T object) {
        ((WritableRegistry)registry).registerMapping(i, ResourceKey.create(registry.key, new ResourceLocation(string)), object, Lifecycle.stable());
        return object;
    }

    public abstract Registry<T> freeze();

    public abstract Holder<T> getOrCreateHolder(ResourceKey<T> var1);

    public abstract Holder.Reference<T> createIntrusiveHolder(T var1);

    public abstract Optional<Holder<T>> getHolder(int var1);

    public abstract Optional<Holder<T>> getHolder(ResourceKey<T> var1);

    public Holder<T> getHolderOrThrow(ResourceKey<T> resourceKey) {
        return this.getHolder(resourceKey).orElseThrow(() -> new IllegalStateException("Missing key in " + this.key + ": " + resourceKey));
    }

    public abstract Stream<Holder.Reference<T>> holders();

    public abstract Optional<HolderSet.Named<T>> getTag(TagKey<T> var1);

    public Iterable<Holder<T>> getTagOrEmpty(TagKey<T> tagKey) {
        return DataFixUtils.orElse(this.getTag(tagKey), List.of());
    }

    public abstract HolderSet.Named<T> getOrCreateTag(TagKey<T> var1);

    public abstract Stream<Pair<TagKey<T>, HolderSet.Named<T>>> getTags();

    public abstract Stream<TagKey<T>> getTagNames();

    public abstract boolean isKnownTagName(TagKey<T> var1);

    public abstract void resetTags();

    public abstract void bindTags(Map<TagKey<T>, List<Holder<T>>> var1);

    public IdMap<Holder<T>> asHolderIdMap() {
        return new IdMap<Holder<T>>(){

            @Override
            public int getId(Holder<T> holder) {
                return Registry.this.getId(holder.value());
            }

            @Override
            @Nullable
            public Holder<T> byId(int i) {
                return Registry.this.getHolder(i).orElse(null);
            }

            @Override
            public int size() {
                return Registry.this.size();
            }

            @Override
            public Iterator<Holder<T>> iterator() {
                return Registry.this.holders().map(reference -> reference).iterator();
            }

            @Override
            @Nullable
            public /* synthetic */ Object byId(int i) {
                return this.byId(i);
            }
        };
    }

    static {
        BuiltinRegistries.bootstrap();
        LOADERS.forEach((? super K resourceLocation, ? super V supplier) -> {
            if (supplier.get() == null) {
                LOGGER.error("Unable to bootstrap registry '{}'", resourceLocation);
            }
        });
        Registry.checkRegistry(WRITABLE_REGISTRY);
    }

    @FunctionalInterface
    static interface RegistryBootstrap<T> {
        public T run(Registry<T> var1);
    }
}

