package net.minecraft.client.renderer.item;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class ItemProperties {
	private static final Map<ResourceLocation, ItemPropertyFunction> GENERIC_PROPERTIES = Maps.<ResourceLocation, ItemPropertyFunction>newHashMap();
	private static final String TAG_CUSTOM_MODEL_DATA = "CustomModelData";
	private static final ResourceLocation DAMAGED = new ResourceLocation("damaged");
	private static final ResourceLocation DAMAGE = new ResourceLocation("damage");
	private static final ItemPropertyFunction PROPERTY_DAMAGED = (itemStack, clientLevel, livingEntity, i) -> itemStack.isDamaged() ? 1.0F : 0.0F;
	private static final ItemPropertyFunction PROPERTY_DAMAGE = (itemStack, clientLevel, livingEntity, i) -> Mth.clamp(
			(float)itemStack.getDamageValue() / (float)itemStack.getMaxDamage(), 0.0F, 1.0F
		);
	private static final Map<Item, Map<ResourceLocation, ItemPropertyFunction>> PROPERTIES = Maps.<Item, Map<ResourceLocation, ItemPropertyFunction>>newHashMap();

	private static ItemPropertyFunction registerGeneric(ResourceLocation resourceLocation, ItemPropertyFunction itemPropertyFunction) {
		GENERIC_PROPERTIES.put(resourceLocation, itemPropertyFunction);
		return itemPropertyFunction;
	}

	private static void register(Item item, ResourceLocation resourceLocation, ItemPropertyFunction itemPropertyFunction) {
		((Map)PROPERTIES.computeIfAbsent(item, itemx -> Maps.newHashMap())).put(resourceLocation, itemPropertyFunction);
	}

	@Nullable
	public static ItemPropertyFunction getProperty(Item item, ResourceLocation resourceLocation) {
		if (item.getMaxDamage() > 0) {
			if (DAMAGE.equals(resourceLocation)) {
				return PROPERTY_DAMAGE;
			}

			if (DAMAGED.equals(resourceLocation)) {
				return PROPERTY_DAMAGED;
			}
		}

		ItemPropertyFunction itemPropertyFunction = (ItemPropertyFunction)GENERIC_PROPERTIES.get(resourceLocation);
		if (itemPropertyFunction != null) {
			return itemPropertyFunction;
		} else {
			Map<ResourceLocation, ItemPropertyFunction> map = (Map<ResourceLocation, ItemPropertyFunction>)PROPERTIES.get(item);
			return map == null ? null : (ItemPropertyFunction)map.get(resourceLocation);
		}
	}

	static {
		registerGeneric(
			new ResourceLocation("lefthanded"),
			(itemStack, clientLevel, livingEntity, i) -> livingEntity != null && livingEntity.getMainArm() != HumanoidArm.RIGHT ? 1.0F : 0.0F
		);
		registerGeneric(
			new ResourceLocation("cooldown"),
			(itemStack, clientLevel, livingEntity, i) -> livingEntity instanceof Player
					? ((Player)livingEntity).getCooldowns().getCooldownPercent(itemStack.getItem(), 0.0F)
					: 0.0F
		);
		registerGeneric(
			new ResourceLocation("custom_model_data"),
			(itemStack, clientLevel, livingEntity, i) -> itemStack.hasTag() ? (float)itemStack.getTag().getInt("CustomModelData") : 0.0F
		);
		register(Items.BOW, new ResourceLocation("pull"), (itemStack, clientLevel, livingEntity, i) -> {
			if (livingEntity == null) {
				return 0.0F;
			} else {
				return livingEntity.getUseItem() != itemStack ? 0.0F : (float)(itemStack.getUseDuration() - livingEntity.getUseItemRemainingTicks()) / 20.0F;
			}
		});
		register(
			Items.BOW,
			new ResourceLocation("pulling"),
			(itemStack, clientLevel, livingEntity, i) -> livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack ? 1.0F : 0.0F
		);
		register(Items.BUNDLE, new ResourceLocation("filled"), (itemStack, clientLevel, livingEntity, i) -> BundleItem.getFullnessDisplay(itemStack));
		register(Items.CLOCK, new ResourceLocation("time"), new ItemPropertyFunction() {
			private double rotation;
			private double rota;
			private long lastUpdateTick;

			@Override
			public float call(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int i) {
				Entity entity = (Entity)(livingEntity != null ? livingEntity : itemStack.getEntityRepresentation());
				if (entity == null) {
					return 0.0F;
				} else {
					if (clientLevel == null && entity.level instanceof ClientLevel) {
						clientLevel = (ClientLevel)entity.level;
					}

					if (clientLevel == null) {
						return 0.0F;
					} else {
						double d;
						if (clientLevel.dimensionType().natural()) {
							d = (double)clientLevel.getTimeOfDay(1.0F);
						} else {
							d = Math.random();
						}

						d = this.wobble(clientLevel, d);
						return (float)d;
					}
				}
			}

			private double wobble(Level level, double d) {
				if (level.getGameTime() != this.lastUpdateTick) {
					this.lastUpdateTick = level.getGameTime();
					double e = d - this.rotation;
					e = Mth.positiveModulo(e + 0.5, 1.0) - 0.5;
					this.rota += e * 0.1;
					this.rota *= 0.9;
					this.rotation = Mth.positiveModulo(this.rotation + this.rota, 1.0);
				}

				return this.rotation;
			}
		});
		register(
			Items.COMPASS,
			new ResourceLocation("angle"),
			new ItemPropertyFunction() {
				private final ItemProperties.CompassWobble wobble = new ItemProperties.CompassWobble();
				private final ItemProperties.CompassWobble wobbleRandom = new ItemProperties.CompassWobble();

				@Override
				public float call(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int i) {
					Entity entity = (Entity)(livingEntity != null ? livingEntity : itemStack.getEntityRepresentation());
					if (entity == null) {
						return 0.0F;
					} else {
						if (clientLevel == null && entity.level instanceof ClientLevel) {
							clientLevel = (ClientLevel)entity.level;
						}

						BlockPos blockPos = CompassItem.isLodestoneCompass(itemStack)
							? this.getLodestonePosition(clientLevel, itemStack.getOrCreateTag())
							: this.getSpawnPosition(clientLevel);
						long l = clientLevel.getGameTime();
						if (blockPos != null && !(entity.position().distanceToSqr((double)blockPos.getX() + 0.5, entity.position().y(), (double)blockPos.getZ() + 0.5) < 1.0E-5F)
							)
						 {
							boolean bl = livingEntity instanceof Player && ((Player)livingEntity).isLocalPlayer();
							double e = 0.0;
							if (bl) {
								e = (double)livingEntity.getYRot();
							} else if (entity instanceof ItemFrame) {
								e = this.getFrameRotation((ItemFrame)entity);
							} else if (entity instanceof ItemEntity) {
								e = (double)(180.0F - ((ItemEntity)entity).getSpin(0.5F) / (float) (Math.PI * 2) * 360.0F);
							} else if (livingEntity != null) {
								e = (double)livingEntity.yBodyRot;
							}

							e = Mth.positiveModulo(e / 360.0, 1.0);
							double f = this.getAngleTo(Vec3.atCenterOf(blockPos), entity) / (float) (Math.PI * 2);
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

							double d = this.wobbleRandom.rotation + (double)((float)this.hash(i) / 2.1474836E9F);
							return Mth.positiveModulo((float)d, 1.0F);
						}
					}
				}

				private int hash(int i) {
					return i * 1327217883;
				}

				@Nullable
				private BlockPos getSpawnPosition(ClientLevel clientLevel) {
					return clientLevel.dimensionType().natural() ? clientLevel.getSharedSpawnPos() : null;
				}

				@Nullable
				private BlockPos getLodestonePosition(Level level, CompoundTag compoundTag) {
					boolean bl = compoundTag.contains("LodestonePos");
					boolean bl2 = compoundTag.contains("LodestoneDimension");
					if (bl && bl2) {
						Optional<ResourceKey<Level>> optional = CompassItem.getLodestoneDimension(compoundTag);
						if (optional.isPresent() && level.dimension() == optional.get()) {
							return NbtUtils.readBlockPos(compoundTag.getCompound("LodestonePos"));
						}
					}

					return null;
				}

				private double getFrameRotation(ItemFrame itemFrame) {
					Direction direction = itemFrame.getDirection();
					int i = direction.getAxis().isVertical() ? 90 * direction.getAxisDirection().getStep() : 0;
					return (double)Mth.wrapDegrees(180 + direction.get2DDataValue() * 90 + itemFrame.getRotation() * 45 + i);
				}

				private double getAngleTo(Vec3 vec3, Entity entity) {
					return Math.atan2(vec3.z() - entity.getZ(), vec3.x() - entity.getX());
				}
			}
		);
		register(
			Items.CROSSBOW,
			new ResourceLocation("pull"),
			(itemStack, clientLevel, livingEntity, i) -> {
				if (livingEntity == null) {
					return 0.0F;
				} else {
					return CrossbowItem.isCharged(itemStack)
						? 0.0F
						: (float)(itemStack.getUseDuration() - livingEntity.getUseItemRemainingTicks()) / (float)CrossbowItem.getChargeDuration(itemStack);
				}
			}
		);
		register(
			Items.CROSSBOW,
			new ResourceLocation("pulling"),
			(itemStack, clientLevel, livingEntity, i) -> livingEntity != null
						&& livingEntity.isUsingItem()
						&& livingEntity.getUseItem() == itemStack
						&& !CrossbowItem.isCharged(itemStack)
					? 1.0F
					: 0.0F
		);
		register(
			Items.CROSSBOW,
			new ResourceLocation("charged"),
			(itemStack, clientLevel, livingEntity, i) -> livingEntity != null && CrossbowItem.isCharged(itemStack) ? 1.0F : 0.0F
		);
		register(
			Items.CROSSBOW,
			new ResourceLocation("firework"),
			(itemStack, clientLevel, livingEntity, i) -> livingEntity != null
						&& CrossbowItem.isCharged(itemStack)
						&& CrossbowItem.containsChargedProjectile(itemStack, Items.FIREWORK_ROCKET)
					? 1.0F
					: 0.0F
		);
		register(Items.ELYTRA, new ResourceLocation("broken"), (itemStack, clientLevel, livingEntity, i) -> ElytraItem.isFlyEnabled(itemStack) ? 0.0F : 1.0F);
		register(Items.FISHING_ROD, new ResourceLocation("cast"), (itemStack, clientLevel, livingEntity, i) -> {
			if (livingEntity == null) {
				return 0.0F;
			} else {
				boolean bl = livingEntity.getMainHandItem() == itemStack;
				boolean bl2 = livingEntity.getOffhandItem() == itemStack;
				if (livingEntity.getMainHandItem().getItem() instanceof FishingRodItem) {
					bl2 = false;
				}

				return (bl || bl2) && livingEntity instanceof Player && ((Player)livingEntity).fishing != null ? 1.0F : 0.0F;
			}
		});
		register(
			Items.SHIELD,
			new ResourceLocation("blocking"),
			(itemStack, clientLevel, livingEntity, i) -> livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack ? 1.0F : 0.0F
		);
		register(
			Items.TRIDENT,
			new ResourceLocation("throwing"),
			(itemStack, clientLevel, livingEntity, i) -> livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack ? 1.0F : 0.0F
		);
		register(Items.LIGHT, new ResourceLocation("level"), (itemStack, clientLevel, livingEntity, i) -> {
			CompoundTag compoundTag = itemStack.getTagElement("BlockStateTag");

			try {
				if (compoundTag != null) {
					return (float)Integer.parseInt(compoundTag.getString(LightBlock.LEVEL.getName()));
				}
			} catch (NumberFormatException var6) {
			}

			return 15.0F;
		});
	}

	@Environment(EnvType.CLIENT)
	static class CompassWobble {
		private double rotation;
		private double deltaRotation;
		private long lastUpdateTick;

		private CompassWobble() {
		}

		private boolean shouldUpdate(long l) {
			return this.lastUpdateTick != l;
		}

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
