package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.GoalDebugPayload;

@Environment(EnvType.CLIENT)
public class GoalSelectorDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
	private static final int MAX_RENDER_DIST = 160;
	private final Minecraft minecraft;
	private final Int2ObjectMap<GoalSelectorDebugRenderer.EntityGoalInfo> goalSelectors = new Int2ObjectOpenHashMap<>();

	@Override
	public void clear() {
		this.goalSelectors.clear();
	}

	public void addGoalSelector(int i, BlockPos blockPos, List<GoalDebugPayload.DebugGoal> list) {
		this.goalSelectors.put(i, new GoalSelectorDebugRenderer.EntityGoalInfo(blockPos, list));
	}

	public void removeGoalSelector(int i) {
		this.goalSelectors.remove(i);
	}

	public GoalSelectorDebugRenderer(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
		Camera camera = this.minecraft.gameRenderer.getMainCamera();
		BlockPos blockPos = BlockPos.containing(camera.getPosition().x, 0.0, camera.getPosition().z);

		for (GoalSelectorDebugRenderer.EntityGoalInfo entityGoalInfo : this.goalSelectors.values()) {
			BlockPos blockPos2 = entityGoalInfo.entityPos;
			if (blockPos.closerThan(blockPos2, 160.0)) {
				for (int i = 0; i < entityGoalInfo.goals.size(); i++) {
					GoalDebugPayload.DebugGoal debugGoal = (GoalDebugPayload.DebugGoal)entityGoalInfo.goals.get(i);
					double g = (double)blockPos2.getX() + 0.5;
					double h = (double)blockPos2.getY() + 2.0 + (double)i * 0.25;
					double j = (double)blockPos2.getZ() + 0.5;
					int k = debugGoal.isRunning() ? -16711936 : -3355444;
					DebugRenderer.renderFloatingText(poseStack, multiBufferSource, debugGoal.name(), g, h, j, k);
				}
			}
		}
	}

	@Environment(EnvType.CLIENT)
	static record EntityGoalInfo(BlockPos entityPos, List<GoalDebugPayload.DebugGoal> goals) {
	}
}
