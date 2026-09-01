package br.com.automaticminer.init;

import br.com.automaticminer.AutomaticMiner;
import br.com.automaticminer.block.BlockAutomaticMiner;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

public class ModBlocks {
    public static final BlockAutomaticMiner MINER=new BlockAutomaticMiner();

    public static void init(){
        IForgeRegistry<net.minecraft.block.Block> br=GameRegistry.findRegistry(net.minecraft.block.Block.class);
        MINER.setRegistryName(AutomaticMiner.MODID,"automatic_miner");
        MINER.setUnlocalizedName(AutomaticMiner.MODID+".automatic_miner");
        br.register(MINER);

        IForgeRegistry<net.minecraft.item.Item> ir=GameRegistry.findRegistry(net.minecraft.item.Item.class);
        ItemBlock item=new ItemBlock(MINER);
        item.setRegistryName(MINER.getRegistryName());
        ir.register(item);

        GameRegistry.addShapedRecipe(MINER.getRegistryName(),null,
            new net.minecraft.item.ItemStack(MINER),
            "III","IDI","III",
            'I',net.minecraft.init.Items.IRON_INGOT,
            'D',net.minecraft.init.Items.DIAMOND);
    }
}
