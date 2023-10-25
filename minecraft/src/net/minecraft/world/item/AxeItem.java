package net.minecraft.world.item;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class AxeItem extends DiggerItem {
	protected static final Map<Block, Block> STRIPPABLES = new Builder<Block, Block>()
		.put(Blocks.OAK_WOOD, Blocks.STRIPPED_OAK_WOOD)
		.put(Blocks.OAK_LOG, Blocks.STRIPPED_OAK_LOG)
		.put(Blocks.DARK_OAK_WOOD, Blocks.STRIPPED_DARK_OAK_WOOD)
		.put(Blocks.DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_LOG)
		.put(Blocks.ACACIA_WOOD, Blocks.STRIPPED_ACACIA_WOOD)
		.put(Blocks.ACACIA_LOG, Blocks.STRIPPED_ACACIA_LOG)
		.put(Blocks.CHERRY_WOOD, Blocks.STRIPPED_CHERRY_WOOD)
		.put(Blocks.CHERRY_LOG, Blocks.STRIPPED_CHERRY_LOG)
		.put(Blocks.BIRCH_WOOD, Blocks.STRIPPED_BIRCH_WOOD)
		.put(Blocks.BIRCH_LOG, Blocks.STRIPPED_BIRCH_LOG)
		.put(Blocks.JUNGLE_WOOD, Blocks.STRIPPED_JUNGLE_WOOD)
		.put(Blocks.JUNGLE_LOG, Blocks.STRIPPED_JUNGLE_LOG)
		.put(Blocks.SPRUCE_WOOD, Blocks.STRIPPED_SPRUCE_WOOD)
		.put(Blocks.SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_LOG)
		.put(Blocks.WARPED_STEM, Blocks.STRIPPED_WARPED_STEM)
		.put(Blocks.WARPED_HYPHAE, Blocks.STRIPPED_WARPED_HYPHAE)
		.put(Blocks.CRIMSON_STEM, Blocks.STRIPPED_CRIMSON_STEM)
		.put(Blocks.CRIMSON_HYPHAE, Blocks.STRIPPED_CRIMSON_HYPHAE)
		.put(Blocks.MANGROVE_WOOD, Blocks.STRIPPED_MANGROVE_WOOD)
		.put(Blocks.MANGROVE_LOG, Blocks.STRIPPED_MANGROVE_LOG)
		.put(Blocks.BAMBOO_BLOCK, Blocks.STRIPPED_BAMBOO_BLOCK)
		.build();

	protected AxeItem(Tier tier, float f, float g, Item.Properties properties) {
		super(f, g, tier, BlockTags.MINEABLE_WITH_AXE, properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		Level level = useOnContext.getLevel();
		BlockPos blockPos = useOnContext.getClickedPos();
		Player player = useOnContext.getPlayer();
		Optional<BlockState> optional = this.evaluateNewBlockState(level, blockPos, player, level.getBlockState(blockPos));
		if (optional.isEmpty()) {
			return InteractionResult.PASS;
		} else {
			ItemStack itemStack = useOnContext.getItemInHand();
			if (player instanceof ServerPlayer) {
				CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)player, blockPos, itemStack);
			}

			level.setBlock(blockPos, (BlockState)optional.get(), 11);
			level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(player, (BlockState)optional.get()));
			if (player != null) {
				itemStack.hurtAndBreak(1, player, playerx -> playerx.broadcastBreakEvent(useOnContext.getHand()));
			}

			return InteractionResult.sidedSuccess(level.isClientSide);
		}
	}

	private Optional<BlockState> evaluateNewBlockState(Level level, BlockPos blockPos, @Nullable Player player, BlockState blockState) {
		Optional<BlockState> optional = this.getStripped(blockState);
		if (optional.isPresent()) {
			level.playSound(player, blockPos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
			return optional;
		} else {
			Optional<BlockState> optional2 = WeatheringCopper.getPrevious(blockState);
			if (optional2.isPresent()) {
				level.playSound(player, blockPos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
				level.levelEvent(player, 3005, blockPos, 0);
				return optional2;
			} else {
				Optional<BlockState> optional3 = Optional.ofNullable((Block)((BiMap)HoneycombItem.WAX_OFF_BY_BLOCK.get()).get(blockState.getBlock()))
					.map(block -> block.withPropertiesOf(blockState));
				if (optional3.isPresent()) {
					level.playSound(player, blockPos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
					level.levelEvent(player, 3004, blockPos, 0);
					return optional3;
				} else {
					return Optional.empty();
				}
			}
		}
	}

	private Optional<BlockState> getStripped(BlockState blockState) {
		return Optional.ofNullable((Block)STRIPPABLES.get(blockState.getBlock()))
			.map(block -> block.defaultBlockState().setValue(RotatedPillarBlock.AXIS, (Direction.Axis)blockState.getValue(RotatedPillarBlock.AXIS)));
	}
}
