package net.minecraft.world.item.armortrim;

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

	public static void bootstrap(BootstapContext<TrimMaterial> bootstapContext) {
	}

	public static void nextUpdate(BootstapContext<TrimMaterial> bootstapContext) {
		register(bootstapContext, QUARTZ, Items.QUARTZ, Style.EMPTY.withColor(14931140), 0.1F, Optional.empty());
		register(bootstapContext, IRON, Items.IRON_INGOT, Style.EMPTY.withColor(15527148), 0.2F, Optional.of(ArmorMaterials.IRON));
		register(bootstapContext, NETHERITE, Items.NETHERITE_INGOT, Style.EMPTY.withColor(6445145), 0.3F, Optional.of(ArmorMaterials.NETHERITE));
		register(bootstapContext, REDSTONE, Items.REDSTONE, Style.EMPTY.withColor(9901575), 0.4F, Optional.empty());
		register(bootstapContext, COPPER, Items.COPPER_INGOT, Style.EMPTY.withColor(11823181), 0.5F, Optional.empty());
		register(bootstapContext, GOLD, Items.GOLD_INGOT, Style.EMPTY.withColor(14594349), 0.6F, Optional.of(ArmorMaterials.GOLD));
		register(bootstapContext, EMERALD, Items.EMERALD, Style.EMPTY.withColor(1155126), 0.7F, Optional.empty());
		register(bootstapContext, DIAMOND, Items.DIAMOND, Style.EMPTY.withColor(7269586), 0.8F, Optional.of(ArmorMaterials.DIAMOND));
		register(bootstapContext, LAPIS, Items.LAPIS_LAZULI, Style.EMPTY.withColor(4288151), 0.9F, Optional.empty());
		register(bootstapContext, AMETHYST, Items.AMETHYST_SHARD, Style.EMPTY.withColor(10116294), 1.0F, Optional.empty());
	}

	public static Optional<Holder.Reference<TrimMaterial>> getFromIngredient(RegistryAccess registryAccess, ItemStack itemStack) {
		return registryAccess.registryOrThrow(Registries.TRIM_MATERIAL)
			.holders()
			.filter(reference -> itemStack.is(((TrimMaterial)reference.value()).ingredient()))
			.findFirst();
	}

	private static void register(
		BootstapContext<TrimMaterial> bootstapContext, ResourceKey<TrimMaterial> resourceKey, Item item, Style style, float f, Optional<ArmorMaterials> optional
	) {
		TrimMaterial trimMaterial = TrimMaterial.create(
			resourceKey.location().getPath(),
			item,
			f,
			optional,
			Component.translatable(Util.makeDescriptionId("trim_material", resourceKey.location())).withStyle(style)
		);
		bootstapContext.register(resourceKey, trimMaterial);
	}

	private static ResourceKey<TrimMaterial> registryKey(String string) {
		return ResourceKey.create(Registries.TRIM_MATERIAL, new ResourceLocation(string));
	}
}
