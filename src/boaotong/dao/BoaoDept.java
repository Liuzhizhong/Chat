package boaotong.dao;

import java.util.HashSet;
import java.util.Set;

/**
 * BoaoDept entity. @author MyEclipse Persistence Tools
 */

public class BoaoDept implements java.io.Serializable {

	// Fields

	private Integer did;
	private String name;
	private Set boaoUsers = new HashSet(0);

	// Constructors

	/** default constructor */
	public BoaoDept() {
	}

	/** minimal constructor */
	public BoaoDept(String name) {
		this.name = name;
	}

	/** full constructor */
	public BoaoDept(String name, Set boaoUsers) {
		this.name = name;
		this.boaoUsers = boaoUsers;
	}

	// Property accessors

	public Integer getDid() {
		return this.did;
	}

	public void setDid(Integer did) {
		this.did = did;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set getBoaoUsers() {
		return this.boaoUsers;
	}

	public void setBoaoUsers(Set boaoUsers) {
		this.boaoUsers = boaoUsers;
	}

}