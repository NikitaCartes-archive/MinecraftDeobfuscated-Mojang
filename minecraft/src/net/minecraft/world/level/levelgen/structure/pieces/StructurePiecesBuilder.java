package net.minecraft.world.level.levelgen.structure.pieces;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;

public class StructurePiecesBuilder implements StructurePieceAccessor {
	private final List<StructurePiece> pieces = Lists.<StructurePiece>newArrayList();

	@Override
	public void addPiece(StructurePiece structurePiece) {
		this.pieces.add(structurePiece);
	}

	public void addAll(Collection<StructurePiece> collection) {
		this.pieces.addAll(collection);
	}

	@Nullable
	@Override
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
	public void moveBelowSeaLevel(int i, int j, Random random, int k) {
		int l = i - k;
		BoundingBox boundingBox = this.getBoundingBox();
		int m = boundingBox.getYSpan() + j + 1;
		if (m < l) {
			m += random.nextInt(l - m);
		}

		int n = m - boundingBox.maxY();
		this.offsetPiecesVertically(n);
	}

	public void moveInsideHeights(Random random, int i, int j) {
		BoundingBox boundingBox = this.getBoundingBox();
		int k = j - i + 1 - boundingBox.getYSpan();
		int l;
		if (k > 1) {
			l = i + random.nextInt(k);
		} else {
			l = i;
		}

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
