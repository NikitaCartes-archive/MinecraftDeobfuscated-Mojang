package net.minecraft.world.item;

import java.util.Optional;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;

public class CompassItem extends Item implements Vanishable {
	public CompassItem(Item.Properties properties) {
		super(properties);
		this.addProperty(
			new ResourceLocation("angle"),
			new ItemPropertyFunction() {
				private final CompassItem.CompassWobble wobble = new CompassItem.CompassWobble();
				private final CompassItem.CompassWobble wobbleRandom = new CompassItem.CompassWobble();

				@Environment(EnvType.CLIENT)
				@Override
				public float call(ItemStack itemStack, @Nullable Level level, @Nullable LivingEntity livingEntity) {
					if (livingEntity == null && !itemStack.isFramed()) {
						return 0.0F;
					} else {
						boolean bl = livingEntity != null;
						Entity entity = (Entity)(bl ? livingEntity : itemStack.getFrame());
						if (level == null) {
							level = entity.level;
						}

						CompoundTag compoundTag = itemStack.getOrCreateTag();
						BlockPos blockPos = CompassItem.hasLodestoneData(compoundTag)
							? CompassItem.this.getLodestonePosition(level, compoundTag)
							: CompassItem.this.getSpawnPosition(level);
						double g;
						if (blockPos != null) {
							double d = bl ? (double)entity.yRot : CompassItem.getFrameRotation((ItemFrame)entity);
							d = Mth.positiveModulo(d / 360.0, 1.0);
							double e = 0.5 - (d - 0.25);
							boolean bl2 = !bl && entity.getDirection().getAxis().isVertical();
							boolean bl3 = bl2 && entity.getDirection() == Direction.UP;
							double f = CompassItem.getAngleTo(Vec3.atCenterOf(blockPos), entity) / (float) (Math.PI * 2) * (double)(bl3 ? -1 : 1);
							g = 0.5 - (d - 0.25 - f) * (double)(bl2 ? -1 : 1);
							if (bl) {
								this.wobble.update(level, e);
								g -= e - this.wobble.getRotation();
							}
						} else {
							this.wobbleRandom.update(level, Math.random());
							g = this.wobbleRandom.getRotation();
						}

						return Mth.positiveModulo((float)g, 1.0F);
					}
				}
			}
		);
	}

	private static boolean hasLodestoneData(CompoundTag compoundTag) {
		return compoundTag.contains("LodestoneDimension") || compoundTag.contains("LodestonePos");
	}

	private static boolean isLodestoneCompass(ItemStack itemStack) {
		return hasLodestoneData(itemStack.getOrCreateTag());
	}

	@Override
	public boolean isFoil(ItemStack itemStack) {
		return isLodestoneCompass(itemStack) || super.isFoil(itemStack);
	}

	private static Optional<DimensionType> getLodestoneDimension(CompoundTag compoundTag) {
		ResourceLocation resourceLocation = ResourceLocation.tryParse(compoundTag.getString("LodestoneDimension"));
		return resourceLocation != null ? Registry.DIMENSION_TYPE.getOptional(resourceLocation) : Optional.empty();
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	private BlockPos getSpawnPosition(Level level) {
		return level.dimension.isNaturalDimension() ? level.getSharedSpawnPos() : null;
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	private BlockPos getLodestonePosition(Level level, CompoundTag compoundTag) {
		boolean bl = compoundTag.contains("LodestonePos");
		boolean bl2 = compoundTag.contains("LodestoneDimension");
		if (bl && bl2) {
			Optional<DimensionType> optional = getLodestoneDimension(compoundTag);
			if (optional.isPresent() && level.dimension.getType().equals(optional.get())) {
				return NbtUtils.readBlockPos((CompoundTag)compoundTag.get("LodestonePos"));
			}
		}

		return null;
	}

	@Environment(EnvType.CLIENT)
	private static double getFrameRotation(ItemFrame itemFrame) {
		Direction direction = itemFrame.getDirection();
		return direction.getAxis().isVertical()
			? (double)Mth.wrapDegrees(itemFrame.getRotation() * -45)
			: (double)Mth.wrapDegrees(180 + itemFrame.getDirection().get2DDataValue() * 90 + itemFrame.getRotation() * 45);
	}

	@Environment(EnvType.CLIENT)
	private static double getAngleTo(Vec3 vec3, Entity entity) {
		return Math.atan2(vec3.z() - entity.getZ(), vec3.x() - entity.getX());
	}

	@Override
	public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int i, boolean bl) {
		if (!level.isClientSide) {
			CompoundTag compoundTag = itemStack.getOrCreateTag();
			if (hasLodestoneData(compoundTag)) {
				if (compoundTag.contains("LodestoneTracked") && !compoundTag.getBoolean("LodestoneTracked")) {
					return;
				}

				Optional<DimensionType> optional = getLodestoneDimension(compoundTag);
				if (optional.isPresent()
					&& ((DimensionType)optional.get()).equals(level.dimension.getType())
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
		if (useOnContext.level.getBlockState(blockPos).getBlock() == Blocks.LODESTONE) {
			useOnContext.level.playSound(null, blockPos, SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 1.0F, 1.0F);
			CompoundTag compoundTag = useOnContext.itemStack.getOrCreateTag();
			compoundTag.put("LodestonePos", NbtUtils.writeBlockPos(blockPos));
			compoundTag.putString("LodestoneDimension", DimensionType.getName(useOnContext.level.dimension.getType()).toString());
			compoundTag.putBoolean("LodestoneTracked", true);
			return InteractionResult.SUCCESS;
		} else {
			return super.useOn(useOnContext);
		}
	}

	@Override
	public String getDescriptionId(ItemStack itemStack) {
		return isLodestoneCompass(itemStack) ? "item.minecraft.lodestone_compass" : super.getDescriptionId(itemStack);
	}

	static class CompassWobble {
		@Environment(EnvType.CLIENT)
		private double rotation;
		@Environment(EnvType.CLIENT)
		private double deltaRotation;
		@Environment(EnvType.CLIENT)
		private long lastUpdateTick;

		private CompassWobble() {
		}

		@Environment(EnvType.CLIENT)
		public double getRotation() {
			return this.rotation;
		}

		@Environment(EnvType.CLIENT)
		private void update(Level level, double d) {
			if (level.getGameTime() != this.lastUpdateTick) {
				this.lastUpdateTick = level.getGameTime();
				double e = d - this.rotation;
				e = Mth.positiveModulo(e + 0.5, 1.0) - 0.5;
				this.deltaRotation += e * 0.1;
				this.deltaRotation *= 0.8;
				this.rotation = Mth.positiveModulo(this.rotation + this.deltaRotation, 1.0);
			}
		}
	}
}
