package open.dolphin.stampbox;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

/**
 * Director of StampTree builder.
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */

public class StampTreeDirector {

    protected final int TT_STAMP_INFO  	= 0;
    protected final int TT_NODE  	= 1;
    protected final int TT_ROOT  	= 2;
    protected final int TT_STAMP_TREE  	= 3;
    protected final int TT_STAMP_BOX  	= 4;

    protected AbstractStampTreeBuilder builder;

    /**
     * Creates new StampTreeDirector.
     */
    public StampTreeDirector() {}

    public StampTreeDirector(AbstractStampTreeBuilder builder) {
        this.builder = builder;
    }

    public List<StampTree> build(BufferedReader reader) {

        SAXBuilder docBuilder = new SAXBuilder();

        try {
            Document doc = docBuilder.build(reader);
            Element root = doc.getRootElement();

            builder.buildStart();
            parseChildren(root);
            builder.buildEnd();
        }
        // indicates a well-formedness error
        catch (JDOMException | IOException e) {
            e.printStackTrace(System.err);
        }

        return builder.getProduct();
    }


    public void parseChildren(Element current) {

        int eType = startElement(current.getName(), current);

        List children = current.getChildren();
        Iterator iterator = children.iterator();

        while (iterator.hasNext()) {
            Element child = (Element) iterator.next();
            parseChildren(child);
        }

        endElement(eType);
    }

    public int startElement(String eName, Element e) {

        switch (eName) {
            case "stampInfo":
                builder.buildStampInfo(e.getAttributeValue("name"),
                        e.getAttributeValue("role"),
                        e.getAttributeValue("entity"),
                        e.getAttributeValue("editable"),
                        e.getAttributeValue("memo"),
                        e.getAttributeValue("stampId")
                );
                return TT_STAMP_INFO;

            case "node":
                builder.buildNode(e.getAttributeValue("name"));
                return TT_NODE;

            case "root":
                builder.buildRoot(e.getAttributeValue("name"), e.getAttributeValue("entity"));
                return TT_ROOT;

            case "stampTree":
                return TT_STAMP_TREE;

            case "stampBox":
                return TT_STAMP_BOX;

            default:
                break;
        }
        return -1;
    }

    public void endElement(int eType) {

        switch (eType) {
            case TT_NODE:
                builder.buildNodeEnd();
                break;

            case TT_ROOT:
                builder.buildRootEnd();
                break;

            case TT_STAMP_TREE:
                break;

            case TT_STAMP_BOX:
                break;
        }
    }
}
