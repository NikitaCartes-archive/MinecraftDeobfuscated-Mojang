/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.schemas;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.Hook;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import net.minecraft.util.datafix.schemas.V100;
import net.minecraft.util.datafix.schemas.V704;
import net.minecraft.util.datafix.schemas.V99;

public class V705
extends NamespacedSchema {
    protected static final Hook.HookFunction ADD_NAMES = new Hook.HookFunction(){

        @Override
        public <T> T apply(DynamicOps<T> dynamicOps, T object) {
            return V99.addNames(new Dynamic<T>(dynamicOps, object), V704.ITEM_TO_BLOCKENTITY, "minecraft:armor_stand");
        }
    };

    public V705(int i, Schema schema) {
        super(i, schema);
    }

    protected static void registerMob(Schema schema, Map<String, Supplier<TypeTemplate>> map, String string) {
        schema.register(map, string, () -> V100.equipment(schema));
    }

    protected static void registerThrowableProjectile(Schema schema, Map<String, Supplier<TypeTemplate>> map, String string) {
        schema.register(map, string, () -> DSL.optionalFields("inTile", References.BLOCK_NAME.in(schema)));
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        HashMap<String, Supplier<TypeTemplate>> map = Maps.newHashMap();
        schema.registerSimple(map, "minecraft:area_effect_cloud");
        V705.registerMob(schema, map, "minecraft:armor_stand");
        schema.register(map, "minecraft:arrow", (String string) -> DSL.optionalFields("inTile", References.BLOCK_NAME.in(schema)));
        V705.registerMob(schema, map, "minecraft:bat");
        V705.registerMob(schema, map, "minecraft:blaze");
        schema.registerSimple(map, "minecraft:boat");
        V705.registerMob(schema, map, "minecraft:cave_spider");
        schema.register(map, "minecraft:chest_minecart", (String string) -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(schema), "Items", DSL.list(References.ITEM_STACK.in(schema))));
        V705.registerMob(schema, map, "minecraft:chicken");
        schema.register(map, "minecraft:commandblock_minecart", (String string) -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(schema)));
        V705.registerMob(schema, map, "minecraft:cow");
        V705.registerMob(schema, map, "minecraft:creeper");
        schema.register(map, "minecraft:donkey", (String string) -> DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(schema)), "SaddleItem", References.ITEM_STACK.in(schema), V100.equipment(schema)));
        schema.registerSimple(map, "minecraft:dragon_fireball");
        V705.registerThrowableProjectile(schema, map, "minecraft:egg");
        V705.registerMob(schema, map, "minecraft:elder_guardian");
        schema.registerSimple(map, "minecraft:ender_crystal");
        V705.registerMob(schema, map, "minecraft:ender_dragon");
        schema.register(map, "minecraft:enderman", (String string) -> DSL.optionalFields("carried", References.BLOCK_NAME.in(schema), V100.equipment(schema)));
        V705.registerMob(schema, map, "minecraft:endermite");
        V705.registerThrowableProjectile(schema, map, "minecraft:ender_pearl");
        schema.registerSimple(map, "minecraft:eye_of_ender_signal");
        schema.register(map, "minecraft:falling_block", (String string) -> DSL.optionalFields("Block", References.BLOCK_NAME.in(schema), "TileEntityData", References.BLOCK_ENTITY.in(schema)));
        V705.registerThrowableProjectile(schema, map, "minecraft:fireball");
        schema.register(map, "minecraft:fireworks_rocket", (String string) -> DSL.optionalFields("FireworksItem", References.ITEM_STACK.in(schema)));
        schema.register(map, "minecraft:furnace_minecart", (String string) -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(schema)));
        V705.registerMob(schema, map, "minecraft:ghast");
        V705.registerMob(schema, map, "minecraft:giant");
        V705.registerMob(schema, map, "minecraft:guardian");
        schema.register(map, "minecraft:hopper_minecart", (String string) -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(schema), "Items", DSL.list(References.ITEM_STACK.in(schema))));
        schema.register(map, "minecraft:horse", (String string) -> DSL.optionalFields("ArmorItem", References.ITEM_STACK.in(schema), "SaddleItem", References.ITEM_STACK.in(schema), V100.equipment(schema)));
        V705.registerMob(schema, map, "minecraft:husk");
        schema.register(map, "minecraft:item", (String string) -> DSL.optionalFields("Item", References.ITEM_STACK.in(schema)));
        schema.register(map, "minecraft:item_frame", (String string) -> DSL.optionalFields("Item", References.ITEM_STACK.in(schema)));
        schema.registerSimple(map, "minecraft:leash_knot");
        V705.registerMob(schema, map, "minecraft:magma_cube");
        schema.register(map, "minecraft:minecart", (String string) -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(schema)));
        V705.registerMob(schema, map, "minecraft:mooshroom");
        schema.register(map, "minecraft:mule", (String string) -> DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(schema)), "SaddleItem", References.ITEM_STACK.in(schema), V100.equipment(schema)));
        V705.registerMob(schema, map, "minecraft:ocelot");
        schema.registerSimple(map, "minecraft:painting");
        schema.registerSimple(map, "minecraft:parrot");
        V705.registerMob(schema, map, "minecraft:pig");
        V705.registerMob(schema, map, "minecraft:polar_bear");
        schema.register(map, "minecraft:potion", (String string) -> DSL.optionalFields("Potion", References.ITEM_STACK.in(schema), "inTile", References.BLOCK_NAME.in(schema)));
        V705.registerMob(schema, map, "minecraft:rabbit");
        V705.registerMob(schema, map, "minecraft:sheep");
        V705.registerMob(schema, map, "minecraft:shulker");
        schema.registerSimple(map, "minecraft:shulker_bullet");
        V705.registerMob(schema, map, "minecraft:silverfish");
        V705.registerMob(schema, map, "minecraft:skeleton");
        schema.register(map, "minecraft:skeleton_horse", (String string) -> DSL.optionalFields("SaddleItem", References.ITEM_STACK.in(schema), V100.equipment(schema)));
        V705.registerMob(schema, map, "minecraft:slime");
        V705.registerThrowableProjectile(schema, map, "minecraft:small_fireball");
        V705.registerThrowableProjectile(schema, map, "minecraft:snowball");
        V705.registerMob(schema, map, "minecraft:snowman");
        schema.register(map, "minecraft:spawner_minecart", (String string) -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(schema), References.UNTAGGED_SPAWNER.in(schema)));
        schema.register(map, "minecraft:spectral_arrow", (String string) -> DSL.optionalFields("inTile", References.BLOCK_NAME.in(schema)));
        V705.registerMob(schema, map, "minecraft:spider");
        V705.registerMob(schema, map, "minecraft:squid");
        V705.registerMob(schema, map, "minecraft:stray");
        schema.registerSimple(map, "minecraft:tnt");
        schema.register(map, "minecraft:tnt_minecart", (String string) -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(schema)));
        schema.register(map, "minecraft:villager", (String string) -> DSL.optionalFields("Inventory", DSL.list(References.ITEM_STACK.in(schema)), "Offers", DSL.optionalFields("Recipes", DSL.list(DSL.optionalFields("buy", References.ITEM_STACK.in(schema), "buyB", References.ITEM_STACK.in(schema), "sell", References.ITEM_STACK.in(schema)))), V100.equipment(schema)));
        V705.registerMob(schema, map, "minecraft:villager_golem");
        V705.registerMob(schema, map, "minecraft:witch");
        V705.registerMob(schema, map, "minecraft:wither");
        V705.registerMob(schema, map, "minecraft:wither_skeleton");
        V705.registerThrowableProjectile(schema, map, "minecraft:wither_skull");
        V705.registerMob(schema, map, "minecraft:wolf");
        V705.registerThrowableProjectile(schema, map, "minecraft:xp_bottle");
        schema.registerSimple(map, "minecraft:xp_orb");
        V705.registerMob(schema, map, "minecraft:zombie");
        schema.register(map, "minecraft:zombie_horse", (String string) -> DSL.optionalFields("SaddleItem", References.ITEM_STACK.in(schema), V100.equipment(schema)));
        V705.registerMob(schema, map, "minecraft:zombie_pigman");
        V705.registerMob(schema, map, "minecraft:zombie_villager");
        schema.registerSimple(map, "minecraft:evocation_fangs");
        V705.registerMob(schema, map, "minecraft:evocation_illager");
        schema.registerSimple(map, "minecraft:illusion_illager");
        schema.register(map, "minecraft:llama", (String string) -> DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(schema)), "SaddleItem", References.ITEM_STACK.in(schema), "DecorItem", References.ITEM_STACK.in(schema), V100.equipment(schema)));
        schema.registerSimple(map, "minecraft:llama_spit");
        V705.registerMob(schema, map, "minecraft:vex");
        V705.registerMob(schema, map, "minecraft:vindication_illager");
        return map;
    }

    @Override
    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map2) {
        super.registerTypes(schema, map, map2);
        schema.registerType(true, References.ENTITY, () -> DSL.taggedChoiceLazy("id", V705.namespacedString(), map));
        schema.registerType(true, References.ITEM_STACK, () -> DSL.hook(DSL.optionalFields("id", References.ITEM_NAME.in(schema), "tag", DSL.optionalFields("EntityTag", References.ENTITY_TREE.in(schema), "BlockEntityTag", References.BLOCK_ENTITY.in(schema), "CanDestroy", DSL.list(References.BLOCK_NAME.in(schema)), "CanPlaceOn", DSL.list(References.BLOCK_NAME.in(schema)))), ADD_NAMES, Hook.HookFunction.IDENTITY));
    }
}

