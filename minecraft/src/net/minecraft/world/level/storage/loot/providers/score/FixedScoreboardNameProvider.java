package net.minecraft.world.level.storage.loot.providers.score;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public class FixedScoreboardNameProvider implements ScoreboardNameProvider {
	private final String name;

	private FixedScoreboardNameProvider(String string) {
		this.name = string;
	}

	public static ScoreboardNameProvider forName(String string) {
		return new FixedScoreboardNameProvider(string);
	}

	@Override
	public LootScoreProviderType getType() {
		return ScoreboardNameProviders.FIXED;
	}

	public String getName() {
		return this.name;
	}

	@Nullable
	@Override
	public String getScoreboardName(LootContext lootContext) {
		return this.name;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return ImmutableSet.of();
	}

	public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<FixedScoreboardNameProvider> {
		public void serialize(JsonObject jsonObject, FixedScoreboardNameProvider fixedScoreboardNameProvider, JsonSerializationContext jsonSerializationContext) {
			jsonObject.addProperty("name", fixedScoreboardNameProvider.name);
		}

		public FixedScoreboardNameProvider deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
			String string = GsonHelper.getAsString(jsonObject, "name");
			return new FixedScoreboardNameProvider(string);
		}
	}
}
