/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources;

import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

@Environment(value=EnvType.CLIENT)
public class MobEffectTextureManager
extends TextureAtlasHolder {
    public MobEffectTextureManager(TextureManager textureManager) {
        super(textureManager, new ResourceLocation("textures/atlas/mob_effects.png"), "mob_effect");
    }

    @Override
    protected Stream<ResourceLocation> getResourcesToLoad() {
        return Registry.MOB_EFFECT.keySet().stream();
    }

    public TextureAtlasSprite get(MobEffect mobEffect) {
        return this.getSprite(Registry.MOB_EFFECT.getKey(mobEffect));
    }
}

