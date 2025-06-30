#<span style="color:yellow">项目配置说明！！！！！！！</span>
**<span style="color:orange">&emsp;&emsp;将IDEA工具maven的settings.xml文件设置为项目根目录下的settings.xml。</br>（配置文件使用maven默认仓库地址，如果要改变仓库地址就修改IDEA -> maven的Local repository设置，不要修改settings.xml配置文件，会影响其他人！！！）</span>**
#<span style="color:yellow">项目结构说明！！！！！！！</span>
**<span style="color:orange">&emsp;&emsp;项目代码结构规定以业务模块去划分为MAVEN子模块，这样可以使模块代码相对独立与其他模块代码解耦，目录结构也更加清晰易读。
</br>&emsp;&emsp;业务子模块结构需要将可能被其他模块单独引用的层创建为MAVEN子模块，不会被其他模块引用到的独属于该模块的代码都放到 core 层，这样可以避免其他模块使用该模块代码时引入不必要的代码，并且可以解决在开发过程中MAVEN模块之间发生循环引用的问题。（什么是MAVEN模块循环引用？比如项目中有A、B两个模块，A引用了模块B，B又用到了模块A，就会发生循环引用，导致MAVEN无法编译）
</br></span>**
**<span style="color:green"></br>以用户模块为例的项目结构：</span>**
```
project                             项目根目录
│
├─project-user                      用户业务模块
│  ├─project-user-bean              用户JAVA对象类模块（如：vo、dto、pojo）
│  ├─project-user-common            用户公共模块（如：cache、constant）
│  ├─project-user-core              用户业务核心（如：controller、handler、initializer）
│  ├─project-user-dao               用户数据库访问模块（数据库mapper、数据库表model对象）
│  ├─project-user-service           用户业务接口（业务接口与实现）
│  pom.xml                          用户模块POM文件
│  │
├─project-common                    项目公共模块
├─project-starter                   项目启动模块
│  .gitignore                       .gitignore文件
│  pom.xml                          父类POM文件
│  README.md                        项目说明文件
│  settings.xml                     MAVEN配置文件
│
```