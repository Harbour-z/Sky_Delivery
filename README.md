nginx中为前端代码，项目采用前后端分离模式开发；后端分模块至三个包进行管理pojo、配置与具体业务实现代码。

在JWT令牌校验中，对文件 `sky-server/src/main/java/com/sky/interceptor/JwtTokenAdminInterceptor.java` 
重写了afterCompletion方法，旨在删除添加到线程空间中的动态获取的ID值。