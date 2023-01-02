package open.dolphin.client;

import javax.swing.*;
import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

/**
 * PrintablePanel.
 *
 * @author Junzo SATO
 */
public class PrintablePanel extends JPanel implements Printable {
    
    private String patientName;
    private int height;

    public PrintablePanel() {
    }

    // Junzo SATO
    public void printPanel(

            PageFormat pageFormat,
            int numOfCopies,
            boolean useDialog, String name, int height) {

        patientName = name + " カルテ";
        this.height = height;

        boolean buffered = this.isDoubleBuffered();
        this.setDoubleBuffered(false);
        //----------------------------------------------------------------------
        PrinterJob pj = PrinterJob.getPrinterJob();
        if (pj != null) {
            pj.setCopies(numOfCopies);
            pj.setJobName(patientName + " by Dolphin");
            pj.setPrintable(this, pageFormat);

            if (useDialog) {
                if (pj.printDialog()) {
                    try {
                        pj.print();
                    } catch (PrinterException printErr) {
                        printErr.printStackTrace(System.err);
                    }
                }
            } else {
                try {
                    pj.print();
                } catch (PrinterException printErr) {
                    printErr.printStackTrace(System.err);
                }
            }
        }
        //----------------------------------------------------------------------
        this.setDoubleBuffered(buffered);
    }

    @Override
    public int print(Graphics g, PageFormat pf, int pi) {

        Graphics2D g2 = (Graphics2D) g;
        Font f = new Font("Courier", Font.ITALIC, 9);
        g2.setFont(f);
        g2.setPaint(Color.black);
        g2.setColor(Color.black);

        //
        int fontHeight = g2.getFontMetrics().getHeight();
        int fontDescent = g2.getFontMetrics().getDescent();
        double footerHeight = fontHeight;
        double pageHeight = pf.getImageableHeight() - footerHeight;
        double pageWidth = pf.getImageableWidth();
        //
        double componentHeight = height == 0 ? this.getSize().getHeight() : height;
        double componentWidth = this.getSize().getWidth();

        //
        double scale = 1;
        if (componentWidth >= pageWidth) {
            scale = pageWidth / componentWidth;// shrink
        }
        //
        double scaledComponentHeight = componentHeight * scale;
        int totalNumPages = (int) Math.ceil(scaledComponentHeight / pageHeight);

        if (pi >= totalNumPages) {
            return Printable.NO_SUCH_PAGE;
        }

        // footer
        g2.translate(pf.getImageableX(), pf.getImageableY());
        String footerString = patientName + "  Page: " + (pi + 1) + " of " + totalNumPages;
        int strW = SwingUtilities.computeStringWidth(g2.getFontMetrics(), footerString);
        g2.drawString(
                footerString,
                (int) pageWidth / 2 - strW / 2,
                (int) (pageHeight + fontHeight - fontDescent)
                //(int)(pageHeight + fontHeight)
        );

        // page
        g2.translate(0f, 0f);
        g2.translate(0f, -pi * pageHeight);

        if (pi == totalNumPages - 1) {
            g2.setClip(
                    0, (int) (pageHeight * pi),
                    (int) Math.ceil(pageWidth),
                    (int) (scaledComponentHeight - pageHeight * (totalNumPages - 1))
            );
        } else {
            g2.setClip(
                    0, (int) (pageHeight * pi),
                    (int) Math.ceil(pageWidth),
                    (int) Math.ceil(pageHeight)
            );
        }

        g2.scale(scale, scale);

        boolean wasBuffered = isDoubleBuffered();
        paint(g2);
        setDoubleBuffered(wasBuffered);

        return Printable.PAGE_EXISTS;
    }
}
