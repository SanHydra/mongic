package com.mindc.mongic.utils;



import com.mindc.mongic.exception.MongicException;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @Title: MongoGenerator.java
 * @Description: 格式化工具
 * @Author Hydra
 * @Date 2021/6/23 3:15 PM
 */
public class MongoGenerator {
    private String outputDir = "";
    private String basePackage;
    private String serviceDir = "service";
    private String serviceImplDir = "service.impl";
    private Class entity;
    private String entityName;
    private String entitySimpleName;
    private String author;

    private String servicePrefix = "";



    public static MongoGenerator create(String packageName,Class entityClass){
        MongoGenerator mongoGenerator = new MongoGenerator();
        mongoGenerator.basePackage = packageName;
        mongoGenerator.entity = entityClass;
        //获取实体的名称，作为import的内容
        mongoGenerator.entityName = entityClass.getName();
        mongoGenerator.entitySimpleName = entityClass.getSimpleName();
        return mongoGenerator;
    }

    public MongoGenerator outputDir(String dir){
        this.outputDir = dir;
        return this;
    }

    public MongoGenerator basePackage(String packageName){
        this.basePackage = packageName;
        return this;
    }
    public MongoGenerator author(String author){
        this.author = author;
        return this;
    }

    public MongoGenerator entity(Class entityClass){
        this.entity = entityClass;
        //获取实体的名称，作为import的内容
        this.entityName = entityClass.getName();
        this.entitySimpleName = entityClass.getSimpleName();
        return this;
    }

    /**
     * 设置service的路径
     * @param packageName
     * @return
     */
    public MongoGenerator serviceDir(String packageName){
        this.serviceDir = packageName;
        return this;
    }
    public MongoGenerator servicePrefixI(boolean withI){
        if (withI){
            this.servicePrefix = "I";
        }else {
            this.servicePrefix = "";
        }
        return this;
    }
    public MongoGenerator serviceImplDir(String packageName){
        this.serviceImplDir = packageName;
        return this;
    }

    public void build(){
        if (StringUtils.isEmpty(outputDir)){
            throw new MongicException("output dir can't be empty");
        }
        if (author == null){
            author = System.getProperty("user.name");
        }
        //填充service模板
        String packageName = basePackage +"."+serviceDir;
        String entityImport = entityName;
        String serviceName = servicePrefix+entitySimpleName+"Service";

        String serviceFileStr = serviceTemplate.replace("{package}",packageName)
                .replace("{entityImport}",entityImport)
                .replace("{serviceName}",serviceName)
                .replace("{entitySimpleName}",entitySimpleName)
                .replace("{author}",author);
        //获取路径

        String serviceFilePath = outputDir + "/" + packageName.replace(".",File.separator);
        File serviceDir = new File(serviceFilePath);

        if (!serviceDir.exists()){
            //创建目录
            boolean mk = serviceDir.mkdirs();
        }
        String serviceFile1 = serviceFilePath+File.separator+serviceName+".java";
        File serviceFile = new File(serviceFile1);
        if (!serviceFile.exists()){
            try {
                FileWriter fileWriter = new FileWriter(serviceFile);
                fileWriter.write(serviceFileStr);
                fileWriter.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //生成serviceImpl 文件
        String implPackageName = basePackage +"."+serviceImplDir;
        String serviceImplName = entitySimpleName+"ServiceImpl";

        String implStr = serviceImplTemplate.replace("{package}", implPackageName)
                .replace("{entityImport}", entityImport)
                .replace("{serviceImport}", packageName + "." + serviceName)
                .replace("{serviceImplName}", serviceImplName)
                .replace("{entitySimpleName}", entitySimpleName)
                .replace("{serviceName}", serviceName)
                .replace("{author}",author);

        String serviceImplFilePath = outputDir + "/" + implPackageName.replace(".",File.separator);
        File serviceImplDir = new File(serviceImplFilePath);

        if (!serviceImplDir.exists()){
            //创建目录
            boolean mk = serviceImplDir.mkdirs();
        }
        String serviceImplFile1 = serviceImplFilePath+File.separator+serviceImplName+".java";
        File serviceImplFile = new File(serviceImplFile1);
        if (!serviceImplFile.exists()){
            try {
                FileWriter fileWriter = new FileWriter(serviceImplFile);
                fileWriter.write(implStr);
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }




    private static final String serviceTemplate = "package {package};\n" +
            "\n" +
            "import {entityImport};\n" +
            "import com.mindc.mongic.service.BaseService;\n" +
            "\n/**\n" +
            " * @author {author}\n" +
            " */\n" +
            "public interface {serviceName} extends BaseService<{entitySimpleName}> {\n\n}";
    private static final String serviceImplTemplate = "package {package};\n" +
            "\n" +
            "import {entityImport};\n" +
            "import {serviceImport};\n" +
            "import org.springframework.stereotype.Service;\n"+
            "import com.mindc.mongic.service.BaseServiceImpl;\n" +
            "\n/**\n" +
            " * @author {author}\n" +
            " */\n" +
            "@Service\n" +
            "public class {serviceImplName} extends BaseServiceImpl<{entitySimpleName}> implements {serviceName} {\n\n}";


}
