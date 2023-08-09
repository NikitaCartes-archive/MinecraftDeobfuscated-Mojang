package net.minecraft.world.level.storage.loot.providers.score;

import com.mojang.serialization.Codec;

public record LootScoreProviderType(Codec<? extends ScoreboardNameProvider> codec) {
}
