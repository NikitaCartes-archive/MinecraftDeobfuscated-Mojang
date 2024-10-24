package net.minecraft.client.renderer.item;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.LightBlock;

@Environment(EnvType.CLIENT)
public class ItemProperties {
	private static final Map<ResourceLocation, ItemPropertyFunction> GENERIC_PROPERTIES = Maps.<ResourceLocation, ItemPropertyFunction>newHashMap();
	private static final ResourceLocation DAMAGED = ResourceLocation.withDefaultNamespace("damaged");
	private static final ResourceLocation DAMAGE = ResourceLocation.withDefaultNamespace("damage");
	private static final ClampedItemPropertyFunction PROPERTY_DAMAGED = (itemStack, clientLevel, livingEntity, i) -> itemStack.isDamaged() ? 1.0F : 0.0F;
	private static final ClampedItemPropertyFunction PROPERTY_DAMAGE = (itemStack, clientLevel, livingEntity, i) -> Mth.clamp(
			(float)itemStack.getDamageValue() / (float)itemStack.getMaxDamage(), 0.0F, 1.0F
		);
	private static final Map<Item, Map<ResourceLocation, ItemPropertyFunction>> PROPERTIES = Maps.<Item, Map<ResourceLocation, ItemPropertyFunction>>newHashMap();

	private static ClampedItemPropertyFunction registerGeneric(ResourceLocation resourceLocation, ClampedItemPropertyFunction clampedItemPropertyFunction) {
		GENERIC_PROPERTIES.put(resourceLocation, clampedItemPropertyFunction);
		return clampedItemPropertyFunction;
	}

	private static void registerCustomModelData(ItemPropertyFunction itemPropertyFunction) {
		GENERIC_PROPERTIES.put(ResourceLocation.withDefaultNamespace("custom_model_data"), itemPropertyFunction);
	}

	private static void register(Item item, ResourceLocation resourceLocation, ClampedItemPropertyFunction clampedItemPropertyFunction) {
		((Map)PROPERTIES.computeIfAbsent(item, itemx -> Maps.newHashMap())).put(resourceLocation, clampedItemPropertyFunction);
	}

	private static int honeyLevelProperty(ItemStack itemStack) {
		BlockItemStateProperties blockItemStateProperties = itemStack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY);
		Integer integer = blockItemStateProperties.get(BeehiveBlock.HONEY_LEVEL);
		return integer != null && integer == 5 ? 1 : 0;
	}

	@Nullable
	public static ItemPropertyFunction getProperty(ItemStack itemStack, ResourceLocation resourceLocation) {
		if (itemStack.getMaxDamage() > 0) {
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
			Map<ResourceLocation, ItemPropertyFunction> map = (Map<ResourceLocation, ItemPropertyFunction>)PROPERTIES.get(itemStack.getItem());
			return map == null ? null : (ItemPropertyFunction)map.get(resourceLocation);
		}
	}

	static {
		registerGeneric(
			ResourceLocation.withDefaultNamespace("lefthanded"),
			(itemStack, clientLevel, livingEntity, i) -> livingEntity != null && livingEntity.getMainArm() != HumanoidArm.RIGHT ? 1.0F : 0.0F
		);
		registerGeneric(
			ResourceLocation.withDefaultNamespace("cooldown"),
			(itemStack, clientLevel, livingEntity, i) -> livingEntity instanceof Player
					? ((Player)livingEntity).getCooldowns().getCooldownPercent(itemStack, 0.0F)
					: 0.0F
		);
		ClampedItemPropertyFunction clampedItemPropertyFunction = (itemStack, clientLevel, livingEntity, i) -> {
			ArmorTrim armorTrim = itemStack.get(DataComponents.TRIM);
			return armorTrim != null ? armorTrim.material().value().itemModelIndex() : Float.NEGATIVE_INFINITY;
		};
		registerGeneric(ItemModelGenerators.TRIM_TYPE_PREDICATE_ID, clampedItemPropertyFunction);
		registerGeneric(ResourceLocation.withDefaultNamespace("broken"), (itemStack, clientLevel, livingEntity, i) -> itemStack.nextDamageWillBreak() ? 1.0F : 0.0F);
		registerCustomModelData(
			(itemStack, clientLevel, livingEntity, i) -> (float)itemStack.getOrDefault(DataComponents.CUSTOM_MODEL_DATA, CustomModelData.DEFAULT).value()
		);
		register(Items.BOW, ResourceLocation.withDefaultNamespace("pull"), (itemStack, clientLevel, livingEntity, i) -> {
			if (livingEntity == null) {
				return 0.0F;
			} else {
				return livingEntity.getUseItem() != itemStack ? 0.0F : (float)(itemStack.getUseDuration(livingEntity) - livingEntity.getUseItemRemainingTicks()) / 20.0F;
			}
		});
		register(
			Items.BRUSH,
			ResourceLocation.withDefaultNamespace("brushing"),
			(itemStack, clientLevel, livingEntity, i) -> livingEntity != null && livingEntity.getUseItem() == itemStack
					? (float)(livingEntity.getUseItemRemainingTicks() % 10) / 10.0F
					: 0.0F
		);
		register(
			Items.BOW,
			ResourceLocation.withDefaultNamespace("pulling"),
			(itemStack, clientLevel, livingEntity, i) -> livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack ? 1.0F : 0.0F
		);

		for (BundleItem bundleItem : BundleItem.getAllBundleItemColors()) {
			register(
				bundleItem.asItem(), ResourceLocation.withDefaultNamespace("filled"), (itemStack, clientLevel, livingEntity, i) -> BundleItem.getFullnessDisplay(itemStack)
			);
		}

		register(Items.CLOCK, ResourceLocation.withDefaultNamespace("time"), new ClampedItemPropertyFunction() {
			private double rotation;
			private double rota;
			private long lastUpdateTick;

			@Override
			public float unclampedCall(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int i) {
				Entity entity = (Entity)(livingEntity != null ? livingEntity : itemStack.getEntityRepresentation());
				if (entity == null) {
					return 0.0F;
				} else {
					if (clientLevel == null && entity.level() instanceof ClientLevel) {
						clientLevel = (ClientLevel)entity.level();
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
		register(Items.COMPASS, ResourceLocation.withDefaultNamespace("angle"), new CompassItemPropertyFunction((clientLevel, itemStack, entity) -> {
			LodestoneTracker lodestoneTracker = itemStack.get(DataComponents.LODESTONE_TRACKER);
			return lodestoneTracker != null ? (GlobalPos)lodestoneTracker.target().orElse(null) : CompassItem.getSpawnPosition(clientLevel);
		}));
		register(
			Items.RECOVERY_COMPASS,
			ResourceLocation.withDefaultNamespace("angle"),
			new CompassItemPropertyFunction(
				(clientLevel, itemStack, entity) -> entity instanceof Player player ? (GlobalPos)player.getLastDeathLocation().orElse(null) : null
			)
		);
		register(
			Items.CROSSBOW,
			ResourceLocation.withDefaultNamespace("pull"),
			(itemStack, clientLevel, livingEntity, i) -> {
				if (livingEntity == null) {
					return 0.0F;
				} else {
					return CrossbowItem.isCharged(itemStack)
						? 0.0F
						: (float)(itemStack.getUseDuration(livingEntity) - livingEntity.getUseItemRemainingTicks())
							/ (float)CrossbowItem.getChargeDuration(itemStack, livingEntity);
				}
			}
		);
		register(
			Items.CROSSBOW,
			ResourceLocation.withDefaultNamespace("pulling"),
			(itemStack, clientLevel, livingEntity, i) -> livingEntity != null
						&& livingEntity.isUsingItem()
						&& livingEntity.getUseItem() == itemStack
						&& !CrossbowItem.isCharged(itemStack)
					? 1.0F
					: 0.0F
		);
		register(
			Items.CROSSBOW,
			ResourceLocation.withDefaultNamespace("charged"),
			(itemStack, clientLevel, livingEntity, i) -> CrossbowItem.isCharged(itemStack) ? 1.0F : 0.0F
		);
		register(Items.CROSSBOW, ResourceLocation.withDefaultNamespace("firework"), (itemStack, clientLevel, livingEntity, i) -> {
			ChargedProjectiles chargedProjectiles = itemStack.get(DataComponents.CHARGED_PROJECTILES);
			return chargedProjectiles != null && chargedProjectiles.contains(Items.FIREWORK_ROCKET) ? 1.0F : 0.0F;
		});
		register(Items.FISHING_ROD, ResourceLocation.withDefaultNamespace("cast"), (itemStack, clientLevel, livingEntity, i) -> {
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
			ResourceLocation.withDefaultNamespace("blocking"),
			(itemStack, clientLevel, livingEntity, i) -> livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack ? 1.0F : 0.0F
		);
		register(
			Items.TRIDENT,
			ResourceLocation.withDefaultNamespace("throwing"),
			(itemStack, clientLevel, livingEntity, i) -> livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack ? 1.0F : 0.0F
		);
		register(Items.LIGHT, ResourceLocation.withDefaultNamespace("level"), (itemStack, clientLevel, livingEntity, i) -> {
			BlockItemStateProperties blockItemStateProperties = itemStack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY);
			Integer integer = blockItemStateProperties.get(LightBlock.LEVEL);
			return integer != null ? (float)integer.intValue() / 16.0F : 1.0F;
		});
		register(
			Items.GOAT_HORN,
			ResourceLocation.withDefaultNamespace("tooting"),
			(itemStack, clientLevel, livingEntity, i) -> livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack ? 1.0F : 0.0F
		);
		register(
			Items.BEE_NEST, ResourceLocation.withDefaultNamespace("honey_level"), (itemStack, clientLevel, livingEntity, i) -> (float)honeyLevelProperty(itemStack)
		);
		register(
			Items.BEEHIVE, ResourceLocation.withDefaultNamespace("honey_level"), (itemStack, clientLevel, livingEntity, i) -> (float)honeyLevelProperty(itemStack)
		);
	}
}
