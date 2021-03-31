package net.minecraft.world.level.levelgen.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;

public abstract class ScatteredFeaturePiece extends StructurePiece {
	protected final int width;
	protected final int height;
	protected final int depth;
	protected int heightPosition = -1;

	protected ScatteredFeaturePiece(StructurePieceType structurePieceType, int i, int j, int k, int l, int m, int n, Direction direction) {
		super(structurePieceType, 0, StructurePiece.makeBoundingBox(i, j, k, direction, l, m, n));
		this.width = l;
		this.height = m;
		this.depth = n;
		this.setOrientation(direction);
	}

	protected ScatteredFeaturePiece(StructurePieceType structurePieceType, CompoundTag compoundTag) {
		super(structurePieceType, compoundTag);
		this.width = compoundTag.getInt("Width");
		this.height = compoundTag.getInt("Height");
		this.depth = compoundTag.getInt("Depth");
		this.heightPosition = compoundTag.getInt("HPos");
	}

	@Override
	protected void addAdditionalSaveData(ServerLevel serverLevel, CompoundTag compoundTag) {
		compoundTag.putInt("Width", this.width);
		compoundTag.putInt("Height", this.height);
		compoundTag.putInt("Depth", this.depth);
		compoundTag.putInt("HPos", this.heightPosition);
	}

	protected boolean updateAverageGroundHeight(LevelAccessor levelAccessor, BoundingBox boundingBox, int i) {
		if (this.heightPosition >= 0) {
			return true;
		} else {
			int j = 0;
			int k = 0;
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			for (int l = this.boundingBox.minZ(); l <= this.boundingBox.maxZ(); l++) {
				for (int m = this.boundingBox.minX(); m <= this.boundingBox.maxX(); m++) {
					mutableBlockPos.set(m, 64, l);
					if (boundingBox.isInside(mutableBlockPos)) {
						j += levelAccessor.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, mutableBlockPos).getY();
						k++;
					}
				}
			}

			if (k == 0) {
				return false;
			} else {
				this.heightPosition = j / k;
				this.boundingBox.move(0, this.heightPosition - this.boundingBox.minY() + i, 0);
				return true;
			}
		}
	}
}
