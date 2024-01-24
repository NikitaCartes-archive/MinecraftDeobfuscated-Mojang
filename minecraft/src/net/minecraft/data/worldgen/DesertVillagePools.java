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

public class DesertVillagePools {
	public static final ResourceKey<StructureTemplatePool> START = Pools.createKey("village/desert/town_centers");
	private static final ResourceKey<StructureTemplatePool> TERMINATORS_KEY = Pools.createKey("village/desert/terminators");
	private static final ResourceKey<StructureTemplatePool> ZOMBIE_TERMINATORS_KEY = Pools.createKey("village/desert/zombie/terminators");

	public static void bootstrap(BootstrapContext<StructureTemplatePool> bootstrapContext) {
		HolderGetter<PlacedFeature> holderGetter = bootstrapContext.lookup(Registries.PLACED_FEATURE);
		Holder<PlacedFeature> holder = holderGetter.getOrThrow(VillagePlacements.PATCH_CACTUS_VILLAGE);
		Holder<PlacedFeature> holder2 = holderGetter.getOrThrow(VillagePlacements.PILE_HAY_VILLAGE);
		HolderGetter<StructureProcessorList> holderGetter2 = bootstrapContext.lookup(Registries.PROCESSOR_LIST);
		Holder<StructureProcessorList> holder3 = holderGetter2.getOrThrow(ProcessorLists.ZOMBIE_DESERT);
		Holder<StructureProcessorList> holder4 = holderGetter2.getOrThrow(ProcessorLists.FARM_DESERT);
		HolderGetter<StructureTemplatePool> holderGetter3 = bootstrapContext.lookup(Registries.TEMPLATE_POOL);
		Holder<StructureTemplatePool> holder5 = holderGetter3.getOrThrow(Pools.EMPTY);
		Holder<StructureTemplatePool> holder6 = holderGetter3.getOrThrow(TERMINATORS_KEY);
		Holder<StructureTemplatePool> holder7 = holderGetter3.getOrThrow(ZOMBIE_TERMINATORS_KEY);
		bootstrapContext.register(
			START,
			new StructureTemplatePool(
				holder5,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/desert/town_centers/desert_meeting_point_1"), 98),
					Pair.of(StructurePoolElement.legacy("village/desert/town_centers/desert_meeting_point_2"), 98),
					Pair.of(StructurePoolElement.legacy("village/desert/town_centers/desert_meeting_point_3"), 49),
					Pair.of(StructurePoolElement.legacy("village/desert/zombie/town_centers/desert_meeting_point_1", holder3), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/zombie/town_centers/desert_meeting_point_2", holder3), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/zombie/town_centers/desert_meeting_point_3", holder3), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"village/desert/streets",
			new StructureTemplatePool(
				holder6,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/desert/streets/corner_01"), 3),
					Pair.of(StructurePoolElement.legacy("village/desert/streets/corner_02"), 3),
					Pair.of(StructurePoolElement.legacy("village/desert/streets/straight_01"), 4),
					Pair.of(StructurePoolElement.legacy("village/desert/streets/straight_02"), 4),
					Pair.of(StructurePoolElement.legacy("village/desert/streets/straight_03"), 3),
					Pair.of(StructurePoolElement.legacy("village/desert/streets/crossroad_01"), 3),
					Pair.of(StructurePoolElement.legacy("village/desert/streets/crossroad_02"), 3),
					Pair.of(StructurePoolElement.legacy("village/desert/streets/crossroad_03"), 3),
					Pair.of(StructurePoolElement.legacy("village/desert/streets/square_01"), 3),
					Pair.of(StructurePoolElement.legacy("village/desert/streets/square_02"), 3),
					Pair.of(StructurePoolElement.legacy("village/desert/streets/turn_01"), 3)
				),
				StructureTemplatePool.Projection.TERRAIN_MATCHING
			)
		);
		Pools.register(
			bootstrapContext,
			"village/desert/zombie/streets",
			new StructureTemplatePool(
				holder7,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/desert/zombie/streets/corner_01"), 3),
					Pair.of(StructurePoolElement.legacy("village/desert/zombie/streets/corner_02"), 3),
					Pair.of(StructurePoolElement.legacy("village/desert/zombie/streets/straight_01"), 4),
					Pair.of(StructurePoolElement.legacy("village/desert/zombie/streets/straight_02"), 4),
					Pair.of(StructurePoolElement.legacy("village/desert/zombie/streets/straight_03"), 3),
					Pair.of(StructurePoolElement.legacy("village/desert/zombie/streets/crossroad_01"), 3),
					Pair.of(StructurePoolElement.legacy("village/desert/zombie/streets/crossroad_02"), 3),
					Pair.of(StructurePoolElement.legacy("village/desert/zombie/streets/crossroad_03"), 3),
					Pair.of(StructurePoolElement.legacy("village/desert/zombie/streets/square_01"), 3),
					Pair.of(StructurePoolElement.legacy("village/desert/zombie/streets/square_02"), 3),
					Pair.of(StructurePoolElement.legacy("village/desert/zombie/streets/turn_01"), 3)
				),
				StructureTemplatePool.Projection.TERRAIN_MATCHING
			)
		);
		Pools.register(
			bootstrapContext,
			"village/desert/houses",
			new StructureTemplatePool(
				holder6,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_small_house_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_small_house_2"), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_small_house_3"), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_small_house_4"), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_small_house_5"), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_small_house_6"), 1),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_small_house_7"), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_small_house_8"), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_medium_house_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_medium_house_2"), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_butcher_shop_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_tool_smith_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_fletcher_house_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_shepherd_house_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_armorer_1"), 1),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_fisher_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_tannery_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_cartographer_house_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_library_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_mason_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_weaponsmith_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_temple_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_temple_2"), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_large_farm_1", holder4), 11),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_farm_1", holder4), 4),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_farm_2", holder4), 4),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_animal_pen_1"), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_animal_pen_2"), 2),
					Pair.of(StructurePoolElement.empty(), 5)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"village/desert/zombie/houses",
			new StructureTemplatePool(
				holder7,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/desert/zombie/houses/desert_small_house_1", holder3), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/zombie/houses/desert_small_house_2", holder3), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/zombie/houses/desert_small_house_3", holder3), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/zombie/houses/desert_small_house_4", holder3), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/zombie/houses/desert_small_house_5", holder3), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/zombie/houses/desert_small_house_6", holder3), 1),
					Pair.of(StructurePoolElement.legacy("village/desert/zombie/houses/desert_small_house_7", holder3), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/zombie/houses/desert_small_house_8", holder3), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/zombie/houses/desert_medium_house_1", holder3), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/zombie/houses/desert_medium_house_2", holder3), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_butcher_shop_1", holder3), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_tool_smith_1", holder3), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_fletcher_house_1", holder3), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_shepherd_house_1", holder3), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_armorer_1", holder3), 1),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_fisher_1", holder3), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_tannery_1", holder3), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_cartographer_house_1", holder3), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_library_1", holder3), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_mason_1", holder3), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_weaponsmith_1", holder3), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_temple_1", holder3), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_temple_2", holder3), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_large_farm_1", holder3), 7),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_farm_1", holder3), 4),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_farm_2", holder3), 4),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_animal_pen_1", holder3), 2),
					Pair.of(StructurePoolElement.legacy("village/desert/houses/desert_animal_pen_2", holder3), 2),
					Pair.of(StructurePoolElement.empty(), 5)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		bootstrapContext.register(
			TERMINATORS_KEY,
			new StructureTemplatePool(
				holder5,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/desert/terminators/terminator_01"), 1),
					Pair.of(StructurePoolElement.legacy("village/desert/terminators/terminator_02"), 1)
				),
				StructureTemplatePool.Projection.TERRAIN_MATCHING
			)
		);
		bootstrapContext.register(
			ZOMBIE_TERMINATORS_KEY,
			new StructureTemplatePool(
				holder5,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/desert/terminators/terminator_01"), 1),
					Pair.of(StructurePoolElement.legacy("village/desert/zombie/terminators/terminator_02"), 1)
				),
				StructureTemplatePool.Projection.TERRAIN_MATCHING
			)
		);
		Pools.register(
			bootstrapContext,
			"village/desert/decor",
			new StructureTemplatePool(
				holder5,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/desert/desert_lamp_1"), 10),
					Pair.of(StructurePoolElement.feature(holder), 4),
					Pair.of(StructurePoolElement.feature(holder2), 4),
					Pair.of(StructurePoolElement.empty(), 10)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"village/desert/zombie/decor",
			new StructureTemplatePool(
				holder5,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/desert/desert_lamp_1", holder3), 10),
					Pair.of(StructurePoolElement.feature(holder), 4),
					Pair.of(StructurePoolElement.feature(holder2), 4),
					Pair.of(StructurePoolElement.empty(), 10)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"village/desert/villagers",
			new StructureTemplatePool(
				holder5,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/desert/villagers/nitwit"), 1),
					Pair.of(StructurePoolElement.legacy("village/desert/villagers/baby"), 1),
					Pair.of(StructurePoolElement.legacy("village/desert/villagers/unemployed"), 10)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"village/desert/camel",
			new StructureTemplatePool(
				holder5, ImmutableList.of(Pair.of(StructurePoolElement.legacy("village/desert/camel_spawn"), 1)), StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"village/desert/zombie/villagers",
			new StructureTemplatePool(
				holder5,
				ImmutableList.of(
					Pair.of(StructurePoolElement.legacy("village/desert/zombie/villagers/nitwit"), 1),
					Pair.of(StructurePoolElement.legacy("village/desert/zombie/villagers/unemployed"), 10)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
	}
}
