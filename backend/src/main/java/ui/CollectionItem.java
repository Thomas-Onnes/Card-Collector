package ui;

public class CollectionItem {

    private final int collectionId;
    private final String collectionName;
    private final String gameType;

    public CollectionItem(
            int collectionId,
            String collectionName,
            String gameType
    ) {
        this.collectionId = collectionId;
        this.collectionName = collectionName;
        this.gameType = gameType;
    }

    public int getCollectionId() {
        return collectionId;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public String getGameType() {
        return gameType;
    }
}