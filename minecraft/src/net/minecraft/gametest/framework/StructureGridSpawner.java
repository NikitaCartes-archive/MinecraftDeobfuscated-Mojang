package net.minecraft.gametest.framework;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

public class StructureGridSpawner implements GameTestRunner.StructureSpawner {
	private static final int SPACE_BETWEEN_COLUMNS = 5;
	private static final int SPACE_BETWEEN_ROWS = 6;
	private final int testsPerRow;
	private int currentRowCount;
	private AABB rowBounds;
	private final BlockPos.MutableBlockPos nextTestNorthWestCorner;
	private final BlockPos firstTestNorthWestCorner;

	public StructureGridSpawner(BlockPos blockPos, int i) {
		this.testsPerRow = i;
		this.nextTestNorthWestCorner = blockPos.mutable();
		this.rowBounds = new AABB(this.nextTestNorthWestCorner);
		this.firstTestNorthWestCorner = blockPos;
	}

	@Override
	public Optional<GameTestInfo> spawnStructure(GameTestInfo gameTestInfo) {
		BlockPos blockPos = new BlockPos(this.nextTestNorthWestCorner);
		gameTestInfo.setNorthWestCorner(blockPos);
		gameTestInfo.prepareTestStructure();
		AABB aABB = StructureUtils.getStructureBounds(gameTestInfo.getStructureBlockEntity());
		this.rowBounds = this.rowBounds.minmax(aABB);
		this.nextTestNorthWestCorner.move((int)aABB.getXsize() + 5, 0, 0);
		if (++this.currentRowCount >= this.testsPerRow) {
			this.currentRowCount = 0;
			this.nextTestNorthWestCorner.move(0, 0, (int)this.rowBounds.getZsize() + 6);
			this.nextTestNorthWestCorner.setX(this.firstTestNorthWestCorner.getX());
			this.rowBounds = new AABB(this.nextTestNorthWestCorner);
		}

		return Optional.of(gameTestInfo);
	}
}
