<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Secured by RULE</title>
</head>
<body>

This page is secured by the rule: <br>

<pre>
&lt;Rule Effect="Deny" RuleId="urn:polcy:spep2:default:rule2"&gt;
        &lt;Description&gt;Only allow beddoes access to this file.&lt;/Description&gt;
        &lt;Target&gt;
          &lt;Resources&gt;
            &lt;Resource&gt;
              &lt;AttributeValue&gt;/secure/secure.*\.jsp&lt;/AttributeValue&gt;
            &lt;/Resource&gt;
          &lt;/Resources&gt;
        &lt;/Target&gt;
        &lt;Condition&gt;
          &lt;Apply FunctionId="not"&gt;
             &lt;Apply FunctionId="string-equal"&gt;
                    &lt;SubjectAttributeDesignator AttributeId="uid" /&gt;
                    &lt;AttributeValue&gt;beddoes&lt;/AttributeValue&gt;
             &lt;/Apply&gt;
          &lt;/Apply&gt;
        &lt;/Condition&gt;
&lt;/Rule&gt;
</pre>

</body>
</html>