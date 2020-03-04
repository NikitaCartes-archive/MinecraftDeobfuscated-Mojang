package net.minecraft.world.entity.vehicle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class MinecartSpawner extends AbstractMinecart {
	private final BaseSpawner spawner = new BaseSpawner() {
		@Override
		public void broadcastEvent(int i) {
			MinecartSpawner.this.level.broadcastEntityEvent(MinecartSpawner.this, (byte)i);
		}

		@Override
		public Level getLevel() {
			return MinecartSpawner.this.level;
		}

		@Override
		public BlockPos getPos() {
			return MinecartSpawner.this.blockPosition();
		}
	};

	public MinecartSpawner(EntityType<? extends MinecartSpawner> entityType, Level level) {
		super(entityType, level);
	}

	public MinecartSpawner(Level level, double d, double e, double f) {
		super(EntityType.SPAWNER_MINECART, level, d, e, f);
	}

	@Override
	public AbstractMinecart.Type getMinecartType() {
		return AbstractMinecart.Type.SPAWNER;
	}

	@Override
	public BlockState getDefaultDisplayBlockState() {
		return Blocks.SPAWNER.defaultBlockState();
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.spawner.load(compoundTag);
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		this.spawner.save(compoundTag);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void handleEntityEvent(byte b) {
		this.spawner.onEventTriggered(b);
	}

	@Override
	public void tick() {
		super.tick();
		this.spawner.tick();
	}

	@Override
	public boolean onlyOpCanSetNbt() {
		return true;
	}
}
