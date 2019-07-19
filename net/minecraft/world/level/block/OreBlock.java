/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class OreBlock
extends Block {
    public OreBlock(Block.Properties properties) {
        super(properties);
    }

    protected int xpOnDrop(Random random) {
        if (this == Blocks.COAL_ORE) {
            return Mth.nextInt(random, 0, 2);
        }
        if (this == Blocks.DIAMOND_ORE) {
            return Mth.nextInt(random, 3, 7);
        }
        if (this == Blocks.EMERALD_ORE) {
            return Mth.nextInt(random, 3, 7);
        }
        if (this == Blocks.LAPIS_ORE) {
            return Mth.nextInt(random, 2, 5);
        }
        if (this == Blocks.NETHER_QUARTZ_ORE) {
            return Mth.nextInt(random, 2, 5);
        }
        return 0;
    }

    @Override
    public void spawnAfterBreak(BlockState blockState, Level level, BlockPos blockPos, ItemStack itemStack) {
        int i;
        super.spawnAfterBreak(blockState, level, blockPos, itemStack);
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, itemStack) == 0 && (i = this.xpOnDrop(level.random)) > 0) {
            this.popExperience(level, blockPos, i);
        }
    }
}

