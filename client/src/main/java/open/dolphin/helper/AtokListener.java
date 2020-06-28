package open.dolphin.helper;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;

/**
 * TextComponentUndoManager で使う ATOK のリスナ. Ctrl-Del の確定アンドゥ, かな/英数キー2度打ちに対応.
 *
 * @author pns
 */
public class AtokListener implements KeyListener, InputMethodListener {
    private Logger logger = LoggerFactory.getLogger(AtokListener.class);

    // キーストローク
    private static KeyStroke CTRL_BACKSPACE = KeyStroke.getKeyStroke("ctrl pressed BACK_SPACE");
    private static KeyStroke KANA = KeyStroke.getKeyStroke("released KATAKANA");
    private static KeyStroke EISU = KeyStroke.getKeyStroke("released ALPHANUMERIC");
    // TextComponent
    JTextComponent textComponent;
    // UndoManager
    TextComponentUndoManager undoManager;
    // 確定アンドゥのキーが押されたかどうか
    private boolean ctrlBackspace = false;
    // 確定アンドゥ中かどうか
    private boolean isInKakuteiUndo = false;
    // かなキー2度押しされたかどうか
    private boolean doubleKana = false;
    // 英数キー2度押しされたかどうか
    private boolean doubleEisu = false;
    // IME 開始された時間
    private long timeImeTextChanged;
    // KeyPressed があった時間
    private long timeKeyPressed;
    // KeyReleased があった時間
    private long timeKeyReleased;
    // 変換前文字列と変換後文字
    private String textInProcess = "", textCommitted = "";

    public AtokListener(JTextComponent tc, TextComponentUndoManager manager) {
        textComponent = tc;
        undoManager = manager;
    }

    @Override
    public void inputMethodTextChanged(InputMethodEvent event) {
        long timeFromImeTextChange = System.currentTimeMillis() - timeImeTextChanged;
        long timeFromKeyPress = System.currentTimeMillis() - timeKeyPressed;

        // かな/英数は KeyReleased でしか検出できないのでで間に合わない
        // 2度打ちが検出されたら, KeyReleased からここを null で呼ぶ
        if (event == null) {
            if (timeFromImeTextChange > 300) { return; }
            try {
                // 再変換元の文字列の最後を検出
                int pos = textComponent.getCaretPosition();
                int end = pos - textInProcess.length();

                // かなキー2度打ちの処理
                if (doubleKana) {
                    // end から逆にたどって, alphanumeric 以外の文字が出てくるところを検出
                    int start = end;
                    while(start-- > 0) {
                        char c = textComponent.getText(start, 1).charAt(0);
                        logger.info(start + ": " + c);
                        if (!StringTool.isHanakuLower(c) && !StringTool.isHankakuUpper(c)) { break; }
                    }
                    start++;
                    // 変換元の文字列を削除
                    textComponent.getDocument().remove(start, end - start);

                } else if (doubleEisu) {
                    logger.error("double eisu detected");
                }

            } catch (BadLocationException e) {
                logger.error(e.getMessage());
            }

            textInProcess = "";
            textCommitted = "";

            return;
        }

        // ctrl-backspace は, 押してすぐ入ってこなかったら無視
        ctrlBackspace = ctrlBackspace && timeFromKeyPress < 100;

        // 確定アンドゥは commit されたら終了
        if (isInKakuteiUndo && event.getCommittedCharacterCount() > 0) {
            logger.info("Kakutei-undo done: " + event.getCommittedCharacterCount());
            isInKakuteiUndo = false;
        }

        // 確定アンドゥ 1回目
        if (ctrlBackspace) {
            logger.info("Kakutei-undo start");
            undoManager.undo();
            ctrlBackspace = false;
            isInKakuteiUndo = true;

        } else if (isInKakuteiUndo && timeFromImeTextChange < 10) {
            // 2回目以降の ctrl-backspace キーは ATOK に取られて検出できないが，
            // timeFromTextChange が非常に短く入ってくるので検出できる
            logger.info("Kakutei-undo cont'd: " + timeFromImeTextChange);
            undoManager.undo();
        }

        // timeImeTextChanged 記録
        timeImeTextChanged = System.currentTimeMillis();

        // 処理文字列保存
        AttributedCharacterIterator iter = event.getText();
        StringBuilder sb = new StringBuilder();
        int count = event.getCommittedCharacterCount();
        char c = iter.first();
        while (count-- > 0) {
            sb.append(c);
            c = iter.next();
        }
        if (!StringUtils.isEmpty(sb.toString())) {
            textCommitted = sb.toString();
        }
        sb = new StringBuilder();
        while (c != CharacterIterator.DONE) {
            sb.append(c);
            c = iter.next();
        }
        if (!StringUtils.isEmpty(sb.toString())) {
            textInProcess = sb.toString();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        KeyStroke key = KeyStroke.getKeyStrokeForEvent(e);
        if (key.equals(CTRL_BACKSPACE)) {
            ctrlBackspace = true;
        }
        // timeKeyPressed 記録
        timeKeyPressed = System.currentTimeMillis();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // かなキー, 英数キーの2度打ち検出
        KeyStroke key = KeyStroke.getKeyStrokeForEvent(e);
        long lap = System.currentTimeMillis() - timeKeyReleased;

        if (key.equals(KANA)) {
            if (doubleKana && lap < 300) {
                inputMethodTextChanged(null);
                doubleKana = false;
            } else {
                doubleKana = true;
            }
        } else if (key.equals(EISU)) {
            if (doubleEisu && lap < 300) {
                inputMethodTextChanged(null);
                doubleEisu = false;
            } else {
                doubleEisu = true;
            }
        } else {
            doubleKana = false; doubleEisu = false;
        }

        // timeKeyReleased 記録
        timeKeyReleased = System.currentTimeMillis();
    }

    @Override
    public void caretPositionChanged(InputMethodEvent event) { }

    @Override
    public void keyTyped(KeyEvent e) { }
}
