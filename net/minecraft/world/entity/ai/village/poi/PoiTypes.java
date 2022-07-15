/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.village.poi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

public class PoiTypes {
    public static final ResourceKey<PoiType> ARMORER = PoiTypes.createKey("armorer");
    public static final ResourceKey<PoiType> BUTCHER = PoiTypes.createKey("butcher");
    public static final ResourceKey<PoiType> CARTOGRAPHER = PoiTypes.createKey("cartographer");
    public static final ResourceKey<PoiType> CLERIC = PoiTypes.createKey("cleric");
    public static final ResourceKey<PoiType> FARMER = PoiTypes.createKey("farmer");
    public static final ResourceKey<PoiType> FISHERMAN = PoiTypes.createKey("fisherman");
    public static final ResourceKey<PoiType> FLETCHER = PoiTypes.createKey("fletcher");
    public static final ResourceKey<PoiType> LEATHERWORKER = PoiTypes.createKey("leatherworker");
    public static final ResourceKey<PoiType> LIBRARIAN = PoiTypes.createKey("librarian");
    public static final ResourceKey<PoiType> MASON = PoiTypes.createKey("mason");
    public static final ResourceKey<PoiType> SHEPHERD = PoiTypes.createKey("shepherd");
    public static final ResourceKey<PoiType> TOOLSMITH = PoiTypes.createKey("toolsmith");
    public static final ResourceKey<PoiType> WEAPONSMITH = PoiTypes.createKey("weaponsmith");
    public static final ResourceKey<PoiType> HOME = PoiTypes.createKey("home");
    public static final ResourceKey<PoiType> MEETING = PoiTypes.createKey("meeting");
    public static final ResourceKey<PoiType> BEEHIVE = PoiTypes.createKey("beehive");
    public static final ResourceKey<PoiType> BEE_NEST = PoiTypes.createKey("bee_nest");
    public static final ResourceKey<PoiType> NETHER_PORTAL = PoiTypes.createKey("nether_portal");
    public static final ResourceKey<PoiType> LODESTONE = PoiTypes.createKey("lodestone");
    public static final ResourceKey<PoiType> LIGHTNING_ROD = PoiTypes.createKey("lightning_rod");
    private static final Set<BlockState> BEDS = ImmutableList.of(Blocks.RED_BED, Blocks.BLACK_BED, Blocks.BLUE_BED, Blocks.BROWN_BED, Blocks.CYAN_BED, Blocks.GRAY_BED, Blocks.GREEN_BED, Blocks.LIGHT_BLUE_BED, Blocks.LIGHT_GRAY_BED, Blocks.LIME_BED, Blocks.MAGENTA_BED, Blocks.ORANGE_BED, new Block[]{Blocks.PINK_BED, Blocks.PURPLE_BED, Blocks.WHITE_BED, Blocks.YELLOW_BED}).stream().flatMap(block -> block.getStateDefinition().getPossibleStates().stream()).filter(blockState -> blockState.getValue(BedBlock.PART) == BedPart.HEAD).collect(ImmutableSet.toImmutableSet());
    private static final Set<BlockState> CAULDRONS = ImmutableList.of(Blocks.CAULDRON, Blocks.LAVA_CAULDRON, Blocks.WATER_CAULDRON, Blocks.POWDER_SNOW_CAULDRON).stream().flatMap(block -> block.getStateDefinition().getPossibleStates().stream()).collect(ImmutableSet.toImmutableSet());
    private static final Map<BlockState, Holder<PoiType>> TYPE_BY_STATE = Maps.newHashMap();
    protected static final Set<BlockState> ALL_STATES = new ObjectOpenHashSet<BlockState>(TYPE_BY_STATE.keySet());

    private static Set<BlockState> getBlockStates(Block block) {
        return ImmutableSet.copyOf(block.getStateDefinition().getPossibleStates());
    }

    private static ResourceKey<PoiType> createKey(String string) {
        return ResourceKey.create(Registry.POINT_OF_INTEREST_TYPE_REGISTRY, new ResourceLocation(string));
    }

    private static PoiType register(Registry<PoiType> registry, ResourceKey<PoiType> resourceKey, Set<BlockState> set, int i, int j) {
        PoiType poiType = new PoiType(set, i, j);
        Registry.register(registry, resourceKey, poiType);
        PoiTypes.registerBlockStates(registry.getHolderOrThrow(resourceKey));
        return poiType;
    }

    private static void registerBlockStates(Holder<PoiType> holder) {
        holder.value().matchingStates().forEach(blockState -> {
            Holder<PoiType> holder2 = TYPE_BY_STATE.put((BlockState)blockState, holder);
            if (holder2 != null) {
                throw Util.pauseInIde(new IllegalStateException(String.format(Locale.ROOT, "%s is defined in more than one PoI type", blockState)));
            }
        });
    }

    public static Optional<Holder<PoiType>> forState(BlockState blockState) {
        return Optional.ofNullable(TYPE_BY_STATE.get(blockState));
    }

    public static PoiType bootstrap(Registry<PoiType> registry) {
        PoiTypes.register(registry, ARMORER, PoiTypes.getBlockStates(Blocks.BLAST_FURNACE), 1, 1);
        PoiTypes.register(registry, BUTCHER, PoiTypes.getBlockStates(Blocks.SMOKER), 1, 1);
        PoiTypes.register(registry, CARTOGRAPHER, PoiTypes.getBlockStates(Blocks.CARTOGRAPHY_TABLE), 1, 1);
        PoiTypes.register(registry, CLERIC, PoiTypes.getBlockStates(Blocks.BREWING_STAND), 1, 1);
        PoiTypes.register(registry, FARMER, PoiTypes.getBlockStates(Blocks.COMPOSTER), 1, 1);
        PoiTypes.register(registry, FISHERMAN, PoiTypes.getBlockStates(Blocks.BARREL), 1, 1);
        PoiTypes.register(registry, FLETCHER, PoiTypes.getBlockStates(Blocks.FLETCHING_TABLE), 1, 1);
        PoiTypes.register(registry, LEATHERWORKER, CAULDRONS, 1, 1);
        PoiTypes.register(registry, LIBRARIAN, PoiTypes.getBlockStates(Blocks.LECTERN), 1, 1);
        PoiTypes.register(registry, MASON, PoiTypes.getBlockStates(Blocks.STONECUTTER), 1, 1);
        PoiTypes.register(registry, SHEPHERD, PoiTypes.getBlockStates(Blocks.LOOM), 1, 1);
        PoiTypes.register(registry, TOOLSMITH, PoiTypes.getBlockStates(Blocks.SMITHING_TABLE), 1, 1);
        PoiTypes.register(registry, WEAPONSMITH, PoiTypes.getBlockStates(Blocks.GRINDSTONE), 1, 1);
        PoiTypes.register(registry, HOME, BEDS, 1, 1);
        PoiTypes.register(registry, MEETING, PoiTypes.getBlockStates(Blocks.BELL), 32, 6);
        PoiTypes.register(registry, BEEHIVE, PoiTypes.getBlockStates(Blocks.BEEHIVE), 0, 1);
        PoiTypes.register(registry, BEE_NEST, PoiTypes.getBlockStates(Blocks.BEE_NEST), 0, 1);
        PoiTypes.register(registry, NETHER_PORTAL, PoiTypes.getBlockStates(Blocks.NETHER_PORTAL), 0, 1);
        PoiTypes.register(registry, LODESTONE, PoiTypes.getBlockStates(Blocks.LODESTONE), 0, 1);
        return PoiTypes.register(registry, LIGHTNING_ROD, PoiTypes.getBlockStates(Blocks.LIGHTNING_ROD), 0, 1);
    }
}

