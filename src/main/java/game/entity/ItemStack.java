package game.entity;

public class ItemStack {
    public static final int MAX_STACK_SIZE = 64;
    
    private byte blockType;
    private int amount;
    
    public ItemStack(byte blockType, int amount) {
        this.blockType = blockType;
        this.amount = Math.min(amount, MAX_STACK_SIZE);
    }
    
    public boolean canStackWith(ItemStack other) {
        return other != null && other.blockType == this.blockType;
    }
    
    public void increase(int amount) {
        this.amount = Math.min(this.amount + amount, MAX_STACK_SIZE);
    }
    
    public void decrease(int amount) {
        this.amount = Math.max(0, this.amount - amount);
    }
    
    public void decrease() {
        decrease(1);
    }
    
    public boolean isEmpty() {
        return amount <= 0;
    }
    
    public byte getBlockType() {
        return blockType;
    }
    
    public int getAmount() {
        return amount;
    }
}
