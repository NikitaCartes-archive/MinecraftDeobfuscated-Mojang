package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class InfestedRotatedPillarBlock extends InfestedBlock {
	public static final MapCodec<InfestedRotatedPillarBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(BuiltInRegistries.BLOCK.byNameCodec().fieldOf("host").forGetter(InfestedBlock::getHostBlock), propertiesCodec())
				.apply(instance, InfestedRotatedPillarBlock::new)
	);

	@Override
	public MapCodec<InfestedRotatedPillarBlock> codec() {
		return CODEC;
	}

	public InfestedRotatedPillarBlock(Block block, BlockBehaviour.Properties properties) {
		super(block, properties);
		this.registerDefaultState(this.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
	}

	@Override
	protected BlockState rotate(BlockState blockState, Rotation rotation) {
		return RotatedPillarBlock.rotatePillar(blockState, rotation);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(RotatedPillarBlock.AXIS);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		return this.defaultBlockState().setValue(RotatedPillarBlock.AXIS, blockPlaceContext.getClickedFace().getAxis());
	}
}
