/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.armortrim;

import java.util.Map;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.TrimMaterial;

public class TrimMaterials {
    public static final ResourceKey<TrimMaterial> QUARTZ = TrimMaterials.registryKey("quartz");
    public static final ResourceKey<TrimMaterial> IRON = TrimMaterials.registryKey("iron");
    public static final ResourceKey<TrimMaterial> NETHERITE = TrimMaterials.registryKey("netherite");
    public static final ResourceKey<TrimMaterial> REDSTONE = TrimMaterials.registryKey("redstone");
    public static final ResourceKey<TrimMaterial> COPPER = TrimMaterials.registryKey("copper");
    public static final ResourceKey<TrimMaterial> GOLD = TrimMaterials.registryKey("gold");
    public static final ResourceKey<TrimMaterial> EMERALD = TrimMaterials.registryKey("emerald");
    public static final ResourceKey<TrimMaterial> DIAMOND = TrimMaterials.registryKey("diamond");
    public static final ResourceKey<TrimMaterial> LAPIS = TrimMaterials.registryKey("lapis");
    public static final ResourceKey<TrimMaterial> AMETHYST = TrimMaterials.registryKey("amethyst");

    public static void bootstrap(BootstapContext<TrimMaterial> bootstapContext) {
    }

    public static void nextUpdate(BootstapContext<TrimMaterial> bootstapContext) {
        TrimMaterials.register(bootstapContext, QUARTZ, Items.QUARTZ, Style.EMPTY.withColor(14931140), 0.1f);
        TrimMaterials.register(bootstapContext, IRON, Items.IRON_INGOT, Style.EMPTY.withColor(0xECECEC), 0.2f, Map.of(ArmorMaterials.IRON, "iron_darker"));
        TrimMaterials.register(bootstapContext, NETHERITE, Items.NETHERITE_INGOT, Style.EMPTY.withColor(6445145), 0.3f, Map.of(ArmorMaterials.NETHERITE, "netherite_darker"));
        TrimMaterials.register(bootstapContext, REDSTONE, Items.REDSTONE, Style.EMPTY.withColor(9901575), 0.4f);
        TrimMaterials.register(bootstapContext, COPPER, Items.COPPER_INGOT, Style.EMPTY.withColor(11823181), 0.5f);
        TrimMaterials.register(bootstapContext, GOLD, Items.GOLD_INGOT, Style.EMPTY.withColor(14594349), 0.6f, Map.of(ArmorMaterials.GOLD, "gold_darker"));
        TrimMaterials.register(bootstapContext, EMERALD, Items.EMERALD, Style.EMPTY.withColor(1155126), 0.7f);
        TrimMaterials.register(bootstapContext, DIAMOND, Items.DIAMOND, Style.EMPTY.withColor(7269586), 0.8f, Map.of(ArmorMaterials.DIAMOND, "diamond_darker"));
        TrimMaterials.register(bootstapContext, LAPIS, Items.LAPIS_LAZULI, Style.EMPTY.withColor(4288151), 0.9f);
        TrimMaterials.register(bootstapContext, AMETHYST, Items.AMETHYST_SHARD, Style.EMPTY.withColor(10116294), 1.0f);
    }

    public static Optional<Holder.Reference<TrimMaterial>> getFromIngredient(RegistryAccess registryAccess, ItemStack itemStack) {
        return registryAccess.registryOrThrow(Registries.TRIM_MATERIAL).holders().filter(reference -> itemStack.is(((TrimMaterial)reference.value()).ingredient())).findFirst();
    }

    private static void register(BootstapContext<TrimMaterial> bootstapContext, ResourceKey<TrimMaterial> resourceKey, Item item, Style style, float f) {
        TrimMaterials.register(bootstapContext, resourceKey, item, style, f, Map.of());
    }

    private static void register(BootstapContext<TrimMaterial> bootstapContext, ResourceKey<TrimMaterial> resourceKey, Item item, Style style, float f, Map<ArmorMaterials, String> map) {
        TrimMaterial trimMaterial = TrimMaterial.create(resourceKey.location().getPath(), item, f, Component.translatable(Util.makeDescriptionId("trim_material", resourceKey.location())).withStyle(style), map);
        bootstapContext.register(resourceKey, trimMaterial);
    }

    private static ResourceKey<TrimMaterial> registryKey(String string) {
        return ResourceKey.create(Registries.TRIM_MATERIAL, new ResourceLocation(string));
    }
}

