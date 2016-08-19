package boaotong.dao;

import java.sql.Blob;

/**
 * BoaoTemp entity. @author MyEclipse Persistence Tools
 */

public class BoaoTemp implements java.io.Serializable {

	// Fields

	private Integer rid;
	private Integer gid;
	private Integer fuid;
	private Integer tuid;
	private String sendtime;
	private String content;
    private Integer firstlength;
	private Blob blob;
    private String guid;

	// Constructors

	public Blob getBlob() {
		return blob;
	}

	public void setBlob(Blob blob) {
		this.blob = blob;
	}

	/** default constructor */
	public BoaoTemp() {
	}

	/** minimal constructor */
	public BoaoTemp(Integer fuid,Integer firstlength, Blob blob) {
            this.firstlength = firstlength;
		this.fuid = fuid;
		this.blob = blob;
	}

	/** full constructor */
	public BoaoTemp(Integer gid, Integer fuid,
			Integer tuid, String guid, String sendtime,Integer firstlength, Blob blob) {
		this.gid = gid;
		this.fuid = fuid;
		this.tuid = tuid;
		this.sendtime = sendtime;
        this.firstlength = firstlength;
		this.blob = blob;
        this.guid = guid;
	}

	// Property accessors
        public Integer getFirstlength() {
		return firstlength;
	}

	public void setFirstlength(Integer firstlength) {
		this.firstlength = firstlength;
	}
	public Integer getRid() {
		return this.rid;
	}

	public void setRid(Integer rid) {
		this.rid = rid;
	}

	public Integer getGid() {
		return gid;
	}

	public void setGid(Integer gid) {
		this.gid = gid;
	}

	public Integer getFuid() {
		return fuid;
	}

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

	public void setFuid(Integer fuid) {
		this.fuid = fuid;
	}

	public Integer getTuid() {
		return tuid;
	}

	public void setTuid(Integer tuid) {
		this.tuid = tuid;
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