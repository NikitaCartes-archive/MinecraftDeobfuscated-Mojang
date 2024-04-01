package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.XpComponent;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BigBrainBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BigBrainBlock extends BaseEntityBlock {
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	private static final VoxelShape SHAPE = Shapes.or(Block.box(6.0, 0.0, 6.0, 10.0, 5.0, 10.0), Block.box(1.0, 5.0, 1.0, 15.0, 15.0, 15.0));
	public static final MapCodec<BigBrainBlock> CODEC = simpleCodec(BigBrainBlock::new);
	public static final int XP_DROP_THRESHOLD = 1000;

	public BigBrainBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		return this.defaultBlockState().setValue(FACING, blockPlaceContext.getHorizontalDirection());
	}

	@Override
	public MapCodec<BigBrainBlock> codec() {
		return CODEC;
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new BigBrainBlockEntity(blockPos, blockState);
	}

	@Override
	protected void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		if (!level.isClientSide && entity instanceof ExperienceOrb experienceOrb) {
			level.getBlockEntity(blockPos, BlockEntityType.BIG_BRAIN).ifPresent(bigBrainBlockEntity -> {
				int i = bigBrainBlockEntity.getXp() + experienceOrb.getTotalValue();
				experienceOrb.discard();

				while (i >= 1000) {
					ItemStack itemStack = new ItemStack(Items.POTATO_OF_KNOWLEDGE);
					itemStack.set(DataComponents.XP, new XpComponent(1000));
					popResource(level, blockPos, itemStack);
					i -= 1000;
				}

				bigBrainBlockEntity.setXp(i);
				bigBrainBlockEntity.setChanged();
			});
		}
	}

	@Override
	protected RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
		return createTickerHelper(blockEntityType, BlockEntityType.BIG_BRAIN, BigBrainBlockEntity::tick);
	}
}
