package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;

public record DamagePredicate(
	MinMaxBounds.Doubles dealtDamage,
	MinMaxBounds.Doubles takenDamage,
	Optional<EntityPredicate> sourceEntity,
	Optional<Boolean> blocked,
	Optional<DamageSourcePredicate> type
) {
	static Optional<DamagePredicate> of(
		MinMaxBounds.Doubles doubles,
		MinMaxBounds.Doubles doubles2,
		Optional<EntityPredicate> optional,
		Optional<Boolean> optional2,
		Optional<DamageSourcePredicate> optional3
	) {
		return doubles.isAny() && doubles2.isAny() && optional.isEmpty() && optional2.isEmpty() && optional3.isEmpty()
			? Optional.empty()
			: Optional.of(new DamagePredicate(doubles, doubles2, optional, optional2, optional3));
	}

	public boolean matches(ServerPlayer serverPlayer, DamageSource damageSource, float f, float g, boolean bl) {
		if (!this.dealtDamage.matches((double)f)) {
			return false;
		} else if (!this.takenDamage.matches((double)g)) {
			return false;
		} else if (this.sourceEntity.isPresent() && !((EntityPredicate)this.sourceEntity.get()).matches(serverPlayer, damageSource.getEntity())) {
			return false;
		} else {
			return this.blocked.isPresent() && this.blocked.get() != bl
				? false
				: !this.type.isPresent() || ((DamageSourcePredicate)this.type.get()).matches(serverPlayer, damageSource);
		}
	}

	public static Optional<DamagePredicate> fromJson(@Nullable JsonElement jsonElement) {
		if (jsonElement != null && !jsonElement.isJsonNull()) {
			JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "damage");
			MinMaxBounds.Doubles doubles = MinMaxBounds.Doubles.fromJson(jsonObject.get("dealt"));
			MinMaxBounds.Doubles doubles2 = MinMaxBounds.Doubles.fromJson(jsonObject.get("taken"));
			Optional<Boolean> optional = jsonObject.has("blocked") ? Optional.of(GsonHelper.getAsBoolean(jsonObject, "blocked")) : Optional.empty();
			Optional<EntityPredicate> optional2 = EntityPredicate.fromJson(jsonObject.get("source_entity"));
			Optional<DamageSourcePredicate> optional3 = DamageSourcePredicate.fromJson(jsonObject.get("type"));
			return of(doubles, doubles2, optional2, optional, optional3);
		} else {
			return Optional.empty();
		}
	}

	public JsonElement serializeToJson() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.add("dealt", this.dealtDamage.serializeToJson());
		jsonObject.add("taken", this.takenDamage.serializeToJson());
		this.sourceEntity.ifPresent(entityPredicate -> jsonObject.add("source_entity", entityPredicate.serializeToJson()));
		this.type.ifPresent(damageSourcePredicate -> jsonObject.add("type", damageSourcePredicate.serializeToJson()));
		this.blocked.ifPresent(boolean_ -> jsonObject.addProperty("blocked", boolean_));
		return jsonObject;
	}

	public static class Builder {
		private MinMaxBounds.Doubles dealtDamage = MinMaxBounds.Doubles.ANY;
		private MinMaxBounds.Doubles takenDamage = MinMaxBounds.Doubles.ANY;
		private Optional<EntityPredicate> sourceEntity = Optional.empty();
		private Optional<Boolean> blocked = Optional.empty();
		private Optional<DamageSourcePredicate> type = Optional.empty();

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
			this.sourceEntity = Optional.of(entityPredicate);
			return this;
		}

		public DamagePredicate.Builder blocked(Boolean boolean_) {
			this.blocked = Optional.of(boolean_);
			return this;
		}

		public DamagePredicate.Builder type(DamageSourcePredicate damageSourcePredicate) {
			this.type = Optional.of(damageSourcePredicate);
			return this;
		}

		public DamagePredicate.Builder type(DamageSourcePredicate.Builder builder) {
			this.type = builder.build();
			return this;
		}

		public Optional<DamagePredicate> build() {
			return DamagePredicate.of(this.dealtDamage, this.takenDamage, this.sourceEntity, this.blocked, this.type);
		}
	}
}
