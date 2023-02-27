/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class EntityTypeTagsProvider
extends IntrinsicHolderTagsProvider<EntityType<?>> {
    public EntityTypeTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(packOutput, Registries.ENTITY_TYPE, completableFuture, (T entityType) -> entityType.builtInRegistryHolder().key());
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.SKELETONS)).add(EntityType.SKELETON, EntityType.STRAY, EntityType.WITHER_SKELETON);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.RAIDERS)).add(EntityType.EVOKER, EntityType.PILLAGER, EntityType.RAVAGER, EntityType.VINDICATOR, EntityType.ILLUSIONER, EntityType.WITCH);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.BEEHIVE_INHABITORS)).add(EntityType.BEE);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.ARROWS)).add(EntityType.ARROW, EntityType.SPECTRAL_ARROW);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.IMPACT_PROJECTILES)).addTag((TagKey)EntityTypeTags.ARROWS)).add(EntityType.SNOWBALL, EntityType.FIREBALL, EntityType.SMALL_FIREBALL, EntityType.EGG, EntityType.TRIDENT, EntityType.DRAGON_FIREBALL, EntityType.WITHER_SKULL);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS)).add(EntityType.RABBIT, EntityType.ENDERMITE, EntityType.SILVERFISH, EntityType.FOX);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.AXOLOTL_HUNT_TARGETS)).add(EntityType.TROPICAL_FISH, EntityType.PUFFERFISH, EntityType.SALMON, EntityType.COD, EntityType.SQUID, EntityType.GLOW_SQUID, EntityType.TADPOLE);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.AXOLOTL_ALWAYS_HOSTILES)).add(EntityType.DROWNED, EntityType.GUARDIAN, EntityType.ELDER_GUARDIAN);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES)).add(EntityType.STRAY, EntityType.POLAR_BEAR, EntityType.SNOW_GOLEM, EntityType.WITHER);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES)).add(EntityType.STRIDER, EntityType.BLAZE, EntityType.MAGMA_CUBE);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.FROG_FOOD)).add(EntityType.SLIME, EntityType.MAGMA_CUBE);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.FALL_DAMAGE_IMMUNE)).add(EntityType.IRON_GOLEM, EntityType.SNOW_GOLEM, EntityType.SHULKER, EntityType.ALLAY, EntityType.BAT, EntityType.BEE, EntityType.BLAZE, EntityType.CAT, EntityType.CHICKEN, EntityType.GHAST, EntityType.PHANTOM, EntityType.MAGMA_CUBE, EntityType.OCELOT, EntityType.PARROT, EntityType.WITHER);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.DISMOUNTS_UNDERWATER)).add(EntityType.CAMEL, EntityType.CHICKEN, EntityType.DONKEY, EntityType.HORSE, EntityType.LLAMA, EntityType.MULE, EntityType.PIG, EntityType.RAVAGER, EntityType.SPIDER, EntityType.STRIDER, EntityType.TRADER_LLAMA, EntityType.ZOMBIE_HORSE);
    }
}

