/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class DamageSourcePredicate {
    public static final DamageSourcePredicate ANY = Builder.damageType().build();
    private final List<TagPredicate<DamageType>> tags;
    private final EntityPredicate directEntity;
    private final EntityPredicate sourceEntity;

    public DamageSourcePredicate(List<TagPredicate<DamageType>> list, EntityPredicate entityPredicate, EntityPredicate entityPredicate2) {
        this.tags = list;
        this.directEntity = entityPredicate;
        this.sourceEntity = entityPredicate2;
    }

    public boolean matches(ServerPlayer serverPlayer, DamageSource damageSource) {
        return this.matches(serverPlayer.getLevel(), serverPlayer.position(), damageSource);
    }

    public boolean matches(ServerLevel serverLevel, Vec3 vec3, DamageSource damageSource) {
        if (this == ANY) {
            return true;
        }
        for (TagPredicate<DamageType> tagPredicate : this.tags) {
            if (tagPredicate.matches(damageSource.typeHolder())) continue;
            return false;
        }
        if (!this.directEntity.matches(serverLevel, vec3, damageSource.getDirectEntity())) {
            return false;
        }
        return this.sourceEntity.matches(serverLevel, vec3, damageSource.getEntity());
    }

    public static DamageSourcePredicate fromJson(@Nullable JsonElement jsonElement) {
        List<TagPredicate<DamageType>> list;
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return ANY;
        }
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "damage type");
        JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "tags", null);
        if (jsonArray != null) {
            list = new ArrayList(jsonArray.size());
            for (JsonElement jsonElement2 : jsonArray) {
                list.add(TagPredicate.fromJson(jsonElement2, Registries.DAMAGE_TYPE));
            }
        } else {
            list = List.of();
        }
        EntityPredicate entityPredicate = EntityPredicate.fromJson(jsonObject.get("direct_entity"));
        EntityPredicate entityPredicate2 = EntityPredicate.fromJson(jsonObject.get("source_entity"));
        return new DamageSourcePredicate(list, entityPredicate, entityPredicate2);
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        }
        JsonObject jsonObject = new JsonObject();
        if (!this.tags.isEmpty()) {
            JsonArray jsonArray = new JsonArray(this.tags.size());
            for (int i = 0; i < this.tags.size(); ++i) {
                jsonArray.add(this.tags.get(i).serializeToJson());
            }
            jsonObject.add("tags", jsonArray);
        }
        jsonObject.add("direct_entity", this.directEntity.serializeToJson());
        jsonObject.add("source_entity", this.sourceEntity.serializeToJson());
        return jsonObject;
    }

    public static class Builder {
        private final ImmutableList.Builder<TagPredicate<DamageType>> tags = ImmutableList.builder();
        private EntityPredicate directEntity = EntityPredicate.ANY;
        private EntityPredicate sourceEntity = EntityPredicate.ANY;

        public static Builder damageType() {
            return new Builder();
        }

        public Builder tag(TagPredicate<DamageType> tagPredicate) {
            this.tags.add((Object)tagPredicate);
            return this;
        }

        public Builder direct(EntityPredicate entityPredicate) {
            this.directEntity = entityPredicate;
            return this;
        }

        public Builder direct(EntityPredicate.Builder builder) {
            this.directEntity = builder.build();
            return this;
        }

        public Builder source(EntityPredicate entityPredicate) {
            this.sourceEntity = entityPredicate;
            return this;
        }

        public Builder source(EntityPredicate.Builder builder) {
            this.sourceEntity = builder.build();
            return this;
        }

        public DamageSourcePredicate build() {
            return new DamageSourcePredicate((List<TagPredicate<DamageType>>)((Object)this.tags.build()), this.directEntity, this.sourceEntity);
        }
    }
}

