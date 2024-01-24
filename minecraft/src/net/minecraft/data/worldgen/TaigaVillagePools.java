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

public class TaigaVillagePools {
	public static final ResourceKey<StructureTemplatePool> START = Pools.createKey("village/taiga/town_centers");
	private static final ResourceKey<StructureTemplatePool> TERMINATORS_KEY = Pools.createKey("village/taiga/terminators");

	public static void bootstrap(BootstrapContext<StructureTemplatePool> bootstrapContext) {
		HolderGetter<PlacedFeature> holderGetter = bootstrapContext.lookup(Registries.PLACED_FEATURE);
		Holder<PlacedFeature> holder = holderGetter.getOrThrow(VillagePlacements.SPRUCE_VILLAGE);
		Holder<PlacedFeature> holder2 = holderGetter.getOrThrow(VillagePlacements.PINE_VILLAGE);
		Holder<PlacedFeature> holder3 = holderGetter.getOrThrow(VillagePlacements.PILE_PUMPKIN_VILLAGE);
		Holder<PlacedFeature> holder4 = holderGetter.getOrThrow(VillagePlacements.PATCH_TAIGA_GRASS_VILLAGE);
		Holder<PlacedFeature> holder5 = holderGetter.getOrThrow(VillagePlacements.PATCH_BERRY_BUSH_VILLAGE);
		HolderGetter<StructureProcessorList> holderGetter2 = bootstrapContext.lookup(Registries.PROCESSOR_LIST);
		Holder<StructureProcessorList> holder6 = holderGetter2.getOrThrow(ProcessorLists.MOSSIFY_10_PERCENT);
		Holder<StructureProcessorList> holder7 = holderGetter2.getOrThrow(ProcessorLists.ZOMBIE_TAIGA);
		Holder<StructureProcessorList> holder8 = holderGetter2.getOrThrow(ProcessorLists.STREET_SNOWY_OR_TAIGA);
		Holder<StructureProcessorList> holder9 = holderGetter2.getOrThrow(ProcessorLists.FARM_TAIGA);
		HolderGetter<StructureTemplatePool> holderGetter3 = bootstrapContext.lookup(Registries.TEMPLATE_POOL);
		Holder<StructureTemplatePool> holder10 = holderGetter3.getOrThrow(Pools.EMPTY);
		Holder<StructureTemplatePool> holder11 = holderGetter3.getOrThrow(TERMINATORS_KEY);
		bootstrapContext.register(
			START,
			new StructureTemplatePool(
				holder10,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/taiga/town_centers/taiga_meeting_point_1", holder6), 49),
					Pair.of(StructurePoolElement.legacy("village/taiga/town_centers/taiga_meeting_point_2", holder6), 49),
					Pair.of(StructurePoolElement.legacy("village/taiga/zombie/town_centers/taiga_meeting_point_1", holder7), 1),
					Pair.of(StructurePoolElement.legacy("village/taiga/zombie/town_centers/taiga_meeting_point_2", holder7), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"village/taiga/streets",
			new StructureTemplatePool(
				holder11,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/taiga/streets/corner_01", holder8), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/streets/corner_02", holder8), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/streets/corner_03", holder8), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/streets/straight_01", holder8), 4),
					Pair.of(StructurePoolElement.legacy("village/taiga/streets/straight_02", holder8), 4),
					Pair.of(StructurePoolElement.legacy("village/taiga/streets/straight_03", holder8), 4),
					Pair.of(StructurePoolElement.legacy("village/taiga/streets/straight_04", holder8), 7),
					Pair.of(StructurePoolElement.legacy("village/taiga/streets/straight_05", holder8), 7),
					Pair.of(StructurePoolElement.legacy("village/taiga/streets/straight_06", holder8), 4),
					Pair.of(StructurePoolElement.legacy("village/taiga/streets/crossroad_01", holder8), 1),
					Pair.of(StructurePoolElement.legacy("village/taiga/streets/crossroad_02", holder8), 1),
					Pair.of(StructurePoolElement.legacy("village/taiga/streets/crossroad_03", holder8), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/streets/crossroad_04", holder8), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/streets/crossroad_05", holder8), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/streets/crossroad_06", holder8), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/streets/turn_01", holder8), 3)
				),
				StructureTemplatePool.Projection.TERRAIN_MATCHING
			)
		);
		Pools.register(
			bootstrapContext,
			"village/taiga/zombie/streets",
			new StructureTemplatePool(
				holder11,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/taiga/zombie/streets/corner_01", holder8), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/zombie/streets/corner_02", holder8), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/zombie/streets/corner_03", holder8), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/zombie/streets/straight_01", holder8), 4),
					Pair.of(StructurePoolElement.legacy("village/taiga/zombie/streets/straight_02", holder8), 4),
					Pair.of(StructurePoolElement.legacy("village/taiga/zombie/streets/straight_03", holder8), 4),
					Pair.of(StructurePoolElement.legacy("village/taiga/zombie/streets/straight_04", holder8), 7),
					Pair.of(StructurePoolElement.legacy("village/taiga/zombie/streets/straight_05", holder8), 7),
					Pair.of(StructurePoolElement.legacy("village/taiga/zombie/streets/straight_06", holder8), 4),
					Pair.of(StructurePoolElement.legacy("village/taiga/zombie/streets/crossroad_01", holder8), 1),
					Pair.of(StructurePoolElement.legacy("village/taiga/zombie/streets/crossroad_02", holder8), 1),
					Pair.of(StructurePoolElement.legacy("village/taiga/zombie/streets/crossroad_03", holder8), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/zombie/streets/crossroad_04", holder8), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/zombie/streets/crossroad_05", holder8), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/zombie/streets/crossroad_06", holder8), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/zombie/streets/turn_01", holder8), 3)
				),
				StructureTemplatePool.Projection.TERRAIN_MATCHING
			)
		);
		Pools.register(
			bootstrapContext,
			"village/taiga/houses",
			new StructureTemplatePool(
				holder11,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_small_house_1", holder6), 4),
					Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_small_house_2", holder6), 4),
					Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_small_house_3", holder6), 4),
					Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_small_house_4", holder6), 4),
					Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_small_house_5", holder6), 4),
					Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_medium_house_1", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_medium_house_2", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_medium_house_3", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_medium_house_4", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_butcher_shop_1", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_tool_smith_1", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_fletcher_house_1", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_shepherds_house_1", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_armorer_house_1", holder6), 1),
					Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_armorer_2", holder6), 1),
					Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_fisher_cottage_1", holder6), 3),
					Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_tannery_1", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_cartographer_house_1", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_library_1", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_masons_house_1", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_weaponsmith_1", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_weaponsmith_2", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_temple_1", holder6), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_large_farm_1", holder9), 6),
					Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_large_farm_2", holder9), 6),
					Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_small_farm_1", holder6), 1),
					Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_animal_pen_1", holder6), 2),
					Pair.of(StructurePoolElement.empty(), 6)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"village/taiga/zombie/houses",
			new StructureTemplatePool(
				holder11,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/taiga/zombie/houses/taiga_small_house_1", holder7), 4),
					Pair.of(StructurePoolElement.legacy("village/taiga/zombie/houses/taiga_small_house_2", holder7), 4),
					Pair.of(StructurePoolElement.legacy("village/taiga/zombie/houses/taiga_small_house_3", holder7), 4),
					Pair.of(StructurePoolElement.legacy("village/taiga/zombie/houses/taiga_small_house_4", holder7), 4),
					Pair.of(StructurePoolElement.legacy("village/taiga/zombie/houses/taiga_small_house_5", holder7), 4),
					Pair.of(StructurePoolElement.legacy("village/taiga/zombie/houses/taiga_medium_house_1", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/zombie/houses/taiga_medium_house_2", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/zombie/houses/taiga_medium_house_3", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/zombie/houses/taiga_medium_house_4", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_butcher_shop_1", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/zombie/houses/taiga_tool_smith_1", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_fletcher_house_1", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/zombie/houses/taiga_shepherds_house_1", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_armorer_house_1", holder7), 1),
					Pair.of(StructurePoolElement.legacy("village/taiga/zombie/houses/taiga_fisher_cottage_1", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_tannery_1", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/zombie/houses/taiga_cartographer_house_1", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/zombie/houses/taiga_library_1", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_masons_house_1", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_weaponsmith_1", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/zombie/houses/taiga_weaponsmith_2", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/zombie/houses/taiga_temple_1", holder7), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_large_farm_1", holder7), 6),
					Pair.of(StructurePoolElement.legacy("village/taiga/zombie/houses/taiga_large_farm_2", holder7), 6),
					Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_small_farm_1", holder7), 1),
					Pair.of(StructurePoolElement.legacy("village/taiga/houses/taiga_animal_pen_1", holder7), 2),
					Pair.of(StructurePoolElement.empty(), 6)
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
			"village/taiga/decor",
			new StructureTemplatePool(
				holder10,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/taiga/taiga_lamp_post_1"), 10),
					Pair.of(StructurePoolElement.legacy("village/taiga/taiga_decoration_1"), 4),
					Pair.of(StructurePoolElement.legacy("village/taiga/taiga_decoration_2"), 1),
					Pair.of(StructurePoolElement.legacy("village/taiga/taiga_decoration_3"), 1),
					Pair.of(StructurePoolElement.legacy("village/taiga/taiga_decoration_4"), 1),
					Pair.of(StructurePoolElement.legacy("village/taiga/taiga_decoration_5"), 2),
					Pair.of(StructurePoolElement.legacy("village/taiga/taiga_decoration_6"), 1),
					Pair.of(StructurePoolElement.feature(holder), 4),
					Pair.of(StructurePoolElement.feature(holder2), 4),
					Pair.of(StructurePoolElement.feature(holder3), 2),
					Pair.of(StructurePoolElement.feature(holder4), 4),
					Pair.of(StructurePoolElement.feature(holder5), 1),
					Pair.of(StructurePoolElement.empty(), 4)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"village/taiga/zombie/decor",
			new StructureTemplatePool(
				holder10,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/taiga/taiga_decoration_1"), 4),
					Pair.of(StructurePoolElement.legacy("village/taiga/taiga_decoration_2"), 1),
					Pair.of(StructurePoolElement.legacy("village/taiga/taiga_decoration_3"), 1),
					Pair.of(StructurePoolElement.legacy("village/taiga/taiga_decoration_4"), 1),
					Pair.of(StructurePoolElement.feature(holder), 4),
					Pair.of(StructurePoolElement.feature(holder2), 4),
					Pair.of(StructurePoolElement.feature(holder3), 2),
					Pair.of(StructurePoolElement.feature(holder4), 4),
					Pair.of(StructurePoolElement.feature(holder5), 1),
					Pair.of(StructurePoolElement.empty(), 4)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"village/taiga/villagers",
			new StructureTemplatePool(
				holder10,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/taiga/villagers/nitwit"), 1),
					Pair.of(StructurePoolElement.legacy("village/taiga/villagers/baby"), 1),
					Pair.of(StructurePoolElement.legacy("village/taiga/villagers/unemployed"), 10)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"village/taiga/zombie/villagers",
			new StructureTemplatePool(
				holder10,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/taiga/zombie/villagers/nitwit"), 1),
					Pair.of(StructurePoolElement.legacy("village/taiga/zombie/villagers/unemployed"), 10)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
	}
}
