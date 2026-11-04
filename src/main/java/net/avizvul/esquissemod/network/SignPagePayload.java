package net.avizvul.esquissemod.network;

import net.minecraft.network.FriendlyByteBuf;

public record SignPagePayload(String title, boolean offHand) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(this.title);
        buf.writeBoolean(this.offHand);
    }

    public static SignPagePayload decode(FriendlyByteBuf buf) {
        return new SignPagePayload(buf.readUtf(), buf.readBoolean());
    }
}
