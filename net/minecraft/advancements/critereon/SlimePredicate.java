/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SlimePredicate
implements EntitySubPredicate {
    private final MinMaxBounds.Ints size;

    private SlimePredicate(MinMaxBounds.Ints ints) {
        this.size = ints;
    }

    public static SlimePredicate sized(MinMaxBounds.Ints ints) {
        return new SlimePredicate(ints);
    }

    public static SlimePredicate fromJson(JsonObject jsonObject) {
        MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("size"));
        return new SlimePredicate(ints);
    }

    @Override
    public JsonObject serializeCustomData() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("size", this.size.serializeToJson());
        return jsonObject;
    }

    @Override
    public boolean matches(Entity entity, ServerLevel serverLevel, @Nullable Vec3 vec3) {
        if (entity instanceof Slime) {
            Slime slime = (Slime)entity;
            return this.size.matches(slime.getSize());
        }
        return false;
    }

    @Override
    public EntitySubPredicate.Type type() {
        return EntitySubPredicate.Types.SLIME;
    }
}

