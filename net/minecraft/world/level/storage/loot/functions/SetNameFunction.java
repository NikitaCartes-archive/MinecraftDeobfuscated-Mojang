/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.Set;
import java.util.function.UnaryOperator;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class SetNameFunction
extends LootItemConditionalFunction {
    private static final Logger LOGGER = LogUtils.getLogger();
    final Component name;
    @Nullable
    final LootContext.EntityTarget resolutionContext;

    SetNameFunction(LootItemCondition[] lootItemConditions, @Nullable Component component, @Nullable LootContext.EntityTarget entityTarget) {
        super(lootItemConditions);
        this.name = component;
        this.resolutionContext = entityTarget;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_NAME;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.resolutionContext != null ? ImmutableSet.of(this.resolutionContext.getParam()) : ImmutableSet.of();
    }

    public static UnaryOperator<Component> createResolver(LootContext lootContext, @Nullable LootContext.EntityTarget entityTarget) {
        Entity entity;
        if (entityTarget != null && (entity = lootContext.getParamOrNull(entityTarget.getParam())) != null) {
            CommandSourceStack commandSourceStack = entity.createCommandSourceStack().withPermission(2);
            return component -> {
                try {
                    return ComponentUtils.updateForEntity(commandSourceStack, component, entity, 0);
                } catch (CommandSyntaxException commandSyntaxException) {
                    LOGGER.warn("Failed to resolve text component", commandSyntaxException);
                    return component;
                }
            };
        }
        return component -> component;
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        if (this.name != null) {
            itemStack.setHoverName((Component)SetNameFunction.createResolver(lootContext, this.resolutionContext).apply(this.name));
        }
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> setName(Component component) {
        return SetNameFunction.simpleBuilder(lootItemConditions -> new SetNameFunction((LootItemCondition[])lootItemConditions, component, null));
    }

    public static LootItemConditionalFunction.Builder<?> setName(Component component, LootContext.EntityTarget entityTarget) {
        return SetNameFunction.simpleBuilder(lootItemConditions -> new SetNameFunction((LootItemCondition[])lootItemConditions, component, entityTarget));
    }

    public static class Serializer
    extends LootItemConditionalFunction.Serializer<SetNameFunction> {
        @Override
        public void serialize(JsonObject jsonObject, SetNameFunction setNameFunction, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject, setNameFunction, jsonSerializationContext);
            if (setNameFunction.name != null) {
                jsonObject.add("name", Component.Serializer.toJsonTree(setNameFunction.name));
            }
            if (setNameFunction.resolutionContext != null) {
                jsonObject.add("entity", jsonSerializationContext.serialize((Object)setNameFunction.resolutionContext));
            }
        }

        @Override
        public SetNameFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
            MutableComponent component = Component.Serializer.fromJson(jsonObject.get("name"));
            LootContext.EntityTarget entityTarget = GsonHelper.getAsObject(jsonObject, "entity", null, jsonDeserializationContext, LootContext.EntityTarget.class);
            return new SetNameFunction(lootItemConditions, component, entityTarget);
        }

        @Override
        public /* synthetic */ LootItemConditionalFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
            return this.deserialize(jsonObject, jsonDeserializationContext, lootItemConditions);
        }
    }
}

