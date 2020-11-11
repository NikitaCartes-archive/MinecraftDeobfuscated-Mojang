/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.models.model;

import java.util.Optional;
import java.util.stream.IntStream;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;

public class ModelTemplates {
    public static final ModelTemplate CUBE = ModelTemplates.create("cube", TextureSlot.PARTICLE, TextureSlot.NORTH, TextureSlot.SOUTH, TextureSlot.EAST, TextureSlot.WEST, TextureSlot.UP, TextureSlot.DOWN);
    public static final ModelTemplate CUBE_DIRECTIONAL = ModelTemplates.create("cube_directional", TextureSlot.PARTICLE, TextureSlot.NORTH, TextureSlot.SOUTH, TextureSlot.EAST, TextureSlot.WEST, TextureSlot.UP, TextureSlot.DOWN);
    public static final ModelTemplate CUBE_ALL = ModelTemplates.create("cube_all", TextureSlot.ALL);
    public static final ModelTemplate CUBE_MIRRORED_ALL = ModelTemplates.create("cube_mirrored_all", "_mirrored", TextureSlot.ALL);
    public static final ModelTemplate CUBE_COLUMN = ModelTemplates.create("cube_column", TextureSlot.END, TextureSlot.SIDE);
    public static final ModelTemplate CUBE_COLUMN_HORIZONTAL = ModelTemplates.create("cube_column_horizontal", "_horizontal", TextureSlot.END, TextureSlot.SIDE);
    public static final ModelTemplate CUBE_TOP = ModelTemplates.create("cube_top", TextureSlot.TOP, TextureSlot.SIDE);
    public static final ModelTemplate CUBE_BOTTOM_TOP = ModelTemplates.create("cube_bottom_top", TextureSlot.TOP, TextureSlot.BOTTOM, TextureSlot.SIDE);
    public static final ModelTemplate CUBE_ORIENTABLE = ModelTemplates.create("orientable", TextureSlot.TOP, TextureSlot.FRONT, TextureSlot.SIDE);
    public static final ModelTemplate CUBE_ORIENTABLE_TOP_BOTTOM = ModelTemplates.create("orientable_with_bottom", TextureSlot.TOP, TextureSlot.BOTTOM, TextureSlot.SIDE, TextureSlot.FRONT);
    public static final ModelTemplate CUBE_ORIENTABLE_VERTICAL = ModelTemplates.create("orientable_vertical", "_vertical", TextureSlot.FRONT, TextureSlot.SIDE);
    public static final ModelTemplate BUTTON = ModelTemplates.create("button", TextureSlot.TEXTURE);
    public static final ModelTemplate BUTTON_PRESSED = ModelTemplates.create("button_pressed", "_pressed", TextureSlot.TEXTURE);
    public static final ModelTemplate BUTTON_INVENTORY = ModelTemplates.create("button_inventory", "_inventory", TextureSlot.TEXTURE);
    public static final ModelTemplate DOOR_BOTTOM = ModelTemplates.create("door_bottom", "_bottom", TextureSlot.TOP, TextureSlot.BOTTOM);
    public static final ModelTemplate DOOR_BOTTOM_HINGE = ModelTemplates.create("door_bottom_rh", "_bottom_hinge", TextureSlot.TOP, TextureSlot.BOTTOM);
    public static final ModelTemplate DOOR_TOP = ModelTemplates.create("door_top", "_top", TextureSlot.TOP, TextureSlot.BOTTOM);
    public static final ModelTemplate DOOR_TOP_HINGE = ModelTemplates.create("door_top_rh", "_top_hinge", TextureSlot.TOP, TextureSlot.BOTTOM);
    public static final ModelTemplate FENCE_POST = ModelTemplates.create("fence_post", "_post", TextureSlot.TEXTURE);
    public static final ModelTemplate FENCE_SIDE = ModelTemplates.create("fence_side", "_side", TextureSlot.TEXTURE);
    public static final ModelTemplate FENCE_INVENTORY = ModelTemplates.create("fence_inventory", "_inventory", TextureSlot.TEXTURE);
    public static final ModelTemplate WALL_POST = ModelTemplates.create("template_wall_post", "_post", TextureSlot.WALL);
    public static final ModelTemplate WALL_LOW_SIDE = ModelTemplates.create("template_wall_side", "_side", TextureSlot.WALL);
    public static final ModelTemplate WALL_TALL_SIDE = ModelTemplates.create("template_wall_side_tall", "_side_tall", TextureSlot.WALL);
    public static final ModelTemplate WALL_INVENTORY = ModelTemplates.create("wall_inventory", "_inventory", TextureSlot.WALL);
    public static final ModelTemplate FENCE_GATE_CLOSED = ModelTemplates.create("template_fence_gate", TextureSlot.TEXTURE);
    public static final ModelTemplate FENCE_GATE_OPEN = ModelTemplates.create("template_fence_gate_open", "_open", TextureSlot.TEXTURE);
    public static final ModelTemplate FENCE_GATE_WALL_CLOSED = ModelTemplates.create("template_fence_gate_wall", "_wall", TextureSlot.TEXTURE);
    public static final ModelTemplate FENCE_GATE_WALL_OPEN = ModelTemplates.create("template_fence_gate_wall_open", "_wall_open", TextureSlot.TEXTURE);
    public static final ModelTemplate PRESSURE_PLATE_UP = ModelTemplates.create("pressure_plate_up", TextureSlot.TEXTURE);
    public static final ModelTemplate PRESSURE_PLATE_DOWN = ModelTemplates.create("pressure_plate_down", "_down", TextureSlot.TEXTURE);
    public static final ModelTemplate PARTICLE_ONLY = ModelTemplates.create(TextureSlot.PARTICLE);
    public static final ModelTemplate SLAB_BOTTOM = ModelTemplates.create("slab", TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE);
    public static final ModelTemplate SLAB_TOP = ModelTemplates.create("slab_top", "_top", TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE);
    public static final ModelTemplate LEAVES = ModelTemplates.create("leaves", TextureSlot.ALL);
    public static final ModelTemplate STAIRS_STRAIGHT = ModelTemplates.create("stairs", TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE);
    public static final ModelTemplate STAIRS_INNER = ModelTemplates.create("inner_stairs", "_inner", TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE);
    public static final ModelTemplate STAIRS_OUTER = ModelTemplates.create("outer_stairs", "_outer", TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE);
    public static final ModelTemplate TRAPDOOR_TOP = ModelTemplates.create("template_trapdoor_top", "_top", TextureSlot.TEXTURE);
    public static final ModelTemplate TRAPDOOR_BOTTOM = ModelTemplates.create("template_trapdoor_bottom", "_bottom", TextureSlot.TEXTURE);
    public static final ModelTemplate TRAPDOOR_OPEN = ModelTemplates.create("template_trapdoor_open", "_open", TextureSlot.TEXTURE);
    public static final ModelTemplate ORIENTABLE_TRAPDOOR_TOP = ModelTemplates.create("template_orientable_trapdoor_top", "_top", TextureSlot.TEXTURE);
    public static final ModelTemplate ORIENTABLE_TRAPDOOR_BOTTOM = ModelTemplates.create("template_orientable_trapdoor_bottom", "_bottom", TextureSlot.TEXTURE);
    public static final ModelTemplate ORIENTABLE_TRAPDOOR_OPEN = ModelTemplates.create("template_orientable_trapdoor_open", "_open", TextureSlot.TEXTURE);
    public static final ModelTemplate CROSS = ModelTemplates.create("cross", TextureSlot.CROSS);
    public static final ModelTemplate TINTED_CROSS = ModelTemplates.create("tinted_cross", TextureSlot.CROSS);
    public static final ModelTemplate FLOWER_POT_CROSS = ModelTemplates.create("flower_pot_cross", TextureSlot.PLANT);
    public static final ModelTemplate TINTED_FLOWER_POT_CROSS = ModelTemplates.create("tinted_flower_pot_cross", TextureSlot.PLANT);
    public static final ModelTemplate RAIL_FLAT = ModelTemplates.create("rail_flat", TextureSlot.RAIL);
    public static final ModelTemplate RAIL_CURVED = ModelTemplates.create("rail_curved", "_corner", TextureSlot.RAIL);
    public static final ModelTemplate RAIL_RAISED_NE = ModelTemplates.create("template_rail_raised_ne", "_raised_ne", TextureSlot.RAIL);
    public static final ModelTemplate RAIL_RAISED_SW = ModelTemplates.create("template_rail_raised_sw", "_raised_sw", TextureSlot.RAIL);
    public static final ModelTemplate CARPET = ModelTemplates.create("carpet", TextureSlot.WOOL);
    public static final ModelTemplate CORAL_FAN = ModelTemplates.create("coral_fan", TextureSlot.FAN);
    public static final ModelTemplate CORAL_WALL_FAN = ModelTemplates.create("coral_wall_fan", TextureSlot.FAN);
    public static final ModelTemplate GLAZED_TERRACOTTA = ModelTemplates.create("template_glazed_terracotta", TextureSlot.PATTERN);
    public static final ModelTemplate CHORUS_FLOWER = ModelTemplates.create("template_chorus_flower", TextureSlot.TEXTURE);
    public static final ModelTemplate DAYLIGHT_DETECTOR = ModelTemplates.create("template_daylight_detector", TextureSlot.TOP, TextureSlot.SIDE);
    public static final ModelTemplate STAINED_GLASS_PANE_NOSIDE = ModelTemplates.create("template_glass_pane_noside", "_noside", TextureSlot.PANE);
    public static final ModelTemplate STAINED_GLASS_PANE_NOSIDE_ALT = ModelTemplates.create("template_glass_pane_noside_alt", "_noside_alt", TextureSlot.PANE);
    public static final ModelTemplate STAINED_GLASS_PANE_POST = ModelTemplates.create("template_glass_pane_post", "_post", TextureSlot.PANE, TextureSlot.EDGE);
    public static final ModelTemplate STAINED_GLASS_PANE_SIDE = ModelTemplates.create("template_glass_pane_side", "_side", TextureSlot.PANE, TextureSlot.EDGE);
    public static final ModelTemplate STAINED_GLASS_PANE_SIDE_ALT = ModelTemplates.create("template_glass_pane_side_alt", "_side_alt", TextureSlot.PANE, TextureSlot.EDGE);
    public static final ModelTemplate COMMAND_BLOCK = ModelTemplates.create("template_command_block", TextureSlot.FRONT, TextureSlot.BACK, TextureSlot.SIDE);
    public static final ModelTemplate ANVIL = ModelTemplates.create("template_anvil", TextureSlot.TOP);
    public static final ModelTemplate[] STEMS = (ModelTemplate[])IntStream.range(0, 8).mapToObj(i -> ModelTemplates.create("stem_growth" + i, "_stage" + i, TextureSlot.STEM)).toArray(ModelTemplate[]::new);
    public static final ModelTemplate ATTACHED_STEM = ModelTemplates.create("stem_fruit", TextureSlot.STEM, TextureSlot.UPPER_STEM);
    public static final ModelTemplate CROP = ModelTemplates.create("crop", TextureSlot.CROP);
    public static final ModelTemplate FARMLAND = ModelTemplates.create("template_farmland", TextureSlot.DIRT, TextureSlot.TOP);
    public static final ModelTemplate FIRE_FLOOR = ModelTemplates.create("template_fire_floor", TextureSlot.FIRE);
    public static final ModelTemplate FIRE_SIDE = ModelTemplates.create("template_fire_side", TextureSlot.FIRE);
    public static final ModelTemplate FIRE_SIDE_ALT = ModelTemplates.create("template_fire_side_alt", TextureSlot.FIRE);
    public static final ModelTemplate FIRE_UP = ModelTemplates.create("template_fire_up", TextureSlot.FIRE);
    public static final ModelTemplate FIRE_UP_ALT = ModelTemplates.create("template_fire_up_alt", TextureSlot.FIRE);
    public static final ModelTemplate CAMPFIRE = ModelTemplates.create("template_campfire", TextureSlot.FIRE, TextureSlot.LIT_LOG);
    public static final ModelTemplate LANTERN = ModelTemplates.create("template_lantern", TextureSlot.LANTERN);
    public static final ModelTemplate HANGING_LANTERN = ModelTemplates.create("template_hanging_lantern", "_hanging", TextureSlot.LANTERN);
    public static final ModelTemplate TORCH = ModelTemplates.create("template_torch", TextureSlot.TORCH);
    public static final ModelTemplate WALL_TORCH = ModelTemplates.create("template_torch_wall", TextureSlot.TORCH);
    public static final ModelTemplate PISTON = ModelTemplates.create("template_piston", TextureSlot.PLATFORM, TextureSlot.BOTTOM, TextureSlot.SIDE);
    public static final ModelTemplate PISTON_HEAD = ModelTemplates.create("template_piston_head", TextureSlot.PLATFORM, TextureSlot.SIDE, TextureSlot.UNSTICKY);
    public static final ModelTemplate PISTON_HEAD_SHORT = ModelTemplates.create("template_piston_head_short", TextureSlot.PLATFORM, TextureSlot.SIDE, TextureSlot.UNSTICKY);
    public static final ModelTemplate SEAGRASS = ModelTemplates.create("template_seagrass", TextureSlot.TEXTURE);
    public static final ModelTemplate TURTLE_EGG = ModelTemplates.create("template_turtle_egg", TextureSlot.ALL);
    public static final ModelTemplate TWO_TURTLE_EGGS = ModelTemplates.create("template_two_turtle_eggs", TextureSlot.ALL);
    public static final ModelTemplate THREE_TURTLE_EGGS = ModelTemplates.create("template_three_turtle_eggs", TextureSlot.ALL);
    public static final ModelTemplate FOUR_TURTLE_EGGS = ModelTemplates.create("template_four_turtle_eggs", TextureSlot.ALL);
    public static final ModelTemplate SINGLE_FACE = ModelTemplates.create("template_single_face", TextureSlot.TEXTURE);
    public static final ModelTemplate CAULDRON_LEVEL1 = ModelTemplates.create("template_cauldron_level1", TextureSlot.CONTENT, TextureSlot.INSIDE, TextureSlot.PARTICLE, TextureSlot.TOP, TextureSlot.BOTTOM, TextureSlot.SIDE);
    public static final ModelTemplate CAULDRON_LEVEL2 = ModelTemplates.create("template_cauldron_level2", TextureSlot.CONTENT, TextureSlot.INSIDE, TextureSlot.PARTICLE, TextureSlot.TOP, TextureSlot.BOTTOM, TextureSlot.SIDE);
    public static final ModelTemplate CAULDRON_FULL = ModelTemplates.create("template_cauldron_full", TextureSlot.CONTENT, TextureSlot.INSIDE, TextureSlot.PARTICLE, TextureSlot.TOP, TextureSlot.BOTTOM, TextureSlot.SIDE);
    public static final ModelTemplate FLAT_ITEM = ModelTemplates.createItem("generated", TextureSlot.LAYER0);
    public static final ModelTemplate FLAT_HANDHELD_ITEM = ModelTemplates.createItem("handheld", TextureSlot.LAYER0);
    public static final ModelTemplate FLAT_HANDHELD_ROD_ITEM = ModelTemplates.createItem("handheld_rod", TextureSlot.LAYER0);
    public static final ModelTemplate SHULKER_BOX_INVENTORY = ModelTemplates.createItem("template_shulker_box", TextureSlot.PARTICLE);
    public static final ModelTemplate BED_INVENTORY = ModelTemplates.createItem("template_bed", TextureSlot.PARTICLE);
    public static final ModelTemplate BANNER_INVENTORY = ModelTemplates.createItem("template_banner", new TextureSlot[0]);
    public static final ModelTemplate SKULL_INVENTORY = ModelTemplates.createItem("template_skull", new TextureSlot[0]);
    public static final ModelTemplate CANDLE = ModelTemplates.create("template_candle", TextureSlot.ALL, TextureSlot.PARTICLE);
    public static final ModelTemplate TWO_CANDLES = ModelTemplates.create("template_two_candles", TextureSlot.ALL, TextureSlot.PARTICLE);
    public static final ModelTemplate THREE_CANDLES = ModelTemplates.create("template_three_candles", TextureSlot.ALL, TextureSlot.PARTICLE);
    public static final ModelTemplate FOUR_CANDLES = ModelTemplates.create("template_four_candles", TextureSlot.ALL, TextureSlot.PARTICLE);
    public static final ModelTemplate CANDLE_CAKE = ModelTemplates.create("template_cake_with_candle", TextureSlot.CANDLE, TextureSlot.BOTTOM, TextureSlot.SIDE, TextureSlot.TOP, TextureSlot.PARTICLE);

    private static ModelTemplate create(TextureSlot ... textureSlots) {
        return new ModelTemplate(Optional.empty(), Optional.empty(), textureSlots);
    }

    private static ModelTemplate create(String string, TextureSlot ... textureSlots) {
        return new ModelTemplate(Optional.of(new ResourceLocation("minecraft", "block/" + string)), Optional.empty(), textureSlots);
    }

    private static ModelTemplate createItem(String string, TextureSlot ... textureSlots) {
        return new ModelTemplate(Optional.of(new ResourceLocation("minecraft", "item/" + string)), Optional.empty(), textureSlots);
    }

    private static ModelTemplate create(String string, String string2, TextureSlot ... textureSlots) {
        return new ModelTemplate(Optional.of(new ResourceLocation("minecraft", "block/" + string)), Optional.of(string2), textureSlots);
    }
}

