package com.wyj.service.impl;

import com.wyj.domain.Template;
import com.wyj.mapper.TemplateMapper;
import com.wyj.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("templateService")
public class TemplateServiceImpl implements TemplateService {

    @Autowired
    private TemplateMapper templateMapper;

    @Override
    public void saveTemplate(Template template) {
        templateMapper.insertSelective(template);
    }

    @Override
    public Template getTemplateByUUID(String uuid) {
        Template template = new Template();
        template.setUuid(uuid);
        Template t = templateMapper.selectOne(template);
        return t;
    }

    @Override
    public void updateTemplate(Template template) {
        templateMapper.updateByPrimaryKeySelective(template);
    }

    @Override
    public Template getTemplateByNameAndUserId(String name, Long userId) {
        Template template = new Template();
        template.setName(name);
        template.setUserId(userId);
        Template t = templateMapper.selectOne(template);
        return t;
    }

}
