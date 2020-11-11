/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.providers.number;

import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Random;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public final class BinomialDistributionGenerator
implements NumberProvider {
    private final NumberProvider n;
    private final NumberProvider p;

    private BinomialDistributionGenerator(NumberProvider numberProvider, NumberProvider numberProvider2) {
        this.n = numberProvider;
        this.p = numberProvider2;
    }

    @Override
    public LootNumberProviderType getType() {
        return NumberProviders.BINOMIAL;
    }

    @Override
    public int getInt(LootContext lootContext) {
        int i = this.n.getInt(lootContext);
        float f = this.p.getFloat(lootContext);
        Random random = lootContext.getRandom();
        int j = 0;
        for (int k = 0; k < i; ++k) {
            if (!(random.nextFloat() < f)) continue;
            ++j;
        }
        return j;
    }

    @Override
    public float getFloat(LootContext lootContext) {
        return this.getInt(lootContext);
    }

    public static BinomialDistributionGenerator binomial(int i, float f) {
        return new BinomialDistributionGenerator(ConstantValue.exactly(i), ConstantValue.exactly(f));
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Sets.union(this.n.getReferencedContextParams(), this.p.getReferencedContextParams());
    }

    public static class Serializer
    implements net.minecraft.world.level.storage.loot.Serializer<BinomialDistributionGenerator> {
        @Override
        public BinomialDistributionGenerator deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            NumberProvider numberProvider = GsonHelper.getAsObject(jsonObject, "n", jsonDeserializationContext, NumberProvider.class);
            NumberProvider numberProvider2 = GsonHelper.getAsObject(jsonObject, "p", jsonDeserializationContext, NumberProvider.class);
            return new BinomialDistributionGenerator(numberProvider, numberProvider2);
        }

        @Override
        public void serialize(JsonObject jsonObject, BinomialDistributionGenerator binomialDistributionGenerator, JsonSerializationContext jsonSerializationContext) {
            jsonObject.add("n", jsonSerializationContext.serialize(binomialDistributionGenerator.n));
            jsonObject.add("p", jsonSerializationContext.serialize(binomialDistributionGenerator.p));
        }

        @Override
        public /* synthetic */ Object deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return this.deserialize(jsonObject, jsonDeserializationContext);
        }
    }
}

