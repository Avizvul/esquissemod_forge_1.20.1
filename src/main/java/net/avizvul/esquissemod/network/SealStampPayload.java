package net.avizvul.esquissemod.network;

import net.minecraft.network.FriendlyByteBuf;

import java.util.List;

public record SealStampPayload(List<Integer> rows, String name, boolean offHand) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarIntArray(this.rows.stream().mapToInt(Integer::intValue).toArray());
        buf.writeUtf(this.name);
        buf.writeBoolean(this.offHand);
    }

    public static SealStampPayload decode(FriendlyByteBuf buf) {
        return new SealStampPayload(java.util.Arrays.stream(buf.readVarIntArray()).boxed().toList(), buf.readUtf(), buf.readBoolean());
    }
}
