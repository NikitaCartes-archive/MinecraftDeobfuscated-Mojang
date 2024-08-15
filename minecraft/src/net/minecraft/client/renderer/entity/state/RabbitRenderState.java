package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.animal.Rabbit;

@Environment(EnvType.CLIENT)
public class RabbitRenderState extends LivingEntityRenderState {
	public float jumpCompletion;
	public boolean isToast;
	public Rabbit.Variant variant = Rabbit.Variant.BROWN;
}
