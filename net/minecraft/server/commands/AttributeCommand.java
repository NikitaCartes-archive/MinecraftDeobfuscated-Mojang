/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class AttributeCommand {
    private static final SuggestionProvider<CommandSourceStack> AVAILABLE_ATTRIBUTES = (commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggestResource(Registry.ATTRIBUTE.keySet(), suggestionsBuilder);
    private static final DynamicCommandExceptionType ERROR_NOT_LIVING_ENTITY = new DynamicCommandExceptionType(object -> new TranslatableComponent("commands.attribute.failed.entity", object));
    private static final Dynamic2CommandExceptionType ERROR_NO_SUCH_ATTRIBUTE = new Dynamic2CommandExceptionType((object, object2) -> new TranslatableComponent("commands.attribute.failed.no_attribute", object, object2));
    private static final Dynamic3CommandExceptionType ERROR_NO_SUCH_MODIFIER = new Dynamic3CommandExceptionType((object, object2, object3) -> new TranslatableComponent("commands.attribute.failed.no_modifier", object2, object, object3));
    private static final Dynamic3CommandExceptionType ERROR_MODIFIER_ALREADY_PRESENT = new Dynamic3CommandExceptionType((object, object2, object3) -> new TranslatableComponent("commands.attribute.failed.modifier_already_present", object3, object2, object));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("attribute").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(Commands.argument("target", EntityArgument.entity()).then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("attribute", ResourceLocationArgument.id()).suggests(AVAILABLE_ATTRIBUTES).then((ArgumentBuilder<CommandSourceStack, ?>)((LiteralArgumentBuilder)Commands.literal("get").executes(commandContext -> AttributeCommand.getAttributeValue((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity(commandContext, "target"), ResourceLocationArgument.getAttribute(commandContext, "attribute"), 1.0))).then(Commands.argument("scale", DoubleArgumentType.doubleArg()).executes(commandContext -> AttributeCommand.getAttributeValue((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity(commandContext, "target"), ResourceLocationArgument.getAttribute(commandContext, "attribute"), DoubleArgumentType.getDouble(commandContext, "scale")))))).then(((LiteralArgumentBuilder)Commands.literal("base").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("set").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("value", DoubleArgumentType.doubleArg()).executes(commandContext -> AttributeCommand.setAttributeBase((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity(commandContext, "target"), ResourceLocationArgument.getAttribute(commandContext, "attribute"), DoubleArgumentType.getDouble(commandContext, "value")))))).then(((LiteralArgumentBuilder)Commands.literal("get").executes(commandContext -> AttributeCommand.getAttributeBase((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity(commandContext, "target"), ResourceLocationArgument.getAttribute(commandContext, "attribute"), 1.0))).then(Commands.argument("scale", DoubleArgumentType.doubleArg()).executes(commandContext -> AttributeCommand.getAttributeBase((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity(commandContext, "target"), ResourceLocationArgument.getAttribute(commandContext, "attribute"), DoubleArgumentType.getDouble(commandContext, "scale"))))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("modifier").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("add").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("uuid", UuidArgument.uuid()).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("name", StringArgumentType.string()).then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("value", DoubleArgumentType.doubleArg()).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("add").executes(commandContext -> AttributeCommand.addModifier((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity(commandContext, "target"), ResourceLocationArgument.getAttribute(commandContext, "attribute"), UuidArgument.getUuid(commandContext, "uuid"), StringArgumentType.getString(commandContext, "name"), DoubleArgumentType.getDouble(commandContext, "value"), AttributeModifier.Operation.ADDITION)))).then(Commands.literal("multiply").executes(commandContext -> AttributeCommand.addModifier((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity(commandContext, "target"), ResourceLocationArgument.getAttribute(commandContext, "attribute"), UuidArgument.getUuid(commandContext, "uuid"), StringArgumentType.getString(commandContext, "name"), DoubleArgumentType.getDouble(commandContext, "value"), AttributeModifier.Operation.MULTIPLY_TOTAL)))).then(Commands.literal("multiply_base").executes(commandContext -> AttributeCommand.addModifier((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity(commandContext, "target"), ResourceLocationArgument.getAttribute(commandContext, "attribute"), UuidArgument.getUuid(commandContext, "uuid"), StringArgumentType.getString(commandContext, "name"), DoubleArgumentType.getDouble(commandContext, "value"), AttributeModifier.Operation.MULTIPLY_BASE)))))))).then(Commands.literal("remove").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("uuid", UuidArgument.uuid()).executes(commandContext -> AttributeCommand.removeModifier((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity(commandContext, "target"), ResourceLocationArgument.getAttribute(commandContext, "attribute"), UuidArgument.getUuid(commandContext, "uuid")))))).then(Commands.literal("value").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("get").then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("uuid", UuidArgument.uuid()).executes(commandContext -> AttributeCommand.getAttributeModifier((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity(commandContext, "target"), ResourceLocationArgument.getAttribute(commandContext, "attribute"), UuidArgument.getUuid(commandContext, "uuid"), 1.0))).then(Commands.argument("scale", DoubleArgumentType.doubleArg()).executes(commandContext -> AttributeCommand.getAttributeModifier((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity(commandContext, "target"), ResourceLocationArgument.getAttribute(commandContext, "attribute"), UuidArgument.getUuid(commandContext, "uuid"), DoubleArgumentType.getDouble(commandContext, "scale")))))))))));
    }

    private static AttributeInstance getAttributeInstance(Entity entity, Attribute attribute) throws CommandSyntaxException {
        AttributeInstance attributeInstance = AttributeCommand.getLivingEntity(entity).getAttributes().getInstance(attribute);
        if (attributeInstance == null) {
            throw ERROR_NO_SUCH_ATTRIBUTE.create(entity.getName(), new TranslatableComponent(attribute.getDescriptionId()));
        }
        return attributeInstance;
    }

    private static LivingEntity getLivingEntity(Entity entity) throws CommandSyntaxException {
        if (!(entity instanceof LivingEntity)) {
            throw ERROR_NOT_LIVING_ENTITY.create(entity.getName());
        }
        return (LivingEntity)entity;
    }

    private static LivingEntity getEntityWithAttribute(Entity entity, Attribute attribute) throws CommandSyntaxException {
        LivingEntity livingEntity = AttributeCommand.getLivingEntity(entity);
        if (!livingEntity.getAttributes().hasAttribute(attribute)) {
            throw ERROR_NO_SUCH_ATTRIBUTE.create(entity.getName(), new TranslatableComponent(attribute.getDescriptionId()));
        }
        return livingEntity;
    }

    private static int getAttributeValue(CommandSourceStack commandSourceStack, Entity entity, Attribute attribute, double d) throws CommandSyntaxException {
        LivingEntity livingEntity = AttributeCommand.getEntityWithAttribute(entity, attribute);
        double e = livingEntity.getAttributeValue(attribute);
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.attribute.value.get.success", new TranslatableComponent(attribute.getDescriptionId()), entity.getName(), e), false);
        return (int)(e * d);
    }

    private static int getAttributeBase(CommandSourceStack commandSourceStack, Entity entity, Attribute attribute, double d) throws CommandSyntaxException {
        LivingEntity livingEntity = AttributeCommand.getEntityWithAttribute(entity, attribute);
        double e = livingEntity.getAttributeBaseValue(attribute);
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.attribute.base_value.get.success", new TranslatableComponent(attribute.getDescriptionId()), entity.getName(), e), false);
        return (int)(e * d);
    }

    private static int getAttributeModifier(CommandSourceStack commandSourceStack, Entity entity, Attribute attribute, UUID uUID, double d) throws CommandSyntaxException {
        LivingEntity livingEntity = AttributeCommand.getEntityWithAttribute(entity, attribute);
        AttributeMap attributeMap = livingEntity.getAttributes();
        if (!attributeMap.hasModifier(attribute, uUID)) {
            throw ERROR_NO_SUCH_MODIFIER.create(entity.getName(), new TranslatableComponent(attribute.getDescriptionId()), uUID);
        }
        double e = attributeMap.getModifierValue(attribute, uUID);
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.attribute.modifier.value.get.success", uUID, new TranslatableComponent(attribute.getDescriptionId()), entity.getName(), e), false);
        return (int)(e * d);
    }

    private static int setAttributeBase(CommandSourceStack commandSourceStack, Entity entity, Attribute attribute, double d) throws CommandSyntaxException {
        AttributeCommand.getAttributeInstance(entity, attribute).setBaseValue(d);
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.attribute.base_value.set.success", new TranslatableComponent(attribute.getDescriptionId()), entity.getName(), d), false);
        return 1;
    }

    private static int addModifier(CommandSourceStack commandSourceStack, Entity entity, Attribute attribute, UUID uUID, String string, double d, AttributeModifier.Operation operation) throws CommandSyntaxException {
        AttributeModifier attributeModifier;
        AttributeInstance attributeInstance = AttributeCommand.getAttributeInstance(entity, attribute);
        if (attributeInstance.hasModifier(attributeModifier = new AttributeModifier(uUID, string, d, operation))) {
            throw ERROR_MODIFIER_ALREADY_PRESENT.create(entity.getName(), new TranslatableComponent(attribute.getDescriptionId()), uUID);
        }
        attributeInstance.addPermanentModifier(attributeModifier);
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.attribute.modifier.add.success", uUID, new TranslatableComponent(attribute.getDescriptionId()), entity.getName()), false);
        return 1;
    }

    private static int removeModifier(CommandSourceStack commandSourceStack, Entity entity, Attribute attribute, UUID uUID) throws CommandSyntaxException {
        AttributeInstance attributeInstance = AttributeCommand.getAttributeInstance(entity, attribute);
        if (attributeInstance.removePermanentModifier(uUID)) {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.attribute.modifier.remove.success", uUID, new TranslatableComponent(attribute.getDescriptionId()), entity.getName()), false);
            return 1;
        }
        throw ERROR_NO_SUCH_MODIFIER.create(entity.getName(), new TranslatableComponent(attribute.getDescriptionId()), uUID);
    }
}

