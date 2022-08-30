package com.atguigu.controller;


import com.atguigu.base.BaseController;
import com.atguigu.entity.Admin;
import com.atguigu.service.AdminService;
import com.atguigu.service.RoleService;
import com.atguigu.util.QiniuUtils;
import com.github.pagehelper.PageInfo;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController extends BaseController {


    private static final String PAGE_INDEX = "admin/index";
    private static final String PAGE_CREATE = "admin/create";
    private static final String PAGE_EDIT = "admin/edit";
    private static final String LIST_ACTION = "redirect:/admin";
    private static final String PAGE_UPLOAD = "admin/upload";
    private static final String PAGE_ASSIGN_SHOW = "admin/assignShow";


    @Reference
    private AdminService adminService;
    @Reference
    private RoleService roleService;


    /**
     * 跳转'admin/index.html'页面
     *
     * @param filters : 搜索条件
     * @param model
     * @return
     */
    @RequestMapping
    public String index(@RequestParam Map filters, Model model) {
        //数据回显
        model.addAttribute("filters", filters);
        //分页查询
        PageInfo<Admin> pageInfo = adminService.findPage(filters);
        model.addAttribute("page", pageInfo);
	model.addAttribute("num",1);
        return PAGE_INDEX;
	
    }


    /**
     * 跳转'admin/create.html'页面
     *
     * @return
     */
    @RequestMapping("/create")
    public String create() {
        return PAGE_CREATE;
    }


    /**
     * 添加用户
     *
     * @return
     */
    @PostMapping("/save")
    public String save(Admin admin, Model model) {
        adminService.insert(admin);
        return successPage(model, "添加用户成功!");
    }

    /**
     * 跳转'admin/edit.html'页面
     *
     * @param id : 用户id
     * @return
     */
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        Admin admin = adminService.getById(id);
        model.addAttribute("admin", admin);
        return PAGE_EDIT;
    }


    /**
     * 修改用户
     *
     * @param admin
     * @param model
     * @return
     */
    @PostMapping("/update")
    public String update(Admin admin, Model model) {
        adminService.update(admin);
        return successPage(model, "修改用户成功!");
    }


    /**
     * 删除用户
     *
     * @param id
     * @return
     */
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        adminService.delete(id);
        return LIST_ACTION;
    }


    /**
     * 跳转'admin/upload.html'页面
     *
     * @param id ：用户id
     * @return
     */
    @GetMapping("/uploadShow/{id}")
    public String uploadShow(@PathVariable Long id, Model model) {
        model.addAttribute("id", id);
        return PAGE_UPLOAD;
    }

    @PostMapping("/upload/{id}")
    public String upload(@PathVariable Long id, MultipartFile file, Model model) throws IOException {
        //上传文件
        String prefixName = UUID.randomUUID().toString().replace("-", "");
        String suffixName = file.getOriginalFilename().split("\\.")[1];
        String fileName = prefixName + "." + suffixName;
        QiniuUtils.upload2Qiniu(file.getBytes(), fileName);
        //将文件信息保存到acl_admin表中
        Admin admin = new Admin();
        admin.setId(id);
        admin.setHeadUrl("http://rgrgwl4e8.hn-bkt.clouddn.com/" + fileName);
        adminService.update(admin);

        return successPage(model, "用户头像上传成功!");
    }


    /**
     * 跳转'admin/assignShow.html'页面
     *
     * @param id : admin的id
     * @return
     */
    @GetMapping("/assignShow/{id}")
    public String assignShow(@PathVariable Long id, Model model) {
        model.addAttribute("adminId", id);
        //未分配角色列表(unAssignRoleList) , 已分配角色列表(assignRoleList)
        //unAssignRoleList=list , assignRoleList=list2
        Map<String,Object> map = roleService.findRoleListByAdminId(id);
        //将map中的unAssignRoleList,assignRoleList放入到request域
        model.addAllAttributes(map);
        return PAGE_ASSIGN_SHOW;
    }

    @PostMapping("/assignRole")
    public String assignRole(Long adminId , Long[] roleIds ,Model model){

        adminService.assignRole(adminId,roleIds);

        return successPage(model , "分配角色成功!");

    }
}
