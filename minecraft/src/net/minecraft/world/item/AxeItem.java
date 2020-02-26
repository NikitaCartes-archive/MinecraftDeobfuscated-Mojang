package net.minecraft.world.item;

import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public class AxeItem extends DiggerItem {
	private static final Set<Block> DIGGABLES = Sets.<Block>newHashSet(
		Blocks.OAK_PLANKS,
		Blocks.SPRUCE_PLANKS,
		Blocks.BIRCH_PLANKS,
		Blocks.JUNGLE_PLANKS,
		Blocks.ACACIA_PLANKS,
		Blocks.DARK_OAK_PLANKS,
		Blocks.BOOKSHELF,
		Blocks.OAK_WOOD,
		Blocks.SPRUCE_WOOD,
		Blocks.BIRCH_WOOD,
		Blocks.JUNGLE_WOOD,
		Blocks.ACACIA_WOOD,
		Blocks.DARK_OAK_WOOD,
		Blocks.OAK_LOG,
		Blocks.SPRUCE_LOG,
		Blocks.BIRCH_LOG,
		Blocks.JUNGLE_LOG,
		Blocks.ACACIA_LOG,
		Blocks.DARK_OAK_LOG,
		Blocks.CHEST,
		Blocks.PUMPKIN,
		Blocks.CARVED_PUMPKIN,
		Blocks.JACK_O_LANTERN,
		Blocks.MELON,
		Blocks.LADDER,
		Blocks.SCAFFOLDING,
		Blocks.OAK_BUTTON,
		Blocks.SPRUCE_BUTTON,
		Blocks.BIRCH_BUTTON,
		Blocks.JUNGLE_BUTTON,
		Blocks.DARK_OAK_BUTTON,
		Blocks.ACACIA_BUTTON,
		Blocks.OAK_PRESSURE_PLATE,
		Blocks.SPRUCE_PRESSURE_PLATE,
		Blocks.BIRCH_PRESSURE_PLATE,
		Blocks.JUNGLE_PRESSURE_PLATE,
		Blocks.DARK_OAK_PRESSURE_PLATE,
		Blocks.ACACIA_PRESSURE_PLATE,
		Blocks.CRIMSON_PLANKS,
		Blocks.CRIMSON_STEM,
		Blocks.CRIMSON_BUTTON,
		Blocks.CRIMSON_PRESSURE_PLATE,
		Blocks.CRIMSON_FENCE,
		Blocks.CRIMSON_FENCE_GATE,
		Blocks.CRIMSON_STAIRS,
		Blocks.CRIMSON_DOOR,
		Blocks.CRIMSON_TRAPDOOR,
		Blocks.CRIMSON_SIGN,
		Blocks.CRIMSON_SLAB,
		Blocks.WARPED_PLANKS,
		Blocks.WARPED_STEM,
		Blocks.WARPED_BUTTON,
		Blocks.WARPED_PRESSURE_PLATE,
		Blocks.WARPED_FENCE,
		Blocks.WARPED_FENCE_GATE,
		Blocks.WARPED_STAIRS,
		Blocks.WARPED_DOOR,
		Blocks.WARPED_TRAPDOOR,
		Blocks.WARPED_SIGN,
		Blocks.WARPED_SLAB
	);
	protected static final Map<Block, Block> STRIPABLES = new Builder<Block, Block>()
		.put(Blocks.OAK_WOOD, Blocks.STRIPPED_OAK_WOOD)
		.put(Blocks.OAK_LOG, Blocks.STRIPPED_OAK_LOG)
		.put(Blocks.DARK_OAK_WOOD, Blocks.STRIPPED_DARK_OAK_WOOD)
		.put(Blocks.DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_LOG)
		.put(Blocks.ACACIA_WOOD, Blocks.STRIPPED_ACACIA_WOOD)
		.put(Blocks.ACACIA_LOG, Blocks.STRIPPED_ACACIA_LOG)
		.put(Blocks.BIRCH_WOOD, Blocks.STRIPPED_BIRCH_WOOD)
		.put(Blocks.BIRCH_LOG, Blocks.STRIPPED_BIRCH_LOG)
		.put(Blocks.JUNGLE_WOOD, Blocks.STRIPPED_JUNGLE_WOOD)
		.put(Blocks.JUNGLE_LOG, Blocks.STRIPPED_JUNGLE_LOG)
		.put(Blocks.SPRUCE_WOOD, Blocks.STRIPPED_SPRUCE_WOOD)
		.put(Blocks.SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_LOG)
		.put(Blocks.WARPED_STEM, Blocks.STRIPPED_WARPED_STEM)
		.put(Blocks.CRIMSON_STEM, Blocks.STRIPPED_CRIMSON_STEM)
		.build();

	protected AxeItem(Tier tier, float f, float g, Item.Properties properties) {
		super(f, g, tier, DIGGABLES, properties);
	}

	@Override
	public float getDestroySpeed(ItemStack itemStack, BlockState blockState) {
		Material material = blockState.getMaterial();
		return material != Material.WOOD && material != Material.PLANT && material != Material.REPLACEABLE_PLANT && material != Material.BAMBOO
			? super.getDestroySpeed(itemStack, blockState)
			: this.speed;
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		Level level = useOnContext.getLevel();
		BlockPos blockPos = useOnContext.getClickedPos();
		BlockState blockState = level.getBlockState(blockPos);
		Block block = (Block)STRIPABLES.get(blockState.getBlock());
		if (block != null) {
			Player player = useOnContext.getPlayer();
			level.playSound(player, blockPos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
			if (!level.isClientSide) {
				level.setBlock(blockPos, block.defaultBlockState().setValue(RotatedPillarBlock.AXIS, blockState.getValue(RotatedPillarBlock.AXIS)), 11);
				if (player != null) {
					useOnContext.getItemInHand().hurtAndBreak(1, player, playerx -> playerx.broadcastBreakEvent(useOnContext.getHand()));
				}
			}

			return InteractionResult.SUCCESS;
		} else {
			return InteractionResult.PASS;
		}
	}
}
