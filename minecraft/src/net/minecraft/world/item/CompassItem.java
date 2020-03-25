package net.minecraft.world.item;

import java.util.Optional;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
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

public class CompassItem extends Item {
	public CompassItem(Item.Properties properties) {
		super(properties);
		this.addProperty(new ResourceLocation("angle"), new ItemPropertyFunction() {
			@Environment(EnvType.CLIENT)
			private double rotation;
			@Environment(EnvType.CLIENT)
			private double rota;
			@Environment(EnvType.CLIENT)
			private long lastUpdateTick;

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
					boolean bl2 = CompassItem.hasLodestoneData(compoundTag);
					BlockPos blockPos = bl2 ? CompassItem.this.getLodestonePosition(level, compoundTag) : CompassItem.this.getSpawnPosition(level);
					double f;
					if (blockPos != null) {
						double d = bl ? (double)entity.yRot : CompassItem.getFrameRotation((ItemFrame)entity);
						d = Mth.positiveModulo(d / 360.0, 1.0);
						double e = CompassItem.getAngleTo(Vec3.atCenterOf(blockPos), entity) / (float) (Math.PI * 2);
						f = 0.5 - (d - 0.25 - e);
					} else {
						f = Math.random();
					}

					if (bl && !bl2) {
						f = this.wobble(level, f);
					}

					return Mth.positiveModulo((float)f, 1.0F);
				}
			}

			@Environment(EnvType.CLIENT)
			private double wobble(Level level, double d) {
				if (level.getGameTime() != this.lastUpdateTick) {
					this.lastUpdateTick = level.getGameTime();
					double e = d - this.rotation;
					e = Mth.positiveModulo(e + 0.5, 1.0) - 0.5;
					this.rota += e * 0.1;
					this.rota *= 0.8;
					this.rotation = Mth.positiveModulo(this.rotation + this.rota, 1.0);
				}

				return this.rotation;
			}
		});
	}

	private static boolean hasLodestoneData(CompoundTag compoundTag) {
		return compoundTag.contains("LodestoneDimension") || compoundTag.contains("LodestonePos");
	}

	private static boolean isLodestoneCompass(ItemStack itemStack) {
		return hasLodestoneData(itemStack.getOrCreateTag());
	}

	@Override
	public boolean isFoil(ItemStack itemStack) {
		return isLodestoneCompass(itemStack);
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
		boolean bl2 = compoundTag.contains("LodestonePos");
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
		return (double)Mth.wrapDegrees(180 + itemFrame.getDirection().get2DDataValue() * 90);
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
			useOnContext.level.playSound(null, blockPos, SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.NEUTRAL, 1.0F, 1.0F);
			CompoundTag compoundTag = useOnContext.itemStack.getOrCreateTag();
			compoundTag.put("LodestonePos", NbtUtils.writeBlockPos(blockPos));
			compoundTag.putString("LodestoneDimension", DimensionType.getName(useOnContext.level.dimension.getType()).toString());
		}

		return super.useOn(useOnContext);
	}

	@Override
	public String getDescriptionId(ItemStack itemStack) {
		return isLodestoneCompass(itemStack) ? "item.minecraft.lodestone_compass" : super.getDescriptionId(itemStack);
	}
}
