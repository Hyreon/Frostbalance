package botmanager.frostbalance.grid;

import botmanager.Utilities;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public interface Renderable {

    default BufferedImage getImage() throws IOException {
        if (isImageCacheValid()) return getCachedImage();
        else {
            InputStream renderStream = getRender();
            if (renderStream == null) {
                return null;
            } else {
                setCachedImage(ImageIO.read(getRender()));
                setCachedImageDate(Utilities.todayAsLong());
                return getCachedImage();
            }
        }
    }

    default boolean isImageCacheValid() {
        return getCachedImage() != null && getCachedImageDate() >= Utilities.todayAsLong();
    }

    default void invalidateCache() {
        setCachedImage(null);
    }

    long getCachedImageDate();

    void setCachedImageDate(long todayAsLong);

    BufferedImage getCachedImage();

    void setCachedImage(BufferedImage read);

    @Nullable
    InputStream getRender();

}
