package com.uws.teacher.dao;

import java.util.List;

import com.uws.core.hibernate.dao.IBaseDao;
import com.uws.core.hibernate.dao.support.Page;
import com.uws.domain.base.BaseAcademyModel;
import com.uws.domain.teacher.SetClassTeacher;
import com.uws.domain.teacher.StuJobTeamSetModel;
import com.uws.domain.teacher.TeacherInfoModel;
import com.uws.sys.model.Dic;

public interface IStuJobTeamDao extends IBaseDao{
	/**
	 * 班级学工队伍列表查询
	 * @param pageNo 页数
	 * @param pageSize 每页有几条记录
	 * @param StudentInfoModel 学生基本信息Po
	 * @return page
	 */
	public Page queryPageStuJobTeam(SetClassTeacher setClassTeacher, String roleName, Integer pageNo, Integer pageSize);
	/**
	 * 学院学工队伍列表查询
	 * @param po
	 * @param pageNo
	 * @return
	 */
	public List<StuJobTeamSetModel> getAllStuJobTeamSet();
	/**
	 * 根据班级id和教师类型id查找StuJobTeamSetModel
	 * @param klassId
	 * @param teacherTypeId
	 * @return
	 */
	public StuJobTeamSetModel getStuJobByClassIdAndTyId(String klassId, String teacherTypeId);
	/**
	 * 通过学院id和教师类型id查找StuJobTeamSetModel
	 * @param collegeId
	 * @param teacherTypeId
	 * @return
	 */
	public StuJobTeamSetModel getStuJobByCollegeIdAndTyId(String collegeId, String teacherTypeId);
	/**
	 * 根据教师基础表id查询教师拓展表TeacherInfoModel
	 * @param teacherId
	 * @return
	 */
	public TeacherInfoModel getTeacherExtendByTeacherId(String teacherId);
	/**
	 * 通过当前登录人的id查找其所在学院
	 * @param userId
	 * @return BaseAcademyModel
	 */
	public BaseAcademyModel getCollegeByTeacherId(String userId);
	/**
	 * 根据教师id查找其学工身份
	 * @param techerId
	 * @return
	 */
	public List<Dic> getStuJobDicByTeacherId(String techerId);
	/**
	 * 通过教师id返回此人List<StuJobTeamSetModel>
	 * @param teacherId
	 * @return List<StuJobTeamSetModel>
	 */
	public List<StuJobTeamSetModel> queryExistById(String teacherId);
	/**
	 * 通过教师id和教师类型返回List<StuJobTeamSetModel>
	 * @param teacherId
	 * @param teacherType
	 * @return List<StuJobTeamSetModel>
	 */
	public List<StuJobTeamSetModel> getStuJobByTeacherIdAndTyId(String teacherId, String teacherType);
	
	
	/**
	 * 
	 * @Title: getStuJobByConditions
	 * @Description: 学院辅导员设置类型
	 * @param collegeId
	 * @param teacherId
	 * @param teacherType
	 * @return
	 * @throws
	 */
	public StuJobTeamSetModel getStuJobByConditions(String collegeId ,String teacherId, String teacherType);
	
}
