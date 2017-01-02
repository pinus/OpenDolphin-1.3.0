package open.dolphin.inspector;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import open.dolphin.client.CalendarCardPanel;
import open.dolphin.client.ChartImpl;
import open.dolphin.client.ClientContext;
import open.dolphin.infomodel.SimpleDate;

/**
 *
 * @author kazm
 */
public class PatientVisitInspector {

    private CalendarCardPanel calendarCardPanel;

    private String pvtEvent; // PVT

    private ChartImpl context;

    /**
     * PatientVisitInspector を生成する.
     */
    public PatientVisitInspector(ChartImpl context) {
        this.context = context;
        initComponent();
        update();
    }

    /**
     * レイアウトパネルを返す.
     * @return レイアウトパネル
     */
    public JPanel getPanel() {
        return calendarCardPanel;
    }

    /**
     * GUIコンポーネントを初期化する.
     */
    private void initComponent() {
        pvtEvent = ClientContext.getString("eventCode.pvt"); // PVT
        calendarCardPanel = new CalendarCardPanel(ClientContext.getEventColorTable());
    }

    private void update() {

        // 来院歴を取り出す
        //List<String> latestVisit = context.getKarte().getEntryCollection("visit");
        List<String> latestVisit = context.getKarte().getPvtDateEntry();

        // 来院歴
        if (latestVisit != null && !latestVisit.isEmpty()) {
            List<SimpleDate> simpleDates = new ArrayList<>(latestVisit.size());
            for (String pvtDate : latestVisit) {
                SimpleDate sd = SimpleDate.mmlDateToSimpleDate(pvtDate);
                sd.setEventCode(pvtEvent);
                simpleDates.add(sd);
            }
            // CardCalendarに通知する
            calendarCardPanel.setMarkList(simpleDates);
        }
    }

}
