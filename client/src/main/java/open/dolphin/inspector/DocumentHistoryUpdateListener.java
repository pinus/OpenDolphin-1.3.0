package open.dolphin.inspector;

import java.util.EventListener;

/**
 * @author pns
 */
public interface DocumentHistoryUpdateListener extends EventListener {

    void updated(String docId);
}
