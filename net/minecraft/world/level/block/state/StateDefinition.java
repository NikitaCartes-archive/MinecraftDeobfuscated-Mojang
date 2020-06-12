/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.state;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.util.Codecs;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

public class StateDefinition<O, S extends StateHolder<O, S>> {
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-z0-9_]+$");
    private final O owner;
    private final ImmutableSortedMap<String, Property<?>> propertiesByName;
    private final ImmutableList<S> states;

    protected StateDefinition(Function<O, S> function, O object, Factory<O, S> factory, Map<String, Property<?>> map) {
        this.owner = object;
        this.propertiesByName = ImmutableSortedMap.copyOf(map);
        Supplier<StateHolder> supplier = () -> (StateHolder)function.apply(object);
        MapCodec<StateHolder> mapCodec = MapCodec.of(Encoder.empty(), Decoder.unit(supplier));
        for (Map.Entry entry : this.propertiesByName.entrySet()) {
            mapCodec = StateDefinition.appendPropertyCodec(mapCodec, supplier, (String)entry.getKey(), (Property)entry.getValue());
        }
        MapCodec<StateHolder> mapCodec2 = mapCodec;
        LinkedHashMap map2 = Maps.newLinkedHashMap();
        ArrayList<StateHolder> list3 = Lists.newArrayList();
        Stream<List<List<Object>>> stream = Stream.of(Collections.emptyList());
        for (Property property : this.propertiesByName.values()) {
            stream = stream.flatMap(list -> property.getPossibleValues().stream().map(comparable -> {
                ArrayList<Pair<Property, Comparable>> list2 = Lists.newArrayList(list);
                list2.add(Pair.of(property, comparable));
                return list2;
            }));
        }
        stream.forEach(list2 -> {
            ImmutableMap<Property<?>, Comparable<?>> immutableMap = list2.stream().collect(ImmutableMap.toImmutableMap(Pair::getFirst, Pair::getSecond));
            StateHolder stateHolder = (StateHolder)factory.create(object, immutableMap, mapCodec2);
            map2.put(immutableMap, stateHolder);
            list3.add(stateHolder);
        });
        for (StateHolder stateHolder : list3) {
            stateHolder.populateNeighbours(map2);
        }
        this.states = ImmutableList.copyOf(list3);
    }

    private static <S extends StateHolder<?, S>, T extends Comparable<T>> MapCodec<S> appendPropertyCodec(MapCodec<S> mapCodec, Supplier<S> supplier, String string, Property<T> property) {
        return Codec.mapPair(mapCodec, Codecs.setPartial(property.valueCodec().fieldOf(string), () -> property.value((StateHolder)supplier.get()))).xmap(pair -> (StateHolder)((StateHolder)pair.getFirst()).setValue(property, ((Property.Value)pair.getSecond()).value()), stateHolder -> Pair.of(stateHolder, property.value((StateHolder<?, ?>)stateHolder)));
    }

    public ImmutableList<S> getPossibleStates() {
        return this.states;
    }

    public S any() {
        return (S)((StateHolder)this.states.get(0));
    }

    public O getOwner() {
        return this.owner;
    }

    public Collection<Property<?>> getProperties() {
        return this.propertiesByName.values();
    }

    public String toString() {
        return MoreObjects.toStringHelper(this).add("block", this.owner).add("properties", this.propertiesByName.values().stream().map(Property::getName).collect(Collectors.toList())).toString();
    }

    @Nullable
    public Property<?> getProperty(String string) {
        return this.propertiesByName.get(string);
    }

    public static class Builder<O, S extends StateHolder<O, S>> {
        private final O owner;
        private final Map<String, Property<?>> properties = Maps.newHashMap();

        public Builder(O object) {
            this.owner = object;
        }

        public Builder<O, S> add(Property<?> ... propertys) {
            for (Property<?> property : propertys) {
                this.validateProperty(property);
                this.properties.put(property.getName(), property);
            }
            return this;
        }

        private <T extends Comparable<T>> void validateProperty(Property<T> property) {
            String string = property.getName();
            if (!NAME_PATTERN.matcher(string).matches()) {
                throw new IllegalArgumentException(this.owner + " has invalidly named property: " + string);
            }
            Collection<T> collection = property.getPossibleValues();
            if (collection.size() <= 1) {
                throw new IllegalArgumentException(this.owner + " attempted use property " + string + " with <= 1 possible values");
            }
            for (Comparable comparable : collection) {
                String string2 = property.getName(comparable);
                if (NAME_PATTERN.matcher(string2).matches()) continue;
                throw new IllegalArgumentException(this.owner + " has property: " + string + " with invalidly named value: " + string2);
            }
            if (this.properties.containsKey(string)) {
                throw new IllegalArgumentException(this.owner + " has duplicate property: " + string);
            }
        }

        public StateDefinition<O, S> create(Function<O, S> function, Factory<O, S> factory) {
            return new StateDefinition<O, S>(function, this.owner, factory, this.properties);
        }
    }

    public static interface Factory<O, S> {
        public S create(O var1, ImmutableMap<Property<?>, Comparable<?>> var2, MapCodec<S> var3);
    }
}

