package net.nakumaerebos.shrines.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.nakumaerebos.shrines.entity.RemoteBombRoundEntity;
import net.nakumaerebos.shrines.sound.ModSounds;

public class RemoteBombLoopSoundInstance extends AbstractTickableSoundInstance {
    private final Entity bomb;

    public RemoteBombLoopSoundInstance(Entity bomb) {
        // SoundEvent, Kategorie und Zufalls-Samen zuweisen
        super(ModSounds.REMOTE_BOMB_HOLD.get(), SoundSource.NEUTRAL, bomb.getRandom());
        this.bomb = bomb;
        this.looping = true;       // Aktiviert die native Minecraft-Dauerschleife
        this.delay = 0;            // Keine Verzögerung beim Neustart des Samples
        this.volume = 0.5F;        // Basis-Lautstärke (0.0 bis 1.0)
        this.pitch = 1.0F;         // Tonhöhe

        // Initiale Position setzen
        this.x = (float) bomb.getX();
        this.y = (float) bomb.getY();
        this.z = (float) bomb.getZ();
    }

    @Override
    public void tick() {
        // Wenn die Bombe stirbt, ungültig wird oder explodiert -> Loop sofort stoppen!
        if (this.bomb.isRemoved() || !this.bomb.isAlive()) {
            this.stop();
            return;
        }

        // Sound-Position an die aktuellen Entity-Koordinaten heften
        this.x = (float) this.bomb.getX();
        this.y = (float) this.bomb.getY();
        this.z = (float) this.bomb.getZ();
    }
}