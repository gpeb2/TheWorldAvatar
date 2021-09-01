<%@ page language="java"
         contentType="text/html; charset=US-ASCII"
         pageEncoding="US-ASCII"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "https://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
        
        <script  src="${pageContext.request.contextPath}/index.js" type="text/javascript"></script>
        <link href="${pageContext.request.contextPath}/style.css" type="text/css" rel="stylesheet">
        
        <title>StatusAgent Dashboard</title>
        
        <%@ page import="uk.ac.cam.cares.jps.agent.status.StatusAgent" %>
        <%@ page import="uk.ac.cam.cares.jps.agent.status.TemplateHandler" %>
        <%@ page import="uk.ac.cam.cares.jps.agent.status.TestRegistry" %>
        <%@ page import="uk.ac.cam.cares.jps.agent.status.define.*" %>
   
        <%@ page import="java.util.Set" %>
    </head>
    <body onload="initialise()">
        <div id="preamble">
            <div id="title">
                <h1>StatusAgent - Dashboard</h1>
            </div>
            <div id="introduction">
                <p>
                    Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam convallis, neque ac convallis ornare, nunc nisl tincidunt sem, 
                    sed mattis mi diam eu lectus. Maecenas tincidunt lacus eget sapien pellentesque, in convallis nulla hendrerit. Ut non metus
                    leo. In porttitor dapibus odio, nec venenatis est semper ac.                
                </p>
            </div>
        </div>
        <div class="all-tests-container">
            <%
                for (TestType type : TestRegistry.getDefinedTypes()) {
                    out.print("<div class='test-container'>");
                    out.print("<div class='test-container-title center-text'>");
                    out.print("<h3>" + type.toString() + "</h3>");
                    out.print("</div>");
                    
                    Set<TestDefinition> tests = TestRegistry.getDefinedTests(type);
                    
                    for(TestDefinition test : tests) {
                        out.print(TemplateHandler.getTestResultStub(pageContext.getServletContext(), test));
                    }
                    out.print("</div>");
                }
            %>
        </div>

    </body>
</html>