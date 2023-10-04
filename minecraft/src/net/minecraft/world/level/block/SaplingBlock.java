package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SaplingBlock extends BushBlock implements BonemealableBlock {
	public static final MapCodec<SaplingBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(TreeGrower.CODEC.fieldOf("tree").forGetter(saplingBlock -> saplingBlock.treeGrower), propertiesCodec())
				.apply(instance, SaplingBlock::new)
	);
	public static final IntegerProperty STAGE = BlockStateProperties.STAGE;
	protected static final float AABB_OFFSET = 6.0F;
	protected static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 12.0, 14.0);
	protected final TreeGrower treeGrower;

	@Override
	public MapCodec<? extends SaplingBlock> codec() {
		return CODEC;
	}

	protected SaplingBlock(TreeGrower treeGrower, BlockBehaviour.Properties properties) {
		super(properties);
		this.treeGrower = treeGrower;
		this.registerDefaultState(this.stateDefinition.any().setValue(STAGE, Integer.valueOf(0)));
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		if (serverLevel.getMaxLocalRawBrightness(blockPos.above()) >= 9 && randomSource.nextInt(7) == 0) {
			this.advanceTree(serverLevel, blockPos, blockState, randomSource);
		}
	}

	public void advanceTree(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, RandomSource randomSource) {
		if ((Integer)blockState.getValue(STAGE) == 0) {
			serverLevel.setBlock(blockPos, blockState.cycle(STAGE), 4);
		} else {
			this.treeGrower.growTree(serverLevel, serverLevel.getChunkSource().getGenerator(), blockPos, blockState, randomSource);
		}
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
		return true;
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		return (double)level.random.nextFloat() < 0.45;
	}

	@Override
	public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		this.advanceTree(serverLevel, blockPos, blockState, randomSource);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(STAGE);
	}
}
