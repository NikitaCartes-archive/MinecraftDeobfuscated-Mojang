/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V100
extends Schema {
    public V100(int i, Schema schema) {
        super(i, schema);
    }

    protected static TypeTemplate equipment(Schema schema) {
        return DSL.optionalFields("ArmorItems", DSL.list(References.ITEM_STACK.in(schema)), "HandItems", DSL.list(References.ITEM_STACK.in(schema)));
    }

    protected static void registerMob(Schema schema, Map<String, Supplier<TypeTemplate>> map, String string) {
        schema.register(map, string, () -> V100.equipment(schema));
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerEntities(schema);
        V100.registerMob(schema, map, "ArmorStand");
        V100.registerMob(schema, map, "Creeper");
        V100.registerMob(schema, map, "Skeleton");
        V100.registerMob(schema, map, "Spider");
        V100.registerMob(schema, map, "Giant");
        V100.registerMob(schema, map, "Zombie");
        V100.registerMob(schema, map, "Slime");
        V100.registerMob(schema, map, "Ghast");
        V100.registerMob(schema, map, "PigZombie");
        schema.register(map, "Enderman", (String string) -> DSL.optionalFields("carried", References.BLOCK_NAME.in(schema), V100.equipment(schema)));
        V100.registerMob(schema, map, "CaveSpider");
        V100.registerMob(schema, map, "Silverfish");
        V100.registerMob(schema, map, "Blaze");
        V100.registerMob(schema, map, "LavaSlime");
        V100.registerMob(schema, map, "EnderDragon");
        V100.registerMob(schema, map, "WitherBoss");
        V100.registerMob(schema, map, "Bat");
        V100.registerMob(schema, map, "Witch");
        V100.registerMob(schema, map, "Endermite");
        V100.registerMob(schema, map, "Guardian");
        V100.registerMob(schema, map, "Pig");
        V100.registerMob(schema, map, "Sheep");
        V100.registerMob(schema, map, "Cow");
        V100.registerMob(schema, map, "Chicken");
        V100.registerMob(schema, map, "Squid");
        V100.registerMob(schema, map, "Wolf");
        V100.registerMob(schema, map, "MushroomCow");
        V100.registerMob(schema, map, "SnowMan");
        V100.registerMob(schema, map, "Ozelot");
        V100.registerMob(schema, map, "VillagerGolem");
        schema.register(map, "EntityHorse", (String string) -> DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(schema)), "ArmorItem", References.ITEM_STACK.in(schema), "SaddleItem", References.ITEM_STACK.in(schema), V100.equipment(schema)));
        V100.registerMob(schema, map, "Rabbit");
        schema.register(map, "Villager", (String string) -> DSL.optionalFields("Inventory", DSL.list(References.ITEM_STACK.in(schema)), "Offers", DSL.optionalFields("Recipes", DSL.list(DSL.optionalFields("buy", References.ITEM_STACK.in(schema), "buyB", References.ITEM_STACK.in(schema), "sell", References.ITEM_STACK.in(schema)))), V100.equipment(schema)));
        V100.registerMob(schema, map, "Shulker");
        schema.registerSimple(map, "AreaEffectCloud");
        schema.registerSimple(map, "ShulkerBullet");
        return map;
    }

    @Override
    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map2) {
        super.registerTypes(schema, map, map2);
        schema.registerType(false, References.STRUCTURE, () -> DSL.optionalFields("entities", DSL.list(DSL.optionalFields("nbt", References.ENTITY_TREE.in(schema))), "blocks", DSL.list(DSL.optionalFields("nbt", References.BLOCK_ENTITY.in(schema))), "palette", DSL.list(References.BLOCK_STATE.in(schema))));
        schema.registerType(false, References.BLOCK_STATE, DSL::remainder);
    }
}

