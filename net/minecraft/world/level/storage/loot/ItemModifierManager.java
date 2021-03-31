/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.Deserializers;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.PredicateManager;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class ItemModifierManager
extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = Deserializers.createFunctionSerializer().create();
    private final PredicateManager predicateManager;
    private final LootTables lootTables;
    private Map<ResourceLocation, LootItemFunction> functions = ImmutableMap.of();

    public ItemModifierManager(PredicateManager predicateManager, LootTables lootTables) {
        super(GSON, "item_modifiers");
        this.predicateManager = predicateManager;
        this.lootTables = lootTables;
    }

    @Nullable
    public LootItemFunction get(ResourceLocation resourceLocation) {
        return this.functions.get(resourceLocation);
    }

    public LootItemFunction get(ResourceLocation resourceLocation, LootItemFunction lootItemFunction) {
        return this.functions.getOrDefault(resourceLocation, lootItemFunction);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        ImmutableMap.Builder builder = ImmutableMap.builder();
        map.forEach((resourceLocation, jsonElement) -> {
            try {
                if (jsonElement.isJsonArray()) {
                    LootItemFunction[] lootItemFunctions = GSON.fromJson((JsonElement)jsonElement, LootItemFunction[].class);
                    builder.put(resourceLocation, new FunctionSequence(lootItemFunctions));
                } else {
                    LootItemFunction lootItemFunction = GSON.fromJson((JsonElement)jsonElement, LootItemFunction.class);
                    builder.put(resourceLocation, lootItemFunction);
                }
            } catch (Exception exception) {
                LOGGER.error("Couldn't parse item modifier {}", resourceLocation, (Object)exception);
            }
        });
        ImmutableMap<ResourceLocation, LootItemFunction> map2 = builder.build();
        ValidationContext validationContext = new ValidationContext(LootContextParamSets.ALL_PARAMS, this.predicateManager::get, this.lootTables::get);
        map2.forEach((resourceLocation, lootItemFunction) -> lootItemFunction.validate(validationContext));
        validationContext.getProblems().forEach((string, string2) -> LOGGER.warn("Found item modifier validation problem in {}: {}", string, string2));
        this.functions = map2;
    }

    public Set<ResourceLocation> getKeys() {
        return Collections.unmodifiableSet(this.functions.keySet());
    }

    static class FunctionSequence
    implements LootItemFunction {
        protected final LootItemFunction[] functions;
        private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;

        public FunctionSequence(LootItemFunction[] lootItemFunctions) {
            this.functions = lootItemFunctions;
            this.compositeFunction = LootItemFunctions.compose(lootItemFunctions);
        }

        @Override
        public ItemStack apply(ItemStack itemStack, LootContext lootContext) {
            return this.compositeFunction.apply(itemStack, lootContext);
        }

        @Override
        public LootItemFunctionType getType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public /* synthetic */ Object apply(Object object, Object object2) {
            return this.apply((ItemStack)object, (LootContext)object2);
        }
    }
}

