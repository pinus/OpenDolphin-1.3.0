package open.dolphin.infomodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/// SchemaModel.
///
/// @author Kazushi Minagawa, Digital Globe, Inc.
@Entity
@Table(name = "d_image")
public class SchemaModel extends KarteEntryBean<SchemaModel> {
    
    @Embedded
    private ExtRefModel extRef;

    // @Lob
    // @Column(nullable = false)
    // masuda-sensei
    // oid(Large Object) 列を lo_get / lo_from_bytea で bytea として読み書きする。
    // JDBC の Blob 読み出し(lo_open/loread の往復)を避けるため。列の型は oid のまま
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @ColumnTransformer(read = "lo_get(jpegByte)", write = "lo_from_bytea(0, ?)")
    @Column(nullable = false, columnDefinition = "oid")
    private byte[] jpegByte;

    @ManyToOne
    @JoinColumn(name = "doc_id", nullable = false)
    private DocumentModel document;

    // Compatible props
    @Transient
    private String fileName;

    @JsonIgnore
    @Transient
    private ImageIcon icon;

    @Transient
    private int imageNumber;

    public ExtRefModel getExtRef() {
        return extRef;
    }

    public void setExtRef(ExtRefModel val) {
        extRef = val;
    }

    public DocumentModel getDocument() {
        return document;
    }

    public void setDocument(DocumentModel document) {
        this.document = document;
    }

    public byte[] getJpegByte() {
        return jpegByte;
    }

    public void setJpegByte(byte[] jpegByte) {
        this.jpegByte = jpegByte;
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public void setIcon(ImageIcon val) {
        icon = val;
    }

    public int getImageNumber() {
        return imageNumber;
    }

    public void setImageNumber(int imageNumber) {
        this.imageNumber = imageNumber;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String val) {
        fileName = val;
    }

    public IInfoModel getModel() {
        return getExtRef();
    }

    public void setModel(IInfoModel val) {
        setExtRef((ExtRefModel) val);
    }

    /**
     * 確定日及びイメージ番号で比較する.
     *
     * @param other 比較対象
     * @return 比較結果
     */
    @Override
    public int compareTo(@NotNull SchemaModel other) {
        int result = super.compareTo(other);
        if (result == 0) {
            // primitive なので比較はOK
            int no1 = getImageNumber();
            int no2 = other.getImageNumber();
            result = no1 - no2;
        }
        return result;
    }
}
