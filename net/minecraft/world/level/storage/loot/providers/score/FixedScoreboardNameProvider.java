/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.providers.score;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.providers.score.LootScoreProviderType;
import net.minecraft.world.level.storage.loot.providers.score.ScoreboardNameProvider;
import net.minecraft.world.level.storage.loot.providers.score.ScoreboardNameProviders;
import org.jetbrains.annotations.Nullable;

public class FixedScoreboardNameProvider
implements ScoreboardNameProvider {
    private final String name;

    private FixedScoreboardNameProvider(String string) {
        this.name = string;
    }

    @Override
    public LootScoreProviderType getType() {
        return ScoreboardNameProviders.FIXED;
    }

    @Override
    @Nullable
    public String getScoreboardName(LootContext lootContext) {
        return this.name;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of();
    }

    public static class Serializer
    implements net.minecraft.world.level.storage.loot.Serializer<FixedScoreboardNameProvider> {
        @Override
        public void serialize(JsonObject jsonObject, FixedScoreboardNameProvider fixedScoreboardNameProvider, JsonSerializationContext jsonSerializationContext) {
            jsonObject.addProperty("name", fixedScoreboardNameProvider.name);
        }

        @Override
        public FixedScoreboardNameProvider deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            String string = GsonHelper.getAsString(jsonObject, "name");
            return new FixedScoreboardNameProvider(string);
        }

        @Override
        public /* synthetic */ Object deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return this.deserialize(jsonObject, jsonDeserializationContext);
        }
    }
}

