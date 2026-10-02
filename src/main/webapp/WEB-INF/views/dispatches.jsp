<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html>
<head>
    <title>ResQGrid — Dispatches</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body>
<header>
    <h1>ResQGrid</h1>
    <nav>
        <a href="${pageContext.request.contextPath}/incidents">Incidents</a>
        <a href="${pageContext.request.contextPath}/resources">Resources</a>
        <a href="${pageContext.request.contextPath}/dispatches">Dispatches</a>
    </nav>
</header>

<h2>Dispatches</h2>

<c:choose>
    <c:when test="${empty dispatches}">
        <p class="empty">There are no dispatch records to show.</p>
    </c:when>
    <c:otherwise>
        <table>
            <thead>
            <tr>
                <th>ID</th>
                <th>Incident</th>
                <th>Resource</th>
                <th>Status</th>
                <th>Created</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="dispatch" items="${dispatches}">
                <tr>
                    <td>${dispatch.id}</td>
                    <td>${dispatch.incident.description}</td>
                    <td>${dispatch.resource.type}</td>
                    <td class="status-${dispatch.status}">${dispatch.status}</td>
                    <td>${dispatch.createdAt}</td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </c:otherwise>
</c:choose>

</body>
</html>