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
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.info.BlockListReport;
import net.minecraft.data.info.CommandsReport;
import net.minecraft.data.info.RegistryDumpReport;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.models.ModelProvider;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.structures.NbtToSnbt;
import net.minecraft.data.structures.SnbtToNbt;
import net.minecraft.data.structures.StructureUpdater;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.data.tags.GameEventTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.worldgen.biome.BiomeReport;
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
				path, (Collection<Path>)optionSet.valuesOf(optionSpec9).stream().map(string -> Paths.get(string)).collect(Collectors.toList()), bl2, bl3, bl4, bl5, bl6
			);
			dataGenerator.run();
		} else {
			optionParser.printHelpOn(System.out);
		}
	}

	public static DataGenerator createStandardGenerator(Path path, Collection<Path> collection, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5) {
		DataGenerator dataGenerator = new DataGenerator(path, collection);
		if (bl || bl2) {
			dataGenerator.addProvider(new SnbtToNbt(dataGenerator).addFilter(new StructureUpdater()));
		}

		if (bl) {
			dataGenerator.addProvider(new ModelProvider(dataGenerator));
		}

		if (bl2) {
			dataGenerator.addProvider(new FluidTagsProvider(dataGenerator));
			BlockTagsProvider blockTagsProvider = new BlockTagsProvider(dataGenerator);
			dataGenerator.addProvider(blockTagsProvider);
			dataGenerator.addProvider(new ItemTagsProvider(dataGenerator, blockTagsProvider));
			dataGenerator.addProvider(new EntityTypeTagsProvider(dataGenerator));
			dataGenerator.addProvider(new RecipeProvider(dataGenerator));
			dataGenerator.addProvider(new AdvancementProvider(dataGenerator));
			dataGenerator.addProvider(new LootTableProvider(dataGenerator));
			dataGenerator.addProvider(new GameEventTagsProvider(dataGenerator));
		}

		if (bl3) {
			dataGenerator.addProvider(new NbtToSnbt(dataGenerator));
		}

		if (bl4) {
			dataGenerator.addProvider(new BlockListReport(dataGenerator));
			dataGenerator.addProvider(new RegistryDumpReport(dataGenerator));
			dataGenerator.addProvider(new CommandsReport(dataGenerator));
			dataGenerator.addProvider(new BiomeReport(dataGenerator));
		}

		return dataGenerator;
	}
}
