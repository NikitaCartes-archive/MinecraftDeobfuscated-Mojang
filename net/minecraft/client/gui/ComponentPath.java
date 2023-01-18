/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface ComponentPath {
    public static ComponentPath leaf(GuiEventListener guiEventListener) {
        return new Leaf(guiEventListener);
    }

    @Nullable
    public static ComponentPath path(ContainerEventHandler containerEventHandler, @Nullable ComponentPath componentPath) {
        if (componentPath == null) {
            return null;
        }
        return new Path(containerEventHandler, componentPath);
    }

    public static ComponentPath path(GuiEventListener guiEventListener, ContainerEventHandler ... containerEventHandlers) {
        ComponentPath componentPath = ComponentPath.leaf(guiEventListener);
        for (ContainerEventHandler containerEventHandler : containerEventHandlers) {
            componentPath = ComponentPath.path(containerEventHandler, componentPath);
        }
        return componentPath;
    }

    public GuiEventListener component();

    public void applyFocus(boolean var1);

    @Environment(value=EnvType.CLIENT)
    public record Leaf(GuiEventListener component) implements ComponentPath
    {
        @Override
        public void applyFocus(boolean bl) {
            this.component.setFocused(bl);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static final class Path
    extends Record
    implements ComponentPath {
        private final ContainerEventHandler component;
        private final ComponentPath childPath;

        public Path(ContainerEventHandler containerEventHandler, ComponentPath componentPath) {
            this.component = containerEventHandler;
            this.childPath = componentPath;
        }

        @Override
        public void applyFocus(boolean bl) {
            if (!bl) {
                this.component.setFocused(null);
            } else {
                this.component.setFocused(this.childPath.component());
            }
            this.childPath.applyFocus(bl);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Path.class, "component;childPath", "component", "childPath"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Path.class, "component;childPath", "component", "childPath"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Path.class, "component;childPath", "component", "childPath"}, this, object);
        }

        @Override
        public ContainerEventHandler component() {
            return this.component;
        }

        public ComponentPath childPath() {
            return this.childPath;
        }

        @Override
        public /* synthetic */ GuiEventListener component() {
            return this.component();
        }
    }
}

