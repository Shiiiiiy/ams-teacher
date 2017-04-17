package com.uws.teacher.controller;


import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.uws.common.service.IBaseDataService;
import com.uws.common.service.ICommonApproveService;
import com.uws.common.util.SchoolYearUtil;
import com.uws.core.base.BaseController;
import com.uws.core.excel.ExcelException;
import com.uws.core.excel.service.IExcelService;
import com.uws.core.hibernate.dao.support.Page;
import com.uws.core.session.SessionFactory;
import com.uws.core.session.SessionUtil;
import com.uws.core.util.DataUtil;
import com.uws.core.util.DateUtil;
import com.uws.domain.base.BaseAcademyModel;
import com.uws.domain.base.BaseTeacherModel;
import com.uws.domain.common.CommonApproveComments;
import com.uws.domain.teacher.CountTeacherEvaluate;
import com.uws.domain.teacher.EvaluateTeacher;
import com.uws.domain.teacher.StuJobTeamSetModel;
import com.uws.log.LoggerFactory;
import com.uws.sys.model.Dic;
import com.uws.sys.service.DicUtil;
import com.uws.sys.service.FileUtil;
import com.uws.sys.service.impl.DicFactory;
import com.uws.sys.service.impl.FileFactory;
import com.uws.teacher.service.IEvaluateTeacherService;
import com.uws.teacher.service.IStuJobTeamService;
import com.uws.teacher.util.Constants;
import com.uws.user.model.Org;
import com.uws.user.service.IOrgService;
import com.uws.user.service.IUserService;
import com.uws.util.CheckUtils;
import com.uws.util.ProjectConstants;
import com.uws.util.ProjectSessionUtils;

/**
 * @className EvaluateTeacherController.java
 * @package com.uws.teacher.controller
 * @description
 * @author houyue
 * @date 2015-8-28  下午4:05:50
 */
@Controller
public class EvaluateTeacherController extends BaseController {
	@Autowired
	private IBaseDataService baseDateService;
	@Autowired
	private IOrgService orgService;
	@Autowired
	private IEvaluateTeacherService evaluateTeacherService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IBaseDataService baseDataService;
	@Autowired
	private static DicUtil dicUtil = DicFactory.getDicUtil();
	@Autowired
	private IStuJobTeamService stuJobTeamService;
	@Autowired
    private IExcelService excelService;
	@Autowired
	private ICommonApproveService commonApproveService;
	private String auditStatus;
	private SessionUtil sessionUtil = SessionFactory.getSession(Constants.TEACHER_EVALUATE);
	private LoggerFactory log = new LoggerFactory(StuJobTeamSetController.class);
	private FileUtil fileUtil = FileFactory.getFileUtil();

	/**
	 * 系统管理员列表信息(考核信息查询)
	 * @param model
	 * @param request
	 * @param po
	 * @return list
	 */
	@RequestMapping({"/teacher/system/opt-query/evaluateList"})
	public String evaluateList(ModelMap model, HttpServletRequest request, EvaluateTeacher po) {
		//学院
		List<BaseAcademyModel> collegeList = baseDateService.listBaseAcademy();
		model.addAttribute("collegeList", collegeList);
		//学年
		List<Dic> schoolYearList = dicUtil.getDicInfoList("YEAR");
		model.addAttribute("schoolYearList", schoolYearList);
		//审核状态
		List<Dic> statusList = dicUtil.getDicInfoList("AUDIT_STATUS");
		model.addAttribute("statusList", statusList);
		String pageNo = request.getParameter("pageNo");
		pageNo = pageNo != null ? pageNo : "1";
		Page page = evaluateTeacherService.queryPageEvaluateInfo(po, Integer.parseInt(pageNo), Page.DEFAULT_PAGE_SIZE);
//		Page page = evaluateTeacherService.queryPageEvaluateTeacher(po, Integer.parseInt(pageNo), null, null);
		model.addAttribute("po",po);
		model.addAttribute("page", page);
		return Constants.TEACHER_EVALUATE+"/evaluateList";
	}
	
	/**
	 * 未填写考核信息的教师
	 * @param model
	 * @param request
	 * @param po
	 * @return
	 */
	@RequestMapping({"/teacherInfo/view/nsm/restTeacherView"})
	public String restTeacherInfo(ModelMap model, HttpServletRequest request, StuJobTeamSetModel po){
		String pageNo = request.getParameter("pageNo");
		pageNo = pageNo != null ? pageNo : "1";
		Page page = evaluateTeacherService.queryPageSettingInfo(po, 5, Integer.parseInt(pageNo));
		//学院单位
		List<Org> orgList = orgService.queryOrg();
		model.addAttribute("orgList", orgList);
		//当前学年
		String curYear=dicUtil.getDicInfo("YEAR", String.valueOf(DateUtil.getCurYear())).getName();
		model.addAttribute("curYear", curYear);
		model.addAttribute("po", po);
		model.addAttribute("teacherPage", page);
		return "teacher/evaluate/restTeacherView";
	}
	
	/**
	 * 异步列表分页
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequestMapping(value={"/teacherInfo/view/nsm/ajaxueryOnPage"},produces={"text/plain;charset=UTF-8"})
	public String ajaxueryOnPage(ModelMap model,HttpServletRequest request,HttpServletResponse response,StuJobTeamSetModel po){
		//当前学年
		String curYear=dicUtil.getDicInfo("YEAR", String.valueOf(DateUtil.getCurYear())).getName();
		model.addAttribute("curYear", curYear);
		String pageNo = request.getParameter("pageNo");
		pageNo = pageNo != null ? pageNo : "1";
		Page page = evaluateTeacherService.queryPageSettingInfo(po, 5, Integer.parseInt(pageNo));
		model.addAttribute("teacherPage", page);
		return "teacher/evaluate/noEvaluateTeacherList";
	}
	
	/**
	 * 教师个人查看列表
	 * @param model
	 * @param request
	 * @param po
	 * @return list
	 */
	@RequestMapping({"teacher/person/opt-query/teacherList"})
	public String teacherList(ModelMap model, HttpServletRequest request, EvaluateTeacher po){
		String userId = sessionUtil.getCurrentUserId();
		String permission = "0";
		//判断当前登陆人是否有权限访问
		List<StuJobTeamSetModel> settingList = stuJobTeamService.queryTeacherSettingInfo(userId);// .queryExistById(userId);
		//List<StuJobTeamSetModel> queryExistById(String teacherId)
		if(null!=settingList && settingList.size()>0){	
			permission = "1";
			//学年列表
			List<Dic> schoolYearList = dicUtil.getDicInfoList("YEAR");
			model.addAttribute("schoolYearList", schoolYearList);
			//当前学年
			Dic curYear=dicUtil.getDicInfo("YEAR", String.valueOf(DateUtil.getCurYear()));
			model.addAttribute("curYear",curYear);
			//判断是否需要新增加考核信息
			String addJudge = evaluateTeacherService.judgement(curYear,userId);
			model.addAttribute("addJudge", addJudge);
			po.setTeacher(baseDataService.findTeacherById(userId));
			String pageNo = request.getParameter("pageNo");
			pageNo = pageNo != null ? pageNo : "1";
			Page page = evaluateTeacherService.queryPageEvaluateInfo(po, Integer.parseInt(pageNo), Page.DEFAULT_PAGE_SIZE);
			model.addAttribute("po",po);
			model.addAttribute("page", page);
			
			//判断是否显示新增操作功能按钮
			String isAdd = "false";
			for(StuJobTeamSetModel setInfo : settingList)
			{
				if(null != setInfo.getKlass())
				{
					if(ProjectConstants.BASE_CLASS_STAUTS_USE.equals(setInfo.getKlass().getStatus()))
					{
						isAdd ="true";
						break;
					}
				}else{
					isAdd ="true";
					break;
				}
			}
			model.addAttribute("isAdd", isAdd);
		}
		model.addAttribute("permission", permission);
		return Constants.TEACHER_EVALUATE + "/teacherList";
	}
	
	/**
	 * 编辑或更新考核信息
	 * @param model
	 * @param request
	 * @param po
	 * @return
	 */
	@RequestMapping({"/teacher/person/opt-add/editEvaluateInfo","teacher/person/opt-update/editEvaluateInfo"})
	public String editEvaluateInfo(ModelMap model, HttpServletRequest request, EvaluateTeacher po){
		String userId = sessionUtil.getCurrentUserId();
		//现任职务
		List<Dic> curPosition = stuJobTeamService.getStuJobDicByTeacherId(userId);
		String position = evaluateTeacherService.getPosition(curPosition);
		EvaluateTeacher evaluateTeacher = null;
		if((po.getId())!=null){
			if(po.getPresentJob()==position){
				evaluateTeacher = evaluateTeacherService.getEvaluateTeacherById(po.getId());
			}else{
				//如果两次的职位不相等，则更新职位信息
				evaluateTeacher = evaluateTeacherService.getEvaluateTeacherById(po.getId());
				evaluateTeacher.setPresentJob(position);
			}
			model.addAttribute("uploadFileRefList", this.fileUtil.getFileRefsByObjectId(po.getId()));
			String objectId = po.getId();
			model.addAttribute("objectId", objectId);
		}else{
			evaluateTeacher = new EvaluateTeacher();
			//set教师基础信息
			BaseTeacherModel baseTeacher = baseDataService.findTeacherById(userId);
			evaluateTeacher.setTeacher(baseTeacher);
			//set当前学年信息
			evaluateTeacher.setSchoolYear(SchoolYearUtil.getYearDic());
			//set当前职位
			evaluateTeacher.setPresentJob(position);
			
		}
		if(DataUtil.isNotNull(evaluateTeacher) && DataUtil.isNotNull(evaluateTeacher.getStatus()) && DataUtil.isNotNull(evaluateTeacher.getStatus().getId())) {
			String auditHistory = evaluateTeacher.getStatus().getName();
			model.addAttribute("auditHistory", auditHistory);	
		}
		model.addAttribute("evaluateTeacher", evaluateTeacher);
		return Constants.TEACHER_EVALUATE + "/evaluateInfoEdit";
	}
	
	/**
	 * 保存教师个人填写的考核信息
	 * @param model
	 * @param request
	 * @return page
	 */
	@RequestMapping({"/teacher/person/opt-save/evaluateInfo"})
	public String saveEvaluateInfo(ModelMap model, HttpServletRequest request, EvaluateTeacher po, String[] fileId ){
		String status =  request.getParameter("status_id");
		if(status.equals("0")){
			po.setStatus(dicUtil.getDicInfo("AUDIT_STATUS", "SAVE"));
		}else if(status.equals("1")){
			po.setStatus(dicUtil.getDicInfo("AUDIT_STATUS", "COLLEGE_AUDIT"));
		}
		if(DataUtil.isNull(po.getId())){
			this.evaluateTeacherService.saveEvaluateInfo(po,fileId);
		}else{
			this.evaluateTeacherService.updateEvaluateInfo(po,fileId);
		}
		return "redirect:" + "/teacher/person/opt-query/teacherList.do";
	}
	
	/**
	 * 删除教师考核信息
	 * @param model
	 * @param request
	 * @param po
	 * @return
	 */
	@RequestMapping({"teacher/person/opt-del/delEvaluateInfo"})
	@ResponseBody
	public String delEvaluateInfo(ModelMap model, HttpServletRequest request, EvaluateTeacher po){
		this.evaluateTeacherService.deleteEvaluateInfo(po.getId());
		return "success";
	}
	
	/**
	 * 二级学院考核列表
	 * @param model
	 * @param request
	 * @param po
	 * @return page
	 */
	@RequestMapping({"/teacher/collegeAudit/opt-query/auditInfoList"})
	public String collegeAuditList(ModelMap model, HttpServletRequest request, EvaluateTeacher po){
		String collegeId = null;
		String college = null;
		String permission = "0";
		//标识学院审核
		auditStatus = "0";
		model.addAttribute("auditStatus", auditStatus);
		//判断当前登陆人是否有权限
		String userId = sessionUtil.getCurrentUserId();
		BaseTeacherModel btm = baseDataService.findTeacherById(userId);
		if(DataUtil.isNotNull(btm)){
			collegeId = btm.getOrg().getId();
			college = btm.getOrg().getName();
			boolean b = CheckUtils.isCurrentOrgEqCollege(collegeId);
			if(b==true){
				permission = "1";
			}
		}
		model.addAttribute("college", college);
		model.addAttribute("permission", permission);
		//学年
		List<Dic> schoolYearList = dicUtil.getDicInfoList("YEAR");
		model.addAttribute("schoolYearList", schoolYearList);
		//审核状态
		List<Dic> statusList = new ArrayList<Dic>();
		statusList.add(dicUtil.getDicInfo("AUDIT_STATUS", "STU_AFFAIRS_AUDIT"));
		statusList.add(dicUtil.getDicInfo("AUDIT_STATUS", "COLLEGE_AUDIT"));
		statusList.add(dicUtil.getDicInfo("AUDIT_STATUS", "PASS"));
		statusList.add(dicUtil.getDicInfo("AUDIT_STATUS", "REJECT"));
		model.addAttribute("statusList", statusList);
		//获取page
		String pageNo = request.getParameter("pageNo");
		pageNo = pageNo != null ? pageNo : "1";
		//通过审核状态，学院，学年查询获得page
		Page page = evaluateTeacherService.queryPageEvaluateTeacher(po, Integer.parseInt(pageNo), collegeId);
		model.addAttribute("po",po);
		model.addAttribute("page", page);
		return Constants.TEACHER_EVALUATE+"/auditInfoList";
	}
	
	/**
	 * 二级学院审核信息填写
	 * @param model
	 * @param request
	 * @param po
	 * @return page
	 */
	@RequestMapping({"/teacher/collegeAudit/opt-edit/auditInfo"})
	public String editCollegeAuditInfo(ModelMap model, HttpServletRequest request, EvaluateTeacher po){
		auditStatus = "0";
		model.addAttribute("auditStatus", auditStatus);
		EvaluateTeacher evaluateTeacher = evaluateTeacherService.getEvaluateTeacherById(po.getId());
		model.addAttribute("evaluateTeacher", evaluateTeacher);
		List<Dic> auditLevelList = dicUtil.getDicInfoList("AUDIT_LEVEL");
		model.addAttribute("auditLevelList", auditLevelList);
		model.addAttribute("uploadFileRefList", this.fileUtil.getFileRefsByObjectId(po.getId()));
		return Constants.TEACHER_EVALUATE + "/auditInfoEdit";
	}
	/**
	 * 保存二级学院审核信息
	 * @param model
	 * @param request
	 * @param po
	 * @return
	 */
	@RequestMapping({"teacher/collegeAudit/opt-save/auditInfo"})
	public String saveCollegeAuditInfo(ModelMap model, HttpServletRequest request, EvaluateTeacher po){
		String status = request.getParameter("status_id");
		// "0"通过,"1"拒绝
		if (status.equals("0")) {
			// 状态为学生处审核
			po.setStatus(dicUtil.getDicInfo("AUDIT_STATUS", "STU_AFFAIRS_AUDIT"));
			po.setCollegeAuditStatus(ProjectConstants.APPROVE_PASS_COMMENTS);
		} else {
			po.setCollegeAuditOpinion(request.getParameter("collegeOpinion"));
			po.setCollegeAuditStatus(Constants.APPROVE_REJECT_COMMENTS);
			po.setStatus(dicUtil.getDicInfo("AUDIT_STATUS", "REJECT"));
		}
		String userId = sessionUtil.getCurrentUserId();
		po.setCollegeAuditAuditor(ProjectSessionUtils.getCurrentUserName(request));
		// 封装审核信息
		CommonApproveComments approveHistory = new CommonApproveComments();
		// 审核结果
		approveHistory.setApproveOpinion(po.getCollegeAuditStatus());
		// 审核人
		approveHistory.setApprover(userService.getUserById(userId));
		// 审核时间
		approveHistory.setApproveTime(new Date());
		// 审核意见
		approveHistory.setApproveComments(po.getCollegeAuditOpinion());
		// 业务主键
		approveHistory.setObjectId(po.getId());
		commonApproveService.saveApproveComments(approveHistory);
		// 保存审核信息
		this.evaluateTeacherService.updateAuditInfo(po);
		return "redirect:" + "/teacher/collegeAudit/opt-query/auditInfoList.do";
	}
	
	/**
	 * 学生处审核列表
	 * @param model
	 * @param request
	 * @param po
	 * @return page
	 */
	@RequestMapping({"/teacher/stuAffairAudit/opt-query/auditInfoList"})
	public String stuAffairAuditList(ModelMap model, HttpServletRequest request, EvaluateTeacher po){
		//未填写考核信息教师列表
		//学院单位
		List<Org> orgList = orgService.queryOrg();
		model.addAttribute("orgList", orgList);
		//当前学年
		String curYear=dicUtil.getDicInfo("YEAR", String.valueOf(DateUtil.getCurYear())).getName();
		model.addAttribute("curYear", curYear);
		//标识学生处审核
		auditStatus = "1";
		model.addAttribute("auditStatus", auditStatus);
		String permission = "0";
		//学院
		List<BaseAcademyModel> collegeList = baseDateService.listBaseAcademy();
		model.addAttribute("collegeList", collegeList);
		//学年
		List<Dic> schoolYearList = dicUtil.getDicInfoList("YEAR");
		model.addAttribute("schoolYearList", schoolYearList);
		//审核状态
		List<Dic> statusList = new ArrayList<Dic>();
		statusList.add(dicUtil.getDicInfo("AUDIT_STATUS", "STU_AFFAIRS_AUDIT"));
		statusList.add(dicUtil.getDicInfo("AUDIT_STATUS", "PASS"));
		statusList.add(dicUtil.getDicInfo("AUDIT_STATUS", "REJECT"));
		model.addAttribute("statusList", statusList);
		//通过审核状态查询获得page
		String pageNo = request.getParameter("pageNo");
		pageNo = pageNo != null ? pageNo : "1";
		Page page = evaluateTeacherService.queryPageEvaluateTeacher(po, Integer.parseInt(pageNo), null);
		model.addAttribute("po",po);
		model.addAttribute("page", page);
		//判断当前登陆人是否有权限
		String userId = sessionUtil.getCurrentUserId();
		BaseTeacherModel teacher = baseDataService.findTeacherById(userId);
		if(DataUtil.isNotNull(teacher)){
			permission = "1";
		}
		model.addAttribute("permission", permission);
		return Constants.TEACHER_EVALUATE+"/auditInfoList";
	}
	
	/**
	 * 学生处审核信息
	 * @param model
	 * @param request
	 * @param po
	 * @return page
	 */
	@RequestMapping({"/teacher/stuAffairAudit/opt-edit/auditInfo"})
	public String editStuAffairAuditInfo(ModelMap model, HttpServletRequest request, EvaluateTeacher po){
		auditStatus = "1";
		model.addAttribute("auditStatus", auditStatus);
		EvaluateTeacher evaluateTeacher = evaluateTeacherService.getEvaluateTeacherById(po.getId());
		model.addAttribute("evaluateTeacher", evaluateTeacher);
		//评定等级
		List<Dic> auditLevelList = dicUtil.getDicInfoList("AUDIT_LEVEL");
		model.addAttribute("auditLevelList", auditLevelList);
		model.addAttribute("uploadFileRefList", this.fileUtil.getFileRefsByObjectId(po.getId()));
		return Constants.TEACHER_EVALUATE + "/auditInfoEdit";
	}
	/**
	 * 保存学生处审核信息
	 * @param model
	 * @param request
	 * @param po
	 * @return
	 */
	@RequestMapping({"/teacher/stuAffairAudit/opt-save/auditInfo"})
	public String saveStuAffairAuditInfo(ModelMap model, HttpServletRequest request, EvaluateTeacher po){
		String status =  request.getParameter("status_id");
		//"0"通过,"1"拒绝
		if(status.equals("0")){
			po.setCollegeAuditAuditor(request.getParameter("passAuditor"));
			Dic d = new Dic();
			d.setId(request.getParameter("passLevel"));
			po.setCollegeAuditLevel(d);
			po.setCollegeAuditOpinion(request.getParameter("passOpinion"));
			po.setCollegeAuditScroe(Double.parseDouble(request.getParameter("passScore")));
			po.setCollegeAuditStatus(request.getParameter("passStatus"));
			po.setStatus(dicUtil.getDicInfo("AUDIT_STATUS", "PASS"));
			po.setStuAffairsAuditStatus(ProjectConstants.APPROVE_PASS_COMMENTS);
		}else{
			po.setCollegeAuditAuditor(request.getParameter("rejectAuditor"));
			Dic d = new Dic();
			d.setId(request.getParameter("rejectLevel"));
			String score = request.getParameter("rejectScroe");
			po.setCollegeAuditLevel(d);
			po.setCollegeAuditOpinion(request.getParameter("rejectOpinion"));
			po.setCollegeAuditScroe(Double.parseDouble(score));
			po.setCollegeAuditStatus(request.getParameter("rejectStatus"));
			po.setStuAffairsAuditOpinion(request.getParameter("stuAffairsOpinion"));
			po.setStatus(dicUtil.getDicInfo("AUDIT_STATUS", "REJECT"));
			po.setStuAffairsAuditStatus(Constants.APPROVE_REJECT_COMMENTS);
		}
		String userId = sessionUtil.getCurrentUserId();
		po.setStuAffairsAuditor(baseDataService.findTeacherById(userId).getName());
		//封装审核信息
		CommonApproveComments approveHistory= new CommonApproveComments();
		//审核结果
		approveHistory.setApproveOpinion(po.getStuAffairsAuditStatus());
		//审核人
		approveHistory.setApprover(userService.getUserById(userId));
		//审核时间
		approveHistory.setApproveTime(new Date());
		//审核意见
		approveHistory.setApproveComments(po.getStuAffairsAuditOpinion());
		//业务主键
		approveHistory.setObjectId(po.getId());
		commonApproveService.saveApproveComments(approveHistory);
		//保存审核信息
		this.evaluateTeacherService.updateAuditInfo(po);
		return "redirect:" + "/teacher/stuAffairAudit/opt-query/auditInfoList.do";
	}
	
	/**
	 * 查看审核信息
	 * @param model
	 * @param request
	 * @param po
	 * @return
	 */
	@RequestMapping({"teacher/evaluate/opt-view/auditInfo"})
	public String viewAuditInfo(ModelMap model, HttpServletRequest request,String id ,String flag)
	{
		if(!StringUtils.isEmpty(id))
		{
			model.addAttribute("uploadFileRefList", this.fileUtil.getFileRefsByObjectId(id));
			EvaluateTeacher teacher = evaluateTeacherService.getEvaluateTeacherById(id);
			model.addAttribute("objectId", id);
			model.addAttribute("evaluateTeacher", teacher);
			model.addAttribute("flag", flag);
			model.addAttribute("passStatus", dicUtil.getDicInfo("AUDIT_STATUS", "PASS"));
		}
		return Constants.TEACHER_EVALUATE +"/auditInfoView";
	}
	
	/**
	 * 学生处统计审核信息
	 * @param model
	 * @param request
	 * @param po
	 * @return
	 */
	@RequestMapping({"teacher/statistic/opt-query/auditStatistic"})
	public String auditStatistics(ModelMap model, HttpServletRequest request, String schoolYearId, String collegeId) {
		//学年
		List<Dic> schoolYearList = dicUtil.getDicInfoList("YEAR");
		//学院
		List<BaseAcademyModel> collegeList = baseDateService.listBaseAcademy();
		model.addAttribute("schoolYearList", schoolYearList);
		model.addAttribute("collegeList", collegeList);
		String pageNo = request.getParameter("pageNo")!= null ? request.getParameter("pageNo") : "1";
		Page page = evaluateTeacherService.queryEvaluateTeacher(Integer.valueOf(pageNo), 
				Page.DEFAULT_PAGE_SIZE, schoolYearId, collegeId);
		model.addAttribute("page", page);
		model.addAttribute("schoolYearId", schoolYearId);
		model.addAttribute("collegeId", collegeId);
		
		return Constants.TEACHER_EVALUATE +"/auditStatisticList";
	}
	@ResponseBody
	@RequestMapping(value={"teacher/statistic/nsm/auditTeachers"},produces={"text/plain;charset=UTF-8"})
	public String getAuditTeachers(HttpServletRequest request, String schoolYearId, String collegeId, String levelId) {
		List<BaseTeacherModel> teacherList = new ArrayList<BaseTeacherModel>();
		if(DataUtil.isNotNull(schoolYearId) && DataUtil.isNotNull(collegeId) && DataUtil.isNotNull(levelId)) {
			if(levelId.equals("0"))
				levelId = dicUtil.getDicInfo("AUDIT_LEVEL", "EXCELLENT").getId();
			else if(levelId.equals("1"))
				levelId = dicUtil.getDicInfo("AUDIT_LEVEL", "WELL").getId();
			else if(levelId.equals("2"))
				levelId = dicUtil.getDicInfo("AUDIT_LEVEL", "MEDIUM").getId();
			else if(levelId.equals("3"))
				levelId = dicUtil.getDicInfo("AUDIT_LEVEL", "BAD").getId();
			teacherList = evaluateTeacherService.getTeacherBySCL(schoolYearId, collegeId, levelId);
		}
		String result = "";
//		List<JsonModel> jml = new ArrayList<JsonModel>();
		for(int i=0; i<teacherList.size(); i++) {
			if(i!=0)
				result = result + ", " + teacherList.get(i).getName();
			else
				result = teacherList.get(i).getName();
		}
		return result;
	}
	
	/**
	 * 导出页面弹窗
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequestMapping({"teacher/statistic/nsm/exportView" })
	public String exportView(HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		log.info("进入导出页面");
        String size = request.getParameter("exportSize");
        String page = request.getParameter("pageTotalCount");
        int exportSize=Integer.valueOf((size!=null && !size.equals(""))?size:"1").intValue();
        int pageTotalCount=Integer.valueOf((page!=null&& !page.equals(""))?page:"1").intValue();
        int maxNumber=0;
        if(pageTotalCount<exportSize){
            maxNumber=1;
        }else if(pageTotalCount % exportSize == 0){
            maxNumber=pageTotalCount / exportSize;
        }else{
            maxNumber=pageTotalCount / exportSize + 1;
        }
        model.addAttribute("exportSize",Integer.valueOf(exportSize));
        model.addAttribute("maxNumber",Integer.valueOf(maxNumber));
        //为了能将导出的数据效率高，判断每次导出数据500条
        if(maxNumber<500){
            model.addAttribute("isMore", "false");
        }else{
            model.addAttribute("isMore", "true");
        }
         return Constants.TEACHER_MANAGE+"/exportAduitView";
	}
	/**
	 * 学工考核结果导出方法
	 * @param request
	 * @param response
	 * @param schoolYearId
	 * @param collegeId
	 */
	@RequestMapping({"teacher/statistic/opt-export/exportAduit" })
	public void exportAduit(HttpServletRequest request, HttpServletResponse response, String schoolYearId, String collegeId) {
		String exportSize=request.getParameter("stuJob_exportSize");
        String exportPage=request.getParameter("stuJob_exportPage");
        Page page = evaluateTeacherService.queryEvaluateTeacher((DataUtil.isNotNull(exportPage)?Integer.valueOf(exportPage):1), 
        		(DataUtil.isNotNull(exportSize)?Integer.valueOf(exportSize):1000), schoolYearId, collegeId);
        @SuppressWarnings("unchecked")
		List<CountTeacherEvaluate> teacherEvaCountList = (List<CountTeacherEvaluate>) page.getResult();
        List<Map> listMap = new ArrayList<Map>();
        for(CountTeacherEvaluate c:teacherEvaCountList) {
        	Map<String, Object> map = new HashMap<String, Object>();
        	//学年
        	map.put("schooleYear", c.getSchoolYear()!=null?c.getSchoolYear().getName():"");
        	//学院
        	map.put("college", c.getCollege()!=null?c.getCollege().getName():"");
        	//优秀
        	map.put("excellent", c.getExcellent()!=null?c.getExcellent():"");
        	//良好
        	map.put("well", c.getWell()!=null?c.getWell():"");
        	//中等
        	map.put("medium", c.getMedium()!=null?c.getMedium():"");
        	//差
        	map.put("bad", c.getBad()!=null?c.getBad():"");
        	listMap.add(map);
        }
        try {
            HSSFWorkbook wb = excelService.exportData("export_AduitVo.xls", "exportAduitVo", listMap);
            String filename = "学工队伍考核表"+(exportPage!=null?exportPage:"")+".xls";
            response.setContentType("application/x-excel");     
            response.setHeader("Content-disposition", "attachment;filename=" +new String (filename.getBytes("GBK"),"iso-8859-1"));
            response.setCharacterEncoding("UTF-8");
            OutputStream ouputStream = response.getOutputStream();     
            wb.write(ouputStream);
            ouputStream.flush(); 
            ouputStream.close(); 
        } catch (ExcelException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
	}
}
