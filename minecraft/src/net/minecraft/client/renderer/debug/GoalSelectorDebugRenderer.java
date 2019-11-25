package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
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
	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
		Camera camera = this.minecraft.gameRenderer.getMainCamera();
		RenderSystem.pushMatrix();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableTexture();
		BlockPos blockPos = new BlockPos(camera.getPosition().x, 0.0, camera.getPosition().z);
		this.goalSelectors.forEach((integer, list) -> {
			for (int i = 0; i < list.size(); i++) {
				GoalSelectorDebugRenderer.DebugGoal debugGoal = (GoalSelectorDebugRenderer.DebugGoal)list.get(i);
				if (blockPos.closerThan(debugGoal.pos, 160.0)) {
					double dx = (double)debugGoal.pos.getX() + 0.5;
					double ex = (double)debugGoal.pos.getY() + 2.0 + (double)i * 0.25;
					double fx = (double)debugGoal.pos.getZ() + 0.5;
					int j = debugGoal.isRunning ? -16711936 : -3355444;
					DebugRenderer.renderFloatingText(debugGoal.name, dx, ex, fx, j);
				}
			}
		});
		RenderSystem.enableDepthTest();
		RenderSystem.enableTexture();
		RenderSystem.popMatrix();
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
