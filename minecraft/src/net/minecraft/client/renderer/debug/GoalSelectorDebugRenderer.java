package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

@Environment(EnvType.CLIENT)
public class GoalSelectorDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
	private final Minecraft minecraft;
	private final Map<Integer, List<GoalSelectorDebugRenderer.DebugGoal>> goalSelectors = Maps.<Integer, List<GoalSelectorDebugRenderer.DebugGoal>>newHashMap();

	@Override
	public void clear() {
		this.goalSelectors.clear();
	}

	public void addGoalSelector(int i, List<GoalSelectorDebugRenderer.DebugGoal> list) {
		this.goalSelectors.put(i, list);
	}

	public GoalSelectorDebugRenderer(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	@Environment(EnvType.CLIENT)
	public static class DebugGoal {
		public final BlockPos pos;
		public final int priority;
		public final String name;
		public final boolean isRunning;

		public DebugGoal(BlockPos blockPos, int i, String string, boolean bl) {
			this.pos = blockPos;
			this.priority = i;
			this.name = string;
			this.isRunning = bl;
		}
	}
}
