package net.minecraft.world.item;

import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ShovelItem extends DiggerItem {
	protected static final Map<Block, BlockState> FLATTENABLES = Maps.<Block, BlockState>newHashMap(
		new Builder()
			.put(Blocks.GRASS_BLOCK, Blocks.DIRT_PATH.defaultBlockState())
			.put(Blocks.DIRT, Blocks.DIRT_PATH.defaultBlockState())
			.put(Blocks.PODZOL, Blocks.DIRT_PATH.defaultBlockState())
			.put(Blocks.COARSE_DIRT, Blocks.DIRT_PATH.defaultBlockState())
			.put(Blocks.MYCELIUM, Blocks.DIRT_PATH.defaultBlockState())
			.put(Blocks.ROOTED_DIRT, Blocks.DIRT_PATH.defaultBlockState())
			.build()
	);

	public ShovelItem(Tier tier, float f, float g, Item.Properties properties) {
		super(f, g, tier, BlockTags.MINEABLE_WITH_SHOVEL, properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		Level level = useOnContext.getLevel();
		BlockPos blockPos = useOnContext.getClickedPos();
		BlockState blockState = level.getBlockState(blockPos);
		if (useOnContext.getClickedFace() == Direction.DOWN) {
			return InteractionResult.PASS;
		} else {
			Player player = useOnContext.getPlayer();
			BlockState blockState2 = (BlockState)FLATTENABLES.get(blockState.getBlock());
			BlockState blockState3 = null;
			if (blockState2 != null && level.getBlockState(blockPos.above()).isAir()) {
				level.playSound(player, blockPos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);
				blockState3 = blockState2;
			} else if (blockState.getBlock() instanceof CampfireBlock && (Boolean)blockState.getValue(CampfireBlock.LIT)) {
				if (!level.isClientSide()) {
					level.levelEvent(null, 1009, blockPos, 0);
				}

				CampfireBlock.dowse(useOnContext.getPlayer(), level, blockPos, blockState);
				blockState3 = blockState.setValue(CampfireBlock.LIT, Boolean.valueOf(false));
			}

			if (blockState3 != null) {
				if (!level.isClientSide) {
					level.setBlock(blockPos, blockState3, 11);
					if (player != null) {
						useOnContext.getItemInHand().hurtAndBreak(1, player, playerx -> playerx.broadcastBreakEvent(useOnContext.getHand()));
					}
				}

				return InteractionResult.sidedSuccess(level.isClientSide);
			} else {
				return InteractionResult.PASS;
			}
		}
	}
}
