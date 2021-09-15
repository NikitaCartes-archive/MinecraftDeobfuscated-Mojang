/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.IdMap;
import org.jetbrains.annotations.Nullable;

public class IdMapper<T>
implements IdMap<T> {
    private int nextId;
    private final IdentityHashMap<T, Integer> tToId;
    private final List<T> idToT;

    public IdMapper() {
        this(512);
    }

    public IdMapper(int i) {
        this.idToT = Lists.newArrayListWithExpectedSize(i);
        this.tToId = new IdentityHashMap(i);
    }

    public void addMapping(T object, int i) {
        this.tToId.put(object, i);
        while (this.idToT.size() <= i) {
            this.idToT.add(null);
        }
        this.idToT.set(i, object);
        if (this.nextId <= i) {
            this.nextId = i + 1;
        }
    }

    public void add(T object) {
        this.addMapping(object, this.nextId);
    }

    @Override
    public int getId(T object) {
        Integer integer = this.tToId.get(object);
        return integer == null ? -1 : integer;
    }

    @Override
    @Nullable
    public final T byId(int i) {
        if (i >= 0 && i < this.idToT.size()) {
            return this.idToT.get(i);
        }
        return null;
    }

    @Override
    public Iterator<T> iterator() {
        return Iterators.filter(this.idToT.iterator(), Predicates.notNull());
    }

    public boolean contains(int i) {
        return this.byId(i) != null;
    }

    @Override
    public int size() {
        return this.tToId.size();
    }
}

