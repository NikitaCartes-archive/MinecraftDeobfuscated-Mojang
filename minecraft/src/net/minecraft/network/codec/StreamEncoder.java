package net.minecraft.network.codec;

@FunctionalInterface
public interface StreamEncoder<O, T> {
	void encode(O object, T object2);
}
