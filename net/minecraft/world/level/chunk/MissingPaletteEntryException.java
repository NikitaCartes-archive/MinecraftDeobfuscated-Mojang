/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.chunk;

public class MissingPaletteEntryException
extends RuntimeException {
    public MissingPaletteEntryException(int i) {
        super("Missing Palette entry for index " + i + ".");
    }
}

