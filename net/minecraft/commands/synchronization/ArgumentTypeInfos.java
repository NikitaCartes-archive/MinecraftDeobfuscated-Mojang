/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.synchronization;

import com.google.common.collect.Maps;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.Locale;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraft.commands.arguments.AngleArgument;
import net.minecraft.commands.arguments.ColorArgument;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameModeArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.HeightmapTypeArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.commands.arguments.ObjectiveCriteriaArgument;
import net.minecraft.commands.arguments.OperationArgument;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.commands.arguments.RangeArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.commands.arguments.ResourceOrTagKeyArgument;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.commands.arguments.ScoreboardSlotArgument;
import net.minecraft.commands.arguments.SlotArgument;
import net.minecraft.commands.arguments.TeamArgument;
import net.minecraft.commands.arguments.TemplateMirrorArgument;
import net.minecraft.commands.arguments.TemplateRotationArgument;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.SwizzleArgument;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemPredicateArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.commands.synchronization.brigadier.DoubleArgumentInfo;
import net.minecraft.commands.synchronization.brigadier.FloatArgumentInfo;
import net.minecraft.commands.synchronization.brigadier.IntegerArgumentInfo;
import net.minecraft.commands.synchronization.brigadier.LongArgumentInfo;
import net.minecraft.commands.synchronization.brigadier.StringArgumentSerializer;
import net.minecraft.core.Registry;
import net.minecraft.gametest.framework.TestClassNameArgument;
import net.minecraft.gametest.framework.TestFunctionArgument;

public class ArgumentTypeInfos {
    private static final Map<Class<?>, ArgumentTypeInfo<?, ?>> BY_CLASS = Maps.newHashMap();

    private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> ArgumentTypeInfo<A, T> register(Registry<ArgumentTypeInfo<?, ?>> registry, String string, Class<? extends A> class_, ArgumentTypeInfo<A, T> argumentTypeInfo) {
        BY_CLASS.put(class_, argumentTypeInfo);
        return Registry.register(registry, string, argumentTypeInfo);
    }

    public static ArgumentTypeInfo<?, ?> bootstrap(Registry<ArgumentTypeInfo<?, ?>> registry) {
        ArgumentTypeInfos.register(registry, "brigadier:bool", BoolArgumentType.class, SingletonArgumentInfo.contextFree(BoolArgumentType::bool));
        ArgumentTypeInfos.register(registry, "brigadier:float", FloatArgumentType.class, new FloatArgumentInfo());
        ArgumentTypeInfos.register(registry, "brigadier:double", DoubleArgumentType.class, new DoubleArgumentInfo());
        ArgumentTypeInfos.register(registry, "brigadier:integer", IntegerArgumentType.class, new IntegerArgumentInfo());
        ArgumentTypeInfos.register(registry, "brigadier:long", LongArgumentType.class, new LongArgumentInfo());
        ArgumentTypeInfos.register(registry, "brigadier:string", StringArgumentType.class, new StringArgumentSerializer());
        ArgumentTypeInfos.register(registry, "entity", EntityArgument.class, new EntityArgument.Info());
        ArgumentTypeInfos.register(registry, "game_profile", GameProfileArgument.class, SingletonArgumentInfo.contextFree(GameProfileArgument::gameProfile));
        ArgumentTypeInfos.register(registry, "block_pos", BlockPosArgument.class, SingletonArgumentInfo.contextFree(BlockPosArgument::blockPos));
        ArgumentTypeInfos.register(registry, "column_pos", ColumnPosArgument.class, SingletonArgumentInfo.contextFree(ColumnPosArgument::columnPos));
        ArgumentTypeInfos.register(registry, "vec3", Vec3Argument.class, SingletonArgumentInfo.contextFree(Vec3Argument::vec3));
        ArgumentTypeInfos.register(registry, "vec2", Vec2Argument.class, SingletonArgumentInfo.contextFree(Vec2Argument::vec2));
        ArgumentTypeInfos.register(registry, "block_state", BlockStateArgument.class, SingletonArgumentInfo.contextAware(BlockStateArgument::block));
        ArgumentTypeInfos.register(registry, "block_predicate", BlockPredicateArgument.class, SingletonArgumentInfo.contextAware(BlockPredicateArgument::blockPredicate));
        ArgumentTypeInfos.register(registry, "item_stack", ItemArgument.class, SingletonArgumentInfo.contextAware(ItemArgument::item));
        ArgumentTypeInfos.register(registry, "item_predicate", ItemPredicateArgument.class, SingletonArgumentInfo.contextAware(ItemPredicateArgument::itemPredicate));
        ArgumentTypeInfos.register(registry, "color", ColorArgument.class, SingletonArgumentInfo.contextFree(ColorArgument::color));
        ArgumentTypeInfos.register(registry, "component", ComponentArgument.class, SingletonArgumentInfo.contextFree(ComponentArgument::textComponent));
        ArgumentTypeInfos.register(registry, "message", MessageArgument.class, SingletonArgumentInfo.contextFree(MessageArgument::message));
        ArgumentTypeInfos.register(registry, "nbt_compound_tag", CompoundTagArgument.class, SingletonArgumentInfo.contextFree(CompoundTagArgument::compoundTag));
        ArgumentTypeInfos.register(registry, "nbt_tag", NbtTagArgument.class, SingletonArgumentInfo.contextFree(NbtTagArgument::nbtTag));
        ArgumentTypeInfos.register(registry, "nbt_path", NbtPathArgument.class, SingletonArgumentInfo.contextFree(NbtPathArgument::nbtPath));
        ArgumentTypeInfos.register(registry, "objective", ObjectiveArgument.class, SingletonArgumentInfo.contextFree(ObjectiveArgument::objective));
        ArgumentTypeInfos.register(registry, "objective_criteria", ObjectiveCriteriaArgument.class, SingletonArgumentInfo.contextFree(ObjectiveCriteriaArgument::criteria));
        ArgumentTypeInfos.register(registry, "operation", OperationArgument.class, SingletonArgumentInfo.contextFree(OperationArgument::operation));
        ArgumentTypeInfos.register(registry, "particle", ParticleArgument.class, SingletonArgumentInfo.contextAware(ParticleArgument::particle));
        ArgumentTypeInfos.register(registry, "angle", AngleArgument.class, SingletonArgumentInfo.contextFree(AngleArgument::angle));
        ArgumentTypeInfos.register(registry, "rotation", RotationArgument.class, SingletonArgumentInfo.contextFree(RotationArgument::rotation));
        ArgumentTypeInfos.register(registry, "scoreboard_slot", ScoreboardSlotArgument.class, SingletonArgumentInfo.contextFree(ScoreboardSlotArgument::displaySlot));
        ArgumentTypeInfos.register(registry, "score_holder", ScoreHolderArgument.class, new ScoreHolderArgument.Info());
        ArgumentTypeInfos.register(registry, "swizzle", SwizzleArgument.class, SingletonArgumentInfo.contextFree(SwizzleArgument::swizzle));
        ArgumentTypeInfos.register(registry, "team", TeamArgument.class, SingletonArgumentInfo.contextFree(TeamArgument::team));
        ArgumentTypeInfos.register(registry, "item_slot", SlotArgument.class, SingletonArgumentInfo.contextFree(SlotArgument::slot));
        ArgumentTypeInfos.register(registry, "resource_location", ResourceLocationArgument.class, SingletonArgumentInfo.contextFree(ResourceLocationArgument::id));
        ArgumentTypeInfos.register(registry, "function", FunctionArgument.class, SingletonArgumentInfo.contextFree(FunctionArgument::functions));
        ArgumentTypeInfos.register(registry, "entity_anchor", EntityAnchorArgument.class, SingletonArgumentInfo.contextFree(EntityAnchorArgument::anchor));
        ArgumentTypeInfos.register(registry, "int_range", RangeArgument.Ints.class, SingletonArgumentInfo.contextFree(RangeArgument::intRange));
        ArgumentTypeInfos.register(registry, "float_range", RangeArgument.Floats.class, SingletonArgumentInfo.contextFree(RangeArgument::floatRange));
        ArgumentTypeInfos.register(registry, "dimension", DimensionArgument.class, SingletonArgumentInfo.contextFree(DimensionArgument::dimension));
        ArgumentTypeInfos.register(registry, "gamemode", GameModeArgument.class, SingletonArgumentInfo.contextFree(GameModeArgument::gameMode));
        ArgumentTypeInfos.register(registry, "time", TimeArgument.class, new TimeArgument.Info());
        ArgumentTypeInfos.register(registry, "resource_or_tag", ArgumentTypeInfos.fixClassType(ResourceOrTagArgument.class), new ResourceOrTagArgument.Info());
        ArgumentTypeInfos.register(registry, "resource_or_tag_key", ArgumentTypeInfos.fixClassType(ResourceOrTagKeyArgument.class), new ResourceOrTagKeyArgument.Info());
        ArgumentTypeInfos.register(registry, "resource", ArgumentTypeInfos.fixClassType(ResourceArgument.class), new ResourceArgument.Info());
        ArgumentTypeInfos.register(registry, "resource_key", ArgumentTypeInfos.fixClassType(ResourceKeyArgument.class), new ResourceKeyArgument.Info());
        ArgumentTypeInfos.register(registry, "template_mirror", TemplateMirrorArgument.class, SingletonArgumentInfo.contextFree(TemplateMirrorArgument::templateMirror));
        ArgumentTypeInfos.register(registry, "template_rotation", TemplateRotationArgument.class, SingletonArgumentInfo.contextFree(TemplateRotationArgument::templateRotation));
        ArgumentTypeInfos.register(registry, "heightmap", HeightmapTypeArgument.class, SingletonArgumentInfo.contextFree(HeightmapTypeArgument::heightmap));
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            ArgumentTypeInfos.register(registry, "test_argument", TestFunctionArgument.class, SingletonArgumentInfo.contextFree(TestFunctionArgument::testFunctionArgument));
            ArgumentTypeInfos.register(registry, "test_class", TestClassNameArgument.class, SingletonArgumentInfo.contextFree(TestClassNameArgument::testClassName));
        }
        return ArgumentTypeInfos.register(registry, "uuid", UuidArgument.class, SingletonArgumentInfo.contextFree(UuidArgument::uuid));
    }

    private static <T extends ArgumentType<?>> Class<T> fixClassType(Class<? super T> class_) {
        return class_;
    }

    public static boolean isClassRecognized(Class<?> class_) {
        return BY_CLASS.containsKey(class_);
    }

    public static <A extends ArgumentType<?>> ArgumentTypeInfo<A, ?> byClass(A argumentType) {
        ArgumentTypeInfo<?, ?> argumentTypeInfo = BY_CLASS.get(argumentType.getClass());
        if (argumentTypeInfo == null) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "Unrecognized argument type %s (%s)", argumentType, argumentType.getClass()));
        }
        return argumentTypeInfo;
    }

    public static <A extends ArgumentType<?>> ArgumentTypeInfo.Template<A> unpack(A argumentType) {
        return ArgumentTypeInfos.byClass(argumentType).unpack(argumentType);
    }
}

