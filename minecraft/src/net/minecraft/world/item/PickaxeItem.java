package net.minecraft.world.item;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Set;
import net.minecraft.Util;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public class PickaxeItem extends DiggerItem {
	private static final Set<Block> DIGGABLES = ImmutableSet.of(
		Blocks.ACTIVATOR_RAIL,
		Blocks.COAL_ORE,
		Blocks.DEEPSLATE_COAL_ORE,
		Blocks.COBBLESTONE,
		Blocks.DETECTOR_RAIL,
		Blocks.DIAMOND_BLOCK,
		Blocks.DIAMOND_ORE,
		Blocks.DEEPSLATE_DIAMOND_ORE,
		Blocks.POWERED_RAIL,
		Blocks.GOLD_BLOCK,
		Blocks.GOLD_ORE,
		Blocks.NETHER_GOLD_ORE,
		Blocks.DEEPSLATE_GOLD_ORE,
		Blocks.ICE,
		Blocks.IRON_BLOCK,
		Blocks.IRON_ORE,
		Blocks.DEEPSLATE_IRON_ORE,
		Blocks.LAPIS_BLOCK,
		Blocks.LAPIS_ORE,
		Blocks.DEEPSLATE_LAPIS_ORE,
		Blocks.MOSSY_COBBLESTONE,
		Blocks.NETHERRACK,
		Blocks.PACKED_ICE,
		Blocks.BLUE_ICE,
		Blocks.RAIL,
		Blocks.REDSTONE_ORE,
		Blocks.DEEPSLATE_REDSTONE_ORE,
		Blocks.SANDSTONE,
		Blocks.CHISELED_SANDSTONE,
		Blocks.CUT_SANDSTONE,
		Blocks.CHISELED_RED_SANDSTONE,
		Blocks.CUT_RED_SANDSTONE,
		Blocks.RED_SANDSTONE,
		Blocks.STONE,
		Blocks.GRANITE,
		Blocks.POLISHED_GRANITE,
		Blocks.DIORITE,
		Blocks.POLISHED_DIORITE,
		Blocks.ANDESITE,
		Blocks.POLISHED_ANDESITE,
		Blocks.STONE_SLAB,
		Blocks.SMOOTH_STONE_SLAB,
		Blocks.SANDSTONE_SLAB,
		Blocks.PETRIFIED_OAK_SLAB,
		Blocks.COBBLESTONE_SLAB,
		Blocks.BRICK_SLAB,
		Blocks.STONE_BRICK_SLAB,
		Blocks.NETHER_BRICK_SLAB,
		Blocks.QUARTZ_SLAB,
		Blocks.RED_SANDSTONE_SLAB,
		Blocks.PURPUR_SLAB,
		Blocks.SMOOTH_QUARTZ,
		Blocks.SMOOTH_RED_SANDSTONE,
		Blocks.SMOOTH_SANDSTONE,
		Blocks.SMOOTH_STONE,
		Blocks.STONE_BUTTON,
		Blocks.STONE_PRESSURE_PLATE,
		Blocks.POLISHED_GRANITE_SLAB,
		Blocks.SMOOTH_RED_SANDSTONE_SLAB,
		Blocks.MOSSY_STONE_BRICK_SLAB,
		Blocks.POLISHED_DIORITE_SLAB,
		Blocks.MOSSY_COBBLESTONE_SLAB,
		Blocks.END_STONE_BRICK_SLAB,
		Blocks.SMOOTH_SANDSTONE_SLAB,
		Blocks.SMOOTH_QUARTZ_SLAB,
		Blocks.GRANITE_SLAB,
		Blocks.ANDESITE_SLAB,
		Blocks.RED_NETHER_BRICK_SLAB,
		Blocks.POLISHED_ANDESITE_SLAB,
		Blocks.DIORITE_SLAB,
		Blocks.SHULKER_BOX,
		Blocks.BLACK_SHULKER_BOX,
		Blocks.BLUE_SHULKER_BOX,
		Blocks.BROWN_SHULKER_BOX,
		Blocks.CYAN_SHULKER_BOX,
		Blocks.GRAY_SHULKER_BOX,
		Blocks.GREEN_SHULKER_BOX,
		Blocks.LIGHT_BLUE_SHULKER_BOX,
		Blocks.LIGHT_GRAY_SHULKER_BOX,
		Blocks.LIME_SHULKER_BOX,
		Blocks.MAGENTA_SHULKER_BOX,
		Blocks.ORANGE_SHULKER_BOX,
		Blocks.PINK_SHULKER_BOX,
		Blocks.PURPLE_SHULKER_BOX,
		Blocks.RED_SHULKER_BOX,
		Blocks.WHITE_SHULKER_BOX,
		Blocks.YELLOW_SHULKER_BOX,
		Blocks.PISTON,
		Blocks.STICKY_PISTON,
		Blocks.PISTON_HEAD,
		Blocks.AMETHYST_CLUSTER,
		Blocks.SMALL_AMETHYST_BUD,
		Blocks.MEDIUM_AMETHYST_BUD,
		Blocks.LARGE_AMETHYST_BUD,
		Blocks.AMETHYST_BLOCK,
		Blocks.BUDDING_AMETHYST,
		Blocks.COPPER_ORE,
		Blocks.DEEPSLATE_COPPER_ORE,
		Blocks.COPPER_BLOCK,
		Blocks.CUT_COPPER_SLAB,
		Blocks.CUT_COPPER_STAIRS,
		Blocks.CUT_COPPER,
		Blocks.OXIDIZED_COPPER,
		Blocks.OXIDIZED_CUT_COPPER,
		Blocks.OXIDIZED_CUT_COPPER_SLAB,
		Blocks.OXIDIZED_CUT_COPPER_STAIRS,
		Blocks.WEATHERED_COPPER,
		Blocks.WEATHERED_CUT_COPPER,
		Blocks.WEATHERED_CUT_COPPER_SLAB,
		Blocks.WEATHERED_CUT_COPPER_STAIRS,
		Blocks.EXPOSED_COPPER,
		Blocks.EXPOSED_CUT_COPPER_SLAB,
		Blocks.EXPOSED_CUT_COPPER_STAIRS,
		Blocks.EXPOSED_CUT_COPPER,
		Blocks.WAXED_COPPER_BLOCK,
		Blocks.WAXED_CUT_COPPER_SLAB,
		Blocks.WAXED_CUT_COPPER_STAIRS,
		Blocks.WAXED_CUT_COPPER,
		Blocks.WAXED_WEATHERED_COPPER,
		Blocks.WAXED_WEATHERED_CUT_COPPER,
		Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB,
		Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS,
		Blocks.WAXED_EXPOSED_COPPER,
		Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB,
		Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS,
		Blocks.WAXED_EXPOSED_CUT_COPPER,
		Blocks.LIGHTNING_ROD,
		Blocks.DEEPSLATE,
		Blocks.COBBLED_DEEPSLATE,
		Blocks.COBBLED_DEEPSLATE_SLAB,
		Blocks.COBBLED_DEEPSLATE_STAIRS,
		Blocks.COBBLED_DEEPSLATE_WALL,
		Blocks.POLISHED_DEEPSLATE,
		Blocks.POLISHED_DEEPSLATE_STAIRS,
		Blocks.POLISHED_DEEPSLATE_SLAB,
		Blocks.POLISHED_DEEPSLATE_WALL,
		Blocks.DEEPSLATE_TILES,
		Blocks.DEEPSLATE_TILE_STAIRS,
		Blocks.DEEPSLATE_TILE_SLAB,
		Blocks.DEEPSLATE_TILE_WALL,
		Blocks.DEEPSLATE_BRICKS,
		Blocks.DEEPSLATE_BRICK_STAIRS,
		Blocks.DEEPSLATE_BRICK_SLAB,
		Blocks.DEEPSLATE_BRICK_WALL,
		Blocks.CHISELED_DEEPSLATE
	);
	private static final Object2IntMap<Block> MIN_LEVEL_FOR_DROPS = Util.make(
		new Object2IntOpenHashMap<>(),
		object2IntOpenHashMap -> {
			object2IntOpenHashMap.defaultReturnValue(-1);

			for (Block block : ImmutableList.of(Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.NETHERITE_BLOCK, Blocks.RESPAWN_ANCHOR, Blocks.ANCIENT_DEBRIS)) {
				object2IntOpenHashMap.put(block, 3);
			}

			for (Block block : ImmutableList.of(
				Blocks.DIAMOND_BLOCK,
				Blocks.DIAMOND_ORE,
				Blocks.DEEPSLATE_DIAMOND_ORE,
				Blocks.EMERALD_ORE,
				Blocks.DEEPSLATE_EMERALD_ORE,
				Blocks.EMERALD_BLOCK,
				Blocks.GOLD_BLOCK,
				Blocks.GOLD_ORE,
				Blocks.DEEPSLATE_GOLD_ORE,
				Blocks.REDSTONE_ORE,
				Blocks.DEEPSLATE_REDSTONE_ORE,
				Blocks.AMETHYST_BLOCK,
				Blocks.AMETHYST_CLUSTER,
				Blocks.SMALL_AMETHYST_BUD,
				Blocks.MEDIUM_AMETHYST_BUD,
				Blocks.LARGE_AMETHYST_BUD
			)) {
				object2IntOpenHashMap.put(block, 2);
			}

			for (Block block : ImmutableList.of(
				Blocks.IRON_BLOCK,
				Blocks.IRON_ORE,
				Blocks.DEEPSLATE_IRON_ORE,
				Blocks.LAPIS_BLOCK,
				Blocks.LAPIS_ORE,
				Blocks.DEEPSLATE_LAPIS_ORE,
				Blocks.COPPER_BLOCK,
				Blocks.COPPER_ORE,
				Blocks.DEEPSLATE_COPPER_ORE,
				Blocks.CUT_COPPER_SLAB,
				Blocks.CUT_COPPER_STAIRS,
				Blocks.CUT_COPPER,
				Blocks.WEATHERED_COPPER,
				Blocks.WEATHERED_CUT_COPPER_SLAB,
				Blocks.WEATHERED_CUT_COPPER_STAIRS,
				Blocks.WEATHERED_CUT_COPPER,
				Blocks.WEATHERED_COPPER,
				Blocks.WEATHERED_CUT_COPPER_SLAB,
				Blocks.WEATHERED_CUT_COPPER_STAIRS,
				Blocks.WEATHERED_CUT_COPPER,
				Blocks.EXPOSED_COPPER,
				Blocks.EXPOSED_CUT_COPPER_SLAB,
				Blocks.EXPOSED_CUT_COPPER_STAIRS,
				Blocks.EXPOSED_CUT_COPPER,
				Blocks.WAXED_COPPER_BLOCK,
				Blocks.WAXED_CUT_COPPER_SLAB,
				Blocks.WAXED_CUT_COPPER_STAIRS,
				Blocks.WAXED_CUT_COPPER,
				Blocks.WAXED_WEATHERED_COPPER,
				Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB,
				Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS,
				Blocks.WAXED_WEATHERED_CUT_COPPER,
				Blocks.WAXED_EXPOSED_COPPER,
				Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB,
				Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS,
				Blocks.WAXED_EXPOSED_CUT_COPPER
			)) {
				object2IntOpenHashMap.put(block, 1);
			}

			for (Block block : ImmutableList.of(Blocks.NETHER_GOLD_ORE)) {
				object2IntOpenHashMap.put(block, 0);
			}
		}
	);

	protected PickaxeItem(Tier tier, int i, float f, Item.Properties properties) {
		super((float)i, f, tier, DIGGABLES, properties);
	}

	@Override
	public boolean isCorrectToolForDrops(BlockState blockState) {
		int i = this.getTier().getLevel();
		int j = MIN_LEVEL_FOR_DROPS.getInt(blockState.getBlock());
		if (j != -1) {
			return i >= j;
		} else {
			Material material = blockState.getMaterial();
			return material == Material.STONE || material == Material.METAL || material == Material.HEAVY_METAL;
		}
	}

	@Override
	public float getDestroySpeed(ItemStack itemStack, BlockState blockState) {
		Material material = blockState.getMaterial();
		return material != Material.METAL && material != Material.HEAVY_METAL && material != Material.STONE
			? super.getDestroySpeed(itemStack, blockState)
			: this.speed;
	}
}
