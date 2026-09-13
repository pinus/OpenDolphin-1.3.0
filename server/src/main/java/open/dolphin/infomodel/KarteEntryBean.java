package open.dolphin.infomodel;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import open.dolphin.util.DateUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.search.mapper.pojo.automaticindexing.ReindexOnUpdate;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexingDependency;

import java.time.LocalDateTime;

/// KarteEntryBean.
///
/// @param <T> type of objects compared to
/// @author Minagawa, Kazushi
@MappedSuperclass
@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
public class KarteEntryBean<T extends KarteEntryBean<T>> extends InfoModel implements Comparable<T> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false)
    private LocalDateTime confirmed;

    @Column(nullable = false)
    private LocalDateTime started;

    private LocalDateTime ended;

    @Column(nullable = false)
    private LocalDateTime recorded;

    private long linkId;

    private String linkRelation;

    @Column(length = 1, nullable = false)
    private String status;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private UserModel creator;

    @IndexedEmbedded            // hibernate search
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.NO)
    @ManyToOne
    @JoinColumn(name = "karte_id", nullable = false)
    private KarteBean karte;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDateTime getConfirmed() { return confirmed; }

    public void setConfirmed(LocalDateTime confirmed) { this.confirmed = confirmed; }

    public LocalDateTime getStarted() { return started; }

    public void setStarted(LocalDateTime started) {
        this.started = started;
    }

    public LocalDateTime getEnded() {
        return ended;
    }

    public void setEnded(LocalDateTime ended) {
        this.ended = ended;
    }

    public LocalDateTime getRecorded() {
        return recorded;
    }

    public void setRecorded(LocalDateTime recorded) {
        this.recorded = recorded;
    }

    public long getLinkId() {
        return linkId;
    }

    public void setLinkId(long linkId) {
        this.linkId = linkId;
    }

    public String getLinkRelation() {
        return linkRelation;
    }

    public void setLinkRelation(String linkRelation) {
        this.linkRelation = linkRelation;
    }

    public String getStatus() { return status; }

    public void setStatus(String status) {
        this.status = status;
    }

    public UserModel getCreator() {
        return creator;
    }

    public void setCreator(UserModel creator) {
        this.creator = creator;
    }

    public KarteBean getKarte() {
        return karte;
    }

    public void setKarte(KarteBean karte) {
        this.karte = karte;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final KarteEntryBean other = (KarteEntryBean) obj;
        return (id == other.getId());
    }

    /// 適合開始日と確定日で比較する.
    ///
    /// @param other 比較対象
    /// @return Comparable の比較値
    @Override
    public int compareTo(T other) {
        if (other != null) {
            LocalDateTime date1 = getStarted();
            LocalDateTime date2 = other.getStarted();
            int result = compareDate(date1, date2);
            if (result == 0) {
                date1 = getConfirmed();
                date2 = other.getConfirmed();
                result = compareDate(date1, date2);
            }
            return result;
        }
        return -1;
    }

    private int compareDate(LocalDateTime date1, LocalDateTime date2) {
        if (date1 == null) {
            // 両方 null なら等しい
            if (date2 == null) { return 0; }
            // null は最上位
            else { return 1; }
        } else {
            // null は最上位
            if (date2 == null) { return -1; }
            // date1 != null && date2 != null の場合
            else { return date1.compareTo(date2); }
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    //
    // 互換性用のプロキシコード
    //
    public LocalDateTime getFirstConfirmed() {
        return getStarted();
    }

    public void setFirstConfirmed(LocalDateTime firstConfirmed) {
        setStarted(firstConfirmed);
    }

    public String getFirstConfirmDate() {
        return getFirstConfirmed().format(DateUtils.ISO_DATE_TIME_FORMATTER);
    }

    public void setFirstConfirmDate(String timeStamp) {
        setFirstConfirmed(LocalDateTime.parse(timeStamp));
    }

    public String getConfirmDate() {
        return getConfirmed().format(DateUtils.ISO_DATE_TIME_FORMATTER);
    }

    public void setConfirmDate(String timeStamp) {
        setConfirmed(LocalDateTime.parse(timeStamp));
    }


    //
    // 足場コード  Date
    //
    public String firstConfirmDateAsString() {
        return getFirstConfirmed().format(DateUtils.ISO_DATE_FORMATTER);
    }

    public String confirmDateAsString() {
        return getConfirmed().format(DateUtils.ISO_DATE_FORMATTER);
    }

    public String startedDateAsString() {
        return getStarted().format(DateUtils.ISO_DATE_FORMATTER);
    }

    public String endedDateAsString() {
        return getEnded().format(DateUtils.ISO_DATE_FORMATTER);
    }

    public String recordedDateAsString() {
        return getRecorded().format(DateUtils.ISO_DATE_FORMATTER);
    }

    //
    // 足場コード  TimeStamp
    //
    public String confirmedTimeStampAsString() {
        return getConfirmed().format(DateUtils.ISO_DATE_TIME_FORMATTER);
    }

    public String startedTimeStampAsString() {
        return getStarted().format(DateUtils.ISO_DATE_TIME_FORMATTER);
    }

    public String endedTimeStampAsString() {
        return getEnded().format(DateUtils.ISO_DATE_TIME_FORMATTER);
    }

    public String recordedTimeStampAsString() {
        return getRecorded().format(DateUtils.ISO_DATE_TIME_FORMATTER);
    }
}
