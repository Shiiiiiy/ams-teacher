package com.uws.teacher.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.uws.core.hibernate.dao.impl.BaseDaoImpl;
import com.uws.core.hibernate.dao.support.Page;
import com.uws.core.util.DataUtil;
import com.uws.core.util.DateUtil;
import com.uws.core.util.HqlEscapeUtil;
import com.uws.domain.base.BaseAcademyModel;
import com.uws.domain.base.BaseTeacherModel;
import com.uws.domain.teacher.EvaluateTeacher;
import com.uws.domain.teacher.StuJobTeamSetModel;
import com.uws.sys.model.Dic;
import com.uws.sys.service.DicUtil;
import com.uws.sys.service.impl.DicFactory;
import com.uws.teacher.dao.IEvaluateTeacherDao;

/**
 * @className EvaluateTeacherDaoImpl.java
 * @package com.uws.teacher.dao.impl
 * @description
 * @author houyue
 * @date 2015-8-28  下午4:22:07
 */
@Repository("com.uws.teacher.dao.impl.EvaluateTeacherDaoImpl")
public class EvaluateTeacherDaoImpl extends BaseDaoImpl implements IEvaluateTeacherDao {
	@Autowired
	private static DicUtil dicUtil = DicFactory.getDicUtil();

	@Override
	public Page queryPageEvaluateTeacher(EvaluateTeacher po, int pageNo, String collegeId) {
		String hql = new String("from EvaluateTeacher t where 1=1 ");;
		Map<String, Object> params = new HashMap<String, Object>();
		//学院
		if(DataUtil.isNotNull(po) && DataUtil.isNotNull(po.getCollege()) && DataUtil.isNotNull(po.getCollege().getId())){
			hql += "and college.id = :collegeId ";
			params.put("collegeId", po.getCollege().getId());
		}else if(DataUtil.isNotNull(collegeId)){
			hql += "and college.id = :collegeId ";
			params.put("collegeId", collegeId);
		}
		//学年
			if(DataUtil.isNotNull(po) && DataUtil.isNotNull(po.getSchoolYear()) && DataUtil.isNotNull(po.getSchoolYear().getId())){
				hql += "and schoolYear.id = :schoolYearId ";
				params.put("schoolYearId", po.getSchoolYear().getId());
			}
		//状态
		if(DataUtil.isNotNull(po) && DataUtil.isNotNull(po.getStatus()) && DataUtil.isNotNull(po.getStatus().getId())){
			hql += "and status.id = :statusId ";
			params.put("statusId", po.getStatus().getId());
		}else{
			//可以通过collegeId是否为空来判断是学院审核还是学生处审核
			if(DataUtil.isNotNull(collegeId)) {
				//学院审核，显示除了保存状态下的其他状态的信息
				hql += "and status.id != :saveStatusId ";
				params.put("saveStatusId", dicUtil.getDicInfo("AUDIT_STATUS", "SAVE").getId());
			}else {
				//学生处审核
				hql += "and status.id = :stuAudit or status.id = :passStatus or (status.id = :rejectStatus and stuAffairsAuditStatus = :stuAuditStatus)";
				params.put("stuAudit", dicUtil.getDicInfo("AUDIT_STATUS", "STU_AFFAIRS_AUDIT").getId());
				params.put("passStatus", dicUtil.getDicInfo("AUDIT_STATUS", "PASS").getId());
				params.put("rejectStatus", dicUtil.getDicInfo("AUDIT_STATUS", "REJECT").getId());
				params.put("stuAuditStatus", "拒绝");
			}
		}
		//姓名
		if(DataUtil.isNotNull(po) && DataUtil.isNotNull(po.getTeacher()) && DataUtil.isNotNull(po.getTeacher().getName())){
			hql += "and teacher.name like :name ";
			if(HqlEscapeUtil.IsNeedEscape(po.getTeacher().getName())) {
				params.put("name", "%"+HqlEscapeUtil.escape(po.getTeacher().getName())+"%");
				hql += HqlEscapeUtil.HQL_ESCAPE;
			}else {
				params.put("name", "%"+po.getTeacher().getName()+"%");
			}
			
		}
		//工号
		if(DataUtil.isNotNull(po) && DataUtil.isNotNull(po.getTeacher()) && DataUtil.isNotNull(po.getTeacher().getCode())){
			hql += "and teacher.code like :code ";
			if(HqlEscapeUtil.IsNeedEscape(po.getTeacher().getCode())) {
				params.put("code", "%"+HqlEscapeUtil.escape(po.getTeacher().getCode())+"%");
				hql += HqlEscapeUtil.HQL_ESCAPE;
			}else {
				params.put("code", "%"+po.getTeacher().getCode()+"%");
			}
		}
		hql +="order by college.id desc, schoolYear.id desc ";
		return this.pagedQuery(hql, params, Page.DEFAULT_PAGE_SIZE, pageNo);
	}
	@Override
	public EvaluateTeacher getEvaluateTeacherByTeacherId(String teacherId) {
		String hql = "from EvaluateTeacher t where t.teacher.id = ? ";
		return (EvaluateTeacher) this.queryUnique(hql, new String[]{teacherId});
	}

	@Override
	public List<Dic> getSchoolYearDicByTeacherId(String teacherId) {
		String hql = "select t.schoolYear from EvaluateTeacher t where t.teacher.id = ? ";
		return this.query(hql, new String[]{teacherId});
	}
	@Override
	public BaseAcademyModel getBaseAcademyByOrgId(String orgId) {
		String hql = "from BaseAcademyModel t where t.id = ? ";
		return (BaseAcademyModel) this.queryUnique(hql, new String[]{orgId});
	}

	@Override
	public EvaluateTeacher getEvaluateTeacher(String yearId) {
		String hql = "from  EvaluateTeacher t where t.schoolYear.id = ? ";
		return (EvaluateTeacher) this.query(hql, new String[]{yearId});
	}

	@Override
	public Page queryPageSettingInfo(StuJobTeamSetModel po, int pageSize, int pageNo) {
		Map<String, Object> params = new HashMap<String, Object>();
		StringBuilder hql = new StringBuilder("from BaseTeacherModel b where b.id in ( select t.teacher.id from StuJobTeamSetModel " +
				"t where t.teacher.id not in " +
				"(select t.teacher.id from EvaluateTeacher t where t.schoolYear.id = (:yearId) ) " +
				"group by t.teacher.id )");
		//学院
		if(DataUtil.isNotNull(po.getCollege()) && DataUtil.isNotNull(po.getCollege().getId())) {
			hql.append(" and b.org.id = (:collegeId) ");
			params.put("collegeId", po.getCollege().getId());
		}
		//姓名
		if(DataUtil.isNotNull(po.getTeacher()) && DataUtil.isNotNull(po.getTeacher().getName().trim())) {
			hql.append(" and b.name like (:teacherName) ");
			if(HqlEscapeUtil.IsNeedEscape(po.getTeacher().getName().trim())) {
				params.put("teacherName", "%"+HqlEscapeUtil.escape(po.getTeacher().getName().trim())+"%");
				hql.append(HqlEscapeUtil.HQL_ESCAPE);
			}else {
				params.put("teacherName", "%" + po.getTeacher().getName().trim() + "%");
			}
		}
		//工号
		if(DataUtil.isNotNull(po.getTeacher()) && DataUtil.isNotNull(po.getTeacher().getCode().trim())) {
			hql.append(" and b.code like (:teacherCode) ");
			if(HqlEscapeUtil.IsNeedEscape(po.getTeacher().getCode().trim())) {
				params.put("teacherCode", "%"+HqlEscapeUtil.escape(po.getTeacher().getCode().trim())+"%");
				hql.append(HqlEscapeUtil.HQL_ESCAPE);
			}else {
				params.put("teacherCode", "%" + po.getTeacher().getCode().trim() + "%");
			}
		}
		String yearId = dicUtil.getDicInfo("YEAR", String.valueOf(DateUtil.getCurYear())).getId();
		params.put("yearId", yearId);
		hql.append("order by b.org.id desc, b.code asc");
		return this.pagedQuery(hql.toString(), params, pageSize, pageNo);
	}

	@Override
	public Page queryEvaluateTeacher(Integer pageNo, Integer pageSize,
			String schoolYearId, String collegeId) {
		StringBuffer hql = new StringBuffer("from CountTeacherEvaluate t where 1=1 ");
		List<Object> terms = new ArrayList<Object>();
		if(DataUtil.isNotNull(schoolYearId)) {
			hql.append("and t.schoolYear.id = ? ");
			terms.add(schoolYearId);
		}
		if(DataUtil.isNotNull(collegeId)) {
			hql.append("and t.college.id = ? ");
			terms.add(collegeId);
		}
		return this.pagedQuery(hql.toString(), pageNo, pageSize, terms.toArray());
	}
	@Override
	public List<BaseTeacherModel> getTeacherBySCL(String schoolYearId,
			String collegeId, String levelId) {
		StringBuffer hql = new StringBuffer("select t.teacher from EvaluateTeacher t " +
				"where t.schoolYear.id = ? and t.college.id = ? and t.stuAffairsAuditLevel.id = ? ");
		return (List<BaseTeacherModel>)this.query(hql.toString(), new String[]{schoolYearId, collegeId, levelId});
		
	}
	@Override
	public Page queryPageEvaluateInfo(EvaluateTeacher po, int pageNo,
			int pageSize) {
		String hql = new String("from EvaluateTeacher t where 1=1 ");;
		Map<String, Object> params = new HashMap<String, Object>();
		//获得当前教师的信息
		if(DataUtil.isNotNull(po) && DataUtil.isNotNull(po.getTeacher()) && DataUtil.isNotNull(po.getTeacher().getId())){
			hql += "and teacher.id = :teacherId ";
			params.put("teacherId", po.getTeacher().getId());
		}
		//学院
		if(DataUtil.isNotNull(po) && DataUtil.isNotNull(po.getCollege()) && DataUtil.isNotNull(po.getCollege().getId())){
			hql += "and college.id = :collegeId ";
			params.put("collegeId", po.getCollege().getId());
		}
		//学年
			if(DataUtil.isNotNull(po) && DataUtil.isNotNull(po.getSchoolYear()) && DataUtil.isNotNull(po.getSchoolYear().getId())){
				hql += "and schoolYear.id = :schoolYearId ";
				params.put("schoolYearId", po.getSchoolYear().getId());
			}
		//状态
		if(DataUtil.isNotNull(po) && DataUtil.isNotNull(po.getStatus()) && DataUtil.isNotNull(po.getStatus().getId())){
			hql += "and status.id = :statusId ";
			params.put("statusId", po.getStatus().getId());
		}
		//姓名
		if(DataUtil.isNotNull(po) && DataUtil.isNotNull(po.getTeacher()) && DataUtil.isNotNull(po.getTeacher().getName())){
			hql += "and teacher.name like :name ";
			if(HqlEscapeUtil.IsNeedEscape(po.getTeacher().getName())) {
				params.put("name", "%"+HqlEscapeUtil.escape(po.getTeacher().getName())+"%");
				hql += HqlEscapeUtil.HQL_ESCAPE;
			}else {
				params.put("name", "%"+po.getTeacher().getName()+"%");
			}
			
		}
		//工号
		if(DataUtil.isNotNull(po) && DataUtil.isNotNull(po.getTeacher()) && DataUtil.isNotNull(po.getTeacher().getCode())){
			hql += "and teacher.code like :code ";
			if(HqlEscapeUtil.IsNeedEscape(po.getTeacher().getCode())) {
				params.put("code", "%"+HqlEscapeUtil.escape(po.getTeacher().getCode())+"%");
				hql += HqlEscapeUtil.HQL_ESCAPE;
			}else {
				params.put("code", "%"+po.getTeacher().getCode()+"%");
			}
		}
		hql +="order by college.id desc, schoolYear.id desc ";
		return this.pagedQuery(hql, params, pageSize, pageNo);
	}
}
