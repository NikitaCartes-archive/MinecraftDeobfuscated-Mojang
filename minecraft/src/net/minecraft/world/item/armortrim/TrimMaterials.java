package net.minecraft.world.item.armortrim;

import java.util.Map;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TrimMaterials {
	public static final ResourceKey<TrimMaterial> QUARTZ = registryKey("quartz");
	public static final ResourceKey<TrimMaterial> IRON = registryKey("iron");
	public static final ResourceKey<TrimMaterial> NETHERITE = registryKey("netherite");
	public static final ResourceKey<TrimMaterial> REDSTONE = registryKey("redstone");
	public static final ResourceKey<TrimMaterial> COPPER = registryKey("copper");
	public static final ResourceKey<TrimMaterial> GOLD = registryKey("gold");
	public static final ResourceKey<TrimMaterial> EMERALD = registryKey("emerald");
	public static final ResourceKey<TrimMaterial> DIAMOND = registryKey("diamond");
	public static final ResourceKey<TrimMaterial> LAPIS = registryKey("lapis");
	public static final ResourceKey<TrimMaterial> AMETHYST = registryKey("amethyst");
	public static final ResourceKey<TrimMaterial> AMBER = registryKey("amber");

	public static void bootstrap(BootstrapContext<TrimMaterial> bootstrapContext) {
		register(bootstrapContext, QUARTZ, Items.QUARTZ, Style.EMPTY.withColor(14931140), 0.01F);
		register(bootstrapContext, IRON, Items.IRON_INGOT, Style.EMPTY.withColor(15527148), 0.02F, Map.of(ArmorMaterials.IRON, "iron_darker"));
		register(bootstrapContext, NETHERITE, Items.NETHERITE_INGOT, Style.EMPTY.withColor(6445145), 0.03F, Map.of(ArmorMaterials.NETHERITE, "netherite_darker"));
		register(bootstrapContext, REDSTONE, Items.REDSTONE, Style.EMPTY.withColor(9901575), 0.04F);
		register(bootstrapContext, COPPER, Items.COPPER_INGOT, Style.EMPTY.withColor(11823181), 0.05F);
		register(bootstrapContext, GOLD, Items.GOLD_INGOT, Style.EMPTY.withColor(14594349), 0.06F, Map.of(ArmorMaterials.GOLD, "gold_darker"));
		register(bootstrapContext, EMERALD, Items.EMERALD, Style.EMPTY.withColor(1155126), 0.07F);
		register(bootstrapContext, DIAMOND, Items.DIAMOND, Style.EMPTY.withColor(7269586), 0.08F, Map.of(ArmorMaterials.DIAMOND, "diamond_darker"));
		register(bootstrapContext, LAPIS, Items.LAPIS_LAZULI, Style.EMPTY.withColor(4288151), 0.09F);
		register(bootstrapContext, AMETHYST, Items.AMETHYST_SHARD, Style.EMPTY.withColor(10116294), 0.1F);
		register(bootstrapContext, AMBER, Items.AMBER_GEM, Style.EMPTY.withColor(15042820), 1.0F);
	}

	public static Optional<Holder.Reference<TrimMaterial>> getFromIngredient(HolderLookup.Provider provider, ItemStack itemStack) {
		return provider.lookupOrThrow(Registries.TRIM_MATERIAL)
			.listElements()
			.filter(reference -> itemStack.is(((TrimMaterial)reference.value()).ingredient()))
			.findFirst();
	}

	private static void register(BootstrapContext<TrimMaterial> bootstrapContext, ResourceKey<TrimMaterial> resourceKey, Item item, Style style, float f) {
		register(bootstrapContext, resourceKey, item, style, f, Map.of());
	}

	private static void register(
		BootstrapContext<TrimMaterial> bootstrapContext,
		ResourceKey<TrimMaterial> resourceKey,
		Item item,
		Style style,
		float f,
		Map<Holder<ArmorMaterial>, String> map
	) {
		TrimMaterial trimMaterial = TrimMaterial.create(
			resourceKey.location().getPath(), item, f, Component.translatable(Util.makeDescriptionId("trim_material", resourceKey.location())).withStyle(style), map
		);
		bootstrapContext.register(resourceKey, trimMaterial);
	}

	private static ResourceKey<TrimMaterial> registryKey(String string) {
		return ResourceKey.create(Registries.TRIM_MATERIAL, new ResourceLocation(string));
	}
}
