package net.minecraft.world.item.armortrim;

import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TrimPatterns {
	public static final ResourceKey<TrimPattern> SENTRY = registryKey("sentry");
	public static final ResourceKey<TrimPattern> DUNE = registryKey("dune");
	public static final ResourceKey<TrimPattern> COAST = registryKey("coast");
	public static final ResourceKey<TrimPattern> WILD = registryKey("wild");
	public static final ResourceKey<TrimPattern> WARD = registryKey("ward");
	public static final ResourceKey<TrimPattern> EYE = registryKey("eye");
	public static final ResourceKey<TrimPattern> VEX = registryKey("vex");
	public static final ResourceKey<TrimPattern> TIDE = registryKey("tide");
	public static final ResourceKey<TrimPattern> SNOUT = registryKey("snout");
	public static final ResourceKey<TrimPattern> RIB = registryKey("rib");
	public static final ResourceKey<TrimPattern> SPIRE = registryKey("spire");
	public static final ResourceKey<TrimPattern> WAYFINDER = registryKey("wayfinder");
	public static final ResourceKey<TrimPattern> SHAPER = registryKey("shaper");
	public static final ResourceKey<TrimPattern> SILENCE = registryKey("silence");
	public static final ResourceKey<TrimPattern> RAISER = registryKey("raiser");
	public static final ResourceKey<TrimPattern> HOST = registryKey("host");

	public static void bootstrap(BootstrapContext<TrimPattern> bootstrapContext) {
		register(bootstrapContext, Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, SENTRY);
		register(bootstrapContext, Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE, DUNE);
		register(bootstrapContext, Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, COAST);
		register(bootstrapContext, Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE, WILD);
		register(bootstrapContext, Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE, WARD);
		register(bootstrapContext, Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE, EYE);
		register(bootstrapContext, Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, VEX);
		register(bootstrapContext, Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE, TIDE);
		register(bootstrapContext, Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, SNOUT);
		register(bootstrapContext, Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, RIB);
		register(bootstrapContext, Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, SPIRE);
		register(bootstrapContext, Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE, WAYFINDER);
		register(bootstrapContext, Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE, SHAPER);
		register(bootstrapContext, Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE, SILENCE);
		register(bootstrapContext, Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE, RAISER);
		register(bootstrapContext, Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE, HOST);
	}

	public static Optional<Holder.Reference<TrimPattern>> getFromTemplate(RegistryAccess registryAccess, ItemStack itemStack) {
		return registryAccess.registryOrThrow(Registries.TRIM_PATTERN)
			.holders()
			.filter(reference -> itemStack.is(((TrimPattern)reference.value()).templateItem()))
			.findFirst();
	}

	private static void register(BootstrapContext<TrimPattern> bootstrapContext, Item item, ResourceKey<TrimPattern> resourceKey) {
		TrimPattern trimPattern = new TrimPattern(
			resourceKey.location(),
			BuiltInRegistries.ITEM.wrapAsHolder(item),
			Component.translatable(Util.makeDescriptionId("trim_pattern", resourceKey.location())),
			false
		);
		bootstrapContext.register(resourceKey, trimPattern);
	}

	private static ResourceKey<TrimPattern> registryKey(String string) {
		return ResourceKey.create(Registries.TRIM_PATTERN, new ResourceLocation(string));
	}
}
