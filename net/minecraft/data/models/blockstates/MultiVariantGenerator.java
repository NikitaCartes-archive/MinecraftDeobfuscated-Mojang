/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.models.blockstates;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.data.models.blockstates.BlockStateGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Selector;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;

public class MultiVariantGenerator
implements BlockStateGenerator {
    private final Block block;
    private final List<Variant> baseVariants;
    private final Set<Property<?>> seenProperties = Sets.newHashSet();
    private final List<PropertyDispatch> declaredPropertySets = Lists.newArrayList();

    private MultiVariantGenerator(Block block, List<Variant> list) {
        this.block = block;
        this.baseVariants = list;
    }

    public MultiVariantGenerator with(PropertyDispatch propertyDispatch) {
        propertyDispatch.getDefinedProperties().forEach(property -> {
            if (this.block.getStateDefinition().getProperty(property.getName()) != property) {
                throw new IllegalStateException("Property " + property + " is not defined for block " + this.block);
            }
            if (!this.seenProperties.add((Property<?>)property)) {
                throw new IllegalStateException("Values of property " + property + " already defined for block " + this.block);
            }
        });
        this.declaredPropertySets.add(propertyDispatch);
        return this;
    }

    @Override
    public JsonElement get() {
        Stream<Pair<Selector, List<Variant>>> stream = Stream.of(Pair.of(Selector.empty(), this.baseVariants));
        for (PropertyDispatch propertyDispatch : this.declaredPropertySets) {
            Map<Selector, List<Variant>> map = propertyDispatch.getEntries();
            stream = stream.flatMap(pair -> map.entrySet().stream().map(entry -> {
                Selector selector = ((Selector)pair.getFirst()).extend((Selector)entry.getKey());
                List<Variant> list = MultiVariantGenerator.mergeVariants((List)pair.getSecond(), (List)entry.getValue());
                return Pair.of(selector, list);
            }));
        }
        TreeMap map2 = new TreeMap();
        stream.forEach(pair -> map2.put(((Selector)pair.getFirst()).getKey(), Variant.convertList((List)pair.getSecond())));
        JsonObject jsonObject2 = new JsonObject();
        jsonObject2.add("variants", Util.make(new JsonObject(), jsonObject -> map2.forEach(jsonObject::add)));
        return jsonObject2;
    }

    private static List<Variant> mergeVariants(List<Variant> list, List<Variant> list2) {
        ImmutableList.Builder builder = ImmutableList.builder();
        list.forEach(variant -> list2.forEach(variant2 -> builder.add(Variant.merge(variant, variant2))));
        return builder.build();
    }

    @Override
    public Block getBlock() {
        return this.block;
    }

    public static MultiVariantGenerator multiVariant(Block block) {
        return new MultiVariantGenerator(block, ImmutableList.of(Variant.variant()));
    }

    public static MultiVariantGenerator multiVariant(Block block, Variant variant) {
        return new MultiVariantGenerator(block, ImmutableList.of(variant));
    }

    public static MultiVariantGenerator multiVariant(Block block, Variant ... variants) {
        return new MultiVariantGenerator(block, ImmutableList.copyOf(variants));
    }

    @Override
    public /* synthetic */ Object get() {
        return this.get();
    }
}

