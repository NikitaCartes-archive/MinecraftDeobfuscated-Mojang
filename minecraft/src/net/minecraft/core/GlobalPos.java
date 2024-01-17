package net.minecraft.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record GlobalPos(ResourceKey<Level> dimension, BlockPos pos) {
	public static final Codec<GlobalPos> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(GlobalPos::dimension), BlockPos.CODEC.fieldOf("pos").forGetter(GlobalPos::pos)
				)
				.apply(instance, GlobalPos::of)
	);
	public static final StreamCodec<ByteBuf, GlobalPos> STREAM_CODEC = StreamCodec.composite(
		ResourceKey.streamCodec(Registries.DIMENSION), GlobalPos::dimension, BlockPos.STREAM_CODEC, GlobalPos::pos, GlobalPos::of
	);

	public static GlobalPos of(ResourceKey<Level> resourceKey, BlockPos blockPos) {
		return new GlobalPos(resourceKey, blockPos);
	}

	public String toString() {
		return this.dimension + " " + this.pos;
	}
}
