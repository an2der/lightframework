package com.lightframework.database.redis.util;

import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/** 加载 yaml 文件 工具
 * @author ：mashuai
 * @date ：2022/09/07 17:52
 */
@Slf4j
public class YamlLoader {
    /**
     * 指定yaml文件地址,读取出配置对象
     * @param configPath yaml文件地址
     * @param clazz 配置对象类型
     * @param <T>
     * @return 配置对象
     */
    public synchronized static <T> T load(String configPath,Class<T> clazz){
        T config = null;//配置文件对象
        InputStream inputStream = null;
        try {
            //String realPath = findFile(ConfigLoader.class.getClassLoader(), configPath);//真实磁盘地址
            //String realPath = configPath;//真实磁盘地址
            log.debug("ConfigLoader realPath = {}",configPath);
            inputStream = new FileInputStream(configPath);
            Yaml yaml = new Yaml();
            Map<String,Object> load = yaml.load(inputStream);
            log.debug("load={}", load);
            config = JsonUtil.map2Object(load, clazz);
            log.debug("config={}", config);
        } catch (FileNotFoundException e) {
            log.error("ConfigLoader 找不到文件"+configPath, e);
        } catch (Exception e) {
            log.error(configPath + " 配置文件错误", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("", e);
                }
            }
        }
        return config;
    }
}
