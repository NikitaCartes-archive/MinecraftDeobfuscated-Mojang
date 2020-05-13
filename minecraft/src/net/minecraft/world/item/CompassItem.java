package net.minecraft.world.item;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.Dimension;
import net.minecraft.world.level.dimension.DimensionType;

public class CompassItem extends Item implements Vanishable {
	public CompassItem(Item.Properties properties) {
		super(properties);
	}

	public static boolean isLodestoneCompass(ItemStack itemStack) {
		CompoundTag compoundTag = itemStack.getTag();
		return compoundTag != null && (compoundTag.contains("LodestoneDimension") || compoundTag.contains("LodestonePos"));
	}

	@Override
	public boolean isFoil(ItemStack itemStack) {
		return isLodestoneCompass(itemStack) || super.isFoil(itemStack);
	}

	public static Optional<DimensionType> getLodestoneDimension(CompoundTag compoundTag) {
		ResourceLocation resourceLocation = ResourceLocation.tryParse(compoundTag.getString("LodestoneDimension"));
		return resourceLocation != null ? Registry.DIMENSION_TYPE.getOptional(resourceLocation) : Optional.empty();
	}

	@Override
	public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int i, boolean bl) {
		if (!level.isClientSide) {
			if (isLodestoneCompass(itemStack)) {
				CompoundTag compoundTag = itemStack.getOrCreateTag();
				if (compoundTag.contains("LodestoneTracked") && !compoundTag.getBoolean("LodestoneTracked")) {
					return;
				}

				Optional<DimensionType> optional = getLodestoneDimension(compoundTag);
				if (optional.isPresent()
					&& ((DimensionType)optional.get()).equals(level.dimensionType())
					&& compoundTag.contains("LodestonePos")
					&& !((ServerLevel)level).getPoiManager().existsAtPosition(PoiType.LODESTONE, NbtUtils.readBlockPos((CompoundTag)compoundTag.get("LodestonePos")))) {
					compoundTag.remove("LodestonePos");
				}
			}
		}
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		BlockPos blockPos = useOnContext.hitResult.getBlockPos();
		if (!useOnContext.level.getBlockState(blockPos).is(Blocks.LODESTONE)) {
			return super.useOn(useOnContext);
		} else {
			useOnContext.level.playSound(null, blockPos, SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 1.0F, 1.0F);
			boolean bl = !useOnContext.player.abilities.instabuild && useOnContext.itemStack.getCount() == 1;
			if (bl) {
				this.addLodestoneTags(useOnContext.level.getDimension(), blockPos, useOnContext.itemStack.getOrCreateTag());
			} else {
				ItemStack itemStack = new ItemStack(Items.COMPASS, 1);
				CompoundTag compoundTag = useOnContext.itemStack.hasTag() ? useOnContext.itemStack.getTag().copy() : new CompoundTag();
				itemStack.setTag(compoundTag);
				if (!useOnContext.player.abilities.instabuild) {
					useOnContext.itemStack.shrink(1);
				}

				this.addLodestoneTags(useOnContext.level.getDimension(), blockPos, compoundTag);
				if (!useOnContext.player.inventory.add(itemStack)) {
					useOnContext.player.drop(itemStack, false);
				}
			}

			return InteractionResult.SUCCESS;
		}
	}

	private void addLodestoneTags(Dimension dimension, BlockPos blockPos, CompoundTag compoundTag) {
		compoundTag.put("LodestonePos", NbtUtils.writeBlockPos(blockPos));
		compoundTag.putString("LodestoneDimension", DimensionType.getName(dimension.getType()).toString());
		compoundTag.putBoolean("LodestoneTracked", true);
	}

	@Override
	public String getDescriptionId(ItemStack itemStack) {
		return isLodestoneCompass(itemStack) ? "item.minecraft.lodestone_compass" : super.getDescriptionId(itemStack);
	}
}
