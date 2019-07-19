/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.StemGrownBlock;

public class MelonBlock
extends StemGrownBlock {
    protected MelonBlock(Block.Properties properties) {
        super(properties);
    }

    @Override
    public StemBlock getStem() {
        return (StemBlock)Blocks.MELON_STEM;
    }

    @Override
    public AttachedStemBlock getAttachedStem() {
        return (AttachedStemBlock)Blocks.ATTACHED_MELON_STEM;
    }
}

