package net.nakumaerebos.shrines.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractRotatingItemEntity extends Entity {
    // Synchronisierter Daten-Parameter für den Client
    private static final EntityDataAccessor<ItemStack> ITEM_STACK =
            SynchedEntityData.defineId(AbstractRotatingItemEntity.class, EntityDataSerializers.ITEM_STACK);

    public AbstractRotatingItemEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true; // Entity soll nicht durch die Welt geschoben werden
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ITEM_STACK, ItemStack.EMPTY);
    }

    public void setItem(ItemStack stack) {
        this.entityData.set(ITEM_STACK, stack.copy());
    }

    public ItemStack getItem() {
        return this.entityData.get(ITEM_STACK);
    }

    @Override
    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand) {
        if (!this.level().isClientSide) {
            ItemStack stack = getItem();
            if (!stack.isEmpty()) {
                // Item dem Spieler geben
                if (!player.getInventory().add(stack)) {
                    player.drop(stack, false);
                }
                // Entity entfernen
                this.discard();
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    // In deiner AbstractRotatingItemEntity Klasse hinzufügen:

    @Override
    public boolean isPickable() {
        // Ohne das kann man nicht mit dem Entity interagieren (kein Hit-Test)
        return !this.isRemoved();
    }

    @Override
    public boolean isAttackable() {
        // Falls du willst, dass man es auch "kaputtschlagen" kann
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("Item", 10)) {
            // In 1.21.1 nutzt ItemStack.parseOptional das RegistryAccess-System
            setItem(ItemStack.parseOptional(this.registryAccess(), compound.getCompound("Item")));
        }
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag compound) {
        if (!getItem().isEmpty()) {
            compound.put("Item", getItem().save(this.registryAccess()));
        }
    }
}