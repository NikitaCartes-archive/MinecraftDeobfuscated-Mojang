package net.minecraft.world.level.levelgen.structure;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;

public abstract class ScatteredFeaturePiece extends StructurePiece {
	protected final int width;
	protected final int height;
	protected final int depth;
	protected int heightPosition = -1;

	protected ScatteredFeaturePiece(StructurePieceType structurePieceType, Random random, int i, int j, int k, int l, int m, int n) {
		super(structurePieceType, 0);
		this.width = l;
		this.height = m;
		this.depth = n;
		this.setOrientation(Direction.Plane.HORIZONTAL.getRandomDirection(random));
		if (this.getOrientation().getAxis() == Direction.Axis.Z) {
			this.boundingBox = new BoundingBox(i, j, k, i + l - 1, j + m - 1, k + n - 1);
		} else {
			this.boundingBox = new BoundingBox(i, j, k, i + n - 1, j + m - 1, k + l - 1);
		}
	}

	protected ScatteredFeaturePiece(StructurePieceType structurePieceType, CompoundTag compoundTag) {
		super(structurePieceType, compoundTag);
		this.width = compoundTag.getInt("Width");
		this.height = compoundTag.getInt("Height");
		this.depth = compoundTag.getInt("Depth");
		this.heightPosition = compoundTag.getInt("HPos");
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compoundTag) {
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

			for (int l = this.boundingBox.z0; l <= this.boundingBox.z1; l++) {
				for (int m = this.boundingBox.x0; m <= this.boundingBox.x1; m++) {
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
				this.boundingBox.move(0, this.heightPosition - this.boundingBox.y0 + i, 0);
				return true;
			}
		}
	}
}
