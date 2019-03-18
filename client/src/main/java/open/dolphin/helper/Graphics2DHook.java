package open.dolphin.helper;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

public class Graphics2DHook extends Graphics2D {
    private Graphics2D g2d;

    public Graphics2DHook(Graphics g) {
        this.g2d = (Graphics2D) g;
    }

    public Graphics2D getG2d() {
        return g2d;
    }

    @Override
    public void draw(Shape s) {
        g2d.draw(s);
    }

    @Override
    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
        return g2d.drawImage(img, xform, obs);
    }

    @Override
    public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
        g2d.drawImage(img, op, x, y);
    }

    @Override
    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
        g2d.drawRenderedImage(img, xform);
    }

    @Override
    public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
        g2d.drawRenderableImage(img, xform);
    }

    @Override
    public void drawString(String str, int x, int y) {
        g2d.drawString(str, x, y);
    }

    @Override
    public void drawString(String str, float x, float y) {
        g2d.drawString(str, x, y);
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator, int x, int y) {
        g2d.drawString(iterator, x, y);
    }

    @Override
    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        return g2d.drawImage(img, x, y, observer);
    }

    @Override
    public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
        return g2d.drawImage(img, x, y, width, height, observer);
    }

    @Override
    public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
        return g2d.drawImage(img, x, y, bgcolor, observer);
    }

    @Override
    public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) {
        return g2d.drawImage(img, x, y, width, height, bgcolor, observer);
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
        return g2d.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer) {
        return g2d.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
    }

    @Override
    public void dispose() {
        g2d.dispose();
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator, float x, float y) {
        g2d.drawString(iterator, x, y);
    }

    @Override
    public void drawGlyphVector(GlyphVector g, float x, float y) {
        g2d.drawGlyphVector(g, x, y);
    }

    @Override
    public void fill(Shape s) {
        g2d.fill(s);
    }

    @Override
    public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
        return g2d.hit(rect, s, onStroke);
    }

    @Override
    public GraphicsConfiguration getDeviceConfiguration() {
        return g2d.getDeviceConfiguration();
    }

    @Override
    public void setRenderingHint(RenderingHints.Key hintKey, Object hintValue) {
        g2d.setRenderingHint(hintKey, hintValue);
    }

    @Override
    public Object getRenderingHint(RenderingHints.Key hintKey) {
        return g2d.getRenderingHint(hintKey);
    }

    @Override
    public void addRenderingHints(Map<?, ?> hints) {
        g2d.addRenderingHints(hints);
    }

    @Override
    public RenderingHints getRenderingHints() {
        return g2d.getRenderingHints();
    }

    @Override
    public void setRenderingHints(Map<?, ?> hints) {
        g2d.setRenderingHints(hints);
    }

    @Override
    public Graphics create() {
        return g2d.create();
    }

    @Override
    public void translate(int x, int y) {
        g2d.translate(x, y);
    }

    @Override
    public Color getColor() {
        return g2d.getColor();
    }

    @Override
    public void setColor(Color c) {
        g2d.setColor(c);
    }

    @Override
    public void setPaintMode() {
        g2d.setPaintMode();
    }

    @Override
    public void setXORMode(Color c1) {
        g2d.setXORMode(c1);
    }

    @Override
    public Font getFont() {
        return g2d.getFont();
    }

    @Override
    public void setFont(Font font) {
        g2d.setFont(font);
    }

    @Override
    public FontMetrics getFontMetrics(Font f) {
        return g2d.getFontMetrics(f);
    }

    @Override
    public Rectangle getClipBounds() {
        return g2d.getClipBounds();
    }

    @Override
    public void clipRect(int x, int y, int width, int height) {
        g2d.clipRect(x, y, width, height);
    }

    @Override
    public void setClip(int x, int y, int width, int height) {
        g2d.setClip(x, y, width, height);
    }

    @Override
    public Shape getClip() {
        return g2d.getClip();
    }

    @Override
    public void setClip(Shape clip) {
        g2d.setClip(clip);
    }

    @Override
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        g2d.copyArea(x, y, width, height, dx, dy);
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {
        g2d.drawLine(x1, y1, x2, y2);
    }

    @Override
    public void fillRect(int x, int y, int width, int height) {
        g2d.fillRect(x, y, width, height);
    }

    @Override
    public void clearRect(int x, int y, int width, int height) {
        g2d.clearRect(x, y, width, height);
    }

    @Override
    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        g2d.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    @Override
    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        g2d.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    @Override
    public void drawOval(int x, int y, int width, int height) {
        g2d.drawOval(x, y, width, height);
    }

    @Override
    public void fillOval(int x, int y, int width, int height) {
        g2d.fillOval(x, y, width, height);
    }

    @Override
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        g2d.drawArc(x, y, width, height, startAngle, arcAngle);
    }

    @Override
    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        g2d.fillArc(x, y, width, height, startAngle, arcAngle);
    }

    @Override
    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
        g2d.drawPolyline(xPoints, yPoints, nPoints);
    }

    @Override
    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        g2d.drawPolyline(xPoints, yPoints, nPoints);
    }

    @Override
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        g2d.fillPolygon(xPoints, yPoints, nPoints);
    }

    @Override
    public void translate(double tx, double ty) {
        g2d.translate(tx, ty);
    }

    @Override
    public void rotate(double theta) {
        g2d.rotate(theta);
    }

    @Override
    public void rotate(double theta, double x, double y) {
        g2d.rotate(theta, x, y);
    }

    @Override
    public void scale(double sx, double sy) {
        g2d.scale(sx, sy);
    }

    @Override
    public void shear(double shx, double shy) {
        g2d.shear(shx, shy);
    }

    @Override
    public void transform(AffineTransform Tx) {
        g2d.transform(Tx);
    }

    @Override
    public AffineTransform getTransform() {
        return g2d.getTransform();
    }

    @Override
    public void setTransform(AffineTransform Tx) {
        g2d.setTransform(Tx);
    }

    @Override
    public Paint getPaint() {
        return g2d.getPaint();
    }

    @Override
    public void setPaint(Paint paint) {
        g2d.setPaint(paint);
    }

    @Override
    public Composite getComposite() {
        return g2d.getComposite();
    }

    @Override
    public void setComposite(Composite comp) {
        g2d.setComposite(comp);
    }

    @Override
    public Color getBackground() {
        return g2d.getBackground();
    }

    @Override
    public void setBackground(Color color) {
        g2d.setBackground(color);
    }

    @Override
    public Stroke getStroke() {
        return g2d.getStroke();
    }

    @Override
    public void setStroke(Stroke s) {
        g2d.setStroke(s);
    }

    @Override
    public void clip(Shape s) {
        g2d.clip(s);
    }

    @Override
    public FontRenderContext getFontRenderContext() {
        return g2d.getFontRenderContext();
    }
}
