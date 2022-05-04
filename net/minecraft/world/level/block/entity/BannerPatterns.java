/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.entity;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BannerPattern;

public class BannerPatterns {
    public static final ResourceKey<BannerPattern> BASE = BannerPatterns.create("base");
    public static final ResourceKey<BannerPattern> SQUARE_BOTTOM_LEFT = BannerPatterns.create("square_bottom_left");
    public static final ResourceKey<BannerPattern> SQUARE_BOTTOM_RIGHT = BannerPatterns.create("square_bottom_right");
    public static final ResourceKey<BannerPattern> SQUARE_TOP_LEFT = BannerPatterns.create("square_top_left");
    public static final ResourceKey<BannerPattern> SQUARE_TOP_RIGHT = BannerPatterns.create("square_top_right");
    public static final ResourceKey<BannerPattern> STRIPE_BOTTOM = BannerPatterns.create("stripe_bottom");
    public static final ResourceKey<BannerPattern> STRIPE_TOP = BannerPatterns.create("stripe_top");
    public static final ResourceKey<BannerPattern> STRIPE_LEFT = BannerPatterns.create("stripe_left");
    public static final ResourceKey<BannerPattern> STRIPE_RIGHT = BannerPatterns.create("stripe_right");
    public static final ResourceKey<BannerPattern> STRIPE_CENTER = BannerPatterns.create("stripe_center");
    public static final ResourceKey<BannerPattern> STRIPE_MIDDLE = BannerPatterns.create("stripe_middle");
    public static final ResourceKey<BannerPattern> STRIPE_DOWNRIGHT = BannerPatterns.create("stripe_downright");
    public static final ResourceKey<BannerPattern> STRIPE_DOWNLEFT = BannerPatterns.create("stripe_downleft");
    public static final ResourceKey<BannerPattern> STRIPE_SMALL = BannerPatterns.create("small_stripes");
    public static final ResourceKey<BannerPattern> CROSS = BannerPatterns.create("cross");
    public static final ResourceKey<BannerPattern> STRAIGHT_CROSS = BannerPatterns.create("straight_cross");
    public static final ResourceKey<BannerPattern> TRIANGLE_BOTTOM = BannerPatterns.create("triangle_bottom");
    public static final ResourceKey<BannerPattern> TRIANGLE_TOP = BannerPatterns.create("triangle_top");
    public static final ResourceKey<BannerPattern> TRIANGLES_BOTTOM = BannerPatterns.create("triangles_bottom");
    public static final ResourceKey<BannerPattern> TRIANGLES_TOP = BannerPatterns.create("triangles_top");
    public static final ResourceKey<BannerPattern> DIAGONAL_LEFT = BannerPatterns.create("diagonal_left");
    public static final ResourceKey<BannerPattern> DIAGONAL_RIGHT = BannerPatterns.create("diagonal_up_right");
    public static final ResourceKey<BannerPattern> DIAGONAL_LEFT_MIRROR = BannerPatterns.create("diagonal_up_left");
    public static final ResourceKey<BannerPattern> DIAGONAL_RIGHT_MIRROR = BannerPatterns.create("diagonal_right");
    public static final ResourceKey<BannerPattern> CIRCLE_MIDDLE = BannerPatterns.create("circle");
    public static final ResourceKey<BannerPattern> RHOMBUS_MIDDLE = BannerPatterns.create("rhombus");
    public static final ResourceKey<BannerPattern> HALF_VERTICAL = BannerPatterns.create("half_vertical");
    public static final ResourceKey<BannerPattern> HALF_HORIZONTAL = BannerPatterns.create("half_horizontal");
    public static final ResourceKey<BannerPattern> HALF_VERTICAL_MIRROR = BannerPatterns.create("half_vertical_right");
    public static final ResourceKey<BannerPattern> HALF_HORIZONTAL_MIRROR = BannerPatterns.create("half_horizontal_bottom");
    public static final ResourceKey<BannerPattern> BORDER = BannerPatterns.create("border");
    public static final ResourceKey<BannerPattern> CURLY_BORDER = BannerPatterns.create("curly_border");
    public static final ResourceKey<BannerPattern> GRADIENT = BannerPatterns.create("gradient");
    public static final ResourceKey<BannerPattern> GRADIENT_UP = BannerPatterns.create("gradient_up");
    public static final ResourceKey<BannerPattern> BRICKS = BannerPatterns.create("bricks");
    public static final ResourceKey<BannerPattern> GLOBE = BannerPatterns.create("globe");
    public static final ResourceKey<BannerPattern> CREEPER = BannerPatterns.create("creeper");
    public static final ResourceKey<BannerPattern> SKULL = BannerPatterns.create("skull");
    public static final ResourceKey<BannerPattern> FLOWER = BannerPatterns.create("flower");
    public static final ResourceKey<BannerPattern> MOJANG = BannerPatterns.create("mojang");
    public static final ResourceKey<BannerPattern> PIGLIN = BannerPatterns.create("piglin");

    private static ResourceKey<BannerPattern> create(String string) {
        return ResourceKey.create(Registry.BANNER_PATTERN_REGISTRY, new ResourceLocation(string));
    }

    public static BannerPattern bootstrap(Registry<BannerPattern> registry) {
        Registry.register(registry, BASE, new BannerPattern("b"));
        Registry.register(registry, SQUARE_BOTTOM_LEFT, new BannerPattern("bl"));
        Registry.register(registry, SQUARE_BOTTOM_RIGHT, new BannerPattern("br"));
        Registry.register(registry, SQUARE_TOP_LEFT, new BannerPattern("tl"));
        Registry.register(registry, SQUARE_TOP_RIGHT, new BannerPattern("tr"));
        Registry.register(registry, STRIPE_BOTTOM, new BannerPattern("bs"));
        Registry.register(registry, STRIPE_TOP, new BannerPattern("ts"));
        Registry.register(registry, STRIPE_LEFT, new BannerPattern("ls"));
        Registry.register(registry, STRIPE_RIGHT, new BannerPattern("rs"));
        Registry.register(registry, STRIPE_CENTER, new BannerPattern("cs"));
        Registry.register(registry, STRIPE_MIDDLE, new BannerPattern("ms"));
        Registry.register(registry, STRIPE_DOWNRIGHT, new BannerPattern("drs"));
        Registry.register(registry, STRIPE_DOWNLEFT, new BannerPattern("dls"));
        Registry.register(registry, STRIPE_SMALL, new BannerPattern("ss"));
        Registry.register(registry, CROSS, new BannerPattern("cr"));
        Registry.register(registry, STRAIGHT_CROSS, new BannerPattern("sc"));
        Registry.register(registry, TRIANGLE_BOTTOM, new BannerPattern("bt"));
        Registry.register(registry, TRIANGLE_TOP, new BannerPattern("tt"));
        Registry.register(registry, TRIANGLES_BOTTOM, new BannerPattern("bts"));
        Registry.register(registry, TRIANGLES_TOP, new BannerPattern("tts"));
        Registry.register(registry, DIAGONAL_LEFT, new BannerPattern("ld"));
        Registry.register(registry, DIAGONAL_RIGHT, new BannerPattern("rd"));
        Registry.register(registry, DIAGONAL_LEFT_MIRROR, new BannerPattern("lud"));
        Registry.register(registry, DIAGONAL_RIGHT_MIRROR, new BannerPattern("rud"));
        Registry.register(registry, CIRCLE_MIDDLE, new BannerPattern("mc"));
        Registry.register(registry, RHOMBUS_MIDDLE, new BannerPattern("mr"));
        Registry.register(registry, HALF_VERTICAL, new BannerPattern("vh"));
        Registry.register(registry, HALF_HORIZONTAL, new BannerPattern("hh"));
        Registry.register(registry, HALF_VERTICAL_MIRROR, new BannerPattern("vhr"));
        Registry.register(registry, HALF_HORIZONTAL_MIRROR, new BannerPattern("hhb"));
        Registry.register(registry, BORDER, new BannerPattern("bo"));
        Registry.register(registry, CURLY_BORDER, new BannerPattern("cbo"));
        Registry.register(registry, GRADIENT, new BannerPattern("gra"));
        Registry.register(registry, GRADIENT_UP, new BannerPattern("gru"));
        Registry.register(registry, BRICKS, new BannerPattern("bri"));
        Registry.register(registry, GLOBE, new BannerPattern("glb"));
        Registry.register(registry, CREEPER, new BannerPattern("cre"));
        Registry.register(registry, SKULL, new BannerPattern("sku"));
        Registry.register(registry, FLOWER, new BannerPattern("flo"));
        Registry.register(registry, MOJANG, new BannerPattern("moj"));
        return Registry.register(registry, PIGLIN, new BannerPattern("pig"));
    }
}

