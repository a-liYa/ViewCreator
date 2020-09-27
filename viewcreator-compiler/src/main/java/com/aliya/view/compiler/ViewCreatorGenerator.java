package com.aliya.view.compiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;

/**
 * ViewCreatorGenerator
 *
 * @author a_liYa
 * @date 2020/9/23 15:47.
 */

class ViewCreatorGenerator {
    ProcessingEnvironment mProcessingEnv;
    static String CLASS_NAME = "ViewCreatorImpl";

    public ViewCreatorGenerator(ProcessingEnvironment processingEnv) {
        mProcessingEnv = processingEnv;
    }

    public void createJavaFile() {
        Writer writer = null;
        JavaFileObject javaFile = null;
        try {
            javaFile = mProcessingEnv.getFiler().createSourceFile(CLASS_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (javaFile != null) {

            String classPath = javaFile.toUri().getPath();
            String buildPath = classPath.substring(0, classPath.indexOf("/generated/"));
            Set<String> viewNameSet = readViewNameSet(buildPath + "/tmp/mergeResourcesLayoutParse/view_names.txt");

            try {
                writer = javaFile.openWriter();
                writer.write(buildClassInfo(viewNameSet, CLASS_NAME));
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IOs.close(writer);
            }
        }
    }

    private String buildClassInfo(Set<String> viewNameSet, String className) {
        StringBuilder builder = new StringBuilder();
        builder.append("// Generated code. Do not modify!\n");
        builder.append("package com.aliya.view;\n\n");
        builder.append("import android.content.Context;\n");
        builder.append("import android.util.AttributeSet;\n");
        builder.append("import android.view.*;\n");
        builder.append("import android.widget.*;\n");
        builder.append("import android.webkit.*;\n");
        builder.append("import android.app.*;\n");
        builder.append('\n');

        builder.append("public class ").append(className).append(" {\n\n");

        /*---------------------Method createView(name, context, attr) start-----------------------*/
        builder.append("\tpublic View createView(String name, Context context, AttributeSet attrs) {\n");
        {   // switch 语句
            builder.append("\t\tswitch(name) {\n");
            for (String viewName : viewNameSet) {
                builder.append("\t\t\tcase \"").append(viewName).append("\" :\n");
                builder.append("\t\t\t\treturn new ").append(viewName).append("(context,attrs);\n");
            }
            builder.append("\t\t}\n");
        }
        builder.append("\t\treturn null;\n");
        builder.append("\t}\n");
        /*---------------------Method createView(name, context, attr) end-------------------------*/

        builder.append("}\n");

        return builder.toString();
    }

    private Set<String> readViewNameSet(String inputFilePath) {
        HashSet<String> viewNameSet = new HashSet<>();
        File inputFile = new File(inputFilePath);
        if (inputFile.exists()) {
            FileReader fileReader = null;
            BufferedReader bufferedReader = null;
            try {
                fileReader = new FileReader(inputFile);
                bufferedReader = new BufferedReader(fileReader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    String viewName = line.trim();
                    if (!viewName.isEmpty()) {
                        viewNameSet.add(viewName);
                    }
                }
            } catch (IOException e) {
                // nothing to do
            } finally {
                IOs.close(fileReader, bufferedReader);
            }
        }
        return viewNameSet;
    }
}
