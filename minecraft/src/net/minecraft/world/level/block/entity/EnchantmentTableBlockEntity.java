package net.minecraft.world.level.block.entity;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;

public class EnchantmentTableBlockEntity extends BlockEntity implements Nameable, TickableBlockEntity {
	public int time;
	public float flip;
	public float oFlip;
	public float flipT;
	public float flipA;
	public float open;
	public float oOpen;
	public float rot;
	public float oRot;
	public float tRot;
	private static final Random RANDOM = new Random();
	private Component name;

	public EnchantmentTableBlockEntity() {
		super(BlockEntityType.ENCHANTING_TABLE);
	}

	@Override
	public CompoundTag save(CompoundTag compoundTag) {
		super.save(compoundTag);
		if (this.hasCustomName()) {
			compoundTag.putString("CustomName", Component.Serializer.toJson(this.name));
		}

		return compoundTag;
	}

	@Override
	public void load(CompoundTag compoundTag) {
		super.load(compoundTag);
		if (compoundTag.contains("CustomName", 8)) {
			this.name = Component.Serializer.fromJson(compoundTag.getString("CustomName"));
		}
	}

	@Override
	public void tick() {
		this.oOpen = this.open;
		this.oRot = this.rot;
		Player player = this.level
			.getNearestPlayer(
				(double)((float)this.worldPosition.getX() + 0.5F),
				(double)((float)this.worldPosition.getY() + 0.5F),
				(double)((float)this.worldPosition.getZ() + 0.5F),
				3.0,
				false
			);
		if (player != null) {
			double d = player.getX() - (double)((float)this.worldPosition.getX() + 0.5F);
			double e = player.getZ() - (double)((float)this.worldPosition.getZ() + 0.5F);
			this.tRot = (float)Mth.atan2(e, d);
			this.open += 0.1F;
			if (this.open < 0.5F || RANDOM.nextInt(40) == 0) {
				float f = this.flipT;

				do {
					this.flipT = this.flipT + (float)(RANDOM.nextInt(4) - RANDOM.nextInt(4));
				} while (f == this.flipT);
			}
		} else {
			this.tRot += 0.02F;
			this.open -= 0.1F;
		}

		while (this.rot >= (float) Math.PI) {
			this.rot -= (float) (Math.PI * 2);
		}

		while (this.rot < (float) -Math.PI) {
			this.rot += (float) (Math.PI * 2);
		}

		while (this.tRot >= (float) Math.PI) {
			this.tRot -= (float) (Math.PI * 2);
		}

		while (this.tRot < (float) -Math.PI) {
			this.tRot += (float) (Math.PI * 2);
		}

		float g = this.tRot - this.rot;

		while (g >= (float) Math.PI) {
			g -= (float) (Math.PI * 2);
		}

		while (g < (float) -Math.PI) {
			g += (float) (Math.PI * 2);
		}

		this.rot += g * 0.4F;
		this.open = Mth.clamp(this.open, 0.0F, 1.0F);
		this.time++;
		this.oFlip = this.flip;
		float h = (this.flipT - this.flip) * 0.4F;
		float i = 0.2F;
		h = Mth.clamp(h, -0.2F, 0.2F);
		this.flipA = this.flipA + (h - this.flipA) * 0.9F;
		this.flip = this.flip + this.flipA;
	}

	@Override
	public Component getName() {
		return (Component)(this.name != null ? this.name : new TranslatableComponent("container.enchant"));
	}

	public void setCustomName(@Nullable Component component) {
		this.name = component;
	}

	@Nullable
	@Override
	public Component getCustomName() {
		return this.name;
	}
}
