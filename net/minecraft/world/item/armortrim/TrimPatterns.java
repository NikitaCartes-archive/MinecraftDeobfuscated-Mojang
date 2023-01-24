/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.armortrim;

import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.TrimPattern;

public class TrimPatterns {
    public static final ResourceKey<TrimPattern> SENTRY = TrimPatterns.registryKey("sentry");
    public static final ResourceKey<TrimPattern> DUNE = TrimPatterns.registryKey("dune");
    public static final ResourceKey<TrimPattern> COAST = TrimPatterns.registryKey("coast");
    public static final ResourceKey<TrimPattern> WILD = TrimPatterns.registryKey("wild");
    public static final ResourceKey<TrimPattern> WARD = TrimPatterns.registryKey("ward");
    public static final ResourceKey<TrimPattern> EYE = TrimPatterns.registryKey("eye");
    public static final ResourceKey<TrimPattern> VEX = TrimPatterns.registryKey("vex");
    public static final ResourceKey<TrimPattern> TIDE = TrimPatterns.registryKey("tide");
    public static final ResourceKey<TrimPattern> SNOUT = TrimPatterns.registryKey("snout");
    public static final ResourceKey<TrimPattern> RIB = TrimPatterns.registryKey("rib");
    public static final ResourceKey<TrimPattern> SPIRE = TrimPatterns.registryKey("spire");

    public static void bootstrap(BootstapContext<TrimPattern> bootstapContext) {
    }

    public static void nextUpdate(BootstapContext<TrimPattern> bootstapContext) {
        TrimPatterns.register(bootstapContext, Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, SENTRY);
        TrimPatterns.register(bootstapContext, Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE, DUNE);
        TrimPatterns.register(bootstapContext, Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, COAST);
        TrimPatterns.register(bootstapContext, Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE, WILD);
        TrimPatterns.register(bootstapContext, Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE, WARD);
        TrimPatterns.register(bootstapContext, Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE, EYE);
        TrimPatterns.register(bootstapContext, Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, VEX);
        TrimPatterns.register(bootstapContext, Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE, TIDE);
        TrimPatterns.register(bootstapContext, Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, SNOUT);
        TrimPatterns.register(bootstapContext, Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, RIB);
        TrimPatterns.register(bootstapContext, Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, SPIRE);
    }

    public static Optional<Holder.Reference<TrimPattern>> getFromTemplate(RegistryAccess registryAccess, ItemStack itemStack) {
        return registryAccess.registryOrThrow(Registries.TRIM_PATTERN).holders().filter(reference -> itemStack.is(((TrimPattern)reference.value()).templateItem())).findFirst();
    }

    private static void register(BootstapContext<TrimPattern> bootstapContext, Item item, ResourceKey<TrimPattern> resourceKey) {
        TrimPattern trimPattern = new TrimPattern(resourceKey.location(), BuiltInRegistries.ITEM.wrapAsHolder(item), Component.translatable(Util.makeDescriptionId("trim_pattern", resourceKey.location())));
        bootstapContext.register(resourceKey, trimPattern);
    }

    private static ResourceKey<TrimPattern> registryKey(String string) {
        return ResourceKey.create(Registries.TRIM_PATTERN, new ResourceLocation(string));
    }
}

