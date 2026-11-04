package net.avizvul.esquissemod.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public record PrinterActionPayload(BlockPos pos) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }

    public static PrinterActionPayload decode(FriendlyByteBuf buf) {
        return new PrinterActionPayload(buf.readBlockPos());
    }
}
