package open.dolphin.message;

import open.dolphin.infomodel.ClaimBundle;

/**
 * ClaimHelper
 *
 * @author Minagawa,Kazushi
 *
 */
public class ClaimHelper {

    /** 確定日 */
    private String confirmDate;

    /** Creator ID */
    private String creatorId;

    /** Creator 名 */
    private String creatorName;

    /** 診療科コード */
    private String creatorDept;

    /** 診療科名 */
    private String creatorDeptDesc;

    /** 医療資格 */
    private String creatorLicense;

    /** 患者ID */
    private String patientId;

    /** 生成目的 */
    private String generationPurpose;

    /** 文書ID */
    private String docId;

    /** 健康保険 GUID */
    private String healthInsuranceGUID;

    /** 健康保険コード値 */
    private String healthInsuranceClassCode;

    /** 健康保険説明 */
    private String healthInsuranceDesc;

    /** ClaimBundle 配列 */
    private ClaimBundle[] claimBundle;

    private String deptName;
    private String deptCode;
    private String doctorName;
    private String doctorId;
    private String jmariCode;
    private String facilityName;


    public void setConfirmDate(String confirmDate) {
        this.confirmDate = confirmDate;
    }

    public String getConfirmDate() {
        return confirmDate;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorLicense(String creatorLicense) {
        this.creatorLicense = creatorLicense;
    }

    public String getCreatorLicense() {
        return creatorLicense;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setGenerationPurpose(String generationPurpose) {
        this.generationPurpose = generationPurpose;
    }

    public String getGenerationPurpose() {
        return generationPurpose;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getDocId() {
        return docId;
    }

    public void setHealthInsuranceGUID(String healthInsuranceGUID) {
        this.healthInsuranceGUID = healthInsuranceGUID;
    }

    public String getHealthInsuranceGUID() {
        return healthInsuranceGUID;
    }

    public void setHealthInsuranceClassCode(String healthInsuranceClassCode) {
        this.healthInsuranceClassCode = healthInsuranceClassCode;
    }

    public String getHealthInsuranceClassCode() {
        return healthInsuranceClassCode;
    }

    public void setHealthInsuranceDesc(String healthInsuranceDesc) {
        this.healthInsuranceDesc = healthInsuranceDesc;
    }

    public String getHealthInsuranceDesc() {
        return healthInsuranceDesc;
    }

    public void setClaimBundle(ClaimBundle[] claimBundle) {
        this.claimBundle = claimBundle;
    }

    public ClaimBundle[] getClaimBundle() {
        return claimBundle;
    }

    public void addClaimBundle(ClaimBundle val) {
        if (claimBundle == null) {
            claimBundle = new ClaimBundle[1];
            claimBundle[0] = val;
            return;
        }
        int len = claimBundle.length;
        ClaimBundle[] dest = new ClaimBundle[len + 1];
        System.arraycopy(claimBundle, 0, dest, 0, len);
        claimBundle = dest;
        claimBundle[len] = val;
    }

    public String getCreatorDept() {
        return creatorDept;
    }

    public void setCreatorDept(String creatorDept) {
        this.creatorDept = creatorDept;
    }

    public String getCreatorDeptDesc() {
        return creatorDeptDesc;
    }

    public void setCreatorDeptDesc(String creatorDeptDesc) {
        this.creatorDeptDesc = creatorDeptDesc;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getDeptCode() {
        return deptCode;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getJmariCode() {
        return jmariCode;
    }

    public void setJmariCode(String jmariCode) {
        this.jmariCode = jmariCode;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }
}
