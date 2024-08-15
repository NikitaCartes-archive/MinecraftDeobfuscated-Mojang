package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.animal.Panda;

@Environment(EnvType.CLIENT)
public class PandaRenderState extends LivingEntityRenderState {
	public Panda.Gene variant = Panda.Gene.NORMAL;
	public boolean isUnhappy;
	public boolean isSneezing;
	public int sneezeTime;
	public boolean isEating;
	public boolean isScared;
	public boolean isSitting;
	public float sitAmount;
	public float lieOnBackAmount;
	public float rollAmount;
	public float rollTime;
}
