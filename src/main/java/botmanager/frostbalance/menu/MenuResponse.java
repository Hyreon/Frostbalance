package botmanager.frostbalance.menu;

public abstract class MenuResponse {

    String emoji;
    String name;

    MenuResponse(String emoji, String name) {
        this.emoji = emoji;
        this.name = name;
    }

    public void applyReaction() {
        if (validConditions()) reactEvent();
    }

    public abstract void reactEvent();

    public abstract boolean validConditions();

}
