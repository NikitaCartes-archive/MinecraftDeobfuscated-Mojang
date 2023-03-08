/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.math.Transformation;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntFunction;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Brightness;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;

public abstract class Display
extends Entity {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final long NO_LAST_UPDATE = -1000L;
    public static final int NO_BRIGHTNESS_OVERRIDE = -1;
    private static final EntityDataAccessor<Long> DATA_INTERPOLATION_START_TICKS_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Integer> DATA_INTERPOLATION_DURATION_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Vector3f> DATA_TRANSLATION_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Vector3f> DATA_SCALE_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Quaternionf> DATA_LEFT_ROTATION_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.QUATERNION);
    private static final EntityDataAccessor<Quaternionf> DATA_RIGHT_ROTATION_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.QUATERNION);
    private static final EntityDataAccessor<Byte> DATA_BILLBOARD_RENDER_CONSTRAINTS_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> DATA_BRIGHTNESS_OVERRIDE_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_VIEW_RANGE_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_SHADOW_RADIUS_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_SHADOW_STRENGTH_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_WIDTH_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_HEIGHT_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_GLOW_COLOR_OVERRIDE_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.INT);
    private static final float INITIAL_SHADOW_RADIUS = 0.0f;
    private static final float INITIAL_SHADOW_STRENGTH = 1.0f;
    private static final int NO_GLOW_COLOR_OVERRIDE = -1;
    public static final String TAG_INTERPOLATION_DURATION = "interpolation_duration";
    public static final String TAG_INTERPOLATION_START = "interpolation_start";
    public static final String TAG_TRANSFORMATION = "transformation";
    public static final String TAG_BILLBOARD = "billboard";
    public static final String TAG_BRIGHTNESS = "brightness";
    public static final String TAG_VIEW_RANGE = "view_range";
    public static final String TAG_SHADOW_RADIUS = "shadow_radius";
    public static final String TAG_SHADOW_STRENGTH = "shadow_strength";
    public static final String TAG_WIDTH = "width";
    public static final String TAG_HEIGHT = "height";
    public static final String TAG_GLOW_COLOR_OVERRIDE = "glow_color_override";
    private final GenericInterpolator<Transformation> transformation = new GenericInterpolator<Transformation>(Transformation.identity()){

        @Override
        protected Transformation interpolate(float f, Transformation transformation, Transformation transformation2) {
            return transformation.slerp(transformation2, f);
        }
    };
    private final FloatInterpolator shadowRadius = new FloatInterpolator(0.0f);
    private final FloatInterpolator shadowStrength = new FloatInterpolator(1.0f);
    private final Quaternionf orientation = new Quaternionf();
    protected final InterpolatorSet interpolators = new InterpolatorSet();
    private long interpolationStartClientTick;
    private float lastProgress;
    private AABB cullingBoundingBox;

    public Display(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.noCulling = true;
        this.cullingBoundingBox = this.getBoundingBox();
        this.interpolators.addEntry(Set.of(DATA_TRANSLATION_ID, DATA_LEFT_ROTATION_ID, DATA_SCALE_ID, DATA_RIGHT_ROTATION_ID), (f, synchedEntityData) -> this.transformation.updateValue(f, Display.createTransformation(synchedEntityData)));
        this.interpolators.addEntry(DATA_SHADOW_STRENGTH_ID, this.shadowStrength);
        this.interpolators.addEntry(DATA_SHADOW_RADIUS_ID, this.shadowRadius);
    }

    @Override
    public void onSyncedDataUpdated(List<SynchedEntityData.DataValue<?>> list) {
        super.onSyncedDataUpdated(list);
        boolean bl = false;
        for (SynchedEntityData.DataValue<?> dataValue : list) {
            bl |= this.interpolators.shouldTriggerUpdate(dataValue.id());
        }
        if (bl) {
            this.interpolators.updateValues(this.lastProgress, this.entityData);
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        super.onSyncedDataUpdated(entityDataAccessor);
        if (DATA_HEIGHT_ID.equals(entityDataAccessor) || DATA_WIDTH_ID.equals(entityDataAccessor)) {
            this.updateCulling();
        }
        if (DATA_INTERPOLATION_START_TICKS_ID.equals(entityDataAccessor)) {
            long l = this.entityData.get(DATA_INTERPOLATION_START_TICKS_ID) - this.level.getGameTime();
            this.interpolationStartClientTick = (long)this.tickCount + l;
        }
    }

    private static Transformation createTransformation(SynchedEntityData synchedEntityData) {
        Vector3f vector3f = synchedEntityData.get(DATA_TRANSLATION_ID);
        Quaternionf quaternionf = synchedEntityData.get(DATA_LEFT_ROTATION_ID);
        Vector3f vector3f2 = synchedEntityData.get(DATA_SCALE_ID);
        Quaternionf quaternionf2 = synchedEntityData.get(DATA_RIGHT_ROTATION_ID);
        return new Transformation(vector3f, quaternionf, vector3f2, quaternionf2);
    }

    @Override
    public void tick() {
        Entity entity = this.getVehicle();
        if (entity != null && entity.isRemoved()) {
            this.stopRiding();
        }
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_INTERPOLATION_START_TICKS_ID, -1000L);
        this.entityData.define(DATA_INTERPOLATION_DURATION_ID, 0);
        this.entityData.define(DATA_TRANSLATION_ID, new Vector3f());
        this.entityData.define(DATA_SCALE_ID, new Vector3f(1.0f, 1.0f, 1.0f));
        this.entityData.define(DATA_RIGHT_ROTATION_ID, new Quaternionf());
        this.entityData.define(DATA_LEFT_ROTATION_ID, new Quaternionf());
        this.entityData.define(DATA_BILLBOARD_RENDER_CONSTRAINTS_ID, BillboardConstraints.FIXED.getId());
        this.entityData.define(DATA_BRIGHTNESS_OVERRIDE_ID, -1);
        this.entityData.define(DATA_VIEW_RANGE_ID, Float.valueOf(1.0f));
        this.entityData.define(DATA_SHADOW_RADIUS_ID, Float.valueOf(0.0f));
        this.entityData.define(DATA_SHADOW_STRENGTH_ID, Float.valueOf(1.0f));
        this.entityData.define(DATA_WIDTH_ID, Float.valueOf(0.0f));
        this.entityData.define(DATA_HEIGHT_ID, Float.valueOf(0.0f));
        this.entityData.define(DATA_GLOW_COLOR_OVERRIDE_ID, -1);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        if (compoundTag.contains(TAG_TRANSFORMATION)) {
            Transformation.EXTENDED_CODEC.decode(NbtOps.INSTANCE, compoundTag.get(TAG_TRANSFORMATION)).resultOrPartial(Util.prefix("Display entity", LOGGER::error)).ifPresent(pair -> this.setTransformation((Transformation)pair.getFirst()));
        }
        if (compoundTag.contains(TAG_INTERPOLATION_DURATION, 99)) {
            int i = compoundTag.getInt(TAG_INTERPOLATION_DURATION);
            this.setInterpolationDuration(i);
        }
        if (compoundTag.contains(TAG_INTERPOLATION_START, 99)) {
            long l = compoundTag.getLong(TAG_INTERPOLATION_START);
            if (l < 0L) {
                long m = -l - 1L;
                this.setInterpolationStartTick(this.level.getGameTime() + m);
            } else {
                this.setInterpolationStartTick(l);
            }
        }
        if (compoundTag.contains(TAG_BILLBOARD, 8)) {
            BillboardConstraints.CODEC.decode(NbtOps.INSTANCE, compoundTag.get(TAG_BILLBOARD)).resultOrPartial(Util.prefix("Display entity", LOGGER::error)).ifPresent(pair -> this.setBillboardConstraints((BillboardConstraints)pair.getFirst()));
        }
        if (compoundTag.contains(TAG_VIEW_RANGE, 99)) {
            this.setViewRange(compoundTag.getFloat(TAG_VIEW_RANGE));
        }
        if (compoundTag.contains(TAG_SHADOW_RADIUS, 99)) {
            this.setShadowRadius(compoundTag.getFloat(TAG_SHADOW_RADIUS));
        }
        if (compoundTag.contains(TAG_SHADOW_STRENGTH, 99)) {
            this.setShadowStrength(compoundTag.getFloat(TAG_SHADOW_STRENGTH));
        }
        if (compoundTag.contains(TAG_WIDTH, 99)) {
            this.setWidth(compoundTag.getFloat(TAG_WIDTH));
        }
        if (compoundTag.contains(TAG_HEIGHT, 99)) {
            this.setHeight(compoundTag.getFloat(TAG_HEIGHT));
        }
        if (compoundTag.contains(TAG_GLOW_COLOR_OVERRIDE, 99)) {
            this.setGlowColorOverride(compoundTag.getInt(TAG_GLOW_COLOR_OVERRIDE));
        }
        if (compoundTag.contains(TAG_BRIGHTNESS, 10)) {
            Brightness.CODEC.decode(NbtOps.INSTANCE, compoundTag.get(TAG_BRIGHTNESS)).resultOrPartial(Util.prefix("Display entity", LOGGER::error)).ifPresent(pair -> this.setBrightnessOverride((Brightness)pair.getFirst()));
        } else {
            this.setBrightnessOverride(null);
        }
    }

    private void setTransformation(Transformation transformation) {
        this.entityData.set(DATA_TRANSLATION_ID, transformation.getTranslation());
        this.entityData.set(DATA_LEFT_ROTATION_ID, transformation.getLeftRotation());
        this.entityData.set(DATA_SCALE_ID, transformation.getScale());
        this.entityData.set(DATA_RIGHT_ROTATION_ID, transformation.getRightRotation());
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        Transformation.EXTENDED_CODEC.encodeStart(NbtOps.INSTANCE, Display.createTransformation(this.entityData)).result().ifPresent(tag -> compoundTag.put(TAG_TRANSFORMATION, (Tag)tag));
        BillboardConstraints.CODEC.encodeStart(NbtOps.INSTANCE, this.getBillboardConstraints()).result().ifPresent(tag -> compoundTag.put(TAG_BILLBOARD, (Tag)tag));
        compoundTag.putInt(TAG_INTERPOLATION_DURATION, this.getInterpolationDuration());
        compoundTag.putFloat(TAG_VIEW_RANGE, this.getViewRange());
        compoundTag.putFloat(TAG_SHADOW_RADIUS, this.getShadowRadius());
        compoundTag.putFloat(TAG_SHADOW_STRENGTH, this.getShadowStrength());
        compoundTag.putFloat(TAG_WIDTH, this.getWidth());
        compoundTag.putFloat(TAG_HEIGHT, this.getHeight());
        compoundTag.putLong(TAG_INTERPOLATION_START, this.getInterpolationStartTick());
        compoundTag.putInt(TAG_GLOW_COLOR_OVERRIDE, this.getGlowColorOverride());
        Brightness brightness = this.getBrightnessOverride();
        if (brightness != null) {
            Brightness.CODEC.encodeStart(NbtOps.INSTANCE, brightness).result().ifPresent(tag -> compoundTag.put(TAG_BRIGHTNESS, (Tag)tag));
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return this.cullingBoundingBox;
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    public Quaternionf orientation() {
        return this.orientation;
    }

    public Transformation transformation(float f) {
        return this.transformation.get(f);
    }

    private void setInterpolationDuration(int i) {
        this.entityData.set(DATA_INTERPOLATION_DURATION_ID, i);
    }

    private int getInterpolationDuration() {
        return this.entityData.get(DATA_INTERPOLATION_DURATION_ID);
    }

    private void setInterpolationStartTick(long l) {
        this.entityData.set(DATA_INTERPOLATION_START_TICKS_ID, l);
    }

    private long getInterpolationStartTick() {
        return this.entityData.get(DATA_INTERPOLATION_START_TICKS_ID);
    }

    private void setBillboardConstraints(BillboardConstraints billboardConstraints) {
        this.entityData.set(DATA_BILLBOARD_RENDER_CONSTRAINTS_ID, billboardConstraints.getId());
    }

    public BillboardConstraints getBillboardConstraints() {
        return BillboardConstraints.BY_ID.apply(this.entityData.get(DATA_BILLBOARD_RENDER_CONSTRAINTS_ID).byteValue());
    }

    private void setBrightnessOverride(@Nullable Brightness brightness) {
        this.entityData.set(DATA_BRIGHTNESS_OVERRIDE_ID, brightness != null ? brightness.pack() : -1);
    }

    @Nullable
    private Brightness getBrightnessOverride() {
        int i = this.entityData.get(DATA_BRIGHTNESS_OVERRIDE_ID);
        return i != -1 ? Brightness.unpack(i) : null;
    }

    public int getPackedBrightnessOverride() {
        return this.entityData.get(DATA_BRIGHTNESS_OVERRIDE_ID);
    }

    private void setViewRange(float f) {
        this.entityData.set(DATA_VIEW_RANGE_ID, Float.valueOf(f));
    }

    private float getViewRange() {
        return this.entityData.get(DATA_VIEW_RANGE_ID).floatValue();
    }

    private void setShadowRadius(float f) {
        this.entityData.set(DATA_SHADOW_RADIUS_ID, Float.valueOf(f));
    }

    private float getShadowRadius() {
        return this.entityData.get(DATA_SHADOW_RADIUS_ID).floatValue();
    }

    public float getShadowRadius(float f) {
        return this.shadowRadius.get(f);
    }

    private void setShadowStrength(float f) {
        this.entityData.set(DATA_SHADOW_STRENGTH_ID, Float.valueOf(f));
    }

    private float getShadowStrength() {
        return this.entityData.get(DATA_SHADOW_STRENGTH_ID).floatValue();
    }

    public float getShadowStrength(float f) {
        return this.shadowStrength.get(f);
    }

    private void setWidth(float f) {
        this.entityData.set(DATA_WIDTH_ID, Float.valueOf(f));
    }

    private float getWidth() {
        return this.entityData.get(DATA_WIDTH_ID).floatValue();
    }

    private void setHeight(float f) {
        this.entityData.set(DATA_HEIGHT_ID, Float.valueOf(f));
    }

    private int getGlowColorOverride() {
        return this.entityData.get(DATA_GLOW_COLOR_OVERRIDE_ID);
    }

    private void setGlowColorOverride(int i) {
        this.entityData.set(DATA_GLOW_COLOR_OVERRIDE_ID, i);
    }

    public float calculateInterpolationProgress(float f) {
        float j;
        int i = this.getInterpolationDuration();
        if (i <= 0) {
            return 1.0f;
        }
        float g = (long)this.tickCount - this.interpolationStartClientTick;
        float h = g + f;
        this.lastProgress = j = Mth.clamp(Mth.inverseLerp(h, 0.0f, i), 0.0f, 1.0f);
        return j;
    }

    private float getHeight() {
        return this.entityData.get(DATA_HEIGHT_ID).floatValue();
    }

    @Override
    public void setPos(double d, double e, double f) {
        super.setPos(d, e, f);
        this.updateCulling();
    }

    private void updateCulling() {
        float f = this.getWidth();
        float g = this.getHeight();
        if (f == 0.0f || g == 0.0f) {
            this.noCulling = true;
        } else {
            this.noCulling = false;
            float h = f / 2.0f;
            double d = this.getX();
            double e = this.getY();
            double i = this.getZ();
            this.cullingBoundingBox = new AABB(d - (double)h, e, i - (double)h, d + (double)h, e + (double)g, i + (double)h);
        }
    }

    @Override
    public void setXRot(float f) {
        super.setXRot(f);
        this.updateOrientation();
    }

    @Override
    public void setYRot(float f) {
        super.setYRot(f);
        this.updateOrientation();
    }

    private void updateOrientation() {
        this.orientation.rotationYXZ((float)(-Math.PI) / 180 * this.getYRot(), (float)Math.PI / 180 * this.getXRot(), 0.0f);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        return d < Mth.square((double)this.getViewRange() * 64.0 * Display.getViewScale());
    }

    @Override
    public int getTeamColor() {
        int i = this.getGlowColorOverride();
        return i != -1 ? i : super.getTeamColor();
    }

    static abstract class GenericInterpolator<T>
    extends Interpolator<T> {
        protected GenericInterpolator(T object) {
            super(object);
        }

        protected abstract T interpolate(float var1, T var2, T var3);

        public T get(float f) {
            if ((double)f >= 1.0 || this.lastValue == null) {
                return (T)this.currentValue;
            }
            return (T)this.interpolate(f, this.lastValue, this.currentValue);
        }

        @Override
        public void updateValue(float f, T object) {
            this.lastValue = this.get(f);
            this.currentValue = object;
        }
    }

    static class FloatInterpolator
    extends Interpolator<Float> {
        protected FloatInterpolator(float f) {
            super(Float.valueOf(f));
        }

        protected float interpolate(float f, float g, float h) {
            return Mth.lerp(f, g, h);
        }

        public float get(float f) {
            if ((double)f >= 1.0 || this.lastValue == null) {
                return ((Float)this.currentValue).floatValue();
            }
            return this.interpolate(f, ((Float)this.lastValue).floatValue(), ((Float)this.currentValue).floatValue());
        }

        @Override
        public void updateValue(float f, Float float_) {
            this.lastValue = Float.valueOf(this.get(f));
            this.currentValue = float_;
        }
    }

    static class InterpolatorSet {
        private final IntSet interpolatedData = new IntOpenHashSet();
        private final List<IntepolatorUpdater> updaters = new ArrayList<IntepolatorUpdater>();

        InterpolatorSet() {
        }

        protected <T> void addEntry(EntityDataAccessor<T> entityDataAccessor, Interpolator<T> interpolator) {
            this.interpolatedData.add(entityDataAccessor.getId());
            this.updaters.add((f, synchedEntityData) -> interpolator.updateValue(f, synchedEntityData.get(entityDataAccessor)));
        }

        protected void addEntry(Set<EntityDataAccessor<?>> set, IntepolatorUpdater intepolatorUpdater) {
            for (EntityDataAccessor<?> entityDataAccessor : set) {
                this.interpolatedData.add(entityDataAccessor.getId());
            }
            this.updaters.add(intepolatorUpdater);
        }

        public boolean shouldTriggerUpdate(int i) {
            return this.interpolatedData.contains(i);
        }

        public void updateValues(float f, SynchedEntityData synchedEntityData) {
            for (IntepolatorUpdater intepolatorUpdater : this.updaters) {
                intepolatorUpdater.update(f, synchedEntityData);
            }
        }
    }

    @FunctionalInterface
    static interface IntepolatorUpdater {
        public void update(float var1, SynchedEntityData var2);
    }

    static abstract class Interpolator<T> {
        @Nullable
        protected T lastValue;
        protected T currentValue;

        protected Interpolator(T object) {
            this.currentValue = object;
        }

        public abstract void updateValue(float var1, T var2);
    }

    public static enum BillboardConstraints implements StringRepresentable
    {
        FIXED(0, "fixed"),
        VERTICAL(1, "vertical"),
        HORIZONTAL(2, "horizontal"),
        CENTER(3, "center");

        public static final Codec<BillboardConstraints> CODEC;
        public static final IntFunction<BillboardConstraints> BY_ID;
        private final byte id;
        private final String name;

        private BillboardConstraints(byte b, String string2) {
            this.name = string2;
            this.id = b;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        byte getId() {
            return this.id;
        }

        static {
            CODEC = StringRepresentable.fromEnum(BillboardConstraints::values);
            BY_ID = ByIdMap.continuous(BillboardConstraints::getId, BillboardConstraints.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        }
    }

    static class ColorInterpolator
    extends IntInterpolator {
        protected ColorInterpolator(int i) {
            super(i);
        }

        @Override
        protected int interpolate(float f, int i, int j) {
            return FastColor.ARGB32.lerp(f, i, j);
        }
    }

    static class IntInterpolator
    extends Interpolator<Integer> {
        protected IntInterpolator(int i) {
            super(i);
        }

        protected int interpolate(float f, int i, int j) {
            return Mth.lerpInt(f, i, j);
        }

        public int get(float f) {
            if ((double)f >= 1.0 || this.lastValue == null) {
                return (Integer)this.currentValue;
            }
            return this.interpolate(f, (Integer)this.lastValue, (Integer)this.currentValue);
        }

        @Override
        public void updateValue(float f, Integer integer) {
            this.lastValue = this.get(f);
            this.currentValue = integer;
        }
    }

    public static class TextDisplay
    extends Display {
        public static final String TAG_TEXT = "text";
        private static final String TAG_LINE_WIDTH = "line_width";
        private static final String TAG_TEXT_OPACITY = "text_opacity";
        private static final String TAG_BACKGROUND_COLOR = "background";
        private static final String TAG_SHADOW = "shadow";
        private static final String TAG_SEE_THROUGH = "see_through";
        private static final String TAG_USE_DEFAULT_BACKGROUND = "default_background";
        private static final String TAG_ALIGNMENT = "alignment";
        public static final byte FLAG_SHADOW = 1;
        public static final byte FLAG_SEE_THROUGH = 2;
        public static final byte FLAG_USE_DEFAULT_BACKGROUND = 4;
        public static final byte FLAG_ALIGN_LEFT = 8;
        public static final byte FLAG_ALIGN_RIGHT = 16;
        private static final byte INITIAL_TEXT_OPACITY = -1;
        public static final int INITIAL_BACKGROUND = 0x40000000;
        private static final EntityDataAccessor<Component> DATA_TEXT_ID = SynchedEntityData.defineId(TextDisplay.class, EntityDataSerializers.COMPONENT);
        private static final EntityDataAccessor<Integer> DATA_LINE_WIDTH_ID = SynchedEntityData.defineId(TextDisplay.class, EntityDataSerializers.INT);
        private static final EntityDataAccessor<Integer> DATA_BACKGROUND_COLOR_ID = SynchedEntityData.defineId(TextDisplay.class, EntityDataSerializers.INT);
        private static final EntityDataAccessor<Byte> DATA_TEXT_OPACITY_ID = SynchedEntityData.defineId(TextDisplay.class, EntityDataSerializers.BYTE);
        private static final EntityDataAccessor<Byte> DATA_STYLE_FLAGS_ID = SynchedEntityData.defineId(TextDisplay.class, EntityDataSerializers.BYTE);
        private final IntInterpolator textOpacity = new IntInterpolator(-1);
        private final IntInterpolator backgroundColor = new ColorInterpolator(0x40000000);
        @Nullable
        private CachedInfo clientDisplayCache;

        public TextDisplay(EntityType<?> entityType, Level level) {
            super(entityType, level);
            this.interpolators.addEntry(DATA_BACKGROUND_COLOR_ID, this.backgroundColor);
            this.interpolators.addEntry(Set.of(DATA_TEXT_OPACITY_ID), (f, synchedEntityData) -> this.textOpacity.updateValue(f, synchedEntityData.get(DATA_TEXT_OPACITY_ID) & 0xFF));
        }

        @Override
        protected void defineSynchedData() {
            super.defineSynchedData();
            this.entityData.define(DATA_TEXT_ID, Component.empty());
            this.entityData.define(DATA_LINE_WIDTH_ID, 200);
            this.entityData.define(DATA_BACKGROUND_COLOR_ID, 0x40000000);
            this.entityData.define(DATA_TEXT_OPACITY_ID, (byte)-1);
            this.entityData.define(DATA_STYLE_FLAGS_ID, (byte)0);
        }

        @Override
        public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
            super.onSyncedDataUpdated(entityDataAccessor);
            this.clientDisplayCache = null;
        }

        public Component getText() {
            return this.entityData.get(DATA_TEXT_ID);
        }

        private void setText(Component component) {
            this.entityData.set(DATA_TEXT_ID, component);
        }

        public int getLineWidth() {
            return this.entityData.get(DATA_LINE_WIDTH_ID);
        }

        private void setLineWidth(int i) {
            this.entityData.set(DATA_LINE_WIDTH_ID, i);
        }

        public byte getTextOpacity(float f) {
            return (byte)this.textOpacity.get(f);
        }

        private byte getTextOpacity() {
            return this.entityData.get(DATA_TEXT_OPACITY_ID);
        }

        private void setTextOpacity(byte b) {
            this.entityData.set(DATA_TEXT_OPACITY_ID, b);
        }

        public int getBackgroundColor(float f) {
            return this.backgroundColor.get(f);
        }

        private int getBackgroundColor() {
            return this.entityData.get(DATA_BACKGROUND_COLOR_ID);
        }

        private void setBackgroundColor(int i) {
            this.entityData.set(DATA_BACKGROUND_COLOR_ID, i);
        }

        public byte getFlags() {
            return this.entityData.get(DATA_STYLE_FLAGS_ID);
        }

        private void setFlags(byte b) {
            this.entityData.set(DATA_STYLE_FLAGS_ID, b);
        }

        private static byte loadFlag(byte b, CompoundTag compoundTag, String string, byte c) {
            if (compoundTag.getBoolean(string)) {
                return (byte)(b | c);
            }
            return b;
        }

        @Override
        protected void readAdditionalSaveData(CompoundTag compoundTag) {
            super.readAdditionalSaveData(compoundTag);
            if (compoundTag.contains(TAG_LINE_WIDTH, 99)) {
                this.setLineWidth(compoundTag.getInt(TAG_LINE_WIDTH));
            }
            if (compoundTag.contains(TAG_TEXT_OPACITY, 99)) {
                this.setTextOpacity(compoundTag.getByte(TAG_TEXT_OPACITY));
            }
            if (compoundTag.contains(TAG_BACKGROUND_COLOR, 99)) {
                this.setBackgroundColor(compoundTag.getInt(TAG_BACKGROUND_COLOR));
            }
            byte b = TextDisplay.loadFlag((byte)0, compoundTag, TAG_SHADOW, (byte)1);
            b = TextDisplay.loadFlag(b, compoundTag, TAG_SEE_THROUGH, (byte)2);
            b = TextDisplay.loadFlag(b, compoundTag, TAG_USE_DEFAULT_BACKGROUND, (byte)4);
            Optional<Align> optional = Align.CODEC.decode(NbtOps.INSTANCE, compoundTag.get(TAG_ALIGNMENT)).resultOrPartial(Util.prefix("Display entity", LOGGER::error)).map(Pair::getFirst);
            if (optional.isPresent()) {
                b = switch (optional.get()) {
                    default -> throw new IncompatibleClassChangeError();
                    case Align.CENTER -> b;
                    case Align.LEFT -> (byte)(b | 8);
                    case Align.RIGHT -> (byte)(b | 0x10);
                };
            }
            this.setFlags(b);
            if (compoundTag.contains(TAG_TEXT, 8)) {
                String string = compoundTag.getString(TAG_TEXT);
                try {
                    MutableComponent component = Component.Serializer.fromJson(string);
                    if (component != null) {
                        CommandSourceStack commandSourceStack = this.createCommandSourceStack().withPermission(2);
                        MutableComponent component2 = ComponentUtils.updateForEntity(commandSourceStack, component, (Entity)this, 0);
                        this.setText(component2);
                    } else {
                        this.setText(Component.empty());
                    }
                } catch (Exception exception) {
                    LOGGER.warn("Failed to parse display entity text {}", (Object)string, (Object)exception);
                }
            }
        }

        private static void storeFlag(byte b, CompoundTag compoundTag, String string, byte c) {
            compoundTag.putBoolean(string, (b & c) != 0);
        }

        @Override
        protected void addAdditionalSaveData(CompoundTag compoundTag) {
            super.addAdditionalSaveData(compoundTag);
            compoundTag.putString(TAG_TEXT, Component.Serializer.toJson(this.getText()));
            compoundTag.putInt(TAG_LINE_WIDTH, this.getLineWidth());
            compoundTag.putInt(TAG_BACKGROUND_COLOR, this.getBackgroundColor());
            compoundTag.putByte(TAG_TEXT_OPACITY, this.getTextOpacity());
            byte b = this.getFlags();
            TextDisplay.storeFlag(b, compoundTag, TAG_SHADOW, (byte)1);
            TextDisplay.storeFlag(b, compoundTag, TAG_SEE_THROUGH, (byte)2);
            TextDisplay.storeFlag(b, compoundTag, TAG_USE_DEFAULT_BACKGROUND, (byte)4);
            Align.CODEC.encodeStart(NbtOps.INSTANCE, TextDisplay.getAlign(b)).result().ifPresent(tag -> compoundTag.put(TAG_ALIGNMENT, (Tag)tag));
        }

        public CachedInfo cacheDisplay(LineSplitter lineSplitter) {
            if (this.clientDisplayCache == null) {
                int i = this.getLineWidth();
                this.clientDisplayCache = lineSplitter.split(this.getText(), i);
            }
            return this.clientDisplayCache;
        }

        public static Align getAlign(byte b) {
            if ((b & 8) != 0) {
                return Align.LEFT;
            }
            if ((b & 0x10) != 0) {
                return Align.RIGHT;
            }
            return Align.CENTER;
        }

        public record CachedInfo(List<CachedLine> lines, int width) {
        }

        public static enum Align implements StringRepresentable
        {
            CENTER("center"),
            LEFT("left"),
            RIGHT("right");

            public static final Codec<Align> CODEC;
            private final String name;

            private Align(String string2) {
                this.name = string2;
            }

            @Override
            public String getSerializedName() {
                return this.name;
            }

            static {
                CODEC = StringRepresentable.fromEnum(Align::values);
            }
        }

        @FunctionalInterface
        public static interface LineSplitter {
            public CachedInfo split(Component var1, int var2);
        }

        public record CachedLine(FormattedCharSequence contents, int width) {
        }
    }

    public static class BlockDisplay
    extends Display {
        public static final String TAG_BLOCK_STATE = "block_state";
        private static final EntityDataAccessor<BlockState> DATA_BLOCK_STATE_ID = SynchedEntityData.defineId(BlockDisplay.class, EntityDataSerializers.BLOCK_STATE);

        public BlockDisplay(EntityType<?> entityType, Level level) {
            super(entityType, level);
        }

        @Override
        protected void defineSynchedData() {
            super.defineSynchedData();
            this.entityData.define(DATA_BLOCK_STATE_ID, Blocks.AIR.defaultBlockState());
        }

        public BlockState getBlockState() {
            return this.entityData.get(DATA_BLOCK_STATE_ID);
        }

        public void setBlockState(BlockState blockState) {
            this.entityData.set(DATA_BLOCK_STATE_ID, blockState);
        }

        @Override
        protected void readAdditionalSaveData(CompoundTag compoundTag) {
            super.readAdditionalSaveData(compoundTag);
            this.setBlockState(NbtUtils.readBlockState(this.level.holderLookup(Registries.BLOCK), compoundTag.getCompound(TAG_BLOCK_STATE)));
        }

        @Override
        protected void addAdditionalSaveData(CompoundTag compoundTag) {
            super.addAdditionalSaveData(compoundTag);
            compoundTag.put(TAG_BLOCK_STATE, NbtUtils.writeBlockState(this.getBlockState()));
        }
    }

    public static class ItemDisplay
    extends Display {
        private static final String TAG_ITEM = "item";
        private static final String TAG_ITEM_DISPLAY = "item_display";
        private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK_ID = SynchedEntityData.defineId(ItemDisplay.class, EntityDataSerializers.ITEM_STACK);
        private static final EntityDataAccessor<Byte> DATA_ITEM_DISPLAY_ID = SynchedEntityData.defineId(ItemDisplay.class, EntityDataSerializers.BYTE);
        private final SlotAccess slot = new SlotAccess(){

            @Override
            public ItemStack get() {
                return this.getItemStack();
            }

            @Override
            public boolean set(ItemStack itemStack) {
                this.setItemStack(itemStack);
                return true;
            }
        };

        public ItemDisplay(EntityType<?> entityType, Level level) {
            super(entityType, level);
        }

        @Override
        protected void defineSynchedData() {
            super.defineSynchedData();
            this.entityData.define(DATA_ITEM_STACK_ID, ItemStack.EMPTY);
            this.entityData.define(DATA_ITEM_DISPLAY_ID, ItemDisplayContext.NONE.getId());
        }

        public ItemStack getItemStack() {
            return this.entityData.get(DATA_ITEM_STACK_ID);
        }

        void setItemStack(ItemStack itemStack) {
            this.entityData.set(DATA_ITEM_STACK_ID, itemStack);
        }

        private void setItemTransform(ItemDisplayContext itemDisplayContext) {
            this.entityData.set(DATA_ITEM_DISPLAY_ID, itemDisplayContext.getId());
        }

        public ItemDisplayContext getItemTransform() {
            return ItemDisplayContext.BY_ID.apply(this.entityData.get(DATA_ITEM_DISPLAY_ID).byteValue());
        }

        @Override
        protected void readAdditionalSaveData(CompoundTag compoundTag) {
            super.readAdditionalSaveData(compoundTag);
            this.setItemStack(ItemStack.of(compoundTag.getCompound(TAG_ITEM)));
            if (compoundTag.contains(TAG_ITEM_DISPLAY, 8)) {
                ItemDisplayContext.CODEC.decode(NbtOps.INSTANCE, compoundTag.get(TAG_ITEM_DISPLAY)).resultOrPartial(Util.prefix("Display entity", LOGGER::error)).ifPresent(pair -> this.setItemTransform((ItemDisplayContext)pair.getFirst()));
            }
        }

        @Override
        protected void addAdditionalSaveData(CompoundTag compoundTag) {
            super.addAdditionalSaveData(compoundTag);
            compoundTag.put(TAG_ITEM, this.getItemStack().save(new CompoundTag()));
            ItemDisplayContext.CODEC.encodeStart(NbtOps.INSTANCE, this.getItemTransform()).result().ifPresent(tag -> compoundTag.put(TAG_ITEM_DISPLAY, (Tag)tag));
        }

        @Override
        public SlotAccess getSlot(int i) {
            if (i == 0) {
                return this.slot;
            }
            return SlotAccess.NULL;
        }
    }
}

