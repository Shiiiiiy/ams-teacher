package com.uws.teacher.service.impl;

import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uws.core.base.BaseServiceImpl;
import com.uws.core.hibernate.dao.support.Page;
import com.uws.domain.base.BaseAcademyModel;
import com.uws.domain.base.BaseTeacherModel;
import com.uws.domain.teacher.EvaluateTeacher;
import com.uws.domain.teacher.StuJobTeamSetModel;
import com.uws.sys.model.Dic;
import com.uws.sys.model.UploadFileRef;
import com.uws.sys.service.FileUtil;
import com.uws.sys.service.impl.FileFactory;
import com.uws.teacher.dao.IEvaluateTeacherDao;
import com.uws.teacher.service.IEvaluateTeacherService;

/**
 * @className EvaluateTeacherServiceImpl.java
 * @package com.uws.teacher.service.impl
 * @description
 * @author houyue
 * @date 2015-8-28  下午4:24:30
 */
@Service("com.uws.teacher.service.impl.EvaluateTeacherServiceImpl")
public class EvaluateTeacherServiceImpl extends BaseServiceImpl implements IEvaluateTeacherService {
	@Autowired
	private IEvaluateTeacherDao evaluateTeacherDao;
	private FileUtil fileUtil = FileFactory.getFileUtil();
	@Override
	public EvaluateTeacher getEvaluateTeacherById(String id) {
		return (EvaluateTeacher) this.evaluateTeacherDao.get(EvaluateTeacher.class,id);
	}
	@Override
	public void saveEvaluateInfo(EvaluateTeacher po, String[] fileId) {
		this.evaluateTeacherDao.save(po);
		//上传的附件进行处理
		 if (ArrayUtils.isEmpty(fileId)) {
		       return;
		    }
      for (String id : fileId)
      this.fileUtil.updateFormalFileTempTag(id, po.getId());
		
	}
	@Override
	public void updateEvaluateInfo(EvaluateTeacher po,String[] fileId) {
		this.evaluateTeacherDao.update(po);
		//上传的附件进行处理
		 if (ArrayUtils.isEmpty(fileId))
			 fileId = new String[0];
		     List<UploadFileRef> list = this.fileUtil.getFileRefsByObjectId(po.getId());
		     for (UploadFileRef ufr : list) {
		       if (!ArrayUtils.contains(fileId, ufr.getUploadFile().getId())){
		    	   this.fileUtil.deleteFormalFile(ufr);
		       }
		     }
		     for (String id : fileId){
		       this.fileUtil.updateFormalFileTempTag(id, po.getId());
		     }
	}
	@Override
	public EvaluateTeacher getEvaluateTeacherByTeacherId(String teacherId) {
		return this.evaluateTeacherDao.getEvaluateTeacherByTeacherId(teacherId);
	}
	@Override
	public String judgement(Dic curYear,String teacherId) {
		String add ="";
		List<Dic> schoolYear =evaluateTeacherDao.getSchoolYearDicByTeacherId(teacherId);
		for(int i=0;i<schoolYear.size();i++){
			add+=schoolYear.get(i).getName();
		}
		if(add.contains(curYear.getName())){
			//不添加增添按钮
			return "0";
		}else{
			//添加添加按钮
			return "1";
		}
	}
	@Override
	public String getPosition(List<Dic> curPosition) {
		String position = curPosition.get(0).getName();
		for(Dic d : curPosition) {
			if(!position.contains(d.getName())) {
				position+="、"+d.getName();
			}
		}
		return position;
	}
	@Override
	public BaseAcademyModel getBaseAcademyByOrgId(String OrgId) {
		return this.evaluateTeacherDao.getBaseAcademyByOrgId(OrgId);
	}
	@Override
	public void updateAuditInfo(EvaluateTeacher po) {
		/*EvaluateTeacher to = this.evaluateTeacherDao.getEvaluateTeacherById(po.getId());
		po.setCollegeAuditAuditor(to.getCollegeAuditAuditor());
		po.setCollegeAuditLevel(to.getCollegeAuditLevel());
		po.setCollegeAuditOpinion(to.getCollegeAuditOpinion());
		po.setCollegeAuditScroe(to.getCollegeAuditScroe());
		po.setCollegeAuditStatus(to.getCollegeAuditStatus());*/
		/*EvaluateTeacher to = new EvaluateTeacher();
		if(DataUtil.isNotNull(po.getCollegeAuditAuditor())) {
			BeanUtils.copyProperties(po, to, new String[]{"collegeAuditScroe",
					"collegeAuditOpinion", "collegeAuditAuditor", "collegeAuditStatus", "collegeAuditLevel"});
			this.evaluateTeacherDao.update(to);
		}else {
			this.evaluateTeacherDao.update(po);
		}*/
		this.evaluateTeacherDao.update(po);
	}
	@Override
	public EvaluateTeacher getEvaluateTeacher(String yearId) {
		return this.evaluateTeacherDao.getEvaluateTeacher(yearId);
	}
	@Override
	public Page queryPageSettingInfo(StuJobTeamSetModel po, int pageSize, int pageNo) {
		return this.evaluateTeacherDao.queryPageSettingInfo(po, pageSize, pageNo);
	}
	@Override
	public Page queryPageEvaluateTeacher(EvaluateTeacher po, int pageNo,
			String collegeId) {
		return this.evaluateTeacherDao.queryPageEvaluateTeacher(po, pageNo, collegeId);
	}
	@Override
	public Page queryEvaluateTeacher(Integer pageNo, Integer pageSize, String schoolYearId,
			String collegeId) {
		return evaluateTeacherDao.queryEvaluateTeacher(pageNo, pageSize, schoolYearId, collegeId);
	}
	@Override
	public List<BaseTeacherModel> getTeacherBySCL(String schoolYearId,
			String collegeId, String levelId) {
		return evaluateTeacherDao.getTeacherBySCL(schoolYearId, collegeId, levelId);
	}
	@Override
	public void deleteEvaluateInfo(String id) {
		this.evaluateTeacherDao.deleteById(EvaluateTeacher.class, id);
	}
	@Override
	public Page queryPageEvaluateInfo(EvaluateTeacher po, int pageNo, int pageSize) {
		return this.evaluateTeacherDao.queryPageEvaluateInfo(po, pageNo, pageSize);
	}
}
