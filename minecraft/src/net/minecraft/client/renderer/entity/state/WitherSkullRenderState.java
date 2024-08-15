package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class WitherSkullRenderState extends EntityRenderState {
	public boolean isDangerous;
	public float xRot;
	public float yRot;
}
