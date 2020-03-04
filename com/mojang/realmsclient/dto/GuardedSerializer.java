/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.dto;

import com.google.gson.Gson;
import com.mojang.realmsclient.dto.ReflectionBasedSerialization;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class GuardedSerializer {
    private final Gson gson = new Gson();

    public String toJson(ReflectionBasedSerialization reflectionBasedSerialization) {
        return this.gson.toJson(reflectionBasedSerialization);
    }

    public <T extends ReflectionBasedSerialization> T fromJson(String string, Class<T> class_) {
        return (T)((ReflectionBasedSerialization)this.gson.fromJson(string, class_));
    }
}

