package net.minecraft.world.entity.npc;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class VillagerProfession {
	public static final VillagerProfession NONE = register("none", PoiType.UNEMPLOYED);
	public static final VillagerProfession ARMORER = register("armorer", PoiType.ARMORER);
	public static final VillagerProfession BUTCHER = register("butcher", PoiType.BUTCHER);
	public static final VillagerProfession CARTOGRAPHER = register("cartographer", PoiType.CARTOGRAPHER);
	public static final VillagerProfession CLERIC = register("cleric", PoiType.CLERIC);
	public static final VillagerProfession FARMER = register(
		"farmer", PoiType.FARMER, ImmutableSet.of(Items.WHEAT, Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS), ImmutableSet.of(Blocks.FARMLAND)
	);
	public static final VillagerProfession FISHERMAN = register("fisherman", PoiType.FISHERMAN);
	public static final VillagerProfession FLETCHER = register("fletcher", PoiType.FLETCHER);
	public static final VillagerProfession LEATHERWORKER = register("leatherworker", PoiType.LEATHERWORKER);
	public static final VillagerProfession LIBRARIAN = register("librarian", PoiType.LIBRARIAN);
	public static final VillagerProfession MASON = register("mason", PoiType.MASON);
	public static final VillagerProfession NITWIT = register("nitwit", PoiType.NITWIT);
	public static final VillagerProfession SHEPHERD = register("shepherd", PoiType.SHEPHERD);
	public static final VillagerProfession TOOLSMITH = register("toolsmith", PoiType.TOOLSMITH);
	public static final VillagerProfession WEAPONSMITH = register("weaponsmith", PoiType.WEAPONSMITH);
	private final String name;
	private final PoiType jobPoiType;
	private final ImmutableSet<Item> requestedItems;
	private final ImmutableSet<Block> secondaryPoi;

	private VillagerProfession(String string, PoiType poiType, ImmutableSet<Item> immutableSet, ImmutableSet<Block> immutableSet2) {
		this.name = string;
		this.jobPoiType = poiType;
		this.requestedItems = immutableSet;
		this.secondaryPoi = immutableSet2;
	}

	public PoiType getJobPoiType() {
		return this.jobPoiType;
	}

	public ImmutableSet<Item> getRequestedItems() {
		return this.requestedItems;
	}

	public ImmutableSet<Block> getSecondaryPoi() {
		return this.secondaryPoi;
	}

	public String toString() {
		return this.name;
	}

	static VillagerProfession register(String string, PoiType poiType) {
		return register(string, poiType, ImmutableSet.of(), ImmutableSet.of());
	}

	static VillagerProfession register(String string, PoiType poiType, ImmutableSet<Item> immutableSet, ImmutableSet<Block> immutableSet2) {
		return Registry.register(Registry.VILLAGER_PROFESSION, new ResourceLocation(string), new VillagerProfession(string, poiType, immutableSet, immutableSet2));
	}
}
