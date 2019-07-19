package net.minecraft.world.level.block;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class RedStoneOreBlock extends Block {
	public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;

	public RedStoneOreBlock(Block.Properties properties) {
		super(properties);
		this.registerDefaultState(this.defaultBlockState().setValue(LIT, Boolean.valueOf(false)));
	}

	@Override
	public int getLightEmission(BlockState blockState) {
		return blockState.getValue(LIT) ? super.getLightEmission(blockState) : 0;
	}

	@Override
	public void attack(BlockState blockState, Level level, BlockPos blockPos, Player player) {
		interact(blockState, level, blockPos);
		super.attack(blockState, level, blockPos, player);
	}

	@Override
	public void stepOn(Level level, BlockPos blockPos, Entity entity) {
		interact(level.getBlockState(blockPos), level, blockPos);
		super.stepOn(level, blockPos, entity);
	}

	@Override
	public boolean use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
		interact(blockState, level, blockPos);
		return super.use(blockState, level, blockPos, player, interactionHand, blockHitResult);
	}

	private static void interact(BlockState blockState, Level level, BlockPos blockPos) {
		spawnParticles(level, blockPos);
		if (!(Boolean)blockState.getValue(LIT)) {
			level.setBlock(blockPos, blockState.setValue(LIT, Boolean.valueOf(true)), 3);
		}
	}

	@Override
	public void tick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
		if ((Boolean)blockState.getValue(LIT)) {
			level.setBlock(blockPos, blockState.setValue(LIT, Boolean.valueOf(false)), 3);
		}
	}

	@Override
	public void spawnAfterBreak(BlockState blockState, Level level, BlockPos blockPos, ItemStack itemStack) {
		super.spawnAfterBreak(blockState, level, blockPos, itemStack);
		if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, itemStack) == 0) {
			int i = 1 + level.random.nextInt(5);
			this.popExperience(level, blockPos, i);
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
		if ((Boolean)blockState.getValue(LIT)) {
			spawnParticles(level, blockPos);
		}
	}

	private static void spawnParticles(Level level, BlockPos blockPos) {
		double d = 0.5625;
		Random random = level.random;

		for (Direction direction : Direction.values()) {
			BlockPos blockPos2 = blockPos.relative(direction);
			if (!level.getBlockState(blockPos2).isSolidRender(level, blockPos2)) {
				Direction.Axis axis = direction.getAxis();
				double e = axis == Direction.Axis.X ? 0.5 + 0.5625 * (double)direction.getStepX() : (double)random.nextFloat();
				double f = axis == Direction.Axis.Y ? 0.5 + 0.5625 * (double)direction.getStepY() : (double)random.nextFloat();
				double g = axis == Direction.Axis.Z ? 0.5 + 0.5625 * (double)direction.getStepZ() : (double)random.nextFloat();
				level.addParticle(DustParticleOptions.REDSTONE, (double)blockPos.getX() + e, (double)blockPos.getY() + f, (double)blockPos.getZ() + g, 0.0, 0.0, 0.0);
			}
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LIT);
	}
}
