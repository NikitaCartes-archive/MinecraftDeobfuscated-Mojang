package net.minecraft.client.resources;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

@Environment(EnvType.CLIENT)
public class MobEffectTextureManager extends TextureAtlasHolder {
	public MobEffectTextureManager(TextureManager textureManager) {
		super(textureManager, TextureAtlas.LOCATION_MOB_EFFECTS, "textures/mob_effect");
	}

	@Override
	protected Iterable<ResourceLocation> getResourcesToLoad() {
		return Registry.MOB_EFFECT.keySet();
	}

	public TextureAtlasSprite get(MobEffect mobEffect) {
		return this.getSprite(Registry.MOB_EFFECT.getKey(mobEffect));
	}
}
