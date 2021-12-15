<%--
  Created by IntelliJ IDEA.
  User: wyj
  Date: 2020/11/21
  Time: 下午10:46
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
    <h1>User数据展示</h1>
    <table border="1">
        <tr>
            <th>用户id</th>
            <th>用户姓名</th>
            <th>用户帐号</th>
            <th>用户密码</th>
        </tr>
        <tr>
            <td>${user.id}</td>
            <td>${user.name}</td>
            <td>${user.username}</td>
            <td>${user.password}</td>
        </tr>
    </table>

</body>
</html>
