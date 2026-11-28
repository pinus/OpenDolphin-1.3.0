package open.dolphin.infomodel;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jetbrains.annotations.NotNull;

/// ModuleModel.
/// Field 'model' contains any of BundleMed, BundleDolphin, or ProgressCourse
///
/// @author Kazushi Minagawa, Digital Globe, Inc.
@Entity
@Table(name = "d_module")
public class ModuleModel extends KarteEntryBean<ModuleModel> {

    @Embedded
    private ModuleInfoBean moduleInfo;

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
    @JsonSubTypes({
        @JsonSubTypes.Type(BundleDolphin.class),
        @JsonSubTypes.Type(BundleMed.class),
        @JsonSubTypes.Type(TextStampModel.class),
        @JsonSubTypes.Type(ProgressCourse.class)
    })
    @Transient
    private IInfoModel model;

    // @Lob
    // @Column(nullable = false)
    // masuda-sensei
    // oid(Large Object) 列を lo_get / lo_from_bytea で bytea として読み書きする。
    // JDBC の Blob 読み出し(lo_open/loread の往復)を避けるため。列の型は oid のまま
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @ColumnTransformer(read = "lo_get(beanBytes)", write = "lo_from_bytea(0, ?)")
    @Column(nullable = false, columnDefinition = "oid")
    private byte[] beanBytes;

    @ManyToOne
    @JoinColumn(name = "doc_id", nullable = false)
    private DocumentModel document;

    public ModuleModel() {
        moduleInfo = new ModuleInfoBean();
    }

    public DocumentModel getDocument() {
        return document;
    }

    public void setDocument(DocumentModel document) {
        this.document = document;
    }

    public ModuleInfoBean getModuleInfo() {
        return moduleInfo;
    }

    public void setModuleInfo(ModuleInfoBean moduleInfo) {
        this.moduleInfo = moduleInfo;
    }

    public IInfoModel getModel() { return model; }

    public void setModel(IInfoModel model) { this.model = model; }

    public byte[] getBeanBytes() {
        return beanBytes;
    }

    public void setBeanBytes(byte[] beanBytes) {
        this.beanBytes = beanBytes;
    }

    @Override
    public int compareTo(@NotNull ModuleModel other) {
        if (getClass() == other.getClass()) {
            ModuleInfoBean moduleInfo1 = getModuleInfo();
            ModuleInfoBean moduleInfo2 = other.getModuleInfo();
            return moduleInfo1.compareTo(moduleInfo2);
        }
        return -1;
    }
}
