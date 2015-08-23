package rdmfighter.data;

public enum Food {
    ROCKTAIL("Rocktail", 14617),
    MANTA_RAY("Manta Ray", 392),
    SHARK("Shark", 386),
    LOBSTER("Lobster", 380);
    
    public final String name;
    public final int itemID;
    
    private Food(String name, int itemID) {
        this.name = name;
        this.itemID = itemID;
    }
}