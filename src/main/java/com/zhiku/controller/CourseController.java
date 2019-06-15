/*
关于课程的所以请求
 */
package com.zhiku.controller;

import com.zhiku.entity.Course;
import com.zhiku.service.CourseService;
import com.zhiku.service.SectionService;
import com.zhiku.util.ResponseData;
import com.zhiku.view.CourseView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("course")
public class CourseController {
    @Autowired
    private CourseService courseService;
    @Autowired
    private SectionService sectionService;

    //TODO 获得全部课程，在讨论完学院专业课程的设计之后可以考虑修改这部分逻辑
    /**
     * 获得所有的课
     * 后期修改为可按照专业搜课
     * @return 课程列表
     */
    @ResponseBody
    @RequestMapping(value = "getAllCourse" ,method = RequestMethod.GET)
    public ResponseData getAllCourse(){
       ResponseData responseData = ResponseData.ok();
        List<Course> courses = courseService.getAllCourse();
        responseData.putDataValue("courses",courses);
        return responseData;
    }

    /**
     * 获得一个课程的目录结构
     * @param cid 课程id
     * @return 课程视图（包含了目录结构）
     */
    @ResponseBody
    @RequestMapping(value = "getCourseDetails",method = RequestMethod.GET)
    public ResponseData getCourseDetails(@RequestParam(value = "cid") int cid){
        ResponseData responseData = ResponseData.ok();
        CourseView courseView = courseService.getCourseDetails(cid);
        responseData.putDataValue("courseView",courseView);
        return responseData;
    }
}
