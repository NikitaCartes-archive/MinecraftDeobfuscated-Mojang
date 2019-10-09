package net.minecraft.world.level.border;

import com.google.common.collect.Lists;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WorldBorder {
	private final List<BorderChangeListener> listeners = Lists.<BorderChangeListener>newArrayList();
	private double damagePerBlock = 0.2;
	private double damageSafeZone = 5.0;
	private int warningTime = 15;
	private int warningBlocks = 5;
	private double centerX;
	private double centerZ;
	private int absoluteMaxSize = 29999984;
	private WorldBorder.BorderExtent extent = new WorldBorder.StaticBorderExtent(6.0E7);

	public boolean isWithinBounds(BlockPos blockPos) {
		return (double)(blockPos.getX() + 1) > this.getMinX()
			&& (double)blockPos.getX() < this.getMaxX()
			&& (double)(blockPos.getZ() + 1) > this.getMinZ()
			&& (double)blockPos.getZ() < this.getMaxZ();
	}

	public boolean isWithinBounds(ChunkPos chunkPos) {
		return (double)chunkPos.getMaxBlockX() > this.getMinX()
			&& (double)chunkPos.getMinBlockX() < this.getMaxX()
			&& (double)chunkPos.getMaxBlockZ() > this.getMinZ()
			&& (double)chunkPos.getMinBlockZ() < this.getMaxZ();
	}

	public boolean isWithinBounds(AABB aABB) {
		return aABB.maxX > this.getMinX() && aABB.minX < this.getMaxX() && aABB.maxZ > this.getMinZ() && aABB.minZ < this.getMaxZ();
	}

	public double getDistanceToBorder(Entity entity) {
		return this.getDistanceToBorder(entity.getX(), entity.getZ());
	}

	public VoxelShape getCollisionShape() {
		return this.extent.getCollisionShape();
	}

	public double getDistanceToBorder(double d, double e) {
		double f = e - this.getMinZ();
		double g = this.getMaxZ() - e;
		double h = d - this.getMinX();
		double i = this.getMaxX() - d;
		double j = Math.min(h, i);
		j = Math.min(j, f);
		return Math.min(j, g);
	}

	@Environment(EnvType.CLIENT)
	public BorderStatus getStatus() {
		return this.extent.getStatus();
	}

	public double getMinX() {
		return this.extent.getMinX();
	}

	public double getMinZ() {
		return this.extent.getMinZ();
	}

	public double getMaxX() {
		return this.extent.getMaxX();
	}

	public double getMaxZ() {
		return this.extent.getMaxZ();
	}

	public double getCenterX() {
		return this.centerX;
	}

	public double getCenterZ() {
		return this.centerZ;
	}

	public void setCenter(double d, double e) {
		this.centerX = d;
		this.centerZ = e;
		this.extent.onCenterChange();

		for (BorderChangeListener borderChangeListener : this.getListeners()) {
			borderChangeListener.onBorderCenterSet(this, d, e);
		}
	}

	public double getSize() {
		return this.extent.getSize();
	}

	public long getLerpRemainingTime() {
		return this.extent.getLerpRemainingTime();
	}

	public double getLerpTarget() {
		return this.extent.getLerpTarget();
	}

	public void setSize(double d) {
		this.extent = new WorldBorder.StaticBorderExtent(d);

		for (BorderChangeListener borderChangeListener : this.getListeners()) {
			borderChangeListener.onBorderSizeSet(this, d);
		}
	}

	public void lerpSizeBetween(double d, double e, long l) {
		this.extent = (WorldBorder.BorderExtent)(d == e ? new WorldBorder.StaticBorderExtent(e) : new WorldBorder.MovingBorderExtent(d, e, l));

		for (BorderChangeListener borderChangeListener : this.getListeners()) {
			borderChangeListener.onBorderSizeLerping(this, d, e, l);
		}
	}

	protected List<BorderChangeListener> getListeners() {
		return Lists.<BorderChangeListener>newArrayList(this.listeners);
	}

	public void addListener(BorderChangeListener borderChangeListener) {
		this.listeners.add(borderChangeListener);
	}

	public void setAbsoluteMaxSize(int i) {
		this.absoluteMaxSize = i;
		this.extent.onAbsoluteMaxSizeChange();
	}

	public int getAbsoluteMaxSize() {
		return this.absoluteMaxSize;
	}

	public double getDamageSafeZone() {
		return this.damageSafeZone;
	}

	public void setDamageSafeZone(double d) {
		this.damageSafeZone = d;

		for (BorderChangeListener borderChangeListener : this.getListeners()) {
			borderChangeListener.onBorderSetDamageSafeZOne(this, d);
		}
	}

	public double getDamagePerBlock() {
		return this.damagePerBlock;
	}

	public void setDamagePerBlock(double d) {
		this.damagePerBlock = d;

		for (BorderChangeListener borderChangeListener : this.getListeners()) {
			borderChangeListener.onBorderSetDamagePerBlock(this, d);
		}
	}

	@Environment(EnvType.CLIENT)
	public double getLerpSpeed() {
		return this.extent.getLerpSpeed();
	}

	public int getWarningTime() {
		return this.warningTime;
	}

	public void setWarningTime(int i) {
		this.warningTime = i;

		for (BorderChangeListener borderChangeListener : this.getListeners()) {
			borderChangeListener.onBorderSetWarningTime(this, i);
		}
	}

	public int getWarningBlocks() {
		return this.warningBlocks;
	}

	public void setWarningBlocks(int i) {
		this.warningBlocks = i;

		for (BorderChangeListener borderChangeListener : this.getListeners()) {
			borderChangeListener.onBorderSetWarningBlocks(this, i);
		}
	}

	public void tick() {
		this.extent = this.extent.update();
	}

	public void saveWorldBorderData(LevelData levelData) {
		levelData.setBorderSize(this.getSize());
		levelData.setBorderX(this.getCenterX());
		levelData.setBorderZ(this.getCenterZ());
		levelData.setBorderSafeZone(this.getDamageSafeZone());
		levelData.setBorderDamagePerBlock(this.getDamagePerBlock());
		levelData.setBorderWarningBlocks(this.getWarningBlocks());
		levelData.setBorderWarningTime(this.getWarningTime());
		levelData.setBorderSizeLerpTarget(this.getLerpTarget());
		levelData.setBorderSizeLerpTime(this.getLerpRemainingTime());
	}

	public void readBorderData(LevelData levelData) {
		this.setCenter(levelData.getBorderX(), levelData.getBorderZ());
		this.setDamagePerBlock(levelData.getBorderDamagePerBlock());
		this.setDamageSafeZone(levelData.getBorderSafeZone());
		this.setWarningBlocks(levelData.getBorderWarningBlocks());
		this.setWarningTime(levelData.getBorderWarningTime());
		if (levelData.getBorderSizeLerpTime() > 0L) {
			this.lerpSizeBetween(levelData.getBorderSize(), levelData.getBorderSizeLerpTarget(), levelData.getBorderSizeLerpTime());
		} else {
			this.setSize(levelData.getBorderSize());
		}
	}

	interface BorderExtent {
		double getMinX();

		double getMaxX();

		double getMinZ();

		double getMaxZ();

		double getSize();

		@Environment(EnvType.CLIENT)
		double getLerpSpeed();

		long getLerpRemainingTime();

		double getLerpTarget();

		@Environment(EnvType.CLIENT)
		BorderStatus getStatus();

		void onAbsoluteMaxSizeChange();

		void onCenterChange();

		WorldBorder.BorderExtent update();

		VoxelShape getCollisionShape();
	}

	class MovingBorderExtent implements WorldBorder.BorderExtent {
		private final double from;
		private final double to;
		private final long lerpEnd;
		private final long lerpBegin;
		private final double lerpDuration;

		private MovingBorderExtent(double d, double e, long l) {
			this.from = d;
			this.to = e;
			this.lerpDuration = (double)l;
			this.lerpBegin = Util.getMillis();
			this.lerpEnd = this.lerpBegin + l;
		}

		@Override
		public double getMinX() {
			return Math.max(WorldBorder.this.getCenterX() - this.getSize() / 2.0, (double)(-WorldBorder.this.absoluteMaxSize));
		}

		@Override
		public double getMinZ() {
			return Math.max(WorldBorder.this.getCenterZ() - this.getSize() / 2.0, (double)(-WorldBorder.this.absoluteMaxSize));
		}

		@Override
		public double getMaxX() {
			return Math.min(WorldBorder.this.getCenterX() + this.getSize() / 2.0, (double)WorldBorder.this.absoluteMaxSize);
		}

		@Override
		public double getMaxZ() {
			return Math.min(WorldBorder.this.getCenterZ() + this.getSize() / 2.0, (double)WorldBorder.this.absoluteMaxSize);
		}

		@Override
		public double getSize() {
			double d = (double)(Util.getMillis() - this.lerpBegin) / this.lerpDuration;
			return d < 1.0 ? Mth.lerp(d, this.from, this.to) : this.to;
		}

		@Environment(EnvType.CLIENT)
		@Override
		public double getLerpSpeed() {
			return Math.abs(this.from - this.to) / (double)(this.lerpEnd - this.lerpBegin);
		}

		@Override
		public long getLerpRemainingTime() {
			return this.lerpEnd - Util.getMillis();
		}

		@Override
		public double getLerpTarget() {
			return this.to;
		}

		@Environment(EnvType.CLIENT)
		@Override
		public BorderStatus getStatus() {
			return this.to < this.from ? BorderStatus.SHRINKING : BorderStatus.GROWING;
		}

		@Override
		public void onCenterChange() {
		}

		@Override
		public void onAbsoluteMaxSizeChange() {
		}

		@Override
		public WorldBorder.BorderExtent update() {
			return (WorldBorder.BorderExtent)(this.getLerpRemainingTime() <= 0L ? WorldBorder.this.new StaticBorderExtent(this.to) : this);
		}

		@Override
		public VoxelShape getCollisionShape() {
			return Shapes.join(
				Shapes.INFINITY,
				Shapes.box(
					Math.floor(this.getMinX()),
					Double.NEGATIVE_INFINITY,
					Math.floor(this.getMinZ()),
					Math.ceil(this.getMaxX()),
					Double.POSITIVE_INFINITY,
					Math.ceil(this.getMaxZ())
				),
				BooleanOp.ONLY_FIRST
			);
		}
	}

	class StaticBorderExtent implements WorldBorder.BorderExtent {
		private final double size;
		private double minX;
		private double minZ;
		private double maxX;
		private double maxZ;
		private VoxelShape shape;

		public StaticBorderExtent(double d) {
			this.size = d;
			this.updateBox();
		}

		@Override
		public double getMinX() {
			return this.minX;
		}

		@Override
		public double getMaxX() {
			return this.maxX;
		}

		@Override
		public double getMinZ() {
			return this.minZ;
		}

		@Override
		public double getMaxZ() {
			return this.maxZ;
		}

		@Override
		public double getSize() {
			return this.size;
		}

		@Environment(EnvType.CLIENT)
		@Override
		public BorderStatus getStatus() {
			return BorderStatus.STATIONARY;
		}

		@Environment(EnvType.CLIENT)
		@Override
		public double getLerpSpeed() {
			return 0.0;
		}

		@Override
		public long getLerpRemainingTime() {
			return 0L;
		}

		@Override
		public double getLerpTarget() {
			return this.size;
		}

		private void updateBox() {
			this.minX = Math.max(WorldBorder.this.getCenterX() - this.size / 2.0, (double)(-WorldBorder.this.absoluteMaxSize));
			this.minZ = Math.max(WorldBorder.this.getCenterZ() - this.size / 2.0, (double)(-WorldBorder.this.absoluteMaxSize));
			this.maxX = Math.min(WorldBorder.this.getCenterX() + this.size / 2.0, (double)WorldBorder.this.absoluteMaxSize);
			this.maxZ = Math.min(WorldBorder.this.getCenterZ() + this.size / 2.0, (double)WorldBorder.this.absoluteMaxSize);
			this.shape = Shapes.join(
				Shapes.INFINITY,
				Shapes.box(
					Math.floor(this.getMinX()),
					Double.NEGATIVE_INFINITY,
					Math.floor(this.getMinZ()),
					Math.ceil(this.getMaxX()),
					Double.POSITIVE_INFINITY,
					Math.ceil(this.getMaxZ())
				),
				BooleanOp.ONLY_FIRST
			);
		}

		@Override
		public void onAbsoluteMaxSizeChange() {
			this.updateBox();
		}

		@Override
		public void onCenterChange() {
			this.updateBox();
		}

		@Override
		public WorldBorder.BorderExtent update() {
			return this;
		}

		@Override
		public VoxelShape getCollisionShape() {
			return this.shape;
		}
	}
}
