/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class LighthingBoltPredicate {
    public static final LighthingBoltPredicate ANY = new LighthingBoltPredicate(MinMaxBounds.Ints.ANY, EntityPredicate.ANY);
    private static final String BLOCKS_SET_ON_FIRE_KEY = "blocks_set_on_fire";
    private static final String ENTITY_STRUCK_KEY = "entity_struck";
    private final MinMaxBounds.Ints blocksSetOnFire;
    private final EntityPredicate entityStruck;

    private LighthingBoltPredicate(MinMaxBounds.Ints ints, EntityPredicate entityPredicate) {
        this.blocksSetOnFire = ints;
        this.entityStruck = entityPredicate;
    }

    public static LighthingBoltPredicate blockSetOnFire(MinMaxBounds.Ints ints) {
        return new LighthingBoltPredicate(ints, EntityPredicate.ANY);
    }

    public static LighthingBoltPredicate fromJson(@Nullable JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return ANY;
        }
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "lightning");
        return new LighthingBoltPredicate(MinMaxBounds.Ints.fromJson(jsonObject.get(BLOCKS_SET_ON_FIRE_KEY)), EntityPredicate.fromJson(jsonObject.get(ENTITY_STRUCK_KEY)));
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(BLOCKS_SET_ON_FIRE_KEY, this.blocksSetOnFire.serializeToJson());
        jsonObject.add(ENTITY_STRUCK_KEY, this.entityStruck.serializeToJson());
        return jsonObject;
    }

    public boolean matches(Entity entity2, ServerLevel serverLevel, @Nullable Vec3 vec3) {
        if (this == ANY) {
            return true;
        }
        if (!(entity2 instanceof LightningBolt)) {
            return false;
        }
        LightningBolt lightningBolt = (LightningBolt)entity2;
        return this.blocksSetOnFire.matches(lightningBolt.getBlocksSetOnFire()) && (this.entityStruck == EntityPredicate.ANY || lightningBolt.getHitEntities().anyMatch(entity -> this.entityStruck.matches(serverLevel, vec3, (Entity)entity)));
    }
}

