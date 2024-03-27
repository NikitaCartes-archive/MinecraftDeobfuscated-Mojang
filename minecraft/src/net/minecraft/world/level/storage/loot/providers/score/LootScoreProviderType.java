package net.minecraft.world.level.storage.loot.providers.score;

import com.mojang.serialization.MapCodec;

public record LootScoreProviderType(MapCodec<? extends ScoreboardNameProvider> codec) {
}
