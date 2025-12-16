package mage.api.dto;

import mage.constants.PlayerAction;

public class GameActionRequest {
    private PlayerAction playerAction;
    private Object data;
    private String playerId;

    public GameActionRequest() {
    }

    public PlayerAction getPlayerAction() {
        return playerAction;
    }

    public void setPlayerAction(PlayerAction playerAction) {
        this.playerAction = playerAction;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
}

