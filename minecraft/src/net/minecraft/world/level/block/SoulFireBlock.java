package net.minecraft.world.level.block;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class SoulFireBlock extends BaseFireBlock {
	public static final int MAX_AGE = 25;
	public static final IntegerProperty AGE = BlockStateProperties.AGE_25;
	private final Supplier<BiMap<Block, Tuple<Double, Block>>> fireInteractions = Suppliers.memoize(
		() -> ImmutableBiMap.<Block, Tuple<Double, Block>>builder()
				.put(Blocks.IRON_ORE, new Tuple<>(0.1, Blocks.IRON_BLOCK))
				.put(Blocks.FIRE, new Tuple<>(0.5, Blocks.SOUL_FIRE))
				.build()
	);

	public SoulFireBlock(BlockBehaviour.Properties properties) {
		super(properties, 2.0F);
		this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AGE);
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		return this.canSurvive(blockState, levelAccessor, blockPos) ? this.defaultBlockState() : Blocks.AIR.defaultBlockState();
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		serverLevel.scheduleTick(blockPos, this, getFireTickDelay(serverLevel.random));
		int i = (Integer)blockState.getValue(AGE);
		if (random.nextFloat() < 0.2F + (float)i * 0.03F) {
			serverLevel.removeBlock(blockPos, false);
		} else {
			int j = Math.min(25, i + random.nextInt(3) / 2);
			if (i != j) {
				blockState = blockState.setValue(AGE, Integer.valueOf(j));
				serverLevel.setBlock(blockPos, blockState, 4);

				for (Direction direction : Direction.values()) {
					BlockPos blockPos2 = blockPos.relative(direction);
					Block block = serverLevel.getBlockState(blockPos2).getBlock();
					if (((BiMap)this.fireInteractions.get()).containsKey(block)) {
						Tuple<Double, Block> tuple = (Tuple<Double, Block>)((BiMap)this.fireInteractions.get()).get(block);
						if (serverLevel.random.nextDouble() > tuple.getA()) {
							serverLevel.setBlock(blockPos2, tuple.getB().defaultBlockState(), 3);
						}
					}
				}
			}
		}
	}

	@Override
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		super.onPlace(blockState, level, blockPos, blockState2, bl);
		level.scheduleTick(blockPos, this, getFireTickDelay(level.random));
	}

	private static int getFireTickDelay(Random random) {
		return 30 + random.nextInt(10);
	}

	@Override
	public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		return canSurviveOnBlock(levelReader.getBlockState(blockPos.below()));
	}

	public static boolean canSurviveOnBlock(BlockState blockState) {
		return true;
	}

	@Override
	protected boolean canBurn(BlockState blockState) {
		return true;
	}
}
