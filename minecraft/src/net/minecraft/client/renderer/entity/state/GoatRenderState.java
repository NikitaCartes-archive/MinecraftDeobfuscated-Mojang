package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class GoatRenderState extends LivingEntityRenderState {
	public boolean hasLeftHorn = true;
	public boolean hasRightHorn = true;
	public float rammingXHeadRot;
}
