package net.avizvul.esquissemod.network;

import net.avizvul.esquissemod.component.SketchData;
import net.minecraft.network.FriendlyByteBuf;

public record SketchbookSavePayload(int pageIndex, SketchData sketchData, int pencilPixels, int eraserPixels, int kneadedPixels, int smudgePixels) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.pageIndex);
        this.sketchData.write(buf);
        buf.writeInt(this.pencilPixels);
        buf.writeInt(this.eraserPixels);
        buf.writeInt(this.kneadedPixels);
        buf.writeInt(this.smudgePixels);
    }

    public static SketchbookSavePayload decode(FriendlyByteBuf buf) {
        return new SketchbookSavePayload(buf.readInt(), SketchData.read(buf), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
    }
}
