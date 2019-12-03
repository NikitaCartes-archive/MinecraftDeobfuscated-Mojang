package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class TntBlock extends Block {
	public static final BooleanProperty UNSTABLE = BlockStateProperties.UNSTABLE;

	public TntBlock(Block.Properties properties) {
		super(properties);
		this.registerDefaultState(this.defaultBlockState().setValue(UNSTABLE, Boolean.valueOf(false)));
	}

	@Override
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (blockState2.getBlock() != blockState.getBlock()) {
			if (level.hasNeighborSignal(blockPos)) {
				explode(level, blockPos);
				level.removeBlock(blockPos, false);
			}
		}
	}

	@Override
	public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		if (level.hasNeighborSignal(blockPos)) {
			explode(level, blockPos);
			level.removeBlock(blockPos, false);
		}
	}

	@Override
	public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
		if (!level.isClientSide() && !player.isCreative() && (Boolean)blockState.getValue(UNSTABLE)) {
			explode(level, blockPos);
		}

		super.playerWillDestroy(level, blockPos, blockState, player);
	}

	@Override
	public void wasExploded(Level level, BlockPos blockPos, Explosion explosion) {
		if (!level.isClientSide) {
			PrimedTnt primedTnt = new PrimedTnt(
				level, (double)((float)blockPos.getX() + 0.5F), (double)blockPos.getY(), (double)((float)blockPos.getZ() + 0.5F), explosion.getSourceMob()
			);
			primedTnt.setFuse((short)(level.random.nextInt(primedTnt.getLife() / 4) + primedTnt.getLife() / 8));
			level.addFreshEntity(primedTnt);
		}
	}

	public static void explode(Level level, BlockPos blockPos) {
		explode(level, blockPos, null);
	}

	private static void explode(Level level, BlockPos blockPos, @Nullable LivingEntity livingEntity) {
		if (!level.isClientSide) {
			PrimedTnt primedTnt = new PrimedTnt(level, (double)blockPos.getX() + 0.5, (double)blockPos.getY(), (double)blockPos.getZ() + 0.5, livingEntity);
			level.addFreshEntity(primedTnt);
			level.playSound(null, primedTnt.getX(), primedTnt.getY(), primedTnt.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
		}
	}

	@Override
	public InteractionResult use(
		BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		Item item = itemStack.getItem();
		if (item != Items.FLINT_AND_STEEL && item != Items.FIRE_CHARGE) {
			return super.use(blockState, level, blockPos, player, interactionHand, blockHitResult);
		} else {
			explode(level, blockPos, player);
			level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 11);
			if (!player.isCreative()) {
				if (item == Items.FLINT_AND_STEEL) {
					itemStack.hurtAndBreak(1, player, playerx -> playerx.broadcastBreakEvent(interactionHand));
				} else {
					itemStack.shrink(1);
				}
			}

			return InteractionResult.SUCCESS;
		}
	}

	@Override
	public void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Entity entity) {
		if (!level.isClientSide && entity instanceof AbstractArrow) {
			AbstractArrow abstractArrow = (AbstractArrow)entity;
			Entity entity2 = abstractArrow.getOwner();
			if (abstractArrow.isOnFire()) {
				BlockPos blockPos = blockHitResult.getBlockPos();
				explode(level, blockPos, entity2 instanceof LivingEntity ? (LivingEntity)entity2 : null);
				level.removeBlock(blockPos, false);
			}
		}
	}

	@Override
	public boolean dropFromExplosion(Explosion explosion) {
		return false;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(UNSTABLE);
	}
}
