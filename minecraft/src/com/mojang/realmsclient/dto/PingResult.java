package com.mojang.realmsclient.dto;

import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class PingResult extends ValueObject implements ReflectionBasedSerialization {
	@SerializedName("pingResults")
	public List<RegionPingResult> pingResults = Lists.<RegionPingResult>newArrayList();
	@SerializedName("worldIds")
	public List<Long> realmIds = Lists.<Long>newArrayList();
}
