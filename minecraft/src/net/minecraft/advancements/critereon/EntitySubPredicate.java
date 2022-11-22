package net.minecraft.advancements.critereon;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.Variant;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.Vec3;

public interface EntitySubPredicate {
	EntitySubPredicate ANY = new EntitySubPredicate() {
		@Override
		public boolean matches(Entity entity, ServerLevel serverLevel, @Nullable Vec3 vec3) {
			return true;
		}

		@Override
		public JsonObject serializeCustomData() {
			return new JsonObject();
		}

		@Override
		public EntitySubPredicate.Type type() {
			return EntitySubPredicate.Types.ANY;
		}
	};

	static EntitySubPredicate fromJson(@Nullable JsonElement jsonElement) {
		if (jsonElement != null && !jsonElement.isJsonNull()) {
			JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "type_specific");
			String string = GsonHelper.getAsString(jsonObject, "type", null);
			if (string == null) {
				return ANY;
			} else {
				EntitySubPredicate.Type type = (EntitySubPredicate.Type)EntitySubPredicate.Types.TYPES.get(string);
				if (type == null) {
					throw new JsonSyntaxException("Unknown sub-predicate type: " + string);
				} else {
					return type.deserialize(jsonObject);
				}
			}
		} else {
			return ANY;
		}
	}

	boolean matches(Entity entity, ServerLevel serverLevel, @Nullable Vec3 vec3);

	JsonObject serializeCustomData();

	default JsonElement serialize() {
		if (this.type() == EntitySubPredicate.Types.ANY) {
			return JsonNull.INSTANCE;
		} else {
			JsonObject jsonObject = this.serializeCustomData();
			String string = (String)EntitySubPredicate.Types.TYPES.inverse().get(this.type());
			jsonObject.addProperty("type", string);
			return jsonObject;
		}
	}

	EntitySubPredicate.Type type();

	static EntitySubPredicate variant(CatVariant catVariant) {
		return EntitySubPredicate.Types.CAT.createPredicate(catVariant);
	}

	static EntitySubPredicate variant(FrogVariant frogVariant) {
		return EntitySubPredicate.Types.FROG.createPredicate(frogVariant);
	}

	public interface Type {
		EntitySubPredicate deserialize(JsonObject jsonObject);
	}

	public static final class Types {
		public static final EntitySubPredicate.Type ANY = jsonObject -> EntitySubPredicate.ANY;
		public static final EntitySubPredicate.Type LIGHTNING = LighthingBoltPredicate::fromJson;
		public static final EntitySubPredicate.Type FISHING_HOOK = FishingHookPredicate::fromJson;
		public static final EntitySubPredicate.Type PLAYER = PlayerPredicate::fromJson;
		public static final EntitySubPredicate.Type SLIME = SlimePredicate::fromJson;
		public static final EntityVariantPredicate<CatVariant> CAT = EntityVariantPredicate.create(
			BuiltInRegistries.CAT_VARIANT, entity -> entity instanceof Cat cat ? Optional.of(cat.getVariant()) : Optional.empty()
		);
		public static final EntityVariantPredicate<FrogVariant> FROG = EntityVariantPredicate.create(
			BuiltInRegistries.FROG_VARIANT, entity -> entity instanceof Frog frog ? Optional.of(frog.getVariant()) : Optional.empty()
		);
		public static final EntityVariantPredicate<Axolotl.Variant> AXOLOTL = EntityVariantPredicate.create(
			Axolotl.Variant.CODEC, entity -> entity instanceof Axolotl axolotl ? Optional.of(axolotl.getVariant()) : Optional.empty()
		);
		public static final EntityVariantPredicate<Boat.Type> BOAT = EntityVariantPredicate.create(
			Boat.Type.CODEC, entity -> entity instanceof Boat boat ? Optional.of(boat.getVariant()) : Optional.empty()
		);
		public static final EntityVariantPredicate<Fox.Type> FOX = EntityVariantPredicate.create(
			Fox.Type.CODEC, entity -> entity instanceof Fox fox ? Optional.of(fox.getVariant()) : Optional.empty()
		);
		public static final EntityVariantPredicate<MushroomCow.MushroomType> MOOSHROOM = EntityVariantPredicate.create(
			MushroomCow.MushroomType.CODEC, entity -> entity instanceof MushroomCow mushroomCow ? Optional.of(mushroomCow.getVariant()) : Optional.empty()
		);
		public static final EntityVariantPredicate<Holder<PaintingVariant>> PAINTING = EntityVariantPredicate.create(
			BuiltInRegistries.PAINTING_VARIANT.holderByNameCodec(),
			entity -> entity instanceof Painting painting ? Optional.of(painting.getVariant()) : Optional.empty()
		);
		public static final EntityVariantPredicate<Rabbit.Variant> RABBIT = EntityVariantPredicate.create(
			Rabbit.Variant.CODEC, entity -> entity instanceof Rabbit rabbit ? Optional.of(rabbit.getVariant()) : Optional.empty()
		);
		public static final EntityVariantPredicate<Variant> HORSE = EntityVariantPredicate.create(
			Variant.CODEC, entity -> entity instanceof Horse horse ? Optional.of(horse.getVariant()) : Optional.empty()
		);
		public static final EntityVariantPredicate<Llama.Variant> LLAMA = EntityVariantPredicate.create(
			Llama.Variant.CODEC, entity -> entity instanceof Llama llama ? Optional.of(llama.getVariant()) : Optional.empty()
		);
		public static final EntityVariantPredicate<VillagerType> VILLAGER = EntityVariantPredicate.create(
			BuiltInRegistries.VILLAGER_TYPE.byNameCodec(),
			entity -> entity instanceof VillagerDataHolder villagerDataHolder ? Optional.of(villagerDataHolder.getVariant()) : Optional.empty()
		);
		public static final EntityVariantPredicate<Parrot.Variant> PARROT = EntityVariantPredicate.create(
			Parrot.Variant.CODEC, entity -> entity instanceof Parrot parrot ? Optional.of(parrot.getVariant()) : Optional.empty()
		);
		public static final EntityVariantPredicate<TropicalFish.Pattern> TROPICAL_FISH = EntityVariantPredicate.create(
			TropicalFish.Pattern.CODEC, entity -> entity instanceof TropicalFish tropicalFish ? Optional.of(tropicalFish.getVariant()) : Optional.empty()
		);
		public static final BiMap<String, EntitySubPredicate.Type> TYPES = ImmutableBiMap.<String, EntitySubPredicate.Type>builder()
			.put("any", ANY)
			.put("lightning", LIGHTNING)
			.put("fishing_hook", FISHING_HOOK)
			.put("player", PLAYER)
			.put("slime", SLIME)
			.put("cat", CAT.type())
			.put("frog", FROG.type())
			.put("axolotl", AXOLOTL.type())
			.put("boat", BOAT.type())
			.put("fox", FOX.type())
			.put("mooshroom", MOOSHROOM.type())
			.put("painting", PAINTING.type())
			.put("rabbit", RABBIT.type())
			.put("horse", HORSE.type())
			.put("llama", LLAMA.type())
			.put("villager", VILLAGER.type())
			.put("parrot", PARROT.type())
			.put("tropical_fish", TROPICAL_FISH.type())
			.buildOrThrow();
	}
}
