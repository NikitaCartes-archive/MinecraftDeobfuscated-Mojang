package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.voting.rules.Rules;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class TntBlock extends Block {
	public static final BooleanProperty UNSTABLE = BlockStateProperties.UNSTABLE;

	public TntBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.defaultBlockState().setValue(UNSTABLE, Boolean.valueOf(false)));
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		BlockState blockState = super.getStateForPlacement(blockPlaceContext);
		return blockState != null && Rules.UNSTABLE_TNT.get() ? blockState.setValue(UNSTABLE, Boolean.valueOf(true)) : blockState;
	}

	@Override
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!blockState2.is(blockState.getBlock())) {
			if (level.hasNeighborSignal(blockPos)) {
				this.explode(level, blockPos);
				level.removeBlock(blockPos, false);
			}
		}
	}

	@Override
	public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		if (level.hasNeighborSignal(blockPos)) {
			this.explode(level, blockPos);
			level.removeBlock(blockPos, false);
		}
	}

	@Override
	public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
		if (!level.isClientSide() && !player.isCreative() && (Boolean)blockState.getValue(UNSTABLE)) {
			this.explode(level, blockPos);
		}

		super.playerWillDestroy(level, blockPos, blockState, player);
	}

	@Override
	public void wasExploded(Level level, BlockPos blockPos, Explosion explosion) {
		if (!level.isClientSide) {
			PrimedTnt primedTnt = new PrimedTnt(
				level, (double)blockPos.getX() + 0.5, (double)blockPos.getY(), (double)blockPos.getZ() + 0.5, explosion.getIndirectSourceEntity()
			);
			int i = primedTnt.getFuse();
			primedTnt.setFuse((short)(level.random.nextInt(i / 4) + i / 8));
			level.addFreshEntity(primedTnt);
		}
	}

	private void explode(Level level, BlockPos blockPos) {
		explode(level, blockPos, null, this.defaultBlockState());
	}

	public static void explode(Level level, BlockPos blockPos, BlockState blockState) {
		explode(level, blockPos, null, blockState);
	}

	public static void explode(Level level, BlockPos blockPos, @Nullable LivingEntity livingEntity, BlockState blockState) {
		if (!level.isClientSide) {
			PrimedTnt primedTnt = new PrimedTnt(level, (double)blockPos.getX() + 0.5, (double)blockPos.getY(), (double)blockPos.getZ() + 0.5, livingEntity);
			primedTnt.setBlockState(blockState);
			level.addFreshEntity(primedTnt);
			level.playSound(null, primedTnt.getX(), primedTnt.getY(), primedTnt.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
			level.gameEvent(livingEntity, GameEvent.PRIME_FUSE, blockPos);
		}
	}

	@Override
	public InteractionResult use(
		BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (!itemStack.is(Items.FLINT_AND_STEEL) && !itemStack.is(Items.FIRE_CHARGE)) {
			return super.use(blockState, level, blockPos, player, interactionHand, blockHitResult);
		} else {
			explode(level, blockPos, player, this.defaultBlockState());
			level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 11);
			Item item = itemStack.getItem();
			if (!player.isCreative()) {
				if (itemStack.is(Items.FLINT_AND_STEEL)) {
					itemStack.hurtAndBreak(1, player, playerx -> playerx.broadcastBreakEvent(interactionHand));
				} else {
					itemStack.shrink(1);
				}
			}

			player.awardStat(Stats.ITEM_USED.get(item));
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
	}

	@Override
	public void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
		if (!level.isClientSide) {
			BlockPos blockPos = blockHitResult.getBlockPos();
			Entity entity = projectile.getOwner();
			if (projectile.isOnFire() && projectile.mayInteract(level, blockPos)) {
				explode(level, blockPos, entity instanceof LivingEntity ? (LivingEntity)entity : null, this.defaultBlockState());
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
