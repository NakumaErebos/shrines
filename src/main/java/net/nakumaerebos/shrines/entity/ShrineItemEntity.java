package net.nakumaerebos.shrines.entity;

import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ShrineItemEntity extends AbstractRotatingItemEntity {

    public ShrineItemEntity(EntityType<? extends ShrineItemEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public @NotNull EntityDimensions getDimensions(@NotNull Pose pose) {
        // Definiert die Größe als 1x1x1 Block (Breite, Höhe)
        return EntityDimensions.fixed(1.0F, 1.0F);
    }
}