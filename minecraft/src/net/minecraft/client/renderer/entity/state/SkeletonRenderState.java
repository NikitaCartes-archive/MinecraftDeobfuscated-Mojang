package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class SkeletonRenderState extends HumanoidRenderState {
	public boolean isAggressive;
	public boolean isShaking;
}
