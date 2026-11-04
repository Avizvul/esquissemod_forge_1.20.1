package net.avizvul.esquissemod.block.entity;

import net.avizvul.esquissemod.component.ModDataComponents;
import net.avizvul.esquissemod.component.SketchData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SketchedPageBlockEntity extends BlockEntity {

    private String author = null;
    private boolean signed = false;
    private boolean copied = false;
    private Component customName = null;

    /** Забирает подпись, автора и пометку копии с предмета при размещении. */
    public void readMetaFrom(ItemStack stack) {
        this.author = ModDataComponents.PAGE_AUTHOR.get(stack);
        this.signed = ModDataComponents.PAGE_SIGNED.getOrDefault(stack, false);
        this.copied = ModDataComponents.PAGE_COPIED.getOrDefault(stack, false);
        this.customName = net.avizvul.esquissemod.component.ModDataComponents.CUSTOM_NAME.get(stack);
        setChanged();
    }

    /** Возвращает всё обратно на предмет при снятии со стены. */
    public void writeMetaTo(ItemStack stack) {
        if (this.author != null) ModDataComponents.PAGE_AUTHOR.set(stack, this.author);
        if (this.signed) ModDataComponents.PAGE_SIGNED.set(stack, true);
        if (this.copied) ModDataComponents.PAGE_COPIED.set(stack, true);
        if (this.customName != null) net.avizvul.esquissemod.component.ModDataComponents.CUSTOM_NAME.set(stack, this.customName);
    }

    private SketchData sketchData = SketchData.fromArray(new int[][]{});
    private int rotation = 0;

    public SketchedPageBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SKETCHED_PAGE_BE.get(), pos, state);
    }

    public void setSketchData(SketchData data) {
        this.sketchData = data;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public SketchData getSketchData() {
        return sketchData;
    }

    public int getRotation() {
        return this.rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // === ОБЯЗАТЕЛЬНЫЕ МЕТОДЫ ДЛЯ СИНХРОНИЗАЦИИ С КЛИЕНТОМ ===
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Rotation", this.rotation);
        if (this.sketchData != null) {
            tag.put("SketchData", this.sketchData.toTag());
        }
        if (this.author != null) tag.putString("Author", this.author);
        tag.putBoolean("Signed", this.signed);
        tag.putBoolean("Copied", this.copied);
        if (this.customName != null) {
            tag.putString("PageName", Component.Serializer.toJson(this.customName));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.rotation = tag.getInt("Rotation");
        if (tag.contains("SketchData")) {
            this.sketchData = SketchData.fromTag(tag.getCompound("SketchData"));
        }
        this.author = tag.contains("Author") ? tag.getString("Author") : null;
        this.signed = tag.getBoolean("Signed");
        this.copied = tag.getBoolean("Copied");
        this.customName = tag.contains("PageName")
                ? Component.Serializer.fromJson(tag.getString("PageName")) : null;
    }
}
