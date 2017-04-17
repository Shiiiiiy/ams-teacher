package com.uws.teacher.service;

import java.util.List;

import com.uws.core.base.IBaseService;
import com.uws.core.hibernate.dao.support.Page;
import com.uws.domain.base.BaseAcademyModel;
import com.uws.domain.base.BaseClassModel;
import com.uws.domain.teacher.SetClassTeacher;
import com.uws.domain.teacher.SetCollegeTeacher;
import com.uws.domain.teacher.StuJobTeamSetModel;
import com.uws.domain.teacher.TeacherInfoModel;
import com.uws.sys.model.Dic;

public interface IStuJobTeamService extends IBaseService {
	/**
	 * 通过queryAllStuJobTeam视图查找Page
	 * @param setClassTeacher
	 * @param pageNo
	 * @return Page
	 */
	public Page queryPageStuJobTeam(SetClassTeacher setClassTeacher, String roleName, Integer pageNo, Integer pageSize);
	
	public SetClassTeacher getStuJobTeamById(String Id);

	/**
	 * 
	 * @Title: getAllStuJobTeamSet
	 * @Description:所有设置信息
	 * @param po
	 * @param pageNo
	 * @return
	 * @throws
	 */
	public List<StuJobTeamSetModel> getAllStuJobTeamSet();
	/**
	 * 通过学院id查找SetCollegeTeacher（视图）
	 * @param collegeId
	 * @return
	 */
	public SetCollegeTeacher getSetCollegeTeacherById(String collegeId);
	/**
	 * 通过班级id和教师类型id查找StuJobTeamSetModel
	 * @param classId
	 * @param typeId
	 * @return
	 */
	public StuJobTeamSetModel getStuJobByClassIdAndTyId(String classId, String typeId);
	/**
	 * 存储班级学工队伍
	 * @param classId
	 * @param teacherId
	 * @param teacherTypeId
	 */
	public void editStuJobTeamSet(String classId, String teacherId, String teacherTypeId);
	/**
	 * 通过学院id和学工类型id查找StuJobTeamSetModel
	 * @param collegeId
	 * @param id
	 * @return
	 */
	public StuJobTeamSetModel getStuJobByCollegeIdAndTyId(String collegeId, String id);
	/**
	 * 通过教师id，学院id，教师类型id存储学院学工信息StuJobTeamSetModel
	 * @param collegeId
	 * @param teacherId
	 * @param teacherTypeId
	 */
	public void editCollegeJobSet(String collegeId, String teacherId, String teacherTypeId);
	/**
	 * 通过教师id查询教师拓展表信息
	 * @param teacherId
	 * @return
	 */
	public TeacherInfoModel getTeacherExtendByTeacherId(String teacherId);
	/**
	 * 通过当前教师登录的id查找其所在学院
	 * @param userId
	 * @return BaseAcademyModel
	 */
	public BaseAcademyModel getCollegeByTeacherId(String userId);
	/**
	 * 通过教师id查找其职务
	 * @param techerId
	 * @return
	 */
	public List<Dic> getStuJobDicByTeacherId(String techerId);
	/**
	 * 保存教师拓展信息
	 * @param po
	 * @param fileId
	 */
	public void updateTeacherInfo(TeacherInfoModel po, String[] fileId);
	/**
	 * 设置班长
	 * @param classId
	 * @param stuId
	 */
	public void setMonitor(String classId, String stuId);
	/**
	 * 
	 * @param po
	 */
	public void updateBaseClass(BaseClassModel po);
	/**
	 * 班长角色
	 * @param stuId
	 */
	public void setMonitorRole(String stuId);
	/**
	 * 班级辅导员角色
	 * @param classId 班级id
	 * @param teacherType 辅导员code
	 * @param teacherId 教师id
	 * @param roleCode 角色code
	 */
	public void saveKlassCounsellor(String classId, String teacherType, String teacherId, String roleCode);
	/**
	 * 学院辅导员角色
	 * @param collegeId
	 * @param teacherType
	 * @param teacherId
	 * @param string
	 */
	public void saveCollegeCounsellor(String collegeId, String teacherType,
			String teacherId, String roleCode);
	
	/**
	 * 
	 * @Title: deleteSettingById
	 * @Description: 删除设置信息
	 * @param id
	 * @throws
	 */
	public void deleteSettingById(String id);
	
	/**
	 * 
	 * @Title: queryTeacherSettingInfo
	 * @Description: 按照教师工号查询学工队伍的设置信息
	 * @param teacherId
	 * @throws
	 */
	public List<StuJobTeamSetModel> queryTeacherSettingInfo(String teacherId);
	
}
