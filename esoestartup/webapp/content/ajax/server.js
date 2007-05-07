DatabaseDriver = Class.create();

DatabaseDriver.prototype = 
{
		initialize: function() 
		{
		},
	 
		ajaxUpdate: function(ajaxResponse)
		{
			var elements = ajaxResponse.getElementsByTagName("driver");
			for(var i=0;i<elements.length;i++)
			{
				var response = elements[i];
				var defaultDriver = response.getAttribute("defaultURL");
				Form.Element.setValue("dataRepositoryForm_dataRepositoryURL", defaultDriver);
			}
		}
}