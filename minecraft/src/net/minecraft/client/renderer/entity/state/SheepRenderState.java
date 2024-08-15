package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.DyeColor;

@Environment(EnvType.CLIENT)
public class SheepRenderState extends LivingEntityRenderState {
	public float headEatPositionScale;
	public float headEatAngleScale;
	public boolean isSheared;
	public DyeColor woolColor = DyeColor.WHITE;
	public int id;
}
