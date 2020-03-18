/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.DistancePredicate;
import net.minecraft.advancements.critereon.EntityEquipmentPredicate;
import net.minecraft.advancements.critereon.EntityFlagsPredicate;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.advancements.critereon.FishingHookPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MobEffectsPredicate;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.advancements.critereon.PlayerPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import org.jetbrains.annotations.Nullable;

public class EntityPredicate {
    public static final EntityPredicate ANY = new EntityPredicate(EntityTypePredicate.ANY, DistancePredicate.ANY, LocationPredicate.ANY, MobEffectsPredicate.ANY, NbtPredicate.ANY, EntityFlagsPredicate.ANY, EntityEquipmentPredicate.ANY, PlayerPredicate.ANY, FishingHookPredicate.ANY, null, null);
    public static final EntityPredicate[] ANY_ARRAY = new EntityPredicate[0];
    private final EntityTypePredicate entityType;
    private final DistancePredicate distanceToPlayer;
    private final LocationPredicate location;
    private final MobEffectsPredicate effects;
    private final NbtPredicate nbt;
    private final EntityFlagsPredicate flags;
    private final EntityEquipmentPredicate equipment;
    private final PlayerPredicate player;
    private final FishingHookPredicate fishingHook;
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
        String string = GsonHelper.getAsString(jsonObject, "team", null);
        ResourceLocation resourceLocation = jsonObject.has("catType") ? new ResourceLocation(GsonHelper.getAsString(jsonObject, "catType")) : null;
        return new Builder().entityType(entityTypePredicate).distance(distancePredicate).located(locationPredicate).effects(mobEffectsPredicate).nbt(nbtPredicate).flags(entityFlagsPredicate).equipment(entityEquipmentPredicate).player(playerPredicate).fishingHook(fishingHookPredicate).team(string).catType(resourceLocation).build();
    }

    public static EntityPredicate[] fromJsonArray(@Nullable JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return ANY_ARRAY;
        }
        JsonArray jsonArray = GsonHelper.convertToJsonArray(jsonElement, "entities");
        EntityPredicate[] entityPredicates = new EntityPredicate[jsonArray.size()];
        for (int i = 0; i < jsonArray.size(); ++i) {
            entityPredicates[i] = EntityPredicate.fromJson(jsonArray.get(i));
        }
        return entityPredicates;
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
        jsonObject.addProperty("team", this.team);
        if (this.catType != null) {
            jsonObject.addProperty("catType", this.catType.toString());
        }
        return jsonObject;
    }

    public static JsonElement serializeArrayToJson(EntityPredicate[] entityPredicates) {
        if (entityPredicates == ANY_ARRAY) {
            return JsonNull.INSTANCE;
        }
        JsonArray jsonArray = new JsonArray();
        for (EntityPredicate entityPredicate : entityPredicates) {
            JsonElement jsonElement = entityPredicate.serializeToJson();
            if (jsonElement.isJsonNull()) continue;
            jsonArray.add(jsonElement);
        }
        return jsonArray;
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

        public Builder team(@Nullable String string) {
            this.team = string;
            return this;
        }

        public Builder catType(@Nullable ResourceLocation resourceLocation) {
            this.catType = resourceLocation;
            return this;
        }

        public EntityPredicate build() {
            return new EntityPredicate(this.entityType, this.distanceToPlayer, this.location, this.effects, this.nbt, this.flags, this.equipment, this.player, this.fishingHook, this.team, this.catType);
        }
    }
}

