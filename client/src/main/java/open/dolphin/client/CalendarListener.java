package open.dolphin.client;

import java.util.EventListener;
import open.dolphin.infomodel.SimpleDate;

/**
 *
 * @author pns
 */
public interface CalendarListener extends EventListener {

    public void dateSelected(SimpleDate date);
}
