package open.dolphin.client;

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;

import open.dolphin.infomodel.ModuleInfoBean;


/**
 * StampTree XML builder.
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class DefaultStampTreeXmlBuilder {

    private static final String[] MATCHES = new String[] { "<", ">", "&", "'","\""};

    private static final String[] REPLACES = new String[] { "&lt;", "&gt;", "&amp;" ,"&apos;", "&quot;"};

    /** Control staffs */
    private LinkedList<StampTreeNode> linkedList;
//  private BufferedWriter writer;
    protected BufferedWriter writer;
//    private StringWriter stringWriter;
    protected StringWriter stringWriter;
    private StampTreeNode rootNode;

    private Logger logger;

    /**
     * Creates new DefaultStampTreeXmlBuilder
     */
    public DefaultStampTreeXmlBuilder() {
        super();
        logger = ClientContext.getBootLogger();
    }

    /**
     * Return the product of this builder
     * @return StampTree XML data
     */
    public String getProduct() {
        String result = stringWriter.toString();
        if (logger != null) {
            logger.debug(result);
        }
        return result;
    }

    public void buildStart() throws IOException {
        if (logger != null) {
            logger.debug("StampTree Build start");
        }
        stringWriter = new StringWriter();
        writer = new BufferedWriter(stringWriter);
        writer.write("<stampTree project=");
        writer.write(addQuote("open.dolphin"));
        writer.write(" version=");
        writer.write(addQuote("1.0"));
        writer.write(">\n");
    }

    public void buildRoot(StampTreeNode root) throws IOException {
        if (logger != null) {
            logger.debug("Build Root Node: " + root.toString());
        }
        rootNode = root;
        TreeInfo treeInfo = (TreeInfo)rootNode.getUserObject();
        writer.write("<root name=");
        writer.write(addQuote(treeInfo.getName()));
        writer.write(" entity=");
        writer.write(addQuote(treeInfo.getEntity()));
        writer.write(">\n");
        linkedList = new LinkedList<StampTreeNode>();
        linkedList.addFirst(rootNode);
    }

    public void buildNode(StampTreeNode node) throws IOException {

        if ( node.isLeaf() ) {
            buildLeafNode(node);
        } else {
            buildDirectoryNode(node);
        }
    }

    private void buildDirectoryNode(StampTreeNode node) throws IOException {

        /********************************************************
         ** 子ノードを持たないディレクトリノードは書き出さない **
         ********************************************************/
        if (node.getChildCount() != 0) {

            if (logger != null) {
                logger.debug("Build Directory Node: " + node.toString());
            }

            StampTreeNode myParent = (StampTreeNode) node.getParent();
            StampTreeNode curNode = getCurrentNode();

            if (myParent != curNode) {
                closeBeforeMyParent(myParent);
            }
            linkedList.addFirst(node);

            writer.write("<node name=");
            // 特殊文字を変換する
            String val = toXmlText(node.toString());
            writer.write(addQuote(val));
            writer.write(">\n");
        }
    }

//  private void buildLeafNode(StampTreeNode node) throws IOException {
    protected void buildLeafNode(StampTreeNode node) throws IOException {

        if (logger != null) {
            logger.debug("Build Leaf Node: " + node.toString());
        }

        StampTreeNode myParent = (StampTreeNode) node.getParent();
        StampTreeNode curNode = getCurrentNode();

        if (myParent != curNode) {
            closeBeforeMyParent(myParent);
        }

        // 特殊文字を変換する
        writer.write("<stampInfo name=");
        String val = toXmlText(node.toString());
        writer.write(addQuote(val));

        ModuleInfoBean info = (ModuleInfoBean) node.getUserObject();

        writer.write(" role=");
        writer.write(addQuote(info.getStampRole()));

        writer.write(" entity=");
        writer.write(addQuote(info.getEntity()));

        writer.write(" editable=");
        val = String.valueOf(info.isEditable());
        writer.write(addQuote(val));

        val = info.getStampMemo();
        if (val != null) {
            writer.write(" memo=");
            val = toXmlText(val);
            writer.write(addQuote(val));
        }

        if (info.isSerialized()) {
            val = info.getStampId();
            writer.write(" stampId=");
            writer.write(addQuote(val));
        }
        writer.write("/>\n");
    }

    public void buildRootEnd() throws IOException {

        if (logger != null) {
            logger.debug("Build Root End");
        }
        closeBeforeMyParent(rootNode);
        writer.write("</root>\n");
    }

    public void buildEnd() throws IOException {
        if (logger != null) {
            logger.debug("Build end");
        }
        writer.write("</stampTree>\n");
        writer.flush();
    }

//  private StampTreeNode getCurrentNode() {
    protected StampTreeNode getCurrentNode() {
        return (StampTreeNode) linkedList.getFirst();
    }

//  private void closeBeforeMyParent(StampTreeNode parent) throws IOException {
    protected void closeBeforeMyParent(StampTreeNode parent) throws IOException {

        int index = linkedList.indexOf(parent);

        if (logger != null) {
            logger.debug("Close before my parent: " + index);
        }
        for (int j = 0; j < index; j++) {
            writer.write("</node>\n");
            linkedList.removeFirst();
        }
    }

//  private String addQuote(String s) {
    protected String addQuote(String s) {
        StringBuffer buf = new StringBuffer();
        buf.append("\"");
        buf.append(s);
        buf.append("\"");
        return buf.toString();
    }

    /**
     * 特殊文字を変換する。
     */
//  private String toXmlText(String text) {
    protected String toXmlText(String text) {
        for (int i = 0; i < REPLACES.length; i++) {
            text = text.replaceAll(MATCHES[i], REPLACES[i]);
        }
        return text;
    }
}
