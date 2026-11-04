package net.avizvul.esquissemod.network;

import net.avizvul.esquissemod.component.SketchData;
import net.minecraft.network.FriendlyByteBuf;

public record ExportSketchPayload(SketchData sketchData) {

    public void encode(FriendlyByteBuf buf) {
        this.sketchData.write(buf);
    }

    public static ExportSketchPayload decode(FriendlyByteBuf buf) {
        return new ExportSketchPayload(SketchData.read(buf));
    }
}
