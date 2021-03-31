package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class OreBlock extends Block {
	private final UniformInt xpRange;

	public OreBlock(BlockBehaviour.Properties properties) {
		this(properties, UniformInt.of(0, 0));
	}

	public OreBlock(BlockBehaviour.Properties properties, UniformInt uniformInt) {
		super(properties);
		this.xpRange = uniformInt;
	}

	@Override
	public void spawnAfterBreak(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack) {
		super.spawnAfterBreak(blockState, serverLevel, blockPos, itemStack);
		if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, itemStack) == 0) {
			int i = this.xpRange.sample(serverLevel.random);
			if (i > 0) {
				this.popExperience(serverLevel, blockPos, i);
			}
		}
	}
}
