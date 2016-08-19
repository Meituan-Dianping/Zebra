<%@ page contentType="text/html; charset=utf-8" isELIgnored="false" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="a" uri="/WEB-INF/inspect.tld" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core" %>
<jsp:useBean id="ctx" type="com.dianping.phoenix.inspect.status.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.phoenix.inspect.status.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.phoenix.inspect.status.Model" scope="request"/>

<a:layout>

   <h2>System Status: ${model.state.icon}</h2><br>

   <c:forEach var="component" items="${model.components}">
      <h2><a name="${component.id}"></a>${component.displayName} ${component.state.icon}</h2>

      <c:if test="${not empty component.properties}">
         <table class="table table-striped table-bordered table-hover table-condensed" style="width: 480px">
         <c:forEach var="property" items="${component.properties}">
            <tr><td>${property.key}</td><td>${w:htmlEncode(property.value)}</td></tr>
         </c:forEach>
         </table>
      </c:if>

      <c:set var="tables" value="${component.tables}"/>
      
      <c:forEach var="table" items="${tables}">
         <table class="table table-striped table-bordered table-hover table-condensed" style="${table.style}">
         
         <c:if test="${not empty table.caption}">
            <caption>${table.caption}</caption>
         </c:if>
         
         <c:if test="${not empty table.header}">
            <thead>
               <tr>
                  <c:forEach var="cell" items="${table.header}"><th>${cell}</th></c:forEach>
               </tr>
            </thead>
         </c:if>
         
         <c:forEach var="row" items="${table.body}">
            <tr>
               <c:forEach var="cell" items="${row}"><td>${cell}</td></c:forEach>
            </tr>
         </c:forEach>

         <c:if test="${not empty table.footer}">
            <tfoot>
               <tr>
                  <c:forEach var="cell" items="${table.footer}"><td>${cell}</td></c:forEach>
               </tr>
            </tfoot>
         </c:if>

         </table>
      </c:forEach>
   </c:forEach>

</a:layout>