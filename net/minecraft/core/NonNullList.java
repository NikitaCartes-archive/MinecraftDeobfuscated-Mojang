/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core;

import com.google.common.collect.Lists;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NonNullList<E>
extends AbstractList<E> {
    private final List<E> list;
    @Nullable
    private final E defaultValue;

    public static <E> NonNullList<E> create() {
        return new NonNullList<Object>(Lists.newArrayList(), null);
    }

    public static <E> NonNullList<E> createWithCapacity(int i) {
        return new NonNullList<Object>(Lists.newArrayListWithCapacity(i), null);
    }

    public static <E> NonNullList<E> withSize(int i, E object) {
        Validate.notNull(object);
        Object[] objects = new Object[i];
        Arrays.fill(objects, object);
        return new NonNullList<Object>(Arrays.asList(objects), object);
    }

    @SafeVarargs
    public static <E> NonNullList<E> of(E object, E ... objects) {
        return new NonNullList<E>(Arrays.asList(objects), object);
    }

    protected NonNullList(List<E> list, @Nullable E object) {
        this.list = list;
        this.defaultValue = object;
    }

    @Override
    @NotNull
    public E get(int i) {
        return this.list.get(i);
    }

    @Override
    public E set(int i, E object) {
        Validate.notNull(object);
        return this.list.set(i, object);
    }

    @Override
    public void add(int i, E object) {
        Validate.notNull(object);
        this.list.add(i, object);
    }

    @Override
    public E remove(int i) {
        return this.list.remove(i);
    }

    @Override
    public int size() {
        return this.list.size();
    }

    @Override
    public void clear() {
        if (this.defaultValue == null) {
            super.clear();
        } else {
            for (int i = 0; i < this.size(); ++i) {
                this.set(i, this.defaultValue);
            }
        }
    }
}

