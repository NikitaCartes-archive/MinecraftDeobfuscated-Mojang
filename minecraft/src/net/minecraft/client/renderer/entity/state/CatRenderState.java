package net.minecraft.client.renderer.entity.state;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

@Environment(EnvType.CLIENT)
public class CatRenderState extends FelineRenderState {
	private static final ResourceLocation DEFAULT_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/cat/tabby.png");
	public ResourceLocation texture = DEFAULT_TEXTURE;
	public boolean isLyingOnTopOfSleepingPlayer;
	@Nullable
	public DyeColor collarColor;
}
