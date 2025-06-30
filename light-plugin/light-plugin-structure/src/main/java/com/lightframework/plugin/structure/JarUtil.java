package com.lightframework.plugin.structure;

import com.lightframework.util.project.ProjectUtil;

import java.io.*;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarUtil {
    public static void extract(String sourceDir ,String outputDir,boolean overwrite) throws IOException {
        File pluginFile = ProjectUtil.getThisClassBaseFile();
        JarFile jar = null;
        try {
            jar = new JarFile(pluginFile);
            Enumeration<JarEntry> entryEnumeration = jar.entries();
            while (entryEnumeration.hasMoreElements()){
                JarEntry entry = entryEnumeration.nextElement();
                if(entry.getName().startsWith(sourceDir)) {
                    File entryFile = new File(outputDir, entry.getName().substring(sourceDir.length()));
                    if (!overwrite && entryFile.exists()){
                        continue;
                    }
                    if (entry.isDirectory()) {
                        entryFile.mkdirs();
                    } else {
                        OutputStream os = null;
                        try {
                            InputStream is = jar.getInputStream(entry);
                            os = new FileOutputStream(entryFile);
                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = is.read(buffer)) > 0) {
                                os.write(buffer, 0, len);
                            }
                        }finally {
                            if (os != null) {
                                os.close();
                            }
                        }
                    }
                }
            }
        } finally {
            if(jar != null){
                try {
                    jar.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
