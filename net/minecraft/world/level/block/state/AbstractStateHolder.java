/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.state;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractStateHolder<O, S>
implements StateHolder<S> {
    private static final Function<Map.Entry<Property<?>, Comparable<?>>, String> PROPERTY_ENTRY_TO_STRING_FUNCTION = new Function<Map.Entry<Property<?>, Comparable<?>>, String>(){

        @Override
        public String apply(@Nullable Map.Entry<Property<?>, Comparable<?>> entry) {
            if (entry == null) {
                return "<NULL>";
            }
            Property<?> property = entry.getKey();
            return property.getName() + "=" + this.getName(property, entry.getValue());
        }

        private <T extends Comparable<T>> String getName(Property<T> property, Comparable<?> comparable) {
            return property.getName(comparable);
        }

        @Override
        public /* synthetic */ Object apply(@Nullable Object object) {
            return this.apply((Map.Entry)object);
        }
    };
    protected final O owner;
    private final ImmutableMap<Property<?>, Comparable<?>> values;
    private final int hashCode;
    private Table<Property<?>, Comparable<?>, S> neighbours;

    protected AbstractStateHolder(O object, ImmutableMap<Property<?>, Comparable<?>> immutableMap) {
        this.owner = object;
        this.values = immutableMap;
        this.hashCode = immutableMap.hashCode();
    }

    public <T extends Comparable<T>> S cycle(Property<T> property) {
        return this.setValue(property, (Comparable)AbstractStateHolder.findNextInCollection(property.getPossibleValues(), this.getValue(property)));
    }

    protected static <T> T findNextInCollection(Collection<T> collection, T object) {
        Iterator<T> iterator = collection.iterator();
        while (iterator.hasNext()) {
            if (!iterator.next().equals(object)) continue;
            if (iterator.hasNext()) {
                return iterator.next();
            }
            return collection.iterator().next();
        }
        return iterator.next();
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.owner);
        if (!this.getValues().isEmpty()) {
            stringBuilder.append('[');
            stringBuilder.append(this.getValues().entrySet().stream().map(PROPERTY_ENTRY_TO_STRING_FUNCTION).collect(Collectors.joining(",")));
            stringBuilder.append(']');
        }
        return stringBuilder.toString();
    }

    public Collection<Property<?>> getProperties() {
        return Collections.unmodifiableCollection(this.values.keySet());
    }

    public <T extends Comparable<T>> boolean hasProperty(Property<T> property) {
        return this.values.containsKey(property);
    }

    @Override
    public <T extends Comparable<T>> T getValue(Property<T> property) {
        Comparable<?> comparable = this.values.get(property);
        if (comparable == null) {
            throw new IllegalArgumentException("Cannot get property " + property + " as it does not exist in " + this.owner);
        }
        return (T)((Comparable)property.getValueClass().cast(comparable));
    }

    @Override
    public <T extends Comparable<T>, V extends T> S setValue(Property<T> property, V comparable) {
        Comparable<?> comparable2 = this.values.get(property);
        if (comparable2 == null) {
            throw new IllegalArgumentException("Cannot set property " + property + " as it does not exist in " + this.owner);
        }
        if (comparable2 == comparable) {
            return (S)this;
        }
        S object = this.neighbours.get(property, comparable);
        if (object == null) {
            throw new IllegalArgumentException("Cannot set property " + property + " to " + comparable + " on " + this.owner + ", it is not an allowed value");
        }
        return object;
    }

    public void populateNeighbours(Map<Map<Property<?>, Comparable<?>>, S> map) {
        if (this.neighbours != null) {
            throw new IllegalStateException();
        }
        HashBasedTable<Property, Comparable, S> table = HashBasedTable.create();
        for (Map.Entry entry : this.values.entrySet()) {
            Property property = (Property)entry.getKey();
            for (Comparable comparable : property.getPossibleValues()) {
                if (comparable == entry.getValue()) continue;
                table.put(property, comparable, map.get(this.makeNeighbourValues(property, comparable)));
            }
        }
        this.neighbours = table.isEmpty() ? table : ArrayTable.create(table);
    }

    private Map<Property<?>, Comparable<?>> makeNeighbourValues(Property<?> property, Comparable<?> comparable) {
        HashMap<Property<?>, Comparable<?>> map = Maps.newHashMap(this.values);
        map.put(property, comparable);
        return map;
    }

    @Override
    public ImmutableMap<Property<?>, Comparable<?>> getValues() {
        return this.values;
    }

    public boolean equals(Object object) {
        return this == object;
    }

    public int hashCode() {
        return this.hashCode;
    }
}

