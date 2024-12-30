package game.entity;

public class Inventory {
    public static final int HOTBAR_SIZE = 9;
    public static final int INVENTORY_SIZE = 27;
    
    private ItemStack[] items;
    
    public Inventory() {
        items = new ItemStack[HOTBAR_SIZE + INVENTORY_SIZE];
    }
    
    public boolean addItem(ItemStack stack) {
        // First try to stack with existing items
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null && items[i].canStackWith(stack)) {
                int spaceLeft = ItemStack.MAX_STACK_SIZE - items[i].getAmount();
                if (spaceLeft > 0) {
                    int amountToAdd = Math.min(spaceLeft, stack.getAmount());
                    items[i].increase(amountToAdd);
                    stack.decrease(amountToAdd);
                    
                    if (stack.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        
        // Then try to find empty slot
        for (int i = 0; i < items.length; i++) {
            if (items[i] == null || items[i].isEmpty()) {
                items[i] = stack;
                return true;
            }
        }
        
        return false;
    }
    
    public ItemStack getItem(int slot) {
        if (slot >= 0 && slot < items.length) {
            return items[slot];
        }
        return null;
    }
    
    public void setItem(int slot, ItemStack stack) {
        if (slot >= 0 && slot < items.length) {
            items[slot] = stack;
        }
    }
    
    public void clear() {
        for (int i = 0; i < items.length; i++) {
            items[i] = null;
        }
    }
    
    public ItemStack[] getHotbar() {
        ItemStack[] hotbar = new ItemStack[HOTBAR_SIZE];
        System.arraycopy(items, 0, hotbar, 0, HOTBAR_SIZE);
        return hotbar;
    }
}
