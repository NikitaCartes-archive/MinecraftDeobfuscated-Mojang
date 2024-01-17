package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CropBlock extends BushBlock implements BonemealableBlock {
	public static final MapCodec<CropBlock> CODEC = simpleCodec(CropBlock::new);
	public static final int MAX_AGE = 7;
	public static final IntegerProperty AGE = BlockStateProperties.AGE_7;
	private static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[]{
		Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
		Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
		Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0),
		Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0),
		Block.box(0.0, 0.0, 0.0, 16.0, 10.0, 16.0),
		Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0),
		Block.box(0.0, 0.0, 0.0, 16.0, 14.0, 16.0),
		Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)
	};

	@Override
	public MapCodec<? extends CropBlock> codec() {
		return CODEC;
	}

	protected CropBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(this.getAgeProperty(), Integer.valueOf(0)));
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE_BY_AGE[this.getAge(blockState)];
	}

	@Override
	protected boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return blockState.is(Blocks.FARMLAND);
	}

	protected IntegerProperty getAgeProperty() {
		return AGE;
	}

	public int getMaxAge() {
		return 7;
	}

	public int getAge(BlockState blockState) {
		return (Integer)blockState.getValue(this.getAgeProperty());
	}

	public BlockState getStateForAge(int i) {
		return this.defaultBlockState().setValue(this.getAgeProperty(), Integer.valueOf(i));
	}

	public final boolean isMaxAge(BlockState blockState) {
		return this.getAge(blockState) >= this.getMaxAge();
	}

	@Override
	protected boolean isRandomlyTicking(BlockState blockState) {
		return !this.isMaxAge(blockState);
	}

	@Override
	protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		if (serverLevel.getRawBrightness(blockPos, 0) >= 9) {
			int i = this.getAge(blockState);
			if (i < this.getMaxAge()) {
				float f = getGrowthSpeed(this, serverLevel, blockPos);
				if (randomSource.nextInt((int)(25.0F / f) + 1) == 0) {
					serverLevel.setBlock(blockPos, this.getStateForAge(i + 1), 2);
				}
			}
		}
	}

	public void growCrops(Level level, BlockPos blockPos, BlockState blockState) {
		int i = this.getAge(blockState) + this.getBonemealAgeIncrease(level);
		int j = this.getMaxAge();
		if (i > j) {
			i = j;
		}

		level.setBlock(blockPos, this.getStateForAge(i), 2);
	}

	protected int getBonemealAgeIncrease(Level level) {
		return Mth.nextInt(level.random, 2, 5);
	}

	protected static float getGrowthSpeed(Block block, BlockGetter blockGetter, BlockPos blockPos) {
		float f = 1.0F;
		BlockPos blockPos2 = blockPos.below();

		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				float g = 0.0F;
				BlockState blockState = blockGetter.getBlockState(blockPos2.offset(i, 0, j));
				if (blockState.is(Blocks.FARMLAND)) {
					g = 1.0F;
					if ((Integer)blockState.getValue(FarmBlock.MOISTURE) > 0) {
						g = 3.0F;
					}
				}

				if (i != 0 || j != 0) {
					g /= 4.0F;
				}

				f += g;
			}
		}

		BlockPos blockPos3 = blockPos.north();
		BlockPos blockPos4 = blockPos.south();
		BlockPos blockPos5 = blockPos.west();
		BlockPos blockPos6 = blockPos.east();
		boolean bl = blockGetter.getBlockState(blockPos5).is(block) || blockGetter.getBlockState(blockPos6).is(block);
		boolean bl2 = blockGetter.getBlockState(blockPos3).is(block) || blockGetter.getBlockState(blockPos4).is(block);
		if (bl && bl2) {
			f /= 2.0F;
		} else {
			boolean bl3 = blockGetter.getBlockState(blockPos5.north()).is(block)
				|| blockGetter.getBlockState(blockPos6.north()).is(block)
				|| blockGetter.getBlockState(blockPos6.south()).is(block)
				|| blockGetter.getBlockState(blockPos5.south()).is(block);
			if (bl3) {
				f /= 2.0F;
			}
		}

		return f;
	}

	@Override
	protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		return hasSufficientLight(levelReader, blockPos) && super.canSurvive(blockState, levelReader, blockPos);
	}

	protected static boolean hasSufficientLight(LevelReader levelReader, BlockPos blockPos) {
		return levelReader.getRawBrightness(blockPos, 0) >= 8;
	}

	@Override
	protected void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		if (entity instanceof Ravager && level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
			level.destroyBlock(blockPos, true, entity);
		}

		super.entityInside(blockState, level, blockPos, entity);
	}

	protected ItemLike getBaseSeedId() {
		return Items.WHEAT_SEEDS;
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
		return new ItemStack(this.getBaseSeedId());
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
		return !this.isMaxAge(blockState);
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		this.growCrops(serverLevel, blockPos, blockState);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AGE);
	}
}
