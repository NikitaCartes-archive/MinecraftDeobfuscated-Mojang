package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class EnchantmentTableBlockEntity extends BlockEntity implements Nameable {
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
	private static final RandomSource RANDOM = RandomSource.create();
	private Component name;

	public EnchantmentTableBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.ENCHANTING_TABLE, blockPos, blockState);
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag) {
		super.saveAdditional(compoundTag);
		if (this.hasCustomName()) {
			compoundTag.putString("CustomName", Component.Serializer.toJson(this.name));
		}
	}

	@Override
	public void load(CompoundTag compoundTag) {
		super.load(compoundTag);
		if (compoundTag.contains("CustomName", 8)) {
			this.name = Component.Serializer.fromJson(compoundTag.getString("CustomName"));
		}
	}

	public static void bookAnimationTick(Level level, BlockPos blockPos, BlockState blockState, EnchantmentTableBlockEntity enchantmentTableBlockEntity) {
		enchantmentTableBlockEntity.oOpen = enchantmentTableBlockEntity.open;
		enchantmentTableBlockEntity.oRot = enchantmentTableBlockEntity.rot;
		Player player = level.getNearestPlayer((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, 3.0, false);
		if (player != null) {
			double d = player.getX() - ((double)blockPos.getX() + 0.5);
			double e = player.getZ() - ((double)blockPos.getZ() + 0.5);
			enchantmentTableBlockEntity.tRot = (float)Mth.atan2(e, d);
			enchantmentTableBlockEntity.open += 0.1F;
			if (enchantmentTableBlockEntity.open < 0.5F || RANDOM.nextInt(40) == 0) {
				float f = enchantmentTableBlockEntity.flipT;

				do {
					enchantmentTableBlockEntity.flipT = enchantmentTableBlockEntity.flipT + (float)(RANDOM.nextInt(4) - RANDOM.nextInt(4));
				} while (f == enchantmentTableBlockEntity.flipT);
			}
		} else {
			enchantmentTableBlockEntity.tRot += 0.02F;
			enchantmentTableBlockEntity.open -= 0.1F;
		}

		while (enchantmentTableBlockEntity.rot >= (float) Math.PI) {
			enchantmentTableBlockEntity.rot -= (float) (Math.PI * 2);
		}

		while (enchantmentTableBlockEntity.rot < (float) -Math.PI) {
			enchantmentTableBlockEntity.rot += (float) (Math.PI * 2);
		}

		while (enchantmentTableBlockEntity.tRot >= (float) Math.PI) {
			enchantmentTableBlockEntity.tRot -= (float) (Math.PI * 2);
		}

		while (enchantmentTableBlockEntity.tRot < (float) -Math.PI) {
			enchantmentTableBlockEntity.tRot += (float) (Math.PI * 2);
		}

		float g = enchantmentTableBlockEntity.tRot - enchantmentTableBlockEntity.rot;

		while (g >= (float) Math.PI) {
			g -= (float) (Math.PI * 2);
		}

		while (g < (float) -Math.PI) {
			g += (float) (Math.PI * 2);
		}

		enchantmentTableBlockEntity.rot += g * 0.4F;
		enchantmentTableBlockEntity.open = Mth.clamp(enchantmentTableBlockEntity.open, 0.0F, 1.0F);
		enchantmentTableBlockEntity.time++;
		enchantmentTableBlockEntity.oFlip = enchantmentTableBlockEntity.flip;
		float h = (enchantmentTableBlockEntity.flipT - enchantmentTableBlockEntity.flip) * 0.4F;
		float i = 0.2F;
		h = Mth.clamp(h, -0.2F, 0.2F);
		enchantmentTableBlockEntity.flipA = enchantmentTableBlockEntity.flipA + (h - enchantmentTableBlockEntity.flipA) * 0.9F;
		enchantmentTableBlockEntity.flip = enchantmentTableBlockEntity.flip + enchantmentTableBlockEntity.flipA;
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
