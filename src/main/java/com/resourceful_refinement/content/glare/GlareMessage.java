package com.resourceful_refinement.content.glare;

import net.minecraft.nbt.CompoundTag;

public record GlareMessage(GlareAddress from, GlareAddress to, String body, long gameTime) {
    public static final int MAX_BODY_LENGTH = 512;

    public GlareMessage {
        if (body.length() > MAX_BODY_LENGTH) {
            body = body.substring(0, MAX_BODY_LENGTH);
        }
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.put("From", from.save());
        tag.put("To", to.save());
        tag.putString("Body", body);
        tag.putLong("GameTime", gameTime);
        return tag;
    }

    public static GlareMessage load(CompoundTag tag) {
        return new GlareMessage(
                GlareAddress.load(tag.getCompound("From")),
                GlareAddress.load(tag.getCompound("To")),
                tag.getString("Body"),
                tag.getLong("GameTime")
        );
    }
}
