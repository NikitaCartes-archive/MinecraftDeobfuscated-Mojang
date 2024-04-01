package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PedestalBlock extends Block {
	private static final VoxelShape TOP = Block.box(0.0, 14.0, 0.0, 16.0, 16.0, 16.0);
	private static final VoxelShape BOTTOM = Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);
	private static final VoxelShape BASE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
	private static final VoxelShape SHAPE = Shapes.or(TOP, BASE, BOTTOM);
	public static final MapCodec<PedestalBlock> CODEC = simpleCodec(PedestalBlock::new);

	@Override
	public MapCodec<PedestalBlock> codec() {
		return CODEC;
	}

	public PedestalBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	protected VoxelShape getInteractionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return SHAPE;
	}

	@Override
	protected ItemInteractionResult useItemOn(
		ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		if (itemStack.is(Items.POISONOUS_POTATO)
			&& level.getBlockState(blockPos.above()).canBeReplaced()
			&& level instanceof ServerLevel serverLevel
			&& level.dimensionType().natural()) {
			level.setBlock(blockPos.above(), Blocks.POTATO_PORTAL.defaultBlockState(), 3);
			itemStack.consume(1, player);
			serverLevel.sendParticles(
				ParticleTypes.ELECTRIC_SPARK, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 1.5, (double)blockPos.getZ() + 0.5, 100, 0.5, 0.5, 0.5, 0.2
			);
			serverLevel.playSound(null, blockPos, SoundEvents.PLAGUEWHALE_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F);
			if (!player.chapterIsPast("portal_opened")) {
				player.setPotatoQuestChapter("portal_opened");
			}

			return ItemInteractionResult.SUCCESS;
		}

		return super.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);
	}
}
