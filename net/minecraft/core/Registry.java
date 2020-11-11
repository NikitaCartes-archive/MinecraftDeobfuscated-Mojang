/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.Lifecycle;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.IdMap;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.decoration.Motive;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.inventory.MenuType;
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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.feature.blockplacers.BlockPlacerType;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSizeType;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElementType;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.structure.templatesystem.PosRuleTestType;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public abstract class Registry<T>
implements Codec<T>,
Keyable,
IdMap<T> {
    protected static final Logger LOGGER = LogManager.getLogger();
    private static final Map<ResourceLocation, Supplier<?>> LOADERS = Maps.newLinkedHashMap();
    public static final ResourceLocation ROOT_REGISTRY_NAME = new ResourceLocation("root");
    protected static final WritableRegistry<WritableRegistry<?>> WRITABLE_REGISTRY = new MappedRegistry(Registry.createRegistryKey("root"), Lifecycle.experimental());
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
    public static final ResourceKey<Registry<Motive>> MOTIVE_REGISTRY = Registry.createRegistryKey("motive");
    public static final ResourceKey<Registry<ResourceLocation>> CUSTOM_STAT_REGISTRY = Registry.createRegistryKey("custom_stat");
    public static final ResourceKey<Registry<ChunkStatus>> CHUNK_STATUS_REGISTRY = Registry.createRegistryKey("chunk_status");
    public static final ResourceKey<Registry<RuleTestType<?>>> RULE_TEST_REGISTRY = Registry.createRegistryKey("rule_test");
    public static final ResourceKey<Registry<PosRuleTestType<?>>> POS_RULE_TEST_REGISTRY = Registry.createRegistryKey("pos_rule_test");
    public static final ResourceKey<Registry<MenuType<?>>> MENU_REGISTRY = Registry.createRegistryKey("menu");
    public static final ResourceKey<Registry<RecipeType<?>>> RECIPE_TYPE_REGISTRY = Registry.createRegistryKey("recipe_type");
    public static final ResourceKey<Registry<RecipeSerializer<?>>> RECIPE_SERIALIZER_REGISTRY = Registry.createRegistryKey("recipe_serializer");
    public static final ResourceKey<Registry<Attribute>> ATTRIBUTE_REGISTRY = Registry.createRegistryKey("attribute");
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
    public static final ResourceKey<Registry<DimensionType>> DIMENSION_TYPE_REGISTRY = Registry.createRegistryKey("dimension_type");
    public static final ResourceKey<Registry<Level>> DIMENSION_REGISTRY = Registry.createRegistryKey("dimension");
    public static final ResourceKey<Registry<LevelStem>> LEVEL_STEM_REGISTRY = Registry.createRegistryKey("dimension");
    public static final Registry<SoundEvent> SOUND_EVENT = Registry.registerSimple(SOUND_EVENT_REGISTRY, () -> SoundEvents.ITEM_PICKUP);
    public static final DefaultedRegistry<Fluid> FLUID = Registry.registerDefaulted(FLUID_REGISTRY, "empty", () -> Fluids.EMPTY);
    public static final Registry<MobEffect> MOB_EFFECT = Registry.registerSimple(MOB_EFFECT_REGISTRY, () -> MobEffects.LUCK);
    public static final DefaultedRegistry<Block> BLOCK = Registry.registerDefaulted(BLOCK_REGISTRY, "air", () -> Blocks.AIR);
    public static final Registry<Enchantment> ENCHANTMENT = Registry.registerSimple(ENCHANTMENT_REGISTRY, () -> Enchantments.BLOCK_FORTUNE);
    public static final DefaultedRegistry<EntityType<?>> ENTITY_TYPE = Registry.registerDefaulted(ENTITY_TYPE_REGISTRY, "pig", () -> EntityType.PIG);
    public static final DefaultedRegistry<Item> ITEM = Registry.registerDefaulted(ITEM_REGISTRY, "air", () -> Items.AIR);
    public static final DefaultedRegistry<Potion> POTION = Registry.registerDefaulted(POTION_REGISTRY, "empty", () -> Potions.EMPTY);
    public static final Registry<ParticleType<?>> PARTICLE_TYPE = Registry.registerSimple(PARTICLE_TYPE_REGISTRY, () -> ParticleTypes.BLOCK);
    public static final Registry<BlockEntityType<?>> BLOCK_ENTITY_TYPE = Registry.registerSimple(BLOCK_ENTITY_TYPE_REGISTRY, () -> BlockEntityType.FURNACE);
    public static final DefaultedRegistry<Motive> MOTIVE = Registry.registerDefaulted(MOTIVE_REGISTRY, "kebab", () -> Motive.KEBAB);
    public static final Registry<ResourceLocation> CUSTOM_STAT = Registry.registerSimple(CUSTOM_STAT_REGISTRY, () -> Stats.JUMP);
    public static final DefaultedRegistry<ChunkStatus> CHUNK_STATUS = Registry.registerDefaulted(CHUNK_STATUS_REGISTRY, "empty", () -> ChunkStatus.EMPTY);
    public static final Registry<RuleTestType<?>> RULE_TEST = Registry.registerSimple(RULE_TEST_REGISTRY, () -> RuleTestType.ALWAYS_TRUE_TEST);
    public static final Registry<PosRuleTestType<?>> POS_RULE_TEST = Registry.registerSimple(POS_RULE_TEST_REGISTRY, () -> PosRuleTestType.ALWAYS_TRUE_TEST);
    public static final Registry<MenuType<?>> MENU = Registry.registerSimple(MENU_REGISTRY, () -> MenuType.ANVIL);
    public static final Registry<RecipeType<?>> RECIPE_TYPE = Registry.registerSimple(RECIPE_TYPE_REGISTRY, () -> RecipeType.CRAFTING);
    public static final Registry<RecipeSerializer<?>> RECIPE_SERIALIZER = Registry.registerSimple(RECIPE_SERIALIZER_REGISTRY, () -> RecipeSerializer.SHAPELESS_RECIPE);
    public static final Registry<Attribute> ATTRIBUTE = Registry.registerSimple(ATTRIBUTE_REGISTRY, () -> Attributes.LUCK);
    public static final Registry<StatType<?>> STAT_TYPE = Registry.registerSimple(STAT_TYPE_REGISTRY, () -> Stats.ITEM_USED);
    public static final DefaultedRegistry<VillagerType> VILLAGER_TYPE = Registry.registerDefaulted(VILLAGER_TYPE_REGISTRY, "plains", () -> VillagerType.PLAINS);
    public static final DefaultedRegistry<VillagerProfession> VILLAGER_PROFESSION = Registry.registerDefaulted(VILLAGER_PROFESSION_REGISTRY, "none", () -> VillagerProfession.NONE);
    public static final DefaultedRegistry<PoiType> POINT_OF_INTEREST_TYPE = Registry.registerDefaulted(POINT_OF_INTEREST_TYPE_REGISTRY, "unemployed", () -> PoiType.UNEMPLOYED);
    public static final DefaultedRegistry<MemoryModuleType<?>> MEMORY_MODULE_TYPE = Registry.registerDefaulted(MEMORY_MODULE_TYPE_REGISTRY, "dummy", () -> MemoryModuleType.DUMMY);
    public static final DefaultedRegistry<SensorType<?>> SENSOR_TYPE = Registry.registerDefaulted(SENSOR_TYPE_REGISTRY, "dummy", () -> SensorType.DUMMY);
    public static final Registry<Schedule> SCHEDULE = Registry.registerSimple(SCHEDULE_REGISTRY, () -> Schedule.EMPTY);
    public static final Registry<Activity> ACTIVITY = Registry.registerSimple(ACTIVITY_REGISTRY, () -> Activity.IDLE);
    public static final Registry<LootPoolEntryType> LOOT_POOL_ENTRY_TYPE = Registry.registerSimple(LOOT_ENTRY_REGISTRY, () -> LootPoolEntries.EMPTY);
    public static final Registry<LootItemFunctionType> LOOT_FUNCTION_TYPE = Registry.registerSimple(LOOT_FUNCTION_REGISTRY, () -> LootItemFunctions.SET_COUNT);
    public static final Registry<LootItemConditionType> LOOT_CONDITION_TYPE = Registry.registerSimple(LOOT_ITEM_REGISTRY, () -> LootItemConditions.INVERTED);
    public static final Registry<LootNumberProviderType> LOOT_NUMBER_PROVIDER_TYPE = Registry.registerSimple(LOOT_NUMBER_PROVIDER_REGISTRY, () -> NumberProviders.CONSTANT);
    public static final Registry<LootNbtProviderType> LOOT_NBT_PROVIDER_TYPE = Registry.registerSimple(LOOT_NBT_PROVIDER_REGISTRY, () -> NbtProviders.CONTEXT);
    public static final Registry<LootScoreProviderType> LOOT_SCORE_PROVIDER_TYPE = Registry.registerSimple(LOOT_SCORE_PROVIDER_REGISTRY, () -> ScoreboardNameProviders.CONTEXT);
    public static final ResourceKey<Registry<NoiseGeneratorSettings>> NOISE_GENERATOR_SETTINGS_REGISTRY = Registry.createRegistryKey("worldgen/noise_settings");
    public static final ResourceKey<Registry<ConfiguredSurfaceBuilder<?>>> CONFIGURED_SURFACE_BUILDER_REGISTRY = Registry.createRegistryKey("worldgen/configured_surface_builder");
    public static final ResourceKey<Registry<ConfiguredWorldCarver<?>>> CONFIGURED_CARVER_REGISTRY = Registry.createRegistryKey("worldgen/configured_carver");
    public static final ResourceKey<Registry<ConfiguredFeature<?, ?>>> CONFIGURED_FEATURE_REGISTRY = Registry.createRegistryKey("worldgen/configured_feature");
    public static final ResourceKey<Registry<ConfiguredStructureFeature<?, ?>>> CONFIGURED_STRUCTURE_FEATURE_REGISTRY = Registry.createRegistryKey("worldgen/configured_structure_feature");
    public static final ResourceKey<Registry<StructureProcessorList>> PROCESSOR_LIST_REGISTRY = Registry.createRegistryKey("worldgen/processor_list");
    public static final ResourceKey<Registry<StructureTemplatePool>> TEMPLATE_POOL_REGISTRY = Registry.createRegistryKey("worldgen/template_pool");
    public static final ResourceKey<Registry<Biome>> BIOME_REGISTRY = Registry.createRegistryKey("worldgen/biome");
    public static final ResourceKey<Registry<SurfaceBuilder<?>>> SURFACE_BUILDER_REGISTRY = Registry.createRegistryKey("worldgen/surface_builder");
    public static final Registry<SurfaceBuilder<?>> SURFACE_BUILDER = Registry.registerSimple(SURFACE_BUILDER_REGISTRY, () -> SurfaceBuilder.DEFAULT);
    public static final ResourceKey<Registry<WorldCarver<?>>> CARVER_REGISTRY = Registry.createRegistryKey("worldgen/carver");
    public static final Registry<WorldCarver<?>> CARVER = Registry.registerSimple(CARVER_REGISTRY, () -> WorldCarver.CAVE);
    public static final ResourceKey<Registry<Feature<?>>> FEATURE_REGISTRY = Registry.createRegistryKey("worldgen/feature");
    public static final Registry<Feature<?>> FEATURE = Registry.registerSimple(FEATURE_REGISTRY, () -> Feature.ORE);
    public static final ResourceKey<Registry<StructureFeature<?>>> STRUCTURE_FEATURE_REGISTRY = Registry.createRegistryKey("worldgen/structure_feature");
    public static final Registry<StructureFeature<?>> STRUCTURE_FEATURE = Registry.registerSimple(STRUCTURE_FEATURE_REGISTRY, () -> StructureFeature.MINESHAFT);
    public static final ResourceKey<Registry<StructurePieceType>> STRUCTURE_PIECE_REGISTRY = Registry.createRegistryKey("worldgen/structure_piece");
    public static final Registry<StructurePieceType> STRUCTURE_PIECE = Registry.registerSimple(STRUCTURE_PIECE_REGISTRY, () -> StructurePieceType.MINE_SHAFT_ROOM);
    public static final ResourceKey<Registry<FeatureDecorator<?>>> DECORATOR_REGISTRY = Registry.createRegistryKey("worldgen/decorator");
    public static final Registry<FeatureDecorator<?>> DECORATOR = Registry.registerSimple(DECORATOR_REGISTRY, () -> FeatureDecorator.NOPE);
    public static final ResourceKey<Registry<BlockStateProviderType<?>>> BLOCK_STATE_PROVIDER_TYPE_REGISTRY = Registry.createRegistryKey("worldgen/block_state_provider_type");
    public static final ResourceKey<Registry<BlockPlacerType<?>>> BLOCK_PLACER_TYPE_REGISTRY = Registry.createRegistryKey("worldgen/block_placer_type");
    public static final ResourceKey<Registry<FoliagePlacerType<?>>> FOLIAGE_PLACER_TYPE_REGISTRY = Registry.createRegistryKey("worldgen/foliage_placer_type");
    public static final ResourceKey<Registry<TrunkPlacerType<?>>> TRUNK_PLACER_TYPE_REGISTRY = Registry.createRegistryKey("worldgen/trunk_placer_type");
    public static final ResourceKey<Registry<TreeDecoratorType<?>>> TREE_DECORATOR_TYPE_REGISTRY = Registry.createRegistryKey("worldgen/tree_decorator_type");
    public static final ResourceKey<Registry<FeatureSizeType<?>>> FEATURE_SIZE_TYPE_REGISTRY = Registry.createRegistryKey("worldgen/feature_size_type");
    public static final ResourceKey<Registry<Codec<? extends BiomeSource>>> BIOME_SOURCE_REGISTRY = Registry.createRegistryKey("worldgen/biome_source");
    public static final ResourceKey<Registry<Codec<? extends ChunkGenerator>>> CHUNK_GENERATOR_REGISTRY = Registry.createRegistryKey("worldgen/chunk_generator");
    public static final ResourceKey<Registry<StructureProcessorType<?>>> STRUCTURE_PROCESSOR_REGISTRY = Registry.createRegistryKey("worldgen/structure_processor");
    public static final ResourceKey<Registry<StructurePoolElementType<?>>> STRUCTURE_POOL_ELEMENT_REGISTRY = Registry.createRegistryKey("worldgen/structure_pool_element");
    public static final Registry<BlockStateProviderType<?>> BLOCKSTATE_PROVIDER_TYPES = Registry.registerSimple(BLOCK_STATE_PROVIDER_TYPE_REGISTRY, () -> BlockStateProviderType.SIMPLE_STATE_PROVIDER);
    public static final Registry<BlockPlacerType<?>> BLOCK_PLACER_TYPES = Registry.registerSimple(BLOCK_PLACER_TYPE_REGISTRY, () -> BlockPlacerType.SIMPLE_BLOCK_PLACER);
    public static final Registry<FoliagePlacerType<?>> FOLIAGE_PLACER_TYPES = Registry.registerSimple(FOLIAGE_PLACER_TYPE_REGISTRY, () -> FoliagePlacerType.BLOB_FOLIAGE_PLACER);
    public static final Registry<TrunkPlacerType<?>> TRUNK_PLACER_TYPES = Registry.registerSimple(TRUNK_PLACER_TYPE_REGISTRY, () -> TrunkPlacerType.STRAIGHT_TRUNK_PLACER);
    public static final Registry<TreeDecoratorType<?>> TREE_DECORATOR_TYPES = Registry.registerSimple(TREE_DECORATOR_TYPE_REGISTRY, () -> TreeDecoratorType.LEAVE_VINE);
    public static final Registry<FeatureSizeType<?>> FEATURE_SIZE_TYPES = Registry.registerSimple(FEATURE_SIZE_TYPE_REGISTRY, () -> FeatureSizeType.TWO_LAYERS_FEATURE_SIZE);
    public static final Registry<Codec<? extends BiomeSource>> BIOME_SOURCE = Registry.registerSimple(BIOME_SOURCE_REGISTRY, Lifecycle.stable(), () -> BiomeSource.CODEC);
    public static final Registry<Codec<? extends ChunkGenerator>> CHUNK_GENERATOR = Registry.registerSimple(CHUNK_GENERATOR_REGISTRY, Lifecycle.stable(), () -> ChunkGenerator.CODEC);
    public static final Registry<StructureProcessorType<?>> STRUCTURE_PROCESSOR = Registry.registerSimple(STRUCTURE_PROCESSOR_REGISTRY, () -> StructureProcessorType.BLOCK_IGNORE);
    public static final Registry<StructurePoolElementType<?>> STRUCTURE_POOL_ELEMENT = Registry.registerSimple(STRUCTURE_POOL_ELEMENT_REGISTRY, () -> StructurePoolElementType.EMPTY);
    private final ResourceKey<? extends Registry<T>> key;
    private final Lifecycle lifecycle;

    private static <T> ResourceKey<Registry<T>> createRegistryKey(String string) {
        return ResourceKey.createRegistryKey(new ResourceLocation(string));
    }

    public static <T extends WritableRegistry<?>> void checkRegistry(WritableRegistry<T> writableRegistry) {
        writableRegistry.forEach(writableRegistry2 -> {
            if (writableRegistry2.keySet().isEmpty()) {
                LOGGER.error("Registry '{}' was empty after loading", (Object)writableRegistry.getKey((WritableRegistry)writableRegistry2));
                if (SharedConstants.IS_RUNNING_IN_IDE) {
                    throw new IllegalStateException("Registry: '" + writableRegistry.getKey((WritableRegistry)writableRegistry2) + "' is empty, not allowed, fix me!");
                }
            }
            if (writableRegistry2 instanceof DefaultedRegistry) {
                ResourceLocation resourceLocation = ((DefaultedRegistry)writableRegistry2).getDefaultKey();
                Validate.notNull(writableRegistry2.get(resourceLocation), "Missing default of DefaultedMappedRegistry: " + resourceLocation, new Object[0]);
            }
        });
    }

    private static <T> Registry<T> registerSimple(ResourceKey<? extends Registry<T>> resourceKey, Supplier<T> supplier) {
        return Registry.registerSimple(resourceKey, Lifecycle.experimental(), supplier);
    }

    private static <T> DefaultedRegistry<T> registerDefaulted(ResourceKey<? extends Registry<T>> resourceKey, String string, Supplier<T> supplier) {
        return Registry.registerDefaulted(resourceKey, string, Lifecycle.experimental(), supplier);
    }

    private static <T> Registry<T> registerSimple(ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle, Supplier<T> supplier) {
        return Registry.internalRegister(resourceKey, new MappedRegistry(resourceKey, lifecycle), supplier, lifecycle);
    }

    private static <T> DefaultedRegistry<T> registerDefaulted(ResourceKey<? extends Registry<T>> resourceKey, String string, Lifecycle lifecycle, Supplier<T> supplier) {
        return Registry.internalRegister(resourceKey, new DefaultedRegistry(string, resourceKey, lifecycle), supplier, lifecycle);
    }

    private static <T, R extends WritableRegistry<T>> R internalRegister(ResourceKey<? extends Registry<T>> resourceKey, R writableRegistry, Supplier<T> supplier, Lifecycle lifecycle) {
        ResourceLocation resourceLocation = resourceKey.location();
        LOADERS.put(resourceLocation, supplier);
        WritableRegistry<WritableRegistry<?>> writableRegistry2 = WRITABLE_REGISTRY;
        return writableRegistry2.register(resourceKey, writableRegistry, lifecycle);
    }

    protected Registry(ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle) {
        this.key = resourceKey;
        this.lifecycle = lifecycle;
    }

    public ResourceKey<? extends Registry<T>> key() {
        return this.key;
    }

    public String toString() {
        return "Registry[" + this.key + " (" + this.lifecycle + ")]";
    }

    @Override
    public <U> DataResult<Pair<T, U>> decode(DynamicOps<U> dynamicOps, U object2) {
        if (dynamicOps.compressMaps()) {
            return dynamicOps.getNumberValue(object2).flatMap((? super R number) -> {
                Object object = this.byId(number.intValue());
                if (object == null) {
                    return DataResult.error("Unknown registry id: " + number);
                }
                return DataResult.success(object, this.lifecycle(object));
            }).map((? super R object) -> Pair.of(object, dynamicOps.empty()));
        }
        return ResourceLocation.CODEC.decode(dynamicOps, object2).flatMap((? super R pair) -> {
            T object = this.get((ResourceLocation)pair.getFirst());
            if (object == null) {
                return DataResult.error("Unknown registry key: " + pair.getFirst());
            }
            return DataResult.success(Pair.of(object, pair.getSecond()), this.lifecycle(object));
        });
    }

    @Override
    public <U> DataResult<U> encode(T object, DynamicOps<U> dynamicOps, U object2) {
        ResourceLocation resourceLocation = this.getKey(object);
        if (resourceLocation == null) {
            return DataResult.error("Unknown registry element " + object);
        }
        if (dynamicOps.compressMaps()) {
            return dynamicOps.mergeToPrimitive(object2, dynamicOps.createInt(this.getId(object))).setLifecycle(this.lifecycle);
        }
        return dynamicOps.mergeToPrimitive(object2, dynamicOps.createString(resourceLocation.toString())).setLifecycle(this.lifecycle);
    }

    public <U> Stream<U> keys(DynamicOps<U> dynamicOps) {
        return this.keySet().stream().map((? super T resourceLocation) -> dynamicOps.createString(resourceLocation.toString()));
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

    protected abstract Lifecycle lifecycle(T var1);

    public abstract Lifecycle elementsLifecycle();

    public Optional<T> getOptional(@Nullable ResourceLocation resourceLocation) {
        return Optional.ofNullable(this.get(resourceLocation));
    }

    @Environment(value=EnvType.CLIENT)
    public Optional<T> getOptional(@Nullable ResourceKey<T> resourceKey) {
        return Optional.ofNullable(this.get(resourceKey));
    }

    public T getOrThrow(ResourceKey<T> resourceKey) {
        T object = this.get(resourceKey);
        if (object == null) {
            throw new IllegalStateException("Missing: " + resourceKey);
        }
        return object;
    }

    public abstract Set<ResourceLocation> keySet();

    public abstract Set<Map.Entry<ResourceKey<T>, T>> entrySet();

    public Stream<T> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    @Environment(value=EnvType.CLIENT)
    public abstract boolean containsKey(ResourceLocation var1);

    public static <T> T register(Registry<? super T> registry, String string, T object) {
        return Registry.register(registry, new ResourceLocation(string), object);
    }

    public static <V, T extends V> T register(Registry<V> registry, ResourceLocation resourceLocation, T object) {
        return ((WritableRegistry)registry).register(ResourceKey.create(registry.key, resourceLocation), object, Lifecycle.stable());
    }

    public static <V, T extends V> T registerMapping(Registry<V> registry, int i, String string, T object) {
        return ((WritableRegistry)registry).registerMapping(i, ResourceKey.create(registry.key, new ResourceLocation(string)), object, Lifecycle.stable());
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
}

