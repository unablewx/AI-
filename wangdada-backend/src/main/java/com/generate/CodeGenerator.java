package com.generate;

import cn.hutool.core.io.FileUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.FileWriter;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 代码生成器
 *
 */
public class CodeGenerator {

    /**
     * 用法：修改生成参数和生成路径，注释掉不需要的生成逻辑，然后运行即可
     *
     * @param args
     * @throws TemplateException
     * @throws IOException
     */
    public static void main(String[] args) throws TemplateException, IOException {
        // 实体类所在的路径文件夹
        String entityPath = "src/main/java/com/model/entity";
        // 生成代码中所在的包路径
        String packageName = "com";
        //中文实体注释
        List<String> dataName = new ArrayList<>();
        //实体名
        List<String> dataKey = new ArrayList<>();
        //实体名大写
        List<String> upperDataKey = new ArrayList<>();
        File folder = new File(entityPath);
        if (folder.isDirectory()) {
            File[] files = folder.listFiles((dir, name) -> name.endsWith(".java"));
            if (files != null) {
                for (File file : files) {
                    try {
                        String content = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
                        parseJavaFile(content, dataName, dataKey, upperDataKey);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        for (int i = 0; i < dataName.size(); i++) {
            doGenerate2(packageName, dataName.get(i), dataKey.get(i), upperDataKey.get(i));
        }
    }

    /**
     * 提取中文注释、类名、类名小写
     * @param content 文件路径
     * @param dataName 中文实体注释
     * @param dataKey 实体名
     * @param upperDataKey 实体名大写
     */
    private static void parseJavaFile(String content, List<String> dataName, List<String> dataKey, List<String> upperDataKey) {
        // 匹配注释部分的正则表达式
        Pattern commentPattern = Pattern.compile("/\\*\\*([^*]|[\\r\\n]|(\\*+([^*/]|[\\r\\n])))*\\*+/", Pattern.DOTALL);
        Matcher commentMatcher = commentPattern.matcher(content);

        // 匹配类名的正则表达式
        Pattern classPattern = Pattern.compile("public\\s+class\\s+(\\w+)");
        Matcher classMatcher = classPattern.matcher(content);

        // 提取注释中的中文内容
        while (commentMatcher.find()) {
            String comment = commentMatcher.group();
            String chineseText = extractChinese(comment);
            if (!chineseText.isEmpty()){
                System.out.println("dataName: " + chineseText);
                dataName.add(chineseText);
            }
        }

        // 提取类名
        while (classMatcher.find()) {
            String className = classMatcher.group(1);
            String lowerCaseFirst = toLowerCaseFirst(className);
            if (className != null && !className.isEmpty()){
                System.out.println("upperDataKey: " + className);
                System.out.println("dataKey: " + lowerCaseFirst);
                upperDataKey.add(className);
                dataKey.add(lowerCaseFirst);
            }
        }
    }


    private static String extractChinese(String text) {
        // 使用正则表达式匹配中文字符
        Pattern chinesePattern = Pattern.compile("[\u4e00-\u9fa5]+");
        Matcher chineseMatcher = chinesePattern.matcher(text);
        StringBuilder chineseText = new StringBuilder();
        while (chineseMatcher.find()) {
            chineseText.append(chineseMatcher.group());
        }
        return chineseText.toString();
    }

    /**
     * 首字母小写
     * @param str
     * @return
     */
    public static String toLowerCaseFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        char firstChar = str.charAt(0);
        char updatedFirstChar = Character.toLowerCase(firstChar);
        String remainder = str.substring(1);
        return updatedFirstChar + remainder;
    }

    /**
     * @param packageName  包名
     * @param dataName     中文实体注释
     * @param dataKey      实体名
     * @param upperDataKey 实体名首字母大写
     * @throws TemplateException
     * @throws IOException
     */
    public static void doGenerate2(String packageName, String dataName, String dataKey, String upperDataKey) throws TemplateException, IOException {
        // 封装生成参数
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("packageName", packageName);
        dataModel.put("dataName", dataName);
        dataModel.put("dataKey", dataKey);
        dataModel.put("upperDataKey", upperDataKey);

        // 生成路径默认值
        String projectPath = System.getProperty("user.dir");
        // 参考路径，可以自己调整下面的 outputPath
        String inputPath = projectPath + File.separator + "src/main/resources/templates/模板名称.java.ftl";
        String outputPath = String.format("%s/generator/包名/%s类后缀.java", projectPath, upperDataKey);

        // 1、生成 Controller
        // 指定生成路径
        inputPath = projectPath + File.separator + "src/main/resources/templates/TemplateController.java.ftl";
        outputPath = String.format("%s/generator/controller/%sController.java", projectPath, upperDataKey);
        // 生成
        doGenerate(inputPath, outputPath, dataModel);
        System.out.println("生成 Controller 成功，文件路径：" + outputPath);

        // 2、生成 Service 接口和实现类
        // 生成 Service 接口
        inputPath = projectPath + File.separator + "src/main/resources/templates/TemplateService.java.ftl";
        outputPath = String.format("%s/generator/service/%sService.java", projectPath, upperDataKey);
        doGenerate(inputPath, outputPath, dataModel);
        System.out.println("生成 Service 接口成功，文件路径：" + outputPath);
        // 生成 Service 实现类
        inputPath = projectPath + File.separator + "src/main/resources/templates/TemplateServiceImpl.java.ftl";
        outputPath = String.format("%s/generator/service/impl/%sServiceImpl.java", projectPath, upperDataKey);
        doGenerate(inputPath, outputPath, dataModel);
        System.out.println("生成 Service 实现类成功，文件路径：" + outputPath);

        // 3、生成数据模型封装类（包括 DTO 和 VO）
        // 生成 DTO
        inputPath = projectPath + File.separator + "src/main/resources/templates/model/TemplateAddRequest.java.ftl";
        outputPath = String.format("%s/generator/model/dto/%sAddRequest.java", projectPath, upperDataKey);
        doGenerate(inputPath, outputPath, dataModel);
        inputPath = projectPath + File.separator + "src/main/resources/templates/model/TemplateQueryRequest.java.ftl";
        outputPath = String.format("%s/generator/model/dto/%sQueryRequest.java", projectPath, upperDataKey);
        doGenerate(inputPath, outputPath, dataModel);
        inputPath = projectPath + File.separator + "src/main/resources/templates/model/TemplateEditRequest.java.ftl";
        outputPath = String.format("%s/generator/model/dto/%sEditRequest.java", projectPath, upperDataKey);
        doGenerate(inputPath, outputPath, dataModel);
        inputPath = projectPath + File.separator + "src/main/resources/templates/model/TemplateUpdateRequest.java.ftl";
        outputPath = String.format("%s/generator/model/dto/%sUpdateRequest.java", projectPath, upperDataKey);
        doGenerate(inputPath, outputPath, dataModel);
        System.out.println("生成 DTO 成功，文件路径：" + outputPath);
        // 生成 VO
        inputPath = projectPath + File.separator + "src/main/resources/templates/model/TemplateVO.java.ftl";
        outputPath = String.format("%s/generator/model/vo/%sVO.java", projectPath, upperDataKey);
        doGenerate(inputPath, outputPath, dataModel);
        System.out.println("生成 VO 成功，文件路径：" + outputPath);
    }

    /**
     * 生成文件
     *
     * @param inputPath  模板文件输入路径
     * @param outputPath 输出路径
     * @param model      数据模型
     * @throws IOException
     * @throws TemplateException
     */
    public static void doGenerate(String inputPath, String outputPath, Object model) throws IOException, TemplateException {
        // new 出 Configuration 对象，参数为 FreeMarker 版本号
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_30);

        // 指定模板文件所在的路径
        File templateDir = new File(inputPath).getParentFile();
        configuration.setDirectoryForTemplateLoading(templateDir);

        // 设置模板文件使用的字符集
        configuration.setDefaultEncoding("utf-8");

        // 创建模板对象，加载指定模板
        String templateName = new File(inputPath).getName();
        Template template = configuration.getTemplate(templateName);

        // 文件不存在则创建文件和父目录
        if (!FileUtil.exist(outputPath)) {
            FileUtil.touch(outputPath);
        }

        // 生成
        Writer out = new FileWriter(outputPath);
        template.process(model, out);

        // 生成文件后别忘了关闭哦
        out.close();
    }
}
