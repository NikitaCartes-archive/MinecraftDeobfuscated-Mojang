package net.minecraft.world.level.storage.loot.providers.nbt;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public record StorageNbtProvider(ResourceLocation id) implements NbtProvider {
	public static final Codec<StorageNbtProvider> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(ResourceLocation.CODEC.fieldOf("source").forGetter(StorageNbtProvider::id)).apply(instance, StorageNbtProvider::new)
	);

	@Override
	public LootNbtProviderType getType() {
		return NbtProviders.STORAGE;
	}

	@Nullable
	@Override
	public Tag get(LootContext lootContext) {
		return lootContext.getLevel().getServer().getCommandStorage().get(this.id);
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return ImmutableSet.of();
	}
}
