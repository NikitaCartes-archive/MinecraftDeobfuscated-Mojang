/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.material;

import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.material.PushReaction;

public final class Material {
    public static final Material AIR = Builder.method_15808(new Builder(MaterialColor.NONE).noCollider()).nonSolid().replaceable().build();
    public static final Material STRUCTURAL_AIR = Builder.method_15808(new Builder(MaterialColor.NONE).noCollider()).nonSolid().replaceable().build();
    public static final Material PORTAL = Builder.method_15808(new Builder(MaterialColor.NONE).noCollider()).nonSolid().notPushable().build();
    public static final Material CLOTH_DECORATION = Builder.method_15808(new Builder(MaterialColor.WOOL).noCollider()).nonSolid().flammable().build();
    public static final Material PLANT = Builder.method_15808(new Builder(MaterialColor.PLANT).noCollider()).nonSolid().destroyOnPush().build();
    public static final Material WATER_PLANT = Builder.method_15808(new Builder(MaterialColor.WATER).noCollider()).nonSolid().destroyOnPush().build();
    public static final Material REPLACEABLE_PLANT = Builder.method_15808(new Builder(MaterialColor.PLANT).noCollider()).nonSolid().destroyOnPush().replaceable().flammable().build();
    public static final Material REPLACEABLE_FIREPROOF_PLANT = Builder.method_15808(new Builder(MaterialColor.PLANT).noCollider()).nonSolid().destroyOnPush().replaceable().build();
    public static final Material REPLACEABLE_WATER_PLANT = Builder.method_15808(new Builder(MaterialColor.WATER).noCollider()).nonSolid().destroyOnPush().replaceable().build();
    public static final Material WATER = Builder.method_15808(new Builder(MaterialColor.WATER).noCollider()).nonSolid().destroyOnPush().replaceable().liquid().build();
    public static final Material BUBBLE_COLUMN = Builder.method_15808(new Builder(MaterialColor.WATER).noCollider()).nonSolid().destroyOnPush().replaceable().liquid().build();
    public static final Material LAVA = Builder.method_15808(new Builder(MaterialColor.FIRE).noCollider()).nonSolid().destroyOnPush().replaceable().liquid().build();
    public static final Material TOP_SNOW = Builder.method_15808(new Builder(MaterialColor.SNOW).noCollider()).nonSolid().destroyOnPush().replaceable().build();
    public static final Material FIRE = Builder.method_15808(new Builder(MaterialColor.NONE).noCollider()).nonSolid().destroyOnPush().replaceable().build();
    public static final Material DECORATION = Builder.method_15808(new Builder(MaterialColor.NONE).noCollider()).nonSolid().destroyOnPush().build();
    public static final Material WEB = Builder.method_15808(new Builder(MaterialColor.WOOL).noCollider()).destroyOnPush().build();
    public static final Material SCULK = new Builder(MaterialColor.COLOR_BLACK).build();
    public static final Material BUILDABLE_GLASS = new Builder(MaterialColor.NONE).build();
    public static final Material CLAY = new Builder(MaterialColor.CLAY).build();
    public static final Material DIRT = new Builder(MaterialColor.DIRT).build();
    public static final Material GRASS = new Builder(MaterialColor.GRASS).build();
    public static final Material ICE_SOLID = new Builder(MaterialColor.ICE).build();
    public static final Material SAND = new Builder(MaterialColor.SAND).build();
    public static final Material SPONGE = new Builder(MaterialColor.COLOR_YELLOW).build();
    public static final Material SHULKER_SHELL = new Builder(MaterialColor.COLOR_PURPLE).build();
    public static final Material WOOD = new Builder(MaterialColor.WOOD).flammable().build();
    public static final Material NETHER_WOOD = new Builder(MaterialColor.WOOD).build();
    public static final Material BAMBOO_SAPLING = new Builder(MaterialColor.WOOD).flammable().destroyOnPush().noCollider().build();
    public static final Material BAMBOO = new Builder(MaterialColor.WOOD).flammable().destroyOnPush().build();
    public static final Material WOOL = new Builder(MaterialColor.WOOL).flammable().build();
    public static final Material EXPLOSIVE = Builder.method_15808(new Builder(MaterialColor.FIRE).flammable()).build();
    public static final Material LEAVES = Builder.method_15808(new Builder(MaterialColor.PLANT).flammable()).destroyOnPush().build();
    public static final Material GLASS = Builder.method_15808(new Builder(MaterialColor.NONE)).build();
    public static final Material ICE = Builder.method_15808(new Builder(MaterialColor.ICE)).build();
    public static final Material CACTUS = Builder.method_15808(new Builder(MaterialColor.PLANT)).destroyOnPush().build();
    public static final Material STONE = new Builder(MaterialColor.STONE).build();
    public static final Material METAL = new Builder(MaterialColor.METAL).build();
    public static final Material SNOW = new Builder(MaterialColor.SNOW).build();
    public static final Material HEAVY_METAL = new Builder(MaterialColor.METAL).notPushable().build();
    public static final Material BARRIER = new Builder(MaterialColor.NONE).notPushable().build();
    public static final Material PISTON = new Builder(MaterialColor.STONE).notPushable().build();
    public static final Material CORAL = new Builder(MaterialColor.PLANT).destroyOnPush().build();
    public static final Material VEGETABLE = new Builder(MaterialColor.PLANT).destroyOnPush().build();
    public static final Material EGG = new Builder(MaterialColor.PLANT).destroyOnPush().build();
    public static final Material CAKE = new Builder(MaterialColor.NONE).destroyOnPush().build();
    public static final Material AMETHYST = Builder.method_15808(new Builder(MaterialColor.COLOR_PURPLE)).destroyOnPush().build();
    public static final Material POWDER_SNOW = new Builder(MaterialColor.SNOW).nonSolid().noCollider().build();
    private final MaterialColor color;
    private final PushReaction pushReaction;
    private final boolean blocksMotion;
    private final boolean flammable;
    private final boolean liquid;
    private final boolean solidBlocking;
    private final boolean replaceable;
    private final boolean solid;

    public Material(MaterialColor materialColor, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5, boolean bl6, PushReaction pushReaction) {
        this.color = materialColor;
        this.liquid = bl;
        this.solid = bl2;
        this.blocksMotion = bl3;
        this.solidBlocking = bl4;
        this.flammable = bl5;
        this.replaceable = bl6;
        this.pushReaction = pushReaction;
    }

    public boolean isLiquid() {
        return this.liquid;
    }

    public boolean isSolid() {
        return this.solid;
    }

    public boolean blocksMotion() {
        return this.blocksMotion;
    }

    public boolean isFlammable() {
        return this.flammable;
    }

    public boolean isReplaceable() {
        return this.replaceable;
    }

    public boolean isSolidBlocking() {
        return this.solidBlocking;
    }

    public PushReaction getPushReaction() {
        return this.pushReaction;
    }

    public MaterialColor getColor() {
        return this.color;
    }

    public static class Builder {
        private PushReaction pushReaction = PushReaction.NORMAL;
        private boolean blocksMotion = true;
        private boolean flammable;
        private boolean liquid;
        private boolean replaceable;
        private boolean solid = true;
        private final MaterialColor color;
        private boolean solidBlocking = true;

        public Builder(MaterialColor materialColor) {
            this.color = materialColor;
        }

        public Builder liquid() {
            this.liquid = true;
            return this;
        }

        public Builder nonSolid() {
            this.solid = false;
            return this;
        }

        public Builder noCollider() {
            this.blocksMotion = false;
            return this;
        }

        private Builder notSolidBlocking() {
            this.solidBlocking = false;
            return this;
        }

        protected Builder flammable() {
            this.flammable = true;
            return this;
        }

        public Builder replaceable() {
            this.replaceable = true;
            return this;
        }

        protected Builder destroyOnPush() {
            this.pushReaction = PushReaction.DESTROY;
            return this;
        }

        protected Builder notPushable() {
            this.pushReaction = PushReaction.BLOCK;
            return this;
        }

        public Material build() {
            return new Material(this.color, this.liquid, this.solid, this.blocksMotion, this.solidBlocking, this.flammable, this.replaceable, this.pushReaction);
        }

        static /* synthetic */ Builder method_15808(Builder builder) {
            return builder.notSolidBlocking();
        }
    }
}

