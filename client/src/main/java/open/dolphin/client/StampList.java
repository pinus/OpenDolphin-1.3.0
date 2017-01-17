package open.dolphin.client;

import open.dolphin.infomodel.ModuleModel;

/**
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class StampList implements java.io.Serializable {
    private static final long serialVersionUID = 5919106499806109895L;

    private ModuleModel[] stampList;

    public void setStampList(ModuleModel[] stampList) {
        this.stampList = stampList;
    }

    public ModuleModel[] getStampList() {
        return stampList;
    }
}
