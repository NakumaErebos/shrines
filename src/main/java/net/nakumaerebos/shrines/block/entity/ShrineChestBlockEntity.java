package net.nakumaerebos.shrines.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents; // Ersetze das mit deinen Mod-Sounds!
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.nakumaerebos.shrines.block.custom.ShrineChestBlock;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ShrineChestBlockEntity extends ChestBlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // Unser eigener, sauberer Tracking-Counter für Spieler im GUI
    private int viewerCount = 0;

    // 0 = Geschlossen, 1 = Öffnen, 2 = Schließen, 3 = Leer/Dauerhaft Offen
    private int animationState = 0;

    public ShrineChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SHRINE_CHEST_BE.get(), pos, state);
    }

    @Override
    protected @NotNull Component getDefaultName() {
        return Component.translatable("container.shrines.shrine_chest");
    }

    // WICHTIG: Wir überschreiben startOpen und rufen bewusst NICHT super.startOpen auf!
    // Dadurch wird der private, Sound-erzeugende openersCounter von Minecraft komplett ignoriert.
    @Override
    public void startOpen(@NotNull Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.viewerCount++;
            Level level = this.getLevel();
            BlockPos pos = this.getBlockPos();
            BlockState state = this.getBlockState();

            if (level != null && !level.isClientSide) {
                // Nur beim ERSTEN Spieler, der die Kiste öffnet, triggern wir Sound + Animation
                if (this.viewerCount == 1) {
                    // DEIN EIGENER OPEN SOUND
                    level.playSound(null, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5,
                            SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);

                    // Sende Animations-Zustand 1 (Öffnen) an Clients
                    level.blockEvent(pos, state.getBlock(), 1, 1);
                }
            }
        }
    }

    // WICHTIG: Auch hier rufen wir NICHT super.stopOpen auf.
    @Override
    public void stopOpen(@NotNull Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.viewerCount = Math.max(0, this.viewerCount - 1);
            Level level = this.getLevel();
            BlockPos pos = this.getBlockPos();
            BlockState state = this.getBlockState();

            if (level != null && !level.isClientSide) {
                // Nur ausführen, wenn der LETZTE Spieler das Menü verlässt
                if (this.viewerCount == 0) {

                    // FALL 1: Die Truhe war leer (FILLED = false), aber es wurden Items hineingelegt
                    if (state.hasProperty(ShrineChestBlock.FILLED) && !state.getValue(ShrineChestBlock.FILLED) && !this.isEmpty()) {

                        // Setze FILLED wieder auf true
                        level.setBlock(pos, state.setValue(ShrineChestBlock.FILLED, true), 3);

                        // Spiele deinen Close-Sound
                        level.playSound(null, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5,
                                SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);

                        // Trigger die Schließen-Animation (Zustand 2)
                        level.blockEvent(pos, state.getBlock(), 1, 2);

                    }
                    // FALL 2: Die Truhe ist nach dem Schließen komplett leer
                    else if (this.isEmpty()) {
                        // Bleibt leer/wird leer -> FILLED = false, Dauerhaft offen (Zustand 3), kein Sound
                        level.setBlock(pos, state.setValue(ShrineChestBlock.FILLED, false), 3);
                        level.blockEvent(pos, state.getBlock(), 1, 3);
                    }
                    // FALL 3: Die Truhe war vorher befüllt und ist immer noch befüllt
                    else {
                        // Normaler Close-Sound + Schließen-Animation (Zustand 2)
                        level.playSound(null, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5,
                                SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);

                        level.blockEvent(pos, state.getBlock(), 1, 2);
                    }

                    this.setChanged();
                }
            }
        }
    }

    // Da wir super.startOpen / stopOpen umgehen, müssen wir signalOpenCount ins Leere laufen lassen
    @Override
    protected void signalOpenCount(Level level, BlockPos pos, BlockState state, int oldCount, int newCount) {
        // Absichtlich leer lassen!
    }

    // RecheckOpen wird von der Block-Klasse via Tick aufgerufen. Wir passen es an unseren Counter an.
    @Override
    public void recheckOpen() {
        if (!this.remove && this.viewerCount > 0) {
            // Falls benötigt, könnte hier ein Desync-Schutz rein. Für Einzeltruhen reicht viewerCount völlig aus.
        }
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == 1) {
            this.animationState = type;
            return true;
        }
        return super.triggerEvent(id, type);
    }

    // --- GeckoLib Animationen ---
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "chest_controller", 2, this::deployAnimationController));
    }

    private PlayState deployAnimationController(AnimationState<ShrineChestBlockEntity> state) {
        BlockState blockState = this.getBlockState();

        if (blockState.hasProperty(ShrineChestBlock.FILLED) && !blockState.getValue(ShrineChestBlock.FILLED)) {
            return state.setAndContinue(RawAnimation.begin().then("animation.shrine_chest.open_idle", Animation.LoopType.LOOP));
        }

        return switch (this.animationState) {
            case 1 -> state.setAndContinue(RawAnimation.begin().then("animation.shrine_chest.open", Animation.LoopType.HOLD_ON_LAST_FRAME));
            case 2 -> state.setAndContinue(RawAnimation.begin().then("animation.shrine_chest.close", Animation.LoopType.PLAY_ONCE).then("animation.shrine_chest.closed_idle", Animation.LoopType.HOLD_ON_LAST_FRAME));
            case 3 -> state.setAndContinue(RawAnimation.begin().then("animation.shrine_chest.open_idle", Animation.LoopType.LOOP));
            default -> state.setAndContinue(RawAnimation.begin().then("animation.shrine_chest.closed_idle", Animation.LoopType.HOLD_ON_LAST_FRAME));
        };
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}