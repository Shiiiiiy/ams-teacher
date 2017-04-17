package com.uws.teacher.service.impl;

import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uws.common.dao.ICommonRoleDao;
import com.uws.common.dao.IStudentCommonDao;
import com.uws.common.service.IBaseDataService;
import com.uws.core.base.BaseServiceImpl;
import com.uws.core.hibernate.dao.support.Page;
import com.uws.core.util.DataUtil;
import com.uws.domain.base.BaseAcademyModel;
import com.uws.domain.base.BaseClassModel;
import com.uws.domain.base.BaseTeacherModel;
import com.uws.domain.orientation.StudentInfoModel;
import com.uws.domain.teacher.SetClassTeacher;
import com.uws.domain.teacher.SetCollegeTeacher;
import com.uws.domain.teacher.StuJobTeamSetModel;
import com.uws.domain.teacher.TeacherInfoModel;
import com.uws.sys.model.Dic;
import com.uws.sys.model.UploadFileRef;
import com.uws.sys.service.DicUtil;
import com.uws.sys.service.FileUtil;
import com.uws.sys.service.impl.DicFactory;
import com.uws.sys.service.impl.FileFactory;
import com.uws.teacher.dao.IStuJobTeamDao;
import com.uws.teacher.service.IStuJobTeamService;

@Service("stuJobTeamService")
public class StuJobTeamServiceImpl extends BaseServiceImpl implements
		IStuJobTeamService {
	@Autowired
	private IStuJobTeamDao stuJobTeamDao;
	@Autowired
	ICommonRoleDao commonRoleDao;
	@Autowired
	IStudentCommonDao studentCommonDao;
	@Autowired
	private IBaseDataService baseDateService;
	@Autowired
	private FileUtil fileUtil = FileFactory.getFileUtil();
	@Autowired
	private DicUtil dicUtil = DicFactory.getDicUtil();
	@Override
	public Page queryPageStuJobTeam(SetClassTeacher setClassTeacher, String roleName, Integer pageNo, Integer pageSize) {
		return this.stuJobTeamDao.queryPageStuJobTeam(setClassTeacher, roleName, pageNo, pageSize);
	}
	public SetClassTeacher getStuJobTeamById(String Id) {
		return (SetClassTeacher) this.stuJobTeamDao.get(SetClassTeacher.class, Id);
	}
	@Override
	public List<StuJobTeamSetModel> getAllStuJobTeamSet()
	{
		return this.stuJobTeamDao.getAllStuJobTeamSet();
	}
	@Override
	public SetCollegeTeacher getSetCollegeTeacherById(String Id) {
		return (SetCollegeTeacher) this.stuJobTeamDao.get(SetCollegeTeacher.class, Id);
	}
	@Override
	public StuJobTeamSetModel getStuJobByClassIdAndTyId(String klassId, String teacherTypeId) {
		return stuJobTeamDao.getStuJobByClassIdAndTyId(klassId, teacherTypeId);
	}
	@Override
	public void editStuJobTeamSet(String classId, String teacherId, String teacherType) {
		if(DataUtil.isNotNull(classId)&&DataUtil.isNotNull(teacherId)&&DataUtil.isNotNull(teacherType)) {
			//教师基础表
			BaseTeacherModel t = new BaseTeacherModel();
			if(teacherId != "" && teacherId !=null){
				t.setId(teacherId);
				//通过教师基础表id查找是否在扩展表中存在， 如果不存在，那就在拓展表中保存
				if(getTeacherExtendByTeacherId(teacherId)==null) {
					//教师扩展表
					TeacherInfoModel po = new TeacherInfoModel();
					po.setTeacher(t);
					stuJobTeamDao.save(po);
				}
				//检查当前班级岗位是否已经存在，通过班级id和教师类型id查询StuJobTeamSetModel
				StuJobTeamSetModel m = getStuJobByClassIdAndTyId(classId, teacherType);
				/**
				 * 判断通过classId和teachertypeId查询的结果是否为空
				 * 如果为空，则将数据存储；否则更新数据
				 */
				if(m==null) {
					//判断根据teacherId查询的结果是否为空，不为空则存储该教师的岗位设置信息；否则不存。
					StuJobTeamSetModel sts = new StuJobTeamSetModel();
					BaseClassModel klass = new BaseClassModel();
					klass.setId(classId);
					sts.setKlass(klass);
					sts.setTeacher(t);
					Dic ty = new Dic();
					ty.setId((teacherType));
					sts.setTeacherType(ty);
					stuJobTeamDao.save(sts);
				}else {
					//如果存在，查看之前在教师扩展表中的数据是否还有必要保存，如果原来教师表在学工队伍中无参与，则删除其扩展表信息
					List<StuJobTeamSetModel> ml = stuJobTeamDao.queryExistById(m.getTeacher().getId());
					if(ml==null || ml.size()<1) {
						stuJobTeamDao.delete(getTeacherExtendByTeacherId(m.getTeacher().getId()));
					}
					m.setTeacher(t);
					stuJobTeamDao.update(m);
				}
			}
		}
	}
	@Override
	public StuJobTeamSetModel getStuJobByCollegeIdAndTyId(String collegeId, String teacherTypeId) {
		return stuJobTeamDao.getStuJobByCollegeIdAndTyId(collegeId, teacherTypeId);
	}
	@Override
	public void editCollegeJobSet(String collegeId, String teacherId, String teacherType) {
		if(DataUtil.isNotNull(collegeId)&&DataUtil.isNotNull(teacherId)&&DataUtil.isNotNull(teacherType)) {
			//教师基础表
			BaseTeacherModel t = new BaseTeacherModel();
			if(teacherId != "" && teacherId !=null){
				t.setId(teacherId);
				//通过教师基础表id查找是否在扩展表中存在， 如果不存在，那就在拓展表中保存
				if(getTeacherExtendByTeacherId(teacherId)==null) {
					//教师扩展表
					TeacherInfoModel po = new TeacherInfoModel();
					po.setTeacher(t);
					stuJobTeamDao.save(po);
				}
				//检查当前班级岗位是否已经存在
				StuJobTeamSetModel m = getStuJobByCollegeIdAndTyId(collegeId, teacherType);
				/**
				 * 判断通过classId和teachertypeId查询的结果是否为空
				 * 如果为空，则将数据存储；否则更新数据
				 */
				StuJobTeamSetModel sts = new StuJobTeamSetModel();
				BaseAcademyModel college = new BaseAcademyModel();
				if(m==null) {
					college.setId(collegeId);
					sts.setCollege(college);
					sts.setTeacher(t);
					Dic ty = new Dic();
					ty.setId(teacherType);
					sts.setTeacherType(ty);
					stuJobTeamDao.save(sts);
				}else {
					//如果存在，查看之前在教师扩展表中的数据是否还有必要保存，如果原来教师表在学工队伍中无参与，则删除其扩展表信息
					List<StuJobTeamSetModel> ml = stuJobTeamDao.queryExistById(m.getTeacher().getId());
					if(ml==null || ml.size()<1) {
						stuJobTeamDao.delete(getTeacherExtendByTeacherId(m.getTeacher().getId()));
					}
					m.setTeacher(t);
					stuJobTeamDao.update(m);
				}
			}
		}
	}
	@Override
	public TeacherInfoModel getTeacherExtendByTeacherId(String teacherId) {
		return stuJobTeamDao.getTeacherExtendByTeacherId(teacherId);
	}
	@Override
	public BaseAcademyModel getCollegeByTeacherId(String userId) {
		return stuJobTeamDao.getCollegeByTeacherId(userId);
	}
	@Override
	public List<Dic> getStuJobDicByTeacherId(String techerId) {
		return stuJobTeamDao.getStuJobDicByTeacherId(techerId);
	}
	@Override
	public void updateTeacherInfo(TeacherInfoModel po, String[] fileId) {
		if(po!=null && DataUtil.isNotNull(po.getId())) {
			stuJobTeamDao.update(po);
			//2 附件处理
	        if(fileId!=null && !fileId.equals("")) {
	            List<UploadFileRef>  list= fileUtil.getFileRefsByObjectId(po.getId());     
	            for(UploadFileRef ufr:list)
	                if(!ArrayUtils.contains(fileId, ufr.getUploadFile().getId()))
	                    fileUtil.deleteFormalFile(ufr);
	            for(String id:fileId)
	                fileUtil.updateFormalFileTempTag(id, po.getId());
	        }
		}
	}
	@Override
	public void setMonitor(String classId, String stuId) {
		BaseClassModel klass = baseDateService.findClassById(classId);
		StudentInfoModel stu = new StudentInfoModel();
		stu.setId(stuId);
		klass.setMonitor(stu);
		stuJobTeamDao.update(klass);
	}
	@Override
	public void updateBaseClass(BaseClassModel po) {
		stuJobTeamDao.update(po);
	}
	@Override
	public void setMonitorRole(String stuId) {
		//判断他所在的这个班之前有没有人当过班长，如果有人当过，则进行角色替换如果没有人则保存
		StudentInfoModel sim = studentCommonDao.queryStudentByStudentNo(stuId);
		if(DataUtil.isNotNull(sim) && DataUtil.isNotNull(sim.getClassId()) && DataUtil.isNotNull(sim.getClassId().getMonitor())) {
			//更新角色
			commonRoleDao.deleteUserRole(sim.getClassId().getMonitor().getId(), "HKY_CLASS_MONITOR");
			commonRoleDao.saveUserRole(stuId, "HKY_CLASS_MONITOR");
		}else {
			commonRoleDao.saveUserRole(stuId, "HKY_CLASS_MONITOR");
		}
	}
	@Override
	public void saveKlassCounsellor(String klassId, String teacherType, String teacherId, String roleCode) {
		//判断这个班下原来有没有此种职工
		StuJobTeamSetModel sj = stuJobTeamDao.getStuJobByClassIdAndTyId(klassId, teacherType);
		//判断新选择的教师任没任此种职位
		List<StuJobTeamSetModel> ls = stuJobTeamDao.getStuJobByTeacherIdAndTyId(teacherId, teacherType);
		/**
		 * 1.如果这个班级之前 没有 此类职工并且新选中教师 没有 任此类职务则直接保存新任教师角色。
		 * 2.如果这个班级之前 没有 此类职工并且新选中教师 有 任此类职务 此时查询角色表中是否已保存新教师角色 如果没有保存角色则保存新任教师角色
		 * 3.如果这个班级之前 有 此类职工并且新选中教师 没有 任此类职务 则比较二者id 若相同则检查角色表中是否已保存新教师角色 如果不同则看之前教师是否还在其他班级任职 如果不任职则删除角色
		 * 4.如果这个班级之前 有 此类职工并且新选中教师 有 任此类职务 则比较二者id 若相同则检查角色表中是否已保存新教师角色 如果不同则看之前教师是否还在其他班级任职 如果不任职则删除角色 最后查看新任教师角色是否保存到库 如果没有予以保存
		 */
		if(sj==null && ls==null) {
			commonRoleDao.saveUserRole(teacherId, roleCode);
		}else if(sj==null && ls!=null) {
			if(!commonRoleDao.checkUserIsExist(teacherId, roleCode)) {
				commonRoleDao.saveUserRole(teacherId, roleCode);
			}
		}else if(sj!=null && ls==null) {
			if(!sj.getTeacher().getId().equals(teacherId)) {
				List<StuJobTeamSetModel> sjl = stuJobTeamDao.getStuJobByTeacherIdAndTyId(sj.getTeacher().getId(), teacherType);
				if(sjl.size()==1) { 
					commonRoleDao.deleteUserRole(sj.getTeacher().getId(), roleCode);
				}else {
					if(!commonRoleDao.checkUserIsExist(sj.getTeacher().getId(), roleCode)) {
						commonRoleDao.saveUserRole(sj.getTeacher().getId(), roleCode);
					}
				}
				commonRoleDao.saveUserRole(teacherId, roleCode);
			}else {
				if(!commonRoleDao.checkUserIsExist(teacherId, roleCode))
					commonRoleDao.saveUserRole(teacherId, roleCode);
			}
		}else if(sj!=null && ls!=null) {
			if(!sj.getTeacher().getId().equals(teacherId)) {
				List<StuJobTeamSetModel> sjl = stuJobTeamDao.getStuJobByTeacherIdAndTyId(sj.getTeacher().getId(), teacherType);
				if(sjl.size()==1) {
					commonRoleDao.deleteUserRole(sj.getTeacher().getId(), roleCode);
				}else {
					if(!commonRoleDao.checkUserIsExist(sj.getTeacher().getId(), roleCode)) {
						commonRoleDao.saveUserRole(sj.getTeacher().getId(), roleCode);
					}
				}
			}
			if(!commonRoleDao.checkUserIsExist(teacherId, roleCode))
				commonRoleDao.saveUserRole(teacherId, roleCode);
		}
	}
	
	/**
	 * 描述信息: 保存学工设置信息
	 * @param collegeId
	 * @param teacherType
	 * @param teacherId
	 * @param roleCode
	 * 2015-11-10 下午5:03:12
	 */
	@Override
	public void saveCollegeCounsellor(String collegeId, String teacherType,String teacherId, String roleCode) 
	{
		// 查询当前用户是否存在该角色，没有保存，有跳过
		if(!commonRoleDao.checkUserIsExist(teacherId, roleCode))
			commonRoleDao.saveUserRole(teacherId, roleCode);
		// 更新数据设置
		StuJobTeamSetModel sj = stuJobTeamDao.getStuJobByConditions(collegeId, teacherId ,teacherType);
		if(null == sj)
		{
			sj = new StuJobTeamSetModel();
			BaseAcademyModel college = new BaseAcademyModel();
			college.setId(collegeId);
			sj.setCollege(college);
			Dic dic = new Dic();
			dic.setId(teacherType);
			sj.setTeacherType(dic);
			BaseTeacherModel teacher = new BaseTeacherModel();
			teacher.setId(teacherId);
			sj.setTeacher(teacher);
			stuJobTeamDao.save(sj);
			
		}
	}
	
	/**
	 * 描述信息: 删除设置信息
	 * TODO 角色删除上可能有点问题，时间问题 先不予判断调整
	 * @param id
	 * 2015-11-10 下午1:50:05
	 */
	@Override
    public void deleteSettingById(String id)
    {
	    if(!StringUtils.isEmpty(id))
	    {
	    	StuJobTeamSetModel stuJobTeamSetPo = (StuJobTeamSetModel) stuJobTeamDao.get(StuJobTeamSetModel.class, id);
	    	if(null!=stuJobTeamSetPo)
	    	{
	    		Dic teacherType = stuJobTeamSetPo.getTeacherType();
	    		//测评辅导员
	    		if(dicUtil.getDicInfo("TEACHER_TYPE", "EVALUATION_COUNSELLOR").getId().equals(teacherType.getId()))
	    		{
	    			//删除角色
	    			// TODO有时间待逻辑严谨性上调整，目前时间紧先通用逻辑处理
	    			commonRoleDao.deleteUserRole(stuJobTeamSetPo.getTeacher().getId(), "HKY_EVALUATION_COUNSELOR");
	    		}
	    		//资助辅导员
	    		else if(dicUtil.getDicInfo("TEACHER_TYPE", "SUBSIDIZE_COUNSELLOR").getId().equals(teacherType.getId()))
	    		{
	    			// TODO有时间待逻辑严谨性上调整，目前时间紧先通用逻辑处理
	    			commonRoleDao.deleteUserRole(stuJobTeamSetPo.getTeacher().getId(), "HKY_SPONSOR_COUNSELOR");
	    		}
	    		//就业辅导员
	    		else if(dicUtil.getDicInfo("TEACHER_TYPE", "CAREER_COUNSELLOR").getId().equals(teacherType.getId()))
	    		{
	    			// TODO有时间待逻辑严谨性上调整，目前时间紧先通用逻辑处理
	    			commonRoleDao.deleteUserRole(stuJobTeamSetPo.getTeacher().getId(), "HKY_EMPLOYMENT_COUNSELOR");
	    		}
	    	}
	    	//删除设置信息
	    	stuJobTeamDao.delete(stuJobTeamSetPo);
	    }
    }
	
	/**
	 * 描述信息:按照教师工号查询学工队伍的设置信息
	 * @param teacherId
	 * @return
	 * 2016-2-22 下午3:08:00
	 */
	@Override
    public List<StuJobTeamSetModel> queryTeacherSettingInfo(String teacherId)
    {
		return stuJobTeamDao.queryExistById(teacherId);
    }
	
}