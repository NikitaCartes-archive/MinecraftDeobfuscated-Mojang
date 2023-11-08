package net.minecraft.data.registries;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.UpdateOneTwentyOnePools;
import net.minecraft.data.worldgen.UpdateOneTwentyOneProcessorLists;
import net.minecraft.data.worldgen.UpdateOneTwentyOneStructureSets;
import net.minecraft.data.worldgen.UpdateOneTwentyOneStructures;

public class UpdateOneTwentyOneRegistries {
	private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
		.add(Registries.TEMPLATE_POOL, UpdateOneTwentyOnePools::bootstrap)
		.add(Registries.STRUCTURE, UpdateOneTwentyOneStructures::bootstrap)
		.add(Registries.STRUCTURE_SET, UpdateOneTwentyOneStructureSets::bootstrap)
		.add(Registries.PROCESSOR_LIST, UpdateOneTwentyOneProcessorLists::bootstrap);

	public static CompletableFuture<RegistrySetBuilder.PatchedRegistries> createLookup(CompletableFuture<HolderLookup.Provider> completableFuture) {
		return RegistryPatchGenerator.createLookup(completableFuture, BUILDER);
	}
}
