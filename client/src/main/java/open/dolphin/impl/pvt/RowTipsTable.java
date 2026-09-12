package open.dolphin.impl.pvt;

import open.dolphin.infomodel.KarteState;
import open.dolphin.infomodel.PatientVisitModel;
import open.dolphin.ui.ObjectReflectTableModel;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;

/**
 * @author kazm
 * @author pns
 */
public class RowTipsTable extends JTable {
    
    public RowTipsTable() {
        // フォーカス取ったら選択する
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (getRowCount() > 0 && getSelectedRow() < 0) {
                    getSelectionModel().setSelectionInterval(0, 0);
                }
            }
        });
    }

    @Override
    public String getToolTipText(MouseEvent e) {

        ObjectReflectTableModel model = (ObjectReflectTableModel) getModel();
        int row = rowAtPoint(e.getPoint());
        int col = columnAtPoint(e.getPoint());
        PatientVisitModel pvt = (PatientVisitModel) model.getObject(row);

        if (pvt == null) {
            return null;
        }

        String text = null;

        switch (col) {
            case WaitingListImpl.BIRTHDAY_COLUMN:
                // 生年月日
                text = pvt.getPatientBirthday();
                break;

            case WaitingListImpl.AGE_COLUMN:
                // 年齢
                String[] age = pvt.getPatientAge().split("\\.");
                text = String.format("%s 歳 %s ヶ月", age[0], age[1]);
                break;

            default:
                if (pvt.getState() == KarteState.CLOSE_NONE || pvt.getState() == KarteState.OPEN_NONE) {
                    String waitingTime = WaitingListImpl.getWaitingTime(pvt.getPvtDate());
                    text = pvt.getPatient().getKanaName();
                    text += " - 待ち時間 " + waitingTime;
                }
        }

        return text;
    }
}
