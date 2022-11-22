/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Optional;
import net.minecraft.advancements.critereon.EntityVariantPredicate;
import net.minecraft.advancements.critereon.FishingHookPredicate;
import net.minecraft.advancements.critereon.LighthingBoltPredicate;
import net.minecraft.advancements.critereon.PlayerPredicate;
import net.minecraft.advancements.critereon.SlimePredicate;
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
import org.jetbrains.annotations.Nullable;

public interface EntitySubPredicate {
    public static final EntitySubPredicate ANY = new EntitySubPredicate(){

        @Override
        public boolean matches(Entity entity, ServerLevel serverLevel, @Nullable Vec3 vec3) {
            return true;
        }

        @Override
        public JsonObject serializeCustomData() {
            return new JsonObject();
        }

        @Override
        public Type type() {
            return Types.ANY;
        }
    };

    public static EntitySubPredicate fromJson(@Nullable JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return ANY;
        }
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "type_specific");
        String string = GsonHelper.getAsString(jsonObject, "type", null);
        if (string == null) {
            return ANY;
        }
        Type type = (Type)Types.TYPES.get(string);
        if (type == null) {
            throw new JsonSyntaxException("Unknown sub-predicate type: " + string);
        }
        return type.deserialize(jsonObject);
    }

    public boolean matches(Entity var1, ServerLevel var2, @Nullable Vec3 var3);

    public JsonObject serializeCustomData();

    default public JsonElement serialize() {
        if (this.type() == Types.ANY) {
            return JsonNull.INSTANCE;
        }
        JsonObject jsonObject = this.serializeCustomData();
        String string = (String)Types.TYPES.inverse().get(this.type());
        jsonObject.addProperty("type", string);
        return jsonObject;
    }

    public Type type();

    public static EntitySubPredicate variant(CatVariant catVariant) {
        return Types.CAT.createPredicate(catVariant);
    }

    public static EntitySubPredicate variant(FrogVariant frogVariant) {
        return Types.FROG.createPredicate(frogVariant);
    }

    public static final class Types {
        public static final Type ANY = jsonObject -> ANY;
        public static final Type LIGHTNING = LighthingBoltPredicate::fromJson;
        public static final Type FISHING_HOOK = FishingHookPredicate::fromJson;
        public static final Type PLAYER = PlayerPredicate::fromJson;
        public static final Type SLIME = SlimePredicate::fromJson;
        public static final EntityVariantPredicate<CatVariant> CAT = EntityVariantPredicate.create(BuiltInRegistries.CAT_VARIANT, entity -> {
            Optional<Object> optional;
            if (entity instanceof Cat) {
                Cat cat = (Cat)entity;
                optional = Optional.of(cat.getVariant());
            } else {
                optional = Optional.empty();
            }
            return optional;
        });
        public static final EntityVariantPredicate<FrogVariant> FROG = EntityVariantPredicate.create(BuiltInRegistries.FROG_VARIANT, entity -> {
            Optional<Object> optional;
            if (entity instanceof Frog) {
                Frog frog = (Frog)entity;
                optional = Optional.of(frog.getVariant());
            } else {
                optional = Optional.empty();
            }
            return optional;
        });
        public static final EntityVariantPredicate<Axolotl.Variant> AXOLOTL = EntityVariantPredicate.create(Axolotl.Variant.CODEC, entity -> {
            Optional<Object> optional;
            if (entity instanceof Axolotl) {
                Axolotl axolotl = (Axolotl)entity;
                optional = Optional.of(axolotl.getVariant());
            } else {
                optional = Optional.empty();
            }
            return optional;
        });
        public static final EntityVariantPredicate<Boat.Type> BOAT = EntityVariantPredicate.create(Boat.Type.CODEC, entity -> {
            Optional<Object> optional;
            if (entity instanceof Boat) {
                Boat boat = (Boat)entity;
                optional = Optional.of(boat.getVariant());
            } else {
                optional = Optional.empty();
            }
            return optional;
        });
        public static final EntityVariantPredicate<Fox.Type> FOX = EntityVariantPredicate.create(Fox.Type.CODEC, entity -> {
            Optional<Object> optional;
            if (entity instanceof Fox) {
                Fox fox = (Fox)entity;
                optional = Optional.of(fox.getVariant());
            } else {
                optional = Optional.empty();
            }
            return optional;
        });
        public static final EntityVariantPredicate<MushroomCow.MushroomType> MOOSHROOM = EntityVariantPredicate.create(MushroomCow.MushroomType.CODEC, entity -> {
            Optional<Object> optional;
            if (entity instanceof MushroomCow) {
                MushroomCow mushroomCow = (MushroomCow)entity;
                optional = Optional.of(mushroomCow.getVariant());
            } else {
                optional = Optional.empty();
            }
            return optional;
        });
        public static final EntityVariantPredicate<Holder<PaintingVariant>> PAINTING = EntityVariantPredicate.create(BuiltInRegistries.PAINTING_VARIANT.holderByNameCodec(), entity -> {
            Optional<Object> optional;
            if (entity instanceof Painting) {
                Painting painting = (Painting)entity;
                optional = Optional.of(painting.getVariant());
            } else {
                optional = Optional.empty();
            }
            return optional;
        });
        public static final EntityVariantPredicate<Rabbit.Variant> RABBIT = EntityVariantPredicate.create(Rabbit.Variant.CODEC, entity -> {
            Optional<Object> optional;
            if (entity instanceof Rabbit) {
                Rabbit rabbit = (Rabbit)entity;
                optional = Optional.of(rabbit.getVariant());
            } else {
                optional = Optional.empty();
            }
            return optional;
        });
        public static final EntityVariantPredicate<Variant> HORSE = EntityVariantPredicate.create(Variant.CODEC, entity -> {
            Optional<Object> optional;
            if (entity instanceof Horse) {
                Horse horse = (Horse)entity;
                optional = Optional.of(horse.getVariant());
            } else {
                optional = Optional.empty();
            }
            return optional;
        });
        public static final EntityVariantPredicate<Llama.Variant> LLAMA = EntityVariantPredicate.create(Llama.Variant.CODEC, entity -> {
            Optional<Object> optional;
            if (entity instanceof Llama) {
                Llama llama = (Llama)entity;
                optional = Optional.of(llama.getVariant());
            } else {
                optional = Optional.empty();
            }
            return optional;
        });
        public static final EntityVariantPredicate<VillagerType> VILLAGER = EntityVariantPredicate.create(BuiltInRegistries.VILLAGER_TYPE.byNameCodec(), entity -> {
            Optional<Object> optional;
            if (entity instanceof VillagerDataHolder) {
                VillagerDataHolder villagerDataHolder = (VillagerDataHolder)((Object)entity);
                optional = Optional.of(villagerDataHolder.getVariant());
            } else {
                optional = Optional.empty();
            }
            return optional;
        });
        public static final EntityVariantPredicate<Parrot.Variant> PARROT = EntityVariantPredicate.create(Parrot.Variant.CODEC, entity -> {
            Optional<Object> optional;
            if (entity instanceof Parrot) {
                Parrot parrot = (Parrot)entity;
                optional = Optional.of(parrot.getVariant());
            } else {
                optional = Optional.empty();
            }
            return optional;
        });
        public static final EntityVariantPredicate<TropicalFish.Pattern> TROPICAL_FISH = EntityVariantPredicate.create(TropicalFish.Pattern.CODEC, entity -> {
            Optional<Object> optional;
            if (entity instanceof TropicalFish) {
                TropicalFish tropicalFish = (TropicalFish)entity;
                optional = Optional.of(tropicalFish.getVariant());
            } else {
                optional = Optional.empty();
            }
            return optional;
        });
        public static final BiMap<String, Type> TYPES = ((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)ImmutableBiMap.builder().put("any", ANY)).put("lightning", LIGHTNING)).put("fishing_hook", FISHING_HOOK)).put("player", PLAYER)).put("slime", SLIME)).put("cat", CAT.type())).put("frog", FROG.type())).put("axolotl", AXOLOTL.type())).put("boat", BOAT.type())).put("fox", FOX.type())).put("mooshroom", MOOSHROOM.type())).put("painting", PAINTING.type())).put("rabbit", RABBIT.type())).put("horse", HORSE.type())).put("llama", LLAMA.type())).put("villager", VILLAGER.type())).put("parrot", PARROT.type())).put("tropical_fish", TROPICAL_FISH.type())).buildOrThrow();
    }

    public static interface Type {
        public EntitySubPredicate deserialize(JsonObject var1);
    }
}

