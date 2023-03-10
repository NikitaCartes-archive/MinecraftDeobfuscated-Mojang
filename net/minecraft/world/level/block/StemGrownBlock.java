/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public abstract class StemGrownBlock
extends Block {
    public StemGrownBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public abstract StemBlock getStem();

    public abstract AttachedStemBlock getAttachedStem();
}

