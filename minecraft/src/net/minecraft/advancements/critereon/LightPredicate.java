package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;

public class LightPredicate {
	public static final LightPredicate ANY = new LightPredicate(MinMaxBounds.Ints.ANY);
	private final MinMaxBounds.Ints composite;

	private LightPredicate(MinMaxBounds.Ints ints) {
		this.composite = ints;
	}

	public boolean matches(ServerLevel serverLevel, BlockPos blockPos) {
		return this.composite.matches(serverLevel.getMaxLocalRawBrightness(blockPos));
	}

	public JsonElement serializeToJson() {
		if (this == ANY) {
			return JsonNull.INSTANCE;
		} else {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("light", this.composite.serializeToJson());
			return jsonObject;
		}
	}

	public static LightPredicate fromJson(@Nullable JsonElement jsonElement) {
		if (jsonElement != null && !jsonElement.isJsonNull()) {
			JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "light");
			MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("light"));
			return new LightPredicate(ints);
		} else {
			return ANY;
		}
	}
}
