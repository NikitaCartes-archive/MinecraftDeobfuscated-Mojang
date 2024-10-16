package net.minecraft.data;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.WorldVersion;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.advancements.packs.VanillaAdvancementProvider;
import net.minecraft.data.advancements.packs.WinterDropAdvancementProvider;
import net.minecraft.data.info.BiomeParametersDumpReport;
import net.minecraft.data.info.BlockListReport;
import net.minecraft.data.info.CommandsReport;
import net.minecraft.data.info.DatapackStructureReport;
import net.minecraft.data.info.ItemListReport;
import net.minecraft.data.info.PacketReport;
import net.minecraft.data.info.RegistryDumpReport;
import net.minecraft.data.loot.packs.TradeRebalanceLootTableProvider;
import net.minecraft.data.loot.packs.VanillaLootTableProvider;
import net.minecraft.data.loot.packs.WinterDropLootTableProvider;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.data.models.EquipmentModelProvider;
import net.minecraft.data.models.ModelProvider;
import net.minecraft.data.recipes.packs.VanillaRecipeProvider;
import net.minecraft.data.recipes.packs.WinterDropRecipeProvider;
import net.minecraft.data.registries.RegistriesDatapackGenerator;
import net.minecraft.data.registries.TradeRebalanceRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.data.registries.WinterDropRegistries;
import net.minecraft.data.structures.NbtToSnbt;
import net.minecraft.data.structures.SnbtToNbt;
import net.minecraft.data.structures.StructureUpdater;
import net.minecraft.data.tags.BannerPatternTagsProvider;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.data.tags.CatVariantTagsProvider;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.data.tags.FlatLevelGeneratorPresetTagsProvider;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.data.tags.GameEventTagsProvider;
import net.minecraft.data.tags.InstrumentTagsProvider;
import net.minecraft.data.tags.PaintingVariantTagsProvider;
import net.minecraft.data.tags.PoiTypeTagsProvider;
import net.minecraft.data.tags.StructureTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.data.tags.TradeRebalanceEnchantmentTagsProvider;
import net.minecraft.data.tags.TradeRebalanceStructureTagsProvider;
import net.minecraft.data.tags.VanillaBlockTagsProvider;
import net.minecraft.data.tags.VanillaEnchantmentTagsProvider;
import net.minecraft.data.tags.VanillaItemTagsProvider;
import net.minecraft.data.tags.WinterDropBiomeTagsProvider;
import net.minecraft.data.tags.WinterDropBlockTagsProvider;
import net.minecraft.data.tags.WinterDropEntityTypeTagsProvider;
import net.minecraft.data.tags.WinterDropItemTagsProvider;
import net.minecraft.data.tags.WorldPresetTagsProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.levelgen.structure.Structure;

public class Main {
	@DontObfuscate
	public static void main(String[] strings) throws IOException {
		SharedConstants.tryDetectVersion();
		OptionParser optionParser = new OptionParser();
		OptionSpec<Void> optionSpec = optionParser.accepts("help", "Show the help menu").forHelp();
		OptionSpec<Void> optionSpec2 = optionParser.accepts("server", "Include server generators");
		OptionSpec<Void> optionSpec3 = optionParser.accepts("client", "Include client generators");
		OptionSpec<Void> optionSpec4 = optionParser.accepts("dev", "Include development tools");
		OptionSpec<Void> optionSpec5 = optionParser.accepts("reports", "Include data reports");
		OptionSpec<Void> optionSpec6 = optionParser.accepts("validate", "Validate inputs");
		OptionSpec<Void> optionSpec7 = optionParser.accepts("all", "Include all generators");
		OptionSpec<String> optionSpec8 = optionParser.accepts("output", "Output folder").withRequiredArg().defaultsTo("generated");
		OptionSpec<String> optionSpec9 = optionParser.accepts("input", "Input folder").withRequiredArg();
		OptionSet optionSet = optionParser.parse(strings);
		if (!optionSet.has(optionSpec) && optionSet.hasOptions()) {
			Path path = Paths.get(optionSpec8.value(optionSet));
			boolean bl = optionSet.has(optionSpec7);
			boolean bl2 = bl || optionSet.has(optionSpec3);
			boolean bl3 = bl || optionSet.has(optionSpec2);
			boolean bl4 = bl || optionSet.has(optionSpec4);
			boolean bl5 = bl || optionSet.has(optionSpec5);
			boolean bl6 = bl || optionSet.has(optionSpec6);
			DataGenerator dataGenerator = createStandardGenerator(
				path,
				(Collection<Path>)optionSet.valuesOf(optionSpec9).stream().map(string -> Paths.get(string)).collect(Collectors.toList()),
				bl2,
				bl3,
				bl4,
				bl5,
				bl6,
				SharedConstants.getCurrentVersion(),
				true
			);
			dataGenerator.run();
		} else {
			optionParser.printHelpOn(System.out);
		}
	}

	private static <T extends DataProvider> DataProvider.Factory<T> bindRegistries(
		BiFunction<PackOutput, CompletableFuture<HolderLookup.Provider>, T> biFunction, CompletableFuture<HolderLookup.Provider> completableFuture
	) {
		return packOutput -> (T)biFunction.apply(packOutput, completableFuture);
	}

	public static DataGenerator createStandardGenerator(
		Path path, Collection<Path> collection, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5, WorldVersion worldVersion, boolean bl6
	) {
		DataGenerator dataGenerator = new DataGenerator(path, worldVersion, bl6);
		DataGenerator.PackGenerator packGenerator = dataGenerator.getVanillaPack(bl || bl2);
		packGenerator.addProvider(packOutput -> new SnbtToNbt(packOutput, collection).addFilter(new StructureUpdater()));
		CompletableFuture<HolderLookup.Provider> completableFuture = CompletableFuture.supplyAsync(VanillaRegistries::createLookup, Util.backgroundExecutor());
		DataGenerator.PackGenerator packGenerator2 = dataGenerator.getVanillaPack(bl);
		packGenerator2.addProvider(ModelProvider::new);
		packGenerator2.addProvider(EquipmentModelProvider::new);
		DataGenerator.PackGenerator packGenerator3 = dataGenerator.getVanillaPack(bl2);
		packGenerator3.addProvider(bindRegistries(RegistriesDatapackGenerator::new, completableFuture));
		packGenerator3.addProvider(bindRegistries(VanillaAdvancementProvider::create, completableFuture));
		packGenerator3.addProvider(bindRegistries(VanillaLootTableProvider::create, completableFuture));
		packGenerator3.addProvider(bindRegistries(VanillaRecipeProvider.Runner::new, completableFuture));
		TagsProvider<Block> tagsProvider = packGenerator3.addProvider(bindRegistries(VanillaBlockTagsProvider::new, completableFuture));
		TagsProvider<Item> tagsProvider2 = packGenerator3.addProvider(
			packOutput -> new VanillaItemTagsProvider(packOutput, completableFuture, tagsProvider.contentsGetter())
		);
		TagsProvider<Biome> tagsProvider3 = packGenerator3.addProvider(bindRegistries(BiomeTagsProvider::new, completableFuture));
		TagsProvider<BannerPattern> tagsProvider4 = packGenerator3.addProvider(bindRegistries(BannerPatternTagsProvider::new, completableFuture));
		TagsProvider<Structure> tagsProvider5 = packGenerator3.addProvider(bindRegistries(StructureTagsProvider::new, completableFuture));
		packGenerator3.addProvider(bindRegistries(CatVariantTagsProvider::new, completableFuture));
		packGenerator3.addProvider(bindRegistries(DamageTypeTagsProvider::new, completableFuture));
		packGenerator3.addProvider(bindRegistries(EntityTypeTagsProvider::new, completableFuture));
		packGenerator3.addProvider(bindRegistries(FlatLevelGeneratorPresetTagsProvider::new, completableFuture));
		packGenerator3.addProvider(bindRegistries(FluidTagsProvider::new, completableFuture));
		packGenerator3.addProvider(bindRegistries(GameEventTagsProvider::new, completableFuture));
		packGenerator3.addProvider(bindRegistries(InstrumentTagsProvider::new, completableFuture));
		packGenerator3.addProvider(bindRegistries(PaintingVariantTagsProvider::new, completableFuture));
		packGenerator3.addProvider(bindRegistries(PoiTypeTagsProvider::new, completableFuture));
		packGenerator3.addProvider(bindRegistries(WorldPresetTagsProvider::new, completableFuture));
		packGenerator3.addProvider(bindRegistries(VanillaEnchantmentTagsProvider::new, completableFuture));
		packGenerator3 = dataGenerator.getVanillaPack(bl3);
		packGenerator3.addProvider(packOutput -> new NbtToSnbt(packOutput, collection));
		packGenerator3 = dataGenerator.getVanillaPack(bl4);
		packGenerator3.addProvider(bindRegistries(BiomeParametersDumpReport::new, completableFuture));
		packGenerator3.addProvider(bindRegistries(ItemListReport::new, completableFuture));
		packGenerator3.addProvider(bindRegistries(BlockListReport::new, completableFuture));
		packGenerator3.addProvider(bindRegistries(CommandsReport::new, completableFuture));
		packGenerator3.addProvider(RegistryDumpReport::new);
		packGenerator3.addProvider(PacketReport::new);
		packGenerator3.addProvider(DatapackStructureReport::new);
		CompletableFuture<RegistrySetBuilder.PatchedRegistries> completableFuture2 = TradeRebalanceRegistries.createLookup(completableFuture);
		CompletableFuture<HolderLookup.Provider> completableFuture3 = completableFuture2.thenApply(RegistrySetBuilder.PatchedRegistries::patches);
		DataGenerator.PackGenerator packGenerator4 = dataGenerator.getBuiltinDatapack(bl2, "trade_rebalance");
		packGenerator4.addProvider(bindRegistries(RegistriesDatapackGenerator::new, completableFuture3));
		packGenerator4.addProvider(
			packOutput -> PackMetadataGenerator.forFeaturePack(
					packOutput, Component.translatable("dataPack.trade_rebalance.description"), FeatureFlagSet.of(FeatureFlags.TRADE_REBALANCE)
				)
		);
		packGenerator4.addProvider(bindRegistries(TradeRebalanceLootTableProvider::create, completableFuture));
		packGenerator4.addProvider(bindRegistries(TradeRebalanceStructureTagsProvider::new, completableFuture));
		packGenerator4.addProvider(bindRegistries(TradeRebalanceEnchantmentTagsProvider::new, completableFuture));
		packGenerator3 = dataGenerator.getBuiltinDatapack(bl2, "redstone_experiments");
		packGenerator3.addProvider(
			packOutput -> PackMetadataGenerator.forFeaturePack(
					packOutput, Component.translatable("dataPack.redstone_experiments.description"), FeatureFlagSet.of(FeatureFlags.REDSTONE_EXPERIMENTS)
				)
		);
		packGenerator3 = dataGenerator.getBuiltinDatapack(bl2, "minecart_improvements");
		packGenerator3.addProvider(
			packOutput -> PackMetadataGenerator.forFeaturePack(
					packOutput, Component.translatable("dataPack.minecart_improvements.description"), FeatureFlagSet.of(FeatureFlags.MINECART_IMPROVEMENTS)
				)
		);
		CompletableFuture<RegistrySetBuilder.PatchedRegistries> completableFuture2x = WinterDropRegistries.createLookup(completableFuture);
		completableFuture3 = completableFuture2x.thenApply(RegistrySetBuilder.PatchedRegistries::full);
		packGenerator4 = dataGenerator.getBuiltinDatapack(bl2, "winter_drop");
		packGenerator4.addProvider(bindRegistries(RegistriesDatapackGenerator::new, completableFuture2x.thenApply(RegistrySetBuilder.PatchedRegistries::patches)));
		packGenerator4.addProvider(bindRegistries(WinterDropRecipeProvider.Runner::new, completableFuture3));
		TagsProvider<Block> tagsProvider6 = packGenerator4.addProvider(
			packOutput -> new WinterDropBlockTagsProvider(packOutput, completableFuture3, tagsProvider.contentsGetter())
		);
		packGenerator4.addProvider(
			packOutput -> new WinterDropItemTagsProvider(packOutput, completableFuture3, tagsProvider2.contentsGetter(), tagsProvider6.contentsGetter())
		);
		packGenerator4.addProvider(packOutput -> new WinterDropBiomeTagsProvider(packOutput, completableFuture3, tagsProvider3.contentsGetter()));
		packGenerator4.addProvider(bindRegistries(WinterDropLootTableProvider::create, completableFuture3));
		packGenerator4.addProvider(
			packOutput -> PackMetadataGenerator.forFeaturePack(
					packOutput, Component.translatable("dataPack.winter_drop.description"), FeatureFlagSet.of(FeatureFlags.WINTER_DROP)
				)
		);
		packGenerator4.addProvider(bindRegistries(WinterDropEntityTypeTagsProvider::new, completableFuture3));
		packGenerator4.addProvider(bindRegistries(WinterDropAdvancementProvider::create, completableFuture3));
		return dataGenerator;
	}
}
