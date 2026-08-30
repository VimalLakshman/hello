package com.bornfire.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.format.annotation.DateTimeFormat;

import com.bornfire.entities.BLRS_Access_Role_Entity;
import com.bornfire.entities.BLRS_AuditTable;
import com.bornfire.entities.BLRS_AuditTable_Rep;
import com.bornfire.entities.BLRS_BusinessTable_Entity;
import com.bornfire.entities.BLRS_BusinessTable_Rep;
import com.bornfire.entities.BLRS_UserProfile_Entity;
import com.bornfire.entities.BLRS_UserProfile_Repo;
import com.bornfire.entities.BRECON_Common_Table_Rep;
import com.bornfire.entities.BRECON_DESTINATION_REPO;
import com.bornfire.entities.BankAgentTable;
import com.bornfire.entities.Brecon_core_rep;
import com.bornfire.entities.CalenderMaintanceEntity;
import com.bornfire.entities.CalenderMaintanceRepo;
import com.bornfire.entities.HolidayMaster_Rep;
import com.bornfire.entities.MerchantCategoryRep;
import com.bornfire.entities.MerchantQrCodeRegRep;
import com.bornfire.entities.Organization_Branch_Rep;
import com.bornfire.entities.Organization_Entity;
import com.bornfire.entities.Organization_Repo;
import com.bornfire.services.BIPSBankandBranchServices;
import com.bornfire.services.BLRS_AccessRoleService;
import com.bornfire.services.ListofDataService;
import com.bornfire.services.LoginServices;
import com.bornfire.services.ReportServices;
import com.bornfire.services.UserProfileService;

@Controller
public class NavigationController {

	private static final Logger logger = LoggerFactory.getLogger(NavigationController.class);

	@Autowired
	private LoginServices loginServices;

	@Autowired
	private UserProfileService userProfileService;

	@Autowired
	private BLRS_AccessRoleService accessRoleService;

	@Autowired
	private BLRS_UserProfile_Repo userProfileRep;

	@Autowired
	private ListofDataService listofdataService;

	@Autowired
	private BLRS_AuditTable_Rep auditTableRep;

	@Autowired
	private BLRS_BusinessTable_Rep businessTableRep;

	@Autowired
	MerchantQrCodeRegRep merchantQrCodeRegRep;

	@Autowired
	MerchantCategoryRep merchantCategoryRep;

	@Autowired
	BIPSBankandBranchServices bankandBranchServices;

	@Autowired
	Environment env;
	
	@Autowired
	BRECON_Common_Table_Rep bRECON_Common_Table_Rep;

	@Autowired
	Brecon_core_rep brecon_core_rep;

	@Autowired
	BRECON_DESTINATION_REPO brecon_destination_repo;
	
	@Autowired
	ReportServices reportServices;
	
	@Autowired
	Organization_Branch_Rep organization_Branch_Rep;

	@Autowired
	Organization_Repo organization_Repo;
	
	@Autowired
	HolidayMaster_Rep holidayMaster_Rep;
	
	@Autowired
	CalenderMaintanceRepo calenderMaintanceRepo;
	
	// ---------------------------------------------------------------------------------------------------------------
	// Login & Password Reset
	// ---------------------------------------------------------------------------------------------------------------

	@RequestMapping(value = "changePasswordLogin", method = { RequestMethod.GET, RequestMethod.POST })
	public String changePasswordLogin(@RequestParam(required = false) String formmode, Model md,
			HttpServletRequest req) {
		return "BLRS_ChangePasswordLogin";
	}

	@RequestMapping(value = "resetPassword", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public String resetPassword(@RequestParam(required = false) String formmode, Model md, HttpServletRequest req,
			@RequestParam(required = false) String userid) {
		String loginUser = (String) req.getSession().getAttribute("USERID");
		if (loginUser == null)
			loginUser = "SYSTEM";
		return userProfileService.passwordReset(userid, "Bornfire@123", loginUser);
	}

	@RequestMapping(value = "rest_password", method = RequestMethod.POST)
	@ResponseBody
	public String rest_password(@RequestParam("old_password") String old_password,
			@RequestParam("new_password") String new_password, @RequestParam("user_id") String userid, Model md,
			HttpServletRequest rq) {
		String msg = userProfileService.changePassword(old_password, new_password, userid);
		md.addAttribute("message", "success");
		return msg;
	}

	@GetMapping("/Dashboard")
	public String getMethodName(Model md) {
		return "BLRS_Dashboard";
	}

	// ---------------------------------------------------------------------------------------------------------------
	// User & Business Operations Audit
	// ---------------------------------------------------------------------------------------------------------------

	@RequestMapping(value = "Useroperation", method = { RequestMethod.GET, RequestMethod.POST })
	public String Useroperation(@RequestParam(required = false) String formmode,
			@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date Fromdate,
			@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date Todate, Model md,
			HttpServletRequest rq) {

		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date today = new Date();

		List<BLRS_AuditTable> auditList;
		if (Fromdate != null && Todate != null) {
			auditList = auditTableRep.getauditListLocal(Fromdate, Todate);
		} else if (Fromdate != null) {
			auditList = auditTableRep.getauditListLocalvals(Fromdate);
		} else {
			auditList = auditTableRep.getauditListLocalvals();
		}

		Date minDate = null;
		Date maxDate = null;
		if (auditList != null && !auditList.isEmpty()) {
			for (BLRS_AuditTable audit : auditList) {
				Date d = audit.getAudit_date();
				if (d != null) {
					if (minDate == null || d.before(minDate)) {
						minDate = d;
					}
					if (maxDate == null || d.after(maxDate)) {
						maxDate = d;
					}
				}
			}
		}

		Date effectiveFrom = Fromdate != null ? Fromdate : (minDate != null ? minDate : today);
		Date effectiveTo = Todate != null ? Todate : (maxDate != null ? maxDate : today);

		md.addAttribute("AuditList", auditList);
		md.addAttribute("Fromdate", sdf.format(effectiveFrom));
		md.addAttribute("Todate", sdf.format(effectiveTo));

		return "BLRS_Useroperation";
	}

	@RequestMapping(value = "Businessoperation", method = { RequestMethod.GET, RequestMethod.POST })
	public String Businessoperation(@RequestParam(required = false) String formmode,
			@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date Fromdate,
			@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date Todate, Model md,
			HttpServletRequest rq) {

		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date today = new Date();

		List<BLRS_BusinessTable_Entity> auditList;
		if (Fromdate != null && Todate != null) {
			auditList = businessTableRep.getauditListLocal(Fromdate, Todate);
		} else if (Fromdate != null) {
			auditList = businessTableRep.getauditListLocalvaluesbusiness(Fromdate);
		} else {
			auditList = businessTableRep.getauditListLocalvalues();
		}

		Date minDate = null;
		Date maxDate = null;
		if (auditList != null && !auditList.isEmpty()) {
			for (BLRS_BusinessTable_Entity audit : auditList) {
				Date d = audit.getAudit_date();
				if (d != null) {
					if (minDate == null || d.before(minDate)) {
						minDate = d;
					}
					if (maxDate == null || d.after(maxDate)) {
						maxDate = d;
					}
				}
			}
		}

		Date effectiveFrom = Fromdate != null ? Fromdate : (minDate != null ? minDate : today);
		Date effectiveTo = Todate != null ? Todate : (maxDate != null ? maxDate : today);

		System.out.println("====== [BUSINESS AUDIT INQUIRY] Loaded " + (auditList != null ? auditList.size() : 0)
				+ " records for screen (Fromdate: " + effectiveFrom + ", Todate: " + effectiveTo + ") ======");

		md.addAttribute("AuditList", auditList);
		md.addAttribute("Fromdate", sdf.format(effectiveFrom));
		md.addAttribute("Todate", sdf.format(effectiveTo));

		return "BLRS_Businessoperation";
	}

	// ---------------------------------------------------------------------------------------------------------------
	// User Profile
	// ---------------------------------------------------------------------------------------------------------------

	@RequestMapping(value = "Userprofile", method = { RequestMethod.GET, RequestMethod.POST })
	public String Userprofile(@RequestParam(required = false) String formmode,
			@RequestParam(required = false) String userid, Model md, HttpServletRequest rq) {

		String loginUser = (String) rq.getSession().getAttribute("USERID");
		md.addAttribute("loginuser", loginUser);
		md.addAttribute("RuleIDType", accessRoleService.getRoleIds());

		if (formmode == null || formmode.equalsIgnoreCase("list")) {
			md.addAttribute("formmode", "list");
			List<BLRS_UserProfile_Entity> userProfiles = userProfileService.getUsersList();
			md.addAttribute("userProfiles", userProfiles);
		} else if (formmode.equalsIgnoreCase("add")) {
			md.addAttribute("formmode", "add");
			BLRS_UserProfile_Entity newUser = new BLRS_UserProfile_Entity();
			newUser.setUser_status("Active");
			newUser.setLogin_status("Active");
			md.addAttribute("userProfile", newUser);
		} else if (formmode.equalsIgnoreCase("edit")) {
			md.addAttribute("formmode", "edit");
			md.addAttribute("userProfile", userProfileService.getUser(userid));
		} else if (formmode.equalsIgnoreCase("view")) {
			md.addAttribute("formmode", "view");
			md.addAttribute("userProfile", userProfileService.getUser(userid));
		} else if (formmode.equalsIgnoreCase("verify")) {
			md.addAttribute("formmode", "verify");
			md.addAttribute("userProfile", userProfileService.getUser(userid));
		} else if (formmode.equalsIgnoreCase("cancel")) {
			md.addAttribute("formmode", "cancel");
			md.addAttribute("userProfile", userProfileService.getUser(userid));
		} else if (formmode.equalsIgnoreCase("delete")) {
			md.addAttribute("formmode", "delete");
			md.addAttribute("userProfile", userProfileService.getUser(userid));
		}

		return "BLRS_UserProfile";
	}

	@RequestMapping(value = { "createUser", "editUser" }, method = RequestMethod.POST)
	@ResponseBody
	public String createUser(@RequestParam("formmode") String formmode,
			@ModelAttribute BLRS_UserProfile_Entity userProfile,
			@RequestParam(value = "file", required = false) MultipartFile file, Model md, HttpServletRequest rq)
			throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {

		String loginUser = (String) rq.getSession().getAttribute("USERID");
		if (loginUser == null)
			loginUser = "SYSTEM";

		if (file != null && !file.isEmpty()) {
			userProfile.setPhoto(file.getBytes());
		}

		return userProfileService.addUser(userProfile, formmode, loginUser);
	}

	@RequestMapping(value = "verifyUser", method = RequestMethod.POST)
	@ResponseBody
	public String verifyUser(@RequestParam(value = "userid", required = false) String userid,
			@ModelAttribute BLRS_UserProfile_Entity userProfile, Model md, HttpServletRequest rq) {
		String loginUser = (String) rq.getSession().getAttribute("USERID");
		if (loginUser == null)
			loginUser = "SYSTEM";
		String targetUser = (userid != null && !userid.isEmpty()) ? userid : userProfile.getUserid();
		return userProfileService.verifyUser(targetUser, loginUser);
	}

	@RequestMapping(value = "deleteUser", method = RequestMethod.POST)
	@ResponseBody
	public String deleteUser(@RequestParam(value = "userid", required = false) String userid,
			@ModelAttribute BLRS_UserProfile_Entity userProfile, Model md, HttpServletRequest rq) {
		String loginUser = (String) rq.getSession().getAttribute("USERID");
		if (loginUser == null)
			loginUser = "SYSTEM";
		String targetUser = (userid != null && !userid.isEmpty()) ? userid : userProfile.getUserid();
		return userProfileService.deleteUser(targetUser, "Y", loginUser);
	}

	@RequestMapping(value = "cancelUser", method = RequestMethod.POST)
	@ResponseBody
	public String cancelUser(@RequestParam(value = "userid", required = false) String userid,
			@ModelAttribute BLRS_UserProfile_Entity userProfile, Model md, HttpServletRequest rq) {
		String loginUser = (String) rq.getSession().getAttribute("USERID");
		if (loginUser == null)
			loginUser = "SYSTEM";
		String targetUser = (userid != null && !userid.isEmpty()) ? userid : userProfile.getUserid();
		return userProfileService.cancelUser(targetUser, loginUser);
	}

	@RequestMapping(value = { "passwordResetUser", "passwordReset", "passwordReset1" }, method = { RequestMethod.GET,
			RequestMethod.POST })
	@ResponseBody
	public String passwordResetUser(@RequestParam(value = "userid", required = false) String userid,
			@RequestParam(value = "userid1", required = false) String userid1,
			@RequestParam(value = "newpass", required = false) String newpass,
			@RequestParam(value = "password", required = false) String newPass, Model md, HttpServletRequest rq) {
		String loginUser = (String) rq.getSession().getAttribute("USERID");
		if (loginUser == null)
			loginUser = "SYSTEM";
		String targetUser = (userid != null && !userid.isEmpty()) ? userid : userid1;
		String pass = (newpass != null && !newpass.isEmpty()) ? newpass : newPass;
		if (pass == null || pass.isEmpty())
			pass = "Bornfire@123";
		return userProfileService.passwordReset(targetUser, pass, loginUser);
	}

	@RequestMapping(value = "getUserBlobImage/{userid}", method = RequestMethod.GET)
	@ResponseBody
	public String getUserBlobImage(@PathVariable("userid") String userid) {
		if (userid != null) {
			BLRS_UserProfile_Entity user = userProfileService.getUser(userid);
			if (user != null && user.getPhoto() != null && user.getPhoto().length > 0) {
				return Base64.getEncoder().encodeToString(user.getPhoto());
			}
		}
		return "";
	}

	@RequestMapping(value = "getRoleDetails/{roleId}", method = RequestMethod.GET)
	@ResponseBody
	public String getRoleDetails(@PathVariable("roleId") String roleId) {
		BLRS_Access_Role_Entity role = accessRoleService.getRole(roleId);
		return role.getRole_desc() != null ? role.getRole_desc() : "";
	}

	// ---------------------------------------------------------------------------------------------------------------
	// Access Control & Roles
	// ---------------------------------------------------------------------------------------------------------------

	@RequestMapping(value = "Accesscontrol", method = { RequestMethod.GET, RequestMethod.POST })
	public String Accesscontrol(@RequestParam(required = false) String formmode,
			@RequestParam(required = false) String role_id, Model md, HttpServletRequest rq) {

		String loginUser = (String) rq.getSession().getAttribute("USERID");
		md.addAttribute("loginuser", loginUser);

		if (formmode == null || formmode.equalsIgnoreCase("list")) {
			md.addAttribute("formmode", "list");
			List<BLRS_Access_Role_Entity> accessRoles = accessRoleService.getRoleList();
			md.addAttribute("accessRoles", accessRoles);
			md.addAttribute("AccessandRoles", accessRoles);
		} else if (formmode.equalsIgnoreCase("add")) {
			md.addAttribute("formmode", "add");
			BLRS_Access_Role_Entity newRole = new BLRS_Access_Role_Entity();
			newRole.setWork_class("M");
			md.addAttribute("accessRole", newRole);
			md.addAttribute("IPSAccessRole", newRole);
		} else if (formmode.equalsIgnoreCase("edit")) {
			md.addAttribute("formmode", "edit");
			BLRS_Access_Role_Entity role = accessRoleService.getRole(role_id);
			md.addAttribute("accessRole", role);
			md.addAttribute("IPSAccessRole", role);
		} else if (formmode.equalsIgnoreCase("view") || formmode.equalsIgnoreCase("viewnew")) {
			md.addAttribute("formmode", "view");
			BLRS_Access_Role_Entity role = accessRoleService.getRole(role_id);
			md.addAttribute("accessRole", role);
			md.addAttribute("IPSAccessRole", role);
		} else if (formmode.equalsIgnoreCase("verify")) {
			md.addAttribute("formmode", "verify");
			BLRS_Access_Role_Entity role = accessRoleService.getRole(role_id);
			md.addAttribute("accessRole", role);
			md.addAttribute("IPSAccessRole", role);
		} else if (formmode.equalsIgnoreCase("cancel")) {
			md.addAttribute("formmode", "cancel");
			BLRS_Access_Role_Entity role = accessRoleService.getRole(role_id);
			md.addAttribute("accessRole", role);
			md.addAttribute("IPSAccessRole", role);
		} else if (formmode.equalsIgnoreCase("delete")) {
			md.addAttribute("formmode", "delete");
			BLRS_Access_Role_Entity role = accessRoleService.getRole(role_id);
			md.addAttribute("accessRole", role);
			md.addAttribute("IPSAccessRole", role);
		}

		return "BLRS_Accesscontrol";
	}

	@RequestMapping(value = { "createRole", "createAccessRole" }, method = RequestMethod.POST)
	@ResponseBody
	public String createRole(@RequestParam(value = "formmode", required = false) String formmode,
			@ModelAttribute BLRS_Access_Role_Entity accessRole,
			@RequestParam(value = "finalString", required = false) String finalString,
			@RequestParam(value = "adminValue", required = false) String adminValue,
			@RequestParam(value = "auditLogsValue", required = false) String auditLogsValue,
			@RequestParam(value = "operationsValue", required = false) String operationsValue,
			@RequestParam(value = "inquiriesValue", required = false) String inquiriesValue,
			@RequestParam(value = "reportsValue", required = false) String reportsValue, Model md,
			HttpServletRequest rq) {
		String loginUser = (String) rq.getSession().getAttribute("USERID");
		if (loginUser == null)
			loginUser = "SYSTEM";

		if (formmode == null || formmode.trim().isEmpty()) {
			formmode = "add";
		}

		if (finalString != null && !finalString.isEmpty()) {
			accessRole.setMenulist(finalString);
		}
		if (adminValue != null)
			accessRole.setAdmin(adminValue);
		if (auditLogsValue != null)
			accessRole.setAudit_logs(auditLogsValue);
		if (operationsValue != null)
			accessRole.setOperations(operationsValue);
		if (inquiriesValue != null)
			accessRole.setInquiries(inquiriesValue);
		if (reportsValue != null)
			accessRole.setReports(reportsValue);

		return accessRoleService.addRole(accessRole, formmode, loginUser);
	}

	@RequestMapping(value = "verifyRole", method = RequestMethod.POST)
	@ResponseBody
	public String verifyRole(@RequestParam(value = "role_id", required = false) String roleId,
			@ModelAttribute BLRS_Access_Role_Entity accessRole, Model md, HttpServletRequest rq) {
		String loginUser = (String) rq.getSession().getAttribute("USERID");
		if (loginUser == null)
			loginUser = "SYSTEM";
		String targetRole = (roleId != null && !roleId.isEmpty()) ? roleId : accessRole.getRole_id();
		return accessRoleService.verifyRole(targetRole, loginUser);
	}

	@RequestMapping(value = "deleteRole", method = RequestMethod.POST)
	@ResponseBody
	public String deleteRole(@RequestParam(value = "role_id", required = false) String roleId,
			@ModelAttribute BLRS_Access_Role_Entity accessRole, Model md, HttpServletRequest rq) {
		String loginUser = (String) rq.getSession().getAttribute("USERID");
		if (loginUser == null)
			loginUser = "SYSTEM";
		String targetRole = (roleId != null && !roleId.isEmpty()) ? roleId : accessRole.getRole_id();
		return accessRoleService.deleteRole(targetRole, loginUser);
	}

	@RequestMapping(value = "cancelRole", method = RequestMethod.POST)
	@ResponseBody
	public String cancelRole(@RequestParam(value = "role_id", required = false) String roleId,
			@ModelAttribute BLRS_Access_Role_Entity accessRole, Model md, HttpServletRequest rq) {
		String loginUser = (String) rq.getSession().getAttribute("USERID");
		if (loginUser == null)
			loginUser = "SYSTEM";
		String targetRole = (roleId != null && !roleId.isEmpty()) ? roleId : accessRole.getRole_id();
		return accessRoleService.verifyRole(targetRole, loginUser);
	}

	@RequestMapping(value = { "userprofileimage", "userprofileimage/{userid}" }, method = RequestMethod.GET)
	@ResponseBody
	public String userprofileimage(@RequestParam(value = "userphoto", required = false) String userphoto,
			@RequestParam(value = "userid", required = false) String userid,
			@PathVariable(value = "userid", required = false) String pathUserid, HttpServletRequest req) {
		String targetId = (userphoto != null && !userphoto.trim().isEmpty()) ? userphoto : userid;
		if (targetId == null || targetId.trim().isEmpty()) {
			targetId = pathUserid;
		}
		if (targetId == null || targetId.trim().isEmpty()) {
			targetId = (String) req.getSession().getAttribute("USERID");
		}
		if (targetId != null && !targetId.trim().isEmpty()) {
			BLRS_UserProfile_Entity user = userProfileService.getUser(targetId.trim());
			if (user != null && user.getPhoto() != null && user.getPhoto().length > 0) {
				return Base64.getEncoder().encodeToString(user.getPhoto());
			}
		}
		return "";
	}

	@RequestMapping(value = "MerchantQRREG")
	public String MerchantQRREG(@RequestParam(required = false) String formmode,
			@RequestParam(required = false) String merchant_acct_no, @RequestParam(required = false) String userid,
			@RequestParam(required = false) Optional<Integer> page,
			@RequestParam(value = "size", required = false) Optional<Integer> size,
			@ModelAttribute BankAgentTable bankAgentTable, Model md, HttpServletRequest req)
			throws FileNotFoundException, SQLException, IOException {

		String roleId = (String) req.getSession().getAttribute("ROLEID");
		md.addAttribute("IPSRoleMenu", accessRoleService.getRole(roleId));

		if (formmode == null || formmode.equals("list")) {

			md.addAttribute("formmode", "list");
			md.addAttribute("menu", "MMenupage");
			md.addAttribute("bankDetail", merchantQrCodeRegRep.findAllData());
		} else if (formmode.equals("add")) {

			md.addAttribute("formmode", formmode);
			String paycode = env.getProperty("ipsx.qr.payeecode");
			md.addAttribute("paycode", paycode);
			md.addAttribute("merchantcategory", merchantCategoryRep.findAllCustom());

		} else if (formmode.equals("edit")) {

			md.addAttribute("formmode", formmode);
			md.addAttribute("branchDet", merchantQrCodeRegRep.findByIdCustom(merchant_acct_no));
			md.addAttribute("merchantcategory", merchantCategoryRep.findAllCustom());

		} else if (formmode.equals("delete")) {

			md.addAttribute("formmode", formmode);
			md.addAttribute("branchDet", merchantQrCodeRegRep.findByIdCustom(merchant_acct_no));

		} else if (formmode.equals("verify")) {

			md.addAttribute("formmode", formmode);
			md.addAttribute("branchDet", merchantQrCodeRegRep.findByIdCustom(merchant_acct_no));

		} else if (formmode.equals("qrcode")) {

			md.addAttribute("formmode", "list");
			md.addAttribute("formmode1", formmode);
			md.addAttribute("bankDetail", merchantQrCodeRegRep.findAllData());
			md.addAttribute("branchDet", merchantQrCodeRegRep.findByIdCustom(merchant_acct_no));
			String msg = bankandBranchServices.getqrcode(merchant_acct_no);
			md.addAttribute("msg", msg);
			System.out.println("msg :" + msg);
		}

		return "MerchantQrRegistration";
	}

	@RequestMapping(value = "Dataupload", method = RequestMethod.GET)
	public String Dataupload(@RequestParam(required = false) String formmode,
			@RequestParam(required = false) String keyword, Model md, HttpServletRequest req) {
		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");

		}

		return "Dataupload";
	}

	@RequestMapping(value = "coresystem", method = RequestMethod.GET)
	public String coresystem(@RequestParam(required = false) String formmode,
			@RequestParam(required = false) String srlno, String keyword, Model md, HttpServletRequest req) {
		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
			md.addAttribute("list", brecon_core_rep.getcoresystemlistdata());

		}

		return "Brecon_core";
	}

	@RequestMapping(value = "clearingsystem", method = RequestMethod.GET)
	public String clearingsystem(@RequestParam(required = false) String formmode,
			@RequestParam(required = false) String srlno, String keyword, Model md, HttpServletRequest req) {
		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
			md.addAttribute("list", brecon_destination_repo.getDestination());

		} else if (formmode.equals("upload")) {
			md.addAttribute("formmode", "upload");

		}

		return "Brecon_clearing";
	}
	
	@RequestMapping(value = "Tmtfiletransaction", method = RequestMethod.GET)
	public String Tmtfiletransaction(@RequestParam(required = false) String formmode,
			@RequestParam(required = false) String srlno, String keyword, Model md, HttpServletRequest req) {
		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
			md.addAttribute("chargeback", brecon_destination_repo.getlist());
		} else if (formmode.equals("upload")) {
			md.addAttribute("formmode", "upload");
		} else if (formmode.equals("list1")) {
			md.addAttribute("formmode", "list1");
		} else if (formmode.equals("upload1")) {
			md.addAttribute("formmode", "upload1");
		} else if (formmode.equals("upload2")) {
			md.addAttribute("formmode", "upload2");
		} else if (formmode.equals("upload3")) {
			md.addAttribute("formmode", "upload3");
		} else if (formmode.equals("upload4")) {
			md.addAttribute("formmode", "upload4");
		}

		return "Tmtfileupload";
	}
	
	@RequestMapping(value = "Reconsilationdatas", method = RequestMethod.GET)
	public String Reconsilationdatas(@RequestParam(required = false) String formmode,
			@RequestParam(required = false) String srlno, String keyword, Model md,
			@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date Fromdate,HttpServletRequest req) {
		
		LocalDate today = LocalDate.now(); // Get today's date
		Date fromDateToUse; // Declare a variable for the date to use
		if (Fromdate != null) {
			// If Fromdate has a value, use it
			fromDateToUse = Fromdate;
		} else {
			// If Fromdate has no value, use today's date
			fromDateToUse = java.sql.Date.valueOf(today);
		}
		
		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
			//common table
			md.addAttribute("listvaluesdatas", bRECON_Common_Table_Rep.getcommondatavalues(fromDateToUse));
			md.addAttribute("datavalue", fromDateToUse);
			//source table
			md.addAttribute("listcoredatas1", brecon_core_rep.getcoresystemlistvalue(fromDateToUse));
			//destination table
			md.addAttribute("listvaluesdatas1", brecon_destination_repo.getDestinationdatavalues(fromDateToUse));
			
			//popup
			md.addAttribute("listcoredatas21", bRECON_Common_Table_Rep.getDestinationvaluesdatavalue());
		}  else if (formmode.equals("upload")) {
			md.addAttribute("formmode", "upload");
		}else if (formmode.equals("view1")) {
			md.addAttribute("formmode", "view1");
			md.addAttribute("srlno", brecon_core_rep.getSrlno(srlno));
		}
		return "Reconsilationsdata";
	}
	
	@RequestMapping(value = "Reports_data", method = RequestMethod.GET)
	public String Reports_data(Model md, HttpServletRequest req) {

		md.addAttribute("menu", "XBRLReports");

		md.addAttribute("reportlist", reportServices.getReportsList("BRF REPORTS"));
		return "XBRLReports";
	}
	
	@RequestMapping(value = "OrganizationDetails", method = { RequestMethod.GET, RequestMethod.POST })
	public String organizationDetails(@RequestParam(required = false) String formmode,
	        @RequestParam(required = false) String branch_name, String branch_code, Model md, HttpServletRequest req,
	        @RequestParam(required = false) Long record_srl, @RequestParam(required = false) String month,
	        @RequestParam(required = false) String year) {
		String roleId = (String) req.getSession().getAttribute("ROLEID");
		md.addAttribute("IPSRoleMenu", accessRoleService.getRole(roleId));
	    // -------- FIX: Add this block to populate the menu for the user's role --------

	    if (formmode == null || formmode.equals("add")) {
	        md.addAttribute("formmode", "add");
	        Organization_Entity organizationList = null;
	        List<Organization_Entity> organization = organization_Repo.getAllList();
	        if (!organization.isEmpty()) {
	            organizationList = organization.get(0);
	        }
	        md.addAttribute("organization", organizationList);
	        md.addAttribute("OrgBranch", organization_Branch_Rep.getbranchlist());

	    } else if (formmode.equals("ModifyHead")) {
	        md.addAttribute("formmode", "ModifyHead");
	        Organization_Entity organizationList = null;
	        List<Organization_Entity> organization = organization_Repo.getAllList();
	        if (!organization.isEmpty()) {
	            organizationList = organization.get(0);
	        }
	        md.addAttribute("organization", organizationList);

	    } else if (formmode.equals("DeleteBranch")) {
	        md.addAttribute("formmode", "DeleteBranch");
	        md.addAttribute("OrgBranch", organization_Branch_Rep.getOrgBranch1(branch_code));

	    } else if (formmode.equals("AddBranch")) {
	        md.addAttribute("formmode", "AddBranch");

	    } else if (formmode.equals("modify")) {
	        md.addAttribute("formmode", "modify");
	        md.addAttribute("OrgBranch", organization_Branch_Rep.getOrgBranch1(branch_code));

	    } else if (formmode.equals("ModifyBranch")) {
	        md.addAttribute("formmode", "ModifyBranch");
	        md.addAttribute("OrgBranch", organization_Branch_Rep.getbranchlist());

	    } else if (formmode.equals("view")) {
	        md.addAttribute("formmode", "view");
	        md.addAttribute("OrgBranch", organization_Branch_Rep.getOrgBranch1(branch_code));

	    } else if (formmode.equals("addholiday")) {
	        md.addAttribute("formmode", "addholiday");

	    } else if (formmode.equals("UploadHoliday")) {
	        md.addAttribute("formmode", "UploadHoliday");

	    } else if (formmode.equals("listholiday") || formmode.equals("ModifyHoliday")) {
	        md.addAttribute("formmode", "listholiday");
	        md.addAttribute("Listofvalues", holidayMaster_Rep.getlistofHoliday());

	    } else if (formmode.equals("viewrecord") || formmode.equals("modifyholidayrecord")) {
	        md.addAttribute("formmode", formmode);
	        md.addAttribute("holiday_id", record_srl);
	        md.addAttribute("singlerecord", holidayMaster_Rep.getsinglevalueHoliday(record_srl));

	    } else if (formmode.equals("calender")) {
	        md.addAttribute("formmode", "calender");
	        md.addAttribute("holidays_list", holidayMaster_Rep.holidayList(year, month));
	    }

	    List<CalenderMaintanceEntity> calenderMaintanceEntityList = calenderMaintanceRepo.getAllCalenderMaintanceList();
	    md.addAttribute("calender_list", calenderMaintanceEntityList);

	    return "OrganizationDetails";
	}
}