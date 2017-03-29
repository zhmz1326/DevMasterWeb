package com.jumore.devmaster.controller;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jumore.devmaster.common.CodeMirrorModeContainer;
import com.jumore.devmaster.common.TreeIconClassContainer;
import com.jumore.devmaster.common.util.PathUtils;
import com.jumore.devmaster.common.util.SessionHelper;
import com.jumore.devmaster.entity.Project;
import com.jumore.dove.service.BaseService;

@Controller
@RequestMapping(value = "/code")
public class CodeController {

    @Autowired
    private BaseService baseService;
    
    @RequestMapping("/view")
    public ModelAndView index(Long projectId) {
        ModelAndView mv = new ModelAndView();
        Project projectPo = baseService.get(Project.class, projectId);
        mv.addObject("projectId", projectId);
        mv.addObject("projectName", projectPo.getName());
        mv.addObject("tplId", 0);
        mv.addObject("readonly", true);
        return mv;
    }
    
    @ResponseBody
    @RequestMapping("/getFiles")
    public JSONArray getFiles(String id , Long projectId) {
        Project projectPo = baseService.get(Project.class, projectId);
        String path = id;
        String codePath = SessionHelper.getCodeGenerateDir(projectPo.getName());
        if(StringUtils.isEmpty(path)){
            path = codePath;
        }else{
            path = codePath+path;
        }
        JSONArray result = new JSONArray();
        File[] files = new File(path).listFiles();

        for (File file : files) {
            JSONObject obj = new JSONObject();
            obj.put("text", file.getName());
            obj.put("folder", file.isDirectory());
            obj.put("id", file.getAbsolutePath().replace(codePath, ""));
            obj.put("iconCls", TreeIconClassContainer.getIconClass(file));

            if (file.isDirectory()) {
                obj.put("state", "closed");
            } else {
                obj.put("state", "open");
            }

            result.add(obj);
        }
        return result;
    }
    
    @RequestMapping("/viewFile")
    public ModelAndView viewFile(String path , String projectName) throws IOException {
        ModelAndView mv = new ModelAndView();
        String filePath = PathUtils.getGeneratedCodeAbsolutePath(projectName, path);
        File file = new File(filePath);
        String content = FileUtils.readFileToString(file);
        
        if(!StringUtils.isEmpty(content)){
            content = content.replaceAll("</textarea>", "&lt;/textarea&gt;");
        }
        
        mv.addObject("content", content);
        mv.addObject("path", path);
        mv.addObject("fileName", file.getName());
        mv.addObject("mode", CodeMirrorModeContainer.get(file));
        mv.setViewName("project/codemirror");
        return mv;
    }
}