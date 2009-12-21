Import('env')
Import('conf')

import SCons.Script.Main

class libs:
	conf
	boost_suffix = ''
	boost_libraries = ['program_options', 'thread', 'date_time', 'regex']

	def __init__(self, conf):
		self.conf = conf
		if ARGUMENTS.get('boostsuffix'): self.boost_suffix = ARGUMENTS.get('boostsuffix')

	def openssl_lib(self):
		if env.get('PLATFORM') == 'win32':
			if not conf.CheckLibWithHeader('libeay32', 'openssl/opensslv.h', 'c'):
				print 'Did not find OpenSSL library'
				Exit(1)
			if not conf.CheckLibWithHeader('ssleay32', 'openssl/opensslv.h', 'c'):
				print 'Did not find OpenSSL library'
				Exit(1)
		else:
			if not conf.CheckLibWithHeader(['crypto'], 'openssl/opensslv.h', 'c'):
				print 'Did not find OpenSSL library'
				Exit(1)
			if not conf.CheckLibWithHeader(['ssl'], 'openssl/opensslv.h', 'c'):
				print 'Did not find OpenSSL library'
				Exit(1)

	def xsd_lib(self):
		if not conf.CheckCXXHeader('xsd/cxx/tree/elements.hxx'):
			print 'Did not find XSD library'
			Exit(1)

	def icu_lib(self):
		if not conf.CheckCXXHeader('unicode/unistr.h'):
			print 'Did not find ICU library header'
			Exit(1)

		iculibs = ['icudata', 'icui18n', 'icuio', 'icule', 'iculx', 'icutu', 'icuuc']
		if env.get('PLATFORM') == 'win32':
			iculibs = ['icudt', 'icuin', 'icuio', 'icule', 'iculx', 'icutu', 'icuuc']

		if False in [conf.CheckLib(lib) for lib in iculibs]:
			print 'Did not find ICU library'
			Exit(1)

	def xerces_lib(self):
		if not conf.CheckLibWithHeader('xerces-c', 'xercesc/dom/DOMElement.hpp', 'c++'):
			print 'Did not find Xerces library'
			Exit(1)

	def xml_security_lib(self):
		if not conf.CheckLibWithHeader('xml-security-c', 'xsec/utils/XSECPlatformUtils.hpp', 'c++'):
			print 'Did not find Xml-Security-C library'
			Exit(1)

	def boost_libs(self):
		if env.get('PLATFORM') == 'win32':
			if False in [conf.CheckCXXHeader('boost/'+name+'.hpp') for name in self.boost_libraries]:
				print 'Did not find Boost libraries'
				Exit(1)
		else:
			if False in [conf.CheckLibWithHeader('boost_'+name+self.boost_suffix, 'boost/'+name+'.hpp', 'c++') for name in self.boost_libraries]:
				print 'Did not find Boost libraries'
				Exit(1)

	def curl_lib(self):
		if not conf.CheckLibWithHeader('curl', 'curl/curl.h', 'c'):
			print 'Did not find Curl library'
			Exit(1)

	def asio_lib(self):
		if not conf.CheckCXXHeader('asio.hpp'):
			print 'Did not find Asio library'
			Exit(1)

	def apache_lib(self):
		if not conf.CheckCHeader('apr.h'):
			print 'Did not find APR headers (required for Apache)'
			Exit(1)
		if not conf.CheckCHeader('httpd.h'):
			print 'Did not find Apache headers'
			Exit(1)

	def apreq_lib(self):
		if not conf.CheckLibWithHeader('apreq2', 'apreq2/apreq.h', 'c'):
			print 'Did not find libapreq2 library'
			Exit(1)

	def esoe_saml_lib(self):
		if not conf.CheckLibWithHeader('esoesaml2', 'saml2/SAML2Defs.h', 'c++'):
			print 'Did not find ESOE SAML 2.0 library'
			Exit(1)

	def esoe_spep_lib(self):
		if not conf.CheckLibWithHeader('spep', 'spep/SPEP.h', 'c++'):
			print 'Did not find ESOE SPEP library'
			Exit(1)

class noop_libs:
	def __getattr__(self, name):
		return self.do_nothing
	def do_nothing(self):
		pass

if SCons.Script.Main.GetOption('clean'):
	obj = noop_libs()
else:
	obj = libs(conf)

Return('obj')
