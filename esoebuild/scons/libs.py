Import('conf')

class libs:
	conf
	boost_suffix = ''

	def __init__(self, conf):
		self.conf = conf
		if ARGUMENTS.get('boostsuffix'): self.boost_suffix = ARGUMENTS.get('boostsuffix')

	def check_xsd_lib(self):
		if not self.conf.CheckCXXHeader('xsd/cxx/tree/elements.hxx'):
			print 'Did not find XSD library'
			Exit(1)

	def check_xerces_lib(self):
		if not self.conf.CheckLibWithHeader('xerces-c', 'xercesc/dom/DOMElement.hpp', 'c++'):
			print 'Did not find Xerces library'
			Exit(1)

	def check_xml_security_lib(self):
		if not self.conf.CheckLibWithHeader('xml-security-c', 'xsec/utils/XSECPlatformUtils.hpp', 'c++'):
			print 'Did not find Xml-Security-C library'
			Exit(1)

	def check_boost_libs(self):
		boost_libs = ['program_options', 'thread', 'date_time']

		if False in [self.conf.CheckLibWithHeader('boost_'+name+self.boost_suffix, 'boost/'+name+'.hpp', 'c++') for name in boost_libs]:
			print 'Did not find Boost libraries'
			Exit(1)

obj = libs(conf)
Return('obj')
