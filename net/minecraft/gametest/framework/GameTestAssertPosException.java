/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.gametest.framework;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestAssertException;
import org.jetbrains.annotations.Nullable;

public class GameTestAssertPosException
extends GameTestAssertException {
    private final BlockPos absolutePos;
    private final BlockPos relativePos;
    private final long tick;

    public GameTestAssertPosException(String string, BlockPos blockPos, BlockPos blockPos2, long l) {
        super(string);
        this.absolutePos = blockPos;
        this.relativePos = blockPos2;
        this.tick = l;
    }

    @Override
    public String getMessage() {
        String string = "" + this.absolutePos.getX() + "," + this.absolutePos.getY() + "," + this.absolutePos.getZ() + " (relative: " + this.relativePos.getX() + "," + this.relativePos.getY() + "," + this.relativePos.getZ() + ")";
        return super.getMessage() + " at " + string + " (t=" + this.tick + ")";
    }

    @Nullable
    public String getMessageToShowAtBlock() {
        return super.getMessage() + " here";
    }

    @Nullable
    public BlockPos getRelativePos() {
        return this.relativePos;
    }

    @Nullable
    public BlockPos getAbsolutePos() {
        return this.absolutePos;
    }
}

