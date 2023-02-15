/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.item;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.CompassItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.core.Holder;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LightBlock;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ItemProperties {
    private static final Map<ResourceLocation, ItemPropertyFunction> GENERIC_PROPERTIES = Maps.newHashMap();
    private static final String TAG_CUSTOM_MODEL_DATA = "CustomModelData";
    private static final ResourceLocation DAMAGED = new ResourceLocation("damaged");
    private static final ResourceLocation DAMAGE = new ResourceLocation("damage");
    private static final ClampedItemPropertyFunction PROPERTY_DAMAGED = (itemStack, clientLevel, livingEntity, i) -> itemStack.isDamaged() ? 1.0f : 0.0f;
    private static final ClampedItemPropertyFunction PROPERTY_DAMAGE = (itemStack, clientLevel, livingEntity, i) -> Mth.clamp((float)itemStack.getDamageValue() / (float)itemStack.getMaxDamage(), 0.0f, 1.0f);
    private static final Map<Item, Map<ResourceLocation, ItemPropertyFunction>> PROPERTIES = Maps.newHashMap();

    private static ClampedItemPropertyFunction registerGeneric(ResourceLocation resourceLocation, ClampedItemPropertyFunction clampedItemPropertyFunction) {
        GENERIC_PROPERTIES.put(resourceLocation, clampedItemPropertyFunction);
        return clampedItemPropertyFunction;
    }

    private static void registerCustomModelData(ItemPropertyFunction itemPropertyFunction) {
        GENERIC_PROPERTIES.put(new ResourceLocation("custom_model_data"), itemPropertyFunction);
    }

    private static void register(Item item2, ResourceLocation resourceLocation, ClampedItemPropertyFunction clampedItemPropertyFunction) {
        PROPERTIES.computeIfAbsent(item2, item -> Maps.newHashMap()).put(resourceLocation, clampedItemPropertyFunction);
    }

    @Nullable
    public static ItemPropertyFunction getProperty(Item item, ResourceLocation resourceLocation) {
        ItemPropertyFunction itemPropertyFunction;
        if (item.getMaxDamage() > 0) {
            if (DAMAGE.equals(resourceLocation)) {
                return PROPERTY_DAMAGE;
            }
            if (DAMAGED.equals(resourceLocation)) {
                return PROPERTY_DAMAGED;
            }
        }
        if ((itemPropertyFunction = GENERIC_PROPERTIES.get(resourceLocation)) != null) {
            return itemPropertyFunction;
        }
        Map<ResourceLocation, ItemPropertyFunction> map = PROPERTIES.get(item);
        if (map == null) {
            return null;
        }
        return map.get(resourceLocation);
    }

    static {
        ItemProperties.registerGeneric(new ResourceLocation("lefthanded"), (itemStack, clientLevel, livingEntity, i) -> livingEntity == null || livingEntity.getMainArm() == HumanoidArm.RIGHT ? 0.0f : 1.0f);
        ItemProperties.registerGeneric(new ResourceLocation("cooldown"), (itemStack, clientLevel, livingEntity, i) -> livingEntity instanceof Player ? ((Player)livingEntity).getCooldowns().getCooldownPercent(itemStack.getItem(), 0.0f) : 0.0f);
        ClampedItemPropertyFunction clampedItemPropertyFunction = (itemStack, clientLevel, livingEntity, i) -> {
            if (!itemStack.is(ItemTags.TRIMMABLE_ARMOR)) {
                return Float.NEGATIVE_INFINITY;
            }
            if (clientLevel == null) {
                return 0.0f;
            }
            if (!clientLevel.enabledFeatures().contains(FeatureFlags.UPDATE_1_20)) {
                return Float.NEGATIVE_INFINITY;
            }
            return ArmorTrim.getTrim(clientLevel.registryAccess(), itemStack).map(ArmorTrim::material).map(Holder::value).map(TrimMaterial::itemModelIndex).orElse(Float.valueOf(0.0f)).floatValue();
        };
        ItemProperties.registerGeneric(ItemModelGenerators.TRIM_TYPE_PREDICATE_ID, clampedItemPropertyFunction);
        ItemProperties.registerCustomModelData((itemStack, clientLevel, livingEntity, i) -> itemStack.hasTag() ? (float)itemStack.getTag().getInt(TAG_CUSTOM_MODEL_DATA) : 0.0f);
        ItemProperties.register(Items.BOW, new ResourceLocation("pull"), (itemStack, clientLevel, livingEntity, i) -> {
            if (livingEntity == null) {
                return 0.0f;
            }
            if (livingEntity.getUseItem() != itemStack) {
                return 0.0f;
            }
            return (float)(itemStack.getUseDuration() - livingEntity.getUseItemRemainingTicks()) / 20.0f;
        });
        ItemProperties.register(Items.BRUSH, new ResourceLocation("brushing"), (itemStack, clientLevel, livingEntity, i) -> {
            if (livingEntity == null || livingEntity.getUseItem() != itemStack) {
                return 0.0f;
            }
            return (float)(livingEntity.getUseItemRemainingTicks() % 10) / 10.0f;
        });
        ItemProperties.register(Items.BOW, new ResourceLocation("pulling"), (itemStack, clientLevel, livingEntity, i) -> livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack ? 1.0f : 0.0f);
        ItemProperties.register(Items.BUNDLE, new ResourceLocation("filled"), (itemStack, clientLevel, livingEntity, i) -> BundleItem.getFullnessDisplay(itemStack));
        ItemProperties.register(Items.CLOCK, new ResourceLocation("time"), new ClampedItemPropertyFunction(){
            private double rotation;
            private double rota;
            private long lastUpdateTick;

            @Override
            public float unclampedCall(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int i) {
                Entity entity;
                Entity entity2 = entity = livingEntity != null ? livingEntity : itemStack.getEntityRepresentation();
                if (entity == null) {
                    return 0.0f;
                }
                if (clientLevel == null && entity.level instanceof ClientLevel) {
                    clientLevel = (ClientLevel)entity.level;
                }
                if (clientLevel == null) {
                    return 0.0f;
                }
                double d = clientLevel.dimensionType().natural() ? (double)clientLevel.getTimeOfDay(1.0f) : Math.random();
                d = this.wobble(clientLevel, d);
                return (float)d;
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
        ItemProperties.register(Items.COMPASS, new ResourceLocation("angle"), new CompassItemPropertyFunction((clientLevel, itemStack, entity) -> {
            if (CompassItem.isLodestoneCompass(itemStack)) {
                return CompassItem.getLodestonePosition(itemStack.getOrCreateTag());
            }
            return CompassItem.getSpawnPosition(clientLevel);
        }));
        ItemProperties.register(Items.RECOVERY_COMPASS, new ResourceLocation("angle"), new CompassItemPropertyFunction((clientLevel, itemStack, entity) -> {
            if (entity instanceof Player) {
                Player player = (Player)entity;
                return player.getLastDeathLocation().orElse(null);
            }
            return null;
        }));
        ItemProperties.register(Items.CROSSBOW, new ResourceLocation("pull"), (itemStack, clientLevel, livingEntity, i) -> {
            if (livingEntity == null) {
                return 0.0f;
            }
            if (CrossbowItem.isCharged(itemStack)) {
                return 0.0f;
            }
            return (float)(itemStack.getUseDuration() - livingEntity.getUseItemRemainingTicks()) / (float)CrossbowItem.getChargeDuration(itemStack);
        });
        ItemProperties.register(Items.CROSSBOW, new ResourceLocation("pulling"), (itemStack, clientLevel, livingEntity, i) -> livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack && !CrossbowItem.isCharged(itemStack) ? 1.0f : 0.0f);
        ItemProperties.register(Items.CROSSBOW, new ResourceLocation("charged"), (itemStack, clientLevel, livingEntity, i) -> livingEntity != null && CrossbowItem.isCharged(itemStack) ? 1.0f : 0.0f);
        ItemProperties.register(Items.CROSSBOW, new ResourceLocation("firework"), (itemStack, clientLevel, livingEntity, i) -> livingEntity != null && CrossbowItem.isCharged(itemStack) && CrossbowItem.containsChargedProjectile(itemStack, Items.FIREWORK_ROCKET) ? 1.0f : 0.0f);
        ItemProperties.register(Items.ELYTRA, new ResourceLocation("broken"), (itemStack, clientLevel, livingEntity, i) -> ElytraItem.isFlyEnabled(itemStack) ? 0.0f : 1.0f);
        ItemProperties.register(Items.FISHING_ROD, new ResourceLocation("cast"), (itemStack, clientLevel, livingEntity, i) -> {
            boolean bl2;
            if (livingEntity == null) {
                return 0.0f;
            }
            boolean bl = livingEntity.getMainHandItem() == itemStack;
            boolean bl3 = bl2 = livingEntity.getOffhandItem() == itemStack;
            if (livingEntity.getMainHandItem().getItem() instanceof FishingRodItem) {
                bl2 = false;
            }
            return (bl || bl2) && livingEntity instanceof Player && ((Player)livingEntity).fishing != null ? 1.0f : 0.0f;
        });
        ItemProperties.register(Items.SHIELD, new ResourceLocation("blocking"), (itemStack, clientLevel, livingEntity, i) -> livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack ? 1.0f : 0.0f);
        ItemProperties.register(Items.TRIDENT, new ResourceLocation("throwing"), (itemStack, clientLevel, livingEntity, i) -> livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack ? 1.0f : 0.0f);
        ItemProperties.register(Items.LIGHT, new ResourceLocation("level"), (itemStack, clientLevel, livingEntity, i) -> {
            CompoundTag compoundTag = itemStack.getTagElement("BlockStateTag");
            try {
                Tag tag;
                if (compoundTag != null && (tag = compoundTag.get(LightBlock.LEVEL.getName())) != null) {
                    return (float)Integer.parseInt(tag.getAsString()) / 16.0f;
                }
            } catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
            return 1.0f;
        });
        ItemProperties.register(Items.GOAT_HORN, new ResourceLocation("tooting"), (itemStack, clientLevel, livingEntity, i) -> livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack ? 1.0f : 0.0f);
    }
}

