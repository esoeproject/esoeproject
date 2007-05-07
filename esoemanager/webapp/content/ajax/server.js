EndpointDetails = Class.create();

EndpointDetails.prototype = 
{
		initialize: function() 
		{
		},
	 
		ajaxUpdate: function(ajaxResponse)
		{
			var elements = ajaxResponse.getElementsByTagName("endpoint");
			for(var i=0;i<elements.length;i++)
			{
				var response = elements[i];
				
				var acs = response.getAttribute("acs");
				Form.Element.setValue("spepDetails_AssertionConsumerService", acs);
				
				var sls = response.getAttribute("sls");
				Form.Element.setValue("spepDetails_SingleLogoutService", sls);
				
				var ccs = response.getAttribute("ccs");
				Form.Element.setValue("spepDetails_CacheClearService", ccs);
			}
		}
}