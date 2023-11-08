package com.generator.spring.generate.run;

import com.generator.spring.generate.template.TemplateProperties;
import com.generator.spring.generate.template.seed.TableDescription;
import com.generator.spring.generate.util.GenerateEntity;
import com.generator.spring.generate.util.GenerateTemplate;
import com.generator.spring.generate.util.TemplatePreparation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class generate implements CommandLineRunner {

    private final GenerateTemplate generateTemplate;
    private final TemplatePreparation templatePreparation;
    private final GenerateEntity generateEntity;


    @Override
    public void run(String... args) throws Exception {
        log.trace("#################### Start Generate");
        try {
            List<TableDescription> listTable = generateEntity.generate();

            Velocity velocity = new Velocity();
            VelocityEngine velocityEngine = new VelocityEngine();
            templatePreparation.init(velocity, velocityEngine);

            for (TableDescription tableDescription : listTable) {
                List<TemplateProperties> list = new ArrayList<>();
                String nameClass = tableDescription.getClassName();
                String nameTable = tableDescription.getTableName();
                log.trace("GENERATE CLASS {}", nameClass);
                log.trace("GENERATE TABLE SEED {}", nameTable);

                generateTemplate.generate(templatePreparation.generateTemplatePropertiesEntity(tableDescription));
                generateTemplate.generate(templatePreparation.generateTemplatePropertiesEntityEmbededId(tableDescription));
                generateTemplate.generates(templatePreparation.generateTemplatePropertiesDTO(nameClass, nameTable, tableDescription));
                generateTemplate.generate(templatePreparation.generateTemplatePropertiesRepository(nameClass, tableDescription));
                generateTemplate.generate(templatePreparation.generateTemplatePropertiesService(nameClass, tableDescription));
//                generateTemplate.generate(templatePreparation.generateTemplatePropertiesServiceImpl(nameClass));
                generateTemplate.generate(templatePreparation.generateTemplatePropertiesController(nameClass, tableDescription));
//                generateTemplate.generate(templatePreparation.generateTemplatePropertiesDDL(tableDescription));
            }
//            generateTemplate.generate(templatePreparation.generateTemplatePropertiesDDL(listTable));
            log.trace("#################### Success Generate");
        } catch (Exception e) {
            log.error("#################### Error Generate");
            throw new RuntimeException(e.getMessage());
        }
    }
}
