package RDMFighter;

public enum Food {
    ROCKTAIL("Rocktail", 14617),
    MANTA_RAY("Manta Ray", 392),
    SHARK("Shark", 386),
    LOBSTER("Lobster", 380);
    
    final String name;
    final int itemID;
    
    private Food(String name, int itemID) {
        this.name = name;
        this.itemID = itemID;
    }
}