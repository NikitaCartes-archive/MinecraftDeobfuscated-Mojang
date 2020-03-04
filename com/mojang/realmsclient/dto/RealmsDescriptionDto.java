/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.dto;

import com.google.gson.annotations.SerializedName;
import com.mojang.realmsclient.dto.ReflectionBasedSerialization;
import com.mojang.realmsclient.dto.ValueObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class RealmsDescriptionDto
extends ValueObject
implements ReflectionBasedSerialization {
    @SerializedName(value="name")
    public String name;
    @SerializedName(value="description")
    public String description;

    public RealmsDescriptionDto(String string, String string2) {
        this.name = string;
        this.description = string2;
    }
}

