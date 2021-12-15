<%--
  Created by IntelliJ IDEA.
  User: wyj
  Date: 2020/11/14
  Time: 下午8:08
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
<%--    <form action="${pageContext.request.contextPath}/file/uploadFiles" method="post" enctype="multipart/form-data">--%>
<%--        名称<input type="text" name="name"><br>--%>
<%--        文件1<input type="file" name="uploadFiles">--%>
<%--        文件2<input type="file" name="uploadFiles">--%>
<%--        <input type="submit" value="提交">--%>
<%--    </form>--%>

    <form action="${pageContext.request.contextPath}/file/uploadFile" method="post" enctype="multipart/form-data">
        名称<input type="text" name="name">
        文件<input type="file" name="file">
        <input type="submit" value="提交">
    </form>
</body>
</html>
