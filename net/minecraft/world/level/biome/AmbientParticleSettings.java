/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import java.util.Random;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.particles.SimpleParticleType;

public class AmbientParticleSettings {
    private final SimpleParticleType particleType;
    private final float probability;
    private final Function<Random, Double> xVelocity;
    private final Function<Random, Double> yVelocity;
    private final Function<Random, Double> zVelocity;

    public AmbientParticleSettings(SimpleParticleType simpleParticleType, float f, Function<Random, Double> function, Function<Random, Double> function2, Function<Random, Double> function3) {
        this.particleType = simpleParticleType;
        this.probability = f;
        this.xVelocity = function;
        this.yVelocity = function2;
        this.zVelocity = function3;
    }

    @Environment(value=EnvType.CLIENT)
    public SimpleParticleType getParticleType() {
        return this.particleType;
    }

    @Environment(value=EnvType.CLIENT)
    public boolean canSpawn(Random random) {
        return random.nextFloat() <= this.probability;
    }

    @Environment(value=EnvType.CLIENT)
    public double getXVelocity(Random random) {
        return this.xVelocity.apply(random);
    }

    @Environment(value=EnvType.CLIENT)
    public double getYVelocity(Random random) {
        return this.yVelocity.apply(random);
    }

    @Environment(value=EnvType.CLIENT)
    public double getZVelocity(Random random) {
        return this.zVelocity.apply(random);
    }
}

