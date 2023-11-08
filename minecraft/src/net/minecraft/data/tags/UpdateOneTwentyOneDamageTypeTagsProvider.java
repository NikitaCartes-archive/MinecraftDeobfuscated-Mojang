package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;

public class UpdateOneTwentyOneDamageTypeTagsProvider extends TagsProvider<DamageType> {
	public UpdateOneTwentyOneDamageTypeTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
		super(packOutput, Registries.DAMAGE_TYPE, completableFuture);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		this.tag(DamageTypeTags.BREEZE_IMMUNE_TO).add(DamageTypes.ARROW, DamageTypes.TRIDENT);
	}
}
