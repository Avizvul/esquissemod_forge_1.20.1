package net.avizvul.esquissemod.network;

import net.minecraft.network.FriendlyByteBuf;

public record ToolUsagePayload(int pencilPixels, int eraserPixels, int kneadedPixels, int smudgePixels) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.pencilPixels);
        buf.writeInt(this.eraserPixels);
        buf.writeInt(this.kneadedPixels);
        buf.writeInt(this.smudgePixels);
    }

    public static ToolUsagePayload decode(FriendlyByteBuf buf) {
        return new ToolUsagePayload(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
    }
}
