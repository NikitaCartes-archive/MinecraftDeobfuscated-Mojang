package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
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

	@Override
	public void render(long l) {
		Camera camera = this.minecraft.gameRenderer.getMainCamera();
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.blendFuncSeparate(
			GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
		);
		GlStateManager.disableTexture();
		BlockPos blockPos = new BlockPos(camera.getPosition().x, 0.0, camera.getPosition().z);
		this.goalSelectors.forEach((integer, list) -> {
			for (int i = 0; i < list.size(); i++) {
				GoalSelectorDebugRenderer.DebugGoal debugGoal = (GoalSelectorDebugRenderer.DebugGoal)list.get(i);
				if (blockPos.closerThan(debugGoal.pos, 160.0)) {
					double d = (double)debugGoal.pos.getX() + 0.5;
					double e = (double)debugGoal.pos.getY() + 2.0 + (double)i * 0.25;
					double f = (double)debugGoal.pos.getZ() + 0.5;
					int j = debugGoal.isRunning ? -16711936 : -3355444;
					DebugRenderer.renderFloatingText(debugGoal.name, d, e, f, j);
				}
			}
		});
		GlStateManager.enableDepthTest();
		GlStateManager.enableTexture();
		GlStateManager.popMatrix();
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
