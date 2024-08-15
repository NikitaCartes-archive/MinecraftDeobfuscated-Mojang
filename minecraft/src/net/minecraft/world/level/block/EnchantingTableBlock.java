package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EnchantingTableBlock extends BaseEntityBlock {
	public static final MapCodec<EnchantingTableBlock> CODEC = simpleCodec(EnchantingTableBlock::new);
	protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);
	public static final List<BlockPos> BOOKSHELF_OFFSETS = BlockPos.betweenClosedStream(-2, 0, -2, 2, 1, 2)
		.filter(blockPos -> Math.abs(blockPos.getX()) == 2 || Math.abs(blockPos.getZ()) == 2)
		.map(BlockPos::immutable)
		.toList();

	@Override
	public MapCodec<EnchantingTableBlock> codec() {
		return CODEC;
	}

	protected EnchantingTableBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	public static boolean isValidBookShelf(Level level, BlockPos blockPos, BlockPos blockPos2) {
		return level.getBlockState(blockPos.offset(blockPos2)).is(BlockTags.ENCHANTMENT_POWER_PROVIDER)
			&& level.getBlockState(blockPos.offset(blockPos2.getX() / 2, blockPos2.getY(), blockPos2.getZ() / 2)).is(BlockTags.ENCHANTMENT_POWER_TRANSMITTER);
	}

	@Override
	protected boolean useShapeForLightOcclusion(BlockState blockState) {
		return true;
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
		super.animateTick(blockState, level, blockPos, randomSource);

		for (BlockPos blockPos2 : BOOKSHELF_OFFSETS) {
			if (randomSource.nextInt(16) == 0 && isValidBookShelf(level, blockPos, blockPos2)) {
				level.addParticle(
					ParticleTypes.ENCHANT,
					(double)blockPos.getX() + 0.5,
					(double)blockPos.getY() + 2.0,
					(double)blockPos.getZ() + 0.5,
					(double)((float)blockPos2.getX() + randomSource.nextFloat()) - 0.5,
					(double)((float)blockPos2.getY() - randomSource.nextFloat() - 1.0F),
					(double)((float)blockPos2.getZ() + randomSource.nextFloat()) - 0.5
				);
			}
		}
	}

	@Override
	protected RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new EnchantingTableBlockEntity(blockPos, blockState);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
		return level.isClientSide ? createTickerHelper(blockEntityType, BlockEntityType.ENCHANTING_TABLE, EnchantingTableBlockEntity::bookAnimationTick) : null;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
		if (!level.isClientSide) {
			player.openMenu(blockState.getMenuProvider(level, blockPos));
		}

		return InteractionResult.SUCCESS;
	}

	@Nullable
	@Override
	protected MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos blockPos) {
		BlockEntity blockEntity = level.getBlockEntity(blockPos);
		if (blockEntity instanceof EnchantingTableBlockEntity) {
			Component component = ((Nameable)blockEntity).getDisplayName();
			return new SimpleMenuProvider((i, inventory, player) -> new EnchantmentMenu(i, inventory, ContainerLevelAccess.create(level, blockPos)), component);
		} else {
			return null;
		}
	}

	@Override
	protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
		return false;
	}
}
