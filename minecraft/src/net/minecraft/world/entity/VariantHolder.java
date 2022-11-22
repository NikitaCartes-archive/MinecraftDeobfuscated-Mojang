package net.minecraft.world.entity;

public interface VariantHolder<T> {
	void setVariant(T object);

	T getVariant();
}
