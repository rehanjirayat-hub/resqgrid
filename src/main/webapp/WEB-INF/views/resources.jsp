<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html>
<head>
    <title>ResQGrid — Resources</title>
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

<h2>Emergency Resources</h2>

<form method="get" action="${pageContext.request.contextPath}/resources">
    <label for="status">Filter by status</label>
    <select id="status" name="status">
        <option value="" ${selectedStatus == '' ? 'selected' : ''}>All</option>
        <option value="AVAILABLE" ${selectedStatus == 'AVAILABLE' ? 'selected' : ''}>Available</option>
        <option value="BUSY" ${selectedStatus == 'BUSY' ? 'selected' : ''}>Busy</option>
        <option value="OFFLINE" ${selectedStatus == 'OFFLINE' ? 'selected' : ''}>Offline</option>
        <option value="MAINTENANCE" ${selectedStatus == 'MAINTENANCE' ? 'selected' : ''}>Maintenance</option>
    </select>
    <button type="submit">Apply</button>
</form>

<c:choose>
    <c:when test="${empty resources}">
        <p class="empty">No resources match the current filter.</p>
    </c:when>
    <c:otherwise>
        <table>
            <thead>
            <tr>
                <th>ID</th>
                <th>Type</th>
                <th>Status</th>
                <th>Location</th>
                <th>Dispatches</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="resource" items="${resources}">
                <tr>
                    <td>${resource.id}</td>
                    <td>${resource.type}</td>
                    <td class="status-${resource.status}">${resource.status}</td>
                    <td>${resource.location.latitude}, ${resource.location.longitude}</td>
                    <td>
                        <a href="${pageContext.request.contextPath}/dispatches?resourceId=${resource.id}">History</a>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </c:otherwise>
</c:choose>

</body>
</html>