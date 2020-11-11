package net.minecraft.world.level.storage.loot.providers.nbt;

import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.SerializerType;

public class LootNbtProviderType extends SerializerType<NbtProvider> {
	public LootNbtProviderType(Serializer<? extends NbtProvider> serializer) {
		super(serializer);
	}
}
