package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

@Environment(EnvType.CLIENT)
public class CaveDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
	private final Minecraft minecraft;
	private final Map<BlockPos, BlockPos> tunnelsList = Maps.<BlockPos, BlockPos>newHashMap();
	private final Map<BlockPos, Float> thicknessMap = Maps.<BlockPos, Float>newHashMap();
	private final List<BlockPos> startPoses = Lists.<BlockPos>newArrayList();

	public CaveDebugRenderer(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	public void addTunnel(BlockPos blockPos, List<BlockPos> list, List<Float> list2) {
		for (int i = 0; i < list.size(); i++) {
			this.tunnelsList.put(list.get(i), blockPos);
			this.thicknessMap.put(list.get(i), list2.get(i));
		}

		this.startPoses.add(blockPos);
	}
}
