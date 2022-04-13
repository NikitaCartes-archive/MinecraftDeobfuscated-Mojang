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
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.info.BiomeParametersDumpReport;
import net.minecraft.data.info.BlockListReport;
import net.minecraft.data.info.CommandsReport;
import net.minecraft.data.info.RegistryDumpReport;
import net.minecraft.data.info.WorldgenRegistryDumpReport;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.models.ModelProvider;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.structures.NbtToSnbt;
import net.minecraft.data.structures.SnbtToNbt;
import net.minecraft.data.structures.StructureUpdater;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.CatVariantTagsProvider;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.data.tags.FlatLevelGeneratorPresetTagsProvider;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.data.tags.GameEventTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.StructureTagsProvider;
import net.minecraft.data.tags.WorldPresetTagsProvider;
import net.minecraft.obfuscate.DontObfuscate;

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
		DataGenerator dataGenerator = new DataGenerator(path, collection, worldVersion, bl6);
		dataGenerator.addProvider(bl || bl2, new SnbtToNbt(dataGenerator).addFilter(new StructureUpdater()));
		dataGenerator.addProvider(bl, new ModelProvider(dataGenerator));
		dataGenerator.addProvider(bl2, new FluidTagsProvider(dataGenerator));
		BlockTagsProvider blockTagsProvider = new BlockTagsProvider(dataGenerator);
		dataGenerator.addProvider(bl2, blockTagsProvider);
		dataGenerator.addProvider(bl2, new ItemTagsProvider(dataGenerator, blockTagsProvider));
		dataGenerator.addProvider(bl2, new EntityTypeTagsProvider(dataGenerator));
		dataGenerator.addProvider(bl2, new RecipeProvider(dataGenerator));
		dataGenerator.addProvider(bl2, new AdvancementProvider(dataGenerator));
		dataGenerator.addProvider(bl2, new LootTableProvider(dataGenerator));
		dataGenerator.addProvider(bl2, new GameEventTagsProvider(dataGenerator));
		dataGenerator.addProvider(bl2, new BiomeTagsProvider(dataGenerator));
		dataGenerator.addProvider(bl2, new StructureTagsProvider(dataGenerator));
		dataGenerator.addProvider(bl2, new WorldPresetTagsProvider(dataGenerator));
		dataGenerator.addProvider(bl2, new FlatLevelGeneratorPresetTagsProvider(dataGenerator));
		dataGenerator.addProvider(bl2, new CatVariantTagsProvider(dataGenerator));
		dataGenerator.addProvider(bl3, new NbtToSnbt(dataGenerator));
		dataGenerator.addProvider(bl4, new BlockListReport(dataGenerator));
		dataGenerator.addProvider(bl4, new RegistryDumpReport(dataGenerator));
		dataGenerator.addProvider(bl4, new CommandsReport(dataGenerator));
		dataGenerator.addProvider(bl4, new WorldgenRegistryDumpReport(dataGenerator));
		dataGenerator.addProvider(bl4, new BiomeParametersDumpReport(dataGenerator));
		return dataGenerator;
	}
}
