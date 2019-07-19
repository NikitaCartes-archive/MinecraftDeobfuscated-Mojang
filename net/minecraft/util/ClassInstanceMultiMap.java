/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClassInstanceMultiMap<T>
extends AbstractCollection<T> {
    private final Map<Class<?>, List<T>> byClass = Maps.newHashMap();
    private final Class<T> baseClass;
    private final List<T> allInstances = Lists.newArrayList();

    public ClassInstanceMultiMap(Class<T> class_) {
        this.baseClass = class_;
        this.byClass.put(class_, this.allInstances);
    }

    @Override
    public boolean add(T object) {
        boolean bl = false;
        for (Map.Entry<Class<?>, List<T>> entry : this.byClass.entrySet()) {
            if (!entry.getKey().isInstance(object)) continue;
            bl |= entry.getValue().add(object);
        }
        return bl;
    }

    @Override
    public boolean remove(Object object) {
        boolean bl = false;
        for (Map.Entry<Class<?>, List<T>> entry : this.byClass.entrySet()) {
            if (!entry.getKey().isInstance(object)) continue;
            List<T> list = entry.getValue();
            bl |= list.remove(object);
        }
        return bl;
    }

    @Override
    public boolean contains(Object object) {
        return this.find(object.getClass()).contains(object);
    }

    public <S> Collection<S> find(Class<S> class_2) {
        if (!this.baseClass.isAssignableFrom(class_2)) {
            throw new IllegalArgumentException("Don't know how to search for " + class_2);
        }
        List list = this.byClass.computeIfAbsent(class_2, class_ -> this.allInstances.stream().filter(class_::isInstance).collect(Collectors.toList()));
        return Collections.unmodifiableCollection(list);
    }

    @Override
    public Iterator<T> iterator() {
        if (this.allInstances.isEmpty()) {
            return Collections.emptyIterator();
        }
        return Iterators.unmodifiableIterator(this.allInstances.iterator());
    }

    @Override
    public int size() {
        return this.allInstances.size();
    }
}

