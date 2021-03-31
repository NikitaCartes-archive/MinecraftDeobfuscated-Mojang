/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

public class EntityEquipmentPredicate {
    public static final EntityEquipmentPredicate ANY = new EntityEquipmentPredicate(ItemPredicate.ANY, ItemPredicate.ANY, ItemPredicate.ANY, ItemPredicate.ANY, ItemPredicate.ANY, ItemPredicate.ANY);
    public static final EntityEquipmentPredicate CAPTAIN = new EntityEquipmentPredicate(ItemPredicate.Builder.item().of(Items.WHITE_BANNER).hasNbt(Raid.getLeaderBannerInstance().getTag()).build(), ItemPredicate.ANY, ItemPredicate.ANY, ItemPredicate.ANY, ItemPredicate.ANY, ItemPredicate.ANY);
    private final ItemPredicate head;
    private final ItemPredicate chest;
    private final ItemPredicate legs;
    private final ItemPredicate feet;
    private final ItemPredicate mainhand;
    private final ItemPredicate offhand;

    public EntityEquipmentPredicate(ItemPredicate itemPredicate, ItemPredicate itemPredicate2, ItemPredicate itemPredicate3, ItemPredicate itemPredicate4, ItemPredicate itemPredicate5, ItemPredicate itemPredicate6) {
        this.head = itemPredicate;
        this.chest = itemPredicate2;
        this.legs = itemPredicate3;
        this.feet = itemPredicate4;
        this.mainhand = itemPredicate5;
        this.offhand = itemPredicate6;
    }

    public boolean matches(@Nullable Entity entity) {
        if (this == ANY) {
            return true;
        }
        if (!(entity instanceof LivingEntity)) {
            return false;
        }
        LivingEntity livingEntity = (LivingEntity)entity;
        if (!this.head.matches(livingEntity.getItemBySlot(EquipmentSlot.HEAD))) {
            return false;
        }
        if (!this.chest.matches(livingEntity.getItemBySlot(EquipmentSlot.CHEST))) {
            return false;
        }
        if (!this.legs.matches(livingEntity.getItemBySlot(EquipmentSlot.LEGS))) {
            return false;
        }
        if (!this.feet.matches(livingEntity.getItemBySlot(EquipmentSlot.FEET))) {
            return false;
        }
        if (!this.mainhand.matches(livingEntity.getItemBySlot(EquipmentSlot.MAINHAND))) {
            return false;
        }
        return this.offhand.matches(livingEntity.getItemBySlot(EquipmentSlot.OFFHAND));
    }

    public static EntityEquipmentPredicate fromJson(@Nullable JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return ANY;
        }
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "equipment");
        ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("head"));
        ItemPredicate itemPredicate2 = ItemPredicate.fromJson(jsonObject.get("chest"));
        ItemPredicate itemPredicate3 = ItemPredicate.fromJson(jsonObject.get("legs"));
        ItemPredicate itemPredicate4 = ItemPredicate.fromJson(jsonObject.get("feet"));
        ItemPredicate itemPredicate5 = ItemPredicate.fromJson(jsonObject.get("mainhand"));
        ItemPredicate itemPredicate6 = ItemPredicate.fromJson(jsonObject.get("offhand"));
        return new EntityEquipmentPredicate(itemPredicate, itemPredicate2, itemPredicate3, itemPredicate4, itemPredicate5, itemPredicate6);
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("head", this.head.serializeToJson());
        jsonObject.add("chest", this.chest.serializeToJson());
        jsonObject.add("legs", this.legs.serializeToJson());
        jsonObject.add("feet", this.feet.serializeToJson());
        jsonObject.add("mainhand", this.mainhand.serializeToJson());
        jsonObject.add("offhand", this.offhand.serializeToJson());
        return jsonObject;
    }

    public static class Builder {
        private ItemPredicate head = ItemPredicate.ANY;
        private ItemPredicate chest = ItemPredicate.ANY;
        private ItemPredicate legs = ItemPredicate.ANY;
        private ItemPredicate feet = ItemPredicate.ANY;
        private ItemPredicate mainhand = ItemPredicate.ANY;
        private ItemPredicate offhand = ItemPredicate.ANY;

        public static Builder equipment() {
            return new Builder();
        }

        public Builder head(ItemPredicate itemPredicate) {
            this.head = itemPredicate;
            return this;
        }

        public Builder chest(ItemPredicate itemPredicate) {
            this.chest = itemPredicate;
            return this;
        }

        public Builder legs(ItemPredicate itemPredicate) {
            this.legs = itemPredicate;
            return this;
        }

        public Builder feet(ItemPredicate itemPredicate) {
            this.feet = itemPredicate;
            return this;
        }

        public Builder mainhand(ItemPredicate itemPredicate) {
            this.mainhand = itemPredicate;
            return this;
        }

        public Builder offhand(ItemPredicate itemPredicate) {
            this.offhand = itemPredicate;
            return this;
        }

        public EntityEquipmentPredicate build() {
            return new EntityEquipmentPredicate(this.head, this.chest, this.legs, this.feet, this.mainhand, this.offhand);
        }
    }
}

