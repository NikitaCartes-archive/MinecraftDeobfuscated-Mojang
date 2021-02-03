package net.minecraft.world.item;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class HoeItem extends DiggerItem {
	private static final Set<Block> DIGGABLES = ImmutableSet.of(
		Blocks.NETHER_WART_BLOCK,
		Blocks.WARPED_WART_BLOCK,
		Blocks.HAY_BLOCK,
		Blocks.DRIED_KELP_BLOCK,
		Blocks.TARGET,
		Blocks.SHROOMLIGHT,
		Blocks.SPONGE,
		Blocks.WET_SPONGE,
		Blocks.JUNGLE_LEAVES,
		Blocks.OAK_LEAVES,
		Blocks.SPRUCE_LEAVES,
		Blocks.DARK_OAK_LEAVES,
		Blocks.ACACIA_LEAVES,
		Blocks.BIRCH_LEAVES,
		Blocks.AZALEA_LEAVES,
		Blocks.AZALEA_LEAVES_FLOWERS,
		Blocks.SCULK_SENSOR
	);
	protected static final Map<Block, BlockState> TILLABLES = Maps.<Block, BlockState>newHashMap(
		ImmutableMap.of(
			Blocks.GRASS_BLOCK,
			Blocks.FARMLAND.defaultBlockState(),
			Blocks.DIRT_PATH,
			Blocks.FARMLAND.defaultBlockState(),
			Blocks.DIRT,
			Blocks.FARMLAND.defaultBlockState(),
			Blocks.COARSE_DIRT,
			Blocks.DIRT.defaultBlockState()
		)
	);

	protected HoeItem(Tier tier, int i, float f, Item.Properties properties) {
		super((float)i, f, tier, DIGGABLES, properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		Level level = useOnContext.getLevel();
		BlockPos blockPos = useOnContext.getClickedPos();
		if (useOnContext.getClickedFace() != Direction.DOWN && level.getBlockState(blockPos.above()).isAir()) {
			BlockState blockState = (BlockState)TILLABLES.get(level.getBlockState(blockPos).getBlock());
			if (blockState != null) {
				Player player = useOnContext.getPlayer();
				level.playSound(player, blockPos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
				if (!level.isClientSide) {
					level.setBlock(blockPos, blockState, 11);
					if (player != null) {
						useOnContext.getItemInHand().hurtAndBreak(1, player, playerx -> playerx.broadcastBreakEvent(useOnContext.getHand()));
					}
				}

				return InteractionResult.sidedSuccess(level.isClientSide);
			}
		}

		return InteractionResult.PASS;
	}
}
