/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class NbtPredicate {
    public static final NbtPredicate ANY = new NbtPredicate(null);
    @Nullable
    private final CompoundTag tag;

    public NbtPredicate(@Nullable CompoundTag compoundTag) {
        this.tag = compoundTag;
    }

    public boolean matches(ItemStack itemStack) {
        if (this == ANY) {
            return true;
        }
        return this.matches(itemStack.getTag());
    }

    public boolean matches(Entity entity) {
        if (this == ANY) {
            return true;
        }
        return this.matches(NbtPredicate.getEntityTagToCompare(entity));
    }

    public boolean matches(@Nullable Tag tag) {
        if (tag == null) {
            return this == ANY;
        }
        return this.tag == null || NbtUtils.compareNbt(this.tag, tag, true);
    }

    public JsonElement serializeToJson() {
        if (this == ANY || this.tag == null) {
            return JsonNull.INSTANCE;
        }
        return new JsonPrimitive(this.tag.toString());
    }

    public static NbtPredicate fromJson(@Nullable JsonElement jsonElement) {
        CompoundTag compoundTag;
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return ANY;
        }
        try {
            compoundTag = TagParser.parseTag(GsonHelper.convertToString(jsonElement, "nbt"));
        } catch (CommandSyntaxException commandSyntaxException) {
            throw new JsonSyntaxException("Invalid nbt tag: " + commandSyntaxException.getMessage());
        }
        return new NbtPredicate(compoundTag);
    }

    public static CompoundTag getEntityTagToCompare(Entity entity) {
        ItemStack itemStack;
        CompoundTag compoundTag = entity.saveWithoutId(new CompoundTag());
        if (entity instanceof Player && !(itemStack = ((Player)entity).inventory.getSelected()).isEmpty()) {
            compoundTag.put("SelectedItem", itemStack.save(new CompoundTag()));
        }
        return compoundTag;
    }
}

