package net.minecraft.network.codec;

@FunctionalInterface
public interface StreamDecoder<I, T> {
	T decode(I object);
}
