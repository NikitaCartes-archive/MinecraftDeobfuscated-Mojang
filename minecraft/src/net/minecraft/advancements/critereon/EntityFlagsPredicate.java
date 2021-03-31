package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class EntityFlagsPredicate {
	public static final EntityFlagsPredicate ANY = new EntityFlagsPredicate.Builder().build();
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

	public EntityFlagsPredicate(
		@Nullable Boolean boolean_, @Nullable Boolean boolean2, @Nullable Boolean boolean3, @Nullable Boolean boolean4, @Nullable Boolean boolean5
	) {
		this.isOnFire = boolean_;
		this.isCrouching = boolean2;
		this.isSprinting = boolean3;
		this.isSwimming = boolean4;
		this.isBaby = boolean5;
	}

	public boolean matches(Entity entity) {
		if (this.isOnFire != null && entity.isOnFire() != this.isOnFire) {
			return false;
		} else if (this.isCrouching != null && entity.isCrouching() != this.isCrouching) {
			return false;
		} else if (this.isSprinting != null && entity.isSprinting() != this.isSprinting) {
			return false;
		} else {
			return this.isSwimming != null && entity.isSwimming() != this.isSwimming
				? false
				: this.isBaby == null || !(entity instanceof LivingEntity) || ((LivingEntity)entity).isBaby() == this.isBaby;
		}
	}

	@Nullable
	private static Boolean getOptionalBoolean(JsonObject jsonObject, String string) {
		return jsonObject.has(string) ? GsonHelper.getAsBoolean(jsonObject, string) : null;
	}

	public static EntityFlagsPredicate fromJson(@Nullable JsonElement jsonElement) {
		if (jsonElement != null && !jsonElement.isJsonNull()) {
			JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "entity flags");
			Boolean boolean_ = getOptionalBoolean(jsonObject, "is_on_fire");
			Boolean boolean2 = getOptionalBoolean(jsonObject, "is_sneaking");
			Boolean boolean3 = getOptionalBoolean(jsonObject, "is_sprinting");
			Boolean boolean4 = getOptionalBoolean(jsonObject, "is_swimming");
			Boolean boolean5 = getOptionalBoolean(jsonObject, "is_baby");
			return new EntityFlagsPredicate(boolean_, boolean2, boolean3, boolean4, boolean5);
		} else {
			return ANY;
		}
	}

	private void addOptionalBoolean(JsonObject jsonObject, String string, @Nullable Boolean boolean_) {
		if (boolean_ != null) {
			jsonObject.addProperty(string, boolean_);
		}
	}

	public JsonElement serializeToJson() {
		if (this == ANY) {
			return JsonNull.INSTANCE;
		} else {
			JsonObject jsonObject = new JsonObject();
			this.addOptionalBoolean(jsonObject, "is_on_fire", this.isOnFire);
			this.addOptionalBoolean(jsonObject, "is_sneaking", this.isCrouching);
			this.addOptionalBoolean(jsonObject, "is_sprinting", this.isSprinting);
			this.addOptionalBoolean(jsonObject, "is_swimming", this.isSwimming);
			this.addOptionalBoolean(jsonObject, "is_baby", this.isBaby);
			return jsonObject;
		}
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

		public static EntityFlagsPredicate.Builder flags() {
			return new EntityFlagsPredicate.Builder();
		}

		public EntityFlagsPredicate.Builder setOnFire(@Nullable Boolean boolean_) {
			this.isOnFire = boolean_;
			return this;
		}

		public EntityFlagsPredicate.Builder setCrouching(@Nullable Boolean boolean_) {
			this.isCrouching = boolean_;
			return this;
		}

		public EntityFlagsPredicate.Builder setSprinting(@Nullable Boolean boolean_) {
			this.isSprinting = boolean_;
			return this;
		}

		public EntityFlagsPredicate.Builder setSwimming(@Nullable Boolean boolean_) {
			this.isSwimming = boolean_;
			return this;
		}

		public EntityFlagsPredicate.Builder setIsBaby(@Nullable Boolean boolean_) {
			this.isBaby = boolean_;
			return this;
		}

		public EntityFlagsPredicate build() {
			return new EntityFlagsPredicate(this.isOnFire, this.isCrouching, this.isSprinting, this.isSwimming, this.isBaby);
		}
	}
}
