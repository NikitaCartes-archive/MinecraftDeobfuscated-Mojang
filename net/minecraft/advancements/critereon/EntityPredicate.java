/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import java.util.function.Predicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.DistancePredicate;
import net.minecraft.advancements.critereon.EntityEquipmentPredicate;
import net.minecraft.advancements.critereon.EntityFlagsPredicate;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MobEffectsPredicate;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import org.jetbrains.annotations.Nullable;

public class EntityPredicate {
    public static final EntityPredicate ANY = new EntityPredicate(EntityTypePredicate.ANY, DistancePredicate.ANY, LocationPredicate.ANY, LocationPredicate.ANY, MobEffectsPredicate.ANY, NbtPredicate.ANY, EntityFlagsPredicate.ANY, EntityEquipmentPredicate.ANY, EntitySubPredicate.ANY, null);
    private final EntityTypePredicate entityType;
    private final DistancePredicate distanceToPlayer;
    private final LocationPredicate location;
    private final LocationPredicate steppingOnLocation;
    private final MobEffectsPredicate effects;
    private final NbtPredicate nbt;
    private final EntityFlagsPredicate flags;
    private final EntityEquipmentPredicate equipment;
    private final EntitySubPredicate subPredicate;
    private final EntityPredicate vehicle;
    private final EntityPredicate passenger;
    private final EntityPredicate targetedEntity;
    @Nullable
    private final String team;

    private EntityPredicate(EntityTypePredicate entityTypePredicate, DistancePredicate distancePredicate, LocationPredicate locationPredicate, LocationPredicate locationPredicate2, MobEffectsPredicate mobEffectsPredicate, NbtPredicate nbtPredicate, EntityFlagsPredicate entityFlagsPredicate, EntityEquipmentPredicate entityEquipmentPredicate, EntitySubPredicate entitySubPredicate, @Nullable String string) {
        this.entityType = entityTypePredicate;
        this.distanceToPlayer = distancePredicate;
        this.location = locationPredicate;
        this.steppingOnLocation = locationPredicate2;
        this.effects = mobEffectsPredicate;
        this.nbt = nbtPredicate;
        this.flags = entityFlagsPredicate;
        this.equipment = entityEquipmentPredicate;
        this.subPredicate = entitySubPredicate;
        this.passenger = this;
        this.vehicle = this;
        this.targetedEntity = this;
        this.team = string;
    }

    EntityPredicate(EntityTypePredicate entityTypePredicate, DistancePredicate distancePredicate, LocationPredicate locationPredicate, LocationPredicate locationPredicate2, MobEffectsPredicate mobEffectsPredicate, NbtPredicate nbtPredicate, EntityFlagsPredicate entityFlagsPredicate, EntityEquipmentPredicate entityEquipmentPredicate, EntitySubPredicate entitySubPredicate, EntityPredicate entityPredicate, EntityPredicate entityPredicate2, EntityPredicate entityPredicate3, @Nullable String string) {
        this.entityType = entityTypePredicate;
        this.distanceToPlayer = distancePredicate;
        this.location = locationPredicate;
        this.steppingOnLocation = locationPredicate2;
        this.effects = mobEffectsPredicate;
        this.nbt = nbtPredicate;
        this.flags = entityFlagsPredicate;
        this.equipment = entityEquipmentPredicate;
        this.subPredicate = entitySubPredicate;
        this.vehicle = entityPredicate;
        this.passenger = entityPredicate2;
        this.targetedEntity = entityPredicate3;
        this.team = string;
    }

    public boolean matches(ServerPlayer serverPlayer, @Nullable Entity entity) {
        return this.matches(serverPlayer.getLevel(), serverPlayer.position(), entity);
    }

    public boolean matches(ServerLevel serverLevel, @Nullable Vec3 vec3, @Nullable Entity entity2) {
        Team team;
        Vec3 vec32;
        if (this == ANY) {
            return true;
        }
        if (entity2 == null) {
            return false;
        }
        if (!this.entityType.matches(entity2.getType())) {
            return false;
        }
        if (vec3 == null ? this.distanceToPlayer != DistancePredicate.ANY : !this.distanceToPlayer.matches(vec3.x, vec3.y, vec3.z, entity2.getX(), entity2.getY(), entity2.getZ())) {
            return false;
        }
        if (!this.location.matches(serverLevel, entity2.getX(), entity2.getY(), entity2.getZ())) {
            return false;
        }
        if (this.steppingOnLocation != LocationPredicate.ANY && !this.steppingOnLocation.matches(serverLevel, (vec32 = Vec3.atCenterOf(entity2.getOnPosLegacy())).x(), vec32.y(), vec32.z())) {
            return false;
        }
        if (!this.effects.matches(entity2)) {
            return false;
        }
        if (!this.nbt.matches(entity2)) {
            return false;
        }
        if (!this.flags.matches(entity2)) {
            return false;
        }
        if (!this.equipment.matches(entity2)) {
            return false;
        }
        if (!this.subPredicate.matches(entity2, serverLevel, vec3)) {
            return false;
        }
        if (!this.vehicle.matches(serverLevel, vec3, entity2.getVehicle())) {
            return false;
        }
        if (this.passenger != ANY && entity2.getPassengers().stream().noneMatch(entity -> this.passenger.matches(serverLevel, vec3, (Entity)entity))) {
            return false;
        }
        if (!this.targetedEntity.matches(serverLevel, vec3, entity2 instanceof Mob ? ((Mob)entity2).getTarget() : null)) {
            return false;
        }
        return this.team == null || (team = entity2.getTeam()) != null && this.team.equals(team.getName());
    }

    public static EntityPredicate fromJson(@Nullable JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return ANY;
        }
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "entity");
        EntityTypePredicate entityTypePredicate = EntityTypePredicate.fromJson(jsonObject.get("type"));
        DistancePredicate distancePredicate = DistancePredicate.fromJson(jsonObject.get("distance"));
        LocationPredicate locationPredicate = LocationPredicate.fromJson(jsonObject.get("location"));
        LocationPredicate locationPredicate2 = LocationPredicate.fromJson(jsonObject.get("stepping_on"));
        MobEffectsPredicate mobEffectsPredicate = MobEffectsPredicate.fromJson(jsonObject.get("effects"));
        NbtPredicate nbtPredicate = NbtPredicate.fromJson(jsonObject.get("nbt"));
        EntityFlagsPredicate entityFlagsPredicate = EntityFlagsPredicate.fromJson(jsonObject.get("flags"));
        EntityEquipmentPredicate entityEquipmentPredicate = EntityEquipmentPredicate.fromJson(jsonObject.get("equipment"));
        EntitySubPredicate entitySubPredicate = EntitySubPredicate.fromJson(jsonObject.get("type_specific"));
        EntityPredicate entityPredicate = EntityPredicate.fromJson(jsonObject.get("vehicle"));
        EntityPredicate entityPredicate2 = EntityPredicate.fromJson(jsonObject.get("passenger"));
        EntityPredicate entityPredicate3 = EntityPredicate.fromJson(jsonObject.get("targeted_entity"));
        String string = GsonHelper.getAsString(jsonObject, "team", null);
        return new Builder().entityType(entityTypePredicate).distance(distancePredicate).located(locationPredicate).steppingOn(locationPredicate2).effects(mobEffectsPredicate).nbt(nbtPredicate).flags(entityFlagsPredicate).equipment(entityEquipmentPredicate).subPredicate(entitySubPredicate).team(string).vehicle(entityPredicate).passenger(entityPredicate2).targetedEntity(entityPredicate3).build();
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("type", this.entityType.serializeToJson());
        jsonObject.add("distance", this.distanceToPlayer.serializeToJson());
        jsonObject.add("location", this.location.serializeToJson());
        jsonObject.add("stepping_on", this.steppingOnLocation.serializeToJson());
        jsonObject.add("effects", this.effects.serializeToJson());
        jsonObject.add("nbt", this.nbt.serializeToJson());
        jsonObject.add("flags", this.flags.serializeToJson());
        jsonObject.add("equipment", this.equipment.serializeToJson());
        jsonObject.add("type_specific", this.subPredicate.serialize());
        jsonObject.add("vehicle", this.vehicle.serializeToJson());
        jsonObject.add("passenger", this.passenger.serializeToJson());
        jsonObject.add("targeted_entity", this.targetedEntity.serializeToJson());
        jsonObject.addProperty("team", this.team);
        return jsonObject;
    }

    public static LootContext createContext(ServerPlayer serverPlayer, Entity entity) {
        return new LootContext.Builder(serverPlayer.getLevel()).withParameter(LootContextParams.THIS_ENTITY, entity).withParameter(LootContextParams.ORIGIN, serverPlayer.position()).withRandom(serverPlayer.getRandom()).create(LootContextParamSets.ADVANCEMENT_ENTITY);
    }

    public static class Builder {
        private EntityTypePredicate entityType = EntityTypePredicate.ANY;
        private DistancePredicate distanceToPlayer = DistancePredicate.ANY;
        private LocationPredicate location = LocationPredicate.ANY;
        private LocationPredicate steppingOnLocation = LocationPredicate.ANY;
        private MobEffectsPredicate effects = MobEffectsPredicate.ANY;
        private NbtPredicate nbt = NbtPredicate.ANY;
        private EntityFlagsPredicate flags = EntityFlagsPredicate.ANY;
        private EntityEquipmentPredicate equipment = EntityEquipmentPredicate.ANY;
        private EntitySubPredicate subPredicate = EntitySubPredicate.ANY;
        private EntityPredicate vehicle = ANY;
        private EntityPredicate passenger = ANY;
        private EntityPredicate targetedEntity = ANY;
        @Nullable
        private String team;

        public static Builder entity() {
            return new Builder();
        }

        public Builder of(EntityType<?> entityType) {
            this.entityType = EntityTypePredicate.of(entityType);
            return this;
        }

        public Builder of(TagKey<EntityType<?>> tagKey) {
            this.entityType = EntityTypePredicate.of(tagKey);
            return this;
        }

        public Builder entityType(EntityTypePredicate entityTypePredicate) {
            this.entityType = entityTypePredicate;
            return this;
        }

        public Builder distance(DistancePredicate distancePredicate) {
            this.distanceToPlayer = distancePredicate;
            return this;
        }

        public Builder located(LocationPredicate locationPredicate) {
            this.location = locationPredicate;
            return this;
        }

        public Builder steppingOn(LocationPredicate locationPredicate) {
            this.steppingOnLocation = locationPredicate;
            return this;
        }

        public Builder effects(MobEffectsPredicate mobEffectsPredicate) {
            this.effects = mobEffectsPredicate;
            return this;
        }

        public Builder nbt(NbtPredicate nbtPredicate) {
            this.nbt = nbtPredicate;
            return this;
        }

        public Builder flags(EntityFlagsPredicate entityFlagsPredicate) {
            this.flags = entityFlagsPredicate;
            return this;
        }

        public Builder equipment(EntityEquipmentPredicate entityEquipmentPredicate) {
            this.equipment = entityEquipmentPredicate;
            return this;
        }

        public Builder subPredicate(EntitySubPredicate entitySubPredicate) {
            this.subPredicate = entitySubPredicate;
            return this;
        }

        public Builder vehicle(EntityPredicate entityPredicate) {
            this.vehicle = entityPredicate;
            return this;
        }

        public Builder passenger(EntityPredicate entityPredicate) {
            this.passenger = entityPredicate;
            return this;
        }

        public Builder targetedEntity(EntityPredicate entityPredicate) {
            this.targetedEntity = entityPredicate;
            return this;
        }

        public Builder team(@Nullable String string) {
            this.team = string;
            return this;
        }

        public EntityPredicate build() {
            return new EntityPredicate(this.entityType, this.distanceToPlayer, this.location, this.steppingOnLocation, this.effects, this.nbt, this.flags, this.equipment, this.subPredicate, this.vehicle, this.passenger, this.targetedEntity, this.team);
        }
    }

    public static class Composite {
        public static final Composite ANY = new Composite(new LootItemCondition[0]);
        private final LootItemCondition[] conditions;
        private final Predicate<LootContext> compositePredicates;

        private Composite(LootItemCondition[] lootItemConditions) {
            this.conditions = lootItemConditions;
            this.compositePredicates = LootItemConditions.andConditions(lootItemConditions);
        }

        public static Composite create(LootItemCondition ... lootItemConditions) {
            return new Composite(lootItemConditions);
        }

        public static Composite fromJson(JsonObject jsonObject, String string, DeserializationContext deserializationContext) {
            JsonElement jsonElement = jsonObject.get(string);
            return Composite.fromElement(string, deserializationContext, jsonElement);
        }

        public static Composite[] fromJsonArray(JsonObject jsonObject, String string, DeserializationContext deserializationContext) {
            JsonElement jsonElement = jsonObject.get(string);
            if (jsonElement == null || jsonElement.isJsonNull()) {
                return new Composite[0];
            }
            JsonArray jsonArray = GsonHelper.convertToJsonArray(jsonElement, string);
            Composite[] composites = new Composite[jsonArray.size()];
            for (int i = 0; i < jsonArray.size(); ++i) {
                composites[i] = Composite.fromElement(string + "[" + i + "]", deserializationContext, jsonArray.get(i));
            }
            return composites;
        }

        private static Composite fromElement(String string, DeserializationContext deserializationContext, @Nullable JsonElement jsonElement) {
            if (jsonElement != null && jsonElement.isJsonArray()) {
                LootItemCondition[] lootItemConditions = deserializationContext.deserializeConditions(jsonElement.getAsJsonArray(), deserializationContext.getAdvancementId() + "/" + string, LootContextParamSets.ADVANCEMENT_ENTITY);
                return new Composite(lootItemConditions);
            }
            EntityPredicate entityPredicate = EntityPredicate.fromJson(jsonElement);
            return Composite.wrap(entityPredicate);
        }

        public static Composite wrap(EntityPredicate entityPredicate) {
            if (entityPredicate == ANY) {
                return ANY;
            }
            LootItemCondition lootItemCondition = LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, entityPredicate).build();
            return new Composite(new LootItemCondition[]{lootItemCondition});
        }

        public boolean matches(LootContext lootContext) {
            return this.compositePredicates.test(lootContext);
        }

        public JsonElement toJson(SerializationContext serializationContext) {
            if (this.conditions.length == 0) {
                return JsonNull.INSTANCE;
            }
            return serializationContext.serializeConditions(this.conditions);
        }

        public static JsonElement toJson(Composite[] composites, SerializationContext serializationContext) {
            if (composites.length == 0) {
                return JsonNull.INSTANCE;
            }
            JsonArray jsonArray = new JsonArray();
            for (Composite composite : composites) {
                jsonArray.add(composite.toJson(serializationContext));
            }
            return jsonArray;
        }
    }
}

