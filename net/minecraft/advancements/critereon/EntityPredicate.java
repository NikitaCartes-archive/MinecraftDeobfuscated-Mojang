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
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.advancements.critereon.FishingHookPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MobEffectsPredicate;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.advancements.critereon.PlayerPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Cat;
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
    public static final EntityPredicate ANY = new EntityPredicate(EntityTypePredicate.ANY, DistancePredicate.ANY, LocationPredicate.ANY, MobEffectsPredicate.ANY, NbtPredicate.ANY, EntityFlagsPredicate.ANY, EntityEquipmentPredicate.ANY, PlayerPredicate.ANY, FishingHookPredicate.ANY, null, null);
    private final EntityTypePredicate entityType;
    private final DistancePredicate distanceToPlayer;
    private final LocationPredicate location;
    private final MobEffectsPredicate effects;
    private final NbtPredicate nbt;
    private final EntityFlagsPredicate flags;
    private final EntityEquipmentPredicate equipment;
    private final PlayerPredicate player;
    private final FishingHookPredicate fishingHook;
    private final EntityPredicate vehicle;
    private final EntityPredicate targetedEntity;
    @Nullable
    private final String team;
    @Nullable
    private final ResourceLocation catType;

    private EntityPredicate(EntityTypePredicate entityTypePredicate, DistancePredicate distancePredicate, LocationPredicate locationPredicate, MobEffectsPredicate mobEffectsPredicate, NbtPredicate nbtPredicate, EntityFlagsPredicate entityFlagsPredicate, EntityEquipmentPredicate entityEquipmentPredicate, PlayerPredicate playerPredicate, FishingHookPredicate fishingHookPredicate, @Nullable String string, @Nullable ResourceLocation resourceLocation) {
        this.entityType = entityTypePredicate;
        this.distanceToPlayer = distancePredicate;
        this.location = locationPredicate;
        this.effects = mobEffectsPredicate;
        this.nbt = nbtPredicate;
        this.flags = entityFlagsPredicate;
        this.equipment = entityEquipmentPredicate;
        this.player = playerPredicate;
        this.fishingHook = fishingHookPredicate;
        this.vehicle = this;
        this.targetedEntity = this;
        this.team = string;
        this.catType = resourceLocation;
    }

    EntityPredicate(EntityTypePredicate entityTypePredicate, DistancePredicate distancePredicate, LocationPredicate locationPredicate, MobEffectsPredicate mobEffectsPredicate, NbtPredicate nbtPredicate, EntityFlagsPredicate entityFlagsPredicate, EntityEquipmentPredicate entityEquipmentPredicate, PlayerPredicate playerPredicate, FishingHookPredicate fishingHookPredicate, EntityPredicate entityPredicate, EntityPredicate entityPredicate2, @Nullable String string, @Nullable ResourceLocation resourceLocation) {
        this.entityType = entityTypePredicate;
        this.distanceToPlayer = distancePredicate;
        this.location = locationPredicate;
        this.effects = mobEffectsPredicate;
        this.nbt = nbtPredicate;
        this.flags = entityFlagsPredicate;
        this.equipment = entityEquipmentPredicate;
        this.player = playerPredicate;
        this.fishingHook = fishingHookPredicate;
        this.vehicle = entityPredicate;
        this.targetedEntity = entityPredicate2;
        this.team = string;
        this.catType = resourceLocation;
    }

    public boolean matches(ServerPlayer serverPlayer, @Nullable Entity entity) {
        return this.matches(serverPlayer.getLevel(), serverPlayer.position(), entity);
    }

    public boolean matches(ServerLevel serverLevel, @Nullable Vec3 vec3, @Nullable Entity entity) {
        Team team;
        if (this == ANY) {
            return true;
        }
        if (entity == null) {
            return false;
        }
        if (!this.entityType.matches(entity.getType())) {
            return false;
        }
        if (vec3 == null ? this.distanceToPlayer != DistancePredicate.ANY : !this.distanceToPlayer.matches(vec3.x, vec3.y, vec3.z, entity.getX(), entity.getY(), entity.getZ())) {
            return false;
        }
        if (!this.location.matches(serverLevel, entity.getX(), entity.getY(), entity.getZ())) {
            return false;
        }
        if (!this.effects.matches(entity)) {
            return false;
        }
        if (!this.nbt.matches(entity)) {
            return false;
        }
        if (!this.flags.matches(entity)) {
            return false;
        }
        if (!this.equipment.matches(entity)) {
            return false;
        }
        if (!this.player.matches(entity)) {
            return false;
        }
        if (!this.fishingHook.matches(entity)) {
            return false;
        }
        if (!this.vehicle.matches(serverLevel, vec3, entity.getVehicle())) {
            return false;
        }
        if (!this.targetedEntity.matches(serverLevel, vec3, entity instanceof Mob ? ((Mob)entity).getTarget() : null)) {
            return false;
        }
        if (!(this.team == null || (team = entity.getTeam()) != null && this.team.equals(team.getName()))) {
            return false;
        }
        return this.catType == null || entity instanceof Cat && ((Cat)entity).getResourceLocation().equals(this.catType);
    }

    public static EntityPredicate fromJson(@Nullable JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return ANY;
        }
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "entity");
        EntityTypePredicate entityTypePredicate = EntityTypePredicate.fromJson(jsonObject.get("type"));
        DistancePredicate distancePredicate = DistancePredicate.fromJson(jsonObject.get("distance"));
        LocationPredicate locationPredicate = LocationPredicate.fromJson(jsonObject.get("location"));
        MobEffectsPredicate mobEffectsPredicate = MobEffectsPredicate.fromJson(jsonObject.get("effects"));
        NbtPredicate nbtPredicate = NbtPredicate.fromJson(jsonObject.get("nbt"));
        EntityFlagsPredicate entityFlagsPredicate = EntityFlagsPredicate.fromJson(jsonObject.get("flags"));
        EntityEquipmentPredicate entityEquipmentPredicate = EntityEquipmentPredicate.fromJson(jsonObject.get("equipment"));
        PlayerPredicate playerPredicate = PlayerPredicate.fromJson(jsonObject.get("player"));
        FishingHookPredicate fishingHookPredicate = FishingHookPredicate.fromJson(jsonObject.get("fishing_hook"));
        EntityPredicate entityPredicate = EntityPredicate.fromJson(jsonObject.get("vehicle"));
        EntityPredicate entityPredicate2 = EntityPredicate.fromJson(jsonObject.get("targeted_entity"));
        String string = GsonHelper.getAsString(jsonObject, "team", null);
        ResourceLocation resourceLocation = jsonObject.has("catType") ? new ResourceLocation(GsonHelper.getAsString(jsonObject, "catType")) : null;
        return new Builder().entityType(entityTypePredicate).distance(distancePredicate).located(locationPredicate).effects(mobEffectsPredicate).nbt(nbtPredicate).flags(entityFlagsPredicate).equipment(entityEquipmentPredicate).player(playerPredicate).fishingHook(fishingHookPredicate).team(string).vehicle(entityPredicate).targetedEntity(entityPredicate2).catType(resourceLocation).build();
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("type", this.entityType.serializeToJson());
        jsonObject.add("distance", this.distanceToPlayer.serializeToJson());
        jsonObject.add("location", this.location.serializeToJson());
        jsonObject.add("effects", this.effects.serializeToJson());
        jsonObject.add("nbt", this.nbt.serializeToJson());
        jsonObject.add("flags", this.flags.serializeToJson());
        jsonObject.add("equipment", this.equipment.serializeToJson());
        jsonObject.add("player", this.player.serializeToJson());
        jsonObject.add("fishing_hook", this.fishingHook.serializeToJson());
        jsonObject.add("vehicle", this.vehicle.serializeToJson());
        jsonObject.add("targeted_entity", this.targetedEntity.serializeToJson());
        jsonObject.addProperty("team", this.team);
        if (this.catType != null) {
            jsonObject.addProperty("catType", this.catType.toString());
        }
        return jsonObject;
    }

    public static LootContext createContext(ServerPlayer serverPlayer, Entity entity) {
        return new LootContext.Builder(serverPlayer.getLevel()).withParameter(LootContextParams.THIS_ENTITY, entity).withParameter(LootContextParams.ORIGIN, serverPlayer.position()).withRandom(serverPlayer.getRandom()).create(LootContextParamSets.ADVANCEMENT_ENTITY);
    }

    public static class Builder {
        private EntityTypePredicate entityType = EntityTypePredicate.ANY;
        private DistancePredicate distanceToPlayer = DistancePredicate.ANY;
        private LocationPredicate location = LocationPredicate.ANY;
        private MobEffectsPredicate effects = MobEffectsPredicate.ANY;
        private NbtPredicate nbt = NbtPredicate.ANY;
        private EntityFlagsPredicate flags = EntityFlagsPredicate.ANY;
        private EntityEquipmentPredicate equipment = EntityEquipmentPredicate.ANY;
        private PlayerPredicate player = PlayerPredicate.ANY;
        private FishingHookPredicate fishingHook = FishingHookPredicate.ANY;
        private EntityPredicate vehicle = ANY;
        private EntityPredicate targetedEntity = ANY;
        private String team;
        private ResourceLocation catType;

        public static Builder entity() {
            return new Builder();
        }

        public Builder of(EntityType<?> entityType) {
            this.entityType = EntityTypePredicate.of(entityType);
            return this;
        }

        public Builder of(Tag<EntityType<?>> tag) {
            this.entityType = EntityTypePredicate.of(tag);
            return this;
        }

        public Builder of(ResourceLocation resourceLocation) {
            this.catType = resourceLocation;
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

        public Builder player(PlayerPredicate playerPredicate) {
            this.player = playerPredicate;
            return this;
        }

        public Builder fishingHook(FishingHookPredicate fishingHookPredicate) {
            this.fishingHook = fishingHookPredicate;
            return this;
        }

        public Builder vehicle(EntityPredicate entityPredicate) {
            this.vehicle = entityPredicate;
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

        public Builder catType(@Nullable ResourceLocation resourceLocation) {
            this.catType = resourceLocation;
            return this;
        }

        public EntityPredicate build() {
            return new EntityPredicate(this.entityType, this.distanceToPlayer, this.location, this.effects, this.nbt, this.flags, this.equipment, this.player, this.fishingHook, this.vehicle, this.targetedEntity, this.team, this.catType);
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

