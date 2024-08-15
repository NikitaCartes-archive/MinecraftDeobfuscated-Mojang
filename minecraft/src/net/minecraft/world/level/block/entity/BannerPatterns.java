package net.minecraft.world.level.block.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
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
	public static final ResourceKey<BannerPattern> FLOW = create("flow");
	public static final ResourceKey<BannerPattern> GUSTER = create("guster");

	private static ResourceKey<BannerPattern> create(String string) {
		return ResourceKey.create(Registries.BANNER_PATTERN, ResourceLocation.withDefaultNamespace(string));
	}

	public static void bootstrap(BootstrapContext<BannerPattern> bootstrapContext) {
		register(bootstrapContext, BASE);
		register(bootstrapContext, SQUARE_BOTTOM_LEFT);
		register(bootstrapContext, SQUARE_BOTTOM_RIGHT);
		register(bootstrapContext, SQUARE_TOP_LEFT);
		register(bootstrapContext, SQUARE_TOP_RIGHT);
		register(bootstrapContext, STRIPE_BOTTOM);
		register(bootstrapContext, STRIPE_TOP);
		register(bootstrapContext, STRIPE_LEFT);
		register(bootstrapContext, STRIPE_RIGHT);
		register(bootstrapContext, STRIPE_CENTER);
		register(bootstrapContext, STRIPE_MIDDLE);
		register(bootstrapContext, STRIPE_DOWNRIGHT);
		register(bootstrapContext, STRIPE_DOWNLEFT);
		register(bootstrapContext, STRIPE_SMALL);
		register(bootstrapContext, CROSS);
		register(bootstrapContext, STRAIGHT_CROSS);
		register(bootstrapContext, TRIANGLE_BOTTOM);
		register(bootstrapContext, TRIANGLE_TOP);
		register(bootstrapContext, TRIANGLES_BOTTOM);
		register(bootstrapContext, TRIANGLES_TOP);
		register(bootstrapContext, DIAGONAL_LEFT);
		register(bootstrapContext, DIAGONAL_RIGHT);
		register(bootstrapContext, DIAGONAL_LEFT_MIRROR);
		register(bootstrapContext, DIAGONAL_RIGHT_MIRROR);
		register(bootstrapContext, CIRCLE_MIDDLE);
		register(bootstrapContext, RHOMBUS_MIDDLE);
		register(bootstrapContext, HALF_VERTICAL);
		register(bootstrapContext, HALF_HORIZONTAL);
		register(bootstrapContext, HALF_VERTICAL_MIRROR);
		register(bootstrapContext, HALF_HORIZONTAL_MIRROR);
		register(bootstrapContext, BORDER);
		register(bootstrapContext, GRADIENT);
		register(bootstrapContext, GRADIENT_UP);
		register(bootstrapContext, BRICKS);
		register(bootstrapContext, CURLY_BORDER);
		register(bootstrapContext, GLOBE);
		register(bootstrapContext, CREEPER);
		register(bootstrapContext, SKULL);
		register(bootstrapContext, FLOWER);
		register(bootstrapContext, MOJANG);
		register(bootstrapContext, PIGLIN);
		register(bootstrapContext, FLOW);
		register(bootstrapContext, GUSTER);
	}

	public static void register(BootstrapContext<BannerPattern> bootstrapContext, ResourceKey<BannerPattern> resourceKey) {
		bootstrapContext.register(resourceKey, new BannerPattern(resourceKey.location(), "block.minecraft.banner." + resourceKey.location().toShortLanguageKey()));
	}
}
