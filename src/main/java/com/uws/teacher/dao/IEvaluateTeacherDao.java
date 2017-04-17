package com.uws.teacher.dao;

import java.util.List;

import com.uws.core.hibernate.dao.IBaseDao;
import com.uws.core.hibernate.dao.support.Page;
import com.uws.domain.base.BaseAcademyModel;
import com.uws.domain.base.BaseTeacherModel;
import com.uws.domain.teacher.EvaluateTeacher;
import com.uws.domain.teacher.StuJobTeamSetModel;
import com.uws.domain.teacher.TeacherInfoModel;
import com.uws.sys.model.Dic;
import com.uws.user.model.Org;

public interface IEvaluateTeacherDao extends IBaseDao {

	/**
	 * 通过教师id查询考核表
	 * @param teacherId
	 * @return EvaluateTeacher
	 */
	EvaluateTeacher getEvaluateTeacherByTeacherId(String teacherId);

	/**
	 * 根据教师id查询该教师以往添加考核新的学年
	 * @param teacherId
	 * @return dic
	 */
	List<Dic> getSchoolYearDicByTeacherId(String teacherId);

	/**
	 * 根据orgId获得baseAcademyModel
	 * @param orgId
	 * @return BaseAcademyModel
	 */
	BaseAcademyModel getBaseAcademyByOrgId(String orgId);

	/**
	 * 通过当前学年id遍历考核信息表
	 * @param yearId
	 * @return
	 */
	EvaluateTeacher getEvaluateTeacher(String yearId);

	/**
	 * 遍历教师岗位设置表获得page
	 * @param po
	 * @param pageNo
	 * @return
	 */
	Page queryPageSettingInfo(StuJobTeamSetModel po, int pageSize, int pageNo);
	
	/**
	 * 根据不同条件查询考核表
	 * @param po
	 * @param pageNo
	 * @param collegeId
	 * @return Page
	 */
	Page queryPageEvaluateTeacher(EvaluateTeacher po, int pageNo, String collegeId);
	/**
	 * 
	 * @param pageNo
	 * @param pageSize
	 * @param schoolId
	 * @param collegeId
	 * @return
	 */
	Page queryEvaluateTeacher(Integer pageNo, Integer pageSize, String schoolYearId, String collegeId);
	/**
	 * 根据学年id 学院id 和水平id 查询教师信息
	 * @param schoolYearId
	 * @param collegeId
	 * @param levelId
	 * @return List<BaseTeacherModel>
	 */
	List<BaseTeacherModel> getTeacherBySCL(String schoolYearId, String collegeId, String levelId);

	/**
	 * 查询，用于考核信息查询
	 * @param po
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	Page queryPageEvaluateInfo(EvaluateTeacher po, int pageNo, int pageSize);
}
