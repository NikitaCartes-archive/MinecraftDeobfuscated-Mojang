/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot;

import java.util.Random;
import net.minecraft.resources.ResourceLocation;

public interface RandomIntGenerator {
    public static final ResourceLocation CONSTANT = new ResourceLocation("constant");
    public static final ResourceLocation UNIFORM = new ResourceLocation("uniform");
    public static final ResourceLocation BINOMIAL = new ResourceLocation("binomial");

    public int getInt(Random var1);

    public ResourceLocation getType();
}

