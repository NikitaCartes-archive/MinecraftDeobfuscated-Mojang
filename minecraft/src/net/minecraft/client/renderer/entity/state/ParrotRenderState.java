package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ParrotModel;
import net.minecraft.world.entity.animal.Parrot;

@Environment(EnvType.CLIENT)
public class ParrotRenderState extends LivingEntityRenderState {
	public Parrot.Variant variant = Parrot.Variant.RED_BLUE;
	public float flapAngle;
	public ParrotModel.Pose pose = ParrotModel.Pose.FLYING;
}
