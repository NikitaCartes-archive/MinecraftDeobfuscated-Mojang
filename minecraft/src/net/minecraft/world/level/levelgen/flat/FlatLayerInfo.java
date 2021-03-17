package net.minecraft.world.level.levelgen.flat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;

public class FlatLayerInfo {
	public static final Codec<FlatLayerInfo> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.intRange(0, DimensionType.Y_SIZE).fieldOf("height").forGetter(FlatLayerInfo::getHeight),
					Registry.BLOCK.fieldOf("block").orElse(Blocks.AIR).forGetter(flatLayerInfo -> flatLayerInfo.getBlockState().getBlock())
				)
				.apply(instance, FlatLayerInfo::new)
	);
	private final Block block;
	private final int height;

	public FlatLayerInfo(int i, Block block) {
		this.height = i;
		this.block = block;
	}

	public int getHeight() {
		return this.height;
	}

	public BlockState getBlockState() {
		return this.block.defaultBlockState();
	}

	public String toString() {
		return (this.height != 1 ? this.height + "*" : "") + Registry.BLOCK.getKey(this.block);
	}
}
