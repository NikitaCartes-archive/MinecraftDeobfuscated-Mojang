package net.minecraft.core;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
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
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSourceType;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.feature.blockplacers.BlockPlacerType;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElementType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.structure.StructureFeatureIO;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Registry<T> implements IdMap<T> {
	protected static final Logger LOGGER = LogManager.getLogger();
	private static final Map<ResourceLocation, Supplier<?>> LOADERS = Maps.<ResourceLocation, Supplier<?>>newLinkedHashMap();
	public static final WritableRegistry<WritableRegistry<?>> REGISTRY = new MappedRegistry<>();
	public static final Registry<SoundEvent> SOUND_EVENT = registerSimple("sound_event", () -> SoundEvents.ITEM_PICKUP);
	public static final DefaultedRegistry<Fluid> FLUID = registerDefaulted("fluid", "empty", () -> Fluids.EMPTY);
	public static final Registry<MobEffect> MOB_EFFECT = registerSimple("mob_effect", () -> MobEffects.LUCK);
	public static final DefaultedRegistry<Block> BLOCK = registerDefaulted("block", "air", () -> Blocks.AIR);
	public static final Registry<Enchantment> ENCHANTMENT = registerSimple("enchantment", () -> Enchantments.BLOCK_FORTUNE);
	public static final DefaultedRegistry<EntityType<?>> ENTITY_TYPE = registerDefaulted("entity_type", "pig", () -> EntityType.PIG);
	public static final DefaultedRegistry<Item> ITEM = registerDefaulted("item", "air", () -> Items.AIR);
	public static final DefaultedRegistry<Potion> POTION = registerDefaulted("potion", "empty", () -> Potions.EMPTY);
	public static final Registry<WorldCarver<?>> CARVER = registerSimple("carver", () -> WorldCarver.CAVE);
	public static final Registry<SurfaceBuilder<?>> SURFACE_BUILDER = registerSimple("surface_builder", () -> SurfaceBuilder.DEFAULT);
	public static final Registry<Feature<?>> FEATURE = registerSimple("feature", () -> Feature.ORE);
	public static final Registry<FeatureDecorator<?>> DECORATOR = registerSimple("decorator", () -> FeatureDecorator.NOPE);
	public static final Registry<Biome> BIOME = registerSimple("biome", () -> Biomes.DEFAULT);
	public static final Registry<BlockStateProviderType<?>> BLOCKSTATE_PROVIDER_TYPES = registerSimple(
		"block_state_provider_type", () -> BlockStateProviderType.SIMPLE_STATE_PROVIDER
	);
	public static final Registry<BlockPlacerType<?>> BLOCK_PLACER_TYPES = registerSimple("block_placer_type", () -> BlockPlacerType.SIMPLE_BLOCK_PLACER);
	public static final Registry<FoliagePlacerType<?>> FOLIAGE_PLACER_TYPES = registerSimple("foliage_placer_type", () -> FoliagePlacerType.BLOB_FOLIAGE_PLACER);
	public static final Registry<TreeDecoratorType<?>> TREE_DECORATOR_TYPES = registerSimple("tree_decorator_type", () -> TreeDecoratorType.LEAVE_VINE);
	public static final Registry<ParticleType<? extends ParticleOptions>> PARTICLE_TYPE = registerSimple("particle_type", () -> ParticleTypes.BLOCK);
	public static final Registry<BiomeSourceType<?, ?>> BIOME_SOURCE_TYPE = registerSimple("biome_source_type", () -> BiomeSourceType.VANILLA_LAYERED);
	public static final Registry<BlockEntityType<?>> BLOCK_ENTITY_TYPE = registerSimple("block_entity_type", () -> BlockEntityType.FURNACE);
	public static final Registry<ChunkGeneratorType<?, ?>> CHUNK_GENERATOR_TYPE = registerSimple("chunk_generator_type", () -> ChunkGeneratorType.FLAT);
	public static final Registry<DimensionType> DIMENSION_TYPE = registerSimple("dimension_type", () -> DimensionType.OVERWORLD);
	public static final DefaultedRegistry<Motive> MOTIVE = registerDefaulted("motive", "kebab", () -> Motive.KEBAB);
	public static final Registry<ResourceLocation> CUSTOM_STAT = registerSimple("custom_stat", () -> Stats.JUMP);
	public static final DefaultedRegistry<ChunkStatus> CHUNK_STATUS = registerDefaulted("chunk_status", "empty", () -> ChunkStatus.EMPTY);
	public static final Registry<StructureFeature<?>> STRUCTURE_FEATURE = registerSimple("structure_feature", () -> StructureFeatureIO.MINESHAFT);
	public static final Registry<StructurePieceType> STRUCTURE_PIECE = registerSimple("structure_piece", () -> StructurePieceType.MINE_SHAFT_ROOM);
	public static final Registry<RuleTestType> RULE_TEST = registerSimple("rule_test", () -> RuleTestType.ALWAYS_TRUE_TEST);
	public static final Registry<StructureProcessorType> STRUCTURE_PROCESSOR = registerSimple("structure_processor", () -> StructureProcessorType.BLOCK_IGNORE);
	public static final Registry<StructurePoolElementType> STRUCTURE_POOL_ELEMENT = registerSimple("structure_pool_element", () -> StructurePoolElementType.EMPTY);
	public static final Registry<MenuType<?>> MENU = registerSimple("menu", () -> MenuType.ANVIL);
	public static final Registry<RecipeType<?>> RECIPE_TYPE = registerSimple("recipe_type", () -> RecipeType.CRAFTING);
	public static final Registry<RecipeSerializer<?>> RECIPE_SERIALIZER = registerSimple("recipe_serializer", () -> RecipeSerializer.SHAPELESS_RECIPE);
	public static final Registry<StatType<?>> STAT_TYPE = registerSimple("stat_type", () -> Stats.ITEM_USED);
	public static final DefaultedRegistry<VillagerType> VILLAGER_TYPE = registerDefaulted("villager_type", "plains", () -> VillagerType.PLAINS);
	public static final DefaultedRegistry<VillagerProfession> VILLAGER_PROFESSION = registerDefaulted("villager_profession", "none", () -> VillagerProfession.NONE);
	public static final DefaultedRegistry<PoiType> POINT_OF_INTEREST_TYPE = registerDefaulted("point_of_interest_type", "unemployed", () -> PoiType.UNEMPLOYED);
	public static final DefaultedRegistry<MemoryModuleType<?>> MEMORY_MODULE_TYPE = registerDefaulted("memory_module_type", "dummy", () -> MemoryModuleType.DUMMY);
	public static final DefaultedRegistry<SensorType<?>> SENSOR_TYPE = registerDefaulted("sensor_type", "dummy", () -> SensorType.DUMMY);
	public static final Registry<Schedule> SCHEDULE = registerSimple("schedule", () -> Schedule.EMPTY);
	public static final Registry<Activity> ACTIVITY = registerSimple("activity", () -> Activity.IDLE);

	private static <T> Registry<T> registerSimple(String string, Supplier<T> supplier) {
		return internalRegister(string, new MappedRegistry<>(), supplier);
	}

	private static <T> DefaultedRegistry<T> registerDefaulted(String string, String string2, Supplier<T> supplier) {
		return internalRegister(string, new DefaultedRegistry<>(string2), supplier);
	}

	private static <T, R extends WritableRegistry<T>> R internalRegister(String string, R writableRegistry, Supplier<T> supplier) {
		ResourceLocation resourceLocation = new ResourceLocation(string);
		LOADERS.put(resourceLocation, supplier);
		return REGISTRY.register(resourceLocation, writableRegistry);
	}

	@Nullable
	public abstract ResourceLocation getKey(T object);

	public abstract int getId(@Nullable T object);

	@Nullable
	public abstract T get(@Nullable ResourceLocation resourceLocation);

	public abstract Optional<T> getOptional(@Nullable ResourceLocation resourceLocation);

	public abstract Set<ResourceLocation> keySet();

	public Stream<T> stream() {
		return StreamSupport.stream(this.spliterator(), false);
	}

	@Environment(EnvType.CLIENT)
	public abstract boolean containsKey(ResourceLocation resourceLocation);

	public static <T> T register(Registry<? super T> registry, String string, T object) {
		return register(registry, new ResourceLocation(string), object);
	}

	public static <T> T register(Registry<? super T> registry, ResourceLocation resourceLocation, T object) {
		return ((WritableRegistry)registry).register(resourceLocation, object);
	}

	public static <T> T registerMapping(Registry<? super T> registry, int i, String string, T object) {
		return ((WritableRegistry)registry).registerMapping(i, new ResourceLocation(string), object);
	}

	static {
		LOADERS.entrySet().forEach(entry -> {
			if (((Supplier)entry.getValue()).get() == null) {
				LOGGER.error("Unable to bootstrap registry '{}'", entry.getKey());
			}
		});
		REGISTRY.forEach(writableRegistry -> {
			if (writableRegistry.isEmpty()) {
				LOGGER.error("Registry '{}' was empty after loading", REGISTRY.getKey(writableRegistry));
				if (SharedConstants.IS_RUNNING_IN_IDE) {
					throw new IllegalStateException("Registry: '" + REGISTRY.getKey(writableRegistry) + "' is empty, not allowed, fix me!");
				}
			}

			if (writableRegistry instanceof DefaultedRegistry) {
				ResourceLocation resourceLocation = ((DefaultedRegistry)writableRegistry).getDefaultKey();
				Validate.notNull(writableRegistry.get(resourceLocation), "Missing default of DefaultedMappedRegistry: " + resourceLocation);
			}
		});
	}
}
