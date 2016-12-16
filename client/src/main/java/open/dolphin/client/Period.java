package open.dolphin.client;

import java.util.EventObject;


/**
 * Period.
 * CareMapDocument のみで使われている
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class Period extends EventObject {
    private static final long serialVersionUID = -8572561462807732975L;

    private String startDate;
    private String endDate;

    public Period(Object source) {
        super(source);
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String val) {
        startDate = val;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String val) {
        endDate = val;
    }
}
