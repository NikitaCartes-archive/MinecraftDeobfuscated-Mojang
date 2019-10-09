/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class DamageSourcePredicate {
    public static final DamageSourcePredicate ANY = Builder.damageType().build();
    private final Boolean isProjectile;
    private final Boolean isExplosion;
    private final Boolean bypassesArmor;
    private final Boolean bypassesInvulnerability;
    private final Boolean bypassesMagic;
    private final Boolean isFire;
    private final Boolean isMagic;
    private final Boolean isLightning;
    private final EntityPredicate directEntity;
    private final EntityPredicate sourceEntity;

    public DamageSourcePredicate(@Nullable Boolean boolean_, @Nullable Boolean boolean2, @Nullable Boolean boolean3, @Nullable Boolean boolean4, @Nullable Boolean boolean5, @Nullable Boolean boolean6, @Nullable Boolean boolean7, @Nullable Boolean boolean8, EntityPredicate entityPredicate, EntityPredicate entityPredicate2) {
        this.isProjectile = boolean_;
        this.isExplosion = boolean2;
        this.bypassesArmor = boolean3;
        this.bypassesInvulnerability = boolean4;
        this.bypassesMagic = boolean5;
        this.isFire = boolean6;
        this.isMagic = boolean7;
        this.isLightning = boolean8;
        this.directEntity = entityPredicate;
        this.sourceEntity = entityPredicate2;
    }

    public boolean matches(ServerPlayer serverPlayer, DamageSource damageSource) {
        return this.matches(serverPlayer.getLevel(), serverPlayer.position(), damageSource);
    }

    public boolean matches(ServerLevel serverLevel, Vec3 vec3, DamageSource damageSource) {
        if (this == ANY) {
            return true;
        }
        if (this.isProjectile != null && this.isProjectile.booleanValue() != damageSource.isProjectile()) {
            return false;
        }
        if (this.isExplosion != null && this.isExplosion.booleanValue() != damageSource.isExplosion()) {
            return false;
        }
        if (this.bypassesArmor != null && this.bypassesArmor.booleanValue() != damageSource.isBypassArmor()) {
            return false;
        }
        if (this.bypassesInvulnerability != null && this.bypassesInvulnerability.booleanValue() != damageSource.isBypassInvul()) {
            return false;
        }
        if (this.bypassesMagic != null && this.bypassesMagic.booleanValue() != damageSource.isBypassMagic()) {
            return false;
        }
        if (this.isFire != null && this.isFire.booleanValue() != damageSource.isFire()) {
            return false;
        }
        if (this.isMagic != null && this.isMagic.booleanValue() != damageSource.isMagic()) {
            return false;
        }
        if (this.isLightning != null && this.isLightning != (damageSource == DamageSource.LIGHTNING_BOLT)) {
            return false;
        }
        if (!this.directEntity.matches(serverLevel, vec3, damageSource.getDirectEntity())) {
            return false;
        }
        return this.sourceEntity.matches(serverLevel, vec3, damageSource.getEntity());
    }

    public static DamageSourcePredicate fromJson(@Nullable JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return ANY;
        }
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "damage type");
        Boolean boolean_ = DamageSourcePredicate.getOptionalBoolean(jsonObject, "is_projectile");
        Boolean boolean2 = DamageSourcePredicate.getOptionalBoolean(jsonObject, "is_explosion");
        Boolean boolean3 = DamageSourcePredicate.getOptionalBoolean(jsonObject, "bypasses_armor");
        Boolean boolean4 = DamageSourcePredicate.getOptionalBoolean(jsonObject, "bypasses_invulnerability");
        Boolean boolean5 = DamageSourcePredicate.getOptionalBoolean(jsonObject, "bypasses_magic");
        Boolean boolean6 = DamageSourcePredicate.getOptionalBoolean(jsonObject, "is_fire");
        Boolean boolean7 = DamageSourcePredicate.getOptionalBoolean(jsonObject, "is_magic");
        Boolean boolean8 = DamageSourcePredicate.getOptionalBoolean(jsonObject, "is_lightning");
        EntityPredicate entityPredicate = EntityPredicate.fromJson(jsonObject.get("direct_entity"));
        EntityPredicate entityPredicate2 = EntityPredicate.fromJson(jsonObject.get("source_entity"));
        return new DamageSourcePredicate(boolean_, boolean2, boolean3, boolean4, boolean5, boolean6, boolean7, boolean8, entityPredicate, entityPredicate2);
    }

    @Nullable
    private static Boolean getOptionalBoolean(JsonObject jsonObject, String string) {
        return jsonObject.has(string) ? Boolean.valueOf(GsonHelper.getAsBoolean(jsonObject, string)) : null;
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        }
        JsonObject jsonObject = new JsonObject();
        this.addOptionally(jsonObject, "is_projectile", this.isProjectile);
        this.addOptionally(jsonObject, "is_explosion", this.isExplosion);
        this.addOptionally(jsonObject, "bypasses_armor", this.bypassesArmor);
        this.addOptionally(jsonObject, "bypasses_invulnerability", this.bypassesInvulnerability);
        this.addOptionally(jsonObject, "bypasses_magic", this.bypassesMagic);
        this.addOptionally(jsonObject, "is_fire", this.isFire);
        this.addOptionally(jsonObject, "is_magic", this.isMagic);
        this.addOptionally(jsonObject, "is_lightning", this.isLightning);
        jsonObject.add("direct_entity", this.directEntity.serializeToJson());
        jsonObject.add("source_entity", this.sourceEntity.serializeToJson());
        return jsonObject;
    }

    private void addOptionally(JsonObject jsonObject, String string, @Nullable Boolean boolean_) {
        if (boolean_ != null) {
            jsonObject.addProperty(string, boolean_);
        }
    }

    public static class Builder {
        private Boolean isProjectile;
        private Boolean isExplosion;
        private Boolean bypassesArmor;
        private Boolean bypassesInvulnerability;
        private Boolean bypassesMagic;
        private Boolean isFire;
        private Boolean isMagic;
        private Boolean isLightning;
        private EntityPredicate directEntity = EntityPredicate.ANY;
        private EntityPredicate sourceEntity = EntityPredicate.ANY;

        public static Builder damageType() {
            return new Builder();
        }

        public Builder isProjectile(Boolean boolean_) {
            this.isProjectile = boolean_;
            return this;
        }

        public Builder isLightning(Boolean boolean_) {
            this.isLightning = boolean_;
            return this;
        }

        public Builder direct(EntityPredicate.Builder builder) {
            this.directEntity = builder.build();
            return this;
        }

        public DamageSourcePredicate build() {
            return new DamageSourcePredicate(this.isProjectile, this.isExplosion, this.bypassesArmor, this.bypassesInvulnerability, this.bypassesMagic, this.isFire, this.isMagic, this.isLightning, this.directEntity, this.sourceEntity);
        }
    }
}

