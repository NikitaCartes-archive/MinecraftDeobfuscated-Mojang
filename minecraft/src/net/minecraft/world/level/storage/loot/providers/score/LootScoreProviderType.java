package net.minecraft.world.level.storage.loot.providers.score;

import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.SerializerType;

public class LootScoreProviderType extends SerializerType<ScoreboardNameProvider> {
	public LootScoreProviderType(Serializer<? extends ScoreboardNameProvider> serializer) {
		super(serializer);
	}
}
