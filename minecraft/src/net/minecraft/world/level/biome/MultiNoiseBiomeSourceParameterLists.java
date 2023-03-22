package net.minecraft.world.level.biome;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class MultiNoiseBiomeSourceParameterLists {
	public static final ResourceKey<MultiNoiseBiomeSourceParameterList> NETHER = register("nether");
	public static final ResourceKey<MultiNoiseBiomeSourceParameterList> OVERWORLD = register("overworld");

	public static void bootstrap(BootstapContext<MultiNoiseBiomeSourceParameterList> bootstapContext) {
		HolderGetter<Biome> holderGetter = bootstapContext.lookup(Registries.BIOME);
		bootstapContext.register(NETHER, new MultiNoiseBiomeSourceParameterList(MultiNoiseBiomeSourceParameterList.Preset.NETHER, holderGetter));
		bootstapContext.register(OVERWORLD, new MultiNoiseBiomeSourceParameterList(MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD, holderGetter));
	}

	private static ResourceKey<MultiNoiseBiomeSourceParameterList> register(String string) {
		return ResourceKey.create(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST, new ResourceLocation(string));
	}
}
