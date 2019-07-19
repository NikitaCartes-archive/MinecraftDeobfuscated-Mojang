package net.minecraft.world.phys.shapes;

public interface BooleanOp {
	BooleanOp FALSE = (bl, bl2) -> false;
	BooleanOp NOT_OR = (bl, bl2) -> !bl && !bl2;
	BooleanOp ONLY_SECOND = (bl, bl2) -> bl2 && !bl;
	BooleanOp NOT_FIRST = (bl, bl2) -> !bl;
	BooleanOp ONLY_FIRST = (bl, bl2) -> bl && !bl2;
	BooleanOp NOT_SECOND = (bl, bl2) -> !bl2;
	BooleanOp NOT_SAME = (bl, bl2) -> bl != bl2;
	BooleanOp NOT_AND = (bl, bl2) -> !bl || !bl2;
	BooleanOp AND = (bl, bl2) -> bl && bl2;
	BooleanOp SAME = (bl, bl2) -> bl == bl2;
	BooleanOp SECOND = (bl, bl2) -> bl2;
	BooleanOp CAUSES = (bl, bl2) -> !bl || bl2;
	BooleanOp FIRST = (bl, bl2) -> bl;
	BooleanOp CAUSED_BY = (bl, bl2) -> bl || !bl2;
	BooleanOp OR = (bl, bl2) -> bl || bl2;
	BooleanOp TRUE = (bl, bl2) -> true;

	boolean apply(boolean bl, boolean bl2);
}
