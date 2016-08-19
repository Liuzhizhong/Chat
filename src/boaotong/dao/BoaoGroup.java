package boaotong.dao;

import java.util.HashSet;
import java.util.Set;
import javax.swing.Icon;

/**
 * BoaoGroup entity. @author MyEclipse Persistence Tools
 */

public class BoaoGroup implements java.io.Serializable {

	// Fields

	private Integer gid;
	private BoaoUser boaoUser;
	private String name;
	private String gonggao;
	private String users;
        private Icon icon;        
        private Set boaoTemps = new HashSet(0);
	private Set boaoRecords = new HashSet(0);
	
        public Icon getIcon(){
            return icon;
        }
        public void setIcon(Icon icon){
            this.icon = icon;
        }
        
	// Constructors 
	/** default constructor */
	public BoaoGroup() {
	}

	/** full constructor */
	public BoaoGroup(BoaoUser boaoUser, String name, String gonggao,
			String users, Set boaoTemps, Set boaoRecords) {
		this.boaoUser = boaoUser;
		this.name = name;
		this.gonggao = gonggao;
		this.users = users;
		this.boaoTemps = boaoTemps;
		this.boaoRecords = boaoRecords;
	}
        
	/** minimal constructor */
	public BoaoGroup(BoaoUser boaoUser, String name) {
		this.boaoUser = boaoUser;
		this.name = name;
	}

	// Property accessors

	public Integer getGid() {
		return this.gid;
	}

	public void setGid(Integer gid) {
		this.gid = gid;
	}

	public BoaoUser getBoaoUser() {
		return this.boaoUser;
	}

	public void setBoaoUser(BoaoUser boaoUser) {
		this.boaoUser = boaoUser;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGonggao() {
		return this.gonggao;
	}

	public void setGonggao(String gonggao) {
		this.gonggao = gonggao;
	}

	public String getUsers() {
		return this.users;
	}

	public void setUsers(String users) {
		this.users = users;
	}
        public Set getBoaoTemps() {
		return this.boaoTemps;
	}

	public void setBoaoTemps(Set boaoTemps) {
		this.boaoTemps = boaoTemps;
	}

	public Set getBoaoRecords() {
		return this.boaoRecords;
	}

	public void setBoaoRecords(Set boaoRecords) {
		this.boaoRecords = boaoRecords;
	}
}