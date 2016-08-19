package boaotong.dao;

/**
 * BoaoLixian entity. @author MyEclipse Persistence Tools
 */

public class BoaoLixian implements java.io.Serializable {

	// Fields

	private Integer rid;
	private Integer fuid;
	private Integer tuid;
	private String filesize;
	private String filename;
	private String sendtime;
	private Boolean isread;

	// Constructors

	/** default constructor */
	public BoaoLixian() {
	}

	/** minimal constructor */
	public BoaoLixian(Integer fuid, Integer tuid, String filesize,
			String filename, Boolean isread) {
		this.fuid = fuid;
		this.tuid = tuid;
		this.filesize = filesize;
		this.filename = filename;
		this.isread = isread;
	}

	/** full constructor */
	public BoaoLixian(Integer fuid, Integer tuid, String filesize,
			String filename, String sendtime, Boolean isread) {
		this.fuid = fuid;
		this.tuid = tuid;
		this.filesize = filesize;
		this.filename = filename;
		this.sendtime = sendtime;
		this.isread = isread;
	}

	// Property accessors

	public Integer getRid() {
		return this.rid;
	}

	public void setRid(Integer rid) {
		this.rid = rid;
	}

	public Integer getFuid() {
		return this.fuid;
	}

	public void setFuid(Integer fuid) {
		this.fuid = fuid;
	}

	public Integer getTuid() {
		return this.tuid;
	}

	public void setTuid(Integer tuid) {
		this.tuid = tuid;
	}

	public String getFilesize() {
		return this.filesize;
	}

	public void setFilesize(String filesize) {
		this.filesize = filesize;
	}

	public String getFilename() {
		return this.filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getSendtime() {
		return this.sendtime;
	}

	public void setSendtime(String sendtime) {
		this.sendtime = sendtime;
	}

	public Boolean getIsread() {
		return this.isread;
	}

	public void setIsread(Boolean isread) {
		this.isread = isread;
	}

}