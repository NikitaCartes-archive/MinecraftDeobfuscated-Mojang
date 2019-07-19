/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootItemBlockStatePropertyCondition
implements LootItemCondition {
    private final Block block;
    private final Map<Property<?>, Object> properties;
    private final Predicate<BlockState> composedPredicate;

    private LootItemBlockStatePropertyCondition(Block block, Map<Property<?>, Object> map) {
        this.block = block;
        this.properties = ImmutableMap.copyOf(map);
        this.composedPredicate = LootItemBlockStatePropertyCondition.bakePredicate(block, map);
    }

    private static Predicate<BlockState> bakePredicate(Block block, Map<Property<?>, Object> map) {
        int i = map.size();
        if (i == 0) {
            return blockState -> blockState.getBlock() == block;
        }
        if (i == 1) {
            Map.Entry<Property<?>, Object> entry = map.entrySet().iterator().next();
            Property<?> property = entry.getKey();
            Object object = entry.getValue();
            return blockState -> blockState.getBlock() == block && object.equals(blockState.getValue(property));
        }
        Predicate<BlockState> predicate = blockState -> blockState.getBlock() == block;
        for (Map.Entry<Property<?>, Object> entry2 : map.entrySet()) {
            Property<?> property2 = entry2.getKey();
            Object object2 = entry2.getValue();
            predicate = predicate.and(blockState -> object2.equals(blockState.getValue(property2)));
        }
        return predicate;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.BLOCK_STATE);
    }

    @Override
    public boolean test(LootContext lootContext) {
        BlockState blockState = lootContext.getParamOrNull(LootContextParams.BLOCK_STATE);
        return blockState != null && this.composedPredicate.test(blockState);
    }

    public static Builder hasBlockStateProperties(Block block) {
        return new Builder(block);
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((LootContext)object);
    }

    public static class Serializer
    extends LootItemCondition.Serializer<LootItemBlockStatePropertyCondition> {
        private static <T extends Comparable<T>> String valueToString(Property<T> property, Object object) {
            return property.getName((Comparable)object);
        }

        protected Serializer() {
            super(new ResourceLocation("block_state_property"), LootItemBlockStatePropertyCondition.class);
        }

        @Override
        public void serialize(JsonObject jsonObject, LootItemBlockStatePropertyCondition lootItemBlockStatePropertyCondition, JsonSerializationContext jsonSerializationContext) {
            jsonObject.addProperty("block", Registry.BLOCK.getKey(lootItemBlockStatePropertyCondition.block).toString());
            JsonObject jsonObject2 = new JsonObject();
            lootItemBlockStatePropertyCondition.properties.forEach((property, object) -> jsonObject2.addProperty(property.getName(), Serializer.valueToString(property, object)));
            jsonObject.add("properties", jsonObject2);
        }

        @Override
        public LootItemBlockStatePropertyCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "block"));
            Block block = (Block)Registry.BLOCK.getOptional(resourceLocation).orElseThrow(() -> new IllegalArgumentException("Can't find block " + resourceLocation));
            StateDefinition<Block, BlockState> stateDefinition = block.getStateDefinition();
            HashMap map = Maps.newHashMap();
            if (jsonObject.has("properties")) {
                JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "properties");
                jsonObject2.entrySet().forEach(entry -> {
                    String string = (String)entry.getKey();
                    Property<?> property = stateDefinition.getProperty(string);
                    if (property == null) {
                        throw new IllegalArgumentException("Block " + Registry.BLOCK.getKey(block) + " does not have property '" + string + "'");
                    }
                    String string2 = GsonHelper.convertToString((JsonElement)entry.getValue(), "value");
                    Object object = property.getValue(string2).orElseThrow(() -> new IllegalArgumentException("Block " + Registry.BLOCK.getKey(block) + " property '" + string + "' does not have value '" + string2 + "'"));
                    map.put(property, object);
                });
            }
            return new LootItemBlockStatePropertyCondition(block, map);
        }

        @Override
        public /* synthetic */ LootItemCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return this.deserialize(jsonObject, jsonDeserializationContext);
        }
    }

    public static class Builder
    implements LootItemCondition.Builder {
        private final Block block;
        private final Set<Property<?>> allowedProperties;
        private final Map<Property<?>, Object> properties = Maps.newHashMap();

        public Builder(Block block) {
            this.block = block;
            this.allowedProperties = Sets.newIdentityHashSet();
            this.allowedProperties.addAll(block.getStateDefinition().getProperties());
        }

        public <T extends Comparable<T>> Builder withProperty(Property<T> property, T comparable) {
            if (!this.allowedProperties.contains(property)) {
                throw new IllegalArgumentException("Block " + Registry.BLOCK.getKey(this.block) + " does not have property '" + property + "'");
            }
            if (!property.getPossibleValues().contains(comparable)) {
                throw new IllegalArgumentException("Block " + Registry.BLOCK.getKey(this.block) + " property '" + property + "' does not have value '" + comparable + "'");
            }
            this.properties.put(property, comparable);
            return this;
        }

        @Override
        public LootItemCondition build() {
            return new LootItemBlockStatePropertyCondition(this.block, this.properties);
        }
    }
}

