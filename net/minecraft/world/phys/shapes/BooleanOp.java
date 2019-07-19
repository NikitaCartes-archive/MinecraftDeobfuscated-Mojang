/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.phys.shapes;

public interface BooleanOp {
    public static final BooleanOp FALSE = (bl, bl2) -> false;
    public static final BooleanOp NOT_OR = (bl, bl2) -> !bl && !bl2;
    public static final BooleanOp ONLY_SECOND = (bl, bl2) -> bl2 && !bl;
    public static final BooleanOp NOT_FIRST = (bl, bl2) -> !bl;
    public static final BooleanOp ONLY_FIRST = (bl, bl2) -> bl && !bl2;
    public static final BooleanOp NOT_SECOND = (bl, bl2) -> !bl2;
    public static final BooleanOp NOT_SAME = (bl, bl2) -> bl != bl2;
    public static final BooleanOp NOT_AND = (bl, bl2) -> !bl || !bl2;
    public static final BooleanOp AND = (bl, bl2) -> bl && bl2;
    public static final BooleanOp SAME = (bl, bl2) -> bl == bl2;
    public static final BooleanOp SECOND = (bl, bl2) -> bl2;
    public static final BooleanOp CAUSES = (bl, bl2) -> !bl || bl2;
    public static final BooleanOp FIRST = (bl, bl2) -> bl;
    public static final BooleanOp CAUSED_BY = (bl, bl2) -> bl || !bl2;
    public static final BooleanOp OR = (bl, bl2) -> bl || bl2;
    public static final BooleanOp TRUE = (bl, bl2) -> true;

    public boolean apply(boolean var1, boolean var2);
}

