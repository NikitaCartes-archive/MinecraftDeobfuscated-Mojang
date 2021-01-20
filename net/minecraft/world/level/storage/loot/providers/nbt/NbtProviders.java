/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.providers.nbt;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.GsonAdapterFactory;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.nbt.LootNbtProviderType;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProvider;
import net.minecraft.world.level.storage.loot.providers.nbt.StorageNbtProvider;

public class NbtProviders {
    public static final LootNbtProviderType STORAGE = NbtProviders.register("storage", new StorageNbtProvider.Serializer());
    public static final LootNbtProviderType CONTEXT = NbtProviders.register("context", new ContextNbtProvider.Serializer());

    private static LootNbtProviderType register(String string, Serializer<? extends NbtProvider> serializer) {
        return Registry.register(Registry.LOOT_NBT_PROVIDER_TYPE, new ResourceLocation(string), new LootNbtProviderType(serializer));
    }

    public static Object createGsonAdapter() {
        return GsonAdapterFactory.builder(Registry.LOOT_NBT_PROVIDER_TYPE, "provider", "type", NbtProvider::getType).withInlineSerializer(CONTEXT, new ContextNbtProvider.InlineSerializer()).build();
    }
}

