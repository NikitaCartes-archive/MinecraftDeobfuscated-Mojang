/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.providers.number;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import net.minecraft.world.level.storage.loot.providers.score.ContextScoreboardNameProvider;
import net.minecraft.world.level.storage.loot.providers.score.ScoreboardNameProvider;
import net.minecraft.world.scores.Objective;

public class ScoreboardValue
implements NumberProvider {
    final ScoreboardNameProvider target;
    final String score;
    final float scale;

    ScoreboardValue(ScoreboardNameProvider scoreboardNameProvider, String string, float f) {
        this.target = scoreboardNameProvider;
        this.score = string;
        this.scale = f;
    }

    @Override
    public LootNumberProviderType getType() {
        return NumberProviders.SCORE;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.target.getReferencedContextParams();
    }

    public static ScoreboardValue fromScoreboard(LootContext.EntityTarget entityTarget, String string) {
        return ScoreboardValue.fromScoreboard(entityTarget, string, 1.0f);
    }

    public static ScoreboardValue fromScoreboard(LootContext.EntityTarget entityTarget, String string, float f) {
        return new ScoreboardValue(ContextScoreboardNameProvider.forTarget(entityTarget), string, f);
    }

    @Override
    public float getFloat(LootContext lootContext) {
        String string = this.target.getScoreboardName(lootContext);
        if (string == null) {
            return 0.0f;
        }
        ServerScoreboard scoreboard = lootContext.getLevel().getScoreboard();
        Objective objective = scoreboard.getObjective(this.score);
        if (objective == null) {
            return 0.0f;
        }
        if (!scoreboard.hasPlayerScore(string, objective)) {
            return 0.0f;
        }
        return (float)scoreboard.getOrCreatePlayerScore(string, objective).getScore() * this.scale;
    }

    public static class Serializer
    implements net.minecraft.world.level.storage.loot.Serializer<ScoreboardValue> {
        @Override
        public ScoreboardValue deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            String string = GsonHelper.getAsString(jsonObject, "score");
            float f = GsonHelper.getAsFloat(jsonObject, "scale", 1.0f);
            ScoreboardNameProvider scoreboardNameProvider = GsonHelper.getAsObject(jsonObject, "target", jsonDeserializationContext, ScoreboardNameProvider.class);
            return new ScoreboardValue(scoreboardNameProvider, string, f);
        }

        @Override
        public void serialize(JsonObject jsonObject, ScoreboardValue scoreboardValue, JsonSerializationContext jsonSerializationContext) {
            jsonObject.addProperty("score", scoreboardValue.score);
            jsonObject.add("target", jsonSerializationContext.serialize(scoreboardValue.target));
            jsonObject.addProperty("scale", Float.valueOf(scoreboardValue.scale));
        }

        @Override
        public /* synthetic */ Object deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return this.deserialize(jsonObject, jsonDeserializationContext);
        }
    }
}

