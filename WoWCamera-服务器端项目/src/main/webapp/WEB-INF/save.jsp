<%--
  Created by IntelliJ IDEA.
  User: wyj
  Date: 2020/11/21
  Time: 下午10:46
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
    <h1>User表单提交</h1>
    <form name="userForm" action="${pageContext.request.contextPath}/user/save" method="post">
        用户名称:<input type="text" name="name"><br>
        用户账户名:<input type="text" name="username"><br>
        用户密码:<input type="password" name="password"><br>
        <input type="submit" value="保存"><br>
    </form>
</body>
</html>
