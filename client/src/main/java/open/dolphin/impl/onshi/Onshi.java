package open.dolphin.impl.onshi;

import open.dolphin.client.AbstractChartDocument;
import open.dolphin.client.ChartImpl;
import open.dolphin.delegater.OrcaDelegater;
import open.dolphin.event.BadgeEvent;
import open.dolphin.helper.PNSTask;
import open.dolphin.helper.StringTool;
import open.dolphin.helper.TextComponentUndoManager;
import open.dolphin.orca.orcadao.bean.OnshiKenshin;
import open.dolphin.orca.orcadao.bean.OnshiYakuzai;
import open.dolphin.ui.PNSScrollPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * オン資 ChartDocument.
 */
public class Onshi extends AbstractChartDocument {
    private final Logger logger = LoggerFactory.getLogger(Onshi.class);
    // Title
    private static final String TITLE = "オン資";
    private JTextPane textPane;

    public Onshi() {
        setTitle(TITLE);
    }

    @Override
    public void start() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        final JButton loadDrugHistoryButton = new JButton("薬剤情報");
        final JButton loadKenshinButton = new JButton("特定健診情報");
        loadDrugHistoryButton.addActionListener(this::loadDrugHistory);
        loadKenshinButton.addActionListener(this::loadKenshin);
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(loadDrugHistoryButton);
        buttonPanel.add(loadKenshinButton);
        buttonPanel.add(Box.createHorizontalGlue());

        textPane = new JTextPane();
        textPane.getDocument().addUndoableEditListener(TextComponentUndoManager.createManager(textPane));

        // クリップボードへ送る内容を、カルテにペーストしやすい形式に変換する
        textPane.setTransferHandler(new TransferHandler() {
            @Override
            public void exportToClipboard(JComponent comp, Clipboard clip, int action) {
                JTextPane pane = (JTextPane) comp;
                String selectedText = pane.getSelectedText();

                if (selectedText != null) {
                    // テキストを加工
                    String processedText = processText(selectedText);

                    // クリップボードに送信
                    StringSelection selection = new StringSelection(processedText);
                    clip.setContents(selection, selection);
                }
            }

            private String processText(String text) {
                // エルデカルシトールカプセル０．７５μｇ「日医工」 1カプセル １日１回朝食後服用 63日分
                text = text.replaceAll("([０-９．]+)ｍｇ", "($1)");
                text = text.replaceAll("([０-９．]+)μｇ", "($1)");
                text = text.replaceAll("[０-９．]+％", "");
                text = text.replaceAll("．", ".");

                text = text.replaceAll("／.*", "");

                text = text.replaceAll("１日", "");
                text = text.replaceAll("回.*服用", "x");
                text = text.replaceAll("カプセル", "Cap");
                text = text.replaceAll("日分", "TD");

                text = text.replaceAll("「.*」", " ");
                text = text.replaceAll("（.*）", " ");

                text = StringTool.toHankakuNumber(text);
                text = StringTool.toHankakuUpperLower(text);

                return text;
            }
        });

        textPane.setMargin(new Insets(7, 7, 7, 7));
        PNSScrollPane scroller = new PNSScrollPane(textPane);
        scroller.setVerticalScrollBarPolicy(PNSScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroller.setHorizontalScrollBarPolicy(PNSScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel myPanel = getUI();
        myPanel.setLayout(new BorderLayout());
        myPanel.add(buttonPanel, BorderLayout.NORTH);
        myPanel.add(scroller, BorderLayout.CENTER);

        /*
         * バッジ表示.
         */
        PNSTask<Integer> task = new PNSTask<>() {
            @Override
            protected Integer doInBackground() {
                String ptnum = getContext().getPatient().getPatientId();
                OrcaDelegater delegater = new OrcaDelegater();

                boolean hasDrugHistory = delegater.hasDrugHistory(ptnum);
                boolean hasKenshin = delegater.hasKenshin(ptnum);
                loadDrugHistoryButton.setEnabled(hasDrugHistory);
                loadKenshinButton.setEnabled(hasKenshin);

                return (hasDrugHistory ? 1 : 0) + (hasKenshin ? 1 : 0);
            }

            public void succeeded(Integer badgeNum) {
                BadgeEvent e = new BadgeEvent(Onshi.this);
                e.setTabIndex(5);
                e.setBadgeNumber(badgeNum);
                ((ChartImpl) getContext()).setBadge(e);
                loadDrugHistoryButton.doClick();
            }
        };
        task.execute();
    }

    public void loadDrugHistory(ActionEvent e) {
        PNSTask<List<OnshiYakuzai>> task = new PNSTask<>() {
            @Override
            protected List<OnshiYakuzai> doInBackground() {
                String ptnum = getContext().getPatient().getPatientId();
                OrcaDelegater delegater = new OrcaDelegater();
                return delegater.getDrugHistory(ptnum);
            }

            @Override
            public void succeeded(List<OnshiYakuzai> onshiYakuzai) {
                StringBuilder byDate = new StringBuilder();

                // 日付順
                //byDate.append("日付順\n");
                String date = "";
                String prevLabel = "";

                for (OnshiYakuzai o : onshiYakuzai) {
                    String hosp = o.getHosp().getFacilityName();
                    String chozai = o.getChozai().getFacilityName();
                    if (o.getHosp().isPharmacy()) {
                        chozai = o.getHosp().getFacilityName();
                        hosp = o.getChozai().getFacilityName();
                    }
                    String label = hosp + "／" + chozai;

                    if (!date.equals(o.getIsoDate()) || !prevLabel.equals(label)) {
                        date = o.getIsoDate();
                        prevLabel = label;
                        byDate.append(String.format("\n%s %s\n", date, label));
                    }

                    String yakuzainame = o.getYakuzainame();
                    byDate.append(String.format("    %s ", yakuzainame));
                    String suryo = Float.toString(o.getSuryo()).replaceAll(".0$", "");
                    if (o.getKaisu() <= 1) {
                        // 外用剤・頓用
                        byDate.append(String.format("%s%s %s\n", suryo, o.getTaniname(), o.getShiji()));
                    } else {
                        byDate.append(String.format("%s%s %s %s日分\n", suryo, o.getTaniname(), o.getYohoname(), o.getKaisu()));
                    }
                }

                // 薬剤別
                StringBuilder byDrug = new StringBuilder();
                // sort
                onshiYakuzai.sort((o1, o2) -> {
                    int dat = o1.getIsoDate().compareTo(o2.getIsoDate());
                    int name = o1.getYakuzainame().replaceAll("「.*」", "") // メーカー名は削除してソート
                        .compareTo(o2.getYakuzainame().replaceAll("「.*」", ""));
                    int yoho = o1.getYohocd().compareTo(o2.getYohocd());
                    return yoho == 0 ? name == 0 ? dat : name : yoho;
                });

//                for (OnshiYakuzai o : onshiYakuzai) {
//                    logger.info("yakuzai: {} {} {} {} {}", o.getYakuzainame(), o.getIsoDate(), o.getKaisu(), o.getYohocd(), o.getYohoname());
//                }

                List<OnshiYakuzai> drugs = new ArrayList<>();

                for (OnshiYakuzai o : onshiYakuzai) {
                    if (o.getKaisu() <= 1) {
                        continue;
                    } // 回数１の処方 (外用剤等) は除外

                    String yakuzainame = toHankaku(o.getYakuzainame());

                    // 薬剤別用の OnshiYakuzai を新たに作る
                    OnshiYakuzai o2 = new OnshiYakuzai();
                    o2.setYakuzainame(yakuzainame.replaceAll("｢.*｣", ""));
                    o2.setIsoDate(o.getIsoDate());
                    o2.setKaisu(o.getKaisu()); // int

                    if (!drugs.isEmpty() && drugs.getLast().getYakuzainame().equals(o2.getYakuzainame())) {
                        // 同じ薬が連続して処方されている場合は統合する
                        OnshiYakuzai last = drugs.getLast();
                        LocalDate lastStartDate = LocalDate.parse(last.getIsoDate());
                        LocalDate thisStartDate = LocalDate.parse(o2.getIsoDate());
                        int daysBetween = (int) ChronoUnit.DAYS.between(lastStartDate, thisStartDate);
                        int diff = daysBetween - last.getKaisu();
                        if (diff < 0) { // 薬切れる前に処方になっている場合
                            diff = 0;
                        }
                        if (diff < 30) { // １ヶ月以内の処方は連続していると判断
                            last.setKaisu(diff + last.getKaisu() + o2.getKaisu());
                        } else {
                            // 非連続処方と判断
                            drugs.add(o2);
                        }

                    } else {
                        drugs.add(o2);
                    }
                }

                // 処方終了日でソートしなおす
                drugs.sort((o1, o2) -> {
                    LocalDate endDate1 = LocalDate.parse(o1.getIsoDate()).plusDays(o1.getKaisu());
                    LocalDate endDate2 = LocalDate.parse(o2.getIsoDate()).plusDays(o2.getKaisu());
                    return endDate1.compareTo(endDate2); // 昇順
                });

//                byDrug.append("\n全部\n\n");
//                for (OnshiYakuzai o: onshiYakuzai) {
//                    LocalDate today = LocalDate.now();
//                    LocalDate startDate = LocalDate.parse(o.getIsoDate());
//                    LocalDate endDate = startDate.plusDays(o.getKaisu());
//                    String period = String.format("%s ~ %s", startDate.format(DateTimeFormatter.ISO_DATE), endDate.format(DateTimeFormatter.ISO_DATE));
//                    byDrug.append(String.format("%s, %s\n", o.getYakuzainame(), period));
//                }

                // 半年以内の内服薬
                byDrug.append("\n直近の薬歴\n\n");
                for (OnshiYakuzai o : drugs) {
                        LocalDate today = LocalDate.now();
                    LocalDate endDate = LocalDate.parse(o.getIsoDate()).plusDays(o.getKaisu());
                    long yearsBetween = ChronoUnit.MONTHS.between(endDate, today);
                    if (yearsBetween < 6) { // 半年以上古いデータは除外
                        String period = String.format("%s ~ %s", o.getIsoDate(), endDate.format(DateTimeFormatter.ISO_DATE));
                            byDrug.append(String.format("%s, %s\n", o.getYakuzainame(), period));
                        }
                }

                textPane.setText(byDate.toString() + byDrug);
            }
        };
        task.execute();
    }

    public void loadKenshin(ActionEvent e) {
        PNSTask<List<OnshiKenshin>> task = new PNSTask<>() {

            @Override
            protected List<OnshiKenshin> doInBackground() {
                String ptnum = getContext().getPatient().getPatientId();
                OrcaDelegater delegater = new OrcaDelegater();
                return delegater.getKenshin(ptnum);
            }

            @Override
            public void succeeded(List<OnshiKenshin> onshiKenshin) {
                StringBuilder sb = new StringBuilder();
                String date = "";

                for (OnshiKenshin k : onshiKenshin) {
                    if (!date.equals(k.getIsoDate())) {
                        date = k.getIsoDate();
                        sb.append(date); sb.append("\n");
                    }
                    String v = k.getDataValue();
                    switch (k.getKomokucd()) {
                        case "1A020000000191111", // 尿糖 機械
                             "1A020000000190111", // 尿糖 目視
                             "1A010000000191111", // 尿蛋白 機械
                             "1A010000000190111"  // 尿蛋白 目視
                            -> k.setDataValue(valToString("1:-、2:±、3:+、4:++、5:+++", v));
                        case "1A105160700166211", // 尿沈渣
                             "9A110160700000011" // 心電図
                            -> k.setDataValue(valToString("1:所見あり、2:所見なし", v));
                        case "9N056000000000011", // 既往歴
                             "9N061000000000011", // 自覚症状
                             "9N066000000000011" // 他覚症状
                            -> k.setDataValue(valToString("1:特記すべきことあり、2:特記すべきことなし", v));
                        case "9N501000000000011" // メタボリックシンドローム判定
                            -> k.setDataValue(valToString("1:基準該当、2:予備群該当、3:非該当、4:判定不能", v));
                        case "9N701000000000011", // 服薬1(血圧)
                             "9N706000000000011", // 服薬2(血糖)
                             "9N711000000000011" // 服薬3(脂質)
                            -> k.setDataValue(valToString("1:服薬あり、2:服薬なし", v));
                        case "9N736000000000011", // 喫煙
                             "9N716000000000011", // 既往歴1(脳血管)
                             "9N721000000000011", // 既往歴2(心血管)
                             "9N726000000000011", // 既往歴3(腎不全・人工透析)
                             "9N731000000000011", // 貧血
                             "9N741000000000011", // 20歳からの体重変化
                             "9N746000000000011", // 30分以上の運動習慣
                             "9N751000000000011", // 歩行又は身体活動
                             "9N761000000000011", // 1年の体重変化
                             "9N756000000000011", // 歩行速度
                             "9N771000000000011", // 食べ方2(就寝前)
                             "9N781000000000011", // 食習慣
                             "9N796000000000011", // 睡眠
                             "9N806000000000011" // 保健指導の希望
                            -> k.setDataValue(valToString("1:はい、2:いいえ", v));
                        case "9N782000000000011" // 食べ方3(間食)
                            -> k.setDataValue(valToString("1:毎日、2:時々、3:ほとんど摂取しない", v));
                        case "9N506000000000011" // 保健指導レベル
                            ->  k.setDataValue(valToString("1:積極的支援、2:動機付け支援、3:なし、4:判定不能", v));
                        case "9N766000000000011" // 食べ方1(早食い等)
                            -> k.setDataValue(valToString("1:速い、2:ふつう、3:遅い", v));
                        case "9N786000000000011" // 飲酒
                            -> k.setDataValue(valToString("1:毎日、2:時々、3:ほとんど飲まない", v));
                        case "9N791000000000011" // 飲酒量
                            -> k.setDataValue(valToString("1:1合未満、2:1~2合未満、3:2~3合未満、4:3合以上", v));
                        case "9N801000000000011" // 生活習慣の改善
                            ->k.setDataValue(valToString("1:意志なし、2:意志あり(6ヶ月以内)、3:意志あり(近いうち)、 4:取組済み(6ヶ月未満)、5:取組済み(6ヶ月以上)", v));
                        case "9N872000000000011" // 咀嚼
                            -> k.setDataValue(valToString("1:何でも、2:かみにくい、3:ほとんどかめない", v));
                    }
                    sb.append(String.format("    %s %s %s\n", k.getKomokuname(), k.getDataValue(), k.getDataTani()));
                }
                textPane.setText(sb.toString());
            }
        };
        task.execute();
    }

    private String toHankaku(String str) {
        str = StringTool.toHankakuNumber(str);
        str = StringTool.toHankakuUpperLower(str);
        str = str.replaceAll("　", " ");
        str = str.replaceAll("（", "(");
        str = str.replaceAll("）", ")");
        str = str.replaceAll("．", ".");
        str = str.replaceAll("％", "%");
        str = str.replaceAll("「", "｢");
        str = str.replaceAll("」", "｣");
        return str;
    }

    /**
     * "1:-、2:±、3:+、4:++、5:+++", 1 から - を返す.
     *
     * @param str raw string
     * @return result
     */
    private String valToString(String str, String val) {
        String[] item = str.split("、");
        for (String s : item) {
            String[] map = s.split(":");
            if (val.equals(map[0])) {
                return map[1];
            }
        }
        return "";
    }
}
