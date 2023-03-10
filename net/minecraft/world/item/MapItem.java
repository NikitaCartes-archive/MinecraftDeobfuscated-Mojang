/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multisets;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ComplexItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

public class MapItem
extends ComplexItem {
    public static final int IMAGE_WIDTH = 128;
    public static final int IMAGE_HEIGHT = 128;
    private static final int DEFAULT_MAP_COLOR = -12173266;
    private static final String TAG_MAP = "map";
    public static final String MAP_SCALE_TAG = "map_scale_direction";
    public static final String MAP_LOCK_TAG = "map_to_lock";

    public MapItem(Item.Properties properties) {
        super(properties);
    }

    public static ItemStack create(Level level, int i, int j, byte b, boolean bl, boolean bl2) {
        ItemStack itemStack = new ItemStack(Items.FILLED_MAP);
        MapItem.createAndStoreSavedData(itemStack, level, i, j, b, bl, bl2, level.dimension());
        return itemStack;
    }

    @Nullable
    public static MapItemSavedData getSavedData(@Nullable Integer integer, Level level) {
        return integer == null ? null : level.getMapData(MapItem.makeKey(integer));
    }

    @Nullable
    public static MapItemSavedData getSavedData(ItemStack itemStack, Level level) {
        Integer integer = MapItem.getMapId(itemStack);
        return MapItem.getSavedData(integer, level);
    }

    @Nullable
    public static Integer getMapId(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getTag();
        return compoundTag != null && compoundTag.contains(TAG_MAP, 99) ? Integer.valueOf(compoundTag.getInt(TAG_MAP)) : null;
    }

    private static int createNewSavedData(Level level, int i, int j, int k, boolean bl, boolean bl2, ResourceKey<Level> resourceKey) {
        MapItemSavedData mapItemSavedData = MapItemSavedData.createFresh(i, j, (byte)k, bl, bl2, resourceKey);
        int l = level.getFreeMapId();
        level.setMapData(MapItem.makeKey(l), mapItemSavedData);
        return l;
    }

    private static void storeMapData(ItemStack itemStack, int i) {
        itemStack.getOrCreateTag().putInt(TAG_MAP, i);
    }

    private static void createAndStoreSavedData(ItemStack itemStack, Level level, int i, int j, int k, boolean bl, boolean bl2, ResourceKey<Level> resourceKey) {
        int l = MapItem.createNewSavedData(level, i, j, k, bl, bl2, resourceKey);
        MapItem.storeMapData(itemStack, l);
    }

    public static String makeKey(int i) {
        return "map_" + i;
    }

    public void update(Level level, Entity entity, MapItemSavedData mapItemSavedData) {
        if (level.dimension() != mapItemSavedData.dimension || !(entity instanceof Player)) {
            return;
        }
        int i = 1 << mapItemSavedData.scale;
        int j = mapItemSavedData.centerX;
        int k = mapItemSavedData.centerZ;
        int l = Mth.floor(entity.getX() - (double)j) / i + 64;
        int m = Mth.floor(entity.getZ() - (double)k) / i + 64;
        int n = 128 / i;
        if (level.dimensionType().hasCeiling()) {
            n /= 2;
        }
        MapItemSavedData.HoldingPlayer holdingPlayer = mapItemSavedData.getHoldingPlayer((Player)entity);
        ++holdingPlayer.step;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();
        boolean bl = false;
        for (int o = l - n + 1; o < l + n; ++o) {
            if ((o & 0xF) != (holdingPlayer.step & 0xF) && !bl) continue;
            bl = false;
            double d = 0.0;
            for (int p = m - n - 1; p < m + n; ++p) {
                double f;
                if (o < 0 || p < -1 || o >= 128 || p >= 128) continue;
                int q = Mth.square(o - l) + Mth.square(p - m);
                boolean bl2 = q > (n - 2) * (n - 2);
                int r = (j / i + o - 64) * i;
                int s = (k / i + p - 64) * i;
                LinkedHashMultiset<MaterialColor> multiset = LinkedHashMultiset.create();
                LevelChunk levelChunk = level.getChunk(SectionPos.blockToSectionCoord(r), SectionPos.blockToSectionCoord(s));
                if (levelChunk.isEmpty()) continue;
                int t = 0;
                double e = 0.0;
                if (level.dimensionType().hasCeiling()) {
                    u = r + s * 231871;
                    if (((u = u * u * 31287121 + u * 11) >> 20 & 1) == 0) {
                        multiset.add(Blocks.DIRT.defaultBlockState().getMapColor(level, BlockPos.ZERO), 10);
                    } else {
                        multiset.add(Blocks.STONE.defaultBlockState().getMapColor(level, BlockPos.ZERO), 100);
                    }
                    e = 100.0;
                } else {
                    for (u = 0; u < i; ++u) {
                        for (int v = 0; v < i; ++v) {
                            BlockState blockState;
                            mutableBlockPos.set(r + u, 0, s + v);
                            int w = levelChunk.getHeight(Heightmap.Types.WORLD_SURFACE, mutableBlockPos.getX(), mutableBlockPos.getZ()) + 1;
                            if (w > level.getMinBuildHeight() + 1) {
                                do {
                                    mutableBlockPos.setY(--w);
                                } while ((blockState = levelChunk.getBlockState(mutableBlockPos)).getMapColor(level, mutableBlockPos) == MaterialColor.NONE && w > level.getMinBuildHeight());
                                if (w > level.getMinBuildHeight() && !blockState.getFluidState().isEmpty()) {
                                    BlockState blockState2;
                                    int x = w - 1;
                                    mutableBlockPos2.set(mutableBlockPos);
                                    do {
                                        mutableBlockPos2.setY(x--);
                                        blockState2 = levelChunk.getBlockState(mutableBlockPos2);
                                        ++t;
                                    } while (x > level.getMinBuildHeight() && !blockState2.getFluidState().isEmpty());
                                    blockState = this.getCorrectStateForFluidBlock(level, blockState, mutableBlockPos);
                                }
                            } else {
                                blockState = Blocks.BEDROCK.defaultBlockState();
                            }
                            mapItemSavedData.checkBanners(level, mutableBlockPos.getX(), mutableBlockPos.getZ());
                            e += (double)w / (double)(i * i);
                            multiset.add(blockState.getMapColor(level, mutableBlockPos));
                        }
                    }
                }
                MaterialColor materialColor = Iterables.getFirst(Multisets.copyHighestCountFirst(multiset), MaterialColor.NONE);
                MaterialColor.Brightness brightness = materialColor == MaterialColor.WATER ? ((f = (double)(t /= i * i) * 0.1 + (double)(o + p & 1) * 0.2) < 0.5 ? MaterialColor.Brightness.HIGH : (f > 0.9 ? MaterialColor.Brightness.LOW : MaterialColor.Brightness.NORMAL)) : ((f = (e - d) * 4.0 / (double)(i + 4) + ((double)(o + p & 1) - 0.5) * 0.4) > 0.6 ? MaterialColor.Brightness.HIGH : (f < -0.6 ? MaterialColor.Brightness.LOW : MaterialColor.Brightness.NORMAL));
                d = e;
                if (p < 0 || q >= n * n || bl2 && (o + p & 1) == 0) continue;
                bl |= mapItemSavedData.updateColor(o, p, materialColor.getPackedId(brightness));
            }
        }
    }

    private BlockState getCorrectStateForFluidBlock(Level level, BlockState blockState, BlockPos blockPos) {
        FluidState fluidState = blockState.getFluidState();
        if (!fluidState.isEmpty() && !blockState.isFaceSturdy(level, blockPos, Direction.UP)) {
            return fluidState.createLegacyBlock();
        }
        return blockState;
    }

    private static boolean isBiomeWatery(boolean[] bls, int i, int j) {
        return bls[j * 128 + i];
    }

    public static void renderBiomePreviewMap(ServerLevel serverLevel, ItemStack itemStack) {
        int o;
        int n;
        MapItemSavedData mapItemSavedData = MapItem.getSavedData(itemStack, (Level)serverLevel);
        if (mapItemSavedData == null) {
            return;
        }
        if (serverLevel.dimension() != mapItemSavedData.dimension) {
            return;
        }
        int i = 1 << mapItemSavedData.scale;
        int j = mapItemSavedData.centerX;
        int k = mapItemSavedData.centerZ;
        boolean[] bls = new boolean[16384];
        int l = j / i - 64;
        int m = k / i - 64;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (n = 0; n < 128; ++n) {
            for (o = 0; o < 128; ++o) {
                Holder<Biome> holder = serverLevel.getBiome(mutableBlockPos.set((l + o) * i, 0, (m + n) * i));
                bls[n * 128 + o] = holder.is(BiomeTags.WATER_ON_MAP_OUTLINES);
            }
        }
        for (n = 1; n < 127; ++n) {
            for (o = 1; o < 127; ++o) {
                int p = 0;
                for (int q = -1; q < 2; ++q) {
                    for (int r = -1; r < 2; ++r) {
                        if (q == 0 && r == 0 || !MapItem.isBiomeWatery(bls, n + q, o + r)) continue;
                        ++p;
                    }
                }
                MaterialColor.Brightness brightness = MaterialColor.Brightness.LOWEST;
                MaterialColor materialColor = MaterialColor.NONE;
                if (MapItem.isBiomeWatery(bls, n, o)) {
                    materialColor = MaterialColor.COLOR_ORANGE;
                    if (p > 7 && o % 2 == 0) {
                        switch ((n + (int)(Mth.sin((float)o + 0.0f) * 7.0f)) / 8 % 5) {
                            case 0: 
                            case 4: {
                                brightness = MaterialColor.Brightness.LOW;
                                break;
                            }
                            case 1: 
                            case 3: {
                                brightness = MaterialColor.Brightness.NORMAL;
                                break;
                            }
                            case 2: {
                                brightness = MaterialColor.Brightness.HIGH;
                            }
                        }
                    } else if (p > 7) {
                        materialColor = MaterialColor.NONE;
                    } else if (p > 5) {
                        brightness = MaterialColor.Brightness.NORMAL;
                    } else if (p > 3) {
                        brightness = MaterialColor.Brightness.LOW;
                    } else if (p > 1) {
                        brightness = MaterialColor.Brightness.LOW;
                    }
                } else if (p > 0) {
                    materialColor = MaterialColor.COLOR_BROWN;
                    brightness = p > 3 ? MaterialColor.Brightness.NORMAL : MaterialColor.Brightness.LOWEST;
                }
                if (materialColor == MaterialColor.NONE) continue;
                mapItemSavedData.setColor(n, o, materialColor.getPackedId(brightness));
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int i, boolean bl) {
        if (level.isClientSide) {
            return;
        }
        MapItemSavedData mapItemSavedData = MapItem.getSavedData(itemStack, level);
        if (mapItemSavedData == null) {
            return;
        }
        if (entity instanceof Player) {
            Player player = (Player)entity;
            mapItemSavedData.tickCarriedBy(player, itemStack);
        }
        if (!mapItemSavedData.locked && (bl || entity instanceof Player && ((Player)entity).getOffhandItem() == itemStack)) {
            this.update(level, entity, mapItemSavedData);
        }
    }

    @Override
    @Nullable
    public Packet<?> getUpdatePacket(ItemStack itemStack, Level level, Player player) {
        Integer integer = MapItem.getMapId(itemStack);
        MapItemSavedData mapItemSavedData = MapItem.getSavedData(integer, level);
        if (mapItemSavedData != null) {
            return mapItemSavedData.getUpdatePacket(integer, player);
        }
        return null;
    }

    @Override
    public void onCraftedBy(ItemStack itemStack, Level level, Player player) {
        CompoundTag compoundTag = itemStack.getTag();
        if (compoundTag != null && compoundTag.contains(MAP_SCALE_TAG, 99)) {
            MapItem.scaleMap(itemStack, level, compoundTag.getInt(MAP_SCALE_TAG));
            compoundTag.remove(MAP_SCALE_TAG);
        } else if (compoundTag != null && compoundTag.contains(MAP_LOCK_TAG, 1) && compoundTag.getBoolean(MAP_LOCK_TAG)) {
            MapItem.lockMap(level, itemStack);
            compoundTag.remove(MAP_LOCK_TAG);
        }
    }

    private static void scaleMap(ItemStack itemStack, Level level, int i) {
        MapItemSavedData mapItemSavedData = MapItem.getSavedData(itemStack, level);
        if (mapItemSavedData != null) {
            int j = level.getFreeMapId();
            level.setMapData(MapItem.makeKey(j), mapItemSavedData.scaled(i));
            MapItem.storeMapData(itemStack, j);
        }
    }

    public static void lockMap(Level level, ItemStack itemStack) {
        MapItemSavedData mapItemSavedData = MapItem.getSavedData(itemStack, level);
        if (mapItemSavedData != null) {
            int i = level.getFreeMapId();
            String string = MapItem.makeKey(i);
            MapItemSavedData mapItemSavedData2 = mapItemSavedData.locked();
            level.setMapData(string, mapItemSavedData2);
            MapItem.storeMapData(itemStack, i);
        }
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        byte b;
        boolean bl;
        Integer integer = MapItem.getMapId(itemStack);
        MapItemSavedData mapItemSavedData = level == null ? null : MapItem.getSavedData(integer, level);
        CompoundTag compoundTag = itemStack.getTag();
        if (compoundTag != null) {
            bl = compoundTag.getBoolean(MAP_LOCK_TAG);
            b = compoundTag.getByte(MAP_SCALE_TAG);
        } else {
            bl = false;
            b = 0;
        }
        if (mapItemSavedData != null && (mapItemSavedData.locked || bl)) {
            list.add(Component.translatable("filled_map.locked", integer).withStyle(ChatFormatting.GRAY));
        }
        if (tooltipFlag.isAdvanced()) {
            if (mapItemSavedData != null) {
                if (!bl && b == 0) {
                    list.add(Component.translatable("filled_map.id", integer).withStyle(ChatFormatting.GRAY));
                }
                int i = Math.min(mapItemSavedData.scale + b, 4);
                list.add(Component.translatable("filled_map.scale", 1 << i).withStyle(ChatFormatting.GRAY));
                list.add(Component.translatable("filled_map.level", i, 4).withStyle(ChatFormatting.GRAY));
            } else {
                list.add(Component.translatable("filled_map.unknown").withStyle(ChatFormatting.GRAY));
            }
        }
    }

    public static int getColor(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getTagElement("display");
        if (compoundTag != null && compoundTag.contains("MapColor", 99)) {
            int i = compoundTag.getInt("MapColor");
            return 0xFF000000 | i & 0xFFFFFF;
        }
        return -12173266;
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        BlockState blockState = useOnContext.getLevel().getBlockState(useOnContext.getClickedPos());
        if (blockState.is(BlockTags.BANNERS)) {
            MapItemSavedData mapItemSavedData;
            if (!useOnContext.getLevel().isClientSide && (mapItemSavedData = MapItem.getSavedData(useOnContext.getItemInHand(), useOnContext.getLevel())) != null && !mapItemSavedData.toggleBanner(useOnContext.getLevel(), useOnContext.getClickedPos())) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.sidedSuccess(useOnContext.getLevel().isClientSide);
        }
        return super.useOn(useOnContext);
    }
}

