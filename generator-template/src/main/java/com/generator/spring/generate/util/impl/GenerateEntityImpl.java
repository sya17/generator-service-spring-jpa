package com.generator.spring.generate.util.impl;

import com.generator.spring.generate.template.seed.ColumnDescription;
import com.generator.spring.generate.template.seed.TableDescription;
import com.generator.spring.generate.util.GenerateEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

@Slf4j
@Service
public class GenerateEntityImpl implements GenerateEntity {

    private final ResourceLoader resourceLoader;

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

    public GenerateEntityImpl(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Value("${gen.spring.seed.name}")
    public String nameSeed;
    @Value("${gen.spring.seed.path}")
    public String pathSeed;

    @Override
    public List<TableDescription> generate() {
        List<TableDescription> tableDescriptions = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(getSeed()))) {
            String line;
            TableDescription currentTableDescription = null;

            while ((line = br.readLine()) != null) {
                if (line.startsWith("#TABLE")) {
                    currentTableDescription = new TableDescription();
                    currentTableDescription.setTableName(line.trim().replace("#TABLE", "").trim());
                    tableDescriptions.add(currentTableDescription);
                    continue;
                }

                if (currentTableDescription != null) {
                    if (line.startsWith("#PK")) {
                        String[] listPk = line.trim().replace("#PK", "").split(",");
                        if (listPk.length > 0) {

                            Map<String, ColumnDescription> mapPk = new HashMap<>();
                            for (String pk : listPk) {
                                mapPk.put(pk.trim(), null);
                            }
                            currentTableDescription.setMapPK(mapPk);
                        }
                        continue;
                    }

                    String[] parts = line.split(",");
                    if (parts.length == 8) {
                        ColumnDescription columnDescription = new ColumnDescription(
                                parts[0].trim(),
                                parts[1].trim(),
                                parts[2].trim().toUpperCase().equals("Y"),
                                parts[3].trim().toUpperCase().equals("Y"),
                                parts[4].trim().toUpperCase().equals("Y"),
                                parts[5].trim().toUpperCase().equals("Y"),
                                parts[6].trim(),
                                parts[7].trim().toUpperCase().equals("Y")
                        );
                        if (currentTableDescription.getMapPK().containsKey(parts[0].trim())) {
                            Map<String, ColumnDescription> mapPk = currentTableDescription.getMapPK();
                            mapPk.put(parts[0].trim(), columnDescription);
                            currentTableDescription.setMapPK(mapPk);
                        }
                        currentTableDescription.addColumnDescription(columnDescription);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        setContexEntity(tableDescriptions);
        return tableDescriptions;
    }

    public void setContexEntity(List<TableDescription> listTD) {
        if (listTD == null) {
            log.error("LIST TABLE NULL");
            throw new RuntimeException("LIST TABLE NULL");
        }
        for (TableDescription tableDescription : listTD) {
            boolean pkIsOne = tableDescription.getMapPK() == null || tableDescription.getMapPK().size() == 0;
            String className = setNameClass(tableDescription.getTableName());
            StringBuffer pkMultiple = new StringBuffer();

            tableDescription.setFileName(className + "Entity");
            tableDescription.setFileNameEmbededId(className + "Id");
            tableDescription.setClassName(className);
            tableDescription.setMapContexEntityEmbededId(!pkIsOne ? getMapContexEntityEmbededId(tableDescription, className, pkMultiple) : null);
            tableDescription.setMapContex(getMapContexEntity(tableDescription, className, pkIsOne, pkMultiple));
        }
    }

    public Map getMapContexEntity(TableDescription tableDescription, String className, boolean pkIsOne, StringBuffer pkMultiple) {
        Map mapEntityContex = new HashMap();
        List listColumnContex = new ArrayList();
        listColumnContex.add(pkMultiple);
        for (ColumnDescription columnDescription : tableDescription.getColumnDescriptions()) {
            String columnContex = setColumnContex(tableDescription, columnDescription, pkIsOne);
            if (columnContex != null) listColumnContex.add(columnContex);
        }
        mapEntityContex.put("package", getNamePackeMultiple(tableDescription, entityPackage));
        mapEntityContex.put("import_parent", getListImportEntity(className, tableDescription));
        mapEntityContex.put("table_name", tableDescription.getTableName());
        mapEntityContex.put("data_list", listColumnContex);
        mapEntityContex.put("class_name", className);
        return mapEntityContex;
    }

    public Map getMapContexEntityEmbededId(TableDescription tableDescription, String className, StringBuffer pkMultiple) {
        Map mapEntityEmbededIdContex = new HashMap();
        List listColumnContex = new ArrayList();
        for (Map.Entry<String, ColumnDescription> map : tableDescription.getMapPK().entrySet()) {
            StringBuffer sb = new StringBuffer();
            setAttrEntityEmbeded(sb, map.getValue());
            if (sb != null && sb.toString() != null) listColumnContex.add(sb);
        }
        mapEntityEmbededIdContex.put("package", getNamePackeMultiple(tableDescription, entityPackage));
        mapEntityEmbededIdContex.put("import_parent", getListImportEntity(className, tableDescription));
        mapEntityEmbededIdContex.put("table_name", tableDescription.getTableName());
        mapEntityEmbededIdContex.put("data_list", listColumnContex);
        mapEntityEmbededIdContex.put("class_name", className);
        setAttrEntityEmbededId(pkMultiple, className);
        return mapEntityEmbededIdContex;
    }

    public String setColumnContex(TableDescription tableDescription, ColumnDescription columnDescription, boolean pkIsOne) {
        if (columnDescription == null) {
            log.error("COLUMN DESC NULL");
        }
        StringBuffer sb = new StringBuffer();
        if (!tableDescription.getMapPK().containsKey(columnDescription.getColumnName())) {
            if (columnDescription.isPrimaryKey()) {
                setIdPkContex(sb, columnDescription);
            } else {
                setAttrEntityContex(sb, columnDescription);
            }
        }
        return sb.toString();
    }

    public void setIdPkContex(StringBuffer sb, ColumnDescription columnDescription) {
        switch (columnDescription.getDataType().toLowerCase()) {
            case "uuid":
                sb.append(" ").append("@Id").append("\n");
                sb.append("     ").append("@GeneratedValue(generator = \"uuid2\")").append("\n");
                sb.append("     ").append("@GenericGenerator(name = \"uuid2\", strategy = \"org.hibernate.id.UUIDGenerator\")").append("\n");
                sb.append("     ").append("@Type(type = \"pg-uuid\")").append("\n");
                setAttrEntityContex(sb, columnDescription);
                break;
            case "varchar":
                sb.append(" ").append("@Id").append("\n");
                sb.append(" ").append("@GeneratedValue(strategy = GenerationType.IDENTITY)").append("\n");
                setAttrEntityContex(sb, columnDescription);
                break;
            case "serial4":
                sb.append(" ").append("@Id").append("\n");
                sb.append("     ").append("@GeneratedValue(strategy = GenerationType.IDENTITY)").append("\n");
                setAttrEntityContex(sb, columnDescription);
                break;
            default:
                sb.append("     ").append("@Id").append("\n");
                sb.append("     ").append("@GeneratedValue(strategy = GenerationType.IDENTITY)").append("\n");
                setAttrEntityContex(sb, columnDescription);
                break;
        }
    }

    public void setAttrEntityContex(StringBuffer sb, ColumnDescription columnDescription) {
        sb.append("     ").append("@Column(name = \"" + columnDescription.getColumnName() + "\" ")
                .append(columnDescription.toString())
                .append(")")
                .append("\n");
        sb.append("     ").append("private")
                .append(" ")
                .append(getTypeAttr(columnDescription.getDataType()))
                .append(" ")
                .append(setNameAttr(columnDescription.getColumnName()))
                .append(";");
    }

    public void setAttrEntityEmbeded(StringBuffer sb, ColumnDescription columnDescription) {
        sb.append("     ").append("@Column(name = \"" + columnDescription.getColumnName() + "\" ")
                .append(columnDescription.toString())
                .append(")")
                .append("\n");
        sb.append("     ").append("private")
                .append(" ")
                .append(getTypeAttr(columnDescription.getDataType()))
                .append(" ")
                .append(setNameAttr(columnDescription.getColumnName()))
                .append(";");
    }

    public void setAttrEntityEmbededId(StringBuffer sb, String className) {
        sb.append("     ").append("@EmbeddedId")
                .append("\n");
        sb.append("     ").append("private")
                .append(" ")
                .append(typeAttrEmbededId(className) + "Id")
                .append(" ")
                .append(nameAttrEmbededId(className))
                .append(";");
    }

    public String typeAttrEmbededId(String input) {
        return input.replace("Entity", "Id");
    }

    public String nameAttrEmbededId(String input) {
        input = input.replace("Entity", "Id");
        return input.substring(0, 1).toLowerCase() + input.substring(1);
    }


    public String getTypeAttr(String type) {
        switch (type.toLowerCase()) {
            case "uuid":
                return "UUID";
            case "bigserial":
                return "Long";
            case "int":
                return "Integer";
            case "int2":
                return "Integer";
            case "int4":
                return "Integer";
            case "serial4":
                return "Integer";
            case "date":
                return "Date";
            case "timestamptz":
                return "Date";
            default:
                return "String";
        }
    }

    public String setNameAttr(String input) {
        String[] words = input.split("_");
        StringBuilder result = new StringBuilder();
        int count = 0;
        for (String word : words) {
            if (count > 0) {
                result.append(Character.toUpperCase(word.charAt(0)));
                result.append(word.substring(1));
            } else {
                result.append(Character.toLowerCase(word.charAt(0)));
                result.append(word.substring(1));
            }
            count++;
        }
        return result.toString();
    }

    public String setNameClass(String input) {
        String[] words = input.split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)));
                result.append(word.substring(1));
            }
        }
        return result.toString();
    }

    public File getSeed() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:" + pathSeed + "/" + nameSeed);
        return resource.getFile();
    }

    public List getListImportEntity(String nameClass, TableDescription tableDescription) {
        List listImportParent = new ArrayList();
        listImportParent.add(dtoRequestPackage + "." + tableDescription.getClassName().toLowerCase() + "." + nameClass + "Request");
        return listImportParent;
    }

    public String getNamePackeMultiple(TableDescription tableDescription, String packageName){
        return packageName + "." + tableDescription.getClassName().toLowerCase();
    }
}
