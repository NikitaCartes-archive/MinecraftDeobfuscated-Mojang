package net.minecraft.world.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;

public class FlintAndSteelItem extends Item {
	public FlintAndSteelItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		Player player = useOnContext.getPlayer();
		Level level = useOnContext.getLevel();
		BlockPos blockPos = useOnContext.getClickedPos();
		BlockState blockState = level.getBlockState(blockPos);
		if (!CampfireBlock.canLight(blockState) && !CandleBlock.canLight(blockState) && !CandleCakeBlock.canLight(blockState)) {
			BlockPos blockPos2 = blockPos.relative(useOnContext.getClickedFace());
			if (BaseFireBlock.canBePlacedAt(level, blockPos2, useOnContext.getHorizontalDirection())) {
				level.playSound(player, blockPos2, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
				BlockState blockState2 = BaseFireBlock.getState(level, blockPos2);
				level.setBlock(blockPos2, blockState2, 11);
				level.gameEvent(player, GameEvent.BLOCK_PLACE, blockPos);
				ItemStack itemStack = useOnContext.getItemInHand();
				if (player instanceof ServerPlayer) {
					CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)player, blockPos2, itemStack);
					itemStack.hurtAndBreak(1, player, playerx -> playerx.broadcastBreakEvent(useOnContext.getHand()));
				}

				return InteractionResult.sidedSuccess(level.isClientSide());
			} else {
				return InteractionResult.FAIL;
			}
		} else {
			level.playSound(player, blockPos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
			level.setBlock(blockPos, blockState.setValue(BlockStateProperties.LIT, Boolean.valueOf(true)), 11);
			level.gameEvent(player, GameEvent.BLOCK_PLACE, blockPos);
			if (player != null) {
				useOnContext.getItemInHand().hurtAndBreak(1, player, playerx -> playerx.broadcastBreakEvent(useOnContext.getHand()));
			}

			return InteractionResult.sidedSuccess(level.isClientSide());
		}
	}
}
