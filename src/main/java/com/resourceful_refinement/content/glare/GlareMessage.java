package com.resourceful_refinement.content.glare;

import net.minecraft.nbt.CompoundTag;

import java.util.Objects;
import java.util.UUID;

public record GlareMessage(UUID id, GlareAddress from, GlareAddress to, String body, long gameTime) {
    public static final int MAX_BODY_LENGTH = 512;

    public GlareMessage(GlareAddress from, GlareAddress to, String body, long gameTime) {
        this(UUID.randomUUID(), from, to, body, gameTime);
    }

    public GlareMessage {
        id = Objects.requireNonNull(id, "id");
        from = Objects.requireNonNull(from, "from");
        to = Objects.requireNonNull(to, "to");
        body = Objects.requireNonNullElse(body, "");
        if (body.length() > MAX_BODY_LENGTH) {
            body = body.substring(0, MAX_BODY_LENGTH);
        }
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", id);
        tag.put("From", from.save());
        tag.put("To", to.save());
        tag.putString("Body", body);
        tag.putLong("GameTime", gameTime);
        return tag;
    }

    public static GlareMessage load(CompoundTag tag) {
        return new GlareMessage(
                tag.hasUUID("Id") ? tag.getUUID("Id") : UUID.randomUUID(),
                GlareAddress.load(tag.getCompound("From")),
                GlareAddress.load(tag.getCompound("To")),
                tag.getString("Body"),
                tag.getLong("GameTime")
        );
    }
}
