package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class FireChargeItem extends Item implements ProjectileItem {
	public FireChargeItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		Level level = useOnContext.getLevel();
		BlockPos blockPos = useOnContext.getClickedPos();
		BlockState blockState = level.getBlockState(blockPos);
		boolean bl = false;
		if (!CampfireBlock.canLight(blockState) && !CandleBlock.canLight(blockState) && !CandleCakeBlock.canLight(blockState)) {
			blockPos = blockPos.relative(useOnContext.getClickedFace());
			if (BaseFireBlock.canBePlacedAt(level, blockPos, useOnContext.getHorizontalDirection())) {
				this.playSound(level, blockPos);
				level.setBlockAndUpdate(blockPos, BaseFireBlock.getState(level, blockPos));
				level.gameEvent(useOnContext.getPlayer(), GameEvent.BLOCK_PLACE, blockPos);
				bl = true;
			}
		} else {
			this.playSound(level, blockPos);
			level.setBlockAndUpdate(blockPos, blockState.setValue(BlockStateProperties.LIT, Boolean.valueOf(true)));
			level.gameEvent(useOnContext.getPlayer(), GameEvent.BLOCK_CHANGE, blockPos);
			bl = true;
		}

		if (bl) {
			useOnContext.getItemInHand().shrink(1);
			return InteractionResult.sidedSuccess(level.isClientSide);
		} else {
			return InteractionResult.FAIL;
		}
	}

	private void playSound(Level level, BlockPos blockPos) {
		RandomSource randomSource = level.getRandom();
		level.playSound(null, blockPos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F);
	}

	@Override
	public Projectile asProjectile(Level level, Position position, ItemStack itemStack, Direction direction) {
		RandomSource randomSource = level.getRandom();
		double d = randomSource.triangle((double)direction.getStepX(), 0.11485000000000001);
		double e = randomSource.triangle((double)direction.getStepY(), 0.11485000000000001);
		double f = randomSource.triangle((double)direction.getStepZ(), 0.11485000000000001);
		SmallFireball smallFireball = new SmallFireball(level, position.x(), position.y(), position.z(), d, e, f);
		smallFireball.setItem(itemStack);
		return smallFireball;
	}

	@Override
	public void shoot(Projectile projectile, double d, double e, double f, float g, float h) {
	}

	@Override
	public ProjectileItem.DispenseConfig createDispenseConfig() {
		return ProjectileItem.DispenseConfig.builder()
			.positionFunction((blockSource, direction) -> DispenserBlock.getDispensePosition(blockSource, 1.0, Vec3.ZERO))
			.uncertainty(6.6666665F)
			.power(1.0F)
			.overrideDispenseEvent(1018)
			.build();
	}
}
