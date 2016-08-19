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
public class UserDao extends BaseDao implements IDao {
    //提取所有在线用户
    @Override
    public List getAllOnLineUsers(){
        return getHibernateTemplate().find("FROM BoaoUser b WHERE b.online = 1");
    }
    @Override
    public List getAllDept(){
       return getHibernateTemplate().find("FROM BoaoDept bd ORDER BY bd.did ASC");
    }
    
    @Override
    public List getMyGroups(Integer uid){
       return getHibernateTemplate().find("FROM BoaoUser bu WHERE bu.uid=" + uid );
    }
    @Override
    public List getGroupUsers(String uidStr){
       return getHibernateTemplate().find("FROM BoaoUser bu WHERE bu.uid in(" + uidStr + ")");
    }
    @Override
    public BoaoUser loginCheck(String name, String pawd) throws BoaoException {
        List list = getHibernateTemplate().find("FROM BoaoUser bu WHERE bu.logname='" + name + "' AND logpwd='" + pawd + "'");
        if ( list != null && list.size() > 0 ){
            BoaoUser user = (BoaoUser) list.get(0);
            if ( user.getOnline() == 1 )
                throw new BoaoException("该用户已经在线。");
            user.setOnline(1);
            update(user);
            return user;
        }
        return null;
    }
    @Override
    public boolean checkGroupForInsert(String name){
        List list = getHibernateTemplate().find("FROM BoaoGroup bg WHERE bg.name = '" + name + "'");
        if ( list != null && list.size() > 0)
            return true;
        return false;
    }
    //检查离线文件表中是否有此名称的文件
    public boolean checkFileNameChongFu(String fileName){
        List list = getHibernateTemplate().find("FROM BoaoLixian b WHERE b.filename='" + fileName + "'");
        if ( list != null && list.size() > 0 ){
            return true;
        }
        return false;
    }
    @Override
    public synchronized void save(Object obj){
        getHibernateTemplate().save(obj);
    }
    
    @Override
    public synchronized void update(Object obj){
        this.getHibernateTemplate().update(obj);
    }
    
    @Override
    public synchronized void delete(Object obj){
        this.getHibernateTemplate().delete(obj);
    }
    
    @Override
    public Object get(Class cls, Serializable id){
        return getHibernateTemplate().get(cls, id);
    }
    
    @Override
    public List getAllUser(){
       return getHibernateTemplate().find("FROM BoaoUser bu ORDER BY bu.boaoDept.did ASC");
    }
    
    @Override
    public List getAllGroupsNotUid(Integer uid){
       return getHibernateTemplate().find("FROM BoaoGroup bg WHERE bg.boaoUser.uid <> " + uid + " ORDER BY bg.gid ASC");
    }
    @Override
    public List getAllGroupTemp(){
        return getHibernateTemplate().find("FROM BoaoTemp b WHERE b.tuid IS NULL ORDER BY b.rid ASC");
    }
	@Override
	public int saveGroupMessage(List<Integer> tuList, Integer fuid,
			Integer gid,Integer firstLength,Blob blob, String datetime) {
//		try{
//			for( int i = 0; i < tuList.size(); i ++ ){ 
//				BoaoTemp boaoTemp = new BoaoTemp();
//				boaoTemp.setFuid(fuid);
//				boaoTemp.setTuid(tuList.get(i));
//				boaoTemp.setGid(gid);
//				boaoTemp.setFirstlength(firstLength);
//				boaoTemp.setBlob(blob);
//				boaoTemp.setSendtime(datetime);
//				save(boaoTemp);
//			}
//		}catch(Exception e){
//			e.printStackTrace();
//			return 0;
//		}
		return 1;
	}
}
