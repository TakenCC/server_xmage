package mage.api.dto;

import mage.cards.decks.DeckCardLists;
import mage.players.PlayerType;

public class JoinTableRequest {
    private String name;
    private PlayerType playerType;
    private int skill;
    private DeckCardLists deckList;
    private String password;

    public JoinTableRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PlayerType getPlayerType() {
        return playerType;
    }

    public void setPlayerType(PlayerType playerType) {
        this.playerType = playerType;
    }

    public int getSkill() {
        return skill;
    }

    public void setSkill(int skill) {
        this.skill = skill;
    }

    public DeckCardLists getDeckList() {
        return deckList;
    }

    public void setDeckList(DeckCardLists deckList) {
        this.deckList = deckList;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

