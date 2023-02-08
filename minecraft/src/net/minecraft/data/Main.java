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
import net.minecraft.data.advancements.packs.UpdateOneTwentyVanillaAdvancementProvider;
import net.minecraft.data.advancements.packs.VanillaAdvancementProvider;
import net.minecraft.data.info.BiomeParametersDumpReport;
import net.minecraft.data.info.BlockListReport;
import net.minecraft.data.info.CommandsReport;
import net.minecraft.data.info.RegistryDumpReport;
import net.minecraft.data.loot.packs.UpdateOneTwentyLootTableProvider;
import net.minecraft.data.loot.packs.VanillaLootTableProvider;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.data.models.ModelProvider;
import net.minecraft.data.recipes.packs.BundleRecipeProvider;
import net.minecraft.data.recipes.packs.UpdateOneTwentyRecipeProvider;
import net.minecraft.data.recipes.packs.VanillaRecipeProvider;
import net.minecraft.data.registries.RegistriesDatapackGenerator;
import net.minecraft.data.registries.UpdateOneTwentyRegistries;
import net.minecraft.data.registries.VanillaRegistries;
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
import net.minecraft.data.tags.UpdateOneTwentyBlockTagsProvider;
import net.minecraft.data.tags.UpdateOneTwentyItemTagsProvider;
import net.minecraft.data.tags.VanillaBlockTagsProvider;
import net.minecraft.data.tags.VanillaItemTagsProvider;
import net.minecraft.data.tags.WorldPresetTagsProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

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
		packGenerator2 = dataGenerator.getVanillaPack(bl2);
		packGenerator2.addProvider(bindRegistries(RegistriesDatapackGenerator::new, completableFuture));
		packGenerator2.addProvider(bindRegistries(VanillaAdvancementProvider::create, completableFuture));
		packGenerator2.addProvider(VanillaLootTableProvider::create);
		packGenerator2.addProvider(VanillaRecipeProvider::new);
		TagsProvider<Block> tagsProvider = packGenerator2.addProvider(bindRegistries(VanillaBlockTagsProvider::new, completableFuture));
		packGenerator2.addProvider(packOutput -> new VanillaItemTagsProvider(packOutput, completableFuture, tagsProvider));
		packGenerator2.addProvider(bindRegistries(BannerPatternTagsProvider::new, completableFuture));
		packGenerator2.addProvider(bindRegistries(BiomeTagsProvider::new, completableFuture));
		packGenerator2.addProvider(bindRegistries(CatVariantTagsProvider::new, completableFuture));
		packGenerator2.addProvider(bindRegistries(DamageTypeTagsProvider::new, completableFuture));
		packGenerator2.addProvider(bindRegistries(EntityTypeTagsProvider::new, completableFuture));
		packGenerator2.addProvider(bindRegistries(FlatLevelGeneratorPresetTagsProvider::new, completableFuture));
		packGenerator2.addProvider(bindRegistries(FluidTagsProvider::new, completableFuture));
		packGenerator2.addProvider(bindRegistries(GameEventTagsProvider::new, completableFuture));
		packGenerator2.addProvider(bindRegistries(InstrumentTagsProvider::new, completableFuture));
		packGenerator2.addProvider(bindRegistries(PaintingVariantTagsProvider::new, completableFuture));
		packGenerator2.addProvider(bindRegistries(PoiTypeTagsProvider::new, completableFuture));
		packGenerator2.addProvider(bindRegistries(StructureTagsProvider::new, completableFuture));
		packGenerator2.addProvider(bindRegistries(WorldPresetTagsProvider::new, completableFuture));
		packGenerator2 = dataGenerator.getVanillaPack(bl3);
		packGenerator2.addProvider(packOutput -> new NbtToSnbt(packOutput, collection));
		packGenerator2 = dataGenerator.getVanillaPack(bl4);
		packGenerator2.addProvider(bindRegistries(BiomeParametersDumpReport::new, completableFuture));
		packGenerator2.addProvider(BlockListReport::new);
		packGenerator2.addProvider(bindRegistries(CommandsReport::new, completableFuture));
		packGenerator2.addProvider(RegistryDumpReport::new);
		packGenerator2 = dataGenerator.getBuiltinDatapack(bl2, "bundle");
		packGenerator2.addProvider(BundleRecipeProvider::new);
		packGenerator2.addProvider(
			packOutput -> PackMetadataGenerator.forFeaturePack(packOutput, Component.translatable("dataPack.bundle.description"), FeatureFlagSet.of(FeatureFlags.BUNDLE))
		);
		CompletableFuture<HolderLookup.Provider> completableFuture2 = CompletableFuture.supplyAsync(
			UpdateOneTwentyRegistries::createLookup, Util.backgroundExecutor()
		);
		DataGenerator.PackGenerator packGenerator3 = dataGenerator.getBuiltinDatapack(bl2, "update_1_20");
		packGenerator3.addProvider(UpdateOneTwentyRecipeProvider::new);
		TagsProvider<Block> tagsProvider2 = packGenerator3.addProvider(bindRegistries(UpdateOneTwentyBlockTagsProvider::new, completableFuture));
		packGenerator3.addProvider(packOutput -> new UpdateOneTwentyItemTagsProvider(packOutput, completableFuture, tagsProvider2));
		packGenerator3.addProvider(UpdateOneTwentyLootTableProvider::create);
		packGenerator3.addProvider(bindRegistries(UpdateOneTwentyVanillaAdvancementProvider::create, completableFuture));
		packGenerator3.addProvider(bindRegistries(RegistriesDatapackGenerator::new, completableFuture2));
		packGenerator3.addProvider(
			packOutput -> PackMetadataGenerator.forFeaturePack(
					packOutput, Component.translatable("dataPack.update_1_20.description"), FeatureFlagSet.of(FeatureFlags.UPDATE_1_20)
				)
		);
		return dataGenerator;
	}
}
