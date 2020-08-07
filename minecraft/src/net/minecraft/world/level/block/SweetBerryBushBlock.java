package net.minecraft.world.level.block;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SweetBerryBushBlock extends BushBlock implements BonemealableBlock {
	public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
	private static final VoxelShape SAPLING_SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 8.0, 13.0);
	private static final VoxelShape MID_GROWTH_SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);

	public SweetBerryBushBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
	}

	@Environment(EnvType.CLIENT)
	@Override
	public ItemStack getCloneItemStack(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
		return new ItemStack(Items.SWEET_BERRIES);
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		if ((Integer)blockState.getValue(AGE) == 0) {
			return SAPLING_SHAPE;
		} else {
			return blockState.getValue(AGE) < 3 ? MID_GROWTH_SHAPE : super.getShape(blockState, blockGetter, blockPos, collisionContext);
		}
	}

	@Override
	public boolean isRandomlyTicking(BlockState blockState) {
		return (Integer)blockState.getValue(AGE) < 3;
	}

	@Override
	public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		int i = (Integer)blockState.getValue(AGE);
		if (i < 3 && random.nextInt(5) == 0 && serverLevel.getRawBrightness(blockPos.above(), 0) >= 9) {
			serverLevel.setBlock(blockPos, blockState.setValue(AGE, Integer.valueOf(i + 1)), 2);
		}
	}

	@Override
	public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		if (entity instanceof LivingEntity && entity.getType() != EntityType.FOX && entity.getType() != EntityType.BEE) {
			entity.makeStuckInBlock(blockState, new Vec3(0.8F, 0.75, 0.8F));
			if (!level.isClientSide && (Integer)blockState.getValue(AGE) > 0 && (entity.xOld != entity.getX() || entity.zOld != entity.getZ())) {
				double d = Math.abs(entity.getX() - entity.xOld);
				double e = Math.abs(entity.getZ() - entity.zOld);
				if (d >= 0.003F || e >= 0.003F) {
					entity.hurt(DamageSource.SWEET_BERRY_BUSH, 1.0F);
				}
			}
		}
	}

	@Override
	public InteractionResult use(
		BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		int i = (Integer)blockState.getValue(AGE);
		boolean bl = i == 3;
		if (!bl && player.getItemInHand(interactionHand).getItem() == Items.BONE_MEAL) {
			return InteractionResult.PASS;
		} else if (i > 1) {
			int j = 1 + level.random.nextInt(2);
			popResource(level, blockPos, new ItemStack(Items.SWEET_BERRIES, j + (bl ? 1 : 0)));
			level.playSound(null, blockPos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
			level.setBlock(blockPos, blockState.setValue(AGE, Integer.valueOf(1)), 2);
			return InteractionResult.sidedSuccess(level.isClientSide);
		} else {
			return super.use(blockState, level, blockPos, player, interactionHand, blockHitResult);
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AGE);
	}

	@Override
	public boolean isValidBonemealTarget(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, boolean bl) {
		return (Integer)blockState.getValue(AGE) < 3;
	}

	@Override
	public boolean isBonemealSuccess(Level level, Random random, BlockPos blockPos, BlockState blockState) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel serverLevel, Random random, BlockPos blockPos, BlockState blockState) {
		int i = Math.min(3, (Integer)blockState.getValue(AGE) + 1);
		serverLevel.setBlock(blockPos, blockState.setValue(AGE, Integer.valueOf(i)), 2);
	}
}
