package open.dolphin.inspector;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
// import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import open.dolphin.ui.AdditionalTableSettings;
import open.dolphin.client.Chart;
import open.dolphin.client.ChartImpl;
import open.dolphin.client.ClientContext;
import open.dolphin.client.GUIConst;
import open.dolphin.delegater.DocumentDelegater;
import open.dolphin.helper.DBTask;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.ModelUtils;
import open.dolphin.infomodel.ObservationModel;
import open.dolphin.infomodel.PhysicalModel;
import open.dolphin.project.Project;
import open.dolphin.table.IndentTableCellRenderer;
import open.dolphin.table.ObjectReflectTableModel;
//pns  import open.dolphin.table.OddEvenRowRenderer;
import open.dolphin.ui.MyJPopupMenu;
import org.apache.log4j.Logger;

/**
 * 身長体重インスペクタクラス.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class PhysicalInspector {

    private ObjectReflectTableModel tableModel;

    private PhysicalView view;

    private ChartImpl context;

    private Logger logger;

    /**
     * PhysicalInspectorオブジェクトを生成する.
     */
    public PhysicalInspector(ChartImpl context) {
        this.context = context;
        logger = ClientContext.getBootLogger();
        initComponents();
        update();
    }

    public Chart getContext() {
        return context;
    }

    public void clear() {
        tableModel.clear();
    }

    /**
     * レイアウトパネルを返す.
     * @return レイアウトパネル
     */
    public JPanel getPanel() {
        return (JPanel) view;
    }

    /**
     * GUIコンポーネントを初期化する.
     */
    private void initComponents() {

        view = new PhysicalView();

         // カラム名
        String[] columnNames = ClientContext.getStringArray("patientInspector.physicalInspector.columnNames"); // {"身長","体重","BMI","測定日"};

        // テーブルの初期行数
        int startNumRows = ClientContext.getInt("patientInspector.physicalInspector.startNumRows");

        // 属性値を取得するためのメソッド名
        String[] methodNames = ClientContext.getStringArray("patientInspector.physicalInspector.methodNames"); // {"getHeight","getWeight","getBMI","getConfirmDate"};

        // 身長体重テーブルを生成する
        tableModel = new ObjectReflectTableModel(columnNames, startNumRows, methodNames, null);
        view.getTable().setModel(tableModel);
//pns   view.getTable().setDefaultRenderer(Object.class, new OddEvenRowRenderer());
        view.getTable().setRowHeight(GUIConst.DEFAULT_TABLE_ROW_HEIGHT);
        view.getTable().setDefaultRenderer(Object.class, new IndentTableCellRenderer(IndentTableCellRenderer.NARROW));
        view.getTable().getColumnModel().getColumn(2).setCellRenderer(new BMIRenderer());
        view.getTable().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 列幅を調整する カット&トライ
        int[] cellWidth = new int[]{50,50,50,110};
        for (int i = 0; i < cellWidth.length; i++) {
            TableColumn column = view.getTable().getColumnModel().getColumn(i);
            column.setPreferredWidth(cellWidth[i]);
        }

        // 右クリックによる追加削除のメニューを登録する
//pns^
        MouseAdapter ma = new MouseAdapter() {
            private void mabeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
//                  isReadOnly対応
                    if (context.isReadOnly()) return;

                    MyJPopupMenu pop = new MyJPopupMenu();
                    JMenuItem item = new JMenuItem("追加");
                    item.setIcon(GUIConst.ICON_LIST_ADD_16);
                    pop.add(item);
                    item.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            new PhysicalEditor(PhysicalInspector.this);
                        }
                    });
                    final int row = view.getTable().rowAtPoint(e.getPoint());
                    if (tableModel.getObject(row) != null) {
                        pop.add(new JSeparator());
                        JMenuItem item2 = new JMenuItem("削除");
                        item2.setIcon(GUIConst.ICON_LIST_REMOVE_16);
                        pop.add(item2);
                        item2.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                delete(row);
                            }
                        });
                    }
                    pop.show(e.getComponent(), e.getX(), e.getY());
                }
            }
            @Override
            public void mousePressed(MouseEvent e) {
//pns^
            if(e.getComponent() == view.getTable().getParent()) {
                view.getTable().clearSelection();
                e.getComponent().requestFocusInWindow();
            }
//pns$
                mabeShowPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mabeShowPopup(e);
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    new PhysicalEditor(PhysicalInspector.this);
                }
            }
        };
        view.getTable().addMouseListener(ma);

//pns   動的 startNumRows 調節のための設定
        // tableModel.setTable(view.getTable());
        AdditionalTableSettings.setTable(view.getTable(), ma);
    }

    private void scroll(boolean ascending) {

        int cnt = tableModel.getObjectCount();
        if (cnt > 0) {
            int row = 0;
            if (ascending) {
                row = cnt - 1;
            }
            Rectangle r = view.getTable().getCellRect(row, row, true);
            view.getTable().scrollRectToVisible(r);
        }
    }

    /**
     * 身長体重データを表示する.
     */
    public void update() {
        List list = context.getKarte().getPhysicalEntry();

        if (list.isEmpty()) {
            return;
        }

        boolean asc = Project.getPreferences().getBoolean(Project.DOC_HISTORY_ASCENDING, false);
        if (asc) {
            Collections.sort(list);
        } else {
            Collections.sort(list, Collections.reverseOrder());
        }

        tableModel.setObjectList(list);
        scroll(asc);
    }

    /**
     * 身長体重データを追加する.
     */
    public void add(final PhysicalModel model) {

        // 同定日
        String confirmedStr = model.getIdentifiedDate();
        Date confirmed = ModelUtils.getDateTimeAsObject(confirmedStr + "T00:00:00");

        // 記録日
        Date recorded = new Date();

        final List<ObservationModel> addList = new ArrayList<ObservationModel>(2);

        if (model.getHeight() != null) {
            ObservationModel observation = new ObservationModel();
            observation.setKarte(context.getKarte());
            observation.setCreator(Project.getUserModel());
            observation.setObservation(IInfoModel.OBSERVATION_PHYSICAL_EXAM);
            observation.setPhenomenon(IInfoModel.PHENOMENON_BODY_HEIGHT);
            observation.setValue(model.getHeight());
            observation.setUnit(IInfoModel.UNIT_BODY_HEIGHT);
            observation.setConfirmed(confirmed);        // 確定（同定日）
            observation.setStarted(confirmed);          // 適合開始日
            observation.setRecorded(recorded);          // 記録日
            observation.setStatus(IInfoModel.STATUS_FINAL);
            //observation.setMemo(model.getMemo());
            addList.add(observation);
        }

        if (model.getWeight() != null) {

            ObservationModel observation = new ObservationModel();
            observation.setKarte(context.getKarte());
            observation.setCreator(Project.getUserModel());
            observation.setObservation(IInfoModel.OBSERVATION_PHYSICAL_EXAM);
            observation.setPhenomenon(IInfoModel.PHENOMENON_BODY_WEIGHT);
            observation.setValue(model.getWeight());
            observation.setUnit(IInfoModel.UNIT_BODY_WEIGHT);
            observation.setConfirmed(confirmed);        // 確定（同定日）
            observation.setStarted(confirmed);          // 適合開始日
            observation.setRecorded(recorded);          // 記録日
            observation.setStatus(IInfoModel.STATUS_FINAL);
            //observation.setMemo(model.getMemo());
            addList.add(observation);
        }

        if (addList.size() == 0) {
            return;
        }

        DBTask task = new DBTask<List<Long>>(context) {

            @Override
            protected List<Long> doInBackground() throws Exception {
                logger.debug("physical add doInBackground");
                DocumentDelegater pdl = new DocumentDelegater();
                List<Long> ids = pdl.addObservations(addList);
                return ids;
            }

            @Override
            protected void succeeded(List<Long> result) {
                logger.debug("physical add succeeded");
                if (model.getHeight() != null && model.getWeight() != null) {
                    model.setHeightId(result.get(0));
                    model.setWeightId(result.get(1));
                } else if (model.getHeight() != null) {
                    model.setHeightId(result.get(0));
                } else {
                    model.setWeightId(result.get(0));
                }
                boolean asc = Project.getPreferences().getBoolean(Project.DOC_HISTORY_ASCENDING, false);
                if (asc) {
                    tableModel.addRow(model);
                } else {
                    tableModel.addRow(0, model);
                }
                scroll(asc);
            }
        };

        task.execute();
    }

    /**
     * テーブルで選択した身長体重データを削除する.
     */
    public void delete(final int row) {

        PhysicalModel model = (PhysicalModel) tableModel.getObject(row);
        if (model == null) {
            return;
        }

        final List<Long> list = new ArrayList<Long>(2);

        if (model.getHeight() != null) {
//pns       list.add(new Long(model.getHeightId()));
            list.add(Long.valueOf(model.getHeightId()));
        }

        if (model.getWeight() != null) {
//pns       list.add(new Long(model.getWeightId()));
            list.add(Long.valueOf(model.getWeightId()));
        }

        DBTask task = new DBTask<Void>(context) {

            @Override
            protected Void doInBackground() throws Exception {
                logger.debug("physical delete doInBackground");
                DocumentDelegater ddl = new DocumentDelegater();
                ddl.removeObservations(list);
                return null;
            }

            @Override
            protected void succeeded(Void result) {
                logger.debug("physical delete succeeded");
                tableModel.deleteRow(row);
            }
        };

        task.execute();
    }

    /**
     * BMI値 を表示するレンダラクラス.
     */
    protected class BMIRenderer extends DefaultTableCellRenderer {

        /**
         * Creates new IconRenderer
         */
        public BMIRenderer() {
            super();
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value,
                boolean isSelected,
                boolean isFocused,
                int row, int col) {
            Component c = super.getTableCellRendererComponent(table,
                    value,
                    isSelected,
                    isFocused, row, col);

            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setForeground(table.getForeground());
                if (row % 2 == 0) {
                    setBackground(ClientContext.getColor("color.even"));
                } else {
                    setBackground(ClientContext.getColor("color.odd"));
                }
            }

            PhysicalModel h = (PhysicalModel) tableModel.getObject(row);

            Color fore = (h != null && h.getBmi() != null && h.getBmi().compareTo("25") > 0)  ? Color.RED : Color.BLACK;
            this.setForeground(fore);

            ((JLabel) c).setText(value == null ? "" : (String) value);

            if (h != null && h.getStandardWeight() != null) {
                this.setToolTipText("標準体重 = " + h.getStandardWeight());
            }

            return c;
        }
    }
}
