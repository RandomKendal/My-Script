package rdmfighter.data;

public enum EnemyNpc {
    GOBLIN("Goblin", 101),
    ROCK_CRAB("Rock Crab", new int[]{ 1265, 1267 }),
    MOUNTAIN_TROLL("Mountain Troll", new int[]{ 1106, 1110 }),
    TZHAAR_KET("TzHaar-Ket", 2610),
    BLUE_DRAGON("Blue Dragon", 55);
    
    public final String name;
    public final int[] npcId;
    
    private EnemyNpc(String name, int[] npcId) {
        this.name = name;
        this.npcId = npcId;
    }
    
    private EnemyNpc(String name, int npcId) {
        this.name = name;
        this.npcId = new int[]{ npcId };
    }
}