/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.state.properties;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;

public record BlockSetType(String name, SoundType soundType, SoundEvent doorClose, SoundEvent doorOpen, SoundEvent trapdoorClose, SoundEvent trapdoorOpen, SoundEvent pressurePlateClickOff, SoundEvent pressurePlateClickOn, SoundEvent buttonClickOff, SoundEvent buttonClickOn) {
    private static final Set<BlockSetType> VALUES = new ObjectArraySet<BlockSetType>();
    public static final BlockSetType IRON = BlockSetType.register(new BlockSetType("iron", SoundType.METAL, SoundEvents.IRON_DOOR_CLOSE, SoundEvents.IRON_DOOR_OPEN, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundEvents.IRON_TRAPDOOR_OPEN, SoundEvents.METAL_PRESSURE_PLATE_CLICK_OFF, SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON, SoundEvents.STONE_BUTTON_CLICK_OFF, SoundEvents.STONE_BUTTON_CLICK_ON));
    public static final BlockSetType GOLD = BlockSetType.register(new BlockSetType("gold", SoundType.METAL, SoundEvents.IRON_DOOR_CLOSE, SoundEvents.IRON_DOOR_OPEN, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundEvents.IRON_TRAPDOOR_OPEN, SoundEvents.METAL_PRESSURE_PLATE_CLICK_OFF, SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON, SoundEvents.STONE_BUTTON_CLICK_OFF, SoundEvents.STONE_BUTTON_CLICK_ON));
    public static final BlockSetType STONE = BlockSetType.register(new BlockSetType("stone", SoundType.STONE, SoundEvents.IRON_DOOR_CLOSE, SoundEvents.IRON_DOOR_OPEN, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundEvents.IRON_TRAPDOOR_OPEN, SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF, SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON, SoundEvents.STONE_BUTTON_CLICK_OFF, SoundEvents.STONE_BUTTON_CLICK_ON));
    public static final BlockSetType POLISHED_BLACKSTONE = BlockSetType.register(new BlockSetType("polished_blackstone", SoundType.STONE, SoundEvents.IRON_DOOR_CLOSE, SoundEvents.IRON_DOOR_OPEN, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundEvents.IRON_TRAPDOOR_OPEN, SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF, SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON, SoundEvents.STONE_BUTTON_CLICK_OFF, SoundEvents.STONE_BUTTON_CLICK_ON));
    public static final BlockSetType OAK = BlockSetType.register(new BlockSetType("oak"));
    public static final BlockSetType SPRUCE = BlockSetType.register(new BlockSetType("spruce"));
    public static final BlockSetType BIRCH = BlockSetType.register(new BlockSetType("birch"));
    public static final BlockSetType ACACIA = BlockSetType.register(new BlockSetType("acacia"));
    public static final BlockSetType CHERRY = BlockSetType.register(new BlockSetType("cherry", SoundType.CHERRY_WOOD, SoundEvents.CHERRY_WOOD_DOOR_CLOSE, SoundEvents.CHERRY_WOOD_DOOR_OPEN, SoundEvents.CHERRY_WOOD_TRAPDOOR_CLOSE, SoundEvents.CHERRY_WOOD_TRAPDOOR_OPEN, SoundEvents.CHERRY_WOOD_PRESSURE_PLATE_CLICK_OFF, SoundEvents.CHERRY_WOOD_PRESSURE_PLATE_CLICK_ON, SoundEvents.CHERRY_WOOD_BUTTON_CLICK_OFF, SoundEvents.CHERRY_WOOD_BUTTON_CLICK_ON));
    public static final BlockSetType JUNGLE = BlockSetType.register(new BlockSetType("jungle"));
    public static final BlockSetType DARK_OAK = BlockSetType.register(new BlockSetType("dark_oak"));
    public static final BlockSetType CRIMSON = BlockSetType.register(new BlockSetType("crimson", SoundType.NETHER_WOOD, SoundEvents.NETHER_WOOD_DOOR_CLOSE, SoundEvents.NETHER_WOOD_DOOR_OPEN, SoundEvents.NETHER_WOOD_TRAPDOOR_CLOSE, SoundEvents.NETHER_WOOD_TRAPDOOR_OPEN, SoundEvents.NETHER_WOOD_PRESSURE_PLATE_CLICK_OFF, SoundEvents.NETHER_WOOD_PRESSURE_PLATE_CLICK_ON, SoundEvents.NETHER_WOOD_BUTTON_CLICK_OFF, SoundEvents.NETHER_WOOD_BUTTON_CLICK_ON));
    public static final BlockSetType WARPED = BlockSetType.register(new BlockSetType("warped", SoundType.NETHER_WOOD, SoundEvents.NETHER_WOOD_DOOR_CLOSE, SoundEvents.NETHER_WOOD_DOOR_OPEN, SoundEvents.NETHER_WOOD_TRAPDOOR_CLOSE, SoundEvents.NETHER_WOOD_TRAPDOOR_OPEN, SoundEvents.NETHER_WOOD_PRESSURE_PLATE_CLICK_OFF, SoundEvents.NETHER_WOOD_PRESSURE_PLATE_CLICK_ON, SoundEvents.NETHER_WOOD_BUTTON_CLICK_OFF, SoundEvents.NETHER_WOOD_BUTTON_CLICK_ON));
    public static final BlockSetType MANGROVE = BlockSetType.register(new BlockSetType("mangrove"));
    public static final BlockSetType BAMBOO = BlockSetType.register(new BlockSetType("bamboo", SoundType.BAMBOO_WOOD, SoundEvents.BAMBOO_WOOD_DOOR_CLOSE, SoundEvents.BAMBOO_WOOD_DOOR_OPEN, SoundEvents.BAMBOO_WOOD_TRAPDOOR_CLOSE, SoundEvents.BAMBOO_WOOD_TRAPDOOR_OPEN, SoundEvents.BAMBOO_WOOD_PRESSURE_PLATE_CLICK_OFF, SoundEvents.BAMBOO_WOOD_PRESSURE_PLATE_CLICK_ON, SoundEvents.BAMBOO_WOOD_BUTTON_CLICK_OFF, SoundEvents.BAMBOO_WOOD_BUTTON_CLICK_ON));

    public BlockSetType(String string) {
        this(string, SoundType.WOOD, SoundEvents.WOODEN_DOOR_CLOSE, SoundEvents.WOODEN_DOOR_OPEN, SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_OFF, SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON, SoundEvents.WOODEN_BUTTON_CLICK_OFF, SoundEvents.WOODEN_BUTTON_CLICK_ON);
    }

    private static BlockSetType register(BlockSetType blockSetType) {
        VALUES.add(blockSetType);
        return blockSetType;
    }

    public static Stream<BlockSetType> values() {
        return VALUES.stream();
    }
}

