/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multisets;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
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
import net.minecraft.world.item.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

public class MapItem
extends ComplexItem {
    public MapItem(Item.Properties properties) {
        super(properties);
    }

    public static ItemStack create(Level level, int i, int j, byte b, boolean bl, boolean bl2) {
        ItemStack itemStack = new ItemStack(Items.FILLED_MAP);
        MapItem.createAndStoreSavedData(itemStack, level, i, j, b, bl, bl2, level.dimension.getType());
        return itemStack;
    }

    @Nullable
    public static MapItemSavedData getSavedData(ItemStack itemStack, Level level) {
        return level.getMapData(MapItem.makeKey(MapItem.getMapId(itemStack)));
    }

    @Nullable
    public static MapItemSavedData getOrCreateSavedData(ItemStack itemStack, Level level) {
        MapItemSavedData mapItemSavedData = MapItem.getSavedData(itemStack, level);
        if (mapItemSavedData == null && !level.isClientSide) {
            mapItemSavedData = MapItem.createAndStoreSavedData(itemStack, level, level.getLevelData().getXSpawn(), level.getLevelData().getZSpawn(), 3, false, false, level.dimension.getType());
        }
        return mapItemSavedData;
    }

    public static int getMapId(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getTag();
        return compoundTag != null && compoundTag.contains("map", 99) ? compoundTag.getInt("map") : 0;
    }

    private static MapItemSavedData createAndStoreSavedData(ItemStack itemStack, Level level, int i, int j, int k, boolean bl, boolean bl2, DimensionType dimensionType) {
        int l = level.getFreeMapId();
        MapItemSavedData mapItemSavedData = new MapItemSavedData(MapItem.makeKey(l));
        mapItemSavedData.setProperties(i, j, k, bl, bl2, dimensionType);
        level.setMapData(mapItemSavedData);
        itemStack.getOrCreateTag().putInt("map", l);
        return mapItemSavedData;
    }

    public static String makeKey(int i) {
        return "map_" + i;
    }

    public void update(Level level, Entity entity, MapItemSavedData mapItemSavedData) {
        if (level.dimension.getType() != mapItemSavedData.dimension || !(entity instanceof Player)) {
            return;
        }
        int i = 1 << mapItemSavedData.scale;
        int j = mapItemSavedData.x;
        int k = mapItemSavedData.z;
        int l = Mth.floor(entity.x - (double)j) / i + 64;
        int m = Mth.floor(entity.z - (double)k) / i + 64;
        int n = 128 / i;
        if (level.dimension.isHasCeiling()) {
            n /= 2;
        }
        MapItemSavedData.HoldingPlayer holdingPlayer = mapItemSavedData.getHoldingPlayer((Player)entity);
        ++holdingPlayer.step;
        boolean bl = false;
        for (int o = l - n + 1; o < l + n; ++o) {
            if ((o & 0xF) != (holdingPlayer.step & 0xF) && !bl) continue;
            bl = false;
            double d = 0.0;
            for (int p = m - n - 1; p < m + n; ++p) {
                byte c;
                byte b;
                MaterialColor materialColor;
                int y;
                if (o < 0 || p < -1 || o >= 128 || p >= 128) continue;
                int q = o - l;
                int r = p - m;
                boolean bl2 = q * q + r * r > (n - 2) * (n - 2);
                int s = (j / i + o - 64) * i;
                int t = (k / i + p - 64) * i;
                LinkedHashMultiset<MaterialColor> multiset = LinkedHashMultiset.create();
                LevelChunk levelChunk = level.getChunkAt(new BlockPos(s, 0, t));
                if (levelChunk.isEmpty()) continue;
                ChunkPos chunkPos = levelChunk.getPos();
                int u = s & 0xF;
                int v = t & 0xF;
                int w = 0;
                double e = 0.0;
                if (level.dimension.isHasCeiling()) {
                    int x = s + t * 231871;
                    if (((x = x * x * 31287121 + x * 11) >> 20 & 1) == 0) {
                        multiset.add(Blocks.DIRT.defaultBlockState().getMapColor(level, BlockPos.ZERO), 10);
                    } else {
                        multiset.add(Blocks.STONE.defaultBlockState().getMapColor(level, BlockPos.ZERO), 100);
                    }
                    e = 100.0;
                } else {
                    BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
                    BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();
                    for (y = 0; y < i; ++y) {
                        for (int z = 0; z < i; ++z) {
                            BlockState blockState;
                            int aa = levelChunk.getHeight(Heightmap.Types.WORLD_SURFACE, y + u, z + v) + 1;
                            if (aa > 1) {
                                do {
                                    mutableBlockPos.set(chunkPos.getMinBlockX() + y + u, --aa, chunkPos.getMinBlockZ() + z + v);
                                } while ((blockState = levelChunk.getBlockState(mutableBlockPos)).getMapColor(level, mutableBlockPos) == MaterialColor.NONE && aa > 0);
                                if (aa > 0 && !blockState.getFluidState().isEmpty()) {
                                    BlockState blockState2;
                                    int ab = aa - 1;
                                    mutableBlockPos2.set(mutableBlockPos);
                                    do {
                                        mutableBlockPos2.setY(ab--);
                                        blockState2 = levelChunk.getBlockState(mutableBlockPos2);
                                        ++w;
                                    } while (ab > 0 && !blockState2.getFluidState().isEmpty());
                                    blockState = this.getCorrectStateForFluidBlock(level, blockState, mutableBlockPos);
                                }
                            } else {
                                blockState = Blocks.BEDROCK.defaultBlockState();
                            }
                            mapItemSavedData.checkBanners(level, chunkPos.getMinBlockX() + y + u, chunkPos.getMinBlockZ() + z + v);
                            e += (double)aa / (double)(i * i);
                            multiset.add(blockState.getMapColor(level, mutableBlockPos));
                        }
                    }
                }
                w /= i * i;
                double f = (e - d) * 4.0 / (double)(i + 4) + ((double)(o + p & 1) - 0.5) * 0.4;
                y = 1;
                if (f > 0.6) {
                    y = 2;
                }
                if (f < -0.6) {
                    y = 0;
                }
                if ((materialColor = Iterables.getFirst(Multisets.copyHighestCountFirst(multiset), MaterialColor.NONE)) == MaterialColor.WATER) {
                    f = (double)w * 0.1 + (double)(o + p & 1) * 0.2;
                    y = 1;
                    if (f < 0.5) {
                        y = 2;
                    }
                    if (f > 0.9) {
                        y = 0;
                    }
                }
                d = e;
                if (p < 0 || q * q + r * r >= n * n || bl2 && (o + p & 1) == 0 || (b = mapItemSavedData.colors[o + p * 128]) == (c = (byte)(materialColor.id * 4 + y))) continue;
                mapItemSavedData.colors[o + p * 128] = c;
                mapItemSavedData.setDirty(o, p);
                bl = true;
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

    private static boolean isLand(Biome[] biomes, int i, int j, int k) {
        return biomes[j * i + k * i * 128 * i].getDepth() >= 0.0f;
    }

    public static void renderBiomePreviewMap(Level level, ItemStack itemStack) {
        MapItemSavedData mapItemSavedData = MapItem.getOrCreateSavedData(itemStack, level);
        if (mapItemSavedData == null) {
            return;
        }
        if (level.dimension.getType() != mapItemSavedData.dimension) {
            return;
        }
        int i = 1 << mapItemSavedData.scale;
        int j = mapItemSavedData.x;
        int k = mapItemSavedData.z;
        Biome[] biomes = level.getChunkSource().getGenerator().getBiomeSource().getBiomeBlock((j / i - 64) * i, (k / i - 64) * i, 128 * i, 128 * i, false);
        for (int l = 0; l < 128; ++l) {
            for (int m = 0; m < 128; ++m) {
                if (l <= 0 || m <= 0 || l >= 127 || m >= 127) continue;
                Biome biome = biomes[l * i + m * i * 128 * i];
                int n = 8;
                if (MapItem.isLand(biomes, i, l - 1, m - 1)) {
                    --n;
                }
                if (MapItem.isLand(biomes, i, l - 1, m + 1)) {
                    --n;
                }
                if (MapItem.isLand(biomes, i, l - 1, m)) {
                    --n;
                }
                if (MapItem.isLand(biomes, i, l + 1, m - 1)) {
                    --n;
                }
                if (MapItem.isLand(biomes, i, l + 1, m + 1)) {
                    --n;
                }
                if (MapItem.isLand(biomes, i, l + 1, m)) {
                    --n;
                }
                if (MapItem.isLand(biomes, i, l, m - 1)) {
                    --n;
                }
                if (MapItem.isLand(biomes, i, l, m + 1)) {
                    --n;
                }
                int o = 3;
                MaterialColor materialColor = MaterialColor.NONE;
                if (biome.getDepth() < 0.0f) {
                    materialColor = MaterialColor.COLOR_ORANGE;
                    if (n > 7 && m % 2 == 0) {
                        o = (l + (int)(Mth.sin((float)m + 0.0f) * 7.0f)) / 8 % 5;
                        if (o == 3) {
                            o = 1;
                        } else if (o == 4) {
                            o = 0;
                        }
                    } else if (n > 7) {
                        materialColor = MaterialColor.NONE;
                    } else if (n > 5) {
                        o = 1;
                    } else if (n > 3) {
                        o = 0;
                    } else if (n > 1) {
                        o = 0;
                    }
                } else if (n > 0) {
                    materialColor = MaterialColor.COLOR_BROWN;
                    o = n > 3 ? 1 : 3;
                }
                if (materialColor == MaterialColor.NONE) continue;
                mapItemSavedData.colors[l + m * 128] = (byte)(materialColor.id * 4 + o);
                mapItemSavedData.setDirty(l, m);
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int i, boolean bl) {
        if (level.isClientSide) {
            return;
        }
        MapItemSavedData mapItemSavedData = MapItem.getOrCreateSavedData(itemStack, level);
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
        return MapItem.getOrCreateSavedData(itemStack, level).getUpdatePacket(itemStack, level, player);
    }

    @Override
    public void onCraftedBy(ItemStack itemStack, Level level, Player player) {
        CompoundTag compoundTag = itemStack.getTag();
        if (compoundTag != null && compoundTag.contains("map_scale_direction", 99)) {
            MapItem.scaleMap(itemStack, level, compoundTag.getInt("map_scale_direction"));
            compoundTag.remove("map_scale_direction");
        }
    }

    protected static void scaleMap(ItemStack itemStack, Level level, int i) {
        MapItemSavedData mapItemSavedData = MapItem.getOrCreateSavedData(itemStack, level);
        if (mapItemSavedData != null) {
            MapItem.createAndStoreSavedData(itemStack, level, mapItemSavedData.x, mapItemSavedData.z, Mth.clamp(mapItemSavedData.scale + i, 0, 4), mapItemSavedData.trackingPosition, mapItemSavedData.unlimitedTracking, mapItemSavedData.dimension);
        }
    }

    @Nullable
    public static ItemStack lockMap(Level level, ItemStack itemStack) {
        MapItemSavedData mapItemSavedData = MapItem.getOrCreateSavedData(itemStack, level);
        if (mapItemSavedData != null) {
            ItemStack itemStack2 = itemStack.copy();
            MapItemSavedData mapItemSavedData2 = MapItem.createAndStoreSavedData(itemStack2, level, 0, 0, mapItemSavedData.scale, mapItemSavedData.trackingPosition, mapItemSavedData.unlimitedTracking, mapItemSavedData.dimension);
            mapItemSavedData2.lockData(mapItemSavedData);
            return itemStack2;
        }
        return null;
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        MapItemSavedData mapItemSavedData;
        MapItemSavedData mapItemSavedData2 = mapItemSavedData = level == null ? null : MapItem.getOrCreateSavedData(itemStack, level);
        if (mapItemSavedData != null && mapItemSavedData.locked) {
            list.add(new TranslatableComponent("filled_map.locked", MapItem.getMapId(itemStack)).withStyle(ChatFormatting.GRAY));
        }
        if (tooltipFlag.isAdvanced()) {
            if (mapItemSavedData != null) {
                list.add(new TranslatableComponent("filled_map.id", MapItem.getMapId(itemStack)).withStyle(ChatFormatting.GRAY));
                list.add(new TranslatableComponent("filled_map.scale", 1 << mapItemSavedData.scale).withStyle(ChatFormatting.GRAY));
                list.add(new TranslatableComponent("filled_map.level", mapItemSavedData.scale, 4).withStyle(ChatFormatting.GRAY));
            } else {
                list.add(new TranslatableComponent("filled_map.unknown", new Object[0]).withStyle(ChatFormatting.GRAY));
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
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
            if (!useOnContext.level.isClientSide) {
                MapItemSavedData mapItemSavedData = MapItem.getOrCreateSavedData(useOnContext.getItemInHand(), useOnContext.getLevel());
                mapItemSavedData.toggleBanner(useOnContext.getLevel(), useOnContext.getClickedPos());
            }
            return InteractionResult.SUCCESS;
        }
        return super.useOn(useOnContext);
    }
}

