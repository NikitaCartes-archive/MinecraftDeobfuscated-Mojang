package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import java.util.Collection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

@Environment(EnvType.CLIENT)
public class RaidDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
	private final Minecraft minecraft;
	private Collection<BlockPos> raidCenters = Lists.<BlockPos>newArrayList();

	public RaidDebugRenderer(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	public void setRaidCenters(Collection<BlockPos> collection) {
		this.raidCenters = collection;
	}
}
