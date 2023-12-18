package net.minecraft.client.resources;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

@Environment(EnvType.CLIENT)
public class MobEffectTextureManager extends TextureAtlasHolder {
	public MobEffectTextureManager(TextureManager textureManager) {
		super(textureManager, new ResourceLocation("textures/atlas/mob_effects.png"), new ResourceLocation("mob_effects"));
	}

	public TextureAtlasSprite get(Holder<MobEffect> holder) {
		return this.getSprite((ResourceLocation)holder.unwrapKey().map(ResourceKey::location).orElseGet(MissingTextureAtlasSprite::getLocation));
	}
}
