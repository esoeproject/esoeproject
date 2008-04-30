#                                               -*- Autoconf -*-
# Process this file with autoconf to produce a configure script.

AC_PREREQ(2.59)
AC_INIT([modspep], 0.5)
AC_CONFIG_SRCDIR([include/Cookies.h])
AC_CONFIG_HEADER([config.h])

# No version info needed for the apache module
dnl AC_SUBST([VERSION_INFO],[0])

AM_INIT_AUTOMAKE
AM_DISABLE_STATIC

# Checks for programs.
AC_PROG_CXX
dnl AC_PROG_CC
dnl AC_PROG_INSTALL
dnl AC_PROG_MKDIR_P
AM_PROG_LIBTOOL

# Checks for libraries.

# Checks for header files.

# Checks for typedefs, structures, and compiler characteristics.
dnl AC_HEADER_STDBOOL
dnl AC_C_CONST
dnl AC_TYPE_SIZE_T

# Checks for library functions.
AC_HEADER_STDC

AC_ARG_WITH(
	[xerces],
	[AS_HELP_STRING(
		[--with-xerces=DIR],
		[path to look for xerces ./include and ./lib])
	],
	[xercespath=$withval],
	[xercespath=]
)

AC_ARG_WITH(
	[xmlsec],
	[AS_HELP_STRING(
		[--with-xmlsec=DIR],
		[path to look for xml-security-c ./include and ./lib])
	],
	[xmlsecpath=$withval],
	[xmlsecpath=]
)

AC_ARG_WITH(
	[saml2cpp],
	[AS_HELP_STRING(
		[--with-saml2cpp=DIR],
		[path to look for saml2cpp ./include and ./lib])
	],
	[saml2path=$withval],
	[saml2path=]
)

AC_ARG_WITH(
	[icu],
	[AS_HELP_STRING(
		[--with-icu=DIR],
		[path to look for ICU ./include and ./lib])
	],
	[icupath=$withval],
	[icupath=]
)

AC_ARG_WITH(
	[boost],
	[AS_HELP_STRING(
		[--with-boost=DIR],
		[path to look for Boost ./include and ./lib])
	],
	[boostpath=$withval],
	[boostpath=]
)

AC_ARG_WITH(
	[boost-suffix],
	[AS_HELP_STRING(
		[--with-boost-suffix=SUFFIX],
		[value to append to Boost library names when linking])
	],
	[boostsuffix=$withval],
	[boostsuffix=]
)


AC_ARG_WITH(
	[xsd],
	[AS_HELP_STRING(
		[--with-xsd=DIR],
		[path to look for xsd ./libxsd header directory])
	],
	[xsdpath=$withval],
	[xsdpath=]
)

AC_ARG_WITH(
	[openssl],
	[AS_HELP_STRING(
		[--with-openssl=DIR],
		[path to look for openssl ./include and ./lib])
	],
	[opensslpath=$withval],
	[opensslpath=]
)

AC_ARG_WITH(
	[curl],
	[AS_HELP_STRING(
		[--with-curl=DIR],
		[path to look for cURL ./include and ./lib])
	],
	[curlpath=$withval],
	[curlpath=]
)

AC_ARG_WITH(
	[spepcpp],
	[AS_HELP_STRING(
		[--with-spepcpp=DIR],
		[path to look for spepcpp ./includes])
	],
	[speppath=$withval],
	[speppath=]
)

apache=0
AC_ARG_WITH(
	[apache1],
	[AS_HELP_STRING(
		[--with-apache1=DIR],
		[path to look for apache 1.3 ./include and ./lib])
	],
	[
		apacheflags="-DAPACHE1=1 -DEAPI=1" 
		apachepath=$withval
		apache=1
	]
)

AC_ARG_WITH(
	[apache2],
	[AS_HELP_STRING(
		[--with-apache2=DIR],
		[path to look for apache 2.x ./include and ./lib])
	],
	[
		apacheflags=
		apachepath=$withval
		apache=2
	]
)

if test $apache -eq 0
then
	AC_MSG_FAILURE([Please provide --with-apache1=DIR or --with-apache2=DIR])
fi

AM_CONDITIONAL([BUILDING_APACHE1],[test $apache -eq 1])
AM_CONDITIONAL([BUILDING_APACHE2],[test $apache -eq 2])

AC_ARG_WITH(
	[apreq],
	[AS_HELP_STRING(
		[--with-apreq=DIR],
		[path to look for apreq (for apache 1.3) or apreq2 (for apache 2.x) ./include and ./lib])
	],
	[apreqpath=$withval],
	[apreqpath=]
)

if test -n "$xercespath"; then
	xercesinc=-I$xercespath/include
	xerceslib=-L$xercespath/lib
fi
if test -n "$xmlsecpath"; then
	xmlsecinc=-I$xmlsecpath/include
	xmlseclib=-L$xmlsecpath/lib
fi
if test -n "$boostpath"; then
	boostinc=-I$boostpath/include
	boostlib=-I$boostpath/lib
fi
if test -n "$icupath"; then
	icuinc=-I$icupath/include
	iculib=-L$icupath/lib
fi
if test -n "$xsdpath"; then
	xsdinc=-I$xsdpath/libxsd
fi
if test -n "$opensslpath"; then
	opensslinc=-I$opensslpath/include
	openssllib=-I$opensslpath/lib
fi
if test -n "$curlpath"; then
	curlinc=-I$curlpath/include
	curllib=-L$curlpath/lib
fi
if test -n "$speppath"; then
	spepinc="-I$speppath/include/spep-\$(VERSION)"
	speplib=-L$speppath/lib
fi
if test -n "$saml2path"; then
	saml2inc="-I$saml2path/include/saml2-\$(VERSION)"
	saml2lib=-L$saml2path/lib
fi
if test -n "$apachepath"; then
	apacheinc=-I$apachepath/include
	apachelib=-L$apachepath/lib
fi
if test -n "$apreqpath"; then
	apreqinc=-I$apreqpath/include
	apreqlib=-L$apreqpath/lib
fi

localinc='-I${top_srcdir}/include'

AC_SUBST([AM_CPPFLAGS],["$localinc $xercesinc $xmlsecinc $boostinc $icuinc $xsdinc $opensslinc $curlinc $saml2inc $spepinc $apacheinc $apreqinc"])
AC_SUBST([AM_LDFLAGS],["$xerceslib $xmlseclib $boostlib $iculib $openssllib $curllib $saml2lib $speplib $apreqlib"])
AC_SUBST([BOOST_SUFFIX],["$boostsuffix"])

AC_CONFIG_FILES([Makefile src/Makefile include/Makefile])
AC_OUTPUT
