package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
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
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;

public class TntBlock extends Block {
	public static final MapCodec<TntBlock> CODEC = simpleCodec(TntBlock::new);
	public static final BooleanProperty UNSTABLE = BlockStateProperties.UNSTABLE;

	@Override
	public MapCodec<TntBlock> codec() {
		return CODEC;
	}

	public TntBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.defaultBlockState().setValue(UNSTABLE, Boolean.valueOf(false)));
	}

	@Override
	protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!blockState2.is(blockState.getBlock())) {
			if (level.hasNeighborSignal(blockPos)) {
				explode(level, blockPos);
				level.removeBlock(blockPos, false);
			}
		}
	}

	@Override
	protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, @Nullable Orientation orientation, boolean bl) {
		if (level.hasNeighborSignal(blockPos)) {
			explode(level, blockPos);
			level.removeBlock(blockPos, false);
		}
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
		if (!level.isClientSide() && !player.isCreative() && (Boolean)blockState.getValue(UNSTABLE)) {
			explode(level, blockPos);
		}

		return super.playerWillDestroy(level, blockPos, blockState, player);
	}

	@Override
	public void wasExploded(ServerLevel serverLevel, BlockPos blockPos, Explosion explosion) {
		PrimedTnt primedTnt = new PrimedTnt(
			serverLevel, (double)blockPos.getX() + 0.5, (double)blockPos.getY(), (double)blockPos.getZ() + 0.5, explosion.getIndirectSourceEntity()
		);
		int i = primedTnt.getFuse();
		primedTnt.setFuse((short)(serverLevel.random.nextInt(i / 4) + i / 8));
		serverLevel.addFreshEntity(primedTnt);
	}

	public static void explode(Level level, BlockPos blockPos) {
		explode(level, blockPos, null);
	}

	private static void explode(Level level, BlockPos blockPos, @Nullable LivingEntity livingEntity) {
		if (!level.isClientSide) {
			PrimedTnt primedTnt = new PrimedTnt(level, (double)blockPos.getX() + 0.5, (double)blockPos.getY(), (double)blockPos.getZ() + 0.5, livingEntity);
			level.addFreshEntity(primedTnt);
			level.playSound(null, primedTnt.getX(), primedTnt.getY(), primedTnt.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
			level.gameEvent(livingEntity, GameEvent.PRIME_FUSE, blockPos);
		}
	}

	@Override
	protected InteractionResult useItemOn(
		ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		if (!itemStack.is(Items.FLINT_AND_STEEL) && !itemStack.is(Items.FIRE_CHARGE)) {
			return super.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);
		} else {
			explode(level, blockPos, player);
			level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 11);
			Item item = itemStack.getItem();
			if (itemStack.is(Items.FLINT_AND_STEEL)) {
				itemStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(interactionHand));
			} else {
				itemStack.consume(1, player);
			}

			player.awardStat(Stats.ITEM_USED.get(item));
			return InteractionResult.SUCCESS;
		}
	}

	@Override
	protected void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
		if (level instanceof ServerLevel serverLevel) {
			BlockPos blockPos = blockHitResult.getBlockPos();
			Entity entity = projectile.getOwner();
			if (projectile.isOnFire() && projectile.mayInteract(serverLevel, blockPos)) {
				explode(level, blockPos, entity instanceof LivingEntity ? (LivingEntity)entity : null);
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
