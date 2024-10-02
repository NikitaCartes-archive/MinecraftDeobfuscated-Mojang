package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;

public class WinterDropEntityTypeTagsProvider extends IntrinsicHolderTagsProvider<EntityType<?>> {
	public WinterDropEntityTypeTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
		super(packOutput, Registries.ENTITY_TYPE, completableFuture, entityType -> entityType.builtInRegistryHolder().key());
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		this.tag(EntityTypeTags.BOAT).add(EntityType.PALE_OAK_BOAT);
	}
}
