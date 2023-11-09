package com.generator.spring.generate.util.impl;

import com.generator.spring.generate.template.TemplateProperties;
import com.generator.spring.generate.util.GenerateTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GenerateTemplateImpl implements GenerateTemplate {


    @Autowired
    protected DataSource dataSource;

    @Override
    public void generates(List<TemplateProperties> listTemplateProperties) {
        if (listTemplateProperties == null) {
            log.error("LIST NULL");
            throw new RuntimeException("LIST NULL");
        }

        for (TemplateProperties templateProperties : listTemplateProperties) {
            if (templateProperties == null) {
                log.error("Template Properties NULL");
//                throw new RuntimeException("Template Properties NULL");
            }

            File fileClass = getFileGenerate(templateProperties);
            if (fileClass != null) {
                Writer writer = null;
                try {
                    writer = new FileWriter(fileClass);
                    Velocity.mergeTemplate(templateProperties.getTemplatePath(), "UTF-8", templateProperties.getContex(), writer);
                    writer.flush();
                    writer.close();
                    logGenerate(templateProperties, "SUCESS");
                } catch (IOException e) {
                    logGenerate(templateProperties, "ERROR");
                    log.error(e.getMessage());
                    e.printStackTrace();
                }
            } else {
                log.error("FILE NULL");
            }
        }
    }

    @Override
    public void generate(TemplateProperties templateProperties) {
        if (templateProperties == null) {
            log.error("TEMPLATE PROPERTIES NULL");
//            throw new RuntimeException("TEMPLATE PROPERTIES NULL");
        } else {
            File fileClass = getFileGenerate(templateProperties);
            if (fileClass != null) {
                Writer writer = null;
                try {
                    writer = new FileWriter(fileClass);
                    Velocity.mergeTemplate(templateProperties.getTemplatePath(), "UTF-8", templateProperties.getContex(), writer);
                    writer.flush();
                    writer.close();
                    logGenerate(templateProperties, "SUCESS");
                } catch (IOException e) {
                    logGenerate(templateProperties, "ERROR");
                    log.error(e.getMessage());
                    e.printStackTrace();
                }
            } else {
                log.error("FILE NULL");
            }
        }
    }

    public File getFileGenerate(TemplateProperties templateProperties) {
        File fileClass = null;
        if (templateProperties.getFolderPath() != null && templateProperties.getFilePath() != null) {
            File folder = new File(templateProperties.getFolderPath());
            fileClass = new File(templateProperties.getFilePath());

            if (folder == null || !folder.exists()) {
                log.error("FOLDER {}", folder.getAbsoluteFile());
                log.error("Folder Not Found");
//                throw new RuntimeException("Folder Not Found");
            }

            int lastIndex = templateProperties.getFilePath().lastIndexOf("/");
            String folderClassPath = templateProperties.getFilePath().substring(0, lastIndex + 1);
            File folderClass = new File(folderClassPath);

            if(folderClass != null && !folderClass.exists()){
                folderClass.mkdir();
                log.trace("FOLDER CLASS CREATE {}", folderClass.getAbsoluteFile());
            }

//            if (templateProperties.isCreateDrop()) {
//                fileClass.delete();
//            }
        }
        return fileClass;
    }

    public void logGenerate(TemplateProperties templateProperties, String status) {
//        log.info("\n");
        log.info("GENERATED >>>>>>>>>>>");
        log.info(status);
        log.info(templateProperties.getProjectPath());
        log.info(templateProperties.getFolderPath());
        log.info(templateProperties.getFilePath());
        log.info(templateProperties.getTemplatePath());
        log.info(templateProperties.getPackageName());
        log.info(templateProperties.getClassName());
        log.info("<<<<<<<<<<< GENERATED");
        log.info("=============================================");

    }
}
