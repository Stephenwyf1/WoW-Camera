package com.wyj.service;

import com.wyj.domain.Template;

public interface TemplateService {

    void saveTemplate(Template template);

    Template getTemplateByUUID(String uuid);

    void updateTemplate(Template template);

    Template getTemplateByNameAndUserId(String name, Long userId);
}
