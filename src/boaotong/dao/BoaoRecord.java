package boaotong.dao;

import java.sql.Blob;

/**
 * BoaoRecord entity. @author MyEclipse Persistence Tools
 */

public class BoaoRecord implements java.io.Serializable {

	// Fields

	private Integer rid;
	private BoaoGroup boaoGroup;
	private BoaoUser boaoUserByFuid;
	private BoaoUser boaoUserByTuid;
	private String sendtime;
	private String content;
	private Integer firstlength;
    private Blob blob;

	// Constructors

	/** default constructor */
	public BoaoRecord() {
	}

	/** minimal constructor */
	public BoaoRecord(BoaoUser boaoUserByFuid,Integer firstlength, Blob blob) {
		this.boaoUserByFuid = boaoUserByFuid;
		this.blob = blob;
                this.firstlength = firstlength;
	}

	/** full constructor */
	public BoaoRecord(BoaoGroup boaoGroup, BoaoUser boaoUserByFuid,
			BoaoUser boaoUserByTuid, String sendtime,Integer firstlength, Blob blob) {
		this.boaoGroup = boaoGroup;
		this.boaoUserByFuid = boaoUserByFuid;
		this.boaoUserByTuid = boaoUserByTuid;
		this.sendtime = sendtime;
                this.firstlength = firstlength;
		this.blob = blob;
	}

    public Blob getBlob() {
        return blob;
    }
    public Integer getFirstlength() {
		return firstlength;
	}

	public void setFirstlength(Integer firstlength) {
		this.firstlength = firstlength;
	}
    public void setBlob(Blob blob) {
        this.blob = blob;
    }

	// Property accessors

	public Integer getRid() {
		return this.rid;
	}

	public void setRid(Integer rid) {
		this.rid = rid;
	}

	public BoaoGroup getBoaoGroup() {
		return this.boaoGroup;
	}

	public void setBoaoGroup(BoaoGroup boaoGroup) {
		this.boaoGroup = boaoGroup;
	}

	public BoaoUser getBoaoUserByFuid() {
		return this.boaoUserByFuid;
	}

	public void setBoaoUserByFuid(BoaoUser boaoUserByFuid) {
		this.boaoUserByFuid = boaoUserByFuid;
	}

	public BoaoUser getBoaoUserByTuid() {
		return this.boaoUserByTuid;
	}

	public void setBoaoUserByTuid(BoaoUser boaoUserByTuid) {
		this.boaoUserByTuid = boaoUserByTuid;
	}

	public String getSendtime() {
		return this.sendtime;
	}

	public void setSendtime(String sendtime) {
		this.sendtime = sendtime;
	}

	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}