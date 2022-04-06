package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.phys.Vec3;

public class FishingHookPredicate implements EntitySubPredicate {
	public static final FishingHookPredicate ANY = new FishingHookPredicate(false);
	private static final String IN_OPEN_WATER_KEY = "in_open_water";
	private final boolean inOpenWater;

	private FishingHookPredicate(boolean bl) {
		this.inOpenWater = bl;
	}

	public static FishingHookPredicate inOpenWater(boolean bl) {
		return new FishingHookPredicate(bl);
	}

	public static FishingHookPredicate fromJson(JsonObject jsonObject) {
		JsonElement jsonElement = jsonObject.get("in_open_water");
		return jsonElement != null ? new FishingHookPredicate(GsonHelper.convertToBoolean(jsonElement, "in_open_water")) : ANY;
	}

	@Override
	public JsonObject serializeCustomData() {
		if (this == ANY) {
			return new JsonObject();
		} else {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("in_open_water", new JsonPrimitive(this.inOpenWater));
			return jsonObject;
		}
	}

	@Override
	public EntitySubPredicate.Type type() {
		return EntitySubPredicate.Types.FISHING_HOOK;
	}

	@Override
	public boolean matches(Entity entity, ServerLevel serverLevel, @Nullable Vec3 vec3) {
		if (this == ANY) {
			return true;
		} else {
			return !(entity instanceof FishingHook fishingHook) ? false : this.inOpenWater == fishingHook.isOpenWaterFishing();
		}
	}
}
