/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.providers.number;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.GsonAdapterFactory;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.ScoreboardValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public class NumberProviders {
    public static final LootNumberProviderType CONSTANT = NumberProviders.register("constant", new ConstantValue.Serializer());
    public static final LootNumberProviderType UNIFORM = NumberProviders.register("uniform", new UniformGenerator.Serializer());
    public static final LootNumberProviderType BINOMIAL = NumberProviders.register("binomial", new BinomialDistributionGenerator.Serializer());
    public static final LootNumberProviderType SCORE = NumberProviders.register("score", new ScoreboardValue.Serializer());

    private static LootNumberProviderType register(String string, Serializer<? extends NumberProvider> serializer) {
        return Registry.register(Registry.LOOT_NUMBER_PROVIDER_TYPE, new ResourceLocation(string), new LootNumberProviderType(serializer));
    }

    public static Object createGsonAdapter() {
        return GsonAdapterFactory.builder(Registry.LOOT_NUMBER_PROVIDER_TYPE, "provider", "type", NumberProvider::getType).withDefaultSerializer(CONSTANT, new ConstantValue.DefaultSerializer()).build();
    }
}

