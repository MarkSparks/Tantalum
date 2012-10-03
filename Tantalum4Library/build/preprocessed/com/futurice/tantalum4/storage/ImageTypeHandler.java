package com.futurice.tantalum4.storage;

import com.futurice.tantalum4.log.L;
import com.futurice.tantalum4.util.ImageUtils;
import javax.microedition.lcdui.Image;

/**
 * This is a helper class for creating an image class. It automatically converts
 * the byte[] to an Image as the data is loaded from the network or cache.
 *
 * @author tsaa
 */
public final class ImageTypeHandler implements DataTypeHandler {
    private final int imageWidth;
    private final boolean processAlpha;
    private final boolean bestQuality;

    /**
     * Create a non-scaling image cache
     * 
     */
    public ImageTypeHandler() {
        imageWidth = -1;
        this.processAlpha = false;
        this.bestQuality = false;
    }

    /**
     * Create an image cache which scales images on load into memory
     * 
     * @param processAlpha
     * @param bestQuality
     * @param width 
     */
    public ImageTypeHandler(final boolean processAlpha, final boolean bestQuality, final int width) {
        imageWidth = width;
        this.processAlpha = processAlpha;
        this.bestQuality = bestQuality;
    }

    public Object convertToUseForm(final byte[] bytes) {
        final Image img;
        
        try {
            if (imageWidth == -1) {
                //#debug
                L.i("convert image", "length=" + bytes.length);
                img = Image.createImage(bytes, 0, bytes.length);
            } else {                
                Image temp = Image.createImage(bytes, 0, bytes.length);
                final int w = temp.getWidth();
                final int h = temp.getHeight();
                int[] data = new int[w*h];
                temp.getRGB(data, 0, w, 0, 0, w, h);
                temp = null;
                img = ImageUtils.downscaleImage(data, w, h, imageWidth, imageWidth, true, processAlpha, bestQuality);
                data = null;
            }
        } catch (IllegalArgumentException e) {
            //#debug
            L.e("Exception converting bytes to image", "image byte length=" + bytes.length, e);
            throw e;
        }
        
        return img;
    }
}
