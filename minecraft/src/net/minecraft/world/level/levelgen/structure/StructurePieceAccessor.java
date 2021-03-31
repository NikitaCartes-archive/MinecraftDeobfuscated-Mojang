package net.minecraft.world.level.levelgen.structure;

import javax.annotation.Nullable;

public interface StructurePieceAccessor {
	void addPiece(StructurePiece structurePiece);

	@Nullable
	StructurePiece findCollisionPiece(BoundingBox boundingBox);
}
