class SpepFilters {
	def filters = {
		all(controller:'*', action:'*') {
			before = {
				def attributes = session[com.qut.middleware.spep.filter.SPEPFilter.ATTRIBUTES]
				def authenticated = (attributes != null)
				request.spep = ['attributes':attributes, 'authenticated':authenticated]
			}
		}
	}
}

