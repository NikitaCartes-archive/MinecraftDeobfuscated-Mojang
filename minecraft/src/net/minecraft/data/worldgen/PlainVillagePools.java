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

public class PlainVillagePools {
	public static final ResourceKey<StructureTemplatePool> START = Pools.createKey("village/plains/town_centers");
	private static final ResourceKey<StructureTemplatePool> TERMINATORS_KEY = Pools.createKey("village/plains/terminators");

	public static void bootstrap(BootstrapContext<StructureTemplatePool> bootstrapContext) {
		HolderGetter<PlacedFeature> holderGetter = bootstrapContext.lookup(Registries.PLACED_FEATURE);
		Holder<PlacedFeature> holder = holderGetter.getOrThrow(VillagePlacements.OAK_VILLAGE);
		Holder<PlacedFeature> holder2 = holderGetter.getOrThrow(VillagePlacements.FLOWER_PLAIN_VILLAGE);
		Holder<PlacedFeature> holder3 = holderGetter.getOrThrow(VillagePlacements.PILE_HAY_VILLAGE);
		HolderGetter<StructureProcessorList> holderGetter2 = bootstrapContext.lookup(Registries.PROCESSOR_LIST);
		Holder<StructureProcessorList> holder4 = holderGetter2.getOrThrow(ProcessorLists.MOSSIFY_10_PERCENT);
		Holder<StructureProcessorList> holder5 = holderGetter2.getOrThrow(ProcessorLists.MOSSIFY_20_PERCENT);
		Holder<StructureProcessorList> holder6 = holderGetter2.getOrThrow(ProcessorLists.MOSSIFY_70_PERCENT);
		Holder<StructureProcessorList> holder7 = holderGetter2.getOrThrow(ProcessorLists.ZOMBIE_PLAINS);
		Holder<StructureProcessorList> holder8 = holderGetter2.getOrThrow(ProcessorLists.STREET_PLAINS);
		Holder<StructureProcessorList> holder9 = holderGetter2.getOrThrow(ProcessorLists.FARM_PLAINS);
		HolderGetter<StructureTemplatePool> holderGetter3 = bootstrapContext.lookup(Registries.TEMPLATE_POOL);
		Holder<StructureTemplatePool> holder10 = holderGetter3.getOrThrow(Pools.EMPTY);
		Holder<StructureTemplatePool> holder11 = holderGetter3.getOrThrow(TERMINATORS_KEY);
		bootstrapContext.register(
			START,
			new StructureTemplatePool(
				holder10,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/plains/town_centers/plains_fountain_01", holder5), 50),
					Pair.of(StructurePoolElement.legacy("village/plains/town_centers/plains_meeting_point_1", holder5), 50),
					Pair.of(StructurePoolElement.legacy("village/plains/town_centers/plains_meeting_point_2"), 50),
					Pair.of(StructurePoolElement.legacy("village/plains/town_centers/plains_meeting_point_3", holder6), 50),
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/town_centers/plains_fountain_01", holder7), 1),
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/town_centers/plains_meeting_point_1", holder7), 1),
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/town_centers/plains_meeting_point_2", holder7), 1),
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/town_centers/plains_meeting_point_3", holder7), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"village/plains/streets",
			new StructureTemplatePool(
				holder11,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/plains/streets/corner_01", holder8), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/streets/corner_02", holder8), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/streets/corner_03", holder8), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/streets/straight_01", holder8), 4),
					Pair.of(StructurePoolElement.legacy("village/plains/streets/straight_02", holder8), 4),
					Pair.of(StructurePoolElement.legacy("village/plains/streets/straight_03", holder8), 7),
					Pair.of(StructurePoolElement.legacy("village/plains/streets/straight_04", holder8), 7),
					Pair.of(StructurePoolElement.legacy("village/plains/streets/straight_05", holder8), 3),
					Pair.of(StructurePoolElement.legacy("village/plains/streets/straight_06", holder8), 4),
					Pair.of(StructurePoolElement.legacy("village/plains/streets/crossroad_01", holder8), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/streets/crossroad_02", holder8), 1),
					Pair.of(StructurePoolElement.legacy("village/plains/streets/crossroad_03", holder8), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/streets/crossroad_04", holder8), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/streets/crossroad_05", holder8), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/streets/crossroad_06", holder8), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/streets/turn_01", holder8), 3)
				),
				StructureTemplatePool.Projection.TERRAIN_MATCHING
			)
		);
		Pools.register(
			bootstrapContext,
			"village/plains/zombie/streets",
			new StructureTemplatePool(
				holder11,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/streets/corner_01", holder8), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/streets/corner_02", holder8), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/streets/corner_03", holder8), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/streets/straight_01", holder8), 4),
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/streets/straight_02", holder8), 4),
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/streets/straight_03", holder8), 7),
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/streets/straight_04", holder8), 7),
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/streets/straight_05", holder8), 3),
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/streets/straight_06", holder8), 4),
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/streets/crossroad_01", holder8), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/streets/crossroad_02", holder8), 1),
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/streets/crossroad_03", holder8), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/streets/crossroad_04", holder8), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/streets/crossroad_05", holder8), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/streets/crossroad_06", holder8), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/streets/turn_01", holder8), 3)
				),
				StructureTemplatePool.Projection.TERRAIN_MATCHING
			)
		);
		Pools.register(
			bootstrapContext,
			"village/plains/houses",
			new StructureTemplatePool(
				holder11,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_small_house_1", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_small_house_2", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_small_house_3", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_small_house_4", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_small_house_5", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_small_house_6", holder4), 1),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_small_house_7", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_small_house_8", holder4), 3),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_medium_house_1", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_medium_house_2", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_big_house_1", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_butcher_shop_1", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_butcher_shop_2", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_tool_smith_1", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_fletcher_house_1", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_shepherds_house_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_armorer_house_1", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_fisher_cottage_1", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_tannery_1", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_cartographer_1", holder4), 1),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_library_1", holder4), 5),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_library_2", holder4), 1),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_masons_house_1", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_weaponsmith_1", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_temple_3", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_temple_4", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_stable_1", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_stable_2"), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_large_farm_1", holder9), 4),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_small_farm_1", holder9), 4),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_animal_pen_1"), 1),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_animal_pen_2"), 1),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_animal_pen_3"), 5),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_accessory_1"), 1),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_meeting_point_4", holder6), 3),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_meeting_point_5"), 1),
					Pair.of(StructurePoolElement.empty(), 10)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"village/plains/zombie/houses",
			new StructureTemplatePool(
				holder11,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/houses/plains_small_house_1", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/houses/plains_small_house_2", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/houses/plains_small_house_3", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/houses/plains_small_house_4", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/houses/plains_small_house_5", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/houses/plains_small_house_6", holder7), 1),
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/houses/plains_small_house_7", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/houses/plains_small_house_8", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/houses/plains_medium_house_1", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/houses/plains_medium_house_2", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/houses/plains_big_house_1", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_butcher_shop_1", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/houses/plains_butcher_shop_2", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_tool_smith_1", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/houses/plains_fletcher_house_1", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/houses/plains_shepherds_house_1", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_armorer_house_1", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_fisher_cottage_1", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_tannery_1", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_cartographer_1", holder7), 1),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_library_1", holder7), 3),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_library_2", holder7), 1),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_masons_house_1", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_weaponsmith_1", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_temple_3", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_temple_4", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/houses/plains_stable_1", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_stable_2", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_large_farm_1", holder7), 4),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_small_farm_1", holder7), 4),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_animal_pen_1", holder7), 1),
					Pair.of(StructurePoolElement.legacy("village/plains/houses/plains_animal_pen_2", holder7), 1),
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/houses/plains_animal_pen_3", holder7), 5),
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/houses/plains_meeting_point_4", holder7), 3),
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/houses/plains_meeting_point_5", holder7), 1),
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
					Pair.of(StructurePoolElement.legacy("village/plains/terminators/terminator_01", holder8), 1),
					Pair.of(StructurePoolElement.legacy("village/plains/terminators/terminator_02", holder8), 1),
					Pair.of(StructurePoolElement.legacy("village/plains/terminators/terminator_03", holder8), 1),
					Pair.of(StructurePoolElement.legacy("village/plains/terminators/terminator_04", holder8), 1)
				),
				StructureTemplatePool.Projection.TERRAIN_MATCHING
			)
		);
		Pools.register(
			bootstrapContext,
			"village/plains/trees",
			new StructureTemplatePool(holder10, ImmutableList.of(Pair.of(StructurePoolElement.feature(holder), 1)), StructureTemplatePool.Projection.RIGID)
		);
		Pools.register(
			bootstrapContext,
			"village/plains/decor",
			new StructureTemplatePool(
				holder10,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/plains/plains_lamp_1"), 2),
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
			"village/plains/zombie/decor",
			new StructureTemplatePool(
				holder10,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/plains/plains_lamp_1", holder7), 1),
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
			"village/plains/villagers",
			new StructureTemplatePool(
				holder10,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/plains/villagers/nitwit"), 1),
					Pair.of(StructurePoolElement.legacy("village/plains/villagers/baby"), 1),
					Pair.of(StructurePoolElement.legacy("village/plains/villagers/unemployed"), 10)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"village/plains/zombie/villagers",
			new StructureTemplatePool(
				holder10,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/villagers/nitwit"), 1),
					Pair.of(StructurePoolElement.legacy("village/plains/zombie/villagers/unemployed"), 10)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"village/common/animals",
			new StructureTemplatePool(
				holder10,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/common/animals/cows_1"), 7),
					Pair.of(StructurePoolElement.legacy("village/common/animals/pigs_1"), 7),
					Pair.of(StructurePoolElement.legacy("village/common/animals/horses_1"), 1),
					Pair.of(StructurePoolElement.legacy("village/common/animals/horses_2"), 1),
					Pair.of(StructurePoolElement.legacy("village/common/animals/horses_3"), 1),
					Pair.of(StructurePoolElement.legacy("village/common/animals/horses_4"), 1),
					Pair.of(StructurePoolElement.legacy("village/common/animals/horses_5"), 1),
					Pair.of(StructurePoolElement.legacy("village/common/animals/sheep_1"), 1),
					Pair.of(StructurePoolElement.legacy("village/common/animals/sheep_2"), 1),
					Pair.of(StructurePoolElement.empty(), 5)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"village/common/sheep",
			new StructureTemplatePool(
				holder10,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/common/animals/sheep_1"), 1), Pair.of(StructurePoolElement.legacy("village/common/animals/sheep_2"), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"village/common/cats",
			new StructureTemplatePool(
				holder10,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/common/animals/cat_black"), 1),
					Pair.of(StructurePoolElement.legacy("village/common/animals/cat_british"), 1),
					Pair.of(StructurePoolElement.legacy("village/common/animals/cat_calico"), 1),
					Pair.of(StructurePoolElement.legacy("village/common/animals/cat_persian"), 1),
					Pair.of(StructurePoolElement.legacy("village/common/animals/cat_ragdoll"), 1),
					Pair.of(StructurePoolElement.legacy("village/common/animals/cat_red"), 1),
					Pair.of(StructurePoolElement.legacy("village/common/animals/cat_siamese"), 1),
					Pair.of(StructurePoolElement.legacy("village/common/animals/cat_tabby"), 1),
					Pair.of(StructurePoolElement.legacy("village/common/animals/cat_white"), 1),
					Pair.of(StructurePoolElement.legacy("village/common/animals/cat_jellie"), 1),
					Pair.of(StructurePoolElement.empty(), 3)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"village/common/butcher_animals",
			new StructureTemplatePool(
				holder10,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/common/animals/cows_1"), 3),
					Pair.of(StructurePoolElement.legacy("village/common/animals/pigs_1"), 3),
					Pair.of(StructurePoolElement.legacy("village/common/animals/sheep_1"), 1),
					Pair.of(StructurePoolElement.legacy("village/common/animals/sheep_2"), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"village/common/iron_golem",
			new StructureTemplatePool(
				holder10, ImmutableList.of(Pair.of(StructurePoolElement.legacy("village/common/iron_golem"), 1)), StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"village/common/well_bottoms",
			new StructureTemplatePool(
				holder10, ImmutableList.of(Pair.of(StructurePoolElement.legacy("village/common/well_bottom"), 1)), StructureTemplatePool.Projection.RIGID
			)
		);
	}
}
