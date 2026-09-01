package br.com.automaticminer.tile;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import br.com.automaticminer.block.BlockAutomaticMiner;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import java.util.UUID;
import net.minecraft.util.math.BlockPos;

public class TileAutomaticMiner extends TileEntity implements ITickable, IInventory {

    private NonNullList<ItemStack> inv = NonNullList.withSize(54, ItemStack.EMPTY);

    public boolean running = false;
    public boolean down = false;
    public boolean rails = false;
    public boolean torches = false;

    public int target = 64;
    public int done = 0;
    public int energy = 0;

    private int ticks = 0;
    public EnumFacing direction = EnumFacing.NORTH;
    private UUID owner;

    @Override
    public void update() {
        if (world == null || world.isRemote || !running) return;

        if (++ticks < 10) return;
        ticks = 0;

        if (done >= target) {
            running = false;
            changed();
            return;
        }

        if (energy < 2 && !loadFuel()) {
            running = false;
            changed();
            return;
        }

        boolean moved = down ? mineDown() : mineForward();

        if (!moved) {
            running = false;
            changed();
        }
    }

    private boolean loadFuel() {
        for (int i = 0; i < 27; i++) {
            ItemStack stack = inv.get(i);
            if (stack.isEmpty()) continue;

            int value = 0;
            Item item = stack.getItem();

            if (item == Items.COAL) value = 64;
            else if (item == Items.LAVA_BUCKET) value = 256;
            else if (item == Item.getItemFromBlock(Blocks.LOG) ||
                     item == Item.getItemFromBlock(Blocks.LOG2)) value = 64;
            else if (item == Item.getItemFromBlock(Blocks.PLANKS)) value = 16;

            if (value > 0) {
                energy += value;
                stack.shrink(1);

                if (item == Items.LAVA_BUCKET) {
                    insert(new ItemStack(Items.BUCKET));
                }

                changed();
                return true;
            }
        }
        return false;
    }

    private boolean mineForward() {
        BlockPos next = pos.offset(direction);

        // Não deixa um buraco abaixo do minerador. Se houver pedra/pedregulho,
        // coloca uma ponte; se não houver material, continua mesmo assim em vez
        // de travar toda a máquina.
        if (world.isAirBlock(next.down())) {
            placeBridgeBlock(next.down());
        }

        // Passagem de 2 blocos de altura.
        if (!breakBlock(next)) return false;
        if (!breakBlock(next.up())) return false;

        if (energy < 2) return false;

        energy -= 2;
        done++;

        BlockPos old = pos;
        if (!moveTo(next)) return false;

        TileEntity te = world.getTileEntity(next);
        if (te instanceof TileAutomaticMiner) {
            TileAutomaticMiner miner = (TileAutomaticMiner) te;
            if (miner.rails) miner.placeRail(old.down());
            if (miner.torches && miner.done % 8 == 0) miner.placeTorch(old.up());
            miner.changed();
        }

        return true;
    }

    private boolean mineDown() {
        BlockPos first = pos.down();
        BlockPos second = first.down();

        if (!breakBlock(first)) return false;
        if (!breakBlock(second)) return false;
        if (energy < 2) return false;

        energy -= 2;
        done++;

        return moveTo(first);
    }

    private boolean breakBlock(BlockPos p) {
        IBlockState state = world.getBlockState(p);

        if (world.isAirBlock(p)) return true;
        if (state.getBlock() == Blocks.BEDROCK) return false;
        if (state.getBlockHardness(world, p) < 0) return false;

        NonNullList<ItemStack> drops = NonNullList.create();
        state.getBlock().getDrops(drops, world, p, state, 0);

        // Primeiro verifica se TODOS os drops cabem.
        for (ItemStack drop : drops) {
            if (!canInsert(drop.copy())) return false;
        }

        for (ItemStack drop : drops) {
            insert(drop.copy());
        }

        world.setBlockToAir(p);
        return true;
    }

    private boolean canInsert(ItemStack stack) {
        int remaining = stack.getCount();

        for (int i = 27; i < 54 && remaining > 0; i++) {
            ItemStack current = inv.get(i);

            if (current.isEmpty()) {
                remaining = 0;
                break;
            }

            if (ItemStack.areItemsEqual(current, stack) &&
                ItemStack.areItemStackTagsEqual(current, stack)) {
                int max = Math.min(getInventoryStackLimit(), current.getMaxStackSize());
                remaining -= Math.max(0, max - current.getCount());
            }
        }

        return remaining <= 0;
    }

    private boolean insert(ItemStack stack) {
        for (int i = 27; i < 54 && !stack.isEmpty(); i++) {
            ItemStack current = inv.get(i);

            if (current.isEmpty()) {
                inv.set(i, stack.copy());
                stack.setCount(0);
                return true;
            }

            if (ItemStack.areItemsEqual(current, stack) &&
                ItemStack.areItemStackTagsEqual(current, stack)) {
                int max = Math.min(getInventoryStackLimit(), current.getMaxStackSize());
                int room = max - current.getCount();
                int amount = Math.min(room, stack.getCount());

                if (amount > 0) {
                    current.grow(amount);
                    stack.shrink(amount);
                }
            }
        }
        return stack.isEmpty();
    }

    private boolean placeBridgeBlock(BlockPos p) {
        if (!world.isAirBlock(p)) return true;

        for (int i = 27; i < 54; i++) {
            ItemStack stack = inv.get(i);
            if (stack.isEmpty()) continue;

            Block block = Block.getBlockFromItem(stack.getItem());
            if (block == Blocks.COBBLESTONE || block == Blocks.STONE) {
                if (world.setBlockState(p, block.getDefaultState(), 3)) {
                    stack.shrink(1);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean take(Item item) {
        for (int i = 27; i < 54; i++) {
            ItemStack stack = inv.get(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    private void placeRail(BlockPos p) {
        if (world.isAirBlock(p) && take(Item.getItemFromBlock(Blocks.RAIL))) {
            world.setBlockState(p, Blocks.RAIL.getDefaultState(), 3);
        }
    }

    private void placeTorch(BlockPos p) {
        if (world.isAirBlock(p) && take(Item.getItemFromBlock(Blocks.TORCH))) {
            world.setBlockState(p, Blocks.TORCH.getDefaultState(), 3);
        }
    }

    private boolean moveTo(BlockPos next) {
        IBlockState myState = world.getBlockState(pos);
        if (!world.setBlockState(next, myState, 3)) return false;

        TileEntity newTe = world.getTileEntity(next);
        if (!(newTe instanceof TileAutomaticMiner)) return false;

        TileAutomaticMiner miner = (TileAutomaticMiner) newTe;
        miner.inv = this.inv;
        miner.running = this.running;
        miner.down = this.down;
        miner.rails = this.rails;
        miner.torches = this.torches;
        miner.target = this.target;
        miner.done = this.done;
        miner.energy = this.energy;
        miner.direction = this.direction;
        miner.owner = this.owner;
        miner.ticks = 0;

        world.setBlockToAir(pos);
        miner.changed();
        return true;
    }

    public void setOwner(UUID value) {
        owner = value;
        changed();
    }

    public boolean isOwner(UUID value) {
        return owner != null && owner.equals(value);
    }

    // Métodos chamados pelo pacote de rede dos botões da GUI.
    public void toggleRunning() {
        running = !running;
        changed();
    }

    public void toggleDown() {
        down = !down;
        changed();
    }

    public void toggleRails() {
        rails = !rails;
        changed();
    }

    public void setTarget(int value) {
        target = Math.max(1, Math.min(value, 100000));
        done = 0;
        changed();
    }

    public void setDirection(EnumFacing facing) {
        if (facing != null && facing.getAxis().isHorizontal()) {
            direction = facing;
            if (world != null && !world.isRemote) {
                IBlockState state = world.getBlockState(pos);
                if (state.getBlock() instanceof BlockAutomaticMiner && state.getPropertyKeys().contains(BlockAutomaticMiner.FACING)) {
                    world.setBlockState(pos, state.withProperty(BlockAutomaticMiner.FACING, facing), 3);
                }
            }
            changed();
        }
    }

    private void changed() {
        markDirty();
        if (world != null && !world.isRemote) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        readFromNBT(tag);
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 1, writeToNBT(new NBTTagCompound()));
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setBoolean("run", running);
        tag.setBoolean("down", down);
        tag.setBoolean("rails", rails);
        tag.setBoolean("torches", torches);
        tag.setInteger("target", target);
        tag.setInteger("done", done);
        tag.setInteger("energy", energy);
        tag.setInteger("dir", direction.getIndex());
        if (owner != null) { tag.setLong("ownerMost", owner.getMostSignificantBits()); tag.setLong("ownerLeast", owner.getLeastSignificantBits()); }

        NBTTagList list = new NBTTagList();
        for (int i = 0; i < inv.size(); i++) {
            if (!inv.get(i).isEmpty()) {
                NBTTagCompound itemTag = new NBTTagCompound();
                itemTag.setByte("slot", (byte) i);
                inv.get(i).writeToNBT(itemTag);
                list.appendTag(itemTag);
            }
        }
        tag.setTag("items", list);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        running = tag.getBoolean("run");
        down = tag.getBoolean("down");
        rails = tag.getBoolean("rails");
        torches = tag.getBoolean("torches");
        target = tag.getInteger("target");
        done = tag.getInteger("done");
        energy = tag.getInteger("energy");

        EnumFacing loaded = EnumFacing.getFront(tag.getInteger("dir"));
        direction = loaded.getAxis().isHorizontal() ? loaded : EnumFacing.NORTH;
        owner = tag.hasKey("ownerMost") && tag.hasKey("ownerLeast") ? new UUID(tag.getLong("ownerMost"), tag.getLong("ownerLeast")) : null;

        for (int i = 0; i < inv.size(); i++) inv.set(i, ItemStack.EMPTY);

        NBTTagList list = tag.getTagList("items", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound itemTag = list.getCompoundTagAt(i);
            int slot = itemTag.getByte("slot") & 255;
            if (slot >= 0 && slot < inv.size()) {
                inv.set(slot, new ItemStack(itemTag));
            }
        }
    }

    @Override public String getName() { return "Automatic Miner"; }
    @Override public boolean hasCustomName() { return false; }
    @Override public int getSizeInventory() { return inv.size(); }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : inv) if (!stack.isEmpty()) return false;
        return true;
    }

    @Override public ItemStack getStackInSlot(int index) { return inv.get(index); }
    @Override public ItemStack decrStackSize(int index, int count) { return ItemStackHelper.getAndSplit(inv, index, count); }
    @Override public ItemStack removeStackFromSlot(int index) { return ItemStackHelper.getAndRemove(inv, index); }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        inv.set(index, stack);
        if (!stack.isEmpty() && stack.getCount() > getInventoryStackLimit()) {
            stack.setCount(getInventoryStackLimit());
        }
        changed();
    }

    @Override public int getInventoryStackLimit() { return 64; }
    @Override public boolean isUsableByPlayer(net.minecraft.entity.player.EntityPlayer player) { return true; }
    @Override public void openInventory(net.minecraft.entity.player.EntityPlayer player) {}
    @Override public void closeInventory(net.minecraft.entity.player.EntityPlayer player) {}
    @Override public boolean isItemValidForSlot(int index, ItemStack stack) { return true; }
    @Override public int getField(int id) { return 0; }
    @Override public void setField(int id, int value) {}
    @Override public int getFieldCount() { return 0; }

    @Override
    public void clear() {
        for (int i = 0; i < inv.size(); i++) inv.set(i, ItemStack.EMPTY);
        changed();
    }
}
