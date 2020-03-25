package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class OreBlock extends Block {
	public OreBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	protected int xpOnDrop(Random random) {
		if (this == Blocks.COAL_ORE) {
			return Mth.nextInt(random, 0, 2);
		} else if (this == Blocks.DIAMOND_ORE) {
			return Mth.nextInt(random, 3, 7);
		} else if (this == Blocks.EMERALD_ORE) {
			return Mth.nextInt(random, 3, 7);
		} else if (this == Blocks.LAPIS_ORE) {
			return Mth.nextInt(random, 2, 5);
		} else if (this == Blocks.NETHER_QUARTZ_ORE) {
			return Mth.nextInt(random, 2, 5);
		} else {
			return this == Blocks.NETHER_GOLD_ORE ? Mth.nextInt(random, 0, 1) : 0;
		}
	}

	@Override
	public void spawnAfterBreak(BlockState blockState, Level level, BlockPos blockPos, ItemStack itemStack) {
		super.spawnAfterBreak(blockState, level, blockPos, itemStack);
		if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, itemStack) == 0) {
			int i = this.xpOnDrop(level.random);
			if (i > 0) {
				this.popExperience(level, blockPos, i);
			}
		}
	}
}
