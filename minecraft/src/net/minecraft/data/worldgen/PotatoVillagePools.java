package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.placement.VillagePlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import org.apache.commons.lang3.tuple.Triple;

public class PotatoVillagePools {
	public static final ResourceKey<StructureTemplatePool> START = Pools.createKey("village/potato/town_centers");
	private static final ResourceKey<StructureTemplatePool> TERMINATORS_KEY = Pools.createKey("village/potato/terminators");

	public static void bootstrap(BootstrapContext<StructureTemplatePool> bootstrapContext) {
		HolderGetter<PlacedFeature> holderGetter = bootstrapContext.lookup(Registries.PLACED_FEATURE);
		Holder<PlacedFeature> holder = holderGetter.getOrThrow(VillagePlacements.POTATO_VILLAGE);
		Holder<PlacedFeature> holder2 = holderGetter.getOrThrow(VillagePlacements.FLOWER_PLAIN_VILLAGE);
		Holder<PlacedFeature> holder3 = holderGetter.getOrThrow(VillagePlacements.PILE_POTATO_FRUIT_VILLAGE);
		HolderGetter<StructureProcessorList> holderGetter2 = bootstrapContext.lookup(Registries.PROCESSOR_LIST);
		Holder<StructureProcessorList> holder4 = holderGetter2.getOrThrow(ProcessorLists.SPOIL_10_PERCENT);
		Holder<StructureProcessorList> holder5 = holderGetter2.getOrThrow(ProcessorLists.SPOIL_20_PERCENT);
		Holder<StructureProcessorList> holder6 = holderGetter2.getOrThrow(ProcessorLists.SPOIL_70_PERCENT);
		Holder<StructureProcessorList> holder7 = holderGetter2.getOrThrow(ProcessorLists.ZOMBIE_POTATO);
		Holder<StructureProcessorList> holder8 = holderGetter2.getOrThrow(ProcessorLists.STREET_POTATO);
		Holder<StructureProcessorList> holder9 = holderGetter2.getOrThrow(ProcessorLists.FARM_POTATO);
		HolderGetter<StructureTemplatePool> holderGetter3 = bootstrapContext.lookup(Registries.TEMPLATE_POOL);
		Holder<StructureTemplatePool> holder10 = holderGetter3.getOrThrow(Pools.EMPTY);
		Holder<StructureTemplatePool> holder11 = holderGetter3.getOrThrow(TERMINATORS_KEY);
		bootstrapContext.register(
			START,
			new StructureTemplatePool(
				holder10,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/potato/town_centers/plains_fountain_01", holder5), 50),
					Pair.of(StructurePoolElement.legacy("village/potato/town_centers/plains_meeting_point_1", holder5), 50),
					Pair.of(StructurePoolElement.legacy("village/potato/town_centers/plains_meeting_point_2"), 50),
					Pair.of(StructurePoolElement.legacy("village/potato/town_centers/plains_meeting_point_3", holder6), 50),
					Pair.of(StructurePoolElement.legacy("village/potato/zombie/town_centers/plains_fountain_01", holder7), 1),
					Pair.of(StructurePoolElement.legacy("village/potato/zombie/town_centers/plains_meeting_point_1", holder7), 1),
					Pair.of(StructurePoolElement.legacy("village/potato/zombie/town_centers/plains_meeting_point_2", holder7), 1),
					Pair.of(StructurePoolElement.legacy("village/potato/zombie/town_centers/plains_meeting_point_3", holder7), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"village/potato/streets",
			new StructureTemplatePool(
				ImmutableList.of(
					Triple.of(StructurePoolElement.legacy("village/potato/streets/corner_01", holder8), 2, StructureTemplatePool.Projection.TERRAIN_MATCHING),
					Triple.of(StructurePoolElement.legacy("village/potato/streets/corner_02", holder8), 2, StructureTemplatePool.Projection.TERRAIN_MATCHING),
					Triple.of(StructurePoolElement.legacy("village/potato/streets/corner_03", holder8), 2, StructureTemplatePool.Projection.TERRAIN_MATCHING),
					Triple.of(StructurePoolElement.legacy("village/potato/streets/straight_01", holder8), 4, StructureTemplatePool.Projection.TERRAIN_MATCHING),
					Triple.of(StructurePoolElement.legacy("village/potato/streets/straight_02", holder8), 4, StructureTemplatePool.Projection.TERRAIN_MATCHING),
					Triple.of(StructurePoolElement.legacy("village/potato/streets/straight_03", holder8), 7, StructureTemplatePool.Projection.TERRAIN_MATCHING),
					Triple.of(StructurePoolElement.legacy("village/potato/streets/straight_04", holder8), 7, StructureTemplatePool.Projection.TERRAIN_MATCHING),
					Triple.of(StructurePoolElement.legacy("village/potato/streets/straight_05", holder8), 3, StructureTemplatePool.Projection.TERRAIN_MATCHING),
					Triple.of(StructurePoolElement.legacy("village/potato/streets/straight_06", holder8), 4, StructureTemplatePool.Projection.TERRAIN_MATCHING),
					Triple.of(StructurePoolElement.legacy("village/potato/streets/crossroad_01", holder8), 2, StructureTemplatePool.Projection.TERRAIN_MATCHING),
					Triple.of(StructurePoolElement.legacy("village/potato/streets/crossroad_02", holder8), 1, StructureTemplatePool.Projection.TERRAIN_MATCHING),
					Triple.of(StructurePoolElement.legacy("village/potato/streets/crossroad_03", holder8), 2, StructureTemplatePool.Projection.TERRAIN_MATCHING),
					Triple.of(StructurePoolElement.legacy("village/potato/streets/crossroad_04", holder8), 2, StructureTemplatePool.Projection.TERRAIN_MATCHING),
					Triple.of(StructurePoolElement.legacy("village/potato/streets/crossroad_05", holder8), 2, StructureTemplatePool.Projection.TERRAIN_MATCHING),
					Triple.of(StructurePoolElement.legacy("village/potato/streets/crossroad_06", holder8), 2, StructureTemplatePool.Projection.TERRAIN_MATCHING),
					Triple.of(StructurePoolElement.legacy("village/potato/streets/turn_01", holder8), 3, StructureTemplatePool.Projection.TERRAIN_MATCHING),
					Triple.of(StructurePoolElement.legacy("village/potato/houses/potato_maze"), 2, StructureTemplatePool.Projection.RIGID)
				),
				holder11
			)
		);
		Pools.register(
			bootstrapContext,
			"village/potato/zombie/streets",
			new StructureTemplatePool(
				ImmutableList.of(
					Triple.of(StructurePoolElement.legacy("village/potato/zombie/streets/corner_01", holder8), 2, StructureTemplatePool.Projection.TERRAIN_MATCHING),
					Triple.of(StructurePoolElement.legacy("village/potato/zombie/streets/corner_02", holder8), 2, StructureTemplatePool.Projection.TERRAIN_MATCHING),
					Triple.of(StructurePoolElement.legacy("village/potato/zombie/streets/corner_03", holder8), 2, StructureTemplatePool.Projection.TERRAIN_MATCHING),
					Triple.of(StructurePoolElement.legacy("village/potato/zombie/streets/straight_01", holder8), 4, StructureTemplatePool.Projection.TERRAIN_MATCHING),
					Triple.of(StructurePoolElement.legacy("village/potato/zombie/streets/straight_02", holder8), 4, StructureTemplatePool.Projection.TERRAIN_MATCHING),
					Triple.of(StructurePoolElement.legacy("village/potato/zombie/streets/straight_03", holder8), 7, StructureTemplatePool.Projection.TERRAIN_MATCHING),
					Triple.of(StructurePoolElement.legacy("village/potato/zombie/streets/straight_04", holder8), 7, StructureTemplatePool.Projection.TERRAIN_MATCHING),
					Triple.of(StructurePoolElement.legacy("village/potato/zombie/streets/straight_05", holder8), 3, StructureTemplatePool.Projection.TERRAIN_MATCHING),
					Triple.of(StructurePoolElement.legacy("village/potato/zombie/streets/straight_06", holder8), 4, StructureTemplatePool.Projection.TERRAIN_MATCHING),
					Triple.of(StructurePoolElement.legacy("village/potato/zombie/streets/crossroad_01", holder8), 2, StructureTemplatePool.Projection.TERRAIN_MATCHING),
					Triple.of(StructurePoolElement.legacy("village/potato/zombie/streets/crossroad_02", holder8), 1, StructureTemplatePool.Projection.TERRAIN_MATCHING),
					Triple.of(StructurePoolElement.legacy("village/potato/zombie/streets/crossroad_03", holder8), 2, StructureTemplatePool.Projection.TERRAIN_MATCHING),
					Triple.of(StructurePoolElement.legacy("village/potato/zombie/streets/crossroad_04", holder8), 2, StructureTemplatePool.Projection.TERRAIN_MATCHING),
					Triple.of(StructurePoolElement.legacy("village/potato/zombie/streets/crossroad_05", holder8), 2, StructureTemplatePool.Projection.TERRAIN_MATCHING),
					Triple.of(StructurePoolElement.legacy("village/potato/zombie/streets/crossroad_06", holder8), 2, StructureTemplatePool.Projection.TERRAIN_MATCHING),
					Triple.of(StructurePoolElement.legacy("village/potato/zombie/streets/turn_01", holder8), 3, StructureTemplatePool.Projection.TERRAIN_MATCHING),
					Triple.of(StructurePoolElement.legacy("village/potato/houses/potato_maze"), 1, StructureTemplatePool.Projection.RIGID)
				),
				holder11
			)
		);
		Pools.register(
			bootstrapContext,
			"village/potato/houses",
			new StructureTemplatePool(
				holder11,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_small_house_1", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_small_house_2", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_small_house_3", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_small_house_4", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_small_house_5", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_small_house_6", holder4), 1),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_small_house_7", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_small_house_8", holder4), 3),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_medium_house_1", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_medium_house_2", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_big_house_1", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_butcher_shop_1", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_butcher_shop_2", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_tool_smith_1", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_fletcher_house_1", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_shepherds_house_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_armorer_house_1", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_fisher_cottage_1", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_tannery_1", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_cartographer_1", holder4), 1),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_library_1", holder4), 5),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_library_2", holder4), 1),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_masons_house_1", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_weaponsmith_1", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_temple_3", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_temple_4", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_stable_1", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_stable_2"), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_large_farm_1", holder9), 4),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_small_farm_1", holder9), 4),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_animal_pen_1"), 1),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_animal_pen_2"), 1),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_animal_pen_3"), 5),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_accessory_1"), 1),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_meeting_point_4", holder6), 3),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_meeting_point_5"), 1),
					Pair.of(StructurePoolElement.empty(), 10)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"village/potato/zombie/houses",
			new StructureTemplatePool(
				holder11,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/potato/zombie/houses/plains_small_house_1", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/zombie/houses/plains_small_house_2", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/zombie/houses/plains_small_house_3", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/zombie/houses/plains_small_house_4", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/zombie/houses/plains_small_house_5", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/zombie/houses/plains_small_house_6", holder7), 1),
					Pair.of(StructurePoolElement.legacy("village/potato/zombie/houses/plains_small_house_7", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/zombie/houses/plains_small_house_8", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/zombie/houses/plains_medium_house_1", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/zombie/houses/plains_medium_house_2", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/zombie/houses/plains_big_house_1", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_butcher_shop_1", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/zombie/houses/plains_butcher_shop_2", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_tool_smith_1", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/zombie/houses/plains_fletcher_house_1", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/zombie/houses/plains_shepherds_house_1", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_armorer_house_1", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_fisher_cottage_1", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_tannery_1", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_cartographer_1", holder7), 1),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_library_1", holder7), 3),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_library_2", holder7), 1),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_masons_house_1", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_weaponsmith_1", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_temple_3", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_temple_4", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/zombie/houses/plains_stable_1", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_stable_2", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_large_farm_1", holder7), 4),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_small_farm_1", holder7), 4),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_animal_pen_1", holder7), 1),
					Pair.of(StructurePoolElement.legacy("village/potato/houses/plains_animal_pen_2", holder7), 1),
					Pair.of(StructurePoolElement.legacy("village/potato/zombie/houses/plains_animal_pen_3", holder7), 5),
					Pair.of(StructurePoolElement.legacy("village/potato/zombie/houses/plains_meeting_point_4", holder7), 3),
					Pair.of(StructurePoolElement.legacy("village/potato/zombie/houses/plains_meeting_point_5", holder7), 1),
					Pair.of(StructurePoolElement.empty(), 10)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		bootstrapContext.register(
			TERMINATORS_KEY,
			new StructureTemplatePool(
				holder10,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/potato/terminators/terminator_01", holder8), 1),
					Pair.of(StructurePoolElement.legacy("village/potato/terminators/terminator_02", holder8), 1),
					Pair.of(StructurePoolElement.legacy("village/potato/terminators/terminator_03", holder8), 1),
					Pair.of(StructurePoolElement.legacy("village/potato/terminators/terminator_04", holder8), 1)
				),
				StructureTemplatePool.Projection.TERRAIN_MATCHING
			)
		);
		Pools.register(
			bootstrapContext,
			"village/potato/trees",
			new StructureTemplatePool(holder10, ImmutableList.of(Pair.of(StructurePoolElement.feature(holder), 1)), StructureTemplatePool.Projection.RIGID)
		);
		Pools.register(
			bootstrapContext,
			"village/potato/decor",
			new StructureTemplatePool(
				holder10,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/potato/plains_lamp_1"), 3),
					Pair.of(StructurePoolElement.feature(holder), 1),
					Pair.of(StructurePoolElement.feature(holder2), 1),
					Pair.of(StructurePoolElement.feature(holder3), 1),
					Pair.of(StructurePoolElement.legacy("village/potato/frying_table_1"), 1),
					Pair.of(StructurePoolElement.empty(), 2)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"village/potato/zombie/decor",
			new StructureTemplatePool(
				holder10,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/potato/plains_lamp_1", holder7), 1),
					Pair.of(StructurePoolElement.feature(holder), 1),
					Pair.of(StructurePoolElement.feature(holder2), 1),
					Pair.of(StructurePoolElement.feature(holder3), 1),
					Pair.of(StructurePoolElement.empty(), 2)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"village/potato/villagers",
			new StructureTemplatePool(
				holder10,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/potato/villagers/nitwit"), 1),
					Pair.of(StructurePoolElement.legacy("village/potato/villagers/baby"), 1),
					Pair.of(StructurePoolElement.legacy("village/potato/villagers/unemployed"), 10)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"village/potato/zombie/villagers",
			new StructureTemplatePool(
				holder10,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/potato/zombie/villagers/nitwit"), 1),
					Pair.of(StructurePoolElement.legacy("village/potato/zombie/villagers/unemployed"), 10)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"village/potato/well_bottoms",
			new StructureTemplatePool(
				holder10, ImmutableList.of(Pair.of(StructurePoolElement.legacy("village/potato/well_bottom"), 1)), StructureTemplatePool.Projection.RIGID
			)
		);
	}
}
