package com.my.ftl.template;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * a helper class for access <a href="http://freemarker.foofun.cn/index.html" target="_blank">FreeMarker</a>
 */
public class FtlTemplateRenderFactory {

    private static final Logger logger = LoggerFactory.getLogger(FtlTemplateRenderFactory.class);

    public interface TemplatePath {
        String DRIVER_DETAIL = "ftl/templates/driverdetail.html";
    }

    public interface TemplateName {
        String DRIVER_DETAIL = "driverdetail.html";
    }

    private static FtlTemplateRenderFactory instance = null;

    @Setter
    @Getter
    private Configuration configuration;

    private FtlTemplateRenderFactory() {
    }

    public static FtlTemplateRenderFactory getInstance() {
        if (instance != null) {
            return instance;
        }
        synchronized (FtlTemplateRenderFactory.class) {
            if (null == instance) {
                instance = new FtlTemplateRenderFactory();

                /* ------------------------------------------------------------------------ */
                /* You should do this ONLY ONCE in the whole application life-cycle:        */

                /* Create and adjust the configuration singleton */
                Configuration cfg = new Configuration(Configuration.VERSION_2_3_22);
                cfg.setTemplateLoader(new ClassTemplateLoader());
                File templateDir = new File("ftl/templates");
//                try {
//                    cfg.setDirectoryForTemplateLoading(templateDir);
//                } catch (IOException e) {
//                    logger.error("failed to setDirectoryForTemplateLoading for Configuration with {}", templateDir, e);
//                }
                cfg.setDefaultEncoding("UTF-8");
                cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

                /* ------------------------------------------------------------------------ */
                /* You usually do these for MULTIPLE TIMES in the application life-cycle:   */

                instance.setConfiguration(cfg);
            }
        }
        return instance;
    }

    public static class MyBasicFtlTemplateRender {

        private static final Logger logger = LoggerFactory.getLogger(MyBasicFtlTemplateRender.class);

        private final Template template;

        public MyBasicFtlTemplateRender(String ftlName) {
            try {
                this.template = getInstance().getConfiguration().getTemplate(ftlName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public boolean processToFile(Object root, String outFilePath) {
            if (null == template) {
                logger.warn("template is null");
                return false;
            }
            if (null == outFilePath) {
                logger.warn("outFilePath is null");
                return false;
            }
            File outFile = new File(outFilePath);
            Writer out = null;
            try {
                out = new OutputStreamWriter(new FileOutputStream(outFile), StandardCharsets.UTF_8);
            } catch (FileNotFoundException e) {
                logger.error("failed to create OutputStream with {}", outFile, e);
                return false;
            }
            try {
                template.process(root, out);
            } catch (TemplateException | IOException e) {
                logger.error("failed to process data to template", e);
                return false;
            }
            return true;
        }
    }

    private static final Map<String, MyBasicFtlTemplateRender> FtlTemplateRenderMap = new HashMap<>();

    public static MyBasicFtlTemplateRender getFtlTemplateRender(String ftlName) {
        MyBasicFtlTemplateRender ftlTemplateRender = FtlTemplateRenderMap.get(ftlName);
        if (null == ftlTemplateRender) {
            ftlTemplateRender = new MyBasicFtlTemplateRender(ftlName);
            FtlTemplateRenderMap.put(ftlName, ftlTemplateRender);
        }
        return ftlTemplateRender;
    }
}
