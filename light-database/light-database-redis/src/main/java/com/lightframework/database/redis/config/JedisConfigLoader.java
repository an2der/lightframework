package com.lightframework.database.redis.config;

import com.lightframework.database.redis.util.YamlLoader;
import com.lightframework.util.project.ProjectUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/** redis配置对象
 * @author ：mashuai
 * @date ：2022/05/13 17:23
 */
@Slf4j
@Data
public class JedisConfigLoader {

    private static final String jedisConfPath = "config/redis.yml";//配置文件地址

    /**
     * 构造配置文件
     * @return
     */
    public synchronized static JedisConfig load(){
        String realPath = new File(ProjectUtil.getProjectRootPath(),jedisConfPath).getAbsolutePath();//真实磁盘地址
        log.debug("redisConfig realPath = {}",realPath);
        JedisConfig jedisConfig = YamlLoader.load(realPath, JedisConfig.class);
        return jedisConfig;
    }


}
