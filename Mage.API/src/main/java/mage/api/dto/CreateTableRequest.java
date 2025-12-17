package mage.api.dto;

public class CreateTableRequest {
    private String name;
    private String gameType;
    private String deckType;
    private boolean limited;
    private boolean multiPlayer = true; // Por defecto multiplayer
    private String password;
    private int winsNeeded;
    private int freeMulligans;

    public CreateTableRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public String getDeckType() {
        return deckType;
    }

    public void setDeckType(String deckType) {
        this.deckType = deckType;
    }

    public boolean isLimited() {
        return limited;
    }

    public void setLimited(boolean limited) {
        this.limited = limited;
    }

    public boolean isMultiPlayer() {
        return multiPlayer;
    }

    public void setMultiPlayer(boolean multiPlayer) {
        this.multiPlayer = multiPlayer;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getWinsNeeded() {
        return winsNeeded;
    }

    public void setWinsNeeded(int winsNeeded) {
        this.winsNeeded = winsNeeded;
    }

    public int getFreeMulligans() {
        return freeMulligans;
    }

    public void setFreeMulligans(int freeMulligans) {
        this.freeMulligans = freeMulligans;
    }
}

