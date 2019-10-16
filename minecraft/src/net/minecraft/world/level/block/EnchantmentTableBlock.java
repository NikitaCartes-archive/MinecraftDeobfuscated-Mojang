package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EnchantmentTableBlock extends BaseEntityBlock {
	protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);

	protected EnchantmentTableBlock(Block.Properties properties) {
		super(properties);
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState blockState) {
		return true;
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
		super.animateTick(blockState, level, blockPos, random);

		for (int i = -2; i <= 2; i++) {
			for (int j = -2; j <= 2; j++) {
				if (i > -2 && i < 2 && j == -1) {
					j = 2;
				}

				if (random.nextInt(16) == 0) {
					for (int k = 0; k <= 1; k++) {
						BlockPos blockPos2 = blockPos.offset(i, k, j);
						if (level.getBlockState(blockPos2).getBlock() == Blocks.BOOKSHELF) {
							if (!level.isEmptyBlock(blockPos.offset(i / 2, 0, j / 2))) {
								break;
							}

							level.addParticle(
								ParticleTypes.ENCHANT,
								(double)blockPos.getX() + 0.5,
								(double)blockPos.getY() + 2.0,
								(double)blockPos.getZ() + 0.5,
								(double)((float)i + random.nextFloat()) - 0.5,
								(double)((float)k - random.nextFloat() - 1.0F),
								(double)((float)j + random.nextFloat()) - 0.5
							);
						}
					}
				}
			}
		}
	}

	@Override
	public RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockEntity newBlockEntity(BlockGetter blockGetter) {
		return new EnchantmentTableBlockEntity();
	}

	@Override
	public InteractionResult use(
		BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		} else {
			player.openMenu(blockState.getMenuProvider(level, blockPos));
			return InteractionResult.SUCCESS;
		}
	}

	@Nullable
	@Override
	public MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos blockPos) {
		BlockEntity blockEntity = level.getBlockEntity(blockPos);
		if (blockEntity instanceof EnchantmentTableBlockEntity) {
			Component component = ((Nameable)blockEntity).getDisplayName();
			return new SimpleMenuProvider((i, inventory, player) -> new EnchantmentMenu(i, inventory, ContainerLevelAccess.create(level, blockPos)), component);
		} else {
			return null;
		}
	}

	@Override
	public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) {
		if (itemStack.hasCustomHoverName()) {
			BlockEntity blockEntity = level.getBlockEntity(blockPos);
			if (blockEntity instanceof EnchantmentTableBlockEntity) {
				((EnchantmentTableBlockEntity)blockEntity).setCustomName(itemStack.getHoverName());
			}
		}
	}

	@Override
	public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
		return false;
	}
}
