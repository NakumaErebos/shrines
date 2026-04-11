package net.nakumaerebos.shrines.datagen;

import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.nakumaerebos.shrines.Shrines;
import net.nakumaerebos.shrines.block.ModBlocks;
import net.nakumaerebos.shrines.block.custom.SheikahStateBlock; // Dein Package-Pfad
import net.nakumaerebos.shrines.block.custom.SheikahStateSlabBlock;
import net.nakumaerebos.shrines.block.custom.SheikahStateStairBlock;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
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
        sheikahStateStairBlockWithItem(ModBlocks.SHEIKAHSTONE_STAIRS,ModBlocks.SHEIKAHSTONE);
        sheikahStateSlabBlockWithItem(ModBlocks.SHEIKAHSTONE_SLAB,ModBlocks.SHEIKAHSTONE);

        sheikahStateStairBlockWithItem(ModBlocks.SHEIKAHSTONE_SWIRLS_STAIRS, ModBlocks.SHEIKAHSTONE_SWIRLS);
        sheikahStateSlabBlockWithItem(ModBlocks.SHEIKAHSTONE_SWIRLS_SLAB, ModBlocks.SHEIKAHSTONE_SWIRLS);

        blockWithItem(ModBlocks.DARKSTONE);
        blockWithItem(ModBlocks.CHISELED_DARKSTONE);

        blockWithOtherModel(ModBlocks.ELEVATOR, ModBlocks.DARKSTONE);
        blockWithOtherModel(ModBlocks.ELEVATOR_EXIT, ModBlocks.DARKSTONE);

        stairBlockWithItem(ModBlocks.DARKSTONE_STAIRS, ModBlocks.DARKSTONE);
        stairBlockWithItem(ModBlocks.CHISELED_DARKSTONE_STAIRS, ModBlocks.CHISELED_DARKSTONE);

        slabBlockWithItem(ModBlocks.DARKSTONE_SLAB, ModBlocks.DARKSTONE);
        slabBlockWithItem(ModBlocks.CHISELED_DARKSTONE_SLAB, ModBlocks.CHISELED_DARKSTONE);

        pillarBlockWithItem(ModBlocks.PILLARED_DARKSTONE);

        triplePillarBlock(ModBlocks.TRIMMED_SHEIKAHSTONE);
        triplePillarBlock(ModBlocks.GLOWTRIMMED_SHEIKAHSTONE);
        triplePillarBlock(ModBlocks.GLOWTRIMMED_DARKSTONE);
        triplePillarBlock(ModBlocks.PILLARED_GLOWTRIMMED_DARKSTONE);
        triplePillarBlock(ModBlocks.TRIMMED_DARKSTONE);
        triplePillarBlock(ModBlocks.PILLARED_TRIMMED_DARKSTONE);

        sheikahWallBlockWithItemStateZero(ModBlocks.SHEIKAHSTONE_SWIRLS_WALL, ModBlocks.SHEIKAHSTONE_SWIRLS);
    }

    private void stairBlockWithItem(DeferredBlock<? extends net.minecraft.world.level.block.StairBlock> block, DeferredBlock<Block> fullBlock) {
        // Holt die Textur des vollen Blocks (Parent-Block)
        ResourceLocation texture = modLoc("block/" + fullBlock.getId().getPath());

        // Erstellt die 3 Treppen-Modelle (normal, inner, outer) basierend auf der Textur
        ModelFile main = models().stairs(block.getId().getPath(), texture, texture, texture);
        ModelFile inner = models().stairsInner(block.getId().getPath() + "_inner", texture, texture, texture);
        ModelFile outer = models().stairsOuter(block.getId().getPath() + "_outer", texture, texture, texture);

        // Nutzt die eingebaute Forge-Logik für alle Zustände (Facing, Half, Shape)
        stairsBlock(block.get(), main, inner, outer);

        // Erstellt das Item-Modell, das auf das Standard-Treppenmodell verweist
        simpleBlockItem(block.get(), main);
    }

    private void sheikahStateStairBlockWithItem(DeferredBlock<SheikahStateStairBlock> block, DeferredBlock<SheikahStateBlock> fullBlock) {
        String path = block.getId().getPath();
        String fullBlockPath = fullBlock.getId().getPath();

        var builder = getVariantBuilder(block.get());

        for (int i = 0; i <= 3; i++) {
            int stateValue = i;
            ResourceLocation tex = modLoc("block/" + fullBlockPath + "_" + stateValue);

            // 1. Modelle erstellen
            ModelFile main = models().stairs(path + "_" + stateValue, tex, tex, tex);
            ModelFile inner = models().stairsInner(path + "_inner_" + stateValue, tex, tex, tex);
            ModelFile outer = models().stairsOuter(path + "_outer_" + stateValue, tex, tex, tex);

            for (Direction facing : BlockStateProperties.HORIZONTAL_FACING.getPossibleValues()) {
                for (Half half : BlockStateProperties.HALF.getPossibleValues()) {
                    for (StairsShape shape : BlockStateProperties.STAIRS_SHAPE.getPossibleValues()) {

                        // Wir korrigieren die Rotation:
                        // Wenn Norden und Süden vertauscht sind, addieren wir 180 Grad zur Y-Rotation.
                        int yRot = (getYRotation(facing, shape) + 180) % 360;

                        builder.partialState()
                                .with(SheikahStateStairBlock.STATE, stateValue)
                                .with(BlockStateProperties.HORIZONTAL_FACING, facing)
                                .with(BlockStateProperties.HALF, half)
                                .with(BlockStateProperties.STAIRS_SHAPE, shape)
                                .addModels(new ConfiguredModel(getSelectedStairModel(shape, main, inner, outer),
                                        getXRotation(half, shape), yRot, isUvLock(shape)));
                    }
                }
            }
        }

        simpleBlockItem(block.get(), models().getExistingFile(modLoc("block/" + path + "_0")));
    }

    private void slabBlockWithItem(DeferredBlock<? extends net.minecraft.world.level.block.SlabBlock> block, DeferredBlock<Block> fullBlock) {
        // Holt die Textur des vollen Blocks
        ResourceLocation texture = modLoc("block/" + fullBlock.getId().getPath());

        // Holt das Modell des vollen Blocks (für den Double-Slab Zustand)
        ModelFile doubleSlab = models().getExistingFile(texture);

        // Erstellt die Slab-Modelle (unten und oben)
        ModelFile bottom = models().slab(block.getId().getPath(), texture, texture, texture);
        ModelFile top = models().slabTop(block.getId().getPath() + "_top", texture, texture, texture);

        // Registriert den Blockstate mit allen Varianten
        slabBlock(block.get(), bottom, top, doubleSlab);

        // Item-Modell (nimmt standardmäßig das untere Slab-Modell)
        simpleBlockItem(block.get(), bottom);
    }

    private void sheikahStateSlabBlockWithItem(DeferredBlock<SheikahStateSlabBlock> block, DeferredBlock<SheikahStateBlock> fullBlock) {
        String path = block.getId().getPath();
        String fullBlockPath = fullBlock.getId().getPath();

        var builder = getVariantBuilder(block.get());

        for (int i = 0; i <= 3; i++) {
            int stateValue = i;
            ResourceLocation tex = modLoc("block/" + fullBlockPath + "_" + stateValue);

            // 1. Modelle erstellen (Unten, Oben, Doppelt)
            ModelFile bottom = models().slab(path + "_" + stateValue, tex, tex, tex);
            ModelFile top = models().slabTop(path + "_top_" + stateValue, tex, tex, tex);
            // Double Slab nutzt einfach das Modell des vollen Blocks
            ModelFile doubleSlab = models().getExistingFile(modLoc("block/" + fullBlockPath + "_" + stateValue));

            // 2. Varianten manuell zuweisen (für jeden SlabType)
            for (SlabType type : BlockStateProperties.SLAB_TYPE.getPossibleValues()) {
                builder.partialState()
                        .with(SheikahStateSlabBlock.STATE, stateValue)
                        .with(BlockStateProperties.SLAB_TYPE, type)
                        .addModels(new ConfiguredModel(getSelectedSlabModel(type, bottom, top, doubleSlab)));
            }
        }

        // Item-Modell (nimmt Modell von State 0)
        simpleBlockItem(block.get(), models().getExistingFile(modLoc("block/" + path + "_0")));
    }

    // Kleine Hilfsmethode zur Modellauswahl (wie bei deiner Treppe)
    private ModelFile getSelectedSlabModel(SlabType type, ModelFile bottom, ModelFile top, ModelFile doubleSlab) {
        return switch (type) {
            case TOP -> top;
            case DOUBLE -> doubleSlab;
            default -> bottom;
        };
    }


    // Hilfsmethoden für die Rotationen (damit der Code oben sauber bleibt)
    private ModelFile getSelectedStairModel(StairsShape shape, ModelFile main, ModelFile inner, ModelFile outer) {
        if (shape == StairsShape.STRAIGHT) return main;
        if (shape == StairsShape.INNER_LEFT || shape == StairsShape.INNER_RIGHT) return inner;
        return outer;
    }

    private int getYRotation(Direction facing, StairsShape shape) {
        int offset = (shape == StairsShape.OUTER_LEFT || shape == StairsShape.INNER_LEFT) ? -90 : 0;
        return ((int) facing.toYRot() + 270 + offset) % 360;
    }

    private int getXRotation(Half half, StairsShape shape) {
        return half == Half.TOP ? 180 : 0;
    }

    private boolean isUvLock(StairsShape shape) {
        return true;
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

    private void sheikahWallBlockWithItemStateZero(DeferredBlock<? extends net.minecraft.world.level.block.WallBlock> block, DeferredBlock<SheikahStateBlock> fullBlock) {
        // Wir erzwingen hier den Pfad zum State 0 des Full-Blocks
        ResourceLocation texture = modLoc("block/" + fullBlock.getId().getPath() + "_0");

        // Die wallBlock Methode nutzt diese Textur für alle Mauer-Teile (Post, Side, Tall)
        wallBlock(block.get(), texture);

        // Das Inventar-Item muss ebenfalls explizit auf die State-0 Textur verweisen
        String path = block.getId().getPath();
        simpleBlockItem(block.get(), models().wallInventory(path + "_inventory", texture));
    }

    private void blockWithItem(DeferredBlock<?> deferredBlock) {
        simpleBlockWithItem(deferredBlock.get(), cubeAll(deferredBlock.get()));
    }

    private void blockWithOtherModel(DeferredBlock<?> blockToChange, DeferredBlock<?> modelSource) {
        // Erzeugt den Blockstate und das Item-Modell basierend auf dem Modell des Quell-Blocks
        simpleBlockWithItem(blockToChange.get(), cubeAll(modelSource.get()));
    }

}