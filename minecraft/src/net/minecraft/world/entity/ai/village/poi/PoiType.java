package net.minecraft.world.entity.ai.village.poi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

public class PoiType {
	private static final Predicate<PoiType> ALL_JOBS = poiType -> ((Set)Registry.VILLAGER_PROFESSION
				.stream()
				.map(VillagerProfession::getJobPoiType)
				.collect(Collectors.toSet()))
			.contains(poiType);
	public static final Predicate<PoiType> ALL = poiType -> true;
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
	private static final Map<BlockState, PoiType> TYPE_BY_STATE = Maps.<BlockState, PoiType>newHashMap();
	public static final PoiType UNEMPLOYED = register("unemployed", ImmutableSet.of(), 1, null, ALL_JOBS, 1);
	public static final PoiType ARMORER = register("armorer", getBlockStates(Blocks.BLAST_FURNACE), 1, SoundEvents.VILLAGER_WORK_ARMORER, 1);
	public static final PoiType BUTCHER = register("butcher", getBlockStates(Blocks.SMOKER), 1, SoundEvents.VILLAGER_WORK_BUTCHER, 1);
	public static final PoiType CARTOGRAPHER = register("cartographer", getBlockStates(Blocks.CARTOGRAPHY_TABLE), 1, SoundEvents.VILLAGER_WORK_CARTOGRAPHER, 1);
	public static final PoiType CLERIC = register("cleric", getBlockStates(Blocks.BREWING_STAND), 1, SoundEvents.VILLAGER_WORK_CLERIC, 1);
	public static final PoiType FARMER = register("farmer", getBlockStates(Blocks.COMPOSTER), 1, SoundEvents.VILLAGER_WORK_FARMER, 1);
	public static final PoiType FISHERMAN = register("fisherman", getBlockStates(Blocks.BARREL), 1, SoundEvents.VILLAGER_WORK_FISHERMAN, 1);
	public static final PoiType FLETCHER = register("fletcher", getBlockStates(Blocks.FLETCHING_TABLE), 1, SoundEvents.VILLAGER_WORK_FLETCHER, 1);
	public static final PoiType LEATHERWORKER = register("leatherworker", getBlockStates(Blocks.CAULDRON), 1, SoundEvents.VILLAGER_WORK_LEATHERWORKER, 1);
	public static final PoiType LIBRARIAN = register("librarian", getBlockStates(Blocks.LECTERN), 1, SoundEvents.VILLAGER_WORK_LIBRARIAN, 1);
	public static final PoiType MASON = register("mason", getBlockStates(Blocks.STONECUTTER), 1, SoundEvents.VILLAGER_WORK_MASON, 1);
	public static final PoiType NITWIT = register("nitwit", ImmutableSet.of(), 1, null, 1);
	public static final PoiType SHEPHERD = register("shepherd", getBlockStates(Blocks.LOOM), 1, SoundEvents.VILLAGER_WORK_SHEPHERD, 1);
	public static final PoiType TOOLSMITH = register("toolsmith", getBlockStates(Blocks.SMITHING_TABLE), 1, SoundEvents.VILLAGER_WORK_TOOLSMITH, 1);
	public static final PoiType WEAPONSMITH = register("weaponsmith", getBlockStates(Blocks.GRINDSTONE), 1, SoundEvents.VILLAGER_WORK_WEAPONSMITH, 1);
	public static final PoiType HOME = register("home", BEDS, 1, null, 1);
	public static final PoiType MEETING = register("meeting", getBlockStates(Blocks.BELL), 32, null, 6);
	private final String name;
	private final Set<BlockState> matchingStates;
	private final int maxTickets;
	@Nullable
	private final SoundEvent soundEvent;
	private final Predicate<PoiType> predicate;
	private final int validRange;

	private static Set<BlockState> getBlockStates(Block block) {
		return ImmutableSet.copyOf(block.getStateDefinition().getPossibleStates());
	}

	private PoiType(String string, Set<BlockState> set, int i, @Nullable SoundEvent soundEvent, Predicate<PoiType> predicate, int j) {
		this.name = string;
		this.matchingStates = ImmutableSet.copyOf(set);
		this.maxTickets = i;
		this.soundEvent = soundEvent;
		this.predicate = predicate;
		this.validRange = j;
	}

	private PoiType(String string, Set<BlockState> set, int i, @Nullable SoundEvent soundEvent, int j) {
		this.name = string;
		this.matchingStates = ImmutableSet.copyOf(set);
		this.maxTickets = i;
		this.soundEvent = soundEvent;
		this.predicate = poiType -> poiType == this;
		this.validRange = j;
	}

	public int getMaxTickets() {
		return this.maxTickets;
	}

	public Predicate<PoiType> getPredicate() {
		return this.predicate;
	}

	public int getValidRange() {
		return this.validRange;
	}

	public String toString() {
		return this.name;
	}

	@Nullable
	public SoundEvent getUseSound() {
		return this.soundEvent;
	}

	private static PoiType register(String string, Set<BlockState> set, int i, @Nullable SoundEvent soundEvent, int j) {
		return registerBlockStates(Registry.POINT_OF_INTEREST_TYPE.register(new ResourceLocation(string), new PoiType(string, set, i, soundEvent, j)));
	}

	private static PoiType register(String string, Set<BlockState> set, int i, @Nullable SoundEvent soundEvent, Predicate<PoiType> predicate, int j) {
		return registerBlockStates(Registry.POINT_OF_INTEREST_TYPE.register(new ResourceLocation(string), new PoiType(string, set, i, soundEvent, predicate, j)));
	}

	private static PoiType registerBlockStates(PoiType poiType) {
		poiType.matchingStates.forEach(blockState -> {
			PoiType poiType2 = (PoiType)TYPE_BY_STATE.put(blockState, poiType);
			if (poiType2 != null) {
				throw new IllegalStateException(String.format("%s is defined in too many tags", blockState));
			}
		});
		return poiType;
	}

	public static Optional<PoiType> forState(BlockState blockState) {
		return Optional.ofNullable(TYPE_BY_STATE.get(blockState));
	}

	public static Stream<BlockState> allPoiStates() {
		return TYPE_BY_STATE.keySet().stream();
	}
}
