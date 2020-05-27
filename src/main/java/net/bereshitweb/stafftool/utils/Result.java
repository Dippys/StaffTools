package net.bereshitweb.stafftool.utils;

public class Result {

    private int count;
    private int itemId;
    private String itemName;

    public Result(String itemName, int itemId, int count) {
        this.count = count;
        this.itemName = itemName;
        this.itemId = itemId;
    }

    public int getItemId() {
        return itemId;
    }

    public int getCount() {
        return count;
    }
    public String getItemName() {
        return itemName;
    }
}
