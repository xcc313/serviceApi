package com.lzj.service;

import javax.annotation.Resource;

import com.alibaba.druid.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.lzj.dao.Dao;

import java.sql.SQLException;
import java.util.*;

@Service
public class ApiService {
	
	@Resource
	private Dao dao;
	
	private static final Logger log = LoggerFactory
			.getLogger(ApiService.class);

	/**
	 * 根据查询条件map获取api_list表中所有数据
	 * @param paramsMap
	 * @return
	 * @author lzj
	 */
	public List getApiList(Map paramsMap){
		List<Object> list = new ArrayList<Object>();
		String sql = "select * from api_list where 1=1 ";
		if(paramsMap!=null && !paramsMap.isEmpty()){
			Set<Map.Entry<String, Object>> whereEntries = paramsMap.entrySet();
			for(Map.Entry<String, Object> whereEntry:whereEntries){
				sql += " and "+whereEntry.getKey()+"=? ";
				list.add(whereEntry.getValue());
			}
		}
		if(list!=null && !list.isEmpty()){
			return dao.find(sql,list.toArray());
		}else{
			return dao.find(sql);
		}
	}

	/**
	 * 更新api接口访问量
	 * @param columnName
	 * @param apiCode
	 * @return 受影响的行数
	 * @throws SQLException
	 * @author lzj
	 */
	public int addApiAccessNum(String columnName,String apiCode) throws SQLException {
		String sql = "update api_list set "+columnName+"="+columnName+"+1 where api_code=?";
		return dao.update(sql,apiCode);
	}

	/**
	 * 更新彩票api接口使用量
	 * @param lotteryCode
	 * @return 受影响的行数
	 * @throws SQLException
	 * @author lzj
	 */
	public int addLotteryUseNum(String lotteryCode) throws SQLException {
		String sql = "update lottery_category set use_num=use_num+1 where lottery_code=?";
		return dao.update(sql,lotteryCode);
	}

	/**
	 * 更新api_list表
	 * @param paramsMap 要更新的参数map
	 * @param whereMap sql中的where条件map
	 * @return 受影响的行数
	 * @throws SQLException
	 * @author lzj
	 */
	public int updateApiList(Map<String, Object> paramsMap,Map<String, Object> whereMap) throws SQLException {
		String sql = "update api_list set ";
		List<Object> list = new ArrayList<Object>();
		Set<Map.Entry<String, Object>> entries = paramsMap.entrySet();
		for (Map.Entry<String, Object> entry:entries) {
			System.out.println(entry.getKey()+":"+entry.getValue());
			sql += entry.getKey()+"=?,";
			list.add(entry.getValue());
		}
		sql = sql.substring(0, sql.length()-1);
		sql += " where ";
		Set<Map.Entry<String, Object>> whereEntries = whereMap.entrySet();
		for(Map.Entry<String, Object> whereEntry:whereEntries){
			sql += whereEntry.getKey()+"=? and ";
			list.add(whereEntry.getValue());
		}
		sql = sql.substring(0, sql.lastIndexOf("and"));
		return dao.update(sql, list.toArray());
	}

	/**
	 * 统一查询方法，返回List
	 * @param tableName 要查询的表
	 * @param whereMap 查询where条件map
	 * @param soreColumn 排序字段
	 * @param sort 排序规则 desc倒序  asc顺序
	 * @param limitNum  查询行数
	 * @return List
	 * @author lzj
	 */
	public List getListMethod(String tableName,Map<String,Object> whereMap,String soreColumn,String sort,int limitNum){
		StringBuffer sbSql = new StringBuffer("select * from ");
		sbSql.append(tableName).append(" where 1=1 ");
		List<Object> list = new ArrayList<Object>();
		if(whereMap!=null && !whereMap.isEmpty()){
			Set<Map.Entry<String, Object>> entries = whereMap.entrySet();
			for (Map.Entry<String, Object> entry:entries) {
				String entryValue = String.valueOf(entry.getValue());
				if(entryValue!=null && !"".equals(entryValue)){
					if(entryValue.contains("%")){
						sbSql.append(" and ").append(entry.getKey()).append(" like ? ");
					}else{
						sbSql.append(" and ").append(entry.getKey()).append("=? ");
					}
					list.add(entry.getValue());
				}
			}
		}
		sbSql.append(" order by ").append(soreColumn).append(" ").append(sort);
		if(limitNum!=0){
			sbSql.append(" limit ").append(limitNum);
		}
		String sql = sbSql.toString();
		if(list!=null && !list.isEmpty()){
			return dao.find(sql, list.toArray());
		}else{
			return dao.find(sql);
		}
	}

	/**
	 * 统一查询方法，返回List
	 * @param tableName 要查询的表
	 * @param whereMap 查询where条件map
	 * @param soreColumn 排序字段
	 * @param sort 排序规则 desc倒序  asc顺序
	 * @param limitBeginNum  查询开始行
	 * @param limitNum  查询总行数
	 * @return List
	 * @author lzj
	 */
	public List getListMethod(String tableName,Map<String,Object> whereMap,String soreColumn,String sort,int limitBeginNum,int limitNum){
		StringBuffer sbSql = new StringBuffer("select * from ");
		sbSql.append(tableName).append(" where 1=1 ");
		List<Object> list = new ArrayList<Object>();
		if(whereMap!=null && !whereMap.isEmpty()){
			Set<Map.Entry<String, Object>> entries = whereMap.entrySet();
			for (Map.Entry<String, Object> entry:entries) {
				String entryValue = String.valueOf(entry.getValue());
				if(entryValue!=null && !"".equals(entryValue)){
					if(entryValue.contains("%")){
						sbSql.append(" and ").append(entry.getKey()).append(" like ? ");
					}else{
						sbSql.append(" and ").append(entry.getKey()).append("=? ");
					}
					list.add(entry.getValue());
				}
			}
		}
		sbSql.append(" order by ").append(soreColumn).append(" ").append(sort);
		if(limitNum!=0){
			sbSql.append(" limit ").append(limitBeginNum+","+limitNum);
		}
		String sql = sbSql.toString();
		if(list!=null && !list.isEmpty()){
			return dao.find(sql, list.toArray());
		}else{
			return dao.find(sql);
		}
	}

	/**
	 * 统一查询方法，返回首条Map
	 * @param tableName 要查询的表
	 * @param whereMap 查询where条件map
	 * @param soreColumn 排序字段
	 * @param sort 排序规则 desc倒序  asc顺序
	 * @param limitNum  查询数目
	 * @return Map
	 * @author lzj
	 */
	public Map getOneMethod(String tableName,Map<String,Object> whereMap,String soreColumn,String sort,int limitNum){
		StringBuffer sbSql = new StringBuffer("select * from ");
		sbSql.append(tableName).append(" where 1=1 ");
		List<Object> list = new ArrayList<Object>();
		if(whereMap!=null && !whereMap.isEmpty()){
			Set<Map.Entry<String, Object>> entries = whereMap.entrySet();
			for (Map.Entry<String, Object> entry:entries) {
				String entryValue = String.valueOf(entry.getValue());
				//if(entryValue!=null && !"".equals(entryValue)){
				if(entryValue!=null){
					if(entryValue.contains("%")){
						sbSql.append(" and ").append(entry.getKey()).append(" like ? ");
					}else{
						sbSql.append(" and ").append(entry.getKey()).append("=? ");
					}
					list.add(entry.getValue());
				}else{
					sbSql.append(" and ").append(entry.getKey()).append(" is null ");
				}
			}
		}
		sbSql.append(" order by ").append(soreColumn).append(" ").append(sort);
		if(limitNum!=0){
			sbSql.append(" limit ").append(limitNum);
		}
		String sql = sbSql.toString();
		if(list!=null && !list.isEmpty()){
			return dao.findFirst(sql, list.toArray());
		}else{
			return dao.findFirst(sql);
		}
	}

	/**
	 * 统一插入数据方法
	 * @param tableName  插入的表名
	 * @param paramsMap  插入参数
	 * @return 所插入的id
	 * @throws SQLException
	 * @author lzj
	 */
	public int insertMethod(String tableName,Map<String,Object> paramsMap) throws SQLException {
		StringBuffer sbSql = new StringBuffer("insert into ");
		StringBuffer valueSql = new StringBuffer("");
		sbSql.append(tableName).append("(");
		List<Object> list = new ArrayList<Object>();
		Set<Map.Entry<String, Object>> entries = paramsMap.entrySet();
		for (Map.Entry<String, Object> entry:entries) {
			sbSql.append(entry.getKey()).append(",");
			valueSql.append("?,");
			list.add(entry.getValue());
		}
		sbSql = sbSql.deleteCharAt(sbSql.length()-1);
		sbSql.append(") values(");
		valueSql = valueSql.deleteCharAt(valueSql.length()-1);
		sbSql.append(valueSql).append(")");
		//dao.update(sbSql.toString(), list.toArray());
		return (int)dao.insertReturnId(sbSql.toString(),list.toArray());
	}

	/**
	 * 统一更新数据方法
	 * @param tableName 表名
	 * @param updateMap 更新参数
	 * @param whereMap 条件参数
	 * @return 受影响的行数
	 * @throws SQLException
	 * @author lzj
	 */
	public int updateMethod(String tableName,Map<String,Object> updateMap,Map<String,Object> whereMap) throws SQLException {
		StringBuffer sbSql = new StringBuffer("update ").append(tableName).append(" set ");
		List<Object> list = new ArrayList<Object>();
		Set<Map.Entry<String, Object>> entries = updateMap.entrySet();
		for (Map.Entry<String, Object> entry:entries) {
			sbSql.append(entry.getKey()).append("=?,");
			list.add(entry.getValue());
		}
		sbSql = sbSql.deleteCharAt(sbSql.length()-1);
		sbSql.append(" where 1=1 ");
		Set<Map.Entry<String, Object>> whereEntries = whereMap.entrySet();
		for (Map.Entry<String, Object> entry:whereEntries) {
			sbSql.append(" and ").append(entry.getKey()).append("=?");
			list.add(entry.getValue());
		}
		return dao.update(sbSql.toString(),list.toArray());
	}

	/**
	 * 得到区县区域ID
	 * @param province
	 * @param city
	 * @param town
	 * @return
	 */
	public String getAreaId(String province,String city,String town){
		if(province.contains("省")){
			province = province.substring(0,province.length()-1);
		}
		if(city.contains("市")){
			city = city.substring(0,city.length()-1);
		}
		city = "%"+city+"%";
		if(town.contains("市") || town.contains("县") || town.contains("区")){
			town = town.substring(0,town.length()-1);
		}
		town = "%"+town+"%";
		String searchParentSql = "select * from china_area where name = ? and parentid='0'";
		Map<String,Object> provinceMap = dao.findFirst(searchParentSql,province);
		String provinceId = String.valueOf(provinceMap.get("areaid"));
		log.info("provinceId====" + provinceId);
		String searchAreaSql = "select * from china_area where name like ? and parentid=?";
		Map<String,Object> cityMap = dao.findFirst(searchAreaSql, new Object[]{city, provinceId});
		String cityId = String.valueOf(cityMap.get("areaid"));
		log.info("cityId====" + cityId);
		Map<String,Object> townMap = dao.findFirst(searchAreaSql,new Object[]{town,cityId});
		String townId = String.valueOf(townMap.get("areaid"));
		log.info("townId====" + townId);
		return townId;
	}

	/**
	 * 根据区域id与关键字查询邮编历史记录
	 * @param areaid
	 * @param searchKey
	 * @return
	 */
	public List<Map<String,Object>> getZipcodeHistory(String areaid,String searchKey){
		searchKey = "%"+searchKey+"%";
		String sql = "select * from zipcode_history where areaid=? and (search_key like ? or address like ?) limit 30";
		return dao.find(sql, new Object[]{areaid, searchKey, searchKey});
	}

	/**
	 * 根据订阅类型查询当前时间所处的未通知的订阅信息index
	 * @param subType
	 * @return subIndex
	 */
	public int getNowSubIndex(String subType){
		Calendar nowCalender = Calendar.getInstance();
		int nowHour = nowCalender.get(Calendar.HOUR_OF_DAY);
		String sql = "select sub_index from sub_category where sub_type=? and time_begin<=? and time_end>? and notify_status=0";
		Object indexObj = dao.findBy(sql,"sub_index",new Object[]{subType,nowHour,nowHour});
		if(indexObj==null){
			return 0;
		}else{
			return (Integer)indexObj;
		}
	}

	/**
	 * 根据用户编号获取最后一次给用户发送笑话的id
	 * @param userNo
	 * @return
	 */
	public int getLastHistoryJokeId(String userNo){
		String sql = "select joke_id from joke_history where user_no=? order by id desc";
		Object resultObj = dao.findBy(sql, "joke_id", userNo);
		if(resultObj==null){
			return 0;
		}else{
			return (Integer)resultObj;
		}
	}

	/**
	 * 根据笑话类型，起始id查询指定条数笑话
	 * @param beginId
	 * @param endId
	 * @param limitNum
	 * @param jokeType
	 * @param sort
	 * @return
	 */
	public List<Map<String,Object>> getNextJoke(int beginId,int endId,int limitNum,String jokeType,String sort){
		String sql = "select * from joke_list where 1=1";
		List list = new ArrayList();
		if(!StringUtils.isEmpty(jokeType)){
			sql += " and type=? ";
			list.add(jokeType);
		}
		if(beginId!=0){
			sql += " and id>? ";
			list.add(beginId);
		}
		if(endId!=0){
			sql += " and id<? ";
			list.add(endId);
		}
		sql += " order by id "+sort+" limit ?";
		list.add(limitNum);
		return dao.find(sql,list.toArray());
	}

	/**
	 * 更新订阅类目是否已通知
	 * @param subIndex
	 * @param notifyStatus
	 * @throws SQLException
	 */
	public void updateSubCategory(String subIndex,int notifyStatus) throws SQLException {
		List list = new ArrayList();
		String sql = "update sub_category set notify_status=? ";
		list.add(notifyStatus);
		if(!StringUtils.isEmpty(subIndex)){
			sql += " where sub_index=? ";
			list.add(subIndex);
		}
		dao.update(sql,list.toArray());
	}

	//插入短信发送记录
	public void sendSms(String mobileNo,String smsCode,String resultStatus,String resultMsg,String source) throws SQLException {
		String sql = "insert into sms_log(mobile_no,sms_code,create_time,result_status,result_msg,source) values(?,?,?,?,?,?)";
		dao.insertReturnId(sql, new Object[]{mobileNo, smsCode, new Date(), resultStatus, resultMsg, source});
	}


}
