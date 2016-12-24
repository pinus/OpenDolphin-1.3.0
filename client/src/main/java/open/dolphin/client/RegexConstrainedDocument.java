package open.dolphin.client;

import javax.swing.text.*;
import java.awt.Toolkit;
import java.util.regex.*;

/**
 * RegexConstrainedDocument.
 */
public class RegexConstrainedDocument extends PlainDocument {
    private static final long serialVersionUID = 4066321190740323979L;

    private boolean beep;
    private boolean debug;
    private Pattern pattern;
    private Matcher matcher;

    public RegexConstrainedDocument () {
    	super();
    }

    public RegexConstrainedDocument (AbstractDocument.Content c) {
    	super(c);
    }

    public RegexConstrainedDocument (AbstractDocument.Content c, String p) {
        super (c);
        setPatternByString (p);
    }

    public RegexConstrainedDocument (String p) {
        super();
        setPatternByString (p);
    }

    private void setPatternByString (String p) {
        pattern = Pattern.compile (p);
        // check the document against the new pattern
        // and removes the content if it no longer matches
        try {
            matcher = pattern.matcher (getText(0, getLength()));
            debug("matcher reset to " + getText (0, getLength()));
            if (! matcher.matches()) {
                debug ("does not match");
                remove (0, getLength());
            }
        } catch (BadLocationException ble) {
            ble.printStackTrace(System.err); // impossible?
        }
    }

    public Pattern getPattern() {
    	return pattern;
    }

    @Override
    public void insertString (int offs, String s, AttributeSet a) throws BadLocationException {
        // consider whether this insert will match
        //String proposedInsert = getText (0, offs) + s + getText (offs, getLength() - offs);
        String proposedInsert = getText (0, getLength()) + s ;
    	//String proposedInsert = s ;
        debug("proposing to change to: " + proposedInsert);
        if (matcher != null) {
            matcher.reset (proposedInsert);
            debug("matcher reset");
            if (! matcher.matches()) {
            	beep();
                debug("insert doesn't match");
                return;
            }
        }
        super.insertString (offs, s, a);
    }

    private void beep() {
    	if (beep) {
    		Toolkit.getDefaultToolkit().beep();
    	}
    }

    private void debug(String msg) {
    	if (debug) {
    		System.out.println(msg);
    	}
    }
}
