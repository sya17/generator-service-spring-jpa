package com.generator.spring.generate.util;

import com.generator.spring.generate.template.TemplateProperties;
import org.apache.velocity.VelocityContext;

import java.util.List;
import java.util.Map;

public interface GenerateTemplate {
    public void generates(List<TemplateProperties> listTemplateProperties);
    public void generate(TemplateProperties templateProperties);
}
