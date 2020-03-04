/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.Dynamic;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public final class NbtUtils {
    private static final Logger LOGGER = LogManager.getLogger();

    @Nullable
    public static GameProfile readGameProfile(CompoundTag compoundTag) {
        String string = null;
        String string2 = null;
        if (compoundTag.contains("Name", 8)) {
            string = compoundTag.getString("Name");
        }
        if (compoundTag.contains("Id", 8)) {
            string2 = compoundTag.getString("Id");
        }
        try {
            UUID uUID;
            try {
                uUID = UUID.fromString(string2);
            } catch (Throwable throwable) {
                uUID = null;
            }
            GameProfile gameProfile = new GameProfile(uUID, string);
            if (compoundTag.contains("Properties", 10)) {
                CompoundTag compoundTag2 = compoundTag.getCompound("Properties");
                for (String string3 : compoundTag2.getAllKeys()) {
                    ListTag listTag = compoundTag2.getList(string3, 10);
                    for (int i = 0; i < listTag.size(); ++i) {
                        CompoundTag compoundTag3 = listTag.getCompound(i);
                        String string4 = compoundTag3.getString("Value");
                        if (compoundTag3.contains("Signature", 8)) {
                            gameProfile.getProperties().put(string3, new com.mojang.authlib.properties.Property(string3, string4, compoundTag3.getString("Signature")));
                            continue;
                        }
                        gameProfile.getProperties().put(string3, new com.mojang.authlib.properties.Property(string3, string4));
                    }
                }
            }
            return gameProfile;
        } catch (Throwable throwable) {
            return null;
        }
    }

    public static CompoundTag writeGameProfile(CompoundTag compoundTag, GameProfile gameProfile) {
        if (!StringUtil.isNullOrEmpty(gameProfile.getName())) {
            compoundTag.putString("Name", gameProfile.getName());
        }
        if (gameProfile.getId() != null) {
            compoundTag.putString("Id", gameProfile.getId().toString());
        }
        if (!gameProfile.getProperties().isEmpty()) {
            CompoundTag compoundTag2 = new CompoundTag();
            for (String string : gameProfile.getProperties().keySet()) {
                ListTag listTag = new ListTag();
                for (com.mojang.authlib.properties.Property property : gameProfile.getProperties().get(string)) {
                    CompoundTag compoundTag3 = new CompoundTag();
                    compoundTag3.putString("Value", property.getValue());
                    if (property.hasSignature()) {
                        compoundTag3.putString("Signature", property.getSignature());
                    }
                    listTag.add(compoundTag3);
                }
                compoundTag2.put(string, listTag);
            }
            compoundTag.put("Properties", compoundTag2);
        }
        return compoundTag;
    }

    @VisibleForTesting
    public static boolean compareNbt(@Nullable Tag tag, @Nullable Tag tag2, boolean bl) {
        if (tag == tag2) {
            return true;
        }
        if (tag == null) {
            return true;
        }
        if (tag2 == null) {
            return false;
        }
        if (!tag.getClass().equals(tag2.getClass())) {
            return false;
        }
        if (tag instanceof CompoundTag) {
            CompoundTag compoundTag = (CompoundTag)tag;
            CompoundTag compoundTag2 = (CompoundTag)tag2;
            for (String string : compoundTag.getAllKeys()) {
                Tag tag3 = compoundTag.get(string);
                if (NbtUtils.compareNbt(tag3, compoundTag2.get(string), bl)) continue;
                return false;
            }
            return true;
        }
        if (tag instanceof ListTag && bl) {
            ListTag listTag = (ListTag)tag;
            ListTag listTag2 = (ListTag)tag2;
            if (listTag.isEmpty()) {
                return listTag2.isEmpty();
            }
            for (int i = 0; i < listTag.size(); ++i) {
                Tag tag4 = listTag.get(i);
                boolean bl2 = false;
                for (int j = 0; j < listTag2.size(); ++j) {
                    if (!NbtUtils.compareNbt(tag4, listTag2.get(j), bl)) continue;
                    bl2 = true;
                    break;
                }
                if (bl2) continue;
                return false;
            }
            return true;
        }
        return tag.equals(tag2);
    }

    public static IntArrayTag createUUIDArray(UUID uUID) {
        long l = uUID.getMostSignificantBits();
        long m = uUID.getLeastSignificantBits();
        return new IntArrayTag(new int[]{(int)(l >> 32), (int)l, (int)(m >> 32), (int)m});
    }

    public static UUID loadUUIDArray(Tag tag) {
        if (tag.getType() != IntArrayTag.TYPE) {
            throw new IllegalArgumentException("Expected UUID-Tag to be of type " + IntArrayTag.TYPE.getName() + ", but found " + tag.getType().getName() + ".");
        }
        int[] is = ((IntArrayTag)tag).getAsIntArray();
        if (is.length != 4) {
            throw new IllegalArgumentException("Expected UUID-Array to be of length 4, but found " + is.length + ".");
        }
        return new UUID((long)is[0] << 32 | (long)is[1] & 0xFFFFFFFFL, (long)is[2] << 32 | (long)is[3] & 0xFFFFFFFFL);
    }

    @Deprecated
    public static CompoundTag createUUIDTag(UUID uUID) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putLong("M", uUID.getMostSignificantBits());
        compoundTag.putLong("L", uUID.getLeastSignificantBits());
        return compoundTag;
    }

    @Deprecated
    public static UUID loadUUIDTag(CompoundTag compoundTag) {
        return new UUID(compoundTag.getLong("M"), compoundTag.getLong("L"));
    }

    public static BlockPos readBlockPos(CompoundTag compoundTag) {
        return new BlockPos(compoundTag.getInt("X"), compoundTag.getInt("Y"), compoundTag.getInt("Z"));
    }

    public static CompoundTag writeBlockPos(BlockPos blockPos) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putInt("X", blockPos.getX());
        compoundTag.putInt("Y", blockPos.getY());
        compoundTag.putInt("Z", blockPos.getZ());
        return compoundTag;
    }

    public static BlockState readBlockState(CompoundTag compoundTag) {
        if (!compoundTag.contains("Name", 8)) {
            return Blocks.AIR.defaultBlockState();
        }
        Block block = Registry.BLOCK.get(new ResourceLocation(compoundTag.getString("Name")));
        BlockState blockState = block.defaultBlockState();
        if (compoundTag.contains("Properties", 10)) {
            CompoundTag compoundTag2 = compoundTag.getCompound("Properties");
            StateDefinition<Block, BlockState> stateDefinition = block.getStateDefinition();
            for (String string : compoundTag2.getAllKeys()) {
                Property<?> property = stateDefinition.getProperty(string);
                if (property == null) continue;
                blockState = NbtUtils.setValueHelper(blockState, property, string, compoundTag2, compoundTag);
            }
        }
        return blockState;
    }

    private static <S extends StateHolder<S>, T extends Comparable<T>> S setValueHelper(S stateHolder, Property<T> property, String string, CompoundTag compoundTag, CompoundTag compoundTag2) {
        Optional<T> optional = property.getValue(compoundTag.getString(string));
        if (optional.isPresent()) {
            return (S)((StateHolder)stateHolder.setValue(property, (Comparable)((Comparable)optional.get())));
        }
        LOGGER.warn("Unable to read property: {} with value: {} for blockstate: {}", (Object)string, (Object)compoundTag.getString(string), (Object)compoundTag2.toString());
        return stateHolder;
    }

    public static CompoundTag writeBlockState(BlockState blockState) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("Name", Registry.BLOCK.getKey(blockState.getBlock()).toString());
        ImmutableMap<Property<?>, Comparable<?>> immutableMap = blockState.getValues();
        if (!immutableMap.isEmpty()) {
            CompoundTag compoundTag2 = new CompoundTag();
            for (Map.Entry entry : immutableMap.entrySet()) {
                Property property = (Property)entry.getKey();
                compoundTag2.putString(property.getName(), NbtUtils.getName(property, (Comparable)entry.getValue()));
            }
            compoundTag.put("Properties", compoundTag2);
        }
        return compoundTag;
    }

    private static <T extends Comparable<T>> String getName(Property<T> property, Comparable<?> comparable) {
        return property.getName(comparable);
    }

    public static CompoundTag update(DataFixer dataFixer, DataFixTypes dataFixTypes, CompoundTag compoundTag, int i) {
        return NbtUtils.update(dataFixer, dataFixTypes, compoundTag, i, SharedConstants.getCurrentVersion().getWorldVersion());
    }

    public static CompoundTag update(DataFixer dataFixer, DataFixTypes dataFixTypes, CompoundTag compoundTag, int i, int j) {
        return dataFixer.update(dataFixTypes.getType(), new Dynamic<CompoundTag>(NbtOps.INSTANCE, compoundTag), i, j).getValue();
    }
}

