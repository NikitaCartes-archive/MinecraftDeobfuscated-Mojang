/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseOnContext;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.DimensionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CompassItem
extends Item
implements Vanishable {
    private static final Logger LOGGER = LogManager.getLogger();

    public CompassItem(Item.Properties properties) {
        super(properties);
    }

    public static boolean isLodestoneCompass(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getTag();
        return compoundTag != null && (compoundTag.contains("LodestoneDimension") || compoundTag.contains("LodestonePos"));
    }

    @Override
    public boolean isFoil(ItemStack itemStack) {
        return CompassItem.isLodestoneCompass(itemStack) || super.isFoil(itemStack);
    }

    public static Optional<ResourceKey<DimensionType>> getLodestoneDimension(CompoundTag compoundTag) {
        return DimensionType.RESOURCE_KEY_CODEC.parse(NbtOps.INSTANCE, compoundTag.get("LodestoneDimension")).result();
    }

    @Override
    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int i, boolean bl) {
        if (level.isClientSide) {
            return;
        }
        if (CompassItem.isLodestoneCompass(itemStack)) {
            CompoundTag compoundTag = itemStack.getOrCreateTag();
            if (compoundTag.contains("LodestoneTracked") && !compoundTag.getBoolean("LodestoneTracked")) {
                return;
            }
            Optional<ResourceKey<DimensionType>> optional = CompassItem.getLodestoneDimension(compoundTag);
            if (optional.isPresent() && optional.get() == level.dimension() && compoundTag.contains("LodestonePos") && !((ServerLevel)level).getPoiManager().existsAtPosition(PoiType.LODESTONE, NbtUtils.readBlockPos(compoundTag.getCompound("LodestonePos")))) {
                compoundTag.remove("LodestonePos");
            }
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        BlockPos blockPos = useOnContext.hitResult.getBlockPos();
        if (useOnContext.level.getBlockState(blockPos).is(Blocks.LODESTONE)) {
            boolean bl;
            useOnContext.level.playSound(null, blockPos, SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 1.0f, 1.0f);
            boolean bl2 = bl = !useOnContext.player.abilities.instabuild && useOnContext.itemStack.getCount() == 1;
            if (bl) {
                this.addLodestoneTags(useOnContext.level.registryAccess(), useOnContext.level.dimensionType(), blockPos, useOnContext.itemStack.getOrCreateTag());
            } else {
                ItemStack itemStack = new ItemStack(Items.COMPASS, 1);
                CompoundTag compoundTag = useOnContext.itemStack.hasTag() ? useOnContext.itemStack.getTag().copy() : new CompoundTag();
                itemStack.setTag(compoundTag);
                if (!useOnContext.player.abilities.instabuild) {
                    useOnContext.itemStack.shrink(1);
                }
                this.addLodestoneTags(useOnContext.level.registryAccess(), useOnContext.level.dimensionType(), blockPos, compoundTag);
                if (!useOnContext.player.inventory.add(itemStack)) {
                    useOnContext.player.drop(itemStack, false);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.useOn(useOnContext);
    }

    private void addLodestoneTags(RegistryAccess registryAccess, DimensionType dimensionType, BlockPos blockPos, CompoundTag compoundTag) {
        compoundTag.put("LodestonePos", NbtUtils.writeBlockPos(blockPos));
        registryAccess.dimensionTypes().encodeStart(NbtOps.INSTANCE, dimensionType).resultOrPartial(LOGGER::error).ifPresent(tag -> compoundTag.put("LodestoneDimension", (Tag)tag));
        compoundTag.putBoolean("LodestoneTracked", true);
    }

    @Override
    public String getDescriptionId(ItemStack itemStack) {
        return CompassItem.isLodestoneCompass(itemStack) ? "item.minecraft.lodestone_compass" : super.getDescriptionId(itemStack);
    }
}

