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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.entity.animal.frog.Frog;
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
                optional = Optional.of(cat.getCatVariant());
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
        public static final BiMap<String, Type> TYPES = ImmutableBiMap.of("any", ANY, "lightning", LIGHTNING, "fishing_hook", FISHING_HOOK, "player", PLAYER, "slime", SLIME, "cat", CAT.type(), "frog", FROG.type());
    }

    public static interface Type {
        public EntitySubPredicate deserialize(JsonObject var1);
    }
}

