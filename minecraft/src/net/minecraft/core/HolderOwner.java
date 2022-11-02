package net.minecraft.core;

public interface HolderOwner<T> {
	default boolean canSerializeIn(HolderOwner<T> holderOwner) {
		return holderOwner == this;
	}
}
