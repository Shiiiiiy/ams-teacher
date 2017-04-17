package com.uws.teacher.service;

import java.util.List;

import com.uws.core.base.IBaseService;
import com.uws.core.hibernate.dao.support.Page;
import com.uws.domain.base.BaseAcademyModel;
import com.uws.domain.base.BaseTeacherModel;
import com.uws.domain.teacher.EvaluateTeacher;
import com.uws.domain.teacher.StuJobTeamSetModel;
import com.uws.sys.model.Dic;
/**
 * 
 * @className IEvaluateTeacherService.java
 * @package com.uws.teacher.service
 * @description
 * @author houyue
 * @date 2015-8-28  下午4:28:05
 */
public interface IEvaluateTeacherService extends IBaseService {
	/**
	 * 通过id查询EvaluateTeacher表单
	 * @param id
	 * @return EvaluateTeacher
	 */
	EvaluateTeacher getEvaluateTeacherById(String id);

	/**
	 * 保存教师提交的考核信息
	 * @param po
	 * @param fileId
	 */
	void saveEvaluateInfo(EvaluateTeacher po, String[] fileId);

	/**
	 * 更新教师的考核信息
	 * @param po
	 * @param fileId
	 */
	void updateEvaluateInfo(EvaluateTeacher po, String[] fileId);

	/**
	 * 通过教师id查询考核表
	 * @param teacherId
	 * @return EvaluateTeacher
	 */
	EvaluateTeacher getEvaluateTeacherByTeacherId(String teacherId);

	/**
	 * 查询考核表，判断该教师是否已经新增了考核信息
	 * @param curYear
	 * @param teacherId
	 * @return string
	 */
	String judgement(Dic curYear,String teacherId);

	/**
	 * 得到该教师的职位信息
	 * @param teacherId
	 * @param curPosition
	 * @return string
	 */
	String getPosition(List<Dic> curPosition);
	/**
	 * 通过orgId获得BaseAcademyModel
	 * @param id
	 * @return BaseAcademyModel
	 */
	BaseAcademyModel getBaseAcademyByOrgId(String orgId);
	/**
	 * 更新审核信息
	 * @param po
	 */
	void updateAuditInfo(EvaluateTeacher po);

	/**
	 * 通过当前学年id遍历考核信息表
	 * @param yearId
	 * @return EvaluateTeacher
	 */
	EvaluateTeacher getEvaluateTeacher(String yearId);

	/**
	 * 查询教师岗位设置表
	 * @param po
	 * @param pageNo
	 * @return
	 */
	Page queryPageSettingInfo(StuJobTeamSetModel po, int pageSize, int pageNo);
	/**
	 * 用于考核查询（根据学院或者）
	 * @param po
	 * @param pageNo
	 * @param collegeId
	 * @param queryStatus
	 * @return Page
	 */
	Page queryPageEvaluateTeacher(EvaluateTeacher po, int pageNo, String collegeId);
	/**
	 * 通过学年Id和学院Id查询统计信息
	 * @param pageNo
	 * @param pageSize
	 * @param schoolYearId
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
	 * 删除教师个人考核信息
	 * @param id
	 */
	public void deleteEvaluateInfo(String id);

	/**
	 * 查询用于考核信息查询列表
	 * @param po
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	Page queryPageEvaluateInfo(EvaluateTeacher po, int pageNo, int pageSize);
}
