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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.Dimension;
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
					Entity entity = (Entity)(livingEntity != null ? livingEntity : itemStack.getEntityRepresentation());
					if (entity == null) {
						return 0.0F;
					} else {
						if (level == null) {
							level = entity.level;
						}

						BlockPos blockPos = CompassItem.isLodestoneCompass(itemStack)
							? CompassItem.this.getLodestonePosition(level, itemStack.getOrCreateTag())
							: CompassItem.this.getSpawnPosition(level);
						long l = level.getGameTime();
						if (blockPos != null && !(entity.position().distanceToSqr((double)blockPos.getX() + 0.5, entity.position().y(), (double)blockPos.getZ() + 0.5) < 1.0E-5F)
							)
						 {
							boolean bl = livingEntity instanceof Player && ((Player)livingEntity).isLocalPlayer();
							double e = 0.0;
							if (bl) {
								e = (double)livingEntity.yRot;
							} else if (entity instanceof ItemFrame) {
								e = CompassItem.getFrameRotation((ItemFrame)entity);
							} else if (entity instanceof ItemEntity) {
								e = (double)(180.0F - ((ItemEntity)entity).getSpin(0.5F) / (float) (Math.PI * 2) * 360.0F);
							} else if (livingEntity != null) {
								e = (double)livingEntity.yBodyRot;
							}

							e = Mth.positiveModulo(e / 360.0, 1.0);
							double f = CompassItem.getAngleTo(Vec3.atCenterOf(blockPos), entity) / (float) (Math.PI * 2);
							double g;
							if (bl) {
								if (this.wobble.shouldUpdate(l)) {
									this.wobble.update(l, 0.5 - (e - 0.25));
								}

								g = f + this.wobble.rotation;
							} else {
								g = 0.5 - (e - 0.25 - f);
							}

							return Mth.positiveModulo((float)g, 1.0F);
						} else {
							if (this.wobbleRandom.shouldUpdate(l)) {
								this.wobbleRandom.update(l, Math.random());
							}

							double d = this.wobbleRandom.rotation + (double)((float)itemStack.hashCode() / 2.1474836E9F);
							return Mth.positiveModulo((float)d, 1.0F);
						}
					}
				}
			}
		);
	}

	private static boolean isLodestoneCompass(ItemStack itemStack) {
		CompoundTag compoundTag = itemStack.getTag();
		return compoundTag != null && (compoundTag.contains("LodestoneDimension") || compoundTag.contains("LodestonePos"));
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
		if (useOnContext.level.getBlockState(blockPos).getBlock() != Blocks.LODESTONE) {
			return super.useOn(useOnContext);
		} else {
			useOnContext.level.playSound(null, blockPos, SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 1.0F, 1.0F);
			boolean bl = !useOnContext.player.abilities.instabuild && useOnContext.itemStack.getCount() == 1;
			if (bl) {
				this.addLodestoneTags(useOnContext.level.dimension, blockPos, useOnContext.itemStack.getOrCreateTag());
			} else {
				ItemStack itemStack = new ItemStack(Items.COMPASS, 1);
				CompoundTag compoundTag = useOnContext.itemStack.hasTag() ? useOnContext.itemStack.getTag().copy() : new CompoundTag();
				itemStack.setTag(compoundTag);
				if (!useOnContext.player.abilities.instabuild) {
					useOnContext.itemStack.shrink(1);
				}

				this.addLodestoneTags(useOnContext.level.dimension, blockPos, compoundTag);
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

	@Environment(EnvType.CLIENT)
	private static double getFrameRotation(ItemFrame itemFrame) {
		Direction direction = itemFrame.getDirection();
		int i = direction.getAxis().isVertical() ? 90 * direction.getAxisDirection().getStep() : 0;
		return (double)Mth.wrapDegrees(180 + direction.get2DDataValue() * 90 + itemFrame.getRotation() * 45 + i);
	}

	@Environment(EnvType.CLIENT)
	private static double getAngleTo(Vec3 vec3, Entity entity) {
		return Math.atan2(vec3.z() - entity.getZ(), vec3.x() - entity.getX());
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
		private boolean shouldUpdate(long l) {
			return this.lastUpdateTick != l;
		}

		@Environment(EnvType.CLIENT)
		private void update(long l, double d) {
			this.lastUpdateTick = l;
			double e = d - this.rotation;
			e = Mth.positiveModulo(e + 0.5, 1.0) - 0.5;
			this.deltaRotation += e * 0.1;
			this.deltaRotation *= 0.8;
			this.rotation = Mth.positiveModulo(this.rotation + this.deltaRotation, 1.0);
		}
	}
}
