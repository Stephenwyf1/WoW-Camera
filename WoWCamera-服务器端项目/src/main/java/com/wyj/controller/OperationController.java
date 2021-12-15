package com.wyj.controller;

import com.wyj.domain.Operation;
import com.wyj.service.OperationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@Slf4j
@RequestMapping("/op")
public class OperationController {

    @Autowired
    private OperationService operationService;

    /**
     * 保存用户操作
     * @param id 用户id
     * @param operation 用户操作
     * @return
     */
    @RequestMapping("/saveOperation/{id}/{name}/{operation}")
    @ResponseBody
    public Boolean saveOperation(@PathVariable("id") Long id,
                                 @PathVariable("name") String name,
                                 @PathVariable("operation") String operation){
        Boolean res = true;
        try {
            Operation op = new Operation();
//            System.out.println(operation);
//            System.out.println(name);
            op.setUserId(id);
            String[] ops = operation.split(" ");
            String userOperaion = "";
            for (String s : ops) {
                if (Integer.parseInt(s) < 510){
                    //510及以后为自定义滤镜
                    userOperaion += s + " ";
                }
            }
            op.setOperation(userOperaion);
            op.setName(name);
            operationService.saveOperation(op);
            log.info("保存配置成功！配置名为:"+op.getName());
        }catch (Exception e){
            e.printStackTrace();
            log.info("配置保存失败");
            res = false;
        }
        return res;
    }

    @RequestMapping("/getOperation/{id}")
    @ResponseBody
    public List<Operation> getOperation(@PathVariable("id") Long id){
        List<Operation> operations = null;
        operations = operationService.getOperationByUserId(id);
        log.info("用户"+id+"的全部配置如下："+operations);
        return operations;
    }

    @RequestMapping("/deleteOperation/{id}")
    @ResponseBody
    public Boolean deleteOperationById(@PathVariable("id")Long id){
        int res = operationService.delOperationById(id);
        if (res==0){
            log.info("删除失败！");
            return false;
        }
        log.info("删除成功！删除的Operation的id为"+id);
        return true;
    }

    @RequestMapping("/updateOperation/{id}/{name}")
    @ResponseBody
    public Boolean updateNameById(@PathVariable("id") Long id,
                                    @PathVariable("name") String name){
        int res = operationService.updateNameById(id, name);
        if (res==0){
            log.info("更新失败！");
            return false;
        }
        log.info("更新成功！更新的Operation的id为"+id+"，name更新为"+name);
        return true;
    }

}
