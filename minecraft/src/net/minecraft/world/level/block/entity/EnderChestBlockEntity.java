package net.minecraft.world.level.block.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvironmentInterface;
import net.fabricmc.api.EnvironmentInterfaces;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;

@EnvironmentInterfaces({@EnvironmentInterface(
		value = EnvType.CLIENT,
		itf = LidBlockEntity.class
	)})
public class EnderChestBlockEntity extends BlockEntity implements LidBlockEntity, TickableBlockEntity {
	public float openness;
	public float oOpenness;
	public int openCount;
	private int tickInterval;

	public EnderChestBlockEntity() {
		super(BlockEntityType.ENDER_CHEST);
	}

	@Override
	public void tick() {
		if (++this.tickInterval % 20 * 4 == 0) {
			this.level.blockEvent(this.worldPosition, Blocks.ENDER_CHEST, 1, this.openCount);
		}

		this.oOpenness = this.openness;
		int i = this.worldPosition.getX();
		int j = this.worldPosition.getY();
		int k = this.worldPosition.getZ();
		float f = 0.1F;
		if (this.openCount > 0 && this.openness == 0.0F) {
			double d = (double)i + 0.5;
			double e = (double)k + 0.5;
			this.level.playSound(null, d, (double)j + 0.5, e, SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
		}

		if (this.openCount == 0 && this.openness > 0.0F || this.openCount > 0 && this.openness < 1.0F) {
			float g = this.openness;
			if (this.openCount > 0) {
				this.openness += 0.1F;
			} else {
				this.openness -= 0.1F;
			}

			if (this.openness > 1.0F) {
				this.openness = 1.0F;
			}

			float h = 0.5F;
			if (this.openness < 0.5F && g >= 0.5F) {
				double e = (double)i + 0.5;
				double l = (double)k + 0.5;
				this.level.playSound(null, e, (double)j + 0.5, l, SoundEvents.ENDER_CHEST_CLOSE, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
			}

			if (this.openness < 0.0F) {
				this.openness = 0.0F;
			}
		}
	}

	@Override
	public boolean triggerEvent(int i, int j) {
		if (i == 1) {
			this.openCount = j;
			return true;
		} else {
			return super.triggerEvent(i, j);
		}
	}

	@Override
	public void setRemoved() {
		this.clearCache();
		super.setRemoved();
	}

	public void startOpen() {
		this.openCount++;
		this.level.blockEvent(this.worldPosition, Blocks.ENDER_CHEST, 1, this.openCount);
	}

	public void stopOpen() {
		this.openCount--;
		this.level.blockEvent(this.worldPosition, Blocks.ENDER_CHEST, 1, this.openCount);
	}

	public boolean stillValid(Player player) {
		return this.level.getBlockEntity(this.worldPosition) != this
			? false
			: !(player.distanceToSqr((double)this.worldPosition.getX() + 0.5, (double)this.worldPosition.getY() + 0.5, (double)this.worldPosition.getZ() + 0.5) > 64.0);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public float getOpenNess(float f) {
		return Mth.lerp(f, this.oOpenness, this.openness);
	}
}
