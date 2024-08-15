package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ThrownTridentRenderState extends EntityRenderState {
	public float xRot;
	public float yRot;
	public boolean isFoil;
}
