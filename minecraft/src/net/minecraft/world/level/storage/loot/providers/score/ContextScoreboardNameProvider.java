package net.minecraft.world.level.storage.loot.providers.score;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.GsonAdapterFactory;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public class ContextScoreboardNameProvider implements ScoreboardNameProvider {
	final LootContext.EntityTarget target;

	ContextScoreboardNameProvider(LootContext.EntityTarget entityTarget) {
		this.target = entityTarget;
	}

	public static ScoreboardNameProvider forTarget(LootContext.EntityTarget entityTarget) {
		return new ContextScoreboardNameProvider(entityTarget);
	}

	@Override
	public LootScoreProviderType getType() {
		return ScoreboardNameProviders.CONTEXT;
	}

	@Nullable
	@Override
	public String getScoreboardName(LootContext lootContext) {
		Entity entity = lootContext.getParamOrNull(this.target.getParam());
		return entity != null ? entity.getScoreboardName() : null;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return ImmutableSet.of(this.target.getParam());
	}

	public static class InlineSerializer implements GsonAdapterFactory.InlineSerializer<ContextScoreboardNameProvider> {
		public JsonElement serialize(ContextScoreboardNameProvider contextScoreboardNameProvider, JsonSerializationContext jsonSerializationContext) {
			return jsonSerializationContext.serialize(contextScoreboardNameProvider.target);
		}

		public ContextScoreboardNameProvider deserialize(JsonElement jsonElement, JsonDeserializationContext jsonDeserializationContext) {
			LootContext.EntityTarget entityTarget = jsonDeserializationContext.deserialize(jsonElement, LootContext.EntityTarget.class);
			return new ContextScoreboardNameProvider(entityTarget);
		}
	}

	public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ContextScoreboardNameProvider> {
		public void serialize(JsonObject jsonObject, ContextScoreboardNameProvider contextScoreboardNameProvider, JsonSerializationContext jsonSerializationContext) {
			jsonObject.addProperty("target", contextScoreboardNameProvider.target.name());
		}

		public ContextScoreboardNameProvider deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
			LootContext.EntityTarget entityTarget = GsonHelper.getAsObject(jsonObject, "target", jsonDeserializationContext, LootContext.EntityTarget.class);
			return new ContextScoreboardNameProvider(entityTarget);
		}
	}
}
