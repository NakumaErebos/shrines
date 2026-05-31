package net.nakumaerebos.shrines.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.nakumaerebos.shrines.effect.ModEffects;

public class DeepDarknessBlock extends Block {

    // 1. Definiere die Property für den Amplifier (0 bis 15)
    // Wir können hier prima die Vanilla-Konstante AGE_15 missbrauchen oder eine eigene erstellen.
    // Eigene Definition für maximale Klarheit:
    public static final IntegerProperty AMPLIFIER = IntegerProperty.create("amplifier", 0, 255);

    public DeepDarknessBlock(Properties properties) {
        super(properties);
        // 2. Setze den Standard-Zustand des Blocks auf Stufe 0
        this.registerDefaultState(this.stateDefinition.any().setValue(AMPLIFIER, 0));
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide() && entity instanceof LivingEntity livingEntity) {
            // 3. Lies den aktuellen Amplifier-Wert direkt aus dem BlockState aus
            int currentAmplifier = (255 - state.getValue(AMPLIFIER));

            livingEntity.addEffect(new MobEffectInstance(
                    ModEffects.DEEP_DARKNESS,
                    MobEffectInstance.INFINITE_DURATION,
                    currentAmplifier, // Nutzt jetzt den Wert vom Block!
                    false,
                    false,
                    false
            ));
        }

        super.stepOn(level, pos, state, entity);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AMPLIFIER);
    }
}