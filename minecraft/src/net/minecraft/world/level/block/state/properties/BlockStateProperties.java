package net.minecraft.world.level.block.state.properties;

import java.util.function.Predicate;
import net.minecraft.core.Direction;

public class BlockStateProperties {
	public static final BooleanProperty ATTACHED = BooleanProperty.create("attached");
	public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");
	public static final BooleanProperty CONDITIONAL = BooleanProperty.create("conditional");
	public static final BooleanProperty DISARMED = BooleanProperty.create("disarmed");
	public static final BooleanProperty DRAG = BooleanProperty.create("drag");
	public static final BooleanProperty ENABLED = BooleanProperty.create("enabled");
	public static final BooleanProperty EXTENDED = BooleanProperty.create("extended");
	public static final BooleanProperty EYE = BooleanProperty.create("eye");
	public static final BooleanProperty FALLING = BooleanProperty.create("falling");
	public static final BooleanProperty HANGING = BooleanProperty.create("hanging");
	public static final BooleanProperty HAS_BOTTLE_0 = BooleanProperty.create("has_bottle_0");
	public static final BooleanProperty HAS_BOTTLE_1 = BooleanProperty.create("has_bottle_1");
	public static final BooleanProperty HAS_BOTTLE_2 = BooleanProperty.create("has_bottle_2");
	public static final BooleanProperty HAS_RECORD = BooleanProperty.create("has_record");
	public static final BooleanProperty HAS_BOOK = BooleanProperty.create("has_book");
	public static final BooleanProperty INVERTED = BooleanProperty.create("inverted");
	public static final BooleanProperty IN_WALL = BooleanProperty.create("in_wall");
	public static final BooleanProperty LIT = BooleanProperty.create("lit");
	public static final BooleanProperty LOCKED = BooleanProperty.create("locked");
	public static final BooleanProperty OCCUPIED = BooleanProperty.create("occupied");
	public static final BooleanProperty OPEN = BooleanProperty.create("open");
	public static final BooleanProperty PERSISTENT = BooleanProperty.create("persistent");
	public static final BooleanProperty POWERED = BooleanProperty.create("powered");
	public static final BooleanProperty SHORT = BooleanProperty.create("short");
	public static final BooleanProperty SIGNAL_FIRE = BooleanProperty.create("signal_fire");
	public static final BooleanProperty SNOWY = BooleanProperty.create("snowy");
	public static final BooleanProperty TRIGGERED = BooleanProperty.create("triggered");
	public static final BooleanProperty UNSTABLE = BooleanProperty.create("unstable");
	public static final BooleanProperty WATERLOGGED = BooleanProperty.create("waterlogged");
	public static final BooleanProperty VINE_END = BooleanProperty.create("vine_end");
	public static final EnumProperty<Direction.Axis> HORIZONTAL_AXIS = EnumProperty.create("axis", Direction.Axis.class, Direction.Axis.X, Direction.Axis.Z);
	public static final EnumProperty<Direction.Axis> AXIS = EnumProperty.create("axis", Direction.Axis.class);
	public static final BooleanProperty UP = BooleanProperty.create("up");
	public static final BooleanProperty DOWN = BooleanProperty.create("down");
	public static final BooleanProperty NORTH = BooleanProperty.create("north");
	public static final BooleanProperty EAST = BooleanProperty.create("east");
	public static final BooleanProperty SOUTH = BooleanProperty.create("south");
	public static final BooleanProperty WEST = BooleanProperty.create("west");
	public static final DirectionProperty FACING = DirectionProperty.create(
		"facing", Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN
	);
	public static final DirectionProperty FACING_HOPPER = DirectionProperty.create("facing", (Predicate<Direction>)(direction -> direction != Direction.UP));
	public static final DirectionProperty HORIZONTAL_FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);
	public static final EnumProperty<AttachFace> ATTACH_FACE = EnumProperty.create("face", AttachFace.class);
	public static final EnumProperty<BellAttachType> BELL_ATTACHMENT = EnumProperty.create("attachment", BellAttachType.class);
	public static final EnumProperty<WallSide> EAST_WALL = EnumProperty.create("east", WallSide.class);
	public static final EnumProperty<WallSide> NORTH_WALL = EnumProperty.create("north", WallSide.class);
	public static final EnumProperty<WallSide> SOUTH_WALL = EnumProperty.create("south", WallSide.class);
	public static final EnumProperty<WallSide> WEST_WALL = EnumProperty.create("west", WallSide.class);
	public static final EnumProperty<RedstoneSide> EAST_REDSTONE = EnumProperty.create("east", RedstoneSide.class);
	public static final EnumProperty<RedstoneSide> NORTH_REDSTONE = EnumProperty.create("north", RedstoneSide.class);
	public static final EnumProperty<RedstoneSide> SOUTH_REDSTONE = EnumProperty.create("south", RedstoneSide.class);
	public static final EnumProperty<RedstoneSide> WEST_REDSTONE = EnumProperty.create("west", RedstoneSide.class);
	public static final EnumProperty<DoubleBlockHalf> DOUBLE_BLOCK_HALF = EnumProperty.create("half", DoubleBlockHalf.class);
	public static final EnumProperty<Half> HALF = EnumProperty.create("half", Half.class);
	public static final EnumProperty<RailShape> RAIL_SHAPE = EnumProperty.create("shape", RailShape.class);
	public static final EnumProperty<RailShape> RAIL_SHAPE_STRAIGHT = EnumProperty.create(
		"shape",
		RailShape.class,
		(Predicate)(railShape -> railShape != RailShape.NORTH_EAST
				&& railShape != RailShape.NORTH_WEST
				&& railShape != RailShape.SOUTH_EAST
				&& railShape != RailShape.SOUTH_WEST)
	);
	public static final IntegerProperty AGE_1 = IntegerProperty.create("age", 0, 1);
	public static final IntegerProperty AGE_2 = IntegerProperty.create("age", 0, 2);
	public static final IntegerProperty AGE_3 = IntegerProperty.create("age", 0, 3);
	public static final IntegerProperty AGE_5 = IntegerProperty.create("age", 0, 5);
	public static final IntegerProperty AGE_7 = IntegerProperty.create("age", 0, 7);
	public static final IntegerProperty AGE_15 = IntegerProperty.create("age", 0, 15);
	public static final IntegerProperty AGE_25 = IntegerProperty.create("age", 0, 25);
	public static final IntegerProperty BITES = IntegerProperty.create("bites", 0, 6);
	public static final IntegerProperty DELAY = IntegerProperty.create("delay", 1, 4);
	public static final IntegerProperty DISTANCE = IntegerProperty.create("distance", 1, 7);
	public static final IntegerProperty EGGS = IntegerProperty.create("eggs", 1, 4);
	public static final IntegerProperty HATCH = IntegerProperty.create("hatch", 0, 2);
	public static final IntegerProperty LAYERS = IntegerProperty.create("layers", 1, 8);
	public static final IntegerProperty LEVEL_CAULDRON = IntegerProperty.create("level", 0, 3);
	public static final IntegerProperty LEVEL_COMPOSTER = IntegerProperty.create("level", 0, 8);
	public static final IntegerProperty LEVEL_FLOWING = IntegerProperty.create("level", 1, 8);
	public static final IntegerProperty LEVEL_HONEY = IntegerProperty.create("honey_level", 0, 5);
	public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 15);
	public static final IntegerProperty MOISTURE = IntegerProperty.create("moisture", 0, 7);
	public static final IntegerProperty NOTE = IntegerProperty.create("note", 0, 24);
	public static final IntegerProperty PICKLES = IntegerProperty.create("pickles", 1, 4);
	public static final IntegerProperty POWER = IntegerProperty.create("power", 0, 15);
	public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 1);
	public static final IntegerProperty STABILITY_DISTANCE = IntegerProperty.create("distance", 0, 7);
	public static final IntegerProperty RESPAWN_ANCHOR_CHARGES = IntegerProperty.create("charges", 0, 4);
	public static final IntegerProperty ROTATION_16 = IntegerProperty.create("rotation", 0, 15);
	public static final EnumProperty<BedPart> BED_PART = EnumProperty.create("part", BedPart.class);
	public static final EnumProperty<ChestType> CHEST_TYPE = EnumProperty.create("type", ChestType.class);
	public static final EnumProperty<ComparatorMode> MODE_COMPARATOR = EnumProperty.create("mode", ComparatorMode.class);
	public static final EnumProperty<DoorHingeSide> DOOR_HINGE = EnumProperty.create("hinge", DoorHingeSide.class);
	public static final EnumProperty<NoteBlockInstrument> NOTEBLOCK_INSTRUMENT = EnumProperty.create("instrument", NoteBlockInstrument.class);
	public static final EnumProperty<PistonType> PISTON_TYPE = EnumProperty.create("type", PistonType.class);
	public static final EnumProperty<SlabType> SLAB_TYPE = EnumProperty.create("type", SlabType.class);
	public static final EnumProperty<StairsShape> STAIRS_SHAPE = EnumProperty.create("shape", StairsShape.class);
	public static final EnumProperty<StructureMode> STRUCTUREBLOCK_MODE = EnumProperty.create("mode", StructureMode.class);
	public static final EnumProperty<BambooLeaves> BAMBOO_LEAVES = EnumProperty.create("leaves", BambooLeaves.class);
}
