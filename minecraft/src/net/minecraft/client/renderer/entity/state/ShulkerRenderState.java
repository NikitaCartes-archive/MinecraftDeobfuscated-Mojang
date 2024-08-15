package net.minecraft.client.renderer.entity.state;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class ShulkerRenderState extends LivingEntityRenderState {
	public Vec3 renderOffset = Vec3.ZERO;
	@Nullable
	public DyeColor color;
	public float peekAmount;
	public float yHeadRot;
	public float yBodyRot;
	public Direction attachFace = Direction.DOWN;
}
