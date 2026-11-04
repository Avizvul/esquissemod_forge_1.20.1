package net.avizvul.esquissemod.network;

import net.minecraft.network.FriendlyByteBuf;

public record ToolSettingsPayload(int toolType, int size, int hardness, int rotation) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.toolType);
        buf.writeInt(this.size);
        buf.writeInt(this.hardness);
        buf.writeInt(this.rotation);
    }

    public static ToolSettingsPayload decode(FriendlyByteBuf buf) {
        return new ToolSettingsPayload(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
    }
}
