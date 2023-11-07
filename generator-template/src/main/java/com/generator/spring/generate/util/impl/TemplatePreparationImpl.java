package com.generator.spring.generate.util.impl;

import com.generator.spring.generate.template.ColumnInformation;
import com.generator.spring.generate.template.TableInformation;
import com.generator.spring.generate.template.TemplateProperties;
import com.generator.spring.generate.template.seed.TableDescription;
import com.generator.spring.generate.util.GenerateDDL;
import com.generator.spring.generate.util.GenerateEntity;
import com.generator.spring.generate.util.TemplatePreparation;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class TemplatePreparationImpl implements TemplatePreparation {

    @Autowired
    protected DataSource dataSource;
    @Autowired
    private GenerateEntity generateEntity;
    @Autowired
    private GenerateDDL generateDDL;

    @Value("${gen.spring.path.resources}")
    private String resourcePath;
    @Value("${gen.spring.path.project}")
    private String projectPath;
    @Value("${gen.spring.path.module}")
    private String moduleName;

    @Value("${gen.spring.package.model}")
    private String modelPackageParent;
    @Value("${gen.spring.package.entity}")
    private String entityPackage;
    @Value("${gen.spring.package.dto-request}")
    private String dtoRequestPackage;
    @Value("${gen.spring.package.dto-response}")
    private String dtoResponsePackage;
    @Value("${gen.spring.package.repository}")
    private String repositoryPackage;
    @Value("${gen.spring.package.service}")
    private String servicePackage;
    @Value("${gen.spring.package.service-impl}")
    private String serviceImplPackage;
    @Value("${gen.spring.package.controller}")
    private String controllerPackage;

    @Value("${gen.spring.name.template.entity}")
    private String entityTempName;
    @Value("${gen.spring.name.template.dto-request}")
    private String dtoRequestTempName;
    @Value("${gen.spring.name.template.dto-response}")
    private String dtoResponseTempName;
    @Value("${gen.spring.name.template.repository}")
    private String repositoryTempName;
    @Value("${gen.spring.name.template.service}")
    private String serviceTempName;
    @Value("${gen.spring.name.template.service-impl}")
    private String serviceImplTempName;
    @Value("${gen.spring.name.template.controller}")
    private String controllerTempName;
    @Value("${gen.spring.name.template.flyway-ddl}")
    private String flywayDDLTempName;

    @Value("${gen.spring.path.flyway-ddl}")
    private String flywayDllPath;

    @Override
    public void init(Velocity velocity, VelocityEngine velocityEngine) {
        Properties props = new Properties();
        props.setProperty("resource.loader", "file, class");
        props.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        props.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
        props.setProperty("file.resource.loader.path", resourcePath);
        velocity.init(props);
        velocityEngine.init(props);
    }

    @Override
    public TemplateProperties generateTemplatePropertiesEntity(TableDescription tableDescription) {
        return generateTemplateProperties(entityTempName, entityPackage, tableDescription.getFileName(), generateContex(tableDescription.getMapContex()));
    }

    @Override
    public List<TemplateProperties> generateTemplatePropertiesDTO(String className, String tableName) {
        List<TemplateProperties> list = new ArrayList<>();
        List listAttr = null;
        try {
            String entityName = className + "Entity";
            listAttr = generateAttrDTOByEntity(pathJoinPackage(entityPackage, entityName), entityName);
        } catch (IOException e) {
            log.error("generateAttrDTOByEntity");
//            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            log.error("generateAttrDTOByEntity");
//            e.printStackTrace();
        }

        List listImportParent = new ArrayList();
        listImportParent.add(entityPackage + "." + className + "Entity");

        Map mapRequest = new HashMap();
        mapRequest.put("package", dtoRequestPackage);
        mapRequest.put("import_parent", listImportParent);
        mapRequest.put("class_name", className);
        mapRequest.put("data_list", listAttr);

        Map mapResponse = new HashMap();
        mapResponse.put("package", dtoResponsePackage);
        mapResponse.put("import_parent", listImportParent);
        mapResponse.put("class_name", className);
        mapResponse.put("data_list", listAttr);

        String fileResponseGenerated = className + "Response";
        String fileRequestGenerated = className + "Request";
        list.add(generateTemplateProperties(dtoRequestTempName, dtoRequestPackage, fileRequestGenerated, generateContex(mapRequest)));
        list.add(generateTemplateProperties(dtoResponseTempName, dtoResponsePackage, fileResponseGenerated, generateContex(mapResponse)));
        return list;
    }

    @Override
    public TemplateProperties generateTemplatePropertiesRepository(String nameClass) {
        String fileGenerated = nameClass + "Repository";

        List listImportParent = new ArrayList();
        listImportParent.add(entityPackage + "." + nameClass + "Entity");

        Map mapContex = new HashMap();
        mapContex.put("package", repositoryPackage);
        mapContex.put("import_parent", listImportParent);
        mapContex.put("class_name", nameClass);

        return generateTemplateProperties(repositoryTempName, repositoryPackage, fileGenerated, generateContex(mapContex));
    }

    @Override
    public TemplateProperties generateTemplatePropertiesService(String nameClass) {
        String fileGenerated = nameClass + "Service";

//        List listImportParent = new ArrayList();
//        listImportParent.add(dtoResponsePackage + "." + nameClass + "Response");
//        listImportParent.add(dtoRequestPackage + "." + nameClass + "Request");
//        setStaticListImportService(listImportParent);
//
        List listImportParent = new ArrayList();
        listImportParent.add(entityPackage + "." + nameClass + "Entity");
        listImportParent.add(repositoryPackage + "." + nameClass + "Repository");
        listImportParent.add(dtoRequestPackage + "." + nameClass + "Request");
        listImportParent.add(dtoResponsePackage + "." + nameClass + "Response");
        setStaticListImportServiceImpl(listImportParent);

        Map mapContex = new HashMap();
        mapContex.put("package", servicePackage);
        mapContex.put("import_parent", listImportParent);
        mapContex.put("class_name", nameClass);

        return generateTemplateProperties(serviceTempName, servicePackage, fileGenerated, generateContex(mapContex));
    }

    @Override
    public TemplateProperties generateTemplatePropertiesServiceImpl(String nameClass) {
        String fileGenerated = nameClass + "ServiceImpl";

        List listImportParent = new ArrayList();
        listImportParent.add(entityPackage + "." + nameClass + "Entity");
        listImportParent.add(repositoryPackage + "." + nameClass + "Repository");
        listImportParent.add(dtoRequestPackage + "." + nameClass + "Request");
        listImportParent.add(dtoResponsePackage + "." + nameClass + "Response");
        listImportParent.add(servicePackage + "." + nameClass + "Service");
        setStaticListImportServiceImpl(listImportParent);

        Map mapContex = new HashMap();
        mapContex.put("package", serviceImplPackage);
        mapContex.put("import_parent", listImportParent);
        mapContex.put("class_name", nameClass);

        return generateTemplateProperties(serviceImplTempName, serviceImplPackage, fileGenerated, generateContex(mapContex));
    }

    @Override
    public TemplateProperties generateTemplatePropertiesController(String nameClass) {
        String fileGenerated = nameClass + "Controller";

        List listImportParent = new ArrayList();
        listImportParent.add(dtoRequestPackage + "." + nameClass + "Request");
        listImportParent.add(servicePackage + "." + nameClass + "Service");
        setStaticListImportController(listImportParent);

        Map mapContex = new HashMap();
        mapContex.put("package", controllerPackage);
        mapContex.put("import_parent", listImportParent);
        mapContex.put("class_name", nameClass);
        mapContex.put("path_api", "/api/v1/" + nameClass.replaceAll("(.)([A-Z])", "$1_$2").toLowerCase());

        return generateTemplateProperties(controllerTempName, controllerPackage, fileGenerated, generateContex(mapContex));
    }

    @Override
    public TemplateProperties generateTemplatePropertiesDDL(List<TableDescription> listTable) {
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy_HHmm");
        String fileGenerated = "V1" + "_" + sdf.format(new Date()) + "_" + "ddl";

        Map mapContex = new HashMap();
        mapContex.put("list_data", generateDDL.generate(listTable));

        return generateTemplateProperties(flywayDDLTempName, null, fileGenerated, true, generateContex(mapContex));
    }

    @Override
    public TemplateProperties generateTemplatePropertiesDDL(TableDescription tableDescription) {
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy_HHmm");
        String fileGenerated = "V1" + "_" + sdf.format(new Date()) + "_" + "create" + "_" + tableDescription.getTableName() + "_" + "ddl";

        Map mapContex = new HashMap();
        mapContex.put("list_data", generateDDL.generate(tableDescription));

        return generateTemplateProperties(flywayDDLTempName, null, fileGenerated, true, generateContex(mapContex));
    }

    public TemplateProperties generateTemplateProperties(String templatePath, String packageName, String classNameGenerated, VelocityContext contex) {
        return generateTemplateProperties(templatePath, packageName, classNameGenerated, false, contex);
    }

    public TemplateProperties generateTemplateProperties(String templatePath, String packageName, String classNameGenerated, boolean isDDL, VelocityContext contex) {
        String projectRoot = Objects.nonNull(projectPath) ? projectPath : System.getProperty("user.dir");
        String javaSourceFolder = moduleName + "src/main/java";
        String packagePath = Objects.nonNull(packageName) ? packageName.replace(".", "/") : "";
        classNameGenerated += ".java";

        String folderPath = projectRoot + "/" +
                javaSourceFolder + "/" +
                packagePath + "/";
        String filePath = projectRoot + "/" +
                javaSourceFolder + "/" +
                packagePath + "/" +
                classNameGenerated;

        if (isDDL) {
            folderPath = projectRoot + "/" +
                    "src/main/resources/" +
                    flywayDllPath;
            filePath = projectRoot + "/" +
                    "src/main/resources/" +
                    flywayDllPath +
                    classNameGenerated.replace(".java", ".sql");
        }

        return TemplateProperties.builder()
                .projectPath(projectPath)
                .packageName(packageName)
                .packagePath(packagePath)
                .templatePath(templatePath)
                .folderPath(folderPath)
                .filePath(filePath)
                .className(classNameGenerated)
//                .isCreateDrop(true)
                .contex(contex)
                .build();
    }

//    @Transactional(readOnly = true)
//    public TableInformation tableInformation(String catalog, String schema, String nameTable) {
//        DatabaseMetaData metaData = null;
//        TableInformation tableInformation = null;
//        try {
//            metaData = dataSource.getConnection().getMetaData();
//            ResultSet tables = metaData.getTables(catalog, schema, nameTable, new String[]{"TABLE"});
//            while (tables.next()) {
//                String tableName = tables.getString("TABLE_NAME");
//                ResultSet columns = metaData.getColumns(null, null, tableName, "%");
//
//                List<ColumnInformation> listColumn = new ArrayList<>();
//                while (columns.next()) {
//                    String columnName = columns.getString("COLUMN_NAME");
//                    String columnType = columns.getString("TYPE_NAME");
//
//                    listColumn.add(ColumnInformation.builder()
//                            .columnName(columnName)
//                            .columnType(columnType)
//                            .build());
//                }
//                tableInformation = TableInformation.builder()
//                        .tableName(tableName)
//                        .columns(listColumn)
//                        .build();
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return tableInformation;
//    }

    public VelocityContext generateContex(Map<String, Object> map) {
//        log.info("--- generateContex");
        if (map == null) {
            throw new RuntimeException("Map Kosong !!!");
        }

        setStaticContex(map); // static value

        VelocityContext context = new VelocityContext();
        for (Map.Entry<String, Object> param : map.entrySet()) {
            String key = param.getKey();
            context.put(key, param.getValue());
        }
//        log.info("--- generateContex");
        return context;
    }

//    public List generateAttrDTOByDB(TableInformation tableInformation) {
//        List listAttr = new ArrayList();
//        if (tableInformation.getColumns() == null) {
//            log.error("LIST COLUMN NULL");
//            throw new RuntimeException("LIST COLUMN NULL");
//        }
//
//        for (ColumnInformation ci : tableInformation.getColumns()) {
//            StringBuffer attr = new StringBuffer();
//            attr.append("private");
//            attr.append(" ");
//            switch (ci.getColumnType().toLowerCase()) {
//                case "bigserial":
//                    attr.append("Long");
//                    break;
//                case "varchar":
//                    attr.append("String");
//                    break;
//                case "int":
//                    attr.append("Integer");
//                    break;
//                case "date":
//                    attr.append("Date");
//                    break;
//                default:
//                    attr.append("String");
//                    break;
//            }
//            attr.append(" ");
//            attr.append(replaceAttrName(ci.getColumnName()));
//            attr.append(" ");
//            attr.append(";");
//            listAttr.add(attr.toString());
//        }
//        return listAttr;
//    }

    public List generateAttrDTOByEntity(String fileName, String entityClassName) throws IOException, ClassNotFoundException {
        List listAttr = new ArrayList();
        File javaFile = new File(fileName);
        List<Map> fieldNames = getFieldNamesFromFile(javaFile, entityClassName);
        for (Map map : fieldNames) {
//            System.out.println(map);
            StringBuffer attr = new StringBuffer();
            attr.append("private");
            attr.append(" ");
            attr.append(replaceAttrName((String) map.get("type")));
            attr.append(" ");
            attr.append(replaceAttrName((String) map.get("name")));
            attr.append(";");
            listAttr.add(attr.toString());
        }
        return listAttr;
    }

    public List<Map> getFieldNamesFromFile(File javaFile, String entityClassName) throws IOException, ClassNotFoundException {
        List<Map> fieldNames = new ArrayList<>();
        String fileContent = new String(Files.readAllBytes(javaFile.toPath()));

        // Cari kelas entity dalam file
        String regex = "class\\s+" + entityClassName + "\\s*\\{([^}]+)\\}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(fileContent);

        if (matcher.find()) {
            String entityClassContent = matcher.group(1);
            regex = "\\s+(\\w+)\\s+(\\w+);"; // Menangkap deklarasi field
            pattern = Pattern.compile(regex);
            matcher = pattern.matcher(entityClassContent);

            while (matcher.find()) {
                String fieldType = matcher.group(1);
                String fieldName = matcher.group(2);
                Map map = new HashMap();
                map.put("name", fieldName);
                map.put("type", fieldType);
                fieldNames.add(map);
            }
        }

        return fieldNames;
    }

    public String replaceAttrName(String input) {
        Pattern pattern = Pattern.compile("_(.)");
        Matcher matcher = pattern.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public void setStaticContex(Map map) {
        map.put("app_size", "\"${app.page-size.category}\"");
    }

    public void setStaticListImportController(List list) {
//        list.add(dtoRequestPackage + ".PageFilterRequest");
//        list.add(dtoResponsePackage + ".Response");
        list.add(dtoResponsePackage + ".BaseResponse");
    }

    public void setStaticListImportServiceImpl(List list) {
//        list.add("com.test.generate.exception.CommonApiException");
//        list.add(dtoRequestPackage + ".PageFilterRequest");
        list.add(dtoResponsePackage + ".BaseResponse");
        list.add("com.fincoreplus.baseservice.exception.CommonApiException");
    }

    public void setStaticListImportService(List list) {
//        list.add(dtoRequestPackage + ".PageFilterRequest");
        list.add(dtoResponsePackage + ".BaseResponse");
    }

    public String pathJoinPackage(String packageName, String classNameGenerated) {
        String projectRoot = Objects.nonNull(projectPath) ? projectPath : System.getProperty("user.dir");
        String javaSourceFolder = moduleName + "src/main/java";
        String packagePath = packageName.replace(".", "/");
        classNameGenerated += ".java";

        return projectRoot + "/" +
                javaSourceFolder + "/" +
                packagePath + "/" +
                classNameGenerated;
    }
}
