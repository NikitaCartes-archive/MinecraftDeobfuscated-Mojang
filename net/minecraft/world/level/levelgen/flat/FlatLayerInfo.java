/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.flat;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class FlatLayerInfo {
    public static final Codec<FlatLayerInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("height")).forGetter(FlatLayerInfo::getHeight), ((MapCodec)Registry.BLOCK.fieldOf("block")).withDefault(Blocks.AIR).forGetter(flatLayerInfo -> flatLayerInfo.getBlockState().getBlock())).apply((Applicative<FlatLayerInfo, ?>)instance, FlatLayerInfo::new));
    private final BlockState blockState;
    private final int height;
    private int start;

    public FlatLayerInfo(int i, Block block) {
        this.height = i;
        this.blockState = block.defaultBlockState();
    }

    public int getHeight() {
        return this.height;
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    public int getStart() {
        return this.start;
    }

    public void setStart(int i) {
        this.start = i;
    }

    public String toString() {
        return (this.height != 1 ? this.height + "*" : "") + Registry.BLOCK.getKey(this.blockState.getBlock());
    }
}

