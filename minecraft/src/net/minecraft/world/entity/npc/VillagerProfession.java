package net.minecraft.world.entity.npc;

import com.google.common.collect.ImmutableSet;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class VillagerProfession {
	public static final VillagerProfession NONE = register("none", PoiType.UNEMPLOYED, null);
	public static final VillagerProfession ARMORER = register("armorer", PoiType.ARMORER, SoundEvents.VILLAGER_WORK_ARMORER);
	public static final VillagerProfession BUTCHER = register("butcher", PoiType.BUTCHER, SoundEvents.VILLAGER_WORK_BUTCHER);
	public static final VillagerProfession CARTOGRAPHER = register("cartographer", PoiType.CARTOGRAPHER, SoundEvents.VILLAGER_WORK_CARTOGRAPHER);
	public static final VillagerProfession CLERIC = register("cleric", PoiType.CLERIC, SoundEvents.VILLAGER_WORK_CLERIC);
	public static final VillagerProfession FARMER = register(
		"farmer",
		PoiType.FARMER,
		ImmutableSet.of(Items.WHEAT, Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS),
		ImmutableSet.of(Blocks.FARMLAND),
		SoundEvents.VILLAGER_WORK_FARMER
	);
	public static final VillagerProfession FISHERMAN = register("fisherman", PoiType.FISHERMAN, SoundEvents.VILLAGER_WORK_FISHERMAN);
	public static final VillagerProfession FLETCHER = register("fletcher", PoiType.FLETCHER, SoundEvents.VILLAGER_WORK_FLETCHER);
	public static final VillagerProfession LEATHERWORKER = register("leatherworker", PoiType.LEATHERWORKER, SoundEvents.VILLAGER_WORK_LEATHERWORKER);
	public static final VillagerProfession LIBRARIAN = register("librarian", PoiType.LIBRARIAN, SoundEvents.VILLAGER_WORK_LIBRARIAN);
	public static final VillagerProfession MASON = register("mason", PoiType.MASON, SoundEvents.VILLAGER_WORK_MASON);
	public static final VillagerProfession NITWIT = register("nitwit", PoiType.NITWIT, null);
	public static final VillagerProfession SHEPHERD = register("shepherd", PoiType.SHEPHERD, SoundEvents.VILLAGER_WORK_SHEPHERD);
	public static final VillagerProfession TOOLSMITH = register("toolsmith", PoiType.TOOLSMITH, SoundEvents.VILLAGER_WORK_TOOLSMITH);
	public static final VillagerProfession WEAPONSMITH = register("weaponsmith", PoiType.WEAPONSMITH, SoundEvents.VILLAGER_WORK_WEAPONSMITH);
	private final String name;
	private final PoiType jobPoiType;
	private final ImmutableSet<Item> requestedItems;
	private final ImmutableSet<Block> secondaryPoi;
	@Nullable
	private final SoundEvent workSound;

	private VillagerProfession(String string, PoiType poiType, ImmutableSet<Item> immutableSet, ImmutableSet<Block> immutableSet2, @Nullable SoundEvent soundEvent) {
		this.name = string;
		this.jobPoiType = poiType;
		this.requestedItems = immutableSet;
		this.secondaryPoi = immutableSet2;
		this.workSound = soundEvent;
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

	@Nullable
	public SoundEvent getWorkSound() {
		return this.workSound;
	}

	public String toString() {
		return this.name;
	}

	static VillagerProfession register(String string, PoiType poiType, @Nullable SoundEvent soundEvent) {
		return register(string, poiType, ImmutableSet.of(), ImmutableSet.of(), soundEvent);
	}

	static VillagerProfession register(
		String string, PoiType poiType, ImmutableSet<Item> immutableSet, ImmutableSet<Block> immutableSet2, @Nullable SoundEvent soundEvent
	) {
		return Registry.register(
			Registry.VILLAGER_PROFESSION, new ResourceLocation(string), new VillagerProfession(string, poiType, immutableSet, immutableSet2, soundEvent)
		);
	}
}
