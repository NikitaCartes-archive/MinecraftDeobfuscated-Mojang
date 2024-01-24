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

public class SnowyVillagePools {
	public static final ResourceKey<StructureTemplatePool> START = Pools.createKey("village/snowy/town_centers");
	private static final ResourceKey<StructureTemplatePool> TERMINATORS_KEY = Pools.createKey("village/snowy/terminators");

	public static void bootstrap(BootstrapContext<StructureTemplatePool> bootstrapContext) {
		HolderGetter<PlacedFeature> holderGetter = bootstrapContext.lookup(Registries.PLACED_FEATURE);
		Holder<PlacedFeature> holder = holderGetter.getOrThrow(VillagePlacements.SPRUCE_VILLAGE);
		Holder<PlacedFeature> holder2 = holderGetter.getOrThrow(VillagePlacements.PILE_SNOW_VILLAGE);
		Holder<PlacedFeature> holder3 = holderGetter.getOrThrow(VillagePlacements.PILE_ICE_VILLAGE);
		HolderGetter<StructureProcessorList> holderGetter2 = bootstrapContext.lookup(Registries.PROCESSOR_LIST);
		Holder<StructureProcessorList> holder4 = holderGetter2.getOrThrow(ProcessorLists.STREET_SNOWY_OR_TAIGA);
		Holder<StructureProcessorList> holder5 = holderGetter2.getOrThrow(ProcessorLists.FARM_SNOWY);
		Holder<StructureProcessorList> holder6 = holderGetter2.getOrThrow(ProcessorLists.ZOMBIE_SNOWY);
		HolderGetter<StructureTemplatePool> holderGetter3 = bootstrapContext.lookup(Registries.TEMPLATE_POOL);
		Holder<StructureTemplatePool> holder7 = holderGetter3.getOrThrow(Pools.EMPTY);
		Holder<StructureTemplatePool> holder8 = holderGetter3.getOrThrow(TERMINATORS_KEY);
		bootstrapContext.register(
			START,
			new StructureTemplatePool(
				holder7,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/snowy/town_centers/snowy_meeting_point_1"), 100),
					Pair.of(StructurePoolElement.legacy("village/snowy/town_centers/snowy_meeting_point_2"), 50),
					Pair.of(StructurePoolElement.legacy("village/snowy/town_centers/snowy_meeting_point_3"), 150),
					Pair.of(StructurePoolElement.legacy("village/snowy/zombie/town_centers/snowy_meeting_point_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/zombie/town_centers/snowy_meeting_point_2"), 1),
					Pair.of(StructurePoolElement.legacy("village/snowy/zombie/town_centers/snowy_meeting_point_3"), 3)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"village/snowy/streets",
			new StructureTemplatePool(
				holder8,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/snowy/streets/corner_01", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/streets/corner_02", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/streets/corner_03", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/streets/square_01", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/streets/straight_01", holder4), 4),
					Pair.of(StructurePoolElement.legacy("village/snowy/streets/straight_02", holder4), 4),
					Pair.of(StructurePoolElement.legacy("village/snowy/streets/straight_03", holder4), 4),
					Pair.of(StructurePoolElement.legacy("village/snowy/streets/straight_04", holder4), 7),
					Pair.of(StructurePoolElement.legacy("village/snowy/streets/straight_06", holder4), 4),
					Pair.of(StructurePoolElement.legacy("village/snowy/streets/straight_08", holder4), 4),
					Pair.of(StructurePoolElement.legacy("village/snowy/streets/crossroad_02", holder4), 1),
					Pair.of(StructurePoolElement.legacy("village/snowy/streets/crossroad_03", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/streets/crossroad_04", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/streets/crossroad_05", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/streets/crossroad_06", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/streets/turn_01", holder4), 3)
				),
				StructureTemplatePool.Projection.TERRAIN_MATCHING
			)
		);
		Pools.register(
			bootstrapContext,
			"village/snowy/zombie/streets",
			new StructureTemplatePool(
				holder8,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/corner_01", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/corner_02", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/corner_03", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/square_01", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/straight_01", holder4), 4),
					Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/straight_02", holder4), 4),
					Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/straight_03", holder4), 4),
					Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/straight_04", holder4), 7),
					Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/straight_06", holder4), 4),
					Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/straight_08", holder4), 4),
					Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/crossroad_02", holder4), 1),
					Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/crossroad_03", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/crossroad_04", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/crossroad_05", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/crossroad_06", holder4), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/zombie/streets/turn_01", holder4), 3)
				),
				StructureTemplatePool.Projection.TERRAIN_MATCHING
			)
		);
		Pools.register(
			bootstrapContext,
			"village/snowy/houses",
			new StructureTemplatePool(
				holder8,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_small_house_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_small_house_2"), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_small_house_3"), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_small_house_4"), 3),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_small_house_5"), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_small_house_6"), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_small_house_7"), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_small_house_8"), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_medium_house_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_medium_house_2"), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_medium_house_3"), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_butchers_shop_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_butchers_shop_2"), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_tool_smith_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_fletcher_house_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_shepherds_house_1"), 3),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_armorer_house_1"), 1),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_armorer_house_2"), 1),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_fisher_cottage"), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_tannery_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_cartographer_house_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_library_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_masons_house_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_masons_house_2"), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_weapon_smith_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_temple_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_farm_1", holder5), 3),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_farm_2", holder5), 3),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_animal_pen_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_animal_pen_2"), 2),
					Pair.of(StructurePoolElement.empty(), 6)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"village/snowy/zombie/houses",
			new StructureTemplatePool(
				holder8,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/snowy/zombie/houses/snowy_small_house_1", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/zombie/houses/snowy_small_house_2", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/zombie/houses/snowy_small_house_3", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/zombie/houses/snowy_small_house_4", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/zombie/houses/snowy_small_house_5", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/zombie/houses/snowy_small_house_6", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/zombie/houses/snowy_small_house_7", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/zombie/houses/snowy_small_house_8", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/zombie/houses/snowy_medium_house_1", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/zombie/houses/snowy_medium_house_2", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/zombie/houses/snowy_medium_house_3", holder6), 1),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_butchers_shop_1", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_butchers_shop_2", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_tool_smith_1", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_fletcher_house_1", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_shepherds_house_1", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_armorer_house_1", holder6), 1),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_armorer_house_2", holder6), 1),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_fisher_cottage", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_tannery_1", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_cartographer_house_1", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_library_1", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_masons_house_1", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_masons_house_2", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_weapon_smith_1", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_temple_1", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_farm_1", holder6), 3),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_farm_2", holder6), 3),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_animal_pen_1", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/snowy/houses/snowy_animal_pen_2", holder6), 2),
					Pair.of(StructurePoolElement.empty(), 6)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		bootstrapContext.register(
			TERMINATORS_KEY,
			new StructureTemplatePool(
				holder7,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/plains/terminators/terminator_01", holder4), 1),
					Pair.of(StructurePoolElement.legacy("village/plains/terminators/terminator_02", holder4), 1),
					Pair.of(StructurePoolElement.legacy("village/plains/terminators/terminator_03", holder4), 1),
					Pair.of(StructurePoolElement.legacy("village/plains/terminators/terminator_04", holder4), 1)
				),
				StructureTemplatePool.Projection.TERRAIN_MATCHING
			)
		);
		Pools.register(
			bootstrapContext,
			"village/snowy/trees",
			new StructureTemplatePool(holder7, ImmutableList.of(Pair.of(StructurePoolElement.feature(holder), 1)), StructureTemplatePool.Projection.RIGID)
		);
		Pools.register(
			bootstrapContext,
			"village/snowy/decor",
			new StructureTemplatePool(
				holder7,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/snowy/snowy_lamp_post_01"), 4),
					Pair.of(StructurePoolElement.legacy("village/snowy/snowy_lamp_post_02"), 4),
					Pair.of(StructurePoolElement.legacy("village/snowy/snowy_lamp_post_03"), 1),
					Pair.of(StructurePoolElement.feature(holder), 4),
					Pair.of(StructurePoolElement.feature(holder2), 4),
					Pair.of(StructurePoolElement.feature(holder3), 1),
					Pair.of(StructurePoolElement.empty(), 9)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"village/snowy/zombie/decor",
			new StructureTemplatePool(
				holder7,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/snowy/snowy_lamp_post_01", holder6), 1),
					Pair.of(StructurePoolElement.legacy("village/snowy/snowy_lamp_post_02", holder6), 1),
					Pair.of(StructurePoolElement.legacy("village/snowy/snowy_lamp_post_03", holder6), 1),
					Pair.of(StructurePoolElement.feature(holder), 4),
					Pair.of(StructurePoolElement.feature(holder2), 4),
					Pair.of(StructurePoolElement.feature(holder3), 4),
					Pair.of(StructurePoolElement.empty(), 7)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"village/snowy/villagers",
			new StructureTemplatePool(
				holder7,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/snowy/villagers/nitwit"), 1),
					Pair.of(StructurePoolElement.legacy("village/snowy/villagers/baby"), 1),
					Pair.of(StructurePoolElement.legacy("village/snowy/villagers/unemployed"), 10)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"village/snowy/zombie/villagers",
			new StructureTemplatePool(
				holder7,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/snowy/zombie/villagers/nitwit"), 1),
					Pair.of(StructurePoolElement.legacy("village/snowy/zombie/villagers/unemployed"), 10)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
	}
}
