package com.generator.spring.generate.util;

import com.generator.spring.generate.template.TableInformation;
import com.generator.spring.generate.template.TemplateProperties;
import com.generator.spring.generate.template.seed.ColumnDescription;
import com.generator.spring.generate.template.seed.TableDescription;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;

import java.util.List;
import java.util.Map;

public interface TemplatePreparation {

    public void init(Velocity velocity, VelocityEngine velocityEngine);

    public TemplateProperties generateTemplatePropertiesEntity(TableDescription tableDescription);

    public TemplateProperties generateTemplatePropertiesEntityEmbededId(TableDescription tableDescription);

    public List<TemplateProperties> generateTemplatePropertiesDTO(String nameClass, String tableName);

    public TemplateProperties generateTemplatePropertiesRepository(String nameClass, List<ColumnDescription> columnDescriptions);

    public TemplateProperties generateTemplatePropertiesService(String nameClass, List<ColumnDescription> columnDescriptions);

    public TemplateProperties generateTemplatePropertiesServiceImpl(String nameClass);

    public TemplateProperties generateTemplatePropertiesController(String nameClass, List<ColumnDescription> columnDescriptions);

    public TemplateProperties generateTemplatePropertiesDDL(List<TableDescription> listTable);

    public TemplateProperties generateTemplatePropertiesDDL(TableDescription tableDescription);
}
