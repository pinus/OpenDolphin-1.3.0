package open.dolphin.impl.pinfo;

import open.dolphin.client.AbstractChartDocument;
import open.dolphin.client.GUIConst;
import open.dolphin.delegater.PatientDelegater;
import open.dolphin.helper.DBTask;
import open.dolphin.infomodel.PVTHealthInsuranceModel;
import open.dolphin.infomodel.PVTPublicInsuranceItemModel;
import open.dolphin.infomodel.PatientModel;
import open.dolphin.ui.PNSBorderFactory;
import open.dolphin.ui.PNSCellEditor;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Documet to show Patient and Health Insurance info.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class PatientInfoDocument extends AbstractChartDocument {

    // Title
    private static final String TITLE = "患者情報";

    // 患者属性名
    private static final String[] PATIENT_ATTRS = {
            "患者 ID", "氏  名", "カナ", "ローマ字 *", "性  別", "生年月日", "国  籍 *", "婚姻状況 *", "郵便番号", "住  所", "電  話", "携帯電話 *", "電子メール *"
    };

    // Info アイコン
    private static final ImageIcon INFO_BUTTON_IMAGE = GUIConst.ICON_INFORMATION_16;

    private static final String INFO = "* の項目は編集が可能です";

    // カラム名
    private static final String[] COLUMN_NAMES = {"項   目", "値"};

    // 編集可能な行
    private static final int[] EDITABLE_ROWS = {3, 6, 7, 11, 12};

    // 保存アイコン
    private static final ImageIcon SAVE_ICON = GUIConst.ICON_SAVE_16;

    // 保存ボタン
    private JButton saveBtn;

    // テーブルモデル
    private PatientInfoTableModel pModel;

    // 属性表示テーブル
    private JTable pTable;

    // State Context
    private StateContext stateMgr;

    /**
     * Creates new PatientInfoDocument.
     */
    public PatientInfoDocument() {
        setTitle(TITLE);

    }

    private void initialize() {

        JPanel myPanel = getUI();
        JComponent compo = createComponent();
        compo.setBorder(PNSBorderFactory.createGroupBoxBorder(new Insets(5, 5, 5, 5)));
        myPanel.setLayout(new BorderLayout());

        //
        // 保存ボタンを生成する
        //
        JPanel cmdPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//pns   cmdPanel.add(new JLabel(ClientContext.getImageIcon(INFO_BUTTON_IMAGE)));
        cmdPanel.add(new JLabel(INFO_BUTTON_IMAGE));
        cmdPanel.add(new JLabel(INFO));
//pns   saveBtn = new JButton(ClientContext.getImageIcon(SAVE_ICON));
        saveBtn = new JButton(SAVE_ICON);
        saveBtn.setEnabled(false);
        saveBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        cmdPanel.add(saveBtn);

        myPanel.add(cmdPanel, BorderLayout.NORTH);
        myPanel.add(compo, BorderLayout.CENTER);

        stateMgr = new StateContext();
        enter();
    }

    @Override
    public void start() {
        initialize();
    }

    @Override
    public void stop() {
    }

    @Override
    public void enter() {
        super.enter();
        if (stateMgr != null) {
            stateMgr.enter();
        }
    }

    @Override
    public boolean isDirty() {
        if (stateMgr != null) {
            return stateMgr.isDirtyState();
        } else {
            return super.isDirty();
        }
    }

    /**
     * 患者情報を更新する.
     */
    @Override
    public void save() {

        final PatientModel update = getContext().getPatient();
        final PatientDelegater pdl = new PatientDelegater();

        DBTask task = new DBTask<Void>(getContext()) {

            @Override
            public Void doInBackground() throws Exception {
                pdl.updatePatient(update);
                return null;
            }

            @Override
            public void succeeded(Void result) {
                stateMgr.processSavedEvent();
            }
        };

        task.execute();
    }

    private JComponent createComponent() {

        //
        // 患者モデルを取得する
        //
        PatientModel patient = getContext().getPatient();
        Collection<PVTHealthInsuranceModel> insList = patient.getPvtHealthInsurances();

        //
        // 患者情報テーブルを生成する
        //
        pModel = new PatientInfoTableModel(patient, PATIENT_ATTRS, COLUMN_NAMES);
        pTable = new JTable(pModel);
        pTable.putClientProperty("Quaqua.Table.style", "striped");
//pns   pTable.setDefaultRenderer(Object.class, new OddEvenRowRenderer());
        TableColumn column = pTable.getColumnModel().getColumn(1);
        DefaultCellEditor de = new PNSCellEditor(new JTextField());
        de.setClickCountToStart(2);
        column.setCellEditor(de);

        //
        // 配置する
        //
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(pTable);

        //
        // 健康保険情報テーブルを生成する
        //
        if (insList != null) {

            for (PVTHealthInsuranceModel insurance : insList) {
                HealthInsuranceTableModel hModel = new HealthInsuranceTableModel(
                        insurance, COLUMN_NAMES);
                JTable hTable = new JTable(hModel);
                hTable.putClientProperty("Quaqua.Table.style", "striped");
//pns           hTable.setDefaultRenderer(Object.class, new OddEvenRowRenderer());

                // 配置する
                panel.add(Box.createVerticalStrut(7));
                panel.add(hTable);
//pns
            }
        }

        JScrollPane scroller = new JScrollPane(panel);
        return scroller;
    }

    /**
     * 患者情報を表示する TableModel クラス.
     */
    protected class PatientInfoTableModel extends AbstractTableModel {

        // 患者モデル
        private PatientModel patient;

        // 属性名の配列
        private String[] attributes;

        // カラム名の配列
        private String[] columnNames;

        public PatientInfoTableModel(PatientModel patient, String[] attrs, String[] columnNames) {
            this.patient = patient;
            this.attributes = attrs;
            this.columnNames = columnNames;
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public int getRowCount() {
            return PATIENT_ATTRS.length;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            //
            // 編集可能な行である場合に true
            //
            boolean ret = false;
            if (col == 1) {
                for (int i = 0; i < EDITABLE_ROWS.length; i++) {
                    if (row == EDITABLE_ROWS[i]) {
                        ret = true;
                        break;
                    }
                }
            }
            return ret;
        }

        @Override
        public Object getValueAt(int row, int col) {

            String ret = null;

            if (col == 0) {
                //
                // 属性名を返す
                //
                ret = attributes[row];

            } else if (col == 1 && patient != null) {

                //
                // 患者属性を返す
                //

                switch (row) {

                    case 0:
                        ret = patient.getPatientId();
                        break;

                    case 1:
                        ret = patient.getFullName();
                        break;

                    case 2:
                        ret = patient.getKanaName();
                        break;

                    case 3:
                        ret = patient.getRomanName();
                        break;

                    case 4:
                        ret = patient.getGender();
                        break;

                    case 5:
                        ret = patient.getAgeBirthday();
                        break;

                    case 6:
                        ret = patient.getNationality();
                        break;

                    case 7:
                        ret = patient.getMaritalStatus();
                        break;

                    case 8:
                        ret = patient.contactZipCode();
                        break;

                    case 9:
                        ret = patient.contactAddress();
                        break;

                    case 10:
                        ret = patient.getTelephone();
                        break;

                    case 11:
                        ret = patient.getMobilePhone();
                        break;

                    case 12:
                        ret = patient.getEmail();
                        break;

                }
            }
            return ret;
        }


        /**
         * 属性値を変更する.
         *
         * @param value 属性値
         * @param row   行
         * @param col   列
         */
        @Override
        public void setValueAt(Object value, int row, int col) {

            if (value == null || value.equals("") || col == 0) {
                return;
            }

            String strValue = (String) value;

            switch (row) {

                case 3:
                    //
                    // ローマ字
                    //
                    patient.setRomanName(strValue);
                    stateMgr.processDirtyEvent();
                    break;

                case 6:
                    //
                    // 国籍
                    //
                    patient.setNationality(strValue);
                    stateMgr.processDirtyEvent();
                    break;

                case 7:
                    //
                    // 婚姻状況
                    //
                    patient.setMaritalStatus(strValue);
                    stateMgr.processDirtyEvent();
                    break;

                case 11:
                    //
                    // 携帯電話
                    //
                    patient.setMobilePhone(strValue);
                    stateMgr.processDirtyEvent();
                    break;

                case 12:
                    //
                    // 電子メール
                    //
                    patient.setEmail(strValue);
                    stateMgr.processDirtyEvent();
                    break;
            }
        }
    }

    /**
     * 保険情報を表示する TableModel クラス.
     */
    protected class HealthInsuranceTableModel extends AbstractTableModel {

        private String[] columnNames;

        private List<String[]> data;

        public HealthInsuranceTableModel(PVTHealthInsuranceModel insurance,
                                         String[] columnNames) {
            this.columnNames = columnNames;
            data = getData(insurance);
        }

        private List getData(PVTHealthInsuranceModel insurance) {

            if (insurance == null) {
                return null;
            }

            List<String[]> list = new ArrayList<String[]>();

            String[] rowData = new String[2];
            rowData[0] = "GUID";
            rowData[1] = insurance.getGUID();
            list.add(rowData);

            rowData = new String[2];
            rowData[0] = "保険種別";
            rowData[1] = insurance.getInsuranceClass();
            list.add(rowData);

            rowData = new String[2];
            rowData[0] = "保険種別コード";
            rowData[1] = insurance.getInsuranceClassCode();
            list.add(rowData);

            rowData = new String[2];
            rowData[0] = "保険者番号";
            rowData[1] = insurance.getInsuranceNumber();
            list.add(rowData);

            rowData = new String[2];
            rowData[0] = "被保険者記号";
            rowData[1] = insurance.getClientGroup();
            list.add(rowData);

            rowData = new String[2];
            rowData[0] = "被保険者番号";
            rowData[1] = insurance.getClientNumber();
            list.add(rowData);

            rowData = new String[2];
            rowData[0] = "本人家族区分";
            //rowData[1] = insurance.getFamilyClass();
            if ("true".equals(insurance.getFamilyClass())) rowData[1] = "本人";
            else if ("false".equals(insurance.getFamilyClass())) rowData[1] = "家族";
            else rowData[1] = "データなし";
            list.add(rowData);

            rowData = new String[2];
            rowData[0] = "開始日";
            rowData[1] = insurance.getStartDate();
            list.add(rowData);

            rowData = new String[2];
            rowData[0] = "有効期限";
            rowData[1] = insurance.getExpiredDate();
            list.add(rowData);

            String[] vals = insurance.getContinuedDisease();
            if (vals != null) {
                int count = vals.length;
                for (int i = 0; i < count; i++) {
                    rowData = new String[2];
                    rowData[0] = "継続適応疾患名";
                    rowData[1] = vals[i];
                    list.add(rowData);
                }
            }

            rowData = new String[2];
            rowData[0] = "入院時の負担率";
            rowData[1] = insurance.getPayInRatio();
            list.add(rowData);

            rowData = new String[2];
            rowData[0] = "外来時の負担率";
            rowData[1] = insurance.getPayOutRatio();
            list.add(rowData);

            PVTPublicInsuranceItemModel[] pbi = insurance
                    .getPVTPublicInsuranceItem();
            if (pbi == null) {
                return list;
            }
            int count = pbi.length;
            for (int i = 0; i < count; i++) {
                PVTPublicInsuranceItemModel item = pbi[i];

                rowData = new String[2];
                rowData[0] = "公費の優先順位";
                rowData[1] = item.getPriority();
                list.add(rowData);

                rowData = new String[2];
                rowData[0] = "公費負担名称";
                rowData[1] = item.getProviderName();
                list.add(rowData);

                rowData = new String[2];
                rowData[0] = "負担者番号";
                rowData[1] = item.getProvider();
                list.add(rowData);

                rowData = new String[2];
                rowData[0] = "受給者番号";
                rowData[1] = item.getRecipient();
                list.add(rowData);

                rowData = new String[2];
                rowData[0] = "開始日";
                rowData[1] = item.getStartDate();
                list.add(rowData);

                rowData = new String[2];
                rowData[0] = "有効期限";
                rowData[1] = item.getExpiredDate();
                list.add(rowData);

                rowData = new String[2];
                rowData[0] = "負担率";
                rowData[1] = item.getPaymentRatio();
                list.add(rowData);

                rowData = new String[2];
                rowData[0] = "負担率または負担金";
                rowData[1] = item.getPaymentRatioType();
                list.add(rowData);
            }

            return list;
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public int getRowCount() {
            return data != null ? data.size() : 5;
        }

        @Override
        public Object getValueAt(int row, int col) {

            if (data == null) {
                return null;
            }

            if (row >= data.size()) {
                return null;
            }

            String[] rowData = (String[]) data.get(row);

            return (Object) rowData[col];
        }
    }

    abstract class State {

        public abstract void enter();

    }

    class CleanState extends State {

        @Override
        public void enter() {
            saveBtn.setEnabled(false);
            setDirty(false);
        }
    }

    class DirtyState extends State {

        @Override
        public void enter() {
            saveBtn.setEnabled(true);
        }
    }

    class StateContext {

        private CleanState cleanState = new CleanState();
        private DirtyState dirtyState = new DirtyState();
        private State curState;

        public StateContext() {
            curState = cleanState;
        }

        public void enter() {
            curState.enter();
        }

        public void processSavedEvent() {
            curState = cleanState;
            this.enter();
        }

        public void processDirtyEvent() {
            if (!isDirtyState()) {
                curState = dirtyState;
                this.enter();
            }
        }

        public boolean isDirtyState() {
            return curState == dirtyState;
        }
    }
}
