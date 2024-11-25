package open.dolphin.impl.orcon;

import org.openqa.selenium.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * キーを横取りして orca に送るための key dispatcher.
 * @author pns
 */
public class OrconKeyDispatcher implements KeyEventDispatcher {
    public enum Mode { DISABLE, FULL, STEALTH }

    private Mode mode;
    private final OrconMacro orconMacro;
    private final Logger logger = LoggerFactory.getLogger(OrconKeyDispatcher.class);

    private boolean shift;
    private boolean ctrl;
    private boolean alt;
    private boolean meta;

    public OrconKeyDispatcher(OrconMacro orconMacro) {
        this.orconMacro = orconMacro;
    }

    public void setMode(Mode mode) {
        logger.info("mode = " + mode);
        this.mode = mode;
    }

    public Mode getMode(){
        return mode;
    }

    /**
     * KeyEvent の modifier 状態をチェックする.
     * @param e KeyEvent
     */
    private void setModifierState(KeyEvent e) {
        shift = e.isShiftDown();
        ctrl = e.isControlDown();
        alt = e.isAltDown();
        meta = e.isMetaDown();
    }

    /**
     * KeyEvent を CharSequence に変換して selenium に流す.
     * @param e KeyEvent
     */
    private void sendThrough(KeyEvent e) {
        //logger.info("pressed = " + e);
        // 単独 modifier は受けない
        if (KeyUtils.isModifier(e.getKeyCode())) { return; }

        // modifier を selenium Keys に変換してセット
        StringBuilder chord = new StringBuilder();
        if (shift) { chord.append(Keys.SHIFT); }
        if (ctrl) { chord.append(Keys.CONTROL); }
        if (alt) { chord.append(Keys.ALT); }
        if (meta) { chord.append(Keys.META); }
        // 特殊キーを selenium Keys に変換
        Keys specialKey = KeyUtils.toSeleniumKey(e.getKeyCode());
        if (specialKey != null) {
            //logger.info("specialKey = " + specialKey);
            chord.append(specialKey);
        } else {
            // 通常キーは getChar で受ける
            //logger.info("normal key = " + e.getKeyChar());
            chord.append(e.getKeyChar());
        }
//        for (int i=0; i<chord.length(); i++) {
//            int specialKeyCode = KeyUtils.toKeyCode(chord.charAt(i));
//            String keytext = specialKeyCode < 65535?
//                KeyEvent.getKeyText(specialKeyCode) : String.valueOf(chord.charAt(i));
//            logger.info(String.format("chord[%d] keyCode:%d converted:%d keyChar:%s(%d) charInChord:%s(%s)",
//                i, e.getKeyCode(), specialKeyCode, e.getKeyChar(), (int)e.getKeyChar(), chord.charAt(i), keytext));
//        }

        orconMacro.sendThrough(chord);
    }

    /**
     * DISABLE: 何も奪わない, FULL: コマンドキーは奪わない
     * STEALTH: コマンドキー、スペース、矢印キーは奪わない
     * @param e the KeyEvent to dispatch
     * @return true to block
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        setModifierState(e);

        // 横取りしないキーを false で返す
        if (mode == Mode.DISABLE) {
            // 横取りしない
            return false;

        } else if (mode == Mode.FULL) {
            // コマンドキーを横取りしない
            if (meta) { return false; }

        } else { // Mode.STEALTH
            // ショートカット、ファンクションキーだけ通す
            int c = e.getKeyCode();
            if (!( (ctrl && c == KeyEvent.VK_ENTER) || (ctrl && c == KeyEvent.VK_K) || (ctrl && c == KeyEvent.VK_B)
                || (ctrl && c == KeyEvent.VK_0) || (ctrl && c == KeyEvent.VK_1) || (ctrl && c == KeyEvent.VK_2)
                || (ctrl && c == KeyEvent.VK_V) || (shift && c == KeyEvent.VK_ENTER) || (shift && meta && c == KeyEvent.VK_A) || (shift && c == KeyEvent.VK_BACK_SPACE)
                || c == KeyEvent.VK_F1 || c == KeyEvent.VK_F2|| c == KeyEvent.VK_F3|| c == KeyEvent.VK_F4|| c == KeyEvent.VK_F5 || c == KeyEvent.VK_F6
                || c == KeyEvent.VK_F7 || c == KeyEvent.VK_F8 || c == KeyEvent.VK_F9 || c == KeyEvent.VK_F10 || c == KeyEvent.VK_F11 || c == KeyEvent.VK_F12)
            ) {
                return false;
            }
        }

        //
        // ショートカットキーでマクロを呼ぶ
        //
        if (ctrl && shift && e.getKeyCode() == KeyEvent.VK_ENTER) {
            // 中途終了展開 SHIFT + CTRL + ENTER
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                if (orconMacro.whereAmI().equals("K02")) {
                    orconMacro.k20ChutoTenkai();
                }
            }

        } else if (ctrl && e.getKeyCode() == KeyEvent.VK_K) {
            // 外来管理加算削除 CTRL + K
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                if (orconMacro.whereAmI().equals("K02")) {
                    orconMacro.k02GairaiKanriDelete();
                }
            }

        } else if (ctrl && e.getKeyCode() == KeyEvent.VK_B) {
            // (C02)病名登録へ移動
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                if (orconMacro.whereAmI().equals("K02")) {
                    orconMacro.k02ToByomeiToroku();
                }
            }

        } else if (ctrl && (e.getKeyCode() == KeyEvent.VK_0 || e.getKeyCode() == KeyEvent.VK_1 || e.getKeyCode() == KeyEvent.VK_2)) {
            // (K03)診療行為入力ｰ請求確認で, 領収書/明細書/処方箋を打ち出すかどうか
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                if (orconMacro.whereAmI().equals("K03")) {
                    orconMacro.k03SelectPrintForms(e.getKeyCode() - KeyEvent.VK_0);
                }
            }

        } else if (ctrl && e.getKeyCode() == KeyEvent.VK_V) {
            // 患者番号送信
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                switch (orconMacro.whereAmI()) {
                    case "K02" -> orconMacro.k02SendPtNum();
                    case "C02" -> orconMacro.c02SendPtNum();
                }
            }

        } else if (shift && e.getKeyCode() == KeyEvent.VK_ENTER) {
            // shift + ENTER で orca enter 入力
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                orconMacro.sendThrough(Keys.ENTER);
            }

        } else if (shift && meta && e.getKeyCode() == KeyEvent.VK_A) {
            // shift + command + A で 全選択
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                orconMacro.sendThrough(Keys.chord(Keys.META, "A"));
            }

        } else if (shift && e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            // shift + backspace で backspace
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                orconMacro.sendThrough(Keys.BACK_SPACE);
            }

        } else {
            // ショートカットキー以外は, そのまま流す
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                sendThrough(e);
            }
        }
        // block
        return true;
    }
}
