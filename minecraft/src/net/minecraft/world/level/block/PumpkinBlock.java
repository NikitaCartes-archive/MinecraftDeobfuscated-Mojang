package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class PumpkinBlock extends Block {
	public static final MapCodec<PumpkinBlock> CODEC = simpleCodec(PumpkinBlock::new);

	@Override
	public MapCodec<PumpkinBlock> codec() {
		return CODEC;
	}

	protected PumpkinBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected InteractionResult useItemOn(
		ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		if (!itemStack.is(Items.SHEARS)) {
			return super.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);
		} else if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		} else {
			Direction direction = blockHitResult.getDirection();
			Direction direction2 = direction.getAxis() == Direction.Axis.Y ? player.getDirection().getOpposite() : direction;
			level.playSound(null, blockPos, SoundEvents.PUMPKIN_CARVE, SoundSource.BLOCKS, 1.0F, 1.0F);
			level.setBlock(blockPos, Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, direction2), 11);
			ItemEntity itemEntity = new ItemEntity(
				level,
				(double)blockPos.getX() + 0.5 + (double)direction2.getStepX() * 0.65,
				(double)blockPos.getY() + 0.1,
				(double)blockPos.getZ() + 0.5 + (double)direction2.getStepZ() * 0.65,
				new ItemStack(Items.PUMPKIN_SEEDS, 4)
			);
			itemEntity.setDeltaMovement(
				0.05 * (double)direction2.getStepX() + level.random.nextDouble() * 0.02, 0.05, 0.05 * (double)direction2.getStepZ() + level.random.nextDouble() * 0.02
			);
			level.addFreshEntity(itemEntity);
			itemStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(interactionHand));
			level.gameEvent(player, GameEvent.SHEAR, blockPos);
			player.awardStat(Stats.ITEM_USED.get(Items.SHEARS));
			return InteractionResult.SUCCESS;
		}
	}
}
