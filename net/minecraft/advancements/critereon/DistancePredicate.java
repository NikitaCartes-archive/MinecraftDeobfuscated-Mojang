/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class DistancePredicate {
    public static final DistancePredicate ANY = new DistancePredicate(MinMaxBounds.Floats.ANY, MinMaxBounds.Floats.ANY, MinMaxBounds.Floats.ANY, MinMaxBounds.Floats.ANY, MinMaxBounds.Floats.ANY);
    private final MinMaxBounds.Floats x;
    private final MinMaxBounds.Floats y;
    private final MinMaxBounds.Floats z;
    private final MinMaxBounds.Floats horizontal;
    private final MinMaxBounds.Floats absolute;

    public DistancePredicate(MinMaxBounds.Floats floats, MinMaxBounds.Floats floats2, MinMaxBounds.Floats floats3, MinMaxBounds.Floats floats4, MinMaxBounds.Floats floats5) {
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
        if (!(this.x.matches(Mth.abs(j)) && this.y.matches(Mth.abs(k)) && this.z.matches(Mth.abs(l)))) {
            return false;
        }
        if (!this.horizontal.matchesSqr(j * j + l * l)) {
            return false;
        }
        return this.absolute.matchesSqr(j * j + k * k + l * l);
    }

    public static DistancePredicate fromJson(@Nullable JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return ANY;
        }
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "distance");
        MinMaxBounds.Floats floats = MinMaxBounds.Floats.fromJson(jsonObject.get("x"));
        MinMaxBounds.Floats floats2 = MinMaxBounds.Floats.fromJson(jsonObject.get("y"));
        MinMaxBounds.Floats floats3 = MinMaxBounds.Floats.fromJson(jsonObject.get("z"));
        MinMaxBounds.Floats floats4 = MinMaxBounds.Floats.fromJson(jsonObject.get("horizontal"));
        MinMaxBounds.Floats floats5 = MinMaxBounds.Floats.fromJson(jsonObject.get("absolute"));
        return new DistancePredicate(floats, floats2, floats3, floats4, floats5);
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("x", this.x.serializeToJson());
        jsonObject.add("y", this.y.serializeToJson());
        jsonObject.add("z", this.z.serializeToJson());
        jsonObject.add("horizontal", this.horizontal.serializeToJson());
        jsonObject.add("absolute", this.absolute.serializeToJson());
        return jsonObject;
    }
}

