package open.dolphin.client;

import open.dolphin.event.ProxyDocumentListener;
import open.dolphin.event.ValidListener;
import open.dolphin.helper.GridBagBuilder;
import open.dolphin.infomodel.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.im.InputSubset;
import java.util.Date;

/**
 * AccountInfoPanel.
 *
 * @author Minagawa,Kazushi
 * @author pns
 */
public class AccountInfoPanel extends JPanel {
    private static final long serialVersionUID = -7695417342865642933L;

    // FACILITY_USERにしておく. FACILITY_OIDはデフォを使う (masuda-sensei)
    //private static final String MEMBER_TYPE = "ASP_TESTER";
    private static final String MEMBER_TYPE = "FACILITY_USER";
    private static final String DEFAULT_FACILITY_OID = IInfoModel.DEFAULT_FACILITY_OID;

    //private JTextField facilityId;
    private JTextField facilityName;
    private JTextField zipField1;
    private JTextField zipField2;
    private JTextField addressField;
    private JTextField areaField;
    private JTextField cityField;
    private JTextField numberField;
    private JTextField urlField;
    private JTextField adminId;
    private JPasswordField adminPassword1;
    private JPasswordField adminPassword2;
    private JTextField adminSir;
    private JTextField adminGiven;
    private JComboBox<LicenseModel> licenseCombo;
    private JComboBox<DepartmentModel> deptCombo;
    private LicenseModel[] licenses;
    private DepartmentModel[] depts;
    private JTextField emailField;
    private int[] userIdLength;
    private int[] passwordLength;
    private UserModel adminModel;
    private boolean validInfo;
    private ValidListener validListener;

    public AccountInfoPanel() {
        initialize();
        connect();
    }

    public UserModel getModel() {
        adminModel = new UserModel();
        return getAdminUser(adminModel);
    }

    public void setModel(UserModel adminModel) {
        this.adminModel = adminModel;
    }

    public void addValidListener(ValidListener listener) {
        validListener = listener;
    }

    public boolean isValidInfo() {
        return validInfo;
    }

    public void setValidInfo(boolean valid) {
        validInfo = valid;
        validListener.validity(valid);
    }

    private UserModel getAdminUser(UserModel admin) {

        FacilityModel facility = new FacilityModel();
        admin.setFacilityModel(facility);

        // 医療機関ID
        //String fid = facilityId.getText().trim();
        //facility.setFacilityId(fid);
        // ここでFacilityIdをセット (masuda-sensei)
        facility.setFacilityId(DEFAULT_FACILITY_OID);

        // 医療機関名
        facility.setFacilityName(facilityName.getText().trim());

        // 郵便番号
        StringBuilder buf = new StringBuilder();
        buf.append(zipField1.getText().trim());
        buf.append("-");
        buf.append(zipField2.getText().trim());
        facility.setZipCode(buf.toString());

        // 住所
        facility.setAddress(addressField.getText().trim());

        // 電話
        buf = new StringBuilder();
        buf.append(areaField.getText().trim());
        buf.append("-");
        buf.append(cityField.getText().trim());
        buf.append("-");
        buf.append(numberField.getText().trim());
        facility.setTelephone(buf.toString());

        // URL
        facility.setUrl(urlField.getText().trim());

        // 登録日 MemberTpe
        Date date = new Date();
        facility.setRegisteredDate(date);
        facility.setMemberType(MEMBER_TYPE);

        // 管理者基本情報
        admin.setUserId(adminId.getText().trim());
        admin.setPassword(new String(adminPassword1.getPassword()));
        admin.setSirName(adminSir.getText().trim());
        admin.setGivenName(adminGiven.getText().trim());
        admin.setCommonName(admin.getSirName() + " " + admin.getGivenName());

        // 医療資格
        int index = licenseCombo.getSelectedIndex();
        admin.setLicenseModel(licenses[index]);

        // 診療科
        index = deptCombo.getSelectedIndex();
        admin.setDepartmentModel(depts[index]);

        // Email
        admin.setEmail(emailField.getText().trim());

        // MemberTpe
        admin.setMemberType(MEMBER_TYPE);

        // 登録日
        admin.setRegisteredDate(date);

        return admin;
    }

    private void initialize() {

        FocusAdapter imeOn = new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                JTextField tf = (JTextField)event.getSource();
                tf.getInputContext().setCharacterSubsets(new Character.Subset[] {InputSubset.KANJI});
            }
        };

        FocusAdapter imeOff = new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                JTextField tf = (JTextField)event.getSource();
                tf.getInputContext().setCharacterSubsets(null);
            }
        };

        facilityName = GUIFactory.createTextField(30, null, imeOn, null);
        zipField1 = GUIFactory.createTextField(3, null, imeOff, null);
        zipField2 = GUIFactory.createTextField(3, null, imeOff, null);
        addressField = GUIFactory.createTextField(30, null, imeOn, null);
        areaField = GUIFactory.createTextField(3, null, imeOff, null);
        cityField = GUIFactory.createTextField(3, null, imeOff, null);
        numberField = GUIFactory.createTextField(3, null, imeOff, null);
        urlField = GUIFactory.createTextField(30, null, imeOff, null);

        adminId = GUIFactory.createTextField(10, null, imeOff, null);
        adminPassword1 = GUIFactory.createPassField(10, null, imeOff, null);
        adminPassword2 = GUIFactory.createPassField(10, null, imeOff, null);
        adminSir = GUIFactory.createTextField(10, null, imeOn, null);
        adminGiven = GUIFactory.createTextField(10, null, imeOn, null);
        emailField =  GUIFactory.createTextField(15, null, imeOff, null);

        String digitPattern = ClientContext.getString("addUser.pattern.digit");
        RegexConstrainedDocument zip1Doc = new RegexConstrainedDocument(digitPattern);
        zipField1.setDocument(zip1Doc);
        RegexConstrainedDocument zip2Doc = new RegexConstrainedDocument(digitPattern);
        zipField2.setDocument(zip2Doc);
        RegexConstrainedDocument areaDoc = new RegexConstrainedDocument(digitPattern);
        areaField.setDocument(areaDoc);
        RegexConstrainedDocument cityDoc = new RegexConstrainedDocument(digitPattern);
        cityField.setDocument(cityDoc);
        RegexConstrainedDocument numberDoc = new RegexConstrainedDocument(digitPattern);
        numberField.setDocument(numberDoc);

        String pattern = ClientContext.getString("addUser.pattern.idPass");
        RegexConstrainedDocument userIdDoc = new RegexConstrainedDocument(pattern);
        adminId.setDocument(userIdDoc);
        adminId.setToolTipText(pattern);

        RegexConstrainedDocument passwordDoc1 = new RegexConstrainedDocument(pattern);
        adminPassword1.setDocument(passwordDoc1);
        adminPassword1.setToolTipText(pattern);
        RegexConstrainedDocument passwordDoc2 = new RegexConstrainedDocument(pattern);
        adminPassword2.setDocument(passwordDoc2);
        adminPassword2.setToolTipText(pattern);

        pattern = ClientContext.getString("addUser.pattern.email");
        RegexConstrainedDocument emailDoc = new RegexConstrainedDocument(pattern);
        emailField.setDocument(emailDoc);
        emailField.setToolTipText("折り返し施設IDを送信します");

        licenses = ClientContext.getLicenseModel();
        licenseCombo = new JComboBox<>(licenses);

        depts = ClientContext.getDepartmentModel();
        deptCombo = new JComboBox<>(depts);

        GridBagBuilder gbl = new GridBagBuilder("施設情報 - URL以外の全ての項目が必要です");
        JLabel label;
        StringBuilder sb;

        int x = 0;
        int y = 0;
        label = new JLabel("医療機関名:", SwingConstants.RIGHT);
        gbl.add(label,        x,    y, GridBagConstraints.EAST);
        gbl.add(facilityName, x+1,  y, GridBagConstraints.WEST);

        x = 0;
        y += 1;
        label = new JLabel("郵便番号:", SwingConstants.RIGHT);
        gbl.add(label, 													x, 	y, GridBagConstraints.EAST);
        gbl.add(GUIFactory.createZipCodePanel(zipField1, zipField2), 	x+1, y, GridBagConstraints.WEST);

        x = 0;
        y += 1;
        label = new JLabel("住　所:", SwingConstants.RIGHT);
        gbl.add(label, x, y, GridBagConstraints.EAST);
        gbl.add(addressField, x+1, y, 2, 1, GridBagConstraints.WEST);

        x = 0;
        y += 1;
        label = new JLabel("電話番号:", SwingConstants.RIGHT);
        gbl.add(label, x, y, GridBagConstraints.EAST);
        gbl.add(GUIFactory.createPhonePanel(areaField, cityField, numberField), x+1, y, GridBagConstraints.WEST);

        x = 0;
        y += 1;
        label = new JLabel("URL:", SwingConstants.RIGHT);
        gbl.add(label, x, y, GridBagConstraints.EAST);
        gbl.add(urlField, x+1, y, GridBagConstraints.WEST);

        JPanel facilityPanel = gbl.getProduct();
        this.add(facilityPanel);

        // 管理者パネル
        x = 0;
        y = 0;
        userIdLength = ClientContext.getIntArray("addUser.userId.length");
        sb = new StringBuilder();
        sb.append("管理者ログインID(半角英数記)");
        sb.append(userIdLength[0]);
        sb.append("~");
        sb.append(userIdLength[1]);
        sb.append("文字):");
        label = new JLabel(sb.toString(), SwingConstants.RIGHT);
        gbl = new GridBagBuilder("この医療機関のOpenDolphin管理者情報 - 全ての項目が必要です");
        gbl.add(label, x, y, GridBagConstraints.EAST);
        gbl.add(adminId, x+1, y, GridBagConstraints.WEST);

        x = 0;
        y += 1;
        passwordLength = ClientContext.getIntArray("addUser.password.length");
        sb = new StringBuilder();
        sb.append("パスワード(半角英数記)");
        sb.append(passwordLength[0]);
        sb.append("~");
        sb.append(passwordLength[1]);
        sb.append("文字):");
        label = new JLabel(sb.toString(), SwingConstants.RIGHT);
        gbl.add(label,           x,     y, GridBagConstraints.EAST);
        gbl.add(adminPassword1,  x + 1, y, GridBagConstraints.WEST);
        label = new JLabel("確認:", SwingConstants.RIGHT);
        gbl.add(label,           x + 2, y, GridBagConstraints.EAST);
        gbl.add(adminPassword2,  x + 3, y, GridBagConstraints.WEST);

        x = 0;
        y += 1;
        label = new JLabel("姓（漢字）:", SwingConstants.RIGHT);
        gbl.add(label, x, y, GridBagConstraints.EAST);
        gbl.add(adminSir, x+ 1, y, GridBagConstraints.WEST);
        label = new JLabel("名（漢字）:", SwingConstants.RIGHT);
        gbl.add(label, x+2, y, GridBagConstraints.EAST);
        gbl.add(adminGiven, x+ 3, y, GridBagConstraints.WEST);

        x = 0;
        y +=1;
        label = new JLabel("医療資格:", SwingConstants.RIGHT);
        gbl.add(label, x, y, GridBagConstraints.EAST);
        gbl.add(licenseCombo,x+ 1, y, GridBagConstraints.WEST);
        label = new JLabel("診療科:", SwingConstants.RIGHT);
        gbl.add(label, x+2, y, GridBagConstraints.EAST);
        gbl.add(deptCombo,x+ 3, y, GridBagConstraints.WEST);

        x = 0;
        y +=1;
        label = new JLabel("電子メール:", SwingConstants.RIGHT);
        gbl.add(label, x, y, GridBagConstraints.EAST);
        gbl.add(emailField, x+1, y, 1, 1, GridBagConstraints.WEST);

        JPanel adminPanel = gbl.getProduct();

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(adminPanel);
    }

    private void connect() {

        ProxyDocumentListener dl = e -> checkValidInfo();

        facilityName.getDocument().addDocumentListener(dl);
        zipField1.getDocument().addDocumentListener(dl);
        zipField2.getDocument().addDocumentListener(dl);
        addressField.getDocument().addDocumentListener(dl);
        areaField.getDocument().addDocumentListener(dl);
        cityField.getDocument().addDocumentListener(dl);
        numberField.getDocument().addDocumentListener(dl);
        adminId.getDocument().addDocumentListener(dl);
        adminPassword1.getDocument().addDocumentListener(dl);
        adminPassword2.getDocument().addDocumentListener(dl);
        adminSir.getDocument().addDocumentListener(dl);
        adminGiven.getDocument().addDocumentListener(dl);
        emailField.getDocument().addDocumentListener(dl);
    }

    private void checkValidInfo() {
        boolean infoOk = ! (
                facilityName.getText().trim().equals("") ||
                zipField1.getText().trim().equals("") ||
                zipField2.getText().trim().equals("") ||
                addressField.getText().trim().equals("") ||
                areaField.getText().trim().equals("") ||
                cityField.getText().trim().equals("") ||
                numberField.getText().trim().equals("") ||
                (! validUserId()) ||
                (! validPassword()) ||
                adminSir.getText().trim().equals("") ||
                adminGiven.getText().trim().equals("") ||
                (licenseCombo.getSelectedItem() == null) ||
                (deptCombo.getSelectedItem() == null ||
                emailField.getText().trim().equals("")) );

        setValidInfo(infoOk);
    }

    private boolean validUserId() {

        String val = adminId.getText().trim();
        if (val.equals("")) {
            return false;
        }
        return !(val.length() < userIdLength[0] || val.length() > userIdLength[1]);
    }

    private boolean validPassword() {

        String passwd1 = new String(adminPassword1.getPassword());
        String passwd2 = new String(adminPassword2.getPassword());

        if (passwd1.equals("") || passwd2.equals("")) {
            return false;
        }

        if ( (passwd1.length() < passwordLength[0]) || (passwd1.length() > passwordLength[1]) ) {
            return false;
        }

        if ( (passwd2.length() < passwordLength[0]) || (passwd2.length() > passwordLength[1]) ) {
            return false;
        }

        return passwd1.equals(passwd2);
    }
}
