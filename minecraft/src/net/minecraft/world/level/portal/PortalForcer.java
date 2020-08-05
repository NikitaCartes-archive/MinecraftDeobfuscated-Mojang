package net.minecraft.world.level.portal;

import java.util.Comparator;
import java.util.Optional;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;

public class PortalForcer {
	private final ServerLevel level;

	public PortalForcer(ServerLevel serverLevel) {
		this.level = serverLevel;
	}

	public Optional<BlockUtil.FoundRectangle> findPortalAround(BlockPos blockPos, boolean bl) {
		PoiManager poiManager = this.level.getPoiManager();
		int i = bl ? 16 : 128;
		poiManager.ensureLoadedAndValid(this.level, blockPos, i);
		Optional<PoiRecord> optional = poiManager.getInSquare(poiType -> poiType == PoiType.NETHER_PORTAL, blockPos, i, PoiManager.Occupancy.ANY)
			.sorted(Comparator.comparingDouble(poiRecord -> poiRecord.getPos().distSqr(blockPos)).thenComparingInt(poiRecord -> poiRecord.getPos().getY()))
			.filter(poiRecord -> this.level.getBlockState(poiRecord.getPos()).hasProperty(BlockStateProperties.HORIZONTAL_AXIS))
			.findFirst();
		return optional.map(
			poiRecord -> {
				BlockPos blockPosx = poiRecord.getPos();
				this.level.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(blockPosx), 3, blockPosx);
				BlockState blockState = this.level.getBlockState(blockPosx);
				return BlockUtil.getLargestRectangleAround(
					blockPosx,
					blockState.getValue(BlockStateProperties.HORIZONTAL_AXIS),
					21,
					Direction.Axis.Y,
					21,
					blockPosxx -> this.level.getBlockState(blockPosxx) == blockState
				);
			}
		);
	}

	public Optional<BlockUtil.FoundRectangle> createPortal(BlockPos blockPos, Direction.Axis axis) {
		Direction direction = Direction.get(Direction.AxisDirection.POSITIVE, axis);
		double d = -1.0;
		BlockPos blockPos2 = null;
		double e = -1.0;
		BlockPos blockPos3 = null;
		WorldBorder worldBorder = this.level.getWorldBorder();
		int i = this.level.getHeight() - 1;
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();

		for (BlockPos.MutableBlockPos mutableBlockPos2 : BlockPos.spiralAround(blockPos, 16, Direction.EAST, Direction.SOUTH)) {
			int j = Math.min(i, this.level.getHeight(Heightmap.Types.MOTION_BLOCKING, mutableBlockPos2.getX(), mutableBlockPos2.getZ()));
			int k = 1;
			if (worldBorder.isWithinBounds(mutableBlockPos2) && worldBorder.isWithinBounds(mutableBlockPos2.move(direction, 1))) {
				mutableBlockPos2.move(direction.getOpposite(), 1);

				for (int l = j; l >= 0; l--) {
					mutableBlockPos2.setY(l);
					if (this.level.isEmptyBlock(mutableBlockPos2)) {
						int m = l;

						while (l > 0 && this.level.isEmptyBlock(mutableBlockPos2.move(Direction.DOWN))) {
							l--;
						}

						if (l + 4 <= i) {
							int n = m - l;
							if (n <= 0 || n >= 3) {
								mutableBlockPos2.setY(l);
								if (this.canHostFrame(mutableBlockPos2, mutableBlockPos, direction, 0)) {
									double f = blockPos.distSqr(mutableBlockPos2);
									if (this.canHostFrame(mutableBlockPos2, mutableBlockPos, direction, -1)
										&& this.canHostFrame(mutableBlockPos2, mutableBlockPos, direction, 1)
										&& (d == -1.0 || d > f)) {
										d = f;
										blockPos2 = mutableBlockPos2.immutable();
									}

									if (d == -1.0 && (e == -1.0 || e > f)) {
										e = f;
										blockPos3 = mutableBlockPos2.immutable();
									}
								}
							}
						}
					}
				}
			}
		}

		if (d == -1.0 && e != -1.0) {
			blockPos2 = blockPos3;
			d = e;
		}

		if (d == -1.0) {
			blockPos2 = new BlockPos(blockPos.getX(), Mth.clamp(blockPos.getY(), 70, this.level.getHeight() - 10), blockPos.getZ()).immutable();
			Direction direction2 = direction.getClockWise();
			if (!worldBorder.isWithinBounds(blockPos2)) {
				return Optional.empty();
			}

			for (int o = -1; o < 2; o++) {
				for (int j = 0; j < 2; j++) {
					for (int k = -1; k < 3; k++) {
						BlockState blockState = k < 0 ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.AIR.defaultBlockState();
						mutableBlockPos.setWithOffset(blockPos2, j * direction.getStepX() + o * direction2.getStepX(), k, j * direction.getStepZ() + o * direction2.getStepZ());
						this.level.setBlockAndUpdate(mutableBlockPos, blockState);
					}
				}
			}
		}

		for (int p = -1; p < 3; p++) {
			for (int o = -1; o < 4; o++) {
				if (p == -1 || p == 2 || o == -1 || o == 3) {
					mutableBlockPos.setWithOffset(blockPos2, p * direction.getStepX(), o, p * direction.getStepZ());
					this.level.setBlock(mutableBlockPos, Blocks.OBSIDIAN.defaultBlockState(), 3);
				}
			}
		}

		BlockState blockState2 = Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, axis);

		for (int ox = 0; ox < 2; ox++) {
			for (int j = 0; j < 3; j++) {
				mutableBlockPos.setWithOffset(blockPos2, ox * direction.getStepX(), j, ox * direction.getStepZ());
				this.level.setBlock(mutableBlockPos, blockState2, 18);
			}
		}

		return Optional.of(new BlockUtil.FoundRectangle(blockPos2.immutable(), 2, 3));
	}

	private boolean canHostFrame(BlockPos blockPos, BlockPos.MutableBlockPos mutableBlockPos, Direction direction, int i) {
		Direction direction2 = direction.getClockWise();

		for (int j = -1; j < 3; j++) {
			for (int k = -1; k < 4; k++) {
				mutableBlockPos.setWithOffset(blockPos, direction.getStepX() * j + direction2.getStepX() * i, k, direction.getStepZ() * j + direction2.getStepZ() * i);
				if (k < 0 && !this.level.getBlockState(mutableBlockPos).getMaterial().isSolid()) {
					return false;
				}

				if (k >= 0 && !this.level.isEmptyBlock(mutableBlockPos)) {
					return false;
				}
			}
		}

		return true;
	}
}
