/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class FishingHookPredicate
implements EntitySubPredicate {
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
        JsonElement jsonElement = jsonObject.get(IN_OPEN_WATER_KEY);
        if (jsonElement != null) {
            return new FishingHookPredicate(GsonHelper.convertToBoolean(jsonElement, IN_OPEN_WATER_KEY));
        }
        return ANY;
    }

    @Override
    public JsonObject serializeCustomData() {
        if (this == ANY) {
            return new JsonObject();
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(IN_OPEN_WATER_KEY, new JsonPrimitive(this.inOpenWater));
        return jsonObject;
    }

    @Override
    public EntitySubPredicate.Type type() {
        return EntitySubPredicate.Types.FISHING_HOOK;
    }

    @Override
    public boolean matches(Entity entity, ServerLevel serverLevel, @Nullable Vec3 vec3) {
        if (this == ANY) {
            return true;
        }
        if (!(entity instanceof FishingHook)) {
            return false;
        }
        FishingHook fishingHook = (FishingHook)entity;
        return this.inOpenWater == fishingHook.isOpenWaterFishing();
    }
}

