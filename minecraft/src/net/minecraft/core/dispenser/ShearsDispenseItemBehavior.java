package net.minecraft.core.dispenser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;

public class ShearsDispenseItemBehavior extends OptionalDispenseItemBehavior {
	@Override
	protected ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
		Level level = blockSource.getLevel();
		if (!level.isClientSide()) {
			BlockPos blockPos = blockSource.getPos().relative(blockSource.getBlockState().getValue(DispenserBlock.FACING));
			this.setSuccess(tryShearBeehive((ServerLevel)level, blockPos) || tryShearLivingEntity((ServerLevel)level, blockPos));
			if (this.isSuccess() && itemStack.hurt(1, level.getRandom(), null)) {
				itemStack.setCount(0);
			}
		}

		return itemStack;
	}

	private static boolean tryShearBeehive(ServerLevel serverLevel, BlockPos blockPos) {
		BlockState blockState = serverLevel.getBlockState(blockPos);
		if (blockState.is(BlockTags.BEEHIVES)) {
			int i = (Integer)blockState.getValue(BeehiveBlock.HONEY_LEVEL);
			if (i >= 5) {
				serverLevel.playSound(null, blockPos, SoundEvents.BEEHIVE_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
				BeehiveBlock.dropHoneycomb(serverLevel, blockPos);
				((BeehiveBlock)blockState.getBlock())
					.releaseBeesAndResetHoneyLevel(serverLevel, blockState, blockPos, null, BeehiveBlockEntity.BeeReleaseStatus.BEE_RELEASED);
				serverLevel.gameEvent(null, GameEvent.SHEAR, blockPos);
				return true;
			}
		}

		return false;
	}

	private static boolean tryShearLivingEntity(ServerLevel serverLevel, BlockPos blockPos) {
		for (LivingEntity livingEntity : serverLevel.getEntitiesOfClass(LivingEntity.class, new AABB(blockPos), EntitySelector.NO_SPECTATORS)) {
			if (livingEntity instanceof Shearable shearable && shearable.readyForShearing()) {
				shearable.shear(SoundSource.BLOCKS);
				serverLevel.gameEvent(null, GameEvent.SHEAR, blockPos);
				return true;
			}
		}

		return false;
	}
}
