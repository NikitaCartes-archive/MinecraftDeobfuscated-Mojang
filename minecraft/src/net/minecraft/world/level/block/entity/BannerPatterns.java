package net.minecraft.world.level.block.entity;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class BannerPatterns {
	public static final ResourceKey<BannerPattern> BASE = create("base");
	public static final ResourceKey<BannerPattern> SQUARE_BOTTOM_LEFT = create("square_bottom_left");
	public static final ResourceKey<BannerPattern> SQUARE_BOTTOM_RIGHT = create("square_bottom_right");
	public static final ResourceKey<BannerPattern> SQUARE_TOP_LEFT = create("square_top_left");
	public static final ResourceKey<BannerPattern> SQUARE_TOP_RIGHT = create("square_top_right");
	public static final ResourceKey<BannerPattern> STRIPE_BOTTOM = create("stripe_bottom");
	public static final ResourceKey<BannerPattern> STRIPE_TOP = create("stripe_top");
	public static final ResourceKey<BannerPattern> STRIPE_LEFT = create("stripe_left");
	public static final ResourceKey<BannerPattern> STRIPE_RIGHT = create("stripe_right");
	public static final ResourceKey<BannerPattern> STRIPE_CENTER = create("stripe_center");
	public static final ResourceKey<BannerPattern> STRIPE_MIDDLE = create("stripe_middle");
	public static final ResourceKey<BannerPattern> STRIPE_DOWNRIGHT = create("stripe_downright");
	public static final ResourceKey<BannerPattern> STRIPE_DOWNLEFT = create("stripe_downleft");
	public static final ResourceKey<BannerPattern> STRIPE_SMALL = create("small_stripes");
	public static final ResourceKey<BannerPattern> CROSS = create("cross");
	public static final ResourceKey<BannerPattern> STRAIGHT_CROSS = create("straight_cross");
	public static final ResourceKey<BannerPattern> TRIANGLE_BOTTOM = create("triangle_bottom");
	public static final ResourceKey<BannerPattern> TRIANGLE_TOP = create("triangle_top");
	public static final ResourceKey<BannerPattern> TRIANGLES_BOTTOM = create("triangles_bottom");
	public static final ResourceKey<BannerPattern> TRIANGLES_TOP = create("triangles_top");
	public static final ResourceKey<BannerPattern> DIAGONAL_LEFT = create("diagonal_left");
	public static final ResourceKey<BannerPattern> DIAGONAL_RIGHT = create("diagonal_up_right");
	public static final ResourceKey<BannerPattern> DIAGONAL_LEFT_MIRROR = create("diagonal_up_left");
	public static final ResourceKey<BannerPattern> DIAGONAL_RIGHT_MIRROR = create("diagonal_right");
	public static final ResourceKey<BannerPattern> CIRCLE_MIDDLE = create("circle");
	public static final ResourceKey<BannerPattern> RHOMBUS_MIDDLE = create("rhombus");
	public static final ResourceKey<BannerPattern> HALF_VERTICAL = create("half_vertical");
	public static final ResourceKey<BannerPattern> HALF_HORIZONTAL = create("half_horizontal");
	public static final ResourceKey<BannerPattern> HALF_VERTICAL_MIRROR = create("half_vertical_right");
	public static final ResourceKey<BannerPattern> HALF_HORIZONTAL_MIRROR = create("half_horizontal_bottom");
	public static final ResourceKey<BannerPattern> BORDER = create("border");
	public static final ResourceKey<BannerPattern> CURLY_BORDER = create("curly_border");
	public static final ResourceKey<BannerPattern> GRADIENT = create("gradient");
	public static final ResourceKey<BannerPattern> GRADIENT_UP = create("gradient_up");
	public static final ResourceKey<BannerPattern> BRICKS = create("bricks");
	public static final ResourceKey<BannerPattern> GLOBE = create("globe");
	public static final ResourceKey<BannerPattern> CREEPER = create("creeper");
	public static final ResourceKey<BannerPattern> SKULL = create("skull");
	public static final ResourceKey<BannerPattern> FLOWER = create("flower");
	public static final ResourceKey<BannerPattern> MOJANG = create("mojang");
	public static final ResourceKey<BannerPattern> PIGLIN = create("piglin");

	private static ResourceKey<BannerPattern> create(String string) {
		return ResourceKey.create(Registries.BANNER_PATTERN, new ResourceLocation(string));
	}

	public static BannerPattern bootstrap(Registry<BannerPattern> registry) {
		register(registry, BASE);
		register(registry, SQUARE_BOTTOM_LEFT);
		register(registry, SQUARE_BOTTOM_RIGHT);
		register(registry, SQUARE_TOP_LEFT);
		register(registry, SQUARE_TOP_RIGHT);
		register(registry, STRIPE_BOTTOM);
		register(registry, STRIPE_TOP);
		register(registry, STRIPE_LEFT);
		register(registry, STRIPE_RIGHT);
		register(registry, STRIPE_CENTER);
		register(registry, STRIPE_MIDDLE);
		register(registry, STRIPE_DOWNRIGHT);
		register(registry, STRIPE_DOWNLEFT);
		register(registry, STRIPE_SMALL);
		register(registry, CROSS);
		register(registry, STRAIGHT_CROSS);
		register(registry, TRIANGLE_BOTTOM);
		register(registry, TRIANGLE_TOP);
		register(registry, TRIANGLES_BOTTOM);
		register(registry, TRIANGLES_TOP);
		register(registry, DIAGONAL_LEFT);
		register(registry, DIAGONAL_RIGHT);
		register(registry, DIAGONAL_LEFT_MIRROR);
		register(registry, DIAGONAL_RIGHT_MIRROR);
		register(registry, CIRCLE_MIDDLE);
		register(registry, RHOMBUS_MIDDLE);
		register(registry, HALF_VERTICAL);
		register(registry, HALF_HORIZONTAL);
		register(registry, HALF_VERTICAL_MIRROR);
		register(registry, HALF_HORIZONTAL_MIRROR);
		register(registry, BORDER);
		register(registry, CURLY_BORDER);
		register(registry, GRADIENT);
		register(registry, GRADIENT_UP);
		register(registry, BRICKS);
		register(registry, GLOBE);
		register(registry, CREEPER);
		register(registry, SKULL);
		register(registry, FLOWER);
		register(registry, MOJANG);
		return register(registry, PIGLIN);
	}

	private static BannerPattern register(Registry<BannerPattern> registry, ResourceKey<BannerPattern> resourceKey) {
		return Registry.register(registry, resourceKey, new BannerPattern());
	}
}
