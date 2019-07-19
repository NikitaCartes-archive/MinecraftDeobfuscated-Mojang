package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;

public class DistancePredicate {
	public static final DistancePredicate ANY = new DistancePredicate(
		MinMaxBounds.Floats.ANY, MinMaxBounds.Floats.ANY, MinMaxBounds.Floats.ANY, MinMaxBounds.Floats.ANY, MinMaxBounds.Floats.ANY
	);
	private final MinMaxBounds.Floats x;
	private final MinMaxBounds.Floats y;
	private final MinMaxBounds.Floats z;
	private final MinMaxBounds.Floats horizontal;
	private final MinMaxBounds.Floats absolute;

	public DistancePredicate(
		MinMaxBounds.Floats floats, MinMaxBounds.Floats floats2, MinMaxBounds.Floats floats3, MinMaxBounds.Floats floats4, MinMaxBounds.Floats floats5
	) {
		this.x = floats;
		this.y = floats2;
		this.z = floats3;
		this.horizontal = floats4;
		this.absolute = floats5;
	}

	public static DistancePredicate horizontal(MinMaxBounds.Floats floats) {
		return new DistancePredicate(MinMaxBounds.Floats.ANY, MinMaxBounds.Floats.ANY, MinMaxBounds.Floats.ANY, floats, MinMaxBounds.Floats.ANY);
	}

	public static DistancePredicate vertical(MinMaxBounds.Floats floats) {
		return new DistancePredicate(MinMaxBounds.Floats.ANY, floats, MinMaxBounds.Floats.ANY, MinMaxBounds.Floats.ANY, MinMaxBounds.Floats.ANY);
	}

	public boolean matches(double d, double e, double f, double g, double h, double i) {
		float j = (float)(d - g);
		float k = (float)(e - h);
		float l = (float)(f - i);
		if (!this.x.matches(Mth.abs(j)) || !this.y.matches(Mth.abs(k)) || !this.z.matches(Mth.abs(l))) {
			return false;
		} else {
			return !this.horizontal.matchesSqr((double)(j * j + l * l)) ? false : this.absolute.matchesSqr((double)(j * j + k * k + l * l));
		}
	}

	public static DistancePredicate fromJson(@Nullable JsonElement jsonElement) {
		if (jsonElement != null && !jsonElement.isJsonNull()) {
			JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "distance");
			MinMaxBounds.Floats floats = MinMaxBounds.Floats.fromJson(jsonObject.get("x"));
			MinMaxBounds.Floats floats2 = MinMaxBounds.Floats.fromJson(jsonObject.get("y"));
			MinMaxBounds.Floats floats3 = MinMaxBounds.Floats.fromJson(jsonObject.get("z"));
			MinMaxBounds.Floats floats4 = MinMaxBounds.Floats.fromJson(jsonObject.get("horizontal"));
			MinMaxBounds.Floats floats5 = MinMaxBounds.Floats.fromJson(jsonObject.get("absolute"));
			return new DistancePredicate(floats, floats2, floats3, floats4, floats5);
		} else {
			return ANY;
		}
	}

	public JsonElement serializeToJson() {
		if (this == ANY) {
			return JsonNull.INSTANCE;
		} else {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("x", this.x.serializeToJson());
			jsonObject.add("y", this.y.serializeToJson());
			jsonObject.add("z", this.z.serializeToJson());
			jsonObject.add("horizontal", this.horizontal.serializeToJson());
			jsonObject.add("absolute", this.absolute.serializeToJson());
			return jsonObject;
		}
	}
}
