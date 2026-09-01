package br.com.automaticminer.block;

import br.com.automaticminer.AutomaticMiner;
import br.com.automaticminer.tile.TileAutomaticMiner;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockAutomaticMiner extends BlockContainer {
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    public BlockAutomaticMiner() {
        super(Material.IRON);
        setHardness(5F);
        setResistance(10F);
        setHarvestLevel("pickaxe", 2);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileAutomaticMiner();
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state,
                                EntityLivingBase placer, ItemStack stack) {
        // A frente visual e a direção de mineração são exatamente a direção
        // horizontal para a qual o jogador estava olhando ao colocar o bloco.
        EnumFacing facing = placer.getHorizontalFacing();
        IBlockState placed = state.withProperty(FACING, facing);
        world.setBlockState(pos, placed, 3);

        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileAutomaticMiner) {
            ((TileAutomaticMiner) te).setDirection(facing);
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                    EntityPlayer player, EnumHand hand, EnumFacing side,
                                    float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileAutomaticMiner) {
                ((TileAutomaticMiner) te).setDirection(state.getValue(FACING));
                ((TileAutomaticMiner) te).setOwner(player.getUniqueID());
            }
            player.openGui(AutomaticMiner.INSTANCE, 0, world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return true;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return true;
    }

    @Override
    public net.minecraft.block.state.BlockFaceShape getBlockFaceShape(
            net.minecraft.world.IBlockAccess world, IBlockState state,
            BlockPos pos, EnumFacing face) {
        return net.minecraft.block.state.BlockFaceShape.SOLID;
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.SOLID;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing facing;
        switch (meta & 3) {
            case 1: facing = EnumFacing.EAST; break;
            case 2: facing = EnumFacing.SOUTH; break;
            case 3: facing = EnumFacing.WEST; break;
            default: facing = EnumFacing.NORTH; break;
        }
        return getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        EnumFacing facing = state.getValue(FACING);
        switch (facing) {
            case EAST: return 1;
            case SOUTH: return 2;
            case WEST: return 3;
            case NORTH:
            default: return 0;
        }
    }
}
