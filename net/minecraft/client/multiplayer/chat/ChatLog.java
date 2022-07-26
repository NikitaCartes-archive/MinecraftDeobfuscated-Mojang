/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.multiplayer.chat;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Collection;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.Spliterators;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.chat.LoggedChatEvent;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface ChatLog {
    public static final int NO_MESSAGE = -1;

    public void push(LoggedChatEvent var1);

    @Nullable
    public LoggedChatEvent lookup(int var1);

    @Nullable
    default public Entry<LoggedChatEvent> lookupEntry(int i) {
        LoggedChatEvent loggedChatEvent = this.lookup(i);
        return loggedChatEvent != null ? new Entry<LoggedChatEvent>(i, loggedChatEvent) : null;
    }

    default public boolean contains(int i) {
        return this.lookup(i) != null;
    }

    public int offset(int var1, int var2);

    default public int before(int i) {
        return this.offset(i, -1);
    }

    default public int after(int i) {
        return this.offset(i, 1);
    }

    public int newest();

    public int oldest();

    default public Selection selectAll() {
        return this.selectAfter(this.oldest());
    }

    default public Selection selectAllDescending() {
        return this.selectBefore(this.newest());
    }

    default public Selection selectAfter(int i) {
        return this.selectSequence(i, this::after);
    }

    default public Selection selectBefore(int i) {
        return this.selectSequence(i, this::before);
    }

    default public Selection selectBetween(int i, int j2) {
        if (!this.contains(i) || !this.contains(j2)) {
            return this.selectNone();
        }
        return this.selectSequence(i, j -> {
            if (j == j2) {
                return -1;
            }
            return this.after(j);
        });
    }

    default public Selection selectSequence(final int i, final IntUnaryOperator intUnaryOperator) {
        if (!this.contains(i)) {
            return this.selectNone();
        }
        return new Selection(this, new PrimitiveIterator.OfInt(){
            private int nextId;
            {
                this.nextId = i;
            }

            @Override
            public int nextInt() {
                int i2 = this.nextId;
                this.nextId = intUnaryOperator.applyAsInt(i2);
                return i2;
            }

            @Override
            public boolean hasNext() {
                return this.nextId != -1;
            }
        });
    }

    private Selection selectNone() {
        return new Selection(this, IntList.of().iterator());
    }

    @Environment(value=EnvType.CLIENT)
    public record Entry<T extends LoggedChatEvent>(int id, T event) {
        @Nullable
        public <U extends LoggedChatEvent> Entry<U> tryCast(Class<U> class_) {
            if (class_.isInstance(this.event)) {
                return new Entry<LoggedChatEvent>(this.id, (LoggedChatEvent)class_.cast(this.event));
            }
            return null;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Selection {
        private static final int CHARACTERISTICS = 1041;
        private final ChatLog log;
        private final PrimitiveIterator.OfInt ids;

        Selection(ChatLog chatLog, PrimitiveIterator.OfInt ofInt) {
            this.log = chatLog;
            this.ids = ofInt;
        }

        public IntStream ids() {
            return StreamSupport.intStream(Spliterators.spliteratorUnknownSize(this.ids, 1041), false);
        }

        public Stream<LoggedChatEvent> events() {
            return this.ids().mapToObj(this.log::lookup).filter(Objects::nonNull);
        }

        public Collection<GameProfile> reportableGameProfiles() {
            return this.events().map(loggedChatEvent -> {
                LoggedChatMessage.Player player;
                if (loggedChatEvent instanceof LoggedChatMessage.Player && (player = (LoggedChatMessage.Player)loggedChatEvent).canReport(player.profile().getId())) {
                    return player.profile();
                }
                return null;
            }).filter(Objects::nonNull).distinct().toList();
        }

        public Stream<Entry<LoggedChatEvent>> entries() {
            return this.ids().mapToObj(this.log::lookupEntry).filter(Objects::nonNull);
        }
    }
}

