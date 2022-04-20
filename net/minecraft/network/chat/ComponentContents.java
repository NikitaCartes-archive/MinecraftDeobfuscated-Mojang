/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface ComponentContents {
    public static final ComponentContents EMPTY = new ComponentContents(){

        public String toString() {
            return "empty";
        }
    };

    default public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style) {
        return Optional.empty();
    }

    default public <T> Optional<T> visit(FormattedText.ContentConsumer<T> contentConsumer) {
        return Optional.empty();
    }

    default public MutableComponent resolve(@Nullable CommandSourceStack commandSourceStack, @Nullable Entity entity, int i) throws CommandSyntaxException {
        return MutableComponent.create(this);
    }
}

