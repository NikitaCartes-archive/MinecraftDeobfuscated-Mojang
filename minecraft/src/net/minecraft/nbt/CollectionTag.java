package net.minecraft.nbt;

import java.util.AbstractList;

public abstract class CollectionTag<T extends Tag> extends AbstractList<T> implements Tag {
	public abstract T set(int i, T tag);

	public abstract void add(int i, T tag);

	public abstract T remove(int i);

	public abstract boolean setTag(int i, Tag tag);

	public abstract boolean addTag(int i, Tag tag);

	public abstract byte getElementType();
}
