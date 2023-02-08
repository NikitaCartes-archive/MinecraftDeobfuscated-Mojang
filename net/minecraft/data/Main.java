/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import joptsimple.AbstractOptionSpec;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.WorldVersion;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
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

public class Main {
    @DontObfuscate
    public static void main(String[] strings) throws IOException {
        SharedConstants.tryDetectVersion();
        OptionParser optionParser = new OptionParser();
        AbstractOptionSpec optionSpec = optionParser.accepts("help", "Show the help menu").forHelp();
        OptionSpecBuilder optionSpec2 = optionParser.accepts("server", "Include server generators");
        OptionSpecBuilder optionSpec3 = optionParser.accepts("client", "Include client generators");
        OptionSpecBuilder optionSpec4 = optionParser.accepts("dev", "Include development tools");
        OptionSpecBuilder optionSpec5 = optionParser.accepts("reports", "Include data reports");
        OptionSpecBuilder optionSpec6 = optionParser.accepts("validate", "Validate inputs");
        OptionSpecBuilder optionSpec7 = optionParser.accepts("all", "Include all generators");
        ArgumentAcceptingOptionSpec<String> optionSpec8 = optionParser.accepts("output", "Output folder").withRequiredArg().defaultsTo("generated", (String[])new String[0]);
        ArgumentAcceptingOptionSpec<String> optionSpec9 = optionParser.accepts("input", "Input folder").withRequiredArg();
        OptionSet optionSet = optionParser.parse(strings);
        if (optionSet.has(optionSpec) || !optionSet.hasOptions()) {
            optionParser.printHelpOn(System.out);
            return;
        }
        Path path = Paths.get((String)optionSpec8.value(optionSet), new String[0]);
        boolean bl = optionSet.has(optionSpec7);
        boolean bl2 = bl || optionSet.has(optionSpec3);
        boolean bl3 = bl || optionSet.has(optionSpec2);
        boolean bl4 = bl || optionSet.has(optionSpec4);
        boolean bl5 = bl || optionSet.has(optionSpec5);
        boolean bl6 = bl || optionSet.has(optionSpec6);
        DataGenerator dataGenerator = Main.createStandardGenerator(path, optionSet.valuesOf(optionSpec9).stream().map(string -> Paths.get(string, new String[0])).collect(Collectors.toList()), bl2, bl3, bl4, bl5, bl6, SharedConstants.getCurrentVersion(), true);
        dataGenerator.run();
    }

    private static <T extends DataProvider> DataProvider.Factory<T> bindRegistries(BiFunction<PackOutput, CompletableFuture<HolderLookup.Provider>, T> biFunction, CompletableFuture<HolderLookup.Provider> completableFuture) {
        return packOutput -> (DataProvider)biFunction.apply(packOutput, completableFuture);
    }

    public static DataGenerator createStandardGenerator(Path path, Collection<Path> collection, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5, WorldVersion worldVersion, boolean bl6) {
        DataGenerator dataGenerator = new DataGenerator(path, worldVersion, bl6);
        DataGenerator.PackGenerator packGenerator = dataGenerator.getVanillaPack(bl || bl2);
        packGenerator.addProvider(packOutput -> new SnbtToNbt(packOutput, collection).addFilter(new StructureUpdater()));
        CompletableFuture<HolderLookup.Provider> completableFuture = CompletableFuture.supplyAsync(VanillaRegistries::createLookup, Util.backgroundExecutor());
        DataGenerator.PackGenerator packGenerator2 = dataGenerator.getVanillaPack(bl);
        packGenerator2.addProvider(ModelProvider::new);
        packGenerator2 = dataGenerator.getVanillaPack(bl2);
        packGenerator2.addProvider(Main.bindRegistries(RegistriesDatapackGenerator::new, completableFuture));
        packGenerator2.addProvider(Main.bindRegistries(VanillaAdvancementProvider::create, completableFuture));
        packGenerator2.addProvider(VanillaLootTableProvider::create);
        packGenerator2.addProvider(VanillaRecipeProvider::new);
        TagsProvider tagsProvider = packGenerator2.addProvider(Main.bindRegistries(VanillaBlockTagsProvider::new, completableFuture));
        packGenerator2.addProvider(packOutput -> new VanillaItemTagsProvider(packOutput, completableFuture, tagsProvider));
        packGenerator2.addProvider(Main.bindRegistries(BannerPatternTagsProvider::new, completableFuture));
        packGenerator2.addProvider(Main.bindRegistries(BiomeTagsProvider::new, completableFuture));
        packGenerator2.addProvider(Main.bindRegistries(CatVariantTagsProvider::new, completableFuture));
        packGenerator2.addProvider(Main.bindRegistries(DamageTypeTagsProvider::new, completableFuture));
        packGenerator2.addProvider(Main.bindRegistries(EntityTypeTagsProvider::new, completableFuture));
        packGenerator2.addProvider(Main.bindRegistries(FlatLevelGeneratorPresetTagsProvider::new, completableFuture));
        packGenerator2.addProvider(Main.bindRegistries(FluidTagsProvider::new, completableFuture));
        packGenerator2.addProvider(Main.bindRegistries(GameEventTagsProvider::new, completableFuture));
        packGenerator2.addProvider(Main.bindRegistries(InstrumentTagsProvider::new, completableFuture));
        packGenerator2.addProvider(Main.bindRegistries(PaintingVariantTagsProvider::new, completableFuture));
        packGenerator2.addProvider(Main.bindRegistries(PoiTypeTagsProvider::new, completableFuture));
        packGenerator2.addProvider(Main.bindRegistries(StructureTagsProvider::new, completableFuture));
        packGenerator2.addProvider(Main.bindRegistries(WorldPresetTagsProvider::new, completableFuture));
        packGenerator2 = dataGenerator.getVanillaPack(bl3);
        packGenerator2.addProvider(packOutput -> new NbtToSnbt(packOutput, collection));
        packGenerator2 = dataGenerator.getVanillaPack(bl4);
        packGenerator2.addProvider(Main.bindRegistries(BiomeParametersDumpReport::new, completableFuture));
        packGenerator2.addProvider(BlockListReport::new);
        packGenerator2.addProvider(Main.bindRegistries(CommandsReport::new, completableFuture));
        packGenerator2.addProvider(RegistryDumpReport::new);
        packGenerator2 = dataGenerator.getBuiltinDatapack(bl2, "bundle");
        packGenerator2.addProvider(BundleRecipeProvider::new);
        packGenerator2.addProvider(packOutput -> PackMetadataGenerator.forFeaturePack(packOutput, Component.translatable("dataPack.bundle.description"), FeatureFlagSet.of(FeatureFlags.BUNDLE)));
        CompletableFuture<HolderLookup.Provider> completableFuture2 = CompletableFuture.supplyAsync(UpdateOneTwentyRegistries::createLookup, Util.backgroundExecutor());
        DataGenerator.PackGenerator packGenerator3 = dataGenerator.getBuiltinDatapack(bl2, "update_1_20");
        packGenerator3.addProvider(UpdateOneTwentyRecipeProvider::new);
        TagsProvider tagsProvider2 = packGenerator3.addProvider(Main.bindRegistries(UpdateOneTwentyBlockTagsProvider::new, completableFuture));
        packGenerator3.addProvider(packOutput -> new UpdateOneTwentyItemTagsProvider(packOutput, completableFuture, tagsProvider2));
        packGenerator3.addProvider(UpdateOneTwentyLootTableProvider::create);
        packGenerator3.addProvider(Main.bindRegistries(UpdateOneTwentyVanillaAdvancementProvider::create, completableFuture));
        packGenerator3.addProvider(Main.bindRegistries(RegistriesDatapackGenerator::new, completableFuture2));
        packGenerator3.addProvider(packOutput -> PackMetadataGenerator.forFeaturePack(packOutput, Component.translatable("dataPack.update_1_20.description"), FeatureFlagSet.of(FeatureFlags.UPDATE_1_20)));
        return dataGenerator;
    }
}

