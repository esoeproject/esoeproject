class SpepFilters {
	def filters = {
		all(controller:'*', action:'*') {
			after = {
				def attributes = session[com.qut.middleware.spep.filter.SPEPFilter.ATTRIBUTES]
				def authenticated = (attributes != null)
				it.spep = ['attributes':attributes, 'authenticated':authenticated]
			}
		}
	}
}

