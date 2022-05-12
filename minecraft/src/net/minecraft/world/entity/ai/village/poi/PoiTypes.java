package net.minecraft.world.entity.ai.village.poi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

public class PoiTypes {
	public static final ResourceKey<PoiType> ARMORER = createKey("armorer");
	public static final ResourceKey<PoiType> BUTCHER = createKey("butcher");
	public static final ResourceKey<PoiType> CARTOGRAPHER = createKey("cartographer");
	public static final ResourceKey<PoiType> CLERIC = createKey("cleric");
	public static final ResourceKey<PoiType> FARMER = createKey("farmer");
	public static final ResourceKey<PoiType> FISHERMAN = createKey("fisherman");
	public static final ResourceKey<PoiType> FLETCHER = createKey("fletcher");
	public static final ResourceKey<PoiType> LEATHERWORKER = createKey("leatherworker");
	public static final ResourceKey<PoiType> LIBRARIAN = createKey("librarian");
	public static final ResourceKey<PoiType> MASON = createKey("mason");
	public static final ResourceKey<PoiType> SHEPHERD = createKey("shepherd");
	public static final ResourceKey<PoiType> TOOLSMITH = createKey("toolsmith");
	public static final ResourceKey<PoiType> WEAPONSMITH = createKey("weaponsmith");
	public static final ResourceKey<PoiType> HOME = createKey("home");
	public static final ResourceKey<PoiType> MEETING = createKey("meeting");
	public static final ResourceKey<PoiType> BEEHIVE = createKey("beehive");
	public static final ResourceKey<PoiType> BEE_NEST = createKey("bee_nest");
	public static final ResourceKey<PoiType> NETHER_PORTAL = createKey("nether_portal");
	public static final ResourceKey<PoiType> LODESTONE = createKey("lodestone");
	public static final ResourceKey<PoiType> LIGHTNING_ROD = createKey("lightning_rod");
	private static final Set<BlockState> BEDS = (Set<BlockState>)ImmutableList.of(
			Blocks.RED_BED,
			Blocks.BLACK_BED,
			Blocks.BLUE_BED,
			Blocks.BROWN_BED,
			Blocks.CYAN_BED,
			Blocks.GRAY_BED,
			Blocks.GREEN_BED,
			Blocks.LIGHT_BLUE_BED,
			Blocks.LIGHT_GRAY_BED,
			Blocks.LIME_BED,
			Blocks.MAGENTA_BED,
			Blocks.ORANGE_BED,
			Blocks.PINK_BED,
			Blocks.PURPLE_BED,
			Blocks.WHITE_BED,
			Blocks.YELLOW_BED
		)
		.stream()
		.flatMap(block -> block.getStateDefinition().getPossibleStates().stream())
		.filter(blockState -> blockState.getValue(BedBlock.PART) == BedPart.HEAD)
		.collect(ImmutableSet.toImmutableSet());
	private static final Set<BlockState> CAULDRONS = (Set<BlockState>)ImmutableList.of(
			Blocks.CAULDRON, Blocks.LAVA_CAULDRON, Blocks.WATER_CAULDRON, Blocks.POWDER_SNOW_CAULDRON
		)
		.stream()
		.flatMap(block -> block.getStateDefinition().getPossibleStates().stream())
		.collect(ImmutableSet.toImmutableSet());
	private static final Map<BlockState, Holder<PoiType>> TYPE_BY_STATE = Maps.<BlockState, Holder<PoiType>>newHashMap();
	protected static final Set<BlockState> ALL_STATES = new ObjectOpenHashSet<>(TYPE_BY_STATE.keySet());

	private static Set<BlockState> getBlockStates(Block block) {
		return ImmutableSet.copyOf(block.getStateDefinition().getPossibleStates());
	}

	private static ResourceKey<PoiType> createKey(String string) {
		return ResourceKey.create(Registry.POINT_OF_INTEREST_TYPE_REGISTRY, new ResourceLocation(string));
	}

	private static PoiType register(Registry<PoiType> registry, ResourceKey<PoiType> resourceKey, Set<BlockState> set, int i, int j) {
		PoiType poiType = new PoiType(set, i, j);
		Registry.register(registry, resourceKey, poiType);
		registerBlockStates(registry.getHolderOrThrow(resourceKey));
		return poiType;
	}

	private static void registerBlockStates(Holder<PoiType> holder) {
		holder.value().matchingStates().forEach(blockState -> {
			Holder<PoiType> holder2 = (Holder<PoiType>)TYPE_BY_STATE.put(blockState, holder);
			if (holder2 != null) {
				throw (IllegalStateException)Util.pauseInIde(new IllegalStateException(String.format("%s is defined in more than one PoI type", blockState)));
			}
		});
	}

	public static Optional<Holder<PoiType>> forState(BlockState blockState) {
		return Optional.ofNullable((Holder)TYPE_BY_STATE.get(blockState));
	}

	public static PoiType bootstrap(Registry<PoiType> registry) {
		register(registry, ARMORER, getBlockStates(Blocks.BLAST_FURNACE), 1, 1);
		register(registry, BUTCHER, getBlockStates(Blocks.SMOKER), 1, 1);
		register(registry, CARTOGRAPHER, getBlockStates(Blocks.CARTOGRAPHY_TABLE), 1, 1);
		register(registry, CLERIC, getBlockStates(Blocks.BREWING_STAND), 1, 1);
		register(registry, FARMER, getBlockStates(Blocks.COMPOSTER), 1, 1);
		register(registry, FISHERMAN, getBlockStates(Blocks.BARREL), 1, 1);
		register(registry, FLETCHER, getBlockStates(Blocks.FLETCHING_TABLE), 1, 1);
		register(registry, LEATHERWORKER, CAULDRONS, 1, 1);
		register(registry, LIBRARIAN, getBlockStates(Blocks.LECTERN), 1, 1);
		register(registry, MASON, getBlockStates(Blocks.STONECUTTER), 1, 1);
		register(registry, SHEPHERD, getBlockStates(Blocks.LOOM), 1, 1);
		register(registry, TOOLSMITH, getBlockStates(Blocks.SMITHING_TABLE), 1, 1);
		register(registry, WEAPONSMITH, getBlockStates(Blocks.GRINDSTONE), 1, 1);
		register(registry, HOME, BEDS, 1, 1);
		register(registry, MEETING, getBlockStates(Blocks.BELL), 32, 6);
		register(registry, BEEHIVE, getBlockStates(Blocks.BEEHIVE), 0, 1);
		register(registry, BEE_NEST, getBlockStates(Blocks.BEE_NEST), 0, 1);
		register(registry, NETHER_PORTAL, getBlockStates(Blocks.NETHER_PORTAL), 0, 1);
		register(registry, LODESTONE, getBlockStates(Blocks.LODESTONE), 0, 1);
		return register(registry, LIGHTNING_ROD, getBlockStates(Blocks.LIGHTNING_ROD), 0, 1);
	}
}
