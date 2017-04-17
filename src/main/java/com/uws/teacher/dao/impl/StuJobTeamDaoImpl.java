package com.uws.teacher.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.uws.core.hibernate.dao.impl.BaseDaoImpl;
import com.uws.core.hibernate.dao.support.Page;
import com.uws.core.util.DataUtil;
import com.uws.domain.base.BaseAcademyModel;
import com.uws.domain.teacher.SetClassTeacher;
import com.uws.domain.teacher.StuJobTeamSetModel;
import com.uws.domain.teacher.TeacherInfoModel;
import com.uws.sys.model.Dic;
import com.uws.teacher.dao.IStuJobTeamDao;

@Repository("stuJobTeamDao")
public class StuJobTeamDaoImpl extends BaseDaoImpl implements IStuJobTeamDao {
	@Override
	public Page queryPageStuJobTeam(SetClassTeacher setClassTeacher, String roleName, Integer pageNo, Integer pageSize) {
		StringBuilder hql = new StringBuilder("from SetClassTeacher where 1=1 ");
		Map<String, Object> params = new HashMap<String, Object>();
		//班级
		if (DataUtil.isNotNull(setClassTeacher.getKlass()) && DataUtil.isNotNull(setClassTeacher.getKlass().getId())) {
			hql.append("and klass.id = :klass ");
			params.put("klass", setClassTeacher.getKlass().getId());
		}
		//年级
		if (DataUtil.isNotNull(setClassTeacher.getGrade())) {
			hql.append("and grade = :grade ");
			params.put("grade", setClassTeacher.getGrade());
		}
		//专业
		if (DataUtil.isNotNull(setClassTeacher.getMajor()) && DataUtil.isNotNull(setClassTeacher.getMajor().getId())) {
			hql.append("and major.id = :major ");
			params.put("major", setClassTeacher.getMajor().getId());
		}
		//学院
		if (DataUtil.isNotNull(setClassTeacher.getCollege()) && DataUtil.isNotNull(setClassTeacher.getCollege().getId())) {
			hql.append( "and college.id = :college ");
			params.put("college", setClassTeacher.getCollege().getId());
		}
		if(roleName!=null && DataUtil.isNotNull(roleName.trim())) {
			hql.append( "and (headMasterName like :headMaster or teacherCounsellorName like :teacherCounsellor or monitorName like :monitor ) ");
			params.put("headMaster", "%"+roleName.trim()+"%");
			params.put("teacherCounsellor", "%"+roleName.trim()+"%");
			params.put("monitor", "%"+roleName.trim()+"%");
		}
		hql.append(" order by college.id, major.id, klass.id ");
		return this.pagedQuery(hql.toString(), params, pageSize, pageNo);
	}
	
	/**
	 * 
	 * @Title: getAllStuJobTeamSet
	 * @Description:学院所有设置信息
	 * @param po
	 * @param pageNo
	 * @return
	 * @throws
	 */
	@Override
    @SuppressWarnings("unchecked")
	public List<StuJobTeamSetModel> getAllStuJobTeamSet()
	{
		return this.query("from StuJobTeamSetModel where college  is not null ");
	}
	@Override
	public StuJobTeamSetModel getStuJobByClassIdAndTyId(String klassId, String teacherTypeId) {
		String hql = "from StuJobTeamSetModel t where t.klass.id = ? and t.teacherType.id = ? ";
		return (StuJobTeamSetModel) this.queryUnique(hql, new String[]{klassId, teacherTypeId});
	}
	@Override
	public StuJobTeamSetModel getStuJobByCollegeIdAndTyId(String collegeId, String teacherTypeId) {
		String hql = "from StuJobTeamSetModel t where t.college.id = ? and t.teacherType.id = ? ";
		return (StuJobTeamSetModel) this.queryUnique(hql, new String[]{collegeId, teacherTypeId});
	}
	@Override
	public TeacherInfoModel getTeacherExtendByTeacherId(String teacherId) {
		String hql = "from TeacherInfoModel t where t.teacher.id = ? ";
		return (TeacherInfoModel) this.queryUnique(hql, new String[]{teacherId});
	}
	@Override
	public BaseAcademyModel getCollegeByTeacherId(String userId) {
		String hql = " from BaseAcademyModel b where b.id = " +
				" (select t.org.id from BaseTeacherModel t where t.id = ? )";
		
		return (BaseAcademyModel)this.queryUnique(hql, new String[]{userId});
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<Dic> getStuJobDicByTeacherId(String teacherId) {
		String hql = "select t.teacherType from StuJobTeamSetModel t where t.teacher.id = ? ";
		return this.query(hql, new String[]{teacherId});
	}
	@Override
	public List<StuJobTeamSetModel> queryExistById(String teacherId) {
		String hql = "from StuJobTeamSetModel t where t.teacher.id = ? ";
		return this.query(hql, new String[]{teacherId});
	}
	@Override
	public List<StuJobTeamSetModel> getStuJobByTeacherIdAndTyId(
			String teacherId, String teacherType) {
		String hql = "from StuJobTeamSetModel t where t.teacher.id = ? and t.teacherType.id = ? ";
		return this.query(hql, new String[]{teacherId, teacherType});
	}

	/**
	 * 描述信息:按照条件查询
	 * @param collegeId
	 * @param teacherId
	 * @param teacherType
	 * @return
	 * 2015-11-10 下午5:14:19
	 */
    @Override
    @SuppressWarnings("unchecked")
    public StuJobTeamSetModel getStuJobByConditions(String collegeId,String teacherId, String teacherType)
    {
		String hql = "from StuJobTeamSetModel t where t.teacher.id = ? and t.teacherType.id = ? and college.id = ?  ";
        List<StuJobTeamSetModel> list = this.query(hql, new Object[]{teacherId, teacherType,collegeId});
		return null == list||list.size() == 0 ? null : list.get(0);
    }
}