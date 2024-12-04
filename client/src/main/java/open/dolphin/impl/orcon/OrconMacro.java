package open.dolphin.impl.orcon;

import open.dolphin.client.Dolphin;
import open.dolphin.helper.WindowHolder;
import open.dolphin.impl.psearch.PatientSearchImpl;
import open.dolphin.impl.pvt.WaitingListImpl;
import open.dolphin.infomodel.PatientModel;
import open.dolphin.infomodel.PatientVisitModel;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * マクロ.
 * @author pns
 */
public class OrconMacro {
    private WebDriver driver;
    private WebDriverWait wait;
    private final OrcaController context;
    private final OrconPanel panel;
    private final OrconProperties props;
    private final Logger logger;

    public OrconMacro(OrcaController context) {
        this.context = context;
        panel = context.getOrconPanel();
        props = context.getOrconProps();
        logger = LoggerFactory.getLogger(OrconMacro.class);
    }

    /**
     * ログイン処理. driver 作成して chrome を起動する.
     */
    public void login() {
        logger.info("login");
        Rectangle bounds = props.loadBounds();
        panel.setLoginState(true);

        try {
            ChromeOptions option = new ChromeOptions();
            List<String> args = new ArrayList<>();
            args.add(String.format("--window-position=%d,%d", bounds.x, bounds.y));
            args.add(String.format("--window-size=%d,%d", bounds.width, bounds.height));
            option.addArguments(args);

            driver = new ChromeDriver(option);
            driver.manage().timeouts().implicitlyWait(Duration.ofMillis(200));
            wait = new WebDriverWait(driver, Duration.ofSeconds(1));

            driver.get(panel.getAddressField().getText());
            WebElement user = driver.findElement(By.id("user"));
            WebElement pass = driver.findElement(By.id("pass"));
            WebElement login = driver.findElement(By.className("gtk-button"));
            user.sendKeys(panel.getUserField().getText());
            pass.sendKeys(new String(panel.getPasswordField().getPassword()));
            login.click();

            // (M01)業務メニューまで進む
            loginMoveToGyomu();
            // (K02)診療行為入力 まで進む
            m01ToShinryoKoi();

        } catch (RuntimeException e) {
            e.printStackTrace(System.err);
            quit();
        }
    }

    /**
     * from (M00)マスターメニュー to (M01)業務メニュー.
     */
    public void loginMoveToGyomu() {
        logger.info("業務メニューまで進む");
        WebElement m00selnum = driver.findElement(By.xpath("//*[@id=\"M00.fixed1.SELNUM\"]"));
        m00selnum.sendKeys("01", Keys.ENTER);
    }

    /**
     * from ANYWHERE to (M01)業務メニュー.
     */
    public void backToGyomu() {
        logger.info("業務メニューに戻る");
        WebElement back = findButtonElement("戻る");
        while (!back.getDomAttribute("id").contains("M01")) {
            back.click();
            back = findButtonElement("戻る");
        }
    }

    /**
     * from (M01)業務メニュー to (K02)診療行為入力.
     */
    public void m01ToShinryoKoi() {
        logger.info("診療行為入力まで進む");
        // 業務メニュー選択番号
        WebElement m01selnum = driver.findElement(By.xpath("//*[@id=\"M01.fixed1.SELNUM\"]"));
        m01selnum.sendKeys("21", Keys.ENTER);
    }

    /**
     * from (K02)診療行為入力 to (C02)病名登録.
     */
    public void k02ToByomeiToroku() {
        logger.info("病名登録へ移動");
        StringBuilder sb = new StringBuilder();
        sb.append(Keys.SHIFT);
        sb.append(Keys.F7);
        sendThrough(sb);
    }

    /**
     * at (K02)診療行為入力 do 中途終了展開.
     */
    public void k20ChutoTenkai() {
        logger.info("中途終了展開");
        try {
            WebElement chutoButton = driver.findElement(By.xpath("//*[@id=\"K02.fixed2.B12CS\"]"));
            chutoButton.click();
            By chutoField = By.xpath("//*[@id=\"K10.fixed1.SELNUM\"]");
            wait.until(ExpectedConditions.presenceOfElementLocated(chutoField));

            // 選択番号1番入力, ENTER ２回で展開
            WebElement k10selnum = driver.findElement(chutoField);
            k10selnum.sendKeys("1", Keys.ENTER, Keys.ENTER);

        } catch (RuntimeException ex) {
            // 中途終了がない場合
            logger.error(ex.getMessage());
        }
    }

    /**
     * at (K02)診療行為入力 do 外来管理加算削除.
     */
    public void k02GairaiKanriDelete() {
        logger.info("外来管理加算削除");
        // list of "入力コード" column
        List<WebElement> elements = driver.findElements(By.xpath("/html/body/div[2]/div/div/div[2]/div[6]/div/div[19]/table/tbody/tr/td[2]/input"));
        WebElement target = null;
        for (int r=0; r<10; r++) {
            // 112011010 外来管理加算コード検索
            if ("112011010".equals(elements.get(r).getAttribute("value"))) {
                target = elements.get(r);
                break;
            }
        }
        // 見つかったら コマンド-A で全選択, 削除
        if (target!= null) {
            target.sendKeys(Keys.chord(Keys.META, "A"), Keys.BACK_SPACE);
        }
    }

    /**
     * at (K03)診療行為入力 do 請求確認でのコンボボックス. 0 発行なし, 1 発行あり
     * @param n 0:全て印刷しない, 1: 領収書のみ印刷, 2: 処方箋のみ印刷, 3:両方印刷
     */
    public void k03SelectPrintForms(int n) {
        logger.info("印刷帳票選択");
        WebElement ryosyusyoElement = driver.findElement(By.xpath("//*[@id=\"K03.fixed3.HAKFLGCOMBO.HAKFLG\"]"));
        WebElement meisaisyoElement = driver.findElement(By.xpath("//*[@id=\"K03.fixed3.MEIPRTFLG_COMB.MEIPRTFLG\"]"));
        WebElement syohoElement = driver.findElement(By.xpath("//*[@id=\"K03.fixed3.SYOHOPRTFLGCOMBO.SYOHOPRTFLG\"]"));

        String ryosyusyo, meisaisyo, syoho;
        switch (n) {
            case 1 -> { ryosyusyo = "1"; meisaisyo = "0"; syoho = "0"; }
            case 2 -> { ryosyusyo = "0"; meisaisyo = "0"; syoho = "1"; }
            case 3 -> { ryosyusyo = "1"; meisaisyo = "1"; syoho = "1"; }
            default -> { ryosyusyo = "0"; meisaisyo = "0"; syoho = "0"; }
        }
        ryosyusyoElement.click();
        ryosyusyoElement.sendKeys(ryosyusyo);
        meisaisyoElement.click();
        meisaisyoElement.sendKeys(meisaisyo);
        syohoElement.click();
        syohoElement.sendKeys(syoho);
    }

    /**
     * at (K02)診療行為入力 do 患者番号送信.
     */
    public void k02SendPtNum() {
        logger.info("患者番号送信");
        sendPtNumTo("//*[@id=\"K02.fixed2.PTNUM\"]");
    }

    /**
     * at (C02)病名登録 do 患者番号送信.
     */
    public void c02SendPtNum() {
        logger.info("患者番号送信");
        sendPtNumTo("//*[@id=\"C02.fixed6.PTNUM\"]");
    }

    /**
     * elementId のフィールドに患者番号送信.
     */
    private void sendPtNumTo(String elementXpath) {
        String ptnum = "";
        if (WindowHolder.allCharts().size() > 0) {
            // チャートが開いていれば, その番号を送る
            ptnum = WindowHolder.allCharts().get(0).getPatient().getPatientId();
        } else {
            // チャートが開いていなければ, リストで選択された番号を送る
            int selected = ((Dolphin) context.getContext()).getTabbedPane().getSelectedIndex();
            if (selected == 0) {
                WaitingListImpl waitingList = context.getContext().getPlugin(WaitingListImpl.class);
                PatientVisitModel[] pvt = waitingList.getSelectedPvt();
                if (pvt != null && pvt.length > 0) {
                    ptnum = pvt[0].getPatientId();
                }
            } else if (selected == 1) {
                PatientSearchImpl patientSearch = context.getContext().getPlugin(PatientSearchImpl.class);
                PatientModel[] pm = patientSearch.getSelectedPatinet();
                if (pm != null && pm.length > 0) {
                    ptnum = pm[0].getPatientId();
                }
            }
        }
        if (!ptnum.isEmpty()) {
            WebElement test = driver.findElement(By.xpath(elementXpath));
            if (test.isDisplayed()) {
                test.sendKeys(ptnum);
            }
        }
    }

    /**
     * activeElement にキーを流す.
     * @param chord charsequence
     */
    public void sendThrough(CharSequence chord) {
        try {
            WebElement activeElement = driver.switchTo().activeElement();
            activeElement.sendKeys(chord);
        } catch (RuntimeException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * 終了処理.
     */
    public void close() {
        logger.info("close");
        org.openqa.selenium.Point loc = driver.manage().window().getPosition();
        org.openqa.selenium.Dimension size = driver.manage().window().getSize();
        Rectangle bounds = new Rectangle(loc.x, loc.y, size.width, size.height);

        props.saveBounds(bounds);
        props.viewToModel();
        panel.setLoginState(false);

        quit();
    }

    /**
     * ドライバ終了で chrome が閉じる.
     */
    public void quit() {
        driver.quit();
        panel.getLoginButton().setEnabled(true);
    }

    /**
     * 指定した文字のボタンを探す.
     * @param text text to search
     * @return button element or null
     */
    private WebElement findButtonElement(String text) {
        List<WebElement> buttons = driver.findElements(By.tagName("button"));
        return buttons.stream().filter(b -> text.equals(b.getText())).findFirst().orElse(null);
    }

    /**
     * ページのキー ("M01" 等) を返す.
     * @return key of the present page
     */
    public String whereAmI() {
        return driver.getTitle().substring(1,4);
    }

    /**
     * ページ更新を待つ ExpectedCondition.
     */
    private class PageUpdated implements ExpectedCondition<Boolean> {
        private String oldPageTitle;

        public void setOldWhereAmI(String oldPageTitle) {
            this.oldPageTitle = oldPageTitle;
        }

        @Override
        public Boolean apply(WebDriver input) {
            return !input.getTitle().equals(oldPageTitle);
        }
    }
}
