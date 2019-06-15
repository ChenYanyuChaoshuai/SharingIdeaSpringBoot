package com.zhiku.controller;

import com.zhiku.entity.Preference;
import com.zhiku.entity.User;
import com.zhiku.exception.UserNotFoundException;
import com.zhiku.service.CourseService;
import com.zhiku.service.FileService;
import com.zhiku.service.PreferenceService;
import com.zhiku.service.UserService;
import com.zhiku.util.JWTUtil;
import com.zhiku.util.ResponseData;
import com.zhiku.util.UserStatus;
import com.zhiku.view.ColCourseView;
import com.zhiku.view.UserBaseInfoView;
import freemarker.template.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;


@Controller
@RequestMapping("user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private PreferenceService preferenceService;
    @Autowired
    private CourseService courseService;
    @Autowired
    private FileService fileService;
    @Autowired
    JavaMailSender javaMailSender;
    @Autowired
    Configuration freemarkerConfig;

    /**
     * 新用户注册
     * @param request 请求
     * @param username 用户名
     * @param password 密码
     * @param email 邮箱
     * @return
     * 处理流程
     * ----检查参数是否规范
     * ----检测是否重复
     * ----正常注册
     */
    @ResponseBody
    @RequestMapping(value = "registe" ,method = RequestMethod.POST)
    public ResponseData registe(
            HttpServletRequest request,
            @RequestParam(name = "username" ) String username,
            @RequestParam(name = "password" ) String password,
            @RequestParam(name = "email" ) String email){
        ResponseData responseData = null;
        try{
            userService.getUserByUsername(username);
            responseData = ResponseData.badRequest();
            responseData.setMessage("用户名重复");
        }catch (UserNotFoundException unfe){
            try{
                userService.getUserByEmail(email);
                responseData = ResponseData.badRequest();
                responseData.setMessage("邮箱重复");
            }catch (UserNotFoundException e){
                if(userService.registeUser(username,password,email,request)){
                    userService.sendEmail(javaMailSender,username,email,"active",freemarkerConfig);
                    responseData = ResponseData.ok();
                }else{
                    responseData = ResponseData.badRequest();
                    responseData.setMessage("发生了一个预期之外的错误");
                }
            }
        }
        return responseData;
    }

    /**
     * 登录请求
     * @param request 获取用户登录的地址信息
     * @param identity 登录的身份：用户名或邮箱
     * @param password 登录密码
     * @param response 返回，用以添加cookie
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/login" ,method = RequestMethod.POST)
    public ResponseData login(
            HttpServletRequest request,
            @RequestParam(name = "identity" ) String identity,
            @RequestParam(name = "password" ) String password,
            HttpServletResponse response) {
        ResponseData responseData = null;
        User user = null;
        try{
            user = userService.getUserByUsername(identity);
        }catch (UserNotFoundException e){
            try{
                user = userService.getUserByEmail(identity);
            }catch (UserNotFoundException unfe){
                responseData = ResponseData.badRequest();
                responseData.setMessage("账号不存在");
            }
        }
        if(user != null){
            UserStatus userStatus = userService.checkUser(user);
            if(userStatus == UserStatus.NORMAL){
                if(userService.checkPassword(user,password)){
                    responseData = ResponseData.ok();
                    //签发token并添加到cookie中
                    try{
                        String token = JWTUtil.signToken(user);
                        Cookie cookie = new Cookie("token",token);
                        cookie.setPath("/");
                        response.addCookie(cookie);
                        responseData.putDataValue("userIcon",user.getUserAvatar());
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    //修改用户的上次登录时间和ip
                    user.setUserLasttime(new Date());
                    user.setUserLastip(request.getRemoteAddr());
                    userService.saveUser(user);
                }else {
                    responseData = ResponseData.badRequest();
                    responseData.setMessage("用户名和密码不正确");
                }
            }else{
                responseData = ResponseData.badRequest();
                responseData.setMessage(userStatus.getMessage());
            }
        }

        return responseData;
    }

    @ResponseBody
    @RequestMapping(value = "mail/active",method = RequestMethod.GET)
    public ResponseData mailHandler(
            String act,
            String username,
            String code){
        ResponseData responseData = null;
        try{
            User user = userService.getUserByUsername(username);
            UserStatus userStatus = userService.checkUser(user);
            if(userStatus.equals(UserStatus.NORMAL)){
                responseData = ResponseData.ok();
                responseData.setMessage("用户已经激活，可直接登录");
            }else if(code.equals(user.hashCode()+"")){
                if(userStatus.equals(UserStatus.UNCHECKED)){
                    userService.activeEmail(user);
                    responseData = ResponseData.ok();
                }else {
                    responseData = ResponseData.badRequest();
                    responseData.setMessage(userStatus.getMessage());
                }
            }else {
                responseData = ResponseData.customerError();
                responseData.setMessage("无效链接");
            }
        }catch (UserNotFoundException e){
            responseData = ResponseData.badRequest();
            responseData.setMessage("用户不存在");
        }

        return responseData;
    }

    @ResponseBody
    @RequestMapping(value = "mail/forgetPassword",method = RequestMethod.GET)
    public ResponseData forgetPassword(String email){
        ResponseData responseData = null;
        try{
            User user = userService.getUserByEmail(email);
            userService.sendEmail(javaMailSender,user.getUserUsername(),user.getUserEmail(),"reset",freemarkerConfig);
            responseData = ResponseData.ok();
        }catch (UserNotFoundException e){
            responseData = ResponseData.badRequest();
            responseData.setMessage("邮箱不存在");
        }
        return responseData;
    }

    @RequestMapping(value = "mail/reset",method = RequestMethod.POST)
    public String reset(){
        return "forward:/reset";
    }

    @ResponseBody
    @RequestMapping(value = "mail/resetPassword",method = RequestMethod.POST)
    public ResponseData resetPassword(String username, String code, String newPsw){
        ResponseData responseData = null;
        try{
            User user = userService.getUserByUsername(username);
            if(code.equals(user.hashCode()+"")){
                userService.resetPassword(user,newPsw);
                responseData = ResponseData.ok();
            }else{
                responseData = ResponseData.badRequest();
                responseData.setMessage("无效链接");
            }
        }catch (UserNotFoundException e){
            responseData = ResponseData.badRequest();
            responseData.setMessage("无效链接");
        }
        return responseData;
    }

    /**
     * 返回的视图有点问题
     * @param user 用户
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "getPrfs" ,method = RequestMethod.GET)
    public ResponseData getPreferences(User user){
        ResponseData responseData = ResponseData.ok();
        responseData.putDataValue("prefers",preferenceService.getPrfByUid(user.getUid()));
        return responseData;
    }

    /**
     * 移除用户的偏好
     * @param user 用户
     * @param prfs 偏好列表
     */
    @ResponseBody
    @RequestMapping(value = "removePrfs" ,method = RequestMethod.DELETE)
    public ResponseData removePrefers(User user, List<Preference> prfs){
        preferenceService.removePrefers(user.getUid(),prfs);
        return ResponseData.ok();
    }

    /**
     * 添加用户偏好
     * @param user 用户
     * @param prfs 偏好列表
     */
    @ResponseBody
    @RequestMapping(value = "addPrfs",method = RequestMethod.GET)
    public ResponseData addPrefers(User user, List<Preference> prfs){
        preferenceService.addPrefers(user.getUid(),prfs);
        return ResponseData.ok();
    }

    /**
     * 查看用户的收藏课程以及对应的进度
     * @param user 用户
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "getColCourses" ,method = RequestMethod.GET)
    public ResponseData getColCourses(User user ){
        ResponseData responseData = null;
        List<ColCourseView> colCourseViews = courseService.getColCourses(user.getUid());
        responseData = ResponseData.ok();
        responseData.putDataValue("colCourseView",colCourseViews);
        return responseData;
    }

    /**
     * 用户收藏课程
     * @param user
     * @param cid
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "colCourse",method = RequestMethod.POST)
    public ResponseData colCourse(User user, int cid){
        ResponseData responseData = null;
        if(courseService.colCourse(user.getUid(),cid)){
            responseData = ResponseData.ok();
        }else{
            responseData = ResponseData.badRequest();
            responseData.setMessage("收藏失败，请重试!");
        }
        return responseData;
    }

    /**
     * 用户清除自己的收藏课程
     * 同时会清除对应课程的学习进度
     * @param user
     * @param cid
     */
    @ResponseBody
    @RequestMapping(value = "removeColCourse",method = RequestMethod.DELETE)
    public ResponseData removeColCourse(User user, int cid){
        courseService.removeColCourse(user.getUid(),cid);
        return ResponseData.ok();
    }

    /**
     * 获得用户的消息列表
     * @param user  用户
     * @param type  类型（收信||写信）
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "getMessages",method = RequestMethod.GET)
    public ResponseData getMessages(User user, int type){
        ResponseData responseData = ResponseData.ok();
        responseData.putDataValue("myMessages",userService.getMessages(user.getUid(),type));
        return responseData;
    }

    @ResponseBody
    @RequestMapping(value = "readMessage",method = RequestMethod.GET)
    public ResponseData readMessage(int mid){
        userService.readMessage(mid);
        return ResponseData.ok();
    }

    @ResponseBody
    @RequestMapping(value = "removeMessage",method = RequestMethod.DELETE)
    public ResponseData removeMessage(int mid){
        userService.removeMessage(mid);
        return ResponseData.ok();
    }

    @ResponseBody
    @RequestMapping(value = "getBaseInfo",method = RequestMethod.GET)
    public ResponseData getBaseInfo(User user){
        ResponseData responseData = null;
        UserBaseInfoView userBaseInfoView = userService.getUserBaseInfo(user.getUid());
        responseData = ResponseData.ok();
        responseData.putDataValue("baseInfo",userBaseInfoView);
        return responseData;
    }

    @ResponseBody
    @RequestMapping(value = "getUploadRecords",method = RequestMethod.GET)
    public ResponseData getUploadRecords(User user, int page){
        ResponseData responseData = null;
        responseData = ResponseData.ok();
        responseData.putDataValue("fileUploadRecords",fileService.getFileUploadRecords(user,page));
        return responseData;
    }

    @ResponseBody
    @RequestMapping(value = "getDownloadRecords",method = RequestMethod.GET)
    public ResponseData getDownloadRecords(User user, int page){
        ResponseData responseData = null;
        responseData = ResponseData.ok();
        responseData.putDataValue("fileDownloadRecords",fileService.getFileDownloadRecords(user,page));
        return responseData;
    }

}
