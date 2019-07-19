package net.minecraft.core;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.lang3.Validate;

public class NonNullList<E> extends AbstractList<E> {
	private final List<E> list;
	private final E defaultValue;

	public static <E> NonNullList<E> create() {
		return new NonNullList<>();
	}

	public static <E> NonNullList<E> withSize(int i, E object) {
		Validate.notNull(object);
		Object[] objects = new Object[i];
		Arrays.fill(objects, object);
		return new NonNullList<>(Arrays.asList(objects), object);
	}

	@SafeVarargs
	public static <E> NonNullList<E> of(E object, E... objects) {
		return new NonNullList<>(Arrays.asList(objects), object);
	}

	protected NonNullList() {
		this(new ArrayList(), null);
	}

	protected NonNullList(List<E> list, @Nullable E object) {
		this.list = list;
		this.defaultValue = object;
	}

	@Nonnull
	public E get(int i) {
		return (E)this.list.get(i);
	}

	public E set(int i, E object) {
		Validate.notNull(object);
		return (E)this.list.set(i, object);
	}

	public void add(int i, E object) {
		Validate.notNull(object);
		this.list.add(i, object);
	}

	public E remove(int i) {
		return (E)this.list.remove(i);
	}

	public int size() {
		return this.list.size();
	}

	public void clear() {
		if (this.defaultValue == null) {
			super.clear();
		} else {
			for (int i = 0; i < this.size(); i++) {
				this.set(i, this.defaultValue);
			}
		}
	}
}
