/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.Nullable;

public class LootContext {
    private final Random random;
    private final float luck;
    private final ServerLevel level;
    private final Function<ResourceLocation, LootTable> lootTables;
    private final Set<LootTable> visitedTables = Sets.newLinkedHashSet();
    private final Function<ResourceLocation, LootItemCondition> conditions;
    private final Set<LootItemCondition> visitedConditions = Sets.newLinkedHashSet();
    private final Map<LootContextParam<?>, Object> params;
    private final Map<ResourceLocation, DynamicDrop> dynamicDrops;

    private LootContext(Random random, float f, ServerLevel serverLevel, Function<ResourceLocation, LootTable> function, Function<ResourceLocation, LootItemCondition> function2, Map<LootContextParam<?>, Object> map, Map<ResourceLocation, DynamicDrop> map2) {
        this.random = random;
        this.luck = f;
        this.level = serverLevel;
        this.lootTables = function;
        this.conditions = function2;
        this.params = ImmutableMap.copyOf(map);
        this.dynamicDrops = ImmutableMap.copyOf(map2);
    }

    public boolean hasParam(LootContextParam<?> lootContextParam) {
        return this.params.containsKey(lootContextParam);
    }

    public <T> T getParam(LootContextParam<T> lootContextParam) {
        Object object = this.params.get(lootContextParam);
        if (object == null) {
            throw new NoSuchElementException(lootContextParam.getName().toString());
        }
        return (T)object;
    }

    public void addDynamicDrops(ResourceLocation resourceLocation, Consumer<ItemStack> consumer) {
        DynamicDrop dynamicDrop = this.dynamicDrops.get(resourceLocation);
        if (dynamicDrop != null) {
            dynamicDrop.add(this, consumer);
        }
    }

    @Nullable
    public <T> T getParamOrNull(LootContextParam<T> lootContextParam) {
        return (T)this.params.get(lootContextParam);
    }

    public boolean addVisitedTable(LootTable lootTable) {
        return this.visitedTables.add(lootTable);
    }

    public void removeVisitedTable(LootTable lootTable) {
        this.visitedTables.remove(lootTable);
    }

    public boolean addVisitedCondition(LootItemCondition lootItemCondition) {
        return this.visitedConditions.add(lootItemCondition);
    }

    public void removeVisitedCondition(LootItemCondition lootItemCondition) {
        this.visitedConditions.remove(lootItemCondition);
    }

    public LootTable getLootTable(ResourceLocation resourceLocation) {
        return this.lootTables.apply(resourceLocation);
    }

    public LootItemCondition getCondition(ResourceLocation resourceLocation) {
        return this.conditions.apply(resourceLocation);
    }

    public Random getRandom() {
        return this.random;
    }

    public float getLuck() {
        return this.luck;
    }

    public ServerLevel getLevel() {
        return this.level;
    }

    public static enum EntityTarget {
        THIS("this", LootContextParams.THIS_ENTITY),
        KILLER("killer", LootContextParams.KILLER_ENTITY),
        DIRECT_KILLER("direct_killer", LootContextParams.DIRECT_KILLER_ENTITY),
        KILLER_PLAYER("killer_player", LootContextParams.LAST_DAMAGE_PLAYER);

        private final String name;
        private final LootContextParam<? extends Entity> param;

        private EntityTarget(String string2, LootContextParam<? extends Entity> lootContextParam) {
            this.name = string2;
            this.param = lootContextParam;
        }

        public LootContextParam<? extends Entity> getParam() {
            return this.param;
        }

        public static EntityTarget getByName(String string) {
            for (EntityTarget entityTarget : EntityTarget.values()) {
                if (!entityTarget.name.equals(string)) continue;
                return entityTarget;
            }
            throw new IllegalArgumentException("Invalid entity target " + string);
        }

        public static class Serializer
        extends TypeAdapter<EntityTarget> {
            @Override
            public void write(JsonWriter jsonWriter, EntityTarget entityTarget) throws IOException {
                jsonWriter.value(entityTarget.name);
            }

            @Override
            public EntityTarget read(JsonReader jsonReader) throws IOException {
                return EntityTarget.getByName(jsonReader.nextString());
            }

            @Override
            public /* synthetic */ Object read(JsonReader jsonReader) throws IOException {
                return this.read(jsonReader);
            }

            @Override
            public /* synthetic */ void write(JsonWriter jsonWriter, Object object) throws IOException {
                this.write(jsonWriter, (EntityTarget)((Object)object));
            }
        }
    }

    public static class Builder {
        private final ServerLevel level;
        private final Map<LootContextParam<?>, Object> params = Maps.newIdentityHashMap();
        private final Map<ResourceLocation, DynamicDrop> dynamicDrops = Maps.newHashMap();
        private Random random;
        private float luck;

        public Builder(ServerLevel serverLevel) {
            this.level = serverLevel;
        }

        public Builder withRandom(Random random) {
            this.random = random;
            return this;
        }

        public Builder withOptionalRandomSeed(long l) {
            if (l != 0L) {
                this.random = new Random(l);
            }
            return this;
        }

        public Builder withOptionalRandomSeed(long l, Random random) {
            this.random = l == 0L ? random : new Random(l);
            return this;
        }

        public Builder withLuck(float f) {
            this.luck = f;
            return this;
        }

        public <T> Builder withParameter(LootContextParam<T> lootContextParam, T object) {
            this.params.put(lootContextParam, object);
            return this;
        }

        public <T> Builder withOptionalParameter(LootContextParam<T> lootContextParam, @Nullable T object) {
            if (object == null) {
                this.params.remove(lootContextParam);
            } else {
                this.params.put(lootContextParam, object);
            }
            return this;
        }

        public Builder withDynamicDrop(ResourceLocation resourceLocation, DynamicDrop dynamicDrop) {
            DynamicDrop dynamicDrop2 = this.dynamicDrops.put(resourceLocation, dynamicDrop);
            if (dynamicDrop2 != null) {
                throw new IllegalStateException("Duplicated dynamic drop '" + this.dynamicDrops + "'");
            }
            return this;
        }

        public ServerLevel getLevel() {
            return this.level;
        }

        public <T> T getParameter(LootContextParam<T> lootContextParam) {
            Object object = this.params.get(lootContextParam);
            if (object == null) {
                throw new IllegalArgumentException("No parameter " + lootContextParam);
            }
            return (T)object;
        }

        @Nullable
        public <T> T getOptionalParameter(LootContextParam<T> lootContextParam) {
            return (T)this.params.get(lootContextParam);
        }

        public LootContext create(LootContextParamSet lootContextParamSet) {
            Sets.SetView<LootContextParam<?>> set = Sets.difference(this.params.keySet(), lootContextParamSet.getAllowed());
            if (!set.isEmpty()) {
                throw new IllegalArgumentException("Parameters not allowed in this parameter set: " + set);
            }
            Sets.SetView<LootContextParam<?>> set2 = Sets.difference(lootContextParamSet.getRequired(), this.params.keySet());
            if (!set2.isEmpty()) {
                throw new IllegalArgumentException("Missing required parameters: " + set2);
            }
            Random random = this.random;
            if (random == null) {
                random = new Random();
            }
            MinecraftServer minecraftServer = this.level.getServer();
            return new LootContext(random, this.luck, this.level, minecraftServer.getLootTables()::get, minecraftServer.getPredicateManager()::get, this.params, this.dynamicDrops);
        }
    }

    @FunctionalInterface
    public static interface DynamicDrop {
        public void add(LootContext var1, Consumer<ItemStack> var2);
    }
}

