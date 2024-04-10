package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class EnchantingTableBlockEntity extends BlockEntity implements Nameable {
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
	@Nullable
	private Component name;

	public EnchantingTableBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.ENCHANTING_TABLE, blockPos, blockState);
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.saveAdditional(compoundTag, provider);
		if (this.hasCustomName()) {
			compoundTag.putString("CustomName", Component.Serializer.toJson(this.name, provider));
		}
	}

	@Override
	protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.loadAdditional(compoundTag, provider);
		if (compoundTag.contains("CustomName", 8)) {
			this.name = Component.Serializer.fromJson(compoundTag.getString("CustomName"), provider);
		}
	}

	public static void bookAnimationTick(Level level, BlockPos blockPos, BlockState blockState, EnchantingTableBlockEntity enchantingTableBlockEntity) {
		enchantingTableBlockEntity.oOpen = enchantingTableBlockEntity.open;
		enchantingTableBlockEntity.oRot = enchantingTableBlockEntity.rot;
		Player player = level.getNearestPlayer((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, 3.0, false);
		if (player != null) {
			double d = player.getX() - ((double)blockPos.getX() + 0.5);
			double e = player.getZ() - ((double)blockPos.getZ() + 0.5);
			enchantingTableBlockEntity.tRot = (float)Mth.atan2(e, d);
			enchantingTableBlockEntity.open += 0.1F;
			if (enchantingTableBlockEntity.open < 0.5F || RANDOM.nextInt(40) == 0) {
				float f = enchantingTableBlockEntity.flipT;

				do {
					enchantingTableBlockEntity.flipT = enchantingTableBlockEntity.flipT + (float)(RANDOM.nextInt(4) - RANDOM.nextInt(4));
				} while (f == enchantingTableBlockEntity.flipT);
			}
		} else {
			enchantingTableBlockEntity.tRot += 0.02F;
			enchantingTableBlockEntity.open -= 0.1F;
		}

		while (enchantingTableBlockEntity.rot >= (float) Math.PI) {
			enchantingTableBlockEntity.rot -= (float) (Math.PI * 2);
		}

		while (enchantingTableBlockEntity.rot < (float) -Math.PI) {
			enchantingTableBlockEntity.rot += (float) (Math.PI * 2);
		}

		while (enchantingTableBlockEntity.tRot >= (float) Math.PI) {
			enchantingTableBlockEntity.tRot -= (float) (Math.PI * 2);
		}

		while (enchantingTableBlockEntity.tRot < (float) -Math.PI) {
			enchantingTableBlockEntity.tRot += (float) (Math.PI * 2);
		}

		float g = enchantingTableBlockEntity.tRot - enchantingTableBlockEntity.rot;

		while (g >= (float) Math.PI) {
			g -= (float) (Math.PI * 2);
		}

		while (g < (float) -Math.PI) {
			g += (float) (Math.PI * 2);
		}

		enchantingTableBlockEntity.rot += g * 0.4F;
		enchantingTableBlockEntity.open = Mth.clamp(enchantingTableBlockEntity.open, 0.0F, 1.0F);
		enchantingTableBlockEntity.time++;
		enchantingTableBlockEntity.oFlip = enchantingTableBlockEntity.flip;
		float h = (enchantingTableBlockEntity.flipT - enchantingTableBlockEntity.flip) * 0.4F;
		float i = 0.2F;
		h = Mth.clamp(h, -0.2F, 0.2F);
		enchantingTableBlockEntity.flipA = enchantingTableBlockEntity.flipA + (h - enchantingTableBlockEntity.flipA) * 0.9F;
		enchantingTableBlockEntity.flip = enchantingTableBlockEntity.flip + enchantingTableBlockEntity.flipA;
	}

	@Override
	public Component getName() {
		return (Component)(this.name != null ? this.name : Component.translatable("container.enchant"));
	}

	public void setCustomName(@Nullable Component component) {
		this.name = component;
	}

	@Nullable
	@Override
	public Component getCustomName() {
		return this.name;
	}

	@Override
	protected void applyImplicitComponents(BlockEntity.DataComponentInput dataComponentInput) {
		super.applyImplicitComponents(dataComponentInput);
		this.name = dataComponentInput.get(DataComponents.CUSTOM_NAME);
	}

	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder builder) {
		super.collectImplicitComponents(builder);
		builder.set(DataComponents.CUSTOM_NAME, this.name);
	}

	@Override
	public void removeComponentsFromTag(CompoundTag compoundTag) {
		compoundTag.remove("CustomName");
	}
}
