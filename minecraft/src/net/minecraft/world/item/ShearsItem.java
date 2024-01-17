package net.minecraft.world.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class ShearsItem extends Item {
	public ShearsItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public boolean mineBlock(ItemStack itemStack, Level level, BlockState blockState, BlockPos blockPos, LivingEntity livingEntity) {
		if (!level.isClientSide && !blockState.is(BlockTags.FIRE)) {
			itemStack.hurtAndBreak(1, livingEntity, EquipmentSlot.MAINHAND);
		}

		return !blockState.is(BlockTags.LEAVES)
				&& !blockState.is(Blocks.COBWEB)
				&& !blockState.is(Blocks.SHORT_GRASS)
				&& !blockState.is(Blocks.FERN)
				&& !blockState.is(Blocks.DEAD_BUSH)
				&& !blockState.is(Blocks.HANGING_ROOTS)
				&& !blockState.is(Blocks.VINE)
				&& !blockState.is(Blocks.TRIPWIRE)
				&& !blockState.is(BlockTags.WOOL)
			? super.mineBlock(itemStack, level, blockState, blockPos, livingEntity)
			: true;
	}

	@Override
	public boolean isCorrectToolForDrops(BlockState blockState) {
		return blockState.is(Blocks.COBWEB) || blockState.is(Blocks.REDSTONE_WIRE) || blockState.is(Blocks.TRIPWIRE);
	}

	@Override
	public float getDestroySpeed(ItemStack itemStack, BlockState blockState) {
		if (blockState.is(Blocks.COBWEB) || blockState.is(BlockTags.LEAVES)) {
			return 15.0F;
		} else if (blockState.is(BlockTags.WOOL)) {
			return 5.0F;
		} else {
			return !blockState.is(Blocks.VINE) && !blockState.is(Blocks.GLOW_LICHEN) ? super.getDestroySpeed(itemStack, blockState) : 2.0F;
		}
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		Level level = useOnContext.getLevel();
		BlockPos blockPos = useOnContext.getClickedPos();
		BlockState blockState = level.getBlockState(blockPos);
		if (blockState.getBlock() instanceof GrowingPlantHeadBlock growingPlantHeadBlock && !growingPlantHeadBlock.isMaxAge(blockState)) {
			Player player = useOnContext.getPlayer();
			ItemStack itemStack = useOnContext.getItemInHand();
			if (player instanceof ServerPlayer) {
				CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)player, blockPos, itemStack);
			}

			level.playSound(player, blockPos, SoundEvents.GROWING_PLANT_CROP, SoundSource.BLOCKS, 1.0F, 1.0F);
			BlockState blockState2 = growingPlantHeadBlock.getMaxAgeState(blockState);
			level.setBlockAndUpdate(blockPos, blockState2);
			level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(useOnContext.getPlayer(), blockState2));
			if (player != null) {
				itemStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(useOnContext.getHand()));
			}

			return InteractionResult.sidedSuccess(level.isClientSide);
		}

		return super.useOn(useOnContext);
	}
}
