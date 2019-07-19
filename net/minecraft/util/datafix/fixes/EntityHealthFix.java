/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Sets;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import java.util.Optional;
import java.util.Set;
import net.minecraft.util.datafix.fixes.References;

public class EntityHealthFix
extends DataFix {
    private static final Set<String> ENTITIES = Sets.newHashSet("ArmorStand", "Bat", "Blaze", "CaveSpider", "Chicken", "Cow", "Creeper", "EnderDragon", "Enderman", "Endermite", "EntityHorse", "Ghast", "Giant", "Guardian", "LavaSlime", "MushroomCow", "Ozelot", "Pig", "PigZombie", "Rabbit", "Sheep", "Shulker", "Silverfish", "Skeleton", "Slime", "SnowMan", "Spider", "Squid", "Villager", "VillagerGolem", "Witch", "WitherBoss", "Wolf", "Zombie");

    public EntityHealthFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    public Dynamic<?> fixTag(Dynamic<?> dynamic) {
        float f;
        Optional<Number> optional = dynamic.get("HealF").asNumber();
        Optional<Number> optional2 = dynamic.get("Health").asNumber();
        if (optional.isPresent()) {
            f = optional.get().floatValue();
            dynamic = dynamic.remove("HealF");
        } else if (optional2.isPresent()) {
            f = optional2.get().floatValue();
        } else {
            return dynamic;
        }
        return dynamic.set("Health", dynamic.createFloat(f));
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("EntityHealthFix", this.getInputSchema().getType(References.ENTITY), typed -> typed.update(DSL.remainderFinder(), this::fixTag));
    }
}

