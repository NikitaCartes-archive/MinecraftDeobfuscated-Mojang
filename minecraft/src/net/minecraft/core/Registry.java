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
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
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
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Registry<T> implements Codec<T>, Keyable, IdMap<T> {
	protected static final Logger LOGGER = LogManager.getLogger();
	private static final Map<ResourceLocation, Supplier<?>> LOADERS = Maps.<ResourceLocation, Supplier<?>>newLinkedHashMap();
	public static final ResourceLocation ROOT_REGISTRY_NAME = new ResourceLocation("root");
	protected static final WritableRegistry<WritableRegistry<?>> WRITABLE_REGISTRY = new MappedRegistry<>(createRegistryKey("root"), Lifecycle.experimental());
	public static final Registry<? extends Registry<?>> REGISTRY = WRITABLE_REGISTRY;
	public static final ResourceKey<Registry<SoundEvent>> SOUND_EVENT_REGISTRY = createRegistryKey("sound_event");
	public static final ResourceKey<Registry<Fluid>> FLUID_REGISTRY = createRegistryKey("fluid");
	public static final ResourceKey<Registry<MobEffect>> MOB_EFFECT_REGISTRY = createRegistryKey("mob_effect");
	public static final ResourceKey<Registry<Block>> BLOCK_REGISTRY = createRegistryKey("block");
	public static final ResourceKey<Registry<Enchantment>> ENCHANTMENT_REGISTRY = createRegistryKey("enchantment");
	public static final ResourceKey<Registry<EntityType<?>>> ENTITY_TYPE_REGISTRY = createRegistryKey("entity_type");
	public static final ResourceKey<Registry<Item>> ITEM_REGISTRY = createRegistryKey("item");
	public static final ResourceKey<Registry<Potion>> POTION_REGISTRY = createRegistryKey("potion");
	public static final ResourceKey<Registry<ParticleType<?>>> PARTICLE_TYPE_REGISTRY = createRegistryKey("particle_type");
	public static final ResourceKey<Registry<BlockEntityType<?>>> BLOCK_ENTITY_TYPE_REGISTRY = createRegistryKey("block_entity_type");
	public static final ResourceKey<Registry<Motive>> MOTIVE_REGISTRY = createRegistryKey("motive");
	public static final ResourceKey<Registry<ResourceLocation>> CUSTOM_STAT_REGISTRY = createRegistryKey("custom_stat");
	public static final ResourceKey<Registry<ChunkStatus>> CHUNK_STATUS_REGISTRY = createRegistryKey("chunk_status");
	public static final ResourceKey<Registry<RuleTestType<?>>> RULE_TEST_REGISTRY = createRegistryKey("rule_test");
	public static final ResourceKey<Registry<PosRuleTestType<?>>> POS_RULE_TEST_REGISTRY = createRegistryKey("pos_rule_test");
	public static final ResourceKey<Registry<MenuType<?>>> MENU_REGISTRY = createRegistryKey("menu");
	public static final ResourceKey<Registry<RecipeType<?>>> RECIPE_TYPE_REGISTRY = createRegistryKey("recipe_type");
	public static final ResourceKey<Registry<RecipeSerializer<?>>> RECIPE_SERIALIZER_REGISTRY = createRegistryKey("recipe_serializer");
	public static final ResourceKey<Registry<Attribute>> ATTRIBUTE_REGISTRY = createRegistryKey("attribute");
	public static final ResourceKey<Registry<StatType<?>>> STAT_TYPE_REGISTRY = createRegistryKey("stat_type");
	public static final ResourceKey<Registry<VillagerType>> VILLAGER_TYPE_REGISTRY = createRegistryKey("villager_type");
	public static final ResourceKey<Registry<VillagerProfession>> VILLAGER_PROFESSION_REGISTRY = createRegistryKey("villager_profession");
	public static final ResourceKey<Registry<PoiType>> POINT_OF_INTEREST_TYPE_REGISTRY = createRegistryKey("point_of_interest_type");
	public static final ResourceKey<Registry<MemoryModuleType<?>>> MEMORY_MODULE_TYPE_REGISTRY = createRegistryKey("memory_module_type");
	public static final ResourceKey<Registry<SensorType<?>>> SENSOR_TYPE_REGISTRY = createRegistryKey("sensor_type");
	public static final ResourceKey<Registry<Schedule>> SCHEDULE_REGISTRY = createRegistryKey("schedule");
	public static final ResourceKey<Registry<Activity>> ACTIVITY_REGISTRY = createRegistryKey("activity");
	public static final ResourceKey<Registry<LootPoolEntryType>> LOOT_ENTRY_REGISTRY = createRegistryKey("loot_pool_entry_type");
	public static final ResourceKey<Registry<LootItemFunctionType>> LOOT_FUNCTION_REGISTRY = createRegistryKey("loot_function_type");
	public static final ResourceKey<Registry<LootItemConditionType>> LOOT_ITEM_REGISTRY = createRegistryKey("loot_condition_type");
	public static final ResourceKey<Registry<DimensionType>> DIMENSION_TYPE_REGISTRY = createRegistryKey("dimension_type");
	public static final ResourceKey<Registry<Level>> DIMENSION_REGISTRY = createRegistryKey("dimension");
	public static final ResourceKey<Registry<LevelStem>> LEVEL_STEM_REGISTRY = createRegistryKey("dimension");
	public static final Registry<SoundEvent> SOUND_EVENT = registerSimple(SOUND_EVENT_REGISTRY, () -> SoundEvents.ITEM_PICKUP);
	public static final DefaultedRegistry<Fluid> FLUID = registerDefaulted(FLUID_REGISTRY, "empty", () -> Fluids.EMPTY);
	public static final Registry<MobEffect> MOB_EFFECT = registerSimple(MOB_EFFECT_REGISTRY, () -> MobEffects.LUCK);
	public static final DefaultedRegistry<Block> BLOCK = registerDefaulted(BLOCK_REGISTRY, "air", () -> Blocks.AIR);
	public static final Registry<Enchantment> ENCHANTMENT = registerSimple(ENCHANTMENT_REGISTRY, () -> Enchantments.BLOCK_FORTUNE);
	public static final DefaultedRegistry<EntityType<?>> ENTITY_TYPE = registerDefaulted(ENTITY_TYPE_REGISTRY, "pig", () -> EntityType.PIG);
	public static final DefaultedRegistry<Item> ITEM = registerDefaulted(ITEM_REGISTRY, "air", () -> Items.AIR);
	public static final DefaultedRegistry<Potion> POTION = registerDefaulted(POTION_REGISTRY, "empty", () -> Potions.EMPTY);
	public static final Registry<ParticleType<?>> PARTICLE_TYPE = registerSimple(PARTICLE_TYPE_REGISTRY, () -> ParticleTypes.BLOCK);
	public static final Registry<BlockEntityType<?>> BLOCK_ENTITY_TYPE = registerSimple(BLOCK_ENTITY_TYPE_REGISTRY, () -> BlockEntityType.FURNACE);
	public static final DefaultedRegistry<Motive> MOTIVE = registerDefaulted(MOTIVE_REGISTRY, "kebab", () -> Motive.KEBAB);
	public static final Registry<ResourceLocation> CUSTOM_STAT = registerSimple(CUSTOM_STAT_REGISTRY, () -> Stats.JUMP);
	public static final DefaultedRegistry<ChunkStatus> CHUNK_STATUS = registerDefaulted(CHUNK_STATUS_REGISTRY, "empty", () -> ChunkStatus.EMPTY);
	public static final Registry<RuleTestType<?>> RULE_TEST = registerSimple(RULE_TEST_REGISTRY, () -> RuleTestType.ALWAYS_TRUE_TEST);
	public static final Registry<PosRuleTestType<?>> POS_RULE_TEST = registerSimple(POS_RULE_TEST_REGISTRY, () -> PosRuleTestType.ALWAYS_TRUE_TEST);
	public static final Registry<MenuType<?>> MENU = registerSimple(MENU_REGISTRY, () -> MenuType.ANVIL);
	public static final Registry<RecipeType<?>> RECIPE_TYPE = registerSimple(RECIPE_TYPE_REGISTRY, () -> RecipeType.CRAFTING);
	public static final Registry<RecipeSerializer<?>> RECIPE_SERIALIZER = registerSimple(RECIPE_SERIALIZER_REGISTRY, () -> RecipeSerializer.SHAPELESS_RECIPE);
	public static final Registry<Attribute> ATTRIBUTE = registerSimple(ATTRIBUTE_REGISTRY, () -> Attributes.LUCK);
	public static final Registry<StatType<?>> STAT_TYPE = registerSimple(STAT_TYPE_REGISTRY, () -> Stats.ITEM_USED);
	public static final DefaultedRegistry<VillagerType> VILLAGER_TYPE = registerDefaulted(VILLAGER_TYPE_REGISTRY, "plains", () -> VillagerType.PLAINS);
	public static final DefaultedRegistry<VillagerProfession> VILLAGER_PROFESSION = registerDefaulted(
		VILLAGER_PROFESSION_REGISTRY, "none", () -> VillagerProfession.NONE
	);
	public static final DefaultedRegistry<PoiType> POINT_OF_INTEREST_TYPE = registerDefaulted(
		POINT_OF_INTEREST_TYPE_REGISTRY, "unemployed", () -> PoiType.UNEMPLOYED
	);
	public static final DefaultedRegistry<MemoryModuleType<?>> MEMORY_MODULE_TYPE = registerDefaulted(
		MEMORY_MODULE_TYPE_REGISTRY, "dummy", () -> MemoryModuleType.DUMMY
	);
	public static final DefaultedRegistry<SensorType<?>> SENSOR_TYPE = registerDefaulted(SENSOR_TYPE_REGISTRY, "dummy", () -> SensorType.DUMMY);
	public static final Registry<Schedule> SCHEDULE = registerSimple(SCHEDULE_REGISTRY, () -> Schedule.EMPTY);
	public static final Registry<Activity> ACTIVITY = registerSimple(ACTIVITY_REGISTRY, () -> Activity.IDLE);
	public static final Registry<LootPoolEntryType> LOOT_POOL_ENTRY_TYPE = registerSimple(LOOT_ENTRY_REGISTRY, () -> LootPoolEntries.EMPTY);
	public static final Registry<LootItemFunctionType> LOOT_FUNCTION_TYPE = registerSimple(LOOT_FUNCTION_REGISTRY, () -> LootItemFunctions.SET_COUNT);
	public static final Registry<LootItemConditionType> LOOT_CONDITION_TYPE = registerSimple(LOOT_ITEM_REGISTRY, () -> LootItemConditions.INVERTED);
	public static final ResourceKey<Registry<NoiseGeneratorSettings>> NOISE_GENERATOR_SETTINGS_REGISTRY = createRegistryKey("worldgen/noise_settings");
	public static final ResourceKey<Registry<ConfiguredSurfaceBuilder<?>>> CONFIGURED_SURFACE_BUILDER_REGISTRY = createRegistryKey(
		"worldgen/configured_surface_builder"
	);
	public static final ResourceKey<Registry<ConfiguredWorldCarver<?>>> CONFIGURED_CARVER_REGISTRY = createRegistryKey("worldgen/configured_carver");
	public static final ResourceKey<Registry<ConfiguredFeature<?, ?>>> CONFIGURED_FEATURE_REGISTRY = createRegistryKey("worldgen/configured_feature");
	public static final ResourceKey<Registry<ConfiguredStructureFeature<?, ?>>> CONFIGURED_STRUCTURE_FEATURE_REGISTRY = createRegistryKey(
		"worldgen/configured_structure_feature"
	);
	public static final ResourceKey<Registry<StructureProcessorList>> PROCESSOR_LIST_REGISTRY = createRegistryKey("worldgen/processor_list");
	public static final ResourceKey<Registry<StructureTemplatePool>> TEMPLATE_POOL_REGISTRY = createRegistryKey("worldgen/template_pool");
	public static final ResourceKey<Registry<Biome>> BIOME_REGISTRY = createRegistryKey("worldgen/biome");
	public static final ResourceKey<Registry<SurfaceBuilder<?>>> SURFACE_BUILDER_REGISTRY = createRegistryKey("worldgen/surface_builder");
	public static final Registry<SurfaceBuilder<?>> SURFACE_BUILDER = registerSimple(SURFACE_BUILDER_REGISTRY, () -> SurfaceBuilder.DEFAULT);
	public static final ResourceKey<Registry<WorldCarver<?>>> CARVER_REGISTRY = createRegistryKey("worldgen/carver");
	public static final Registry<WorldCarver<?>> CARVER = registerSimple(CARVER_REGISTRY, () -> WorldCarver.CAVE);
	public static final ResourceKey<Registry<Feature<?>>> FEATURE_REGISTRY = createRegistryKey("worldgen/feature");
	public static final Registry<Feature<?>> FEATURE = registerSimple(FEATURE_REGISTRY, () -> Feature.ORE);
	public static final ResourceKey<Registry<StructureFeature<?>>> STRUCTURE_FEATURE_REGISTRY = createRegistryKey("worldgen/structure_feature");
	public static final Registry<StructureFeature<?>> STRUCTURE_FEATURE = registerSimple(STRUCTURE_FEATURE_REGISTRY, () -> StructureFeature.MINESHAFT);
	public static final ResourceKey<Registry<StructurePieceType>> STRUCTURE_PIECE_REGISTRY = createRegistryKey("worldgen/structure_piece");
	public static final Registry<StructurePieceType> STRUCTURE_PIECE = registerSimple(STRUCTURE_PIECE_REGISTRY, () -> StructurePieceType.MINE_SHAFT_ROOM);
	public static final ResourceKey<Registry<FeatureDecorator<?>>> DECORATOR_REGISTRY = createRegistryKey("worldgen/decorator");
	public static final Registry<FeatureDecorator<?>> DECORATOR = registerSimple(DECORATOR_REGISTRY, () -> FeatureDecorator.NOPE);
	public static final ResourceKey<Registry<BlockStateProviderType<?>>> BLOCK_STATE_PROVIDER_TYPE_REGISTRY = createRegistryKey(
		"worldgen/block_state_provider_type"
	);
	public static final ResourceKey<Registry<BlockPlacerType<?>>> BLOCK_PLACER_TYPE_REGISTRY = createRegistryKey("worldgen/block_placer_type");
	public static final ResourceKey<Registry<FoliagePlacerType<?>>> FOLIAGE_PLACER_TYPE_REGISTRY = createRegistryKey("worldgen/foliage_placer_type");
	public static final ResourceKey<Registry<TrunkPlacerType<?>>> TRUNK_PLACER_TYPE_REGISTRY = createRegistryKey("worldgen/trunk_placer_type");
	public static final ResourceKey<Registry<TreeDecoratorType<?>>> TREE_DECORATOR_TYPE_REGISTRY = createRegistryKey("worldgen/tree_decorator_type");
	public static final ResourceKey<Registry<FeatureSizeType<?>>> FEATURE_SIZE_TYPE_REGISTRY = createRegistryKey("worldgen/feature_size_type");
	public static final ResourceKey<Registry<Codec<? extends BiomeSource>>> BIOME_SOURCE_REGISTRY = createRegistryKey("worldgen/biome_source");
	public static final ResourceKey<Registry<Codec<? extends ChunkGenerator>>> CHUNK_GENERATOR_REGISTRY = createRegistryKey("worldgen/chunk_generator");
	public static final ResourceKey<Registry<StructureProcessorType<?>>> STRUCTURE_PROCESSOR_REGISTRY = createRegistryKey("worldgen/structure_processor");
	public static final ResourceKey<Registry<StructurePoolElementType<?>>> STRUCTURE_POOL_ELEMENT_REGISTRY = createRegistryKey("worldgen/structure_pool_element");
	public static final Registry<BlockStateProviderType<?>> BLOCKSTATE_PROVIDER_TYPES = registerSimple(
		BLOCK_STATE_PROVIDER_TYPE_REGISTRY, () -> BlockStateProviderType.SIMPLE_STATE_PROVIDER
	);
	public static final Registry<BlockPlacerType<?>> BLOCK_PLACER_TYPES = registerSimple(BLOCK_PLACER_TYPE_REGISTRY, () -> BlockPlacerType.SIMPLE_BLOCK_PLACER);
	public static final Registry<FoliagePlacerType<?>> FOLIAGE_PLACER_TYPES = registerSimple(
		FOLIAGE_PLACER_TYPE_REGISTRY, () -> FoliagePlacerType.BLOB_FOLIAGE_PLACER
	);
	public static final Registry<TrunkPlacerType<?>> TRUNK_PLACER_TYPES = registerSimple(TRUNK_PLACER_TYPE_REGISTRY, () -> TrunkPlacerType.STRAIGHT_TRUNK_PLACER);
	public static final Registry<TreeDecoratorType<?>> TREE_DECORATOR_TYPES = registerSimple(TREE_DECORATOR_TYPE_REGISTRY, () -> TreeDecoratorType.LEAVE_VINE);
	public static final Registry<FeatureSizeType<?>> FEATURE_SIZE_TYPES = registerSimple(FEATURE_SIZE_TYPE_REGISTRY, () -> FeatureSizeType.TWO_LAYERS_FEATURE_SIZE);
	public static final Registry<Codec<? extends BiomeSource>> BIOME_SOURCE = registerSimple(BIOME_SOURCE_REGISTRY, Lifecycle.stable(), () -> BiomeSource.CODEC);
	public static final Registry<Codec<? extends ChunkGenerator>> CHUNK_GENERATOR = registerSimple(
		CHUNK_GENERATOR_REGISTRY, Lifecycle.stable(), () -> ChunkGenerator.CODEC
	);
	public static final Registry<StructureProcessorType<?>> STRUCTURE_PROCESSOR = registerSimple(
		STRUCTURE_PROCESSOR_REGISTRY, () -> StructureProcessorType.BLOCK_IGNORE
	);
	public static final Registry<StructurePoolElementType<?>> STRUCTURE_POOL_ELEMENT = registerSimple(
		STRUCTURE_POOL_ELEMENT_REGISTRY, () -> StructurePoolElementType.EMPTY
	);
	private final ResourceKey<? extends Registry<T>> key;
	private final Lifecycle lifecycle;

	private static <T> ResourceKey<Registry<T>> createRegistryKey(String string) {
		return ResourceKey.createRegistryKey(new ResourceLocation(string));
	}

	public static <T extends WritableRegistry<?>> void checkRegistry(WritableRegistry<T> writableRegistry) {
		writableRegistry.forEach(writableRegistry2 -> {
			if (writableRegistry2.keySet().isEmpty()) {
				LOGGER.error("Registry '{}' was empty after loading", writableRegistry.getKey((T)writableRegistry2));
				if (SharedConstants.IS_RUNNING_IN_IDE) {
					throw new IllegalStateException("Registry: '" + writableRegistry.getKey((T)writableRegistry2) + "' is empty, not allowed, fix me!");
				}
			}

			if (writableRegistry2 instanceof DefaultedRegistry) {
				ResourceLocation resourceLocation = ((DefaultedRegistry)writableRegistry2).getDefaultKey();
				Validate.notNull(writableRegistry2.get(resourceLocation), "Missing default of DefaultedMappedRegistry: " + resourceLocation);
			}
		});
	}

	private static <T> Registry<T> registerSimple(ResourceKey<? extends Registry<T>> resourceKey, Supplier<T> supplier) {
		return registerSimple(resourceKey, Lifecycle.experimental(), supplier);
	}

	private static <T> DefaultedRegistry<T> registerDefaulted(ResourceKey<? extends Registry<T>> resourceKey, String string, Supplier<T> supplier) {
		return registerDefaulted(resourceKey, string, Lifecycle.experimental(), supplier);
	}

	private static <T> Registry<T> registerSimple(ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle, Supplier<T> supplier) {
		return internalRegister(resourceKey, new MappedRegistry<>(resourceKey, lifecycle), supplier);
	}

	private static <T> DefaultedRegistry<T> registerDefaulted(
		ResourceKey<? extends Registry<T>> resourceKey, String string, Lifecycle lifecycle, Supplier<T> supplier
	) {
		return internalRegister(resourceKey, new DefaultedRegistry<>(string, resourceKey, lifecycle), supplier);
	}

	private static <T, R extends WritableRegistry<T>> R internalRegister(ResourceKey<? extends Registry<T>> resourceKey, R writableRegistry, Supplier<T> supplier) {
		ResourceLocation resourceLocation = resourceKey.location();
		LOADERS.put(resourceLocation, supplier);
		WritableRegistry<R> writableRegistry2 = WRITABLE_REGISTRY;
		return writableRegistry2.register((ResourceKey<R>)resourceKey, writableRegistry);
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
	public <U> DataResult<Pair<T, U>> decode(DynamicOps<U> dynamicOps, U object) {
		return dynamicOps.compressMaps()
			? dynamicOps.getNumberValue(object).flatMap(number -> {
				int i = number.intValue();
				if (!this.containsId(i)) {
					return DataResult.error("Unknown registry id: " + number);
				} else {
					T objectx = this.byId(i);
					return DataResult.success(objectx, this.lifecycle);
				}
			}).map(objectx -> Pair.of(objectx, dynamicOps.empty()))
			: ResourceLocation.CODEC
				.decode(dynamicOps, object)
				.addLifecycle(this.lifecycle)
				.flatMap(
					pair -> !this.containsKey((ResourceLocation)pair.getFirst())
							? DataResult.error("Unknown registry key: " + pair.getFirst())
							: DataResult.success(pair.mapFirst(this::get), this.lifecycle)
				);
	}

	@Override
	public <U> DataResult<U> encode(T object, DynamicOps<U> dynamicOps, U object2) {
		ResourceLocation resourceLocation = this.getKey(object);
		if (resourceLocation == null) {
			return DataResult.error("Unknown registry element " + object);
		} else {
			return dynamicOps.compressMaps()
				? dynamicOps.mergeToPrimitive(object2, dynamicOps.createInt(this.getId(object))).setLifecycle(this.lifecycle)
				: dynamicOps.mergeToPrimitive(object2, dynamicOps.createString(resourceLocation.toString())).setLifecycle(this.lifecycle);
		}
	}

	@Override
	public <U> Stream<U> keys(DynamicOps<U> dynamicOps) {
		return this.keySet().stream().map(resourceLocation -> dynamicOps.createString(resourceLocation.toString()));
	}

	@Nullable
	public abstract ResourceLocation getKey(T object);

	public abstract Optional<ResourceKey<T>> getResourceKey(T object);

	@Override
	public abstract int getId(@Nullable T object);

	@Nullable
	public abstract T get(@Nullable ResourceKey<T> resourceKey);

	@Nullable
	public abstract T get(@Nullable ResourceLocation resourceLocation);

	public Optional<T> getOptional(@Nullable ResourceLocation resourceLocation) {
		return Optional.ofNullable(this.get(resourceLocation));
	}

	public abstract Set<ResourceLocation> keySet();

	public abstract Set<Entry<ResourceKey<T>, T>> entrySet();

	public Stream<T> stream() {
		return StreamSupport.stream(this.spliterator(), false);
	}

	public abstract boolean containsKey(ResourceLocation resourceLocation);

	public boolean containsId(int i) {
		return this.byId(i) != null;
	}

	public static <T> T register(Registry<? super T> registry, String string, T object) {
		return register(registry, new ResourceLocation(string), object);
	}

	public static <V, T extends V> T register(Registry<V> registry, ResourceLocation resourceLocation, T object) {
		return ((WritableRegistry)registry).register(ResourceKey.create(registry.key, resourceLocation), object);
	}

	public static <V, T extends V> T registerMapping(Registry<V> registry, int i, String string, T object) {
		return ((WritableRegistry)registry).registerMapping(i, ResourceKey.create(registry.key, new ResourceLocation(string)), object);
	}

	static {
		BuiltinRegistries.bootstrap();
		LOADERS.forEach((resourceLocation, supplier) -> {
			if (supplier.get() == null) {
				LOGGER.error("Unable to bootstrap registry '{}'", resourceLocation);
			}
		});
		checkRegistry(WRITABLE_REGISTRY);
	}
}
