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
import net.minecraft.commands.arguments.ResourceOrIdArgument;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.commands.arguments.ResourceOrTagKeyArgument;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.commands.arguments.ScoreboardSlotArgument;
import net.minecraft.commands.arguments.SlotArgument;
import net.minecraft.commands.arguments.SlotsArgument;
import net.minecraft.commands.arguments.StyleArgument;
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
import net.minecraft.commands.synchronization.brigadier.DoubleArgumentInfo;
import net.minecraft.commands.synchronization.brigadier.FloatArgumentInfo;
import net.minecraft.commands.synchronization.brigadier.IntegerArgumentInfo;
import net.minecraft.commands.synchronization.brigadier.LongArgumentInfo;
import net.minecraft.commands.synchronization.brigadier.StringArgumentSerializer;
import net.minecraft.core.Registry;
import net.minecraft.gametest.framework.TestClassNameArgument;
import net.minecraft.gametest.framework.TestFunctionArgument;

public class ArgumentTypeInfos {
	private static final Map<Class<?>, ArgumentTypeInfo<?, ?>> BY_CLASS = Maps.<Class<?>, ArgumentTypeInfo<?, ?>>newHashMap();

	private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> ArgumentTypeInfo<A, T> register(
		Registry<ArgumentTypeInfo<?, ?>> registry, String string, Class<? extends A> class_, ArgumentTypeInfo<A, T> argumentTypeInfo
	) {
		BY_CLASS.put(class_, argumentTypeInfo);
		return Registry.register(registry, string, argumentTypeInfo);
	}

	public static ArgumentTypeInfo<?, ?> bootstrap(Registry<ArgumentTypeInfo<?, ?>> registry) {
		register(registry, "brigadier:bool", BoolArgumentType.class, SingletonArgumentInfo.contextFree(BoolArgumentType::bool));
		register(registry, "brigadier:float", FloatArgumentType.class, new FloatArgumentInfo());
		register(registry, "brigadier:double", DoubleArgumentType.class, new DoubleArgumentInfo());
		register(registry, "brigadier:integer", IntegerArgumentType.class, new IntegerArgumentInfo());
		register(registry, "brigadier:long", LongArgumentType.class, new LongArgumentInfo());
		register(registry, "brigadier:string", StringArgumentType.class, new StringArgumentSerializer());
		register(registry, "entity", EntityArgument.class, new EntityArgument.Info());
		register(registry, "game_profile", GameProfileArgument.class, SingletonArgumentInfo.contextFree(GameProfileArgument::gameProfile));
		register(registry, "block_pos", BlockPosArgument.class, SingletonArgumentInfo.contextFree(BlockPosArgument::blockPos));
		register(registry, "column_pos", ColumnPosArgument.class, SingletonArgumentInfo.contextFree(ColumnPosArgument::columnPos));
		register(registry, "vec3", Vec3Argument.class, SingletonArgumentInfo.contextFree(Vec3Argument::vec3));
		register(registry, "vec2", Vec2Argument.class, SingletonArgumentInfo.contextFree(Vec2Argument::vec2));
		register(registry, "block_state", BlockStateArgument.class, SingletonArgumentInfo.contextAware(BlockStateArgument::block));
		register(registry, "block_predicate", BlockPredicateArgument.class, SingletonArgumentInfo.contextAware(BlockPredicateArgument::blockPredicate));
		register(registry, "item_stack", ItemArgument.class, SingletonArgumentInfo.contextAware(ItemArgument::item));
		register(registry, "item_predicate", ItemPredicateArgument.class, SingletonArgumentInfo.contextAware(ItemPredicateArgument::itemPredicate));
		register(registry, "color", ColorArgument.class, SingletonArgumentInfo.contextFree(ColorArgument::color));
		register(registry, "component", ComponentArgument.class, SingletonArgumentInfo.contextAware(ComponentArgument::textComponent));
		register(registry, "style", StyleArgument.class, SingletonArgumentInfo.contextAware(StyleArgument::style));
		register(registry, "message", MessageArgument.class, SingletonArgumentInfo.contextFree(MessageArgument::message));
		register(registry, "nbt_compound_tag", CompoundTagArgument.class, SingletonArgumentInfo.contextFree(CompoundTagArgument::compoundTag));
		register(registry, "nbt_tag", NbtTagArgument.class, SingletonArgumentInfo.contextFree(NbtTagArgument::nbtTag));
		register(registry, "nbt_path", NbtPathArgument.class, SingletonArgumentInfo.contextFree(NbtPathArgument::nbtPath));
		register(registry, "objective", ObjectiveArgument.class, SingletonArgumentInfo.contextFree(ObjectiveArgument::objective));
		register(registry, "objective_criteria", ObjectiveCriteriaArgument.class, SingletonArgumentInfo.contextFree(ObjectiveCriteriaArgument::criteria));
		register(registry, "operation", OperationArgument.class, SingletonArgumentInfo.contextFree(OperationArgument::operation));
		register(registry, "particle", ParticleArgument.class, SingletonArgumentInfo.contextAware(ParticleArgument::particle));
		register(registry, "angle", AngleArgument.class, SingletonArgumentInfo.contextFree(AngleArgument::angle));
		register(registry, "rotation", RotationArgument.class, SingletonArgumentInfo.contextFree(RotationArgument::rotation));
		register(registry, "scoreboard_slot", ScoreboardSlotArgument.class, SingletonArgumentInfo.contextFree(ScoreboardSlotArgument::displaySlot));
		register(registry, "score_holder", ScoreHolderArgument.class, new ScoreHolderArgument.Info());
		register(registry, "swizzle", SwizzleArgument.class, SingletonArgumentInfo.contextFree(SwizzleArgument::swizzle));
		register(registry, "team", TeamArgument.class, SingletonArgumentInfo.contextFree(TeamArgument::team));
		register(registry, "item_slot", SlotArgument.class, SingletonArgumentInfo.contextFree(SlotArgument::slot));
		register(registry, "item_slots", SlotsArgument.class, SingletonArgumentInfo.contextFree(SlotsArgument::slots));
		register(registry, "resource_location", ResourceLocationArgument.class, SingletonArgumentInfo.contextFree(ResourceLocationArgument::id));
		register(registry, "function", FunctionArgument.class, SingletonArgumentInfo.contextFree(FunctionArgument::functions));
		register(registry, "entity_anchor", EntityAnchorArgument.class, SingletonArgumentInfo.contextFree(EntityAnchorArgument::anchor));
		register(registry, "int_range", RangeArgument.Ints.class, SingletonArgumentInfo.contextFree(RangeArgument::intRange));
		register(registry, "float_range", RangeArgument.Floats.class, SingletonArgumentInfo.contextFree(RangeArgument::floatRange));
		register(registry, "dimension", DimensionArgument.class, SingletonArgumentInfo.contextFree(DimensionArgument::dimension));
		register(registry, "gamemode", GameModeArgument.class, SingletonArgumentInfo.contextFree(GameModeArgument::gameMode));
		register(registry, "time", TimeArgument.class, new TimeArgument.Info());
		register(registry, "resource_or_tag", fixClassType(ResourceOrTagArgument.class), new ResourceOrTagArgument.Info());
		register(registry, "resource_or_tag_key", fixClassType(ResourceOrTagKeyArgument.class), new ResourceOrTagKeyArgument.Info());
		register(registry, "resource", fixClassType(ResourceArgument.class), new ResourceArgument.Info());
		register(registry, "resource_key", fixClassType(ResourceKeyArgument.class), new ResourceKeyArgument.Info());
		register(registry, "template_mirror", TemplateMirrorArgument.class, SingletonArgumentInfo.contextFree(TemplateMirrorArgument::templateMirror));
		register(registry, "template_rotation", TemplateRotationArgument.class, SingletonArgumentInfo.contextFree(TemplateRotationArgument::templateRotation));
		register(registry, "heightmap", HeightmapTypeArgument.class, SingletonArgumentInfo.contextFree(HeightmapTypeArgument::heightmap));
		register(registry, "loot_table", ResourceOrIdArgument.LootTableArgument.class, SingletonArgumentInfo.contextAware(ResourceOrIdArgument::lootTable));
		register(
			registry, "loot_predicate", ResourceOrIdArgument.LootPredicateArgument.class, SingletonArgumentInfo.contextAware(ResourceOrIdArgument::lootPredicate)
		);
		register(registry, "loot_modifier", ResourceOrIdArgument.LootModifierArgument.class, SingletonArgumentInfo.contextAware(ResourceOrIdArgument::lootModifier));
		if (SharedConstants.IS_RUNNING_IN_IDE) {
			register(registry, "test_argument", TestFunctionArgument.class, SingletonArgumentInfo.contextFree(TestFunctionArgument::testFunctionArgument));
			register(registry, "test_class", TestClassNameArgument.class, SingletonArgumentInfo.contextFree(TestClassNameArgument::testClassName));
		}

		return register(registry, "uuid", UuidArgument.class, SingletonArgumentInfo.contextFree(UuidArgument::uuid));
	}

	private static <T extends ArgumentType<?>> Class<T> fixClassType(Class<? super T> class_) {
		return (Class<T>)class_;
	}

	public static boolean isClassRecognized(Class<?> class_) {
		return BY_CLASS.containsKey(class_);
	}

	public static <A extends ArgumentType<?>> ArgumentTypeInfo<A, ?> byClass(A argumentType) {
		ArgumentTypeInfo<?, ?> argumentTypeInfo = (ArgumentTypeInfo<?, ?>)BY_CLASS.get(argumentType.getClass());
		if (argumentTypeInfo == null) {
			throw new IllegalArgumentException(String.format(Locale.ROOT, "Unrecognized argument type %s (%s)", argumentType, argumentType.getClass()));
		} else {
			return (ArgumentTypeInfo<A, ?>)argumentTypeInfo;
		}
	}

	public static <A extends ArgumentType<?>> ArgumentTypeInfo.Template<A> unpack(A argumentType) {
		return byClass(argumentType).unpack(argumentType);
	}
}
