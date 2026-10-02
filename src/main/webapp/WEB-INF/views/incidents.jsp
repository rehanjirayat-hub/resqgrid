<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html>
<head>
    <title>ResQGrid — Incidents</title>
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

<h2>Incidents</h2>

<form method="get" action="${pageContext.request.contextPath}/incidents">
    <label for="status">Filter by status</label>
    <select id="status" name="status">
        <option value="" ${selectedStatus == '' ? 'selected' : ''}>All</option>
        <option value="REPORTED" ${selectedStatus == 'REPORTED' ? 'selected' : ''}>Reported</option>
        <option value="ASSESSED" ${selectedStatus == 'ASSESSED' ? 'selected' : ''}>Assessed</option>
        <option value="REQUIREMENTS_DETERMINED" ${selectedStatus == 'REQUIREMENTS_DETERMINED' ? 'selected' : ''}>Requirements determined</option>
        <option value="RESPONSE_IN_PROGRESS" ${selectedStatus == 'RESPONSE_IN_PROGRESS' ? 'selected' : ''}>Response in progress</option>
        <option value="RESOLVED" ${selectedStatus == 'RESOLVED' ? 'selected' : ''}>Resolved</option>
    </select>
    <button type="submit">Apply</button>
</form>

<c:choose>
    <c:when test="${empty incidents}">
        <p class="empty">No incidents match the current filter.</p>
    </c:when>
    <c:otherwise>
        <table>
            <thead>
            <tr>
                <th>ID</th>
                <th>Description</th>
                <th>Severity</th>
                <th>Status</th>
                <th>Location</th>
                <th>Reported</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="incident" items="${incidents}">
                <tr>
                    <td>${incident.id}</td>
                    <td>${incident.description}</td>
                    <td class="severity-${incident.severity}">${incident.severity}</td>
                    <td>${incident.status}</td>
                    <td>
                        ${incident.location.latitude}, ${incident.location.longitude}
                    </td>
                    <td>${incident.createdAt}</td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </c:otherwise>
</c:choose>

</body>
</html>