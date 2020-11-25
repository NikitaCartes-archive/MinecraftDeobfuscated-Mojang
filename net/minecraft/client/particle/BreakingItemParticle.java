/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Environment(value=EnvType.CLIENT)
public class BreakingItemParticle
extends TextureSheetParticle {
    private final float uo;
    private final float vo;

    private BreakingItemParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, ItemStack itemStack) {
        this(clientLevel, d, e, f, itemStack);
        this.xd *= (double)0.1f;
        this.yd *= (double)0.1f;
        this.zd *= (double)0.1f;
        this.xd += g;
        this.yd += h;
        this.zd += i;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.TERRAIN_SHEET;
    }

    protected BreakingItemParticle(ClientLevel clientLevel, double d, double e, double f, ItemStack itemStack) {
        super(clientLevel, d, e, f, 0.0, 0.0, 0.0);
        this.setSprite(Minecraft.getInstance().getItemRenderer().getModel(itemStack, clientLevel, null, 0).getParticleIcon());
        this.gravity = 1.0f;
        this.quadSize /= 2.0f;
        this.uo = this.random.nextFloat() * 3.0f;
        this.vo = this.random.nextFloat() * 3.0f;
    }

    @Override
    protected float getU0() {
        return this.sprite.getU((this.uo + 1.0f) / 4.0f * 16.0f);
    }

    @Override
    protected float getU1() {
        return this.sprite.getU(this.uo / 4.0f * 16.0f);
    }

    @Override
    protected float getV0() {
        return this.sprite.getV(this.vo / 4.0f * 16.0f);
    }

    @Override
    protected float getV1() {
        return this.sprite.getV((this.vo + 1.0f) / 4.0f * 16.0f);
    }

    @Environment(value=EnvType.CLIENT)
    public static class SnowballProvider
    implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            return new BreakingItemParticle(clientLevel, d, e, f, new ItemStack(Items.SNOWBALL));
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class SlimeProvider
    implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            return new BreakingItemParticle(clientLevel, d, e, f, new ItemStack(Items.SLIME_BALL));
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Provider
    implements ParticleProvider<ItemParticleOption> {
        @Override
        public Particle createParticle(ItemParticleOption itemParticleOption, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            return new BreakingItemParticle(clientLevel, d, e, f, g, h, i, itemParticleOption.getItem());
        }
    }
}

