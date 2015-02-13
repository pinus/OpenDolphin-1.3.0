package open.dolphin.dao;

import open.dolphin.client.ClientContext;
import org.apache.log4j.Logger;

/**
 * DaoBean
 * host address, port, user, password レベル
 * @author  Kazushi Minagawa
 * modified by pns
 */
public class DaoBean {

    public static final int TT_NONE              = 10;
    public static final int TT_NO_ERROR          =  0;
    public static final int TT_CONNECTION_ERROR  = -1;
    public static final int TT_DATABASE_ERROR    = -2;
    public static final int TT_UNKNOWN_ERROR     = -3;

    private String host;
    private int port;
    private String user;
    private String passwd;

    private int errorCode;
    private String errorMessage;

    protected Logger logger;

    /**
     * Creates a new instance of DaoBean
     */
    public DaoBean() {
        logger = ClientContext.getBootLogger();
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public boolean isNoError() {
        return errorCode == TT_NO_ERROR ? true : false;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * 例外を解析しエラーコードとエラーメッセージを設定する。
     *
     * @param e Exception
     */
    protected void processError(Exception e) {

        StringBuilder sb  = new StringBuilder();

        if (e instanceof org.postgresql.util.PSQLException) {
            setErrorCode(TT_CONNECTION_ERROR);
            sb.append("サーバに接続できません。ネットワーク環境をお確かめください。");
            sb.append("\n");
            sb.append(appenExceptionInfo(e));
            setErrorMessage(sb.toString());

        } else if (e instanceof java.sql.SQLException) {
            setErrorCode(TT_DATABASE_ERROR);
            sb.append("データベースアクセスエラー");
            sb.append("\n");
            sb.append(appenExceptionInfo(e));
            setErrorMessage(sb.toString());
        } else {
            setErrorCode(TT_UNKNOWN_ERROR);
            sb.append("アプリケーションエラー");
            sb.append("\n");
            sb.append(appenExceptionInfo(e));
            setErrorMessage(sb.toString());
        }
        //System.out.println(getErrorMessage());
        e.printStackTrace(System.err);
    }

    /**
     * 例外の持つ情報を加える。
     * @param e 例外
     */
    protected String appenExceptionInfo(Exception e) {

        StringBuilder sb  = new StringBuilder();
        sb.append("例外クラス: ");
        sb.append(e.getClass().getName());
        sb.append("\n");
        if (e.getCause() != null && e.getCause().getMessage() != null) {
            sb.append("原因: ");
            sb.append(e.getCause().getMessage());
            sb.append("\n");
        }
        if (e.getMessage() != null) {
            sb.append("内容: ");
            sb.append(e.getMessage());
        }

        return sb.toString();
    }
}
