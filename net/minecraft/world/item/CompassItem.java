/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import com.mojang.logging.LogUtils;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
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
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class CompassItem
extends Item
implements Vanishable {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String TAG_LODESTONE_POS = "LodestonePos";
    public static final String TAG_LODESTONE_DIMENSION = "LodestoneDimension";
    public static final String TAG_LODESTONE_TRACKED = "LodestoneTracked";

    public CompassItem(Item.Properties properties) {
        super(properties);
    }

    public static boolean isLodestoneCompass(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getTag();
        return compoundTag != null && (compoundTag.contains(TAG_LODESTONE_DIMENSION) || compoundTag.contains(TAG_LODESTONE_POS));
    }

    private static Optional<ResourceKey<Level>> getLodestoneDimension(CompoundTag compoundTag) {
        return Level.RESOURCE_KEY_CODEC.parse(NbtOps.INSTANCE, compoundTag.get(TAG_LODESTONE_DIMENSION)).result();
    }

    @Nullable
    public static GlobalPos getLodestonePosition(CompoundTag compoundTag) {
        Optional<ResourceKey<Level>> optional;
        boolean bl = compoundTag.contains(TAG_LODESTONE_POS);
        boolean bl2 = compoundTag.contains(TAG_LODESTONE_DIMENSION);
        if (bl && bl2 && (optional = CompassItem.getLodestoneDimension(compoundTag)).isPresent()) {
            BlockPos blockPos = NbtUtils.readBlockPos(compoundTag.getCompound(TAG_LODESTONE_POS));
            return GlobalPos.of(optional.get(), blockPos);
        }
        return null;
    }

    @Nullable
    public static GlobalPos getSpawnPosition(Level level) {
        return level.dimensionType().natural() ? GlobalPos.of(level.dimension(), level.getSharedSpawnPos()) : null;
    }

    @Override
    public boolean isFoil(ItemStack itemStack) {
        return CompassItem.isLodestoneCompass(itemStack) || super.isFoil(itemStack);
    }

    @Override
    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int i, boolean bl) {
        if (level.isClientSide) {
            return;
        }
        if (CompassItem.isLodestoneCompass(itemStack)) {
            BlockPos blockPos;
            CompoundTag compoundTag = itemStack.getOrCreateTag();
            if (compoundTag.contains(TAG_LODESTONE_TRACKED) && !compoundTag.getBoolean(TAG_LODESTONE_TRACKED)) {
                return;
            }
            Optional<ResourceKey<Level>> optional = CompassItem.getLodestoneDimension(compoundTag);
            if (optional.isPresent() && optional.get() == level.dimension() && compoundTag.contains(TAG_LODESTONE_POS) && (!level.isInWorldBounds(blockPos = NbtUtils.readBlockPos(compoundTag.getCompound(TAG_LODESTONE_POS))) || !((ServerLevel)level).getPoiManager().existsAtPosition(PoiTypes.LODESTONE, blockPos))) {
                compoundTag.remove(TAG_LODESTONE_POS);
            }
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        BlockPos blockPos = useOnContext.getClickedPos();
        Level level = useOnContext.getLevel();
        if (level.getBlockState(blockPos).is(Blocks.LODESTONE)) {
            boolean bl;
            level.playSound(null, blockPos, SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 1.0f, 1.0f);
            Player player = useOnContext.getPlayer();
            ItemStack itemStack = useOnContext.getItemInHand();
            boolean bl2 = bl = !player.getAbilities().instabuild && itemStack.getCount() == 1;
            if (bl) {
                this.addLodestoneTags(level.dimension(), blockPos, itemStack.getOrCreateTag());
            } else {
                ItemStack itemStack2 = new ItemStack(Items.COMPASS, 1);
                CompoundTag compoundTag = itemStack.hasTag() ? itemStack.getTag().copy() : new CompoundTag();
                itemStack2.setTag(compoundTag);
                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
                this.addLodestoneTags(level.dimension(), blockPos, compoundTag);
                if (!player.getInventory().add(itemStack2)) {
                    player.drop(itemStack2, false);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useOn(useOnContext);
    }

    private void addLodestoneTags(ResourceKey<Level> resourceKey, BlockPos blockPos, CompoundTag compoundTag) {
        compoundTag.put(TAG_LODESTONE_POS, NbtUtils.writeBlockPos(blockPos));
        Level.RESOURCE_KEY_CODEC.encodeStart(NbtOps.INSTANCE, resourceKey).resultOrPartial(LOGGER::error).ifPresent(tag -> compoundTag.put(TAG_LODESTONE_DIMENSION, (Tag)tag));
        compoundTag.putBoolean(TAG_LODESTONE_TRACKED, true);
    }

    @Override
    public String getDescriptionId(ItemStack itemStack) {
        return CompassItem.isLodestoneCompass(itemStack) ? "item.minecraft.lodestone_compass" : super.getDescriptionId(itemStack);
    }
}

