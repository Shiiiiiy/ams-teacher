package com.uws.teacher.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
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
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.uws.common.service.IBaseDataService;
import com.uws.common.service.ICommonRoleService;
import com.uws.common.util.CYLeagueUtil;
import com.uws.comp.service.ICompService;
import com.uws.core.base.BaseController;
import com.uws.core.excel.ExcelException;
import com.uws.core.excel.service.IExcelService;
import com.uws.core.hibernate.dao.support.Page;
import com.uws.core.session.SessionFactory;
import com.uws.core.session.SessionUtil;
import com.uws.core.util.DataUtil;
import com.uws.domain.base.BaseAcademyModel;
import com.uws.domain.base.BaseClassModel;
import com.uws.domain.base.BaseMajorModel;
import com.uws.domain.base.BaseTeacherModel;
import com.uws.domain.teacher.SetClassTeacher;
import com.uws.domain.teacher.SetCollegeTeacher;
import com.uws.domain.teacher.StuJobTeamSetModel;
import com.uws.domain.teacher.TeacherInfoModel;
import com.uws.log.LoggerFactory;
import com.uws.sys.model.Dic;
import com.uws.sys.service.DicUtil;
import com.uws.sys.service.FileUtil;
import com.uws.sys.service.impl.DicFactory;
import com.uws.sys.service.impl.FileFactory;
import com.uws.teacher.service.IStuJobTeamService;
import com.uws.teacher.util.Constants;
import com.uws.util.CheckUtils;
import com.uws.util.ProjectSessionUtils;

@Controller
public class StuJobTeamSetController extends BaseController {
	@Autowired
	private IStuJobTeamService stuJobTeamService;
	@Autowired
	private IBaseDataService baseDateService;
	@Autowired
  	private ICommonRoleService commonRoleService;
	@Autowired
	private ICompService compService;
	@Autowired
	private static DicUtil dicUtil = DicFactory.getDicUtil();
	@Autowired
	private FileUtil fileUtil = FileFactory.getFileUtil();
	@Autowired
    private IExcelService excelService;
	@Autowired
	private IBaseDataService baseDataService;
    private LoggerFactory log = new LoggerFactory(StuJobTeamSetController.class);
	private SessionUtil sessionUtil = SessionFactory.getSession(Constants.TEACHER_MAINTAIN);
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }
	/**
	 * 通过视图V_CLASS_TEACHER_SET查询班级学工信息
	 * @param model
	 * @param request
	 * @param setClassTeacher
	 * @return
	 */
	@RequestMapping({Constants.TEACHER_CLASS_MANAGE + "/opt-query/classStuJobTeamList"})
	public String stuJobTeamSetList(ModelMap model, HttpServletRequest request, SetClassTeacher setClassTeacher, String roleName) {
		String collegeId = ProjectSessionUtils.getCurrentTeacherOrgId(request);
		if(CheckUtils.isCurrentOrgEqCollege(collegeId)) {
			BaseAcademyModel ba = new BaseAcademyModel();
			ba.setId(collegeId);
			setClassTeacher.setCollege(ba);
			model.addAttribute("flag", true);
		}
		//二级学院学工办主任过滤
		/*if(commonRoleService.checkUserIsExist(this.sessionUtil.getCurrentUserId(), Constants.HKY_COLLEGE_DIRECTOR)){
			BaseAcademyModel ba = new BaseAcademyModel();
			ba.setId(collegeId);
			setClassTeacher.setCollege(ba);
		}*/
		
		List<BaseAcademyModel> collegeList = baseDateService.listBaseAcademy();
		List<BaseMajorModel> majorList = null;
		//根据返回的学院查询专业
		if(DataUtil.isNotNull(setClassTeacher.getCollege()) && DataUtil.isNotNull(setClassTeacher.getCollege().getId()))
			majorList = compService.queryMajorByCollage(setClassTeacher.getCollege().getId());
		List<BaseClassModel> klassList = null;
		//根据返回的专业查询班级
		if(DataUtil.isNotNull(setClassTeacher.getMajor()) && DataUtil.isNotNull(setClassTeacher.getMajor().getId()))
			klassList = compService.queryClassByMajor(setClassTeacher.getMajor().getId());
		
		String pageNo = request.getParameter("pageNo");
		pageNo = pageNo != null ? pageNo : "1";
		Page page = stuJobTeamService.queryPageStuJobTeam(setClassTeacher, roleName, Integer.parseInt(pageNo), Page.DEFAULT_PAGE_SIZE);
		model.addAttribute("page", page);
		model.addAttribute("roleName", roleName);
		model.addAttribute("collegeList", collegeList);
		model.addAttribute("majorList", majorList);
		model.addAttribute("klassList", klassList);
		model.addAttribute("gradeList", baseDateService.listGradeList());
		model.addAttribute("setClassTeacher", setClassTeacher);
		return Constants.TEACHER_MANAGE + "/classStuJobTeamList";
	}
	/**
	 * 班级学工队伍设置
	 * @param request
	 * @param response
	 * @param classId
	 * @param teacherId
	 * @param teacherType
	 * @return
	 */
	@RequestMapping({ Constants.TEACHER_CLASS_MANAGE + "/nsm/stuJobTeamSetting" })
	@ResponseBody
	public String setClassJob(HttpServletRequest request, HttpServletResponse response,
			String classId, String teacherId, String teacherType) {
		if(DataUtil.isNotNull(teacherType) && DataUtil.isNotNull(teacherId)) {
			if(teacherType.equals("1")) {
				teacherType = dicUtil.getDicInfo("TEACHER_TYPE", "HEADMASTER").getId();
				//保存班主任到班级表	
				BaseClassModel po = baseDataService.findClassById(classId);
				BaseTeacherModel teacher = new BaseTeacherModel();
				teacher.setId(teacherId);
				po.setHeadermaster(teacher);
				stuJobTeamService.updateBaseClass(po);
				//保存角色
				stuJobTeamService.saveKlassCounsellor(classId, teacherType, teacherId, "HKY_CLASS_HEADEMASTER");
			}else {
				teacherType = dicUtil.getDicInfo("TEACHER_TYPE", "TEACHER_COUNSELLOR").getId();
				stuJobTeamService.saveKlassCounsellor(classId, teacherType, teacherId, "HKY_TEACHEING_COUNSELOR");
			}
			stuJobTeamService.editStuJobTeamSet(classId, teacherId, teacherType);
		} else
			return null;
		return "success";
	}
	/**
	 * 班长设置方法
	 * @param request
	 * @param response
	 * @param classId
	 * @param stuId
	 * @return
	 */
	@RequestMapping({ Constants.TEACHER_CLASS_MANAGE + "/nsm/monitorSetting" })
	@ResponseBody
	public String setClassMonitor(HttpServletRequest request, HttpServletResponse response,
			String classId, String stuId) {
		if(DataUtil.isNotNull(classId) && DataUtil.isNotNull(stuId)) {
			stuJobTeamService.setMonitor(classId, stuId);
			stuJobTeamService.setMonitorRole(stuId);
		}else
			return null;
		return "success";
	}
	/**
	 * 导出页面弹窗
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequestMapping({ Constants.TEACHER_CLASS_MANAGE + "/nsm/exportView" })
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
         return Constants.TEACHER_MANAGE+"/exportStuJobView";
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
    @RequestMapping({ Constants.TEACHER_CLASS_MANAGE + "/opt-export/exportStuJob" })
	public void exportStuJob(HttpServletRequest request, HttpServletResponse response, SetClassTeacher setClassTeacher, String roleName) {
		log.info("导出学工队伍表方法");
        String exportSize=request.getParameter("stuJob_exportSize");
        String exportPage=request.getParameter("stuJob_exportPage");
        String collegeId = ProjectSessionUtils.getCurrentTeacherOrgId(request);
		if(CheckUtils.isCurrentOrgEqCollege(collegeId)) {
			BaseAcademyModel ba = new BaseAcademyModel();
			ba.setId(collegeId);
			setClassTeacher.setCollege(ba);
		}
        Page page = stuJobTeamService.queryPageStuJobTeam(setClassTeacher, roleName, Integer.parseInt(exportPage), Integer.parseInt(exportSize));
        List<SetClassTeacher> sctl = new ArrayList<SetClassTeacher>();
        sctl = (List<SetClassTeacher>) page.getResult();
        List<Map> listMap= new ArrayList<Map>();
        for(SetClassTeacher sct:sctl) {
        	Map<String, Object> map = new HashMap<String, Object>();
        	//学院
        	map.put("college", (sct.getCollege()!=null?sct.getCollege().getName():""));
        	//专业
        	map.put("major", (sct.getMajor()!=null?sct.getMajor().getMajorName():""));
        	//班级
        	map.put("klass", (sct.getKlass()!=null?sct.getKlass().getClassName():""));
        	//班主任
        	map.put("headMaster", (sct.getHeadMaster()!=null?sct.getHeadMaster().getName():""));
        	//教学辅导员
        	map.put("teacherCounsellor", (sct.getTeacherCounsellor()!=null?sct.getTeacherCounsellor().getName():""));
        	//班长
        	map.put("monitor", ((sct.getKlass()!=null && sct.getKlass().getMonitor()!=null)?sct.getKlass().getMonitor().getName():""));
        	listMap.add(map);
        }
        try {
            HSSFWorkbook wb = excelService.exportData("export_stuJobVo.xls", "exportStuJobVo", listMap);
            String filename = "班级学工队伍表"+(exportPage!=null?exportPage:"")+".xls";
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
	/**
	 * 学院辅导员设置列表页面
	 * @param model
	 * @param request
	 * @param po
	 * @return
	 */
	@RequestMapping({Constants.TEACHER_COLLEGE_MANAGE+"/opt-query/collegeStuJobTeamList"})
	public String collegeStuJobList(ModelMap model, HttpServletRequest request, SetCollegeTeacher po) 
	{
		//增加2级院系过滤
		String collegeId = ProjectSessionUtils.getCurrentTeacherOrgId(request);
		boolean flag = CheckUtils.isCurrentOrgEqCollege(collegeId);
		List<BaseAcademyModel> collegeList = baseDateService.listBaseAcademy();
		List<BaseAcademyModel> colList = new ArrayList<BaseAcademyModel>();
		for(BaseAcademyModel bam:collegeList){
			if(flag && bam.getId().equals(collegeId)){
				colList.add(bam);
				break;
			}
		}
		List<StuJobTeamSetModel>  stuJobTeamList = stuJobTeamService.getAllStuJobTeamSet();
		Map<String,List<StuJobTeamSetModel>> resultMap = new HashMap<String,List<StuJobTeamSetModel>>();
		String keyId = "";
		List<StuJobTeamSetModel> tempList = null;
		for(StuJobTeamSetModel teamModel : stuJobTeamList)
		{
			keyId = teamModel.getCollege().getId() + "_" + teamModel.getTeacherType().getId();
			if(resultMap.containsKey(keyId))
			{
				resultMap.get(keyId).add(teamModel);
			}
			else
			{
				tempList = new ArrayList<StuJobTeamSetModel>();
				tempList.add(teamModel);
				resultMap.put(keyId, tempList);
			}
						
		}
		model.addAttribute("resultMap", resultMap);
		model.addAttribute("setCollegeTeacher", po);
		List<Dic> teacherTypeList = new ArrayList<Dic>();
		teacherTypeList.add(dicUtil.getDicInfo("TEACHER_TYPE", "EVALUATION_COUNSELLOR"));
		teacherTypeList.add(dicUtil.getDicInfo("TEACHER_TYPE", "SUBSIDIZE_COUNSELLOR"));
		teacherTypeList.add(dicUtil.getDicInfo("TEACHER_TYPE", "CAREER_COUNSELLOR"));
		model.addAttribute("teacherTypeList", teacherTypeList);
		if(colList.size()==0){
			model.addAttribute("collegeList", collegeList);
		}else{
			model.addAttribute("collegeList", colList);
		}
		
		return Constants.TEACHER_MANAGE +"/collegeStuJobTeamList";
	}
	/**
	 * 学院学工队伍设置
	 * @param collegeId
	 * @param teacherId
	 * @param teacherTypeId
	 * @return
	 */
	@RequestMapping({ Constants.TEACHER_COLLEGE_MANAGE + "/nsm/stuJobTeamSetting" })
	@ResponseBody
	public String setCollegeJob(String collegeId, String teacherId, String teacherTypeId) 
	{
		if(DataUtil.isNotNull(teacherTypeId) && DataUtil.isNotNull(collegeId) && DataUtil.isNotNull(teacherId)) 
		{
			if(dicUtil.getDicInfo("TEACHER_TYPE", "EVALUATION_COUNSELLOR").getId().equals(teacherTypeId)) 
			{
				stuJobTeamService.saveCollegeCounsellor(collegeId, teacherTypeId, teacherId, "HKY_EVALUATION_COUNSELOR");
			}else if(dicUtil.getDicInfo("TEACHER_TYPE", "SUBSIDIZE_COUNSELLOR").getId().equals(teacherTypeId)) 
			{
				stuJobTeamService.saveCollegeCounsellor(collegeId, teacherTypeId, teacherId, "HKY_SPONSOR_COUNSELOR");
			}else if(dicUtil.getDicInfo("TEACHER_TYPE", "CAREER_COUNSELLOR").getId().equals(teacherTypeId))
			{
				stuJobTeamService.saveCollegeCounsellor(collegeId, teacherTypeId, teacherId, "HKY_EMPLOYMENT_COUNSELOR");
			}
			stuJobTeamService.editCollegeJobSet(collegeId, teacherId, teacherTypeId);
		}
		return "success";
	}
	
	/**
	 * 
	 * @Title: delJobTeamSetting
	 * @Description: 删除设置信息
	 * @param request
	 * @param response
	 * @param id
	 * @return
	 * @throws
	 */
	@ResponseBody
	@RequestMapping({ Constants.TEACHER_COLLEGE_MANAGE + "/opt-del/delJobTeamSetting" })
	public String delJobTeamSetting(HttpServletRequest request, HttpServletResponse response,String id) 
	{
		if(!StringUtils.isEmpty(id))
			stuJobTeamService.deleteSettingById(id);
		return "success";
	}
	
	/**
	 * 教师个人信息维护
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequestMapping(value={Constants.TEACHER_MAINTAIN + "/opt-query/personInfo",  Constants.TEACHER_MAINTAIN + "/nsm/viewTeacherInfo" })
	public String maintainPersonInfo(HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		String userId = "";
		String flag = request.getParameter("flag");
		if(flag==null || flag.equals(""))
			userId = sessionUtil.getCurrentUserId();
		else
			userId = request.getParameter("id");
		TeacherInfoModel tim = stuJobTeamService.getTeacherExtendByTeacherId(userId);
		//获取教师所在学院
		BaseAcademyModel college = stuJobTeamService.getCollegeByTeacherId(userId);
		if(tim!=null && DataUtil.isNotNull(tim.getId())) {
			//获取当前日期，并计算出年龄
			Date td = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
			//计算年龄
			Integer age = null;
			if(DataUtil.isNotNull(tim.getTeacher().getBirthday()))
				age = Integer.parseInt(sdf.format(td))- Integer.parseInt(sdf.format(tim.getTeacher().getBirthday()));
			model.addAttribute("age", age);
			model.addAttribute("listFile", fileUtil.getFileRefsByObjectId(tim.getId()));
		}
		else
		{	//逻辑问题，补丁添加  还有点问题,所有教师都能看到,时间问题先这样,后续维护的人请注意一下 哎~~~~   
			tim = new TeacherInfoModel();
			tim.setTeacher(baseDataService.findTeacherById(userId));
		}
		model.addAttribute("college", college);
		model.addAttribute("tim", tim);
		if(flag==null || flag.equals(""))
			return Constants.TEACHER_MANAGE +"/personInfo";
		else
			return Constants.TEACHER_MANAGE +"/stuJobTeamSetView";
	}
	
	//个人信息维护保存
	@RequestMapping({ Constants.TEACHER_MAINTAIN + "/opt-save/savePersonInfo" })
	public String saveSetting(ModelMap model, HttpServletRequest request, TeacherInfoModel po, String[] fileId) {
		stuJobTeamService.updateTeacherInfo(po, fileId);
		return  "redirect:" + Constants.TEACHER_MAINTAIN + "/opt-query/personInfo.do";
	}
}
