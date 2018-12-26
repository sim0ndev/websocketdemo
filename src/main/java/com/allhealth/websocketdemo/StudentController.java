package com.allhealth.websocketdemo;/**
 * @Auther: Administrator
 * @Date: 2018/12/24 16:44
 * @Description:
 */

/**
 * ClassName:StudentController
 * Date:     2017年11月6日 下午4:27:40
 * @author   Joe
 * @version
 * @since    JDK 1.8
 */

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Controller
public class StudentController {

    /**
     * view:(跳转到JSP界面).
     * @author Joe
     * Date:2017年11月6日下午4:29:27
     *
     * @param map
     * @return
     */
    @RequestMapping(value = {"/", "/view"})
    public String view(Map<String, Object> map) {
        map.put("name", "SpringBoot");
        map.put("date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        return "index";
    }
}