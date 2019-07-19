package net.minecraft.world.item;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ShovelItem extends DiggerItem {
	private static final Set<Block> DIGGABLES = Sets.<Block>newHashSet(
		Blocks.CLAY,
		Blocks.DIRT,
		Blocks.COARSE_DIRT,
		Blocks.PODZOL,
		Blocks.FARMLAND,
		Blocks.GRASS_BLOCK,
		Blocks.GRAVEL,
		Blocks.MYCELIUM,
		Blocks.SAND,
		Blocks.RED_SAND,
		Blocks.SNOW_BLOCK,
		Blocks.SNOW,
		Blocks.SOUL_SAND,
		Blocks.GRASS_PATH,
		Blocks.WHITE_CONCRETE_POWDER,
		Blocks.ORANGE_CONCRETE_POWDER,
		Blocks.MAGENTA_CONCRETE_POWDER,
		Blocks.LIGHT_BLUE_CONCRETE_POWDER,
		Blocks.YELLOW_CONCRETE_POWDER,
		Blocks.LIME_CONCRETE_POWDER,
		Blocks.PINK_CONCRETE_POWDER,
		Blocks.GRAY_CONCRETE_POWDER,
		Blocks.LIGHT_GRAY_CONCRETE_POWDER,
		Blocks.CYAN_CONCRETE_POWDER,
		Blocks.PURPLE_CONCRETE_POWDER,
		Blocks.BLUE_CONCRETE_POWDER,
		Blocks.BROWN_CONCRETE_POWDER,
		Blocks.GREEN_CONCRETE_POWDER,
		Blocks.RED_CONCRETE_POWDER,
		Blocks.BLACK_CONCRETE_POWDER
	);
	protected static final Map<Block, BlockState> FLATTENABLES = Maps.<Block, BlockState>newHashMap(
		ImmutableMap.of(Blocks.GRASS_BLOCK, Blocks.GRASS_PATH.defaultBlockState())
	);

	public ShovelItem(Tier tier, float f, float g, Item.Properties properties) {
		super(f, g, tier, DIGGABLES, properties);
	}

	@Override
	public boolean canDestroySpecial(BlockState blockState) {
		Block block = blockState.getBlock();
		return block == Blocks.SNOW || block == Blocks.SNOW_BLOCK;
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		Level level = useOnContext.getLevel();
		BlockPos blockPos = useOnContext.getClickedPos();
		if (useOnContext.getClickedFace() != Direction.DOWN && level.getBlockState(blockPos.above()).isAir()) {
			BlockState blockState = (BlockState)FLATTENABLES.get(level.getBlockState(blockPos).getBlock());
			if (blockState != null) {
				Player player = useOnContext.getPlayer();
				level.playSound(player, blockPos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);
				if (!level.isClientSide) {
					level.setBlock(blockPos, blockState, 11);
					if (player != null) {
						useOnContext.getItemInHand().hurtAndBreak(1, player, playerx -> playerx.broadcastBreakEvent(useOnContext.getHand()));
					}
				}

				return InteractionResult.SUCCESS;
			}
		}

		return InteractionResult.PASS;
	}
}
