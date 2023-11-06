package com.generator.spring.generate.template;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.velocity.VelocityContext;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateProperties {
    private String projectPath;
    private String packagePath;
    private String templatePath;
    private String packageName;
    private String folderPath;
    private String filePath;
    private String className;
//    private boolean isCreateDrop;

    private Map<String, Object> mapContex;
    private VelocityContext contex;
}
