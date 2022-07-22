# 本项目初次灵感来自于SmithCruise提供的思路，文章地址：https://www.jianshu.com/p/f37f8c295057
## 项目的功能点
>1. RESTful API
>2. Maven集成Mybatis Generator(逆向工程)
>3. Shiro + Java-JWT实现无状态鉴权机制(Token)
>4. 密码加密(采用AES-128 + Base64的方式)
>5. 集成Redis(Jedis)
>6. 重写Shiro缓存机制(Redis)
>7. Redis中保存RefreshToken信息(做到JWT的可控性)
>8. 根据RefreshToken自动刷新AccessToken
