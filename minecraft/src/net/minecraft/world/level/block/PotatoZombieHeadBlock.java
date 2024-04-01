package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class PotatoZombieHeadBlock extends HorizontalDirectionalBlock {
	public static final MapCodec<PotatoZombieHeadBlock> CODEC = simpleCodec(PotatoZombieHeadBlock::new);
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	@Override
	public MapCodec<? extends PotatoZombieHeadBlock> codec() {
		return CODEC;
	}

	protected PotatoZombieHeadBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		return this.defaultBlockState().setValue(FACING, blockPlaceContext.getHorizontalDirection().getOpposite());
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
}
