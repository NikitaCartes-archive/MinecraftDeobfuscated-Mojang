package net.minecraft.data;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.SharedConstants;
import net.minecraft.WorldVersion;
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
import net.minecraft.data.structures.NbtToSnbt;
import net.minecraft.data.structures.SnbtToNbt;
import net.minecraft.data.structures.StructureUpdater;
import net.minecraft.data.tags.BannerPatternTagsProvider;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.data.tags.CatVariantTagsProvider;
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
import net.minecraft.data.worldgen.BuiltinRegistriesDatapackGenerator;
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

	public static DataGenerator createStandardGenerator(
		Path path, Collection<Path> collection, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5, WorldVersion worldVersion, boolean bl6
	) {
		DataGenerator dataGenerator = new DataGenerator(path, worldVersion, bl6);
		PackOutput packOutput = dataGenerator.getVanillaPackOutput();
		dataGenerator.addProvider(bl || bl2, new SnbtToNbt(packOutput, collection).addFilter(new StructureUpdater()));
		dataGenerator.addProvider(bl, new ModelProvider(packOutput));
		dataGenerator.addProvider(bl2, new BuiltinRegistriesDatapackGenerator(packOutput));
		dataGenerator.addProvider(bl2, VanillaAdvancementProvider.create(packOutput));
		dataGenerator.addProvider(bl2, VanillaLootTableProvider.create(packOutput));
		dataGenerator.addProvider(bl2, new VanillaRecipeProvider(packOutput));
		TagsProvider<Block> tagsProvider = new VanillaBlockTagsProvider(packOutput);
		dataGenerator.addProvider(bl2, tagsProvider);
		dataGenerator.addProvider(bl2, new VanillaItemTagsProvider(packOutput, tagsProvider));
		dataGenerator.addProvider(bl2, new BannerPatternTagsProvider(packOutput));
		dataGenerator.addProvider(bl2, new BiomeTagsProvider(packOutput));
		dataGenerator.addProvider(bl2, new CatVariantTagsProvider(packOutput));
		dataGenerator.addProvider(bl2, new EntityTypeTagsProvider(packOutput));
		dataGenerator.addProvider(bl2, new FlatLevelGeneratorPresetTagsProvider(packOutput));
		dataGenerator.addProvider(bl2, new FluidTagsProvider(packOutput));
		dataGenerator.addProvider(bl2, new GameEventTagsProvider(packOutput));
		dataGenerator.addProvider(bl2, new InstrumentTagsProvider(packOutput));
		dataGenerator.addProvider(bl2, new PaintingVariantTagsProvider(packOutput));
		dataGenerator.addProvider(bl2, new PoiTypeTagsProvider(packOutput));
		dataGenerator.addProvider(bl2, new StructureTagsProvider(packOutput));
		dataGenerator.addProvider(bl2, new WorldPresetTagsProvider(packOutput));
		dataGenerator.addProvider(bl3, new NbtToSnbt(packOutput, collection));
		dataGenerator.addProvider(bl4, new BiomeParametersDumpReport(packOutput));
		dataGenerator.addProvider(bl4, new BlockListReport(packOutput));
		dataGenerator.addProvider(bl4, new CommandsReport(packOutput));
		dataGenerator.addProvider(bl4, new RegistryDumpReport(packOutput));
		PackOutput packOutput2 = dataGenerator.createBuiltinDatapackOutput("bundle");
		dataGenerator.addProvider(bl2, new BundleRecipeProvider(packOutput2));
		dataGenerator.addProvider(
			bl2,
			PackMetadataGenerator.forFeaturePack(packOutput2, "bundle", Component.translatable("dataPack.bundle.description"), FeatureFlagSet.of(FeatureFlags.BUNDLE))
		);
		PackOutput packOutput3 = dataGenerator.createBuiltinDatapackOutput("update_1_20");
		dataGenerator.addProvider(bl2, new UpdateOneTwentyRecipeProvider(packOutput3));
		UpdateOneTwentyBlockTagsProvider updateOneTwentyBlockTagsProvider = new UpdateOneTwentyBlockTagsProvider(packOutput3);
		dataGenerator.addProvider(bl2, updateOneTwentyBlockTagsProvider);
		dataGenerator.addProvider(bl2, new UpdateOneTwentyItemTagsProvider(packOutput3, updateOneTwentyBlockTagsProvider));
		dataGenerator.addProvider(bl2, UpdateOneTwentyLootTableProvider.create(packOutput3));
		dataGenerator.addProvider(
			bl2,
			PackMetadataGenerator.forFeaturePack(
				packOutput3, "update_1_20", Component.translatable("dataPack.update_1_20.description"), FeatureFlagSet.of(FeatureFlags.UPDATE_1_20)
			)
		);
		return dataGenerator;
	}
}
