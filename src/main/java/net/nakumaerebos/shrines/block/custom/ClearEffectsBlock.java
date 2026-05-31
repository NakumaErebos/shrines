package net.nakumaerebos.shrines.block.custom; // Passe das Paket an dein Projekt an

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ClearEffectsBlock extends Block {

    public ClearEffectsBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        // Auch hier: Nur auf dem Server ausführen und prüfen, ob es eine lebendige Entity ist
        if (!level.isClientSide() && entity instanceof LivingEntity livingEntity) {

            // Prüfen, ob die Entity überhaupt aktive Effekte hat, um unnötige Logik zu sparen
            if (!livingEntity.getActiveEffects().isEmpty()) {
                livingEntity.removeAllEffects(); // Entfernt alle Trankeffekte (positiv wie negativ)
            }
        }

        super.stepOn(level, pos, state, entity);
    }
}