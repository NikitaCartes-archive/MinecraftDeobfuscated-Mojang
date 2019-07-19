/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.network.chat.Component;

public class HoverEvent {
    private final Action action;
    private final Component value;

    public HoverEvent(Action action, Component component) {
        this.action = action;
        this.value = component;
    }

    public Action getAction() {
        return this.action;
    }

    public Component getValue() {
        return this.value;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        HoverEvent hoverEvent = (HoverEvent)object;
        if (this.action != hoverEvent.action) {
            return false;
        }
        return !(this.value != null ? !this.value.equals(hoverEvent.value) : hoverEvent.value != null);
    }

    public String toString() {
        return "HoverEvent{action=" + (Object)((Object)this.action) + ", value='" + this.value + '\'' + '}';
    }

    public int hashCode() {
        int i = this.action.hashCode();
        i = 31 * i + (this.value != null ? this.value.hashCode() : 0);
        return i;
    }

    public static enum Action {
        SHOW_TEXT("show_text", true),
        SHOW_ITEM("show_item", true),
        SHOW_ENTITY("show_entity", true);

        private static final Map<String, Action> LOOKUP;
        private final boolean allowFromServer;
        private final String name;

        private Action(String string2, boolean bl) {
            this.name = string2;
            this.allowFromServer = bl;
        }

        public boolean isAllowedFromServer() {
            return this.allowFromServer;
        }

        public String getName() {
            return this.name;
        }

        public static Action getByName(String string) {
            return LOOKUP.get(string);
        }

        static {
            LOOKUP = Arrays.stream(Action.values()).collect(Collectors.toMap(Action::getName, action -> action));
        }
    }
}

