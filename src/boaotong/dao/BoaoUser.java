package boaotong.dao;
 
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Icon;  

/**
 * BoaoUser entity. @author MyEclipse Persistence Tools
 */

public class BoaoUser implements java.io.Serializable {

    // Fields
    private Integer uid;
    private BoaoDept boaoDept;
    private String logname;
    private String logpwd;
    private String realname;
    private String sex;
    private String birthday;
    private String img;
    private String tel;
    private String place;
    private Integer rid;
    private Integer online;
    private String jianyu;
    private Set boaoGroups = new HashSet(0);
    private Set boaoRecordsForTuid = new HashSet(0);
    private Set boaoRecordsForFuid = new HashSet(0);
    private Set boaoTempsForFuid = new HashSet(0);
    private Set boaoTempsForTuid = new HashSet(0);
    
    private Integer groupId;
    private String groupName;
    private Icon icon; 

    // Constructors 
    /** default constructor */
    public BoaoUser() {
    }

    public BoaoUser(String name){
        this.realname = name;
        this.icon = null;
    }
    public BoaoUser(String name, Icon icon) { 
        this.realname = name;
        this.icon = icon;
    }

    /** minimal constructor */
    public BoaoUser(BoaoDept boaoDept, String logname, String logpwd,
			String realname, String sex) {
		this.boaoDept = boaoDept;
		this.logname = logname;
		this.logpwd = logpwd;
		this.realname = realname;
		this.sex = sex;
	}

    /** full constructor */
    public BoaoUser(BoaoDept boaoDept, String logname, String logpwd,
                    String realname, String sex, String birthday, String img,
                    String tel, String place, Integer rid, Integer online,
                    String jianyu, Set boaoRecordsForTuid, Set boaoRecordsForFuid,
                    Set boaoTempsForFuid, Set boaoTempsForTuid, Set boaoGroups) {
            this.boaoDept = boaoDept;
            this.logname = logname;
            this.logpwd = logpwd;
            this.realname = realname;
            this.sex = sex;
            this.birthday = birthday;
            this.img = img;
            this.tel = tel;
            this.place = place;
            this.rid = rid;
            this.online = online;
            this.jianyu = jianyu;
            this.boaoRecordsForTuid = boaoRecordsForTuid;
            this.boaoRecordsForFuid = boaoRecordsForFuid;
            this.boaoTempsForFuid = boaoTempsForFuid;
            this.boaoTempsForTuid = boaoTempsForTuid;
            this.boaoGroups = boaoGroups;
    }
    // Property accessors
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }    
    public Integer getUid() {
            return this.uid;
    }

    public String getJianyu() {
        return jianyu;
    }

    public void setJianyu(String jianyu) {
        this.jianyu = jianyu;
    }

    public Integer getOnline() {
        return online;
    }

    public void setOnline(Integer online) {
        this.online = online;
    }

    public void setUid(Integer uid) {
            this.uid = uid;
    }

    public BoaoDept getBoaoDept() {
            return this.boaoDept;
    }

    public void setBoaoDept(BoaoDept boaoDept) {
            this.boaoDept = boaoDept;
    }

    public String getLogname() {
            return this.logname;
    }

    public void setLogname(String logname) {
            this.logname = logname;
    }

    public String getLogpwd() {
            return this.logpwd;
    }

    public void setLogpwd(String logpwd) {
            this.logpwd = logpwd;
    }

    public String getRealname() {
            return this.realname;
    }

    public void setRealname(String realname) {
            this.realname = realname;
    }

    public String getSex() {
            return this.sex;
    }

    public void setSex(String sex) {
            this.sex = sex;
    }

    public String getBirthday() {
            return this.birthday;
    }

    public void setBirthday(String birthday) {
            this.birthday = birthday;
    }

    public String getImg() {
            return this.img;
    }

    public void setImg(String img) {
            this.img = img;
    }

    public String getTel() {
            return this.tel;
    }

    public void setTel(String tel) {
            this.tel = tel;
    }

    public String getPlace() {
            return this.place;
    }

    public void setPlace(String place) {
            this.place = place;
    }

    public Integer getRid() {
            return this.rid;
    }

    public void setRid(Integer rid) {
            this.rid = rid;
    }

    public Set getBoaoGroups() {
            return this.boaoGroups;
    }

    public void setBoaoGroups(Set boaoGroups) {
            this.boaoGroups = boaoGroups;
    }

    public String toString() {
            return "<html><font color='red'>" + getRealname() + "</font></html>";
    }

    public Set getBoaoRecordsForFuid() {
        return boaoRecordsForFuid;
    }

    public void setBoaoRecordsForFuid(Set boaoRecordsForFuid) {
        this.boaoRecordsForFuid = boaoRecordsForFuid;
    }

    public Set getBoaoRecordsForTuid() {
        return boaoRecordsForTuid;
    }

    public void setBoaoRecordsForTuid(Set boaoRecordsForTuid) {
        this.boaoRecordsForTuid = boaoRecordsForTuid;
    }

    public Set getBoaoTempsForFuid() {
        return boaoTempsForFuid;
    }

    public void setBoaoTempsForFuid(Set boaoTempsForFuid) {
        this.boaoTempsForFuid = boaoTempsForFuid;
    }

    public Set getBoaoTempsForTuid() {
        return boaoTempsForTuid;
    }

    public void setBoaoTempsForTuid(Set boaoTempsForTuid) {
        this.boaoTempsForTuid = boaoTempsForTuid;
    }
    public void setIcon(Icon icon){
        this.icon = icon;
    } 
    public Icon getIcon(){
        return icon;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }
}