package org.l2jmobius.gameserver.model.holders;

/**
 * Written by Berezkin Nikolay, on 04.05.2021
 */
public class PlayerCollectionData {
    public int getCollectionId() {
        return collectionId;
    }

    public int getItemId() {
        return itemId;
    }

    public int getIndex() {
        return index;
    }

    private final int collectionId, itemId, index;

    public PlayerCollectionData(int collectionId, int itemId, int index) {
        this.collectionId = collectionId;
        this.itemId = itemId;
        this.index = index;
    }
}