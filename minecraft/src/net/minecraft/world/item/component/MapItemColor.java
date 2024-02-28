package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record MapItemColor(int rgb) {
	public static final Codec<MapItemColor> CODEC = Codec.INT.xmap(MapItemColor::new, MapItemColor::rgb);
	public static final StreamCodec<ByteBuf, MapItemColor> STREAM_CODEC = ByteBufCodecs.INT.map(MapItemColor::new, MapItemColor::rgb);
	public static final MapItemColor DEFAULT = new MapItemColor(4603950);
}
