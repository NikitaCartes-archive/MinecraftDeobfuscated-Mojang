/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class EntityFlagsPredicate {
    public static final EntityFlagsPredicate ANY = new Builder().build();
    @Nullable
    private final Boolean isOnFire;
    @Nullable
    private final Boolean isCrouching;
    @Nullable
    private final Boolean isSprinting;
    @Nullable
    private final Boolean isSwimming;
    @Nullable
    private final Boolean isBaby;

    public EntityFlagsPredicate(@Nullable Boolean boolean_, @Nullable Boolean boolean2, @Nullable Boolean boolean3, @Nullable Boolean boolean4, @Nullable Boolean boolean5) {
        this.isOnFire = boolean_;
        this.isCrouching = boolean2;
        this.isSprinting = boolean3;
        this.isSwimming = boolean4;
        this.isBaby = boolean5;
    }

    public boolean matches(Entity entity) {
        if (this.isOnFire != null && entity.isOnFire() != this.isOnFire.booleanValue()) {
            return false;
        }
        if (this.isCrouching != null && entity.isCrouching() != this.isCrouching.booleanValue()) {
            return false;
        }
        if (this.isSprinting != null && entity.isSprinting() != this.isSprinting.booleanValue()) {
            return false;
        }
        if (this.isSwimming != null && entity.isSwimming() != this.isSwimming.booleanValue()) {
            return false;
        }
        return this.isBaby == null || !(entity instanceof LivingEntity) || ((LivingEntity)entity).isBaby() == this.isBaby.booleanValue();
    }

    @Nullable
    private static Boolean getOptionalBoolean(JsonObject jsonObject, String string) {
        return jsonObject.has(string) ? Boolean.valueOf(GsonHelper.getAsBoolean(jsonObject, string)) : null;
    }

    public static EntityFlagsPredicate fromJson(@Nullable JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return ANY;
        }
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "entity flags");
        Boolean boolean_ = EntityFlagsPredicate.getOptionalBoolean(jsonObject, "is_on_fire");
        Boolean boolean2 = EntityFlagsPredicate.getOptionalBoolean(jsonObject, "is_sneaking");
        Boolean boolean3 = EntityFlagsPredicate.getOptionalBoolean(jsonObject, "is_sprinting");
        Boolean boolean4 = EntityFlagsPredicate.getOptionalBoolean(jsonObject, "is_swimming");
        Boolean boolean5 = EntityFlagsPredicate.getOptionalBoolean(jsonObject, "is_baby");
        return new EntityFlagsPredicate(boolean_, boolean2, boolean3, boolean4, boolean5);
    }

    private void addOptionalBoolean(JsonObject jsonObject, String string, @Nullable Boolean boolean_) {
        if (boolean_ != null) {
            jsonObject.addProperty(string, boolean_);
        }
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        }
        JsonObject jsonObject = new JsonObject();
        this.addOptionalBoolean(jsonObject, "is_on_fire", this.isOnFire);
        this.addOptionalBoolean(jsonObject, "is_sneaking", this.isCrouching);
        this.addOptionalBoolean(jsonObject, "is_sprinting", this.isSprinting);
        this.addOptionalBoolean(jsonObject, "is_swimming", this.isSwimming);
        this.addOptionalBoolean(jsonObject, "is_baby", this.isBaby);
        return jsonObject;
    }

    public static class Builder {
        @Nullable
        private Boolean isOnFire;
        @Nullable
        private Boolean isCrouching;
        @Nullable
        private Boolean isSprinting;
        @Nullable
        private Boolean isSwimming;
        @Nullable
        private Boolean isBaby;

        public static Builder flags() {
            return new Builder();
        }

        public Builder setOnFire(@Nullable Boolean boolean_) {
            this.isOnFire = boolean_;
            return this;
        }

        public Builder setIsBaby(@Nullable Boolean boolean_) {
            this.isBaby = boolean_;
            return this;
        }

        public EntityFlagsPredicate build() {
            return new EntityFlagsPredicate(this.isOnFire, this.isCrouching, this.isSprinting, this.isSwimming, this.isBaby);
        }
    }
}

