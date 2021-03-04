package botmanager.frostbalance.grid;

import botmanager.frostbalance.action.ActionQueue;
import botmanager.frostbalance.grid.coordinate.Hex;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;

public abstract class TileObject extends TileData implements Renderable {

    transient private BufferedImage cachedImage;
    transient private long cachedImageDate;
    transient private long lastActiveTurn;
    transient private boolean disabled = false;
    protected ActionQueue actionQueue;

    protected TileObject(Tile tile) {
        super(tile);
    }

    public Tile getTile() {
        return tile;
    }

    public Hex getLocation() {
        return tile.getLocation();
    }

    public WorldMap getMap() {
        return tile.getMap();
    }

    @Override
    public long getCachedImageDate() {
        return cachedImageDate;
    }

    @Override
    public void setCachedImageDate(long cachedImageDate) {
        this.cachedImageDate = cachedImageDate;
    }

    @Override
    public BufferedImage getCachedImage() {
        return cachedImage;
    }

    @Override
    public void setCachedImage(BufferedImage cachedImage) {
        this.cachedImage = cachedImage;
    }

    /**
     * The defined behavior for what an object should do when the turn timer goes down.
     * @return whether or not anything changed as a result of this action.
     */
    public abstract boolean turnAction();

    public boolean doTurn(@NotNull long turn) {
        if (this.lastActiveTurn != turn && isEnabled()) {
            lastActiveTurn = turn;
            return turnAction();
        } else return false;
    }

    public ActionQueue getActionQueue() {
        return actionQueue;
    }

    public void setActionQueue(ActionQueue actionQueue) {
        this.actionQueue = actionQueue;
    }

    /**
     * Used to disable invalid objects. These will not be processed by the game.
     * @param disabled
     */
    protected void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isEnabled() {
        return !disabled;
    }
}
