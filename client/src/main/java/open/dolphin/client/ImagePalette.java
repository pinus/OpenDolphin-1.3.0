package open.dolphin.client;

import open.dolphin.dnd.ImageEntryTransferHandler;
import open.dolphin.helper.ImageHelper;
import open.dolphin.helper.MouseHelper;
import open.dolphin.helper.WindowHolder;
import open.dolphin.ui.PNSBorderFactory;
import open.dolphin.ui.PNSScrollPane;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * ImagePalette.
 *
 * @author Minagawa, Kazushi
 * @author pns
 */
public class ImagePalette extends JPanel {

    private static final int DEFAULT_COLUMN_COUNT = 3;
    private static final int DEFAULT_IMAGE_WIDTH = 120;
    private static final int DEFAULT_IMAGE_HEIGHT = 120;
    private static final String[] DEFAULT_IMAGE_SUFFIX = {".jpg"};
    private final Border selectedBorder = PNSBorderFactory.createSelectedBorder();
    // private Border normalBorder = PNSBorderFactory.createClearBorder();
    private final Border normalBorder = BorderFactory.createEmptyBorder();
    private final ImageTableModel imageTableModel;
    private final int imageWidth;
    private final int imageHeight;
    private JTable imageTable;
    private File imageDirectory;
    private String[] suffix = DEFAULT_IMAGE_SUFFIX;
    private boolean showHeader;

    public ImagePalette(String[] columnNames, int columnCount, int width, int height) {
        imageTableModel = new ImageTableModel(columnNames, columnCount);
        imageWidth = width;
        imageHeight = height;
        initComponent(columnCount);
    }

    public ImagePalette() {
        this(null, DEFAULT_COLUMN_COUNT, DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT);
    }

    public List<ImageEntry> getImageList() {
        return imageTableModel.getImageList();
    }

    public void setImageList(List<ImageEntry> list) {
        imageTableModel.setImageList(list);
    }

    public JTable getable() {
        return imageTable;
    }

    public String[] getimageSuffix() {
        return suffix;
    }

    public void setImageSuffix(String[] suffix) {
        this.suffix = suffix;
    }

    public File getImageDirectory() {
        return imageDirectory;
    }

    public void setImageDirectory(File imageDirectory) {
        this.imageDirectory = imageDirectory;
        refresh();
    }

    public void dispose() {
        if (imageTableModel != null) {
            imageTableModel.clear();
        }
    }

    public void refresh() {

        if ((!imageDirectory.exists()) || (!imageDirectory.isDirectory())) {
            return;
        }

        Dimension imageSize = new Dimension(imageWidth, imageHeight);
        File[] imageFiles = listImageFiles(imageDirectory, suffix);
        if (imageFiles != null && imageFiles.length > 0) {
            List<ImageEntry> imageList = new ArrayList<>();
            for (File imageFile : imageFiles) {
                try {
                    URL url = imageFile.toURI().toURL();
                    ImageIcon icon = new ImageIcon(url);
                    ImageEntry entry = new ImageEntry();
                    entry.setImageIcon(ImageHelper.adjustImageSize(icon, imageSize));
                    entry.setUrl(url.toString());
                    imageList.add(entry);

                } catch (MalformedURLException e) {
                    e.printStackTrace(System.err);
                }
            }
            imageList.sort(Comparator.comparing(ImageEntry::getUrl));
            imageTableModel.setImageList(imageList);
        }
    }

    private void initComponent(int columnCount) {

        // Image table を生成する
        imageTable = new JTable(imageTableModel);
        imageTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        imageTable.setCellSelectionEnabled(true);
        imageTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        ImageEntryTransferHandler handler = new ImageEntryTransferHandler();
        imageTable.setTransferHandler(handler);

        // ドラッグ処理
        imageTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                handler.exportAsDrag((JComponent) e.getSource(), e, TransferHandler.COPY);
            }
        });
        // 素早い操作でドラッグ開始マウス位置がドラッグ方向に少しずれてしまうのの workaround
        imageTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                int col = imageTable.columnAtPoint(p);
                int row = imageTable.rowAtPoint(p);
                handler.setTargetRowColumn(row, col);
            }
        });

        // ダブルクリックで直接入力
        imageTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getClickCount() == 2 && !MouseHelper.mouseMoved()) {
                    int row = imageTable.getSelectedRow();
                    int col = imageTable.getSelectedColumn();
                    ImageEntry entry = (ImageEntry) imageTable.getModel().getValueAt(row, col);

                    List<EditorFrame> allFrames = WindowHolder.allEditorFrames();
                    if (!allFrames.isEmpty()) {
                        EditorFrame frame = allFrames.getFirst();
                        KartePane pane = frame.getEditor().getSOAPane();
                        // caret を最後に送ってから import する
                        JTextPane textPane = pane.getTextPane();
                        KarteStyledDocument doc = pane.getDocument();
                        textPane.setCaretPosition(doc.getLength());
                        // import
                        pane.imageEntryDropped(entry);
                    }
                }
            }
        });

        for (int i = 0; i < columnCount; i++) {
            imageTable.getColumnModel().getColumn(i).setPreferredWidth(imageWidth);
        }
        imageTable.setRowHeight(imageHeight);

        ImageRenderer imageRenderer = new ImageRenderer();
        imageRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        imageTable.setDefaultRenderer(java.lang.Object.class, imageRenderer);

        setLayout(new BorderLayout());
        PNSScrollPane scroller = new PNSScrollPane();

        if (showHeader) {
            scroller.setViewportView(imageTable);
            this.add(scroller);
        } else {
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(imageTable);
            scroller.setViewportView(panel);
            this.add(scroller);
        }

        imageTable.setIntercellSpacing(new Dimension(0, 0));
    }

    private File[] listImageFiles(File dir, String[] suffix) {
        ImageFileFilter filter = new ImageFileFilter(suffix);
        return dir.listFiles(filter);
    }

    private class ImageFileFilter implements FilenameFilter {
        private final String[] suffix;

        public ImageFileFilter(String[] suffix) {
            this.suffix = suffix;
        }

        @Override
        public boolean accept(File dir, String name) {
            return Stream.of(suffix).anyMatch(name.toLowerCase()::endsWith);
        }
    }

    private class ImageRenderer extends DefaultTableCellRenderer {

        public ImageRenderer() {
            setVerticalTextPosition(JLabel.BOTTOM);
            setHorizontalTextPosition(JLabel.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean isFocused, int row, int col) {
            Component compo = super.getTableCellRendererComponent(table, value, isSelected, isFocused, row, col);
            JLabel label = (JLabel) compo;

            label.setBackground(Color.WHITE);
            label.setBorder(isSelected ? selectedBorder : normalBorder);
            ImageEntry entry = (ImageEntry) value;
            label.setIcon(Objects.nonNull(entry) ? entry.getImageIcon() : null);
            label.setText(null);

            return compo;
        }
    }
}
