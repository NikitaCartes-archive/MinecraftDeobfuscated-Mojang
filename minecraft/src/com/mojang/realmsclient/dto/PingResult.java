package com.mojang.realmsclient.dto;

import com.google.common.collect.Lists;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class PingResult extends ValueObject {
	public List<RegionPingResult> pingResults = Lists.<RegionPingResult>newArrayList();
	public List<Long> worldIds = Lists.<Long>newArrayList();
}
