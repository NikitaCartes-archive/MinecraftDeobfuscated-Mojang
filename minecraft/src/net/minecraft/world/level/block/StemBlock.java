package net.minecraft.world.level.block;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class StemBlock extends BushBlock implements BonemealableBlock {
	public static final MapCodec<StemBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					ResourceKey.codec(Registries.BLOCK).fieldOf("fruit").forGetter(stemBlock -> stemBlock.fruit),
					ResourceKey.codec(Registries.BLOCK).fieldOf("attached_stem").forGetter(stemBlock -> stemBlock.attachedStem),
					ResourceKey.codec(Registries.ITEM).fieldOf("seed").forGetter(stemBlock -> stemBlock.seed),
					propertiesCodec()
				)
				.apply(instance, StemBlock::new)
	);
	public static final int MAX_AGE = 7;
	public static final IntegerProperty AGE = BlockStateProperties.AGE_7;
	protected static final float AABB_OFFSET = 1.0F;
	protected static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[]{
		Block.box(7.0, 0.0, 7.0, 9.0, 2.0, 9.0),
		Block.box(7.0, 0.0, 7.0, 9.0, 4.0, 9.0),
		Block.box(7.0, 0.0, 7.0, 9.0, 6.0, 9.0),
		Block.box(7.0, 0.0, 7.0, 9.0, 8.0, 9.0),
		Block.box(7.0, 0.0, 7.0, 9.0, 10.0, 9.0),
		Block.box(7.0, 0.0, 7.0, 9.0, 12.0, 9.0),
		Block.box(7.0, 0.0, 7.0, 9.0, 14.0, 9.0),
		Block.box(7.0, 0.0, 7.0, 9.0, 16.0, 9.0)
	};
	private final ResourceKey<Block> fruit;
	private final ResourceKey<Block> attachedStem;
	private final ResourceKey<Item> seed;

	@Override
	public MapCodec<StemBlock> codec() {
		return CODEC;
	}

	protected StemBlock(ResourceKey<Block> resourceKey, ResourceKey<Block> resourceKey2, ResourceKey<Item> resourceKey3, BlockBehaviour.Properties properties) {
		super(properties);
		this.fruit = resourceKey;
		this.attachedStem = resourceKey2;
		this.seed = resourceKey3;
		this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE_BY_AGE[blockState.getValue(AGE)];
	}

	@Override
	protected boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return blockState.is(Blocks.FARMLAND);
	}

	@Override
	protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		if (serverLevel.getRawBrightness(blockPos, 0) >= 9) {
			float f = CropBlock.getGrowthSpeed(this, serverLevel, blockPos);
			if (randomSource.nextInt((int)(25.0F / f) + 1) == 0) {
				int i = (Integer)blockState.getValue(AGE);
				if (i < 7) {
					blockState = blockState.setValue(AGE, Integer.valueOf(i + 1));
					serverLevel.setBlock(blockPos, blockState, 2);
				} else {
					Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(randomSource);
					BlockPos blockPos2 = blockPos.relative(direction);
					BlockState blockState2 = serverLevel.getBlockState(blockPos2.below());
					if (serverLevel.getBlockState(blockPos2).isAir() && (blockState2.is(Blocks.FARMLAND) || blockState2.is(BlockTags.DIRT))) {
						Registry<Block> registry = serverLevel.registryAccess().lookupOrThrow(Registries.BLOCK);
						Optional<Block> optional = registry.getOptional(this.fruit);
						Optional<Block> optional2 = registry.getOptional(this.attachedStem);
						if (optional.isPresent() && optional2.isPresent()) {
							serverLevel.setBlockAndUpdate(blockPos2, ((Block)optional.get()).defaultBlockState());
							serverLevel.setBlockAndUpdate(blockPos, ((Block)optional2.get()).defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, direction));
						}
					}
				}
			}
		}
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
		return new ItemStack(DataFixUtils.orElse(levelReader.registryAccess().lookupOrThrow(Registries.ITEM).getOptional(this.seed), this));
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
		return (Integer)blockState.getValue(AGE) != 7;
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		int i = Math.min(7, (Integer)blockState.getValue(AGE) + Mth.nextInt(serverLevel.random, 2, 5));
		BlockState blockState2 = blockState.setValue(AGE, Integer.valueOf(i));
		serverLevel.setBlock(blockPos, blockState2, 2);
		if (i == 7) {
			blockState2.randomTick(serverLevel, blockPos, serverLevel.random);
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AGE);
	}
}
