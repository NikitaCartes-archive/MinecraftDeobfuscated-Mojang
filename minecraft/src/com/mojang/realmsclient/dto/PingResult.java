package com.mojang.realmsclient.dto;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class PingResult extends ValueObject {
	public List<RegionPingResult> pingResults = new ArrayList();
	public List<Long> worldIds = new ArrayList();
}
