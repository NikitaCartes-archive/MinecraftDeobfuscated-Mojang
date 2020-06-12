package net.minecraft.world.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class FlintAndSteelItem extends Item {
	public FlintAndSteelItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		Player player = useOnContext.getPlayer();
		LevelAccessor levelAccessor = useOnContext.getLevel();
		BlockPos blockPos = useOnContext.getClickedPos();
		BlockState blockState = levelAccessor.getBlockState(blockPos);
		if (CampfireBlock.canLight(blockState)) {
			levelAccessor.playSound(player, blockPos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.4F + 0.8F);
			levelAccessor.setBlock(blockPos, blockState.setValue(BlockStateProperties.LIT, Boolean.valueOf(true)), 11);
			if (player != null) {
				useOnContext.getItemInHand().hurtAndBreak(1, player, playerx -> playerx.broadcastBreakEvent(useOnContext.getHand()));
			}

			return InteractionResult.sidedSuccess(levelAccessor.isClientSide());
		} else {
			BlockPos blockPos2 = blockPos.relative(useOnContext.getClickedFace());
			if (BaseFireBlock.canBePlacedAt(levelAccessor, blockPos2)) {
				levelAccessor.playSound(player, blockPos2, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.4F + 0.8F);
				BlockState blockState2 = BaseFireBlock.getState(levelAccessor, blockPos2);
				levelAccessor.setBlock(blockPos2, blockState2, 11);
				ItemStack itemStack = useOnContext.getItemInHand();
				if (player instanceof ServerPlayer) {
					CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)player, blockPos2, itemStack);
					itemStack.hurtAndBreak(1, player, playerx -> playerx.broadcastBreakEvent(useOnContext.getHand()));
				}

				return InteractionResult.sidedSuccess(levelAccessor.isClientSide());
			} else {
				return InteractionResult.FAIL;
			}
		}
	}
}
