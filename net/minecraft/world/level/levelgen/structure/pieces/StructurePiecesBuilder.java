/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.pieces;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import org.jetbrains.annotations.Nullable;

public class StructurePiecesBuilder
implements StructurePieceAccessor {
    private final List<StructurePiece> pieces = Lists.newArrayList();

    @Override
    public void addPiece(StructurePiece structurePiece) {
        this.pieces.add(structurePiece);
    }

    @Override
    @Nullable
    public StructurePiece findCollisionPiece(BoundingBox boundingBox) {
        return StructurePiece.findCollisionPiece(this.pieces, boundingBox);
    }

    @Deprecated
    public void offsetPiecesVertically(int i) {
        for (StructurePiece structurePiece : this.pieces) {
            structurePiece.move(0, i, 0);
        }
    }

    @Deprecated
    public int moveBelowSeaLevel(int i, int j, RandomSource randomSource, int k) {
        int l = i - k;
        BoundingBox boundingBox = this.getBoundingBox();
        int m = boundingBox.getYSpan() + j + 1;
        if (m < l) {
            m += randomSource.nextInt(l - m);
        }
        int n = m - boundingBox.maxY();
        this.offsetPiecesVertically(n);
        return n;
    }

    public void moveInsideHeights(RandomSource randomSource, int i, int j) {
        BoundingBox boundingBox = this.getBoundingBox();
        int k = j - i + 1 - boundingBox.getYSpan();
        int l = k > 1 ? i + randomSource.nextInt(k) : i;
        int m = l - boundingBox.minY();
        this.offsetPiecesVertically(m);
    }

    public PiecesContainer build() {
        return new PiecesContainer(this.pieces);
    }

    public void clear() {
        this.pieces.clear();
    }

    public boolean isEmpty() {
        return this.pieces.isEmpty();
    }

    public BoundingBox getBoundingBox() {
        return StructurePiece.createBoundingBox(this.pieces.stream());
    }
}

