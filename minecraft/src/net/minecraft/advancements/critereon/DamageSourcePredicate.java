package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;

public class DamageSourcePredicate {
	public static final DamageSourcePredicate ANY = DamageSourcePredicate.Builder.damageType().build();
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

	public DamageSourcePredicate(
		@Nullable Boolean boolean_,
		@Nullable Boolean boolean2,
		@Nullable Boolean boolean3,
		@Nullable Boolean boolean4,
		@Nullable Boolean boolean5,
		@Nullable Boolean boolean6,
		@Nullable Boolean boolean7,
		@Nullable Boolean boolean8,
		EntityPredicate entityPredicate,
		EntityPredicate entityPredicate2
	) {
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
		} else if (this.isProjectile != null && this.isProjectile != damageSource.isProjectile()) {
			return false;
		} else if (this.isExplosion != null && this.isExplosion != damageSource.isExplosion()) {
			return false;
		} else if (this.bypassesArmor != null && this.bypassesArmor != damageSource.isBypassArmor()) {
			return false;
		} else if (this.bypassesInvulnerability != null && this.bypassesInvulnerability != damageSource.isBypassInvul()) {
			return false;
		} else if (this.bypassesMagic != null && this.bypassesMagic != damageSource.isBypassMagic()) {
			return false;
		} else if (this.isFire != null && this.isFire != damageSource.isFire()) {
			return false;
		} else if (this.isMagic != null && this.isMagic != damageSource.isMagic()) {
			return false;
		} else if (this.isLightning != null && this.isLightning != (damageSource == DamageSource.LIGHTNING_BOLT)) {
			return false;
		} else {
			return !this.directEntity.matches(serverLevel, vec3, damageSource.getDirectEntity())
				? false
				: this.sourceEntity.matches(serverLevel, vec3, damageSource.getEntity());
		}
	}

	public static DamageSourcePredicate fromJson(@Nullable JsonElement jsonElement) {
		if (jsonElement != null && !jsonElement.isJsonNull()) {
			JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "damage type");
			Boolean boolean_ = getOptionalBoolean(jsonObject, "is_projectile");
			Boolean boolean2 = getOptionalBoolean(jsonObject, "is_explosion");
			Boolean boolean3 = getOptionalBoolean(jsonObject, "bypasses_armor");
			Boolean boolean4 = getOptionalBoolean(jsonObject, "bypasses_invulnerability");
			Boolean boolean5 = getOptionalBoolean(jsonObject, "bypasses_magic");
			Boolean boolean6 = getOptionalBoolean(jsonObject, "is_fire");
			Boolean boolean7 = getOptionalBoolean(jsonObject, "is_magic");
			Boolean boolean8 = getOptionalBoolean(jsonObject, "is_lightning");
			EntityPredicate entityPredicate = EntityPredicate.fromJson(jsonObject.get("direct_entity"));
			EntityPredicate entityPredicate2 = EntityPredicate.fromJson(jsonObject.get("source_entity"));
			return new DamageSourcePredicate(boolean_, boolean2, boolean3, boolean4, boolean5, boolean6, boolean7, boolean8, entityPredicate, entityPredicate2);
		} else {
			return ANY;
		}
	}

	@Nullable
	private static Boolean getOptionalBoolean(JsonObject jsonObject, String string) {
		return jsonObject.has(string) ? GsonHelper.getAsBoolean(jsonObject, string) : null;
	}

	public JsonElement serializeToJson() {
		if (this == ANY) {
			return JsonNull.INSTANCE;
		} else {
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

		public static DamageSourcePredicate.Builder damageType() {
			return new DamageSourcePredicate.Builder();
		}

		public DamageSourcePredicate.Builder isProjectile(Boolean boolean_) {
			this.isProjectile = boolean_;
			return this;
		}

		public DamageSourcePredicate.Builder isExplosion(Boolean boolean_) {
			this.isExplosion = boolean_;
			return this;
		}

		public DamageSourcePredicate.Builder bypassesArmor(Boolean boolean_) {
			this.bypassesArmor = boolean_;
			return this;
		}

		public DamageSourcePredicate.Builder bypassesInvulnerability(Boolean boolean_) {
			this.bypassesInvulnerability = boolean_;
			return this;
		}

		public DamageSourcePredicate.Builder bypassesMagic(Boolean boolean_) {
			this.bypassesMagic = boolean_;
			return this;
		}

		public DamageSourcePredicate.Builder isFire(Boolean boolean_) {
			this.isFire = boolean_;
			return this;
		}

		public DamageSourcePredicate.Builder isMagic(Boolean boolean_) {
			this.isMagic = boolean_;
			return this;
		}

		public DamageSourcePredicate.Builder isLightning(Boolean boolean_) {
			this.isLightning = boolean_;
			return this;
		}

		public DamageSourcePredicate.Builder direct(EntityPredicate entityPredicate) {
			this.directEntity = entityPredicate;
			return this;
		}

		public DamageSourcePredicate.Builder direct(EntityPredicate.Builder builder) {
			this.directEntity = builder.build();
			return this;
		}

		public DamageSourcePredicate.Builder source(EntityPredicate entityPredicate) {
			this.sourceEntity = entityPredicate;
			return this;
		}

		public DamageSourcePredicate.Builder source(EntityPredicate.Builder builder) {
			this.sourceEntity = builder.build();
			return this;
		}

		public DamageSourcePredicate build() {
			return new DamageSourcePredicate(
				this.isProjectile,
				this.isExplosion,
				this.bypassesArmor,
				this.bypassesInvulnerability,
				this.bypassesMagic,
				this.isFire,
				this.isMagic,
				this.isLightning,
				this.directEntity,
				this.sourceEntity
			);
		}
	}
}
