package open.dolphin.inspector;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
// import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import open.dolphin.client.CalendarCardPanel;
import open.dolphin.client.ClientContext;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.PhysicalModel;
import open.dolphin.infomodel.SimpleDate;
import open.dolphin.ui.IMEControl;
import open.dolphin.ui.MyJPopupMenu;

/**
 * 身長体重データを編集するエディタクラス。
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class PhysicalEditor {

    private PhysicalInspector inspector;
    private PhysicalEditorView view;
    private JDialog dialog;
    private JButton addBtn;
    private JButton clearBtn;
    private boolean ok;

    private void checkBtn() {

        boolean newOk = true;
        String height = view.getHeightFld().getText().trim();
        String weight = view.getWeightFld().getText().trim();
        String dateStr = view.getIdentifiedDateFld().getText().trim();

        if (height.equals("") && weight.equals("")) {
            newOk = false;
        } else if (dateStr.equals("")) {
            newOk = false;
        }

        if (ok != newOk) {
            ok = newOk;
            addBtn.setEnabled(ok);
            clearBtn.setEnabled(ok);
        }
    }

    private void add() {

        String h = view.getHeightFld().getText().trim();
        String w = view.getWeightFld().getText().trim();
        final PhysicalModel model = new PhysicalModel();

        if (!h.equals("")) {
            model.setHeight(h);
        }
        if (!w.equals("")) {
            model.setWeight(w);
        }

        // 同定日
        String confirmedStr = view.getIdentifiedDateFld().getText().trim();
        model.setIdentifiedDate(confirmedStr);

        addBtn.setEnabled(false);
        clearBtn.setEnabled(false);
        inspector.add(model);
    }

    private void clear() {
        view.getHeightFld().setText("");
        view.getWeightFld().setText("");
        view.getIdentifiedDateFld().setText("");
    }

    class PopupListener extends MouseAdapter implements PropertyChangeListener {

        private MyJPopupMenu popup;
        private JTextField tf;

        // private LiteCalendarPanel calendar;
        public PopupListener(JTextField tf) {
            this.tf = tf;
            tf.addMouseListener(this);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {

            if (e.isPopupTrigger()) {
                popup = new MyJPopupMenu();
                CalendarCardPanel cc = new CalendarCardPanel(ClientContext.getEventColorTable());
                cc.addPropertyChangeListener(CalendarCardPanel.PICKED_DATE, this);
                cc.setCalendarRange(new int[]{-12, 0});
                popup.insert(cc, 0);
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }

        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals(CalendarCardPanel.PICKED_DATE)) {
                SimpleDate sd = (SimpleDate) e.getNewValue();
                tf.setText(SimpleDate.simpleDateToMmldate(sd));
                popup.setVisible(false);
                popup = null;
            }
        }
    }

    public PhysicalEditor(PhysicalInspector inspector) {

        this.inspector = inspector;

        DocumentListener dl = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                checkBtn();
            }

            public void removeUpdate(DocumentEvent e) {
                checkBtn();
            }

            public void changedUpdate(DocumentEvent e) {
                checkBtn();
            }
        };

        view = new PhysicalEditorView();

        view.getHeightFld().getDocument().addDocumentListener(dl);
        view.getWeightFld().getDocument().addDocumentListener(dl);
        view.getIdentifiedDateFld().getDocument().addDocumentListener(dl);

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(IInfoModel.DATE_WITHOUT_TIME);
        String todayString = sdf.format(date);
        view.getIdentifiedDateFld().setText(todayString);
        new PopupListener(view.getIdentifiedDateFld());

//pns   view.getHeightFld().addFocusListener(AutoRomanListener.getInstance());
        IMEControl.setImeOffIfFocused(view.getHeightFld());
//pns   view.getWeightFld().addFocusListener(AutoRomanListener.getInstance());
        IMEControl.setImeOffIfFocused(view.getWeightFld());
//pns   view.getIdentifiedDateFld().addFocusListener(AutoRomanListener.getInstance());
        IMEControl.setImeOffIfFocused(view.getIdentifiedDateFld());

        addBtn = new JButton("追加");
        addBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                add();
            }
        });
        addBtn.setEnabled(false);

        clearBtn = new JButton("クリア");
        clearBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clear();
            }
        });
        clearBtn.setEnabled(false);

        Object[] options = new Object[]{addBtn,clearBtn};

        JOptionPane pane = new JOptionPane(view,
                                           JOptionPane.PLAIN_MESSAGE,
                                           JOptionPane.DEFAULT_OPTION,
                                           null,
                                           options, addBtn);
        dialog = pane.createDialog(inspector.getContext().getFrame(), ClientContext.getFrameTitle("身長体重登録"));

//pns^  dialog が開いたら WeightFld にフォーカスを当てる
        dialog.addWindowListener(new WindowAdapter(){
            @Override
            public void windowOpened(WindowEvent e) {
                // need to invokeLater in java 7
                SwingUtilities.invokeLater(new Runnable(){
                    @Override
                    public void run() {
                        view.requestFocusInWindow();
                        view.getWeightFld().requestFocusInWindow();
                    }
                });
            }
        });
//pns$

//pns^  command-w でウインドウクローズ
        InputMap im = dialog.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.META_MASK);
        im.put(key, "close-window");
        dialog.getRootPane().getActionMap().put("close-window", new AbstractAction() {
            private static final long serialVersionUID = 1L;
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
//pns$
        dialog.setVisible(true);
    }
}
