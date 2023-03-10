/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.dto;

import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;
import com.mojang.realmsclient.dto.ReflectionBasedSerialization;
import com.mojang.realmsclient.dto.RegionPingResult;
import com.mojang.realmsclient.dto.ValueObject;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class PingResult
extends ValueObject
implements ReflectionBasedSerialization {
    @SerializedName(value="pingResults")
    public List<RegionPingResult> pingResults = Lists.newArrayList();
    @SerializedName(value="worldIds")
    public List<Long> worldIds = Lists.newArrayList();
}

