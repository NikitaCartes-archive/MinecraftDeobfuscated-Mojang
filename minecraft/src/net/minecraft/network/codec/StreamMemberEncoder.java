package net.minecraft.network.codec;

@FunctionalInterface
public interface StreamMemberEncoder<O, T> {
	void encode(T object, O object2);
}
