package net.nakumaerebos.shrines.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.nakumaerebos.shrines.Shrines;
import net.nakumaerebos.shrines.block.ModBlocks;
import net.nakumaerebos.shrines.block.custom.SheikahStateBlock; // Dein Package-Pfad
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Shrines.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        // Beispiel für deinen speziellen Block
        sheikahStateBlockWithItem(ModBlocks.STARRY_DARKSTONE);
        sheikahStateBlockWithItem(ModBlocks.SHEIKAHSTONE);
        sheikahStateBlockWithItem(ModBlocks.DARK_SHEIKAHSTONE);
        sheikahStateBlockWithItem(ModBlocks.SHEIKAHSTONE_SWIRLS);

        blockWithItem(ModBlocks.DARKSTONE);
        blockWithItem(ModBlocks.CHISELED_DARKSTONE);

        pillarBlockWithItem(ModBlocks.PILLARED_DARKSTONE);

        triplePillarBlock(ModBlocks.GLOWTRIMMED_DARKSTONE);
        triplePillarBlock(ModBlocks.PILLARED_GLOWTRIMMED_DARKSTONE);
        triplePillarBlock(ModBlocks.TRIMMED_DARKSTONE);
        triplePillarBlock(ModBlocks.PILLARED_TRIMMED_DARKSTONE);
    }

    private void sheikahStateBlockWithItem(DeferredBlock<SheikahStateBlock> block) {
        String path = block.getId().getPath();

        getVariantBuilder(block.get()).forAllStates(state -> {
            int stateValue = state.getValue(SheikahStateBlock.STATE);
            return ConfiguredModel.builder()
                    .modelFile(models().cubeAll(path + "_" + stateValue,
                            modLoc("block/" + path + "_" + stateValue)))
                    .build();
        });

        // Das Item-Modell verweist einfach auf das Block-Modell von State 0
        simpleBlockItem(block.get(), models().getExistingFile(modLoc("block/" + path + "_0")));
    }

    private void pillarBlockWithItem(DeferredBlock<? extends RotatedPillarBlock> deferredBlock) {
        String path = deferredBlock.getId().getPath();
        RotatedPillarBlock block = deferredBlock.get();
        ResourceLocation side = modLoc("block/" + path + "_side");
        ResourceLocation end = modLoc("block/" + path + "_top");
        axisBlock(block, side, end);
        simpleBlockItem(block, models().getExistingFile(modLoc("block/" + path)));
    }

    private void triplePillarBlock(DeferredBlock<? extends RotatedPillarBlock> deferredBlock) {
        String path = deferredBlock.getId().getPath();
        RotatedPillarBlock block = deferredBlock.get();

        // 1. Das vertikale Modell mit 3 Textur-Variablen
        var verticalModel = models().withExistingParent(path, "block/cube_bottom_top")
                .texture("side", modLoc("block/" + path + "_side"))
                .texture("top", modLoc("block/" + path + "_top"))
                .texture("bottom", modLoc("block/" + path + "_bottom"));

        // 2. Das horizontale Modell (hier nutzen wir meist nur die 'top' Textur als 'end')
        var horizontalModel = models().withExistingParent(path + "_horizontal", "block/cube_column_horizontal")
                .texture("side", modLoc("block/" + path + "_side"))
                .texture("end", modLoc("block/" + path + "_top"));

        // 3. Blockstate-Zuweisung für die Achsen
        getVariantBuilder(block)
                .partialState().with(RotatedPillarBlock.AXIS, net.minecraft.core.Direction.Axis.Y)
                .modelForState().modelFile(verticalModel).addModel()
                .partialState().with(RotatedPillarBlock.AXIS, net.minecraft.core.Direction.Axis.Z)
                .modelForState().modelFile(horizontalModel).rotationX(90).addModel()
                .partialState().with(RotatedPillarBlock.AXIS, net.minecraft.core.Direction.Axis.X)
                .modelForState().modelFile(horizontalModel).rotationX(90).rotationY(90).addModel();

        // 4. Item Modell
        simpleBlockItem(block, verticalModel);
    }

    private void blockWithItem(DeferredBlock<?> deferredBlock) {
        simpleBlockWithItem(deferredBlock.get(), cubeAll(deferredBlock.get()));
    }

}