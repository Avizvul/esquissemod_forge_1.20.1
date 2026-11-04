package net.avizvul.esquissemod.network;

import net.avizvul.esquissemod.component.SketchData;
import net.minecraft.network.FriendlyByteBuf;

public record TearPagePayload(int pageIndex, SketchData sketchData) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.pageIndex);
        this.sketchData.write(buf);
    }

    public static TearPagePayload decode(FriendlyByteBuf buf) {
        return new TearPagePayload(buf.readInt(), SketchData.read(buf));
    }
}
