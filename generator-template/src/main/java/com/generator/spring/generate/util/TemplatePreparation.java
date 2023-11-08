package com.generator.spring.generate.util;

import com.generator.spring.generate.template.TemplateProperties;
import com.generator.spring.generate.template.seed.ColumnDescription;
import com.generator.spring.generate.template.seed.TableDescription;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;

import java.util.List;

public interface TemplatePreparation {

    public void init(Velocity velocity, VelocityEngine velocityEngine);

    public TemplateProperties generateTemplatePropertiesEntity(TableDescription tableDescription);

    public TemplateProperties generateTemplatePropertiesEntityEmbededId(TableDescription tableDescription);

    public List<TemplateProperties> generateTemplatePropertiesDTO(String nameClass, String tableName, TableDescription tableDescription);

    //    public TemplateProperties generateTemplatePropertiesRepository(String nameClass, List<ColumnDescription> columnDescriptions);
    public TemplateProperties generateTemplatePropertiesRepository(String nameClass, TableDescription tableDescription);

    public TemplateProperties generateTemplatePropertiesService(String nameClass, TableDescription tableDescription);

    public TemplateProperties generateTemplatePropertiesServiceImpl(String nameClass);

    public TemplateProperties generateTemplatePropertiesController(String nameClass, TableDescription tableDescription);

    public TemplateProperties generateTemplatePropertiesDDL(List<TableDescription> listTable);

    public TemplateProperties generateTemplatePropertiesDDL(TableDescription tableDescription);
}
