/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Sets;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import java.util.Optional;
import java.util.Set;
import net.minecraft.util.datafix.fixes.AbstractUUIDFix;
import net.minecraft.util.datafix.fixes.References;

public class EntityUUIDFix
extends AbstractUUIDFix {
    private static final Set<String> ABSTRACT_HORSES = Sets.newHashSet();
    private static final Set<String> TAMEABLE_ANIMALS = Sets.newHashSet();
    private static final Set<String> ANIMALS = Sets.newHashSet();
    private static final Set<String> MOBS = Sets.newHashSet();
    private static final Set<String> LIVING_ENTITIES = Sets.newHashSet();
    private static final Set<String> PROJECTILES = Sets.newHashSet();

    public EntityUUIDFix(Schema schema) {
        super(schema, References.ENTITY);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("EntityUUIDFixes", this.getInputSchema().getType(this.typeReference), typed -> {
            typed = typed.update(DSL.remainderFinder(), this::updateEntityUUID);
            for (String string : ABSTRACT_HORSES) {
                typed = this.updateNamedChoice((Typed<?>)typed, string, this::updateAnimalOwner);
            }
            for (String string : TAMEABLE_ANIMALS) {
                typed = this.updateNamedChoice((Typed<?>)typed, string, this::updateAnimalOwner);
            }
            for (String string : ANIMALS) {
                typed = this.updateNamedChoice((Typed<?>)typed, string, this::updateAnimal);
            }
            for (String string : MOBS) {
                typed = this.updateNamedChoice((Typed<?>)typed, string, this::updateMob);
            }
            for (String string : LIVING_ENTITIES) {
                typed = this.updateNamedChoice((Typed<?>)typed, string, this::updateLivingEntity);
            }
            for (String string : PROJECTILES) {
                typed = this.updateNamedChoice((Typed<?>)typed, string, this::updateProjectile);
            }
            typed = this.updateNamedChoice((Typed<?>)typed, "minecraft:bee", this::updateHurtBy);
            typed = this.updateNamedChoice((Typed<?>)typed, "minecraft:zombified_piglin", this::updateHurtBy);
            typed = this.updateNamedChoice((Typed<?>)typed, "minecraft:fox", this::updateFox);
            typed = this.updateNamedChoice((Typed<?>)typed, "minecraft:item", this::updateItem);
            typed = this.updateNamedChoice((Typed<?>)typed, "minecraft:shulker_bullet", this::updateShulkerBullet);
            typed = this.updateNamedChoice((Typed<?>)typed, "minecraft:area_effect_cloud", this::updateAreaEffectCloud);
            typed = this.updateNamedChoice((Typed<?>)typed, "minecraft:zombie_villager", this::updateZombieVillager);
            typed = this.updateNamedChoice((Typed<?>)typed, "minecraft:evoker_fangs", this::updateEvokerFangs);
            typed = this.updateNamedChoice((Typed<?>)typed, "minecraft:piglin", this::updatePiglin);
            return typed;
        });
    }

    private Dynamic<?> updatePiglin(Dynamic<?> dynamic2) {
        return dynamic2.update("Brain", dynamic -> dynamic.update("memories", dynamic2 -> dynamic2.update("minecraft:angry_at", dynamic -> EntityUUIDFix.replaceUUIDString(dynamic, "value", "value").orElseGet(() -> {
            LOGGER.warn("angry_at has no value.");
            return dynamic;
        }))));
    }

    private Dynamic<?> updateEvokerFangs(Dynamic<?> dynamic) {
        return EntityUUIDFix.replaceUUIDLeastMost(dynamic, "OwnerUUID", "Owner").orElse(dynamic);
    }

    private Dynamic<?> updateZombieVillager(Dynamic<?> dynamic) {
        return EntityUUIDFix.replaceUUIDLeastMost(dynamic, "ConversionPlayer", "ConversionPlayer").orElse(dynamic);
    }

    private Dynamic<?> updateAreaEffectCloud(Dynamic<?> dynamic) {
        return EntityUUIDFix.replaceUUIDLeastMost(dynamic, "OwnerUUID", "Owner").orElse(dynamic);
    }

    private Dynamic<?> updateShulkerBullet(Dynamic<?> dynamic) {
        dynamic = EntityUUIDFix.replaceUUIDMLTag(dynamic, "Owner", "Owner").orElse(dynamic);
        return EntityUUIDFix.replaceUUIDMLTag(dynamic, "Target", "Target").orElse(dynamic);
    }

    private Dynamic<?> updateItem(Dynamic<?> dynamic) {
        dynamic = EntityUUIDFix.replaceUUIDMLTag(dynamic, "Owner", "Owner").orElse(dynamic);
        return EntityUUIDFix.replaceUUIDMLTag(dynamic, "Thrower", "Thrower").orElse(dynamic);
    }

    private Dynamic<?> updateFox(Dynamic<?> dynamic) {
        Optional<Dynamic> optional = dynamic.get("TrustedUUIDs").map(dynamic22 -> dynamic.createList(dynamic22.asStream().map(dynamic -> EntityUUIDFix.createUUIDFromML(dynamic).orElseGet(() -> {
            LOGGER.warn("Trusted contained invalid data.");
            return dynamic;
        }))));
        return DataFixUtils.orElse(optional.map(dynamic2 -> dynamic.remove("TrustedUUIDs").set("Trusted", (Dynamic<?>)dynamic2)), dynamic);
    }

    private Dynamic<?> updateHurtBy(Dynamic<?> dynamic) {
        return EntityUUIDFix.replaceUUIDString(dynamic, "HurtBy", "HurtBy").orElse(dynamic);
    }

    private Dynamic<?> updateAnimalOwner(Dynamic<?> dynamic) {
        Dynamic<?> dynamic2 = this.updateAnimal(dynamic);
        return EntityUUIDFix.replaceUUIDString(dynamic2, "OwnerUUID", "Owner").orElse(dynamic2);
    }

    private Dynamic<?> updateAnimal(Dynamic<?> dynamic) {
        Dynamic<?> dynamic2 = this.updateMob(dynamic);
        return EntityUUIDFix.replaceUUIDLeastMost(dynamic2, "LoveCause", "LoveCause").orElse(dynamic2);
    }

    private Dynamic<?> updateMob(Dynamic<?> dynamic2) {
        return this.updateLivingEntity(dynamic2).update("Leash", dynamic -> EntityUUIDFix.replaceUUIDLeastMost(dynamic, "UUID", "UUID").orElse((Dynamic<?>)dynamic));
    }

    private Dynamic<?> updateLivingEntity(Dynamic<?> dynamic) {
        return dynamic.update("Attributes", dynamic22 -> dynamic.createList(dynamic22.asStream().map(dynamic -> dynamic.update("Modifiers", dynamic22 -> dynamic.createList(dynamic22.asStream().map(dynamic -> EntityUUIDFix.replaceUUIDLeastMost(dynamic, "UUID", "UUID").orElse((Dynamic<?>)dynamic)))))));
    }

    private Dynamic<?> updateProjectile(Dynamic<?> dynamic) {
        return DataFixUtils.orElse(dynamic.get("OwnerUUID").map(dynamic2 -> dynamic.remove("OwnerUUID").set("Owner", (Dynamic<?>)dynamic2)), dynamic);
    }

    private Dynamic<?> updateEntityUUID(Dynamic<?> dynamic) {
        return EntityUUIDFix.replaceUUIDLeastMost(dynamic, "UUID", "UUID").orElse(dynamic);
    }

    static {
        ABSTRACT_HORSES.add("minecraft:donkey");
        ABSTRACT_HORSES.add("minecraft:horse");
        ABSTRACT_HORSES.add("minecraft:llama");
        ABSTRACT_HORSES.add("minecraft:mule");
        ABSTRACT_HORSES.add("minecraft:skeleton_horse");
        ABSTRACT_HORSES.add("minecraft:trader_llama");
        ABSTRACT_HORSES.add("minecraft:zombie_horse");
        TAMEABLE_ANIMALS.add("minecraft:cat");
        TAMEABLE_ANIMALS.add("minecraft:parrot");
        TAMEABLE_ANIMALS.add("minecraft:wolf");
        ANIMALS.add("minecraft:bee");
        ANIMALS.add("minecraft:chicken");
        ANIMALS.add("minecraft:cow");
        ANIMALS.add("minecraft:fox");
        ANIMALS.add("minecraft:mooshroom");
        ANIMALS.add("minecraft:ocelot");
        ANIMALS.add("minecraft:panda");
        ANIMALS.add("minecraft:pig");
        ANIMALS.add("minecraft:polar_bear");
        ANIMALS.add("minecraft:rabbit");
        ANIMALS.add("minecraft:sheep");
        ANIMALS.add("minecraft:turtle");
        ANIMALS.add("minecraft:hoglin");
        MOBS.add("minecraft:bat");
        MOBS.add("minecraft:blaze");
        MOBS.add("minecraft:cave_spider");
        MOBS.add("minecraft:cod");
        MOBS.add("minecraft:creeper");
        MOBS.add("minecraft:dolphin");
        MOBS.add("minecraft:drowned");
        MOBS.add("minecraft:elder_guardian");
        MOBS.add("minecraft:ender_dragon");
        MOBS.add("minecraft:enderman");
        MOBS.add("minecraft:endermite");
        MOBS.add("minecraft:evoker");
        MOBS.add("minecraft:ghast");
        MOBS.add("minecraft:giant");
        MOBS.add("minecraft:guardian");
        MOBS.add("minecraft:husk");
        MOBS.add("minecraft:illusioner");
        MOBS.add("minecraft:magma_cube");
        MOBS.add("minecraft:pufferfish");
        MOBS.add("minecraft:zombified_piglin");
        MOBS.add("minecraft:salmon");
        MOBS.add("minecraft:shulker");
        MOBS.add("minecraft:silverfish");
        MOBS.add("minecraft:skeleton");
        MOBS.add("minecraft:slime");
        MOBS.add("minecraft:snow_golem");
        MOBS.add("minecraft:spider");
        MOBS.add("minecraft:squid");
        MOBS.add("minecraft:stray");
        MOBS.add("minecraft:tropical_fish");
        MOBS.add("minecraft:vex");
        MOBS.add("minecraft:villager");
        MOBS.add("minecraft:iron_golem");
        MOBS.add("minecraft:vindicator");
        MOBS.add("minecraft:pillager");
        MOBS.add("minecraft:wandering_trader");
        MOBS.add("minecraft:witch");
        MOBS.add("minecraft:wither");
        MOBS.add("minecraft:wither_skeleton");
        MOBS.add("minecraft:zombie");
        MOBS.add("minecraft:zombie_villager");
        MOBS.add("minecraft:phantom");
        MOBS.add("minecraft:ravager");
        MOBS.add("minecraft:piglin");
        LIVING_ENTITIES.add("minecraft:armor_stand");
        PROJECTILES.add("minecraft:arrow");
        PROJECTILES.add("minecraft:dragon_fireball");
        PROJECTILES.add("minecraft:firework_rocket");
        PROJECTILES.add("minecraft:fireball");
        PROJECTILES.add("minecraft:llama_spit");
        PROJECTILES.add("minecraft:small_fireball");
        PROJECTILES.add("minecraft:snowball");
        PROJECTILES.add("minecraft:spectral_arrow");
        PROJECTILES.add("minecraft:egg");
        PROJECTILES.add("minecraft:ender_pearl");
        PROJECTILES.add("minecraft:experience_bottle");
        PROJECTILES.add("minecraft:potion");
        PROJECTILES.add("minecraft:trident");
        PROJECTILES.add("minecraft:wither_skull");
    }
}

