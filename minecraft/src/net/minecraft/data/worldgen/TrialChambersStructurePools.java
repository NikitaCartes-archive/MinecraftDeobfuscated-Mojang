package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBinding;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBindings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public class TrialChambersStructurePools {
	public static final ResourceKey<StructureTemplatePool> START = Pools.createKey("trial_chambers/chamber/end");
	public static final ResourceKey<StructureTemplatePool> HALLWAY_FALLBACK = Pools.createKey("trial_chambers/hallway/fallback");
	public static final ResourceKey<StructureTemplatePool> CHAMBER_CAP_FALLBACK = Pools.createKey("trial_chambers/chamber/entrance_cap");
	public static final List<PoolAliasBinding> ALIAS_BINDINGS = ImmutableList.<PoolAliasBinding>builder()
		.add(
			PoolAliasBinding.randomGroup(
				SimpleWeightedRandomList.<List<PoolAliasBinding>>builder()
					.add(
						List.of(
							PoolAliasBinding.direct(spawner("contents/ranged"), spawner("ranged/skeleton")),
							PoolAliasBinding.direct(spawner("contents/slow_ranged"), spawner("slow_ranged/skeleton"))
						)
					)
					.add(
						List.of(
							PoolAliasBinding.direct(spawner("contents/ranged"), spawner("ranged/stray")),
							PoolAliasBinding.direct(spawner("contents/slow_ranged"), spawner("slow_ranged/stray"))
						)
					)
					.add(
						List.of(
							PoolAliasBinding.direct(spawner("contents/ranged"), spawner("ranged/poison_skeleton")),
							PoolAliasBinding.direct(spawner("contents/slow_ranged"), spawner("slow_ranged/poison_skeleton"))
						)
					)
					.build()
			)
		)
		.add(
			PoolAliasBinding.random(
				spawner("contents/melee"),
				SimpleWeightedRandomList.<String>builder().add(spawner("melee/zombie")).add(spawner("melee/husk")).add(spawner("melee/slime")).build()
			)
		)
		.add(
			PoolAliasBinding.random(
				spawner("contents/small_melee"),
				SimpleWeightedRandomList.<String>builder()
					.add(spawner("small_melee/spider"))
					.add(spawner("small_melee/cave_spider"))
					.add(spawner("small_melee/silverfish"))
					.add(spawner("small_melee/baby_zombie"))
					.build()
			)
		)
		.build();

	public static String spawner(String string) {
		return "trial_chambers/spawner/" + string;
	}

	public static void bootstrap(BootstrapContext<StructureTemplatePool> bootstrapContext) {
		HolderGetter<StructureTemplatePool> holderGetter = bootstrapContext.lookup(Registries.TEMPLATE_POOL);
		Holder<StructureTemplatePool> holder = holderGetter.getOrThrow(Pools.EMPTY);
		Holder<StructureTemplatePool> holder2 = holderGetter.getOrThrow(HALLWAY_FALLBACK);
		Holder<StructureTemplatePool> holder3 = holderGetter.getOrThrow(CHAMBER_CAP_FALLBACK);
		HolderGetter<StructureProcessorList> holderGetter2 = bootstrapContext.lookup(Registries.PROCESSOR_LIST);
		Holder<StructureProcessorList> holder4 = holderGetter2.getOrThrow(UpdateOneTwentyOneProcessorLists.TRIAL_CHAMBERS_COPPER_BULB_DEGRADATION);
		bootstrapContext.register(
			START,
			new StructureTemplatePool(
				holder,
				List.of(
					Pair.of(StructurePoolElement.single("trial_chambers/corridor/end_1", holder4), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/corridor/end_2", holder4), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"trial_chambers/chamber/entrance_cap",
			new StructureTemplatePool(
				holder3, List.of(Pair.of(StructurePoolElement.single("trial_chambers/chamber/entrance_cap", holder4), 1)), StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"trial_chambers/chambers/end",
			new StructureTemplatePool(
				holder2,
				List.of(
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/chamber_1", holder4), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/assembly", holder4), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/eruption", holder4), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/slanted", holder4), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"trial_chambers/corridor",
			new StructureTemplatePool(
				holder,
				List.of(
					Pair.of(StructurePoolElement.single("trial_chambers/corridor/second_plate"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/intersection/intersection_1", holder4), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/intersection/intersection_2", holder4), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/intersection/intersection_3", holder4), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/corridor/first_plate"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/corridor/atrium_1", holder4), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/corridor/entrance_1", holder4), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"trial_chambers/chamber/addon",
			new StructureTemplatePool(
				holder,
				List.of(
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/full_stacked_walkway"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/full_stacked_walkway_2"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/full_corner_column"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/full_column_ranged_spawner", holder4), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/middle_column_ranged_spawner", holder4), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/grate_bridge"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/hanging_platform"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/short_grate_platform"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/short_platform"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/lower_staircase_down"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/lower_walkway_platform"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/walkway_extension"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/side_walkway"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/closed_side_walkway"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/middle_walkway"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/10x15_rise"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/10x15_stacked_pathway"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/10x15_pathway_3"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/platform_with_space"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/stairs_with_space"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/stairs_with_space_2"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/c3_side_walkway_1"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/c3_side_walkway_2"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/walkway_with_bridge_1"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/corner_room_1"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/addon/c1_breeze"), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"trial_chambers/chamber/assembly",
			new StructureTemplatePool(
				holder,
				List.of(
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/assembly/full_column"), 2),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/assembly/cover_1"), 2),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/assembly/cover_2"), 2),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/assembly/cover_3"), 2),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/assembly/cover_4"), 2),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/assembly/cover_5"), 2),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/assembly/cover_6"), 2),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/assembly/platform_1"), 2),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/assembly/spawner_1"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/assembly/spawner_trap_1"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/assembly/hanging_1"), 2),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/assembly/hanging_2"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/assembly/hanging_3"), 2),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/assembly/hanging_4"), 2),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/assembly/left_staircase_1"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/assembly/left_staircase_2"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/assembly/left_staircase_3"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/assembly/right_staircase_1"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/assembly/right_staircase_2"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/assembly/right_staircase_3"), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"trial_chambers/chamber/eruption",
			new StructureTemplatePool(
				holder,
				List.of(
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/eruption/center_1"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/eruption/breeze_slice_1"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/eruption/slice_1"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/eruption/slice_2"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/eruption/slice_3"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/eruption/quadrant_1"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/eruption/quadrant_2"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/eruption/quadrant_3"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/eruption/quadrant_4"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/eruption/quadrant_5"), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"trial_chambers/chamber/slanted",
			new StructureTemplatePool(
				holder,
				List.of(
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/slanted/center"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/slanted/hallway_1"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/slanted/hallway_2"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/slanted/hallway_3"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/slanted/hallway_4"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/slanted/hallway_5"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/slanted/quadrant_1"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/slanted/quadrant_2"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/slanted/quadrant_3"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/slanted/quadrant_4"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/slanted/ramp_1"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/slanted/ramp_2"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/slanted/ramp_3"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/slanted/ramp_4"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/slanted/ominous_upper_arm_1"), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"trial_chambers/chamber/pedestal",
			new StructureTemplatePool(
				holder,
				List.of(
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/pedestal/center_1"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/pedestal/slice_1"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/pedestal/slice_2"), 3),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/pedestal/slice_3"), 3),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/pedestal/slice_4"), 3),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/pedestal/slice_5"), 3),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/pedestal/ominous_slice_1"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/pedestal/quadrant_1"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/pedestal/quadrant_2"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/pedestal/quadrant_3"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/slanted/quadrant_1"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/slanted/quadrant_2"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/slanted/quadrant_3"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/slanted/quadrant_4"), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"trial_chambers/corridor/slices",
			new StructureTemplatePool(
				holder,
				List.of(
					Pair.of(StructurePoolElement.single("trial_chambers/corridor/straight_1", holder4), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/corridor/straight_2", holder4), 2),
					Pair.of(StructurePoolElement.single("trial_chambers/corridor/straight_3", holder4), 2),
					Pair.of(StructurePoolElement.single("trial_chambers/corridor/straight_4", holder4), 2),
					Pair.of(StructurePoolElement.single("trial_chambers/corridor/straight_5", holder4), 2),
					Pair.of(StructurePoolElement.single("trial_chambers/corridor/straight_6", holder4), 2),
					Pair.of(StructurePoolElement.single("trial_chambers/corridor/straight_7", holder4), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/corridor/straight_8", holder4), 2)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		bootstrapContext.register(
			HALLWAY_FALLBACK,
			new StructureTemplatePool(
				holder,
				List.of(
					Pair.of(StructurePoolElement.single("trial_chambers/hallway/rubble"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/hallway/rubble_chamber"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/hallway/rubble_thin"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/hallway/rubble_chamber_thin"), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"trial_chambers/hallway",
			new StructureTemplatePool(
				holder2,
				List.of(
					Pair.of(StructurePoolElement.single("trial_chambers/hallway/corridor_connector_1"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/hallway/upper_hallway_connector", holder4), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/hallway/lower_hallway_connector", holder4), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/hallway/rubble"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/chamber_1", holder4), 150),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/chamber_2", holder4), 150),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/chamber_4", holder4), 150),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/chamber_8", holder4), 150),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/assembly", holder4), 150),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/eruption", holder4), 150),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/slanted", holder4), 150),
					Pair.of(StructurePoolElement.single("trial_chambers/chamber/pedestal", holder4), 150),
					Pair.of(StructurePoolElement.single("trial_chambers/hallway/rubble_chamber", holder4), 10),
					Pair.of(StructurePoolElement.single("trial_chambers/hallway/rubble_chamber_thin", holder4), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/hallway/cache_1", holder4), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/hallway/left_corner", holder4), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/hallway/right_corner", holder4), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/hallway/corner_staircase", holder4), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/hallway/corner_staircase_down", holder4), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/hallway/long_straight_staircase", holder4), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/hallway/long_straight_staircase_down", holder4), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/hallway/straight", holder4), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/hallway/straight_staircase", holder4), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/hallway/straight_staircase_down", holder4), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"trial_chambers/corridors/addon/lower",
			new StructureTemplatePool(
				holder,
				List.of(
					Pair.of(StructurePoolElement.empty(), 8),
					Pair.of(StructurePoolElement.single("trial_chambers/corridor/addon/staircase"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/corridor/addon/wall"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/corridor/addon/ladder_to_middle"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/corridor/addon/arrow_dispenser"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/corridor/addon/bridge_lower"), 2)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"trial_chambers/corridors/addon/middle",
			new StructureTemplatePool(
				holder,
				List.of(
					Pair.of(StructurePoolElement.empty(), 8),
					Pair.of(StructurePoolElement.single("trial_chambers/corridor/addon/open_walkway"), 2),
					Pair.of(StructurePoolElement.single("trial_chambers/corridor/addon/walled_walkway"), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"trial_chambers/corridors/addon/middle_upper",
			new StructureTemplatePool(
				holder,
				List.of(
					Pair.of(StructurePoolElement.empty(), 6),
					Pair.of(StructurePoolElement.single("trial_chambers/corridor/addon/open_walkway_upper"), 2),
					Pair.of(StructurePoolElement.single("trial_chambers/corridor/addon/chandelier_upper"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/corridor/addon/decoration_upper"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/corridor/addon/head_upper"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/corridor/addon/reward_upper"), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"trial_chambers/atrium",
			new StructureTemplatePool(
				holder,
				List.of(
					Pair.of(StructurePoolElement.single("trial_chambers/corridor/atrium/bogged_relief"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/corridor/atrium/breeze_relief"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/corridor/atrium/spiral_relief"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/corridor/atrium/spider_relief"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/corridor/atrium/grand_staircase_1"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/corridor/atrium/grand_staircase_2"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/corridor/atrium/grand_staircase_3"), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"trial_chambers/decor",
			new StructureTemplatePool(
				holder,
				List.of(
					Pair.of(StructurePoolElement.empty(), 22),
					Pair.of(StructurePoolElement.single("trial_chambers/decor/empty_pot"), 2),
					Pair.of(StructurePoolElement.single("trial_chambers/decor/dead_bush_pot"), 2),
					Pair.of(StructurePoolElement.single("trial_chambers/decor/undecorated_pot"), 10),
					Pair.of(StructurePoolElement.single("trial_chambers/decor/flow_pot"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/decor/guster_pot"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/decor/scrape_pot"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/decor/candle_1"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/decor/candle_2"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/decor/candle_3"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/decor/candle_4"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/decor/barrel"), 2)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"trial_chambers/decor_chamber",
			new StructureTemplatePool(
				holder,
				List.of(Pair.of(StructurePoolElement.empty(), 4), Pair.of(StructurePoolElement.single("trial_chambers/decor/undecorated_pot"), 1)),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"trial_chambers/reward/all",
			new StructureTemplatePool(holder, List.of(Pair.of(StructurePoolElement.single("trial_chambers/reward/vault"), 1)), StructureTemplatePool.Projection.RIGID)
		);
		Pools.register(
			bootstrapContext,
			"trial_chambers/reward/ominous_vault",
			new StructureTemplatePool(
				holder, List.of(Pair.of(StructurePoolElement.single("trial_chambers/reward/ominous_vault"), 1)), StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"trial_chambers/reward/contents/default",
			new StructureTemplatePool(holder, List.of(Pair.of(StructurePoolElement.single("trial_chambers/reward/vault"), 1)), StructureTemplatePool.Projection.RIGID)
		);
		Pools.register(
			bootstrapContext,
			"trial_chambers/chests/supply",
			new StructureTemplatePool(
				holder, List.of(Pair.of(StructurePoolElement.single("trial_chambers/chests/connectors/supply"), 1)), StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"trial_chambers/chests/contents/supply",
			new StructureTemplatePool(holder, List.of(Pair.of(StructurePoolElement.single("trial_chambers/chests/supply"), 1)), StructureTemplatePool.Projection.RIGID)
		);
		Pools.register(
			bootstrapContext,
			"trial_chambers/spawner/ranged",
			new StructureTemplatePool(
				holder, List.of(Pair.of(StructurePoolElement.single("trial_chambers/spawner/connectors/ranged"), 1)), StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"trial_chambers/spawner/slow_ranged",
			new StructureTemplatePool(
				holder, List.of(Pair.of(StructurePoolElement.single("trial_chambers/spawner/connectors/slow_ranged"), 1)), StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"trial_chambers/spawner/melee",
			new StructureTemplatePool(
				holder, List.of(Pair.of(StructurePoolElement.single("trial_chambers/spawner/connectors/melee"), 1)), StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"trial_chambers/spawner/small_melee",
			new StructureTemplatePool(
				holder, List.of(Pair.of(StructurePoolElement.single("trial_chambers/spawner/connectors/small_melee"), 1)), StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"trial_chambers/spawner/breeze",
			new StructureTemplatePool(
				holder, List.of(Pair.of(StructurePoolElement.single("trial_chambers/spawner/connectors/breeze"), 1)), StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"trial_chambers/spawner/all",
			new StructureTemplatePool(
				holder,
				List.of(
					Pair.of(StructurePoolElement.single("trial_chambers/spawner/connectors/ranged"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/spawner/connectors/melee"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/spawner/connectors/small_melee"), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"trial_chambers/spawner/contents/breeze",
			new StructureTemplatePool(
				holder, List.of(Pair.of(StructurePoolElement.single("trial_chambers/spawner/breeze/breeze"), 1)), StructureTemplatePool.Projection.RIGID
			)
		);
		Pools.register(
			bootstrapContext,
			"trial_chambers/dispensers/chamber",
			new StructureTemplatePool(
				holder,
				List.of(
					Pair.of(StructurePoolElement.empty(), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/dispensers/chamber"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/dispensers/wall_dispenser"), 1),
					Pair.of(StructurePoolElement.single("trial_chambers/dispensers/floor_dispenser"), 1)
				),
				StructureTemplatePool.Projection.RIGID
			)
		);
		PoolAliasBindings.registerTargetsAsPools(bootstrapContext, holder, ALIAS_BINDINGS);
	}
}
