package com.generator.spring.generate.util.impl;

import com.generator.spring.generate.template.TemplateProperties;
import com.generator.spring.generate.template.seed.ColumnDescription;
import com.generator.spring.generate.template.seed.TableDescription;
import com.generator.spring.generate.util.GenerateDDL;
import com.generator.spring.generate.util.GenerateEntity;
import com.generator.spring.generate.util.TemplatePreparation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
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

    @Value("${gen.spring.package.base}")
    private String basePackage;

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
    @Value("${gen.spring.name.template.entity-id}")
    private String entityEmbededIdTempName;

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
        return generateTemplateProperties(entityTempName, getNamePackeMultiple(tableDescription, entityPackage), tableDescription.getClassName(), generateContex(tableDescription.getMapContex()));
    }

    @Override
    public TemplateProperties generateTemplatePropertiesEntityEmbededId(TableDescription tableDescription) {
        if (tableDescription.getMapContexEntityEmbededId() == null || tableDescription.getMapContexEntityEmbededId().size() == 0)
            return null;
        return generateTemplateProperties(entityEmbededIdTempName, getNamePackeMultiple(tableDescription, entityPackage), tableDescription.getFileNameEmbededId(), generateContex(tableDescription.getMapContexEntityEmbededId()));
    }

    @Override
    public List<TemplateProperties> generateTemplatePropertiesDTO(String className, String tableName, TableDescription tableDescription) {
        List<TemplateProperties> list = new ArrayList<>();
        List listAttr = null;
        try {
            String entityName = className ;
            listAttr = generateAttrDTOByEntity(pathJoinPackage(getNamePackeMultiple(tableDescription, entityPackage), entityName), entityName, tableDescription);
        } catch (IOException e) {
            log.error("generateAttrDTOByEntity");
//            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            log.error("generateAttrDTOByEntity");
//            e.printStackTrace();
        }

        List listImportParent = new ArrayList();
        listImportParent.add(getNamePackeMultiple(tableDescription, entityPackage) + "." + className );

        Map mapRequest = new HashMap();
        mapRequest.put("package", dtoRequestPackage + "." + tableDescription.getClassName().toLowerCase());
        mapRequest.put("import_parent", listImportParent);
        mapRequest.put("class_name", className);
        mapRequest.put("data_list", listAttr);

        Map mapResponse = new HashMap();
        mapResponse.put("package", dtoResponsePackage + "." + tableDescription.getClassName().toLowerCase());
        mapResponse.put("import_parent", listImportParent);
        mapResponse.put("class_name", className);
        mapResponse.put("data_list", listAttr);

        String fileResponseGenerated = className + "Response";
        String fileRequestGenerated = className + "Request";
        list.add(generateTemplateProperties(dtoRequestTempName, getNamePackeMultiple(tableDescription, dtoRequestPackage), fileRequestGenerated, generateContex(mapRequest)));
        list.add(generateTemplateProperties(dtoResponseTempName, getNamePackeMultiple(tableDescription, dtoResponsePackage), fileResponseGenerated, generateContex(mapResponse)));
        return list;
    }

    @Override
//    public TemplateProperties generateTemplatePropertiesRepository(String nameClass, List<ColumnDescription> columnDescriptions) {
    public TemplateProperties generateTemplatePropertiesRepository(String nameClass, TableDescription tableDescription) {
        String fileGenerated = nameClass + "Repository";
        Map mapContex = new HashMap();

        List listImportParent = new ArrayList();
        listImportParent.add(getNamePackeMultiple(tableDescription, entityPackage) + "." + nameClass );
        if (pkIsOne(tableDescription)) {
            setStaticContexByColumnDesc(mapContex, tableDescription.getColumnDescriptions());
        } else {
            listImportParent.add(getNamePackeMultiple(tableDescription, entityPackage) + "." + nameClass + "Id");
            mapContex.put("type_pk", tableDescription.getClassName() + "Id");
        }

        mapContex.put("package", repositoryPackage);
        mapContex.put("import_parent", listImportParent);
        mapContex.put("class_name", nameClass);

        return generateTemplateProperties(repositoryTempName, repositoryPackage, fileGenerated, generateContex(mapContex));
    }

    @Override
    public TemplateProperties generateTemplatePropertiesService(String nameClass, TableDescription tableDescription) {
        String fileGenerated = nameClass + "Service";

//        List listImportParent = new ArrayList();
//        listImportParent.add(dtoResponsePackage + "." + nameClass + "Response");
//        listImportParent.add(dtoRequestPackage + "." + nameClass + "Request");
//        setStaticListImportService(listImportParent);
//
        List listImportParent = new ArrayList();
        listImportParent.add(getNamePackeMultiple(tableDescription, entityPackage) + "." + nameClass );
        if (!pkIsOne(tableDescription))
            listImportParent.add(getNamePackeMultiple(tableDescription, entityPackage) + "." + nameClass + "Id");
        listImportParent.add(getNamePackeMultiple(tableDescription, dtoRequestPackage) + "." + nameClass + "Request");
        listImportParent.add(getNamePackeMultiple(tableDescription, dtoResponsePackage) + "." + nameClass + "Response");
        listImportParent.add(repositoryPackage + "." + nameClass + "Repository");
        setStaticListImportServiceImpl(listImportParent);

        Map mapContex = new HashMap();
        mapContex.put("package", servicePackage);
        mapContex.put("import_parent", listImportParent);
        mapContex.put("class_name", nameClass);
        setStaticContexByColumnDesc(mapContex, tableDescription.getColumnDescriptions());
        mapContex.put("list_param", getListParamService(tableDescription));
        mapContex.put("list_param_value", getListParamValueService(tableDescription));

        return generateTemplateProperties(serviceTempName, servicePackage, fileGenerated, generateContex(mapContex));
    }

    @Override
    public TemplateProperties generateTemplatePropertiesServiceImpl(String nameClass) {
        String fileGenerated = nameClass + "ServiceImpl";

        List listImportParent = new ArrayList();
        listImportParent.add(entityPackage + "." + nameClass );
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
    public TemplateProperties generateTemplatePropertiesController(String nameClass, TableDescription tableDescription) {
        String fileGenerated = nameClass + "Controller";

        List listImportParent = new ArrayList();
        listImportParent.add(getNamePackeMultiple(tableDescription, dtoRequestPackage) + "." + nameClass + "Request");
        listImportParent.add(servicePackage + "." + nameClass + "Service");
        listImportParent.add(getNamePackeMultiple(tableDescription, dtoResponsePackage) + "." + nameClass + "Response");
        setStaticListImportController(listImportParent);

        Map mapContex = new HashMap();
        mapContex.put("package", controllerPackage);
        mapContex.put("import_parent", listImportParent);
        mapContex.put("class_name", nameClass);
        mapContex.put("path_api", "/api/v1/" + nameClass.replaceAll("(.)([A-Z])", "$1_$2").toLowerCase());
        setStaticContexByColumnDesc(mapContex, tableDescription.getColumnDescriptions());
        mapContex.put("list_param", getListParamController(tableDescription));
        mapContex.put("list_param_value", getListParamValueController(tableDescription));
        mapContex.put("list_param_path", getListParamPathController(tableDescription));

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
        VelocityContext context = new VelocityContext();
        if (map == null) {
            log.error("Map Kosong !!!");
        } else {
            setStaticContex(map); // static value

            for (Map.Entry<String, Object> param : map.entrySet()) {
                String key = param.getKey();
                context.put(key, param.getValue());
            }
//        log.info("--- generateContex");
        }
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

    public List generateAttrDTOByEntity(String fileName, String entityClassName, TableDescription tableDescription) throws IOException, ClassNotFoundException {
        List listAttr = new ArrayList();
        File javaFile = new File(fileName);
        List<Map> fieldNames = getFieldNamesFromFile(javaFile, entityClassName);
        Map mapEmbededSet = new HashMap();
        for (Map map : fieldNames) {
            String nameEntityEmbededId = tableDescription.getClassName() + "Id";
            String type = (String) map.get("type");

            if (nameEntityEmbededId.equalsIgnoreCase(type)) {
                fileName = pathJoinPackage(getNamePackeMultiple(tableDescription, entityPackage), nameEntityEmbededId);
                javaFile = new File(fileName);
                fieldNames = getFieldNamesFromFile(javaFile, nameEntityEmbededId);
                for (Map map2 : fieldNames) {
                    StringBuffer attr = new StringBuffer();
                    if (mapEmbededSet.containsKey(map2.get("name"))) {
                        continue;
                    }
                    type = (String) map2.get("type");
                    attr.append("private");
                    attr.append(" ");
                    attr.append(type);
                    attr.append(" ");
                    attr.append(replaceAttrName((String) map2.get("name")));
                    attr.append(";");
                    listAttr.add(attr.toString());

                    mapEmbededSet.put(map2.get("name"), map2.get("name"));
                }
            } else {
                StringBuffer attr = new StringBuffer();
                attr.append("private");
                attr.append(" ");
                attr.append(type);
                attr.append(" ");
                attr.append(replaceAttrName((String) map.get("name")));
                attr.append(";");
                listAttr.add(attr.toString());
            }
        }
        return listAttr;
    }

    public List<Map> getFieldNamesFromFile(File javaFile, String entityClassName) throws IOException, ClassNotFoundException {
        List<Map> fieldNames = new ArrayList<>();
        String fileContent = new String(Files.readAllBytes(javaFile.toPath())).replace("implements Serializable", "");

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

    public void setStaticContexByColumnDesc(Map map, List<ColumnDescription> columnDescriptions) {
        for (ColumnDescription columnDescription : columnDescriptions) {
            if (columnDescription.isPrimaryKey()) {
                map.put("type_pk", generateEntity.getTypeAttr(columnDescription.getDataType()));
            }
        }
    }

    public void setStaticListImportController(List list) {
        list.add(dtoResponsePackage + ".BaseResponse");
        list.add("jakarta.validation.constraints.PositiveOrZero");
    }

    public void setStaticListImportServiceImpl(List list) {
        list.add(dtoResponsePackage + ".BaseResponse");
        list.add(basePackage + ".util.SpecificationUtil");
        list.add(basePackage + ".exception.CommonApiException");
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

    public boolean pkIsOne(TableDescription tableDescription) {
        return tableDescription.getMapPK() == null || tableDescription.getMapPK().size() == 0;
    }

    public List getListParamService(TableDescription tableDescription) {
        List listParam = new ArrayList();
        if (!pkIsOne(tableDescription)) {
            int count = 0;
            for (Map.Entry<String, ColumnDescription> map : tableDescription.getMapPK().entrySet()) {
                StringBuffer sb = new StringBuffer();
                ColumnDescription columnDescription = map.getValue();
                if (columnDescription != null) {
                    sb.append(generateEntity.getTypeAttr(columnDescription.getDataType()));
                    sb.append(" ");
                    sb.append(generateEntity.setNameAttr(columnDescription.getColumnName()));
                    if (count < tableDescription.getMapPK().size() - 1) sb.append(",");
                    count++;
                    listParam.add(sb.toString());
                }
            }
        } else {
            for (ColumnDescription columnDescription : tableDescription.getColumnDescriptions()) {
                if (columnDescription.isPrimaryKey()) {
                    StringBuffer sb = new StringBuffer();
                    sb.append(generateEntity.getTypeAttr(columnDescription.getDataType()));
                    sb.append(" ");
                    sb.append(generateEntity.setNameAttr(columnDescription.getColumnName()));
                    listParam.add(sb.toString());

                }
            }
        }
        return listParam;
    }

    public List getListParamValueService(TableDescription tableDescription) {
        List listParam = new ArrayList();
        if (!pkIsOne(tableDescription)) {
            int count = 0;
            for (Map.Entry<String, ColumnDescription> map : tableDescription.getMapPK().entrySet()) {
                StringBuffer sb = new StringBuffer();
                ColumnDescription columnDescription = map.getValue();
                if (columnDescription != null) {
                    sb.append(generateEntity.setNameAttr(columnDescription.getColumnName()));
                    if (count < tableDescription.getMapPK().size() - 1) sb.append(",");
                    count++;
                    listParam.add(sb.toString());
                }
            }
        } else {
            for (ColumnDescription columnDescription : tableDescription.getColumnDescriptions()) {
                if (columnDescription.isPrimaryKey()) {
                    StringBuffer sb = new StringBuffer();
                    sb.append(generateEntity.setNameAttr(columnDescription.getColumnName()));
                    listParam.add(sb.toString());

                }
            }
        }
        return listParam;
    }

    public List getListParamController(TableDescription tableDescription) {
        List listParam = new ArrayList();
        if (!pkIsOne(tableDescription)) {
            int count = 0;
            for (Map.Entry<String, ColumnDescription> map : tableDescription.getMapPK().entrySet()) {
                StringBuffer sb = new StringBuffer();
                ColumnDescription columnDescription = map.getValue();
                if (columnDescription != null) {
                    sb.append("@PathVariable(\"");
                    sb.append(columnDescription.getColumnName());
                    sb.append("\")");
                    sb.append(" ");
                    sb.append(generateEntity.getTypeAttr(columnDescription.getDataType()));
                    sb.append(" ");
                    sb.append(generateEntity.setNameAttr(columnDescription.getColumnName()));
                    if (count < tableDescription.getMapPK().size() - 1) sb.append(",");
                    count++;
                    listParam.add(sb.toString());
                }
            }
        } else {
            for (ColumnDescription columnDescription : tableDescription.getColumnDescriptions()) {
                if (columnDescription.isPrimaryKey()) {
                    StringBuffer sb = new StringBuffer();
                    sb.append("@PathVariable(\"");
                    sb.append(columnDescription.getColumnName());
                    sb.append("\")");
                    sb.append(" ");
                    sb.append(generateEntity.getTypeAttr(columnDescription.getDataType()));
                    sb.append(" ");
                    sb.append(generateEntity.setNameAttr(columnDescription.getColumnName()));
                    listParam.add(sb.toString());

                }
            }
        }
        return listParam;
    }

    public List getListParamValueController(TableDescription tableDescription) {
        List listParam = new ArrayList();
        if (!pkIsOne(tableDescription)) {
            int count = 0;
            for (Map.Entry<String, ColumnDescription> map : tableDescription.getMapPK().entrySet()) {
                StringBuffer sb = new StringBuffer();
                ColumnDescription columnDescription = map.getValue();
                if (columnDescription != null) {
                    sb.append(generateEntity.setNameAttr(columnDescription.getColumnName()));
                    if (count < tableDescription.getMapPK().size() - 1) sb.append(",");
                    count++;
                    listParam.add(sb.toString());
                }
            }
        } else {
            for (ColumnDescription columnDescription : tableDescription.getColumnDescriptions()) {
                if (columnDescription.isPrimaryKey()) {
                    StringBuffer sb = new StringBuffer();
                    sb.append(generateEntity.setNameAttr(columnDescription.getColumnName()));
                    listParam.add(sb.toString());

                }
            }
        }
        return listParam;
    }

    public List getListParamPathController(TableDescription tableDescription) {
        List listParam = new ArrayList();
        if (!pkIsOne(tableDescription)) {
            int count = 0;
            for (Map.Entry<String, ColumnDescription> map : tableDescription.getMapPK().entrySet()) {
                StringBuffer sb = new StringBuffer();
                ColumnDescription columnDescription = map.getValue();
                if (columnDescription != null) {
                    if (count > 0) sb.append(generateEntity.setNameAttr(columnDescription.getColumnName()));
                    sb.append("/");
                    sb.append("{");
                    sb.append(columnDescription.getColumnName());
                    sb.append("}");
                    if (count < tableDescription.getMapPK().size() - 1) sb.append("/");
                    count++;
                    listParam.add(sb.toString());
                }
            }
        } else {
            for (ColumnDescription columnDescription : tableDescription.getColumnDescriptions()) {
                if (columnDescription.isPrimaryKey()) {
                    StringBuffer sb = new StringBuffer();
                    sb.append("/");
                    sb.append("{");
                    sb.append(columnDescription.getColumnName());
                    sb.append("}");
                    listParam.add(sb.toString());

                }
            }
        }
        return listParam;
    }

    public String getNamePackeMultiple(TableDescription tableDescription, String packageName) {
        return packageName + "." + tableDescription.getClassName().toLowerCase();
    }
}
