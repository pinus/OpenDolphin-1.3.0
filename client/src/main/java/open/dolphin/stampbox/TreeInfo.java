package open.dolphin.stampbox;

/**
 * TreeInfo
 *
 * @author Minagawa,Kazushi
 *
 */
public class TreeInfo {

    private String name;
    private String entity;

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
