/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure;

import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import org.jetbrains.annotations.Nullable;

public interface StructurePieceAccessor {
    public void addPiece(StructurePiece var1);

    @Nullable
    public StructurePiece findCollisionPiece(BoundingBox var1);
}

