package open.dolphin.helper;

import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataNode;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/// ImageHelper.
///
/// @author kazm
/// @author pns
public class ImageHelper {
    private final static Logger logger = LoggerFactory.getLogger(ImageHelper.class);

    /// ImageIcon から BufferedImage に変換. alpha 対応.
    ///
    /// @param src source ImageIcon
    /// @return BufferedImage
    public static BufferedImage imageToBufferedImage(ImageIcon src) {
        if (src == null) {
            return null;
        }

        int width = src.getImage().getWidth(null);
        int height = src.getImage().getHeight(null);
        var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        var g = image.createGraphics();
        src.paintIcon(null, g, 0, 0);
        g.dispose();
        return image;
    }

    /// src image の幅と高さの長い方が maxDim になるように縮小する.
    ///
    /// @param src    BufferedImage
    /// @param maxDim Dimension
    /// @return resized BufferedImage
    public static BufferedImage scaleToMaxDim(BufferedImage src, int maxDim) {
        if (src.getWidth() <= maxDim && src.getHeight() <= maxDim) {
            return src;
        }

        float scale = src.getWidth(null) > src.getHeight(null)
            ? maxDim / (float) src.getWidth(null)
            : maxDim / (float) src.getHeight(null);

        // Determine size of new image. One of them should equal maxDim.
        int scaledW = (int) (scale * src.getWidth(null));
        int scaledH = (int) (scale * src.getHeight(null));

        BufferedImage resized;
        try {
            resized = Thumbnails.of(src)
                .size(scaledW, scaledH).asBufferedImage();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return resized;
    }

    /// ImageIcon のサイズを dim サイズ以内になるように調節する.
    ///
    /// @param icon ImageIcon
    /// @param dim Dimension
    /// @return adjusted ImageIcon
    public static ImageIcon adjustImageSize(ImageIcon icon, Dimension dim) {
        if ((icon.getIconHeight() <= dim.height) && (icon.getIconWidth() <= dim.width)) {
            return icon;
        }

        float hRatio = (float) icon.getIconHeight() / dim.height;
        float wRatio = (float) icon.getIconWidth() / dim.width;
        int h, w;
        if (hRatio > wRatio) {
            h = dim.height;
            w = (int) (icon.getIconWidth() / hRatio);
        } else {
            w = dim.width;
            h = (int) (icon.getIconHeight() / wRatio);
        }

        var src = imageToBufferedImage(icon);
        BufferedImage resized;
        try {
            resized = Thumbnails.of(src)
                .size(w, h)
                .asBufferedImage();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new ImageIcon(resized);
    }

    /// Convert Image to PNG ByteArray.
    ///
    /// @param image java.awt.Image
    /// @return PNG ByteArray
    public static byte[] imageToByteArray(Image image) {
        byte[] ret = null;

        try (var bo = new ByteArrayOutputStream()) {
            var d = new Dimension(image.getWidth(null), image.getHeight(null));

            var bf = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_BGR);
            Graphics g = bf.getGraphics();
            g.setColor(Color.white);
            g.drawImage(image, 0, 0, d.width, d.height, null);

            ImageIO.write(bf, "png", bo);
            ret = bo.toByteArray();

        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }

        return ret;
    }

    /// Convert JPEG ByteArray to PNG ByteArray.
    ///
    /// @param jpegBytes JPEG ByteArray
    /// @return PNG ByteArray
    public static byte[] toPngByteArray(byte[] jpegBytes) {
        byte[] ret = null;

        try (var bis = new ByteArrayInputStream(jpegBytes);
             var bos = new ByteArrayOutputStream()) {
            var bImage = ImageIO.read(bis);
            ImageIO.write(bImage, "png", bos);
            ret = bos.toByteArray();

        } catch (IOException | RuntimeException e) {
            e.printStackTrace(System.err);
        }
        return ret;
    }

    /// Extract PNG Metadata "UnknownChunk".
    /// Format Name: javax_imageio_png_1.0 (nativeMetadataFormatClassName)
    ///
    /// @param bytes PNG ByteArray
    /// @param type type (4 chars)
    /// @return value for the type
    public static String extractMetadata(byte[] bytes, String type) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
            var iis = ImageIO.createImageInputStream(bis);
            var reader = ImageIO.getImageReaders(iis).next();
            reader.setInput(iis, true);

            var metadata = reader.getImageMetadata(0);
            var root = (IIOMetadataNode) metadata.getAsTree("javax_imageio_png_1.0");

            // UnknownChunks length = 0 or 1, 1の場合その中に UnknownChunk が複数入る
            var chunksList = root.getElementsByTagName("UnknownChunks");
            if (chunksList.getLength() > 0) {
                var chunks = (IIOMetadataNode) root.getElementsByTagName("UnknownChunks").item(0);
                for (int i = 0; i < chunks.getLength(); i++) {
                    var chunk = (IIOMetadataNode) chunks.item(i);
                    var chunkType = chunk.getAttributes().getNamedItem("type").getNodeValue();
                    if (chunkType.equals(type)) {
                        return new String((byte[]) chunk.getUserObject());
                    }
                }
            }

        } catch (IOException | RuntimeException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    /// Add optional data to UnknownChunks. (Metadata = com.sun.imageio.plugins.png.PNGMetadata)
    /// "javax_imageio_png_1.0" > UnknownChunks > UnknownChunk [type (4 chars), UserObject (byte[])]
    ///
    /// @param bytes PNG ByteArray
    /// @param type type (4 chars)
    /// @param value UserObject for the type
    /// @return PNG ByteArray with added metadata
    public static byte[] addMetadata(byte[] bytes, String type, String value) {
        byte[] ret = null;

        try (var bis = new ByteArrayInputStream(bytes);
             var bos = new ByteArrayOutputStream()) {

            var iis = ImageIO.createImageInputStream(bis);
            var reader = ImageIO.getImageReaders(iis).next();
            reader.setInput(iis, true);

            // if not PNG bytes, throw exception
            if (!reader.getFormatName().equals("png")) {
                throw new IOException("not png bytes");
            }

            var image = reader.readAll(0, null);

            // preparing nodes
            var root = new IIOMetadataNode("javax_imageio_png_1.0");
            var chunks = new IIOMetadataNode("UnknownChunks");
            var chunk = new IIOMetadataNode("UnknownChunk");

            chunk.setAttribute("type", type);
            chunk.setUserObject(value.getBytes());

            chunks.appendChild(chunk);
            root.appendChild(chunks);

            var writer = ImageIO.getImageWritersByFormatName("png").next();
            var param = writer.getDefaultWriteParam();
            var typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
            var metadata = writer.getDefaultImageMetadata(typeSpecifier, param);

            // mergeNativeTree(root)
            metadata.mergeTree("javax_imageio_png_1.0", root);
            image.setMetadata(metadata);

            var ios = ImageIO.createImageOutputStream(bos);
            writer.setOutput(ios);
            writer.write(metadata, image, param);

            ret = bos.toByteArray();

        } catch (IOException | RuntimeException e) {
            logger.error(e.getMessage());
        }
        return ret;
    }

    /// Show all Nodes.
    ///
    /// @param node node to show
    private static void showNode(IIOMetadataNode node) {
        IO.println("node name = " + node.getNodeName());
        IO.println("node value = " + node.getNodeValue());
        IO.println("node type = " + node.getNodeType());
        IO.println("node attribute size = " + node.getAttributes().getLength());

        for (int i=0; i<node.getAttributes().getLength(); i++) {
            IO.println("attribute node name = " + node.getAttributes().item(i).getNodeName());
            IO.println("attribute node value = " + node.getAttributes().item(i).getNodeValue());
            IO.println("attribute node type = " + node.getAttributes().item(i).getNodeType());
        }

        int len = node.getChildNodes().getLength();
        if (len > 0) {
            for (int i=0; i<len; i++) {
                showNode((IIOMetadataNode) node.getChildNodes().item(i));
            }
        }
    }

    static void main() {
        String sample1 = "/schemaeditor/Sample-square.JPG";

        byte[] buf = null;
        try (var in = ImageHelper.class.getResourceAsStream(sample1)) {
            int n = in.available();
            buf = new byte[n];
            for (int i = 0; i < n; i++) buf[i] = (byte) in.read();
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }

        byte[] pngBytes = toPngByteArray(buf);

        var type = "DSIZ";
        var value = "100x200";

        byte[] buf2 = addMetadata(pngBytes, type, value);
        var val = extractMetadata(buf2, type);

        IO.println("type = " + type);
        IO.println("value = " + val);
    }
}
