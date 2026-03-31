package net.nakumaerebos.shrines.datagen;

import net.minecraft.data.PackOutput;
import net.nakumaerebos.shrines.Shrines;
import net.nakumaerebos.shrines.item.ModItems;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Shrines.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.EXAMPLE.get());
    }
}