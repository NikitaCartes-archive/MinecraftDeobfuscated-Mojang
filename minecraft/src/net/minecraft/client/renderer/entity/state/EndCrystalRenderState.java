package net.minecraft.client.renderer.entity.state;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class EndCrystalRenderState extends EntityRenderState {
	public boolean showsBottom = true;
	@Nullable
	public Vec3 beamOffset;
}
