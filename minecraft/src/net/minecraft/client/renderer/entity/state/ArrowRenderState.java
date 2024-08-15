package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ArrowRenderState extends EntityRenderState {
	public float xRot;
	public float yRot;
	public float shake;
}
