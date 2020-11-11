/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.providers.score;

import java.util.Set;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.providers.score.LootScoreProviderType;
import org.jetbrains.annotations.Nullable;

public interface ScoreboardNameProvider {
    @Nullable
    public String getScoreboardName(LootContext var1);

    public LootScoreProviderType getType();

    public Set<LootContextParam<?>> getReferencedContextParams();
}

