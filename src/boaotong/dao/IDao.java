/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package boaotong.dao;

import java.io.Serializable;
import java.sql.Blob;
import java.util.List;

/**
 *
 * @author Administrator
 */
public interface IDao {
    public List getAllOnLineUsers();
    public List getAllDept();
    public List getMyGroups(Integer uid);
    public List getGroupUsers(String uidStr);
    public BoaoUser loginCheck(String name, String pawd) throws BoaoException;
    public boolean checkGroupForInsert(String name);
    public void save(Object obj);
    public void update(Object obj);
    public void delete(Object obj);
    public Object get(Class cls, Serializable id);
    public List getAllUser();
    public List getAllGroupsNotUid(Integer uid);
    public List getAllGroupTemp();
    public boolean checkFileNameChongFu(String fileName);
    public int saveGroupMessage(List<Integer> tuList, Integer fuid,
			Integer gid,Integer firstLength,Blob blob, String datetime);
}
