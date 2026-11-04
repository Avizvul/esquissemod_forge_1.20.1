package net.avizvul.esquissemod.network;

import net.minecraft.network.FriendlyByteBuf;

public record ChangeColorPayload(int colorIndex, int toolId) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.colorIndex);
        buf.writeInt(this.toolId);
    }

    public static ChangeColorPayload decode(FriendlyByteBuf buf) {
        return new ChangeColorPayload(buf.readInt(), buf.readInt());
    }
}
