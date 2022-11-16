package net.minecraft.data.advancements.packs;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;

public class UpdateOneTwentyVanillaAdvancementProvider {
	public static AdvancementProvider create(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
		return new AdvancementProvider(packOutput, completableFuture, List.of(new UpdateOneTwentyAdvancements()));
	}
}
