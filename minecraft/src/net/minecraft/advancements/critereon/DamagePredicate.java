package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;

public class DamagePredicate {
	public static final DamagePredicate ANY = DamagePredicate.Builder.damageInstance().build();
	private final MinMaxBounds.Doubles dealtDamage;
	private final MinMaxBounds.Doubles takenDamage;
	private final EntityPredicate sourceEntity;
	@Nullable
	private final Boolean blocked;
	private final DamageSourcePredicate type;

	public DamagePredicate() {
		this.dealtDamage = MinMaxBounds.Doubles.ANY;
		this.takenDamage = MinMaxBounds.Doubles.ANY;
		this.sourceEntity = EntityPredicate.ANY;
		this.blocked = null;
		this.type = DamageSourcePredicate.ANY;
	}

	public DamagePredicate(
		MinMaxBounds.Doubles doubles,
		MinMaxBounds.Doubles doubles2,
		EntityPredicate entityPredicate,
		@Nullable Boolean boolean_,
		DamageSourcePredicate damageSourcePredicate
	) {
		this.dealtDamage = doubles;
		this.takenDamage = doubles2;
		this.sourceEntity = entityPredicate;
		this.blocked = boolean_;
		this.type = damageSourcePredicate;
	}

	public boolean matches(ServerPlayer serverPlayer, DamageSource damageSource, float f, float g, boolean bl) {
		if (this == ANY) {
			return true;
		} else if (!this.dealtDamage.matches((double)f)) {
			return false;
		} else if (!this.takenDamage.matches((double)g)) {
			return false;
		} else if (!this.sourceEntity.matches(serverPlayer, damageSource.getEntity())) {
			return false;
		} else {
			return this.blocked != null && this.blocked != bl ? false : this.type.matches(serverPlayer, damageSource);
		}
	}

	public static DamagePredicate fromJson(@Nullable JsonElement jsonElement) {
		if (jsonElement != null && !jsonElement.isJsonNull()) {
			JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "damage");
			MinMaxBounds.Doubles doubles = MinMaxBounds.Doubles.fromJson(jsonObject.get("dealt"));
			MinMaxBounds.Doubles doubles2 = MinMaxBounds.Doubles.fromJson(jsonObject.get("taken"));
			Boolean boolean_ = jsonObject.has("blocked") ? GsonHelper.getAsBoolean(jsonObject, "blocked") : null;
			EntityPredicate entityPredicate = EntityPredicate.fromJson(jsonObject.get("source_entity"));
			DamageSourcePredicate damageSourcePredicate = DamageSourcePredicate.fromJson(jsonObject.get("type"));
			return new DamagePredicate(doubles, doubles2, entityPredicate, boolean_, damageSourcePredicate);
		} else {
			return ANY;
		}
	}

	public JsonElement serializeToJson() {
		if (this == ANY) {
			return JsonNull.INSTANCE;
		} else {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("dealt", this.dealtDamage.serializeToJson());
			jsonObject.add("taken", this.takenDamage.serializeToJson());
			jsonObject.add("source_entity", this.sourceEntity.serializeToJson());
			jsonObject.add("type", this.type.serializeToJson());
			if (this.blocked != null) {
				jsonObject.addProperty("blocked", this.blocked);
			}

			return jsonObject;
		}
	}

	public static class Builder {
		private MinMaxBounds.Doubles dealtDamage = MinMaxBounds.Doubles.ANY;
		private MinMaxBounds.Doubles takenDamage = MinMaxBounds.Doubles.ANY;
		private EntityPredicate sourceEntity = EntityPredicate.ANY;
		@Nullable
		private Boolean blocked;
		private DamageSourcePredicate type = DamageSourcePredicate.ANY;

		public static DamagePredicate.Builder damageInstance() {
			return new DamagePredicate.Builder();
		}

		public DamagePredicate.Builder dealtDamage(MinMaxBounds.Doubles doubles) {
			this.dealtDamage = doubles;
			return this;
		}

		public DamagePredicate.Builder takenDamage(MinMaxBounds.Doubles doubles) {
			this.takenDamage = doubles;
			return this;
		}

		public DamagePredicate.Builder sourceEntity(EntityPredicate entityPredicate) {
			this.sourceEntity = entityPredicate;
			return this;
		}

		public DamagePredicate.Builder blocked(Boolean boolean_) {
			this.blocked = boolean_;
			return this;
		}

		public DamagePredicate.Builder type(DamageSourcePredicate damageSourcePredicate) {
			this.type = damageSourcePredicate;
			return this;
		}

		public DamagePredicate.Builder type(DamageSourcePredicate.Builder builder) {
			this.type = builder.build();
			return this;
		}

		public DamagePredicate build() {
			return new DamagePredicate(this.dealtDamage, this.takenDamage, this.sourceEntity, this.blocked, this.type);
		}
	}
}
