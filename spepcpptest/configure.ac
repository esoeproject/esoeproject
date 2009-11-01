#                                               -*- Autoconf -*-
# Process this file with autoconf to produce a configure script.

AC_PREREQ(2.59)
AC_INIT([SPEPCPPTEST], 0.7)
AC_CONFIG_SRCDIR([src/test.cpp])
#AC_CONFIG_HEADER([config.h])

AM_INIT_AUTOMAKE

# Checks for programs.
AC_PROG_CXX
AC_PROG_CC
dnl AC_PROG_INSTALL
dnl AC_PROG_MKDIR_P

# Checks for libraries.

# Checks for header files.

# Checks for typedefs, structures, and compiler characteristics.
dnl AC_C_CONST

# Checks for library functions.
dnl AC_FUNC_FORK

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
	[gtest],
	[AS_HELP_STRING(
		[--with-gtest=DIR],
		[path to look for gtest ./bin directory])
	],
	[gtestpath=$withval],
	[gtestpath=]
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
		[path to look for spep ./include/spep-${VERSION}])
	],
	[speppath=$withval],
	[speppath=]
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
if test -n "$gtestpath"; then
	gtestinc=$($gtestpath/bin/gtest-config --cxxflags --cppflags)
	gtestlib=$($gtestpath/bin/gtest-config --ldflags --libs)
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

localinc='-I${top_srcdir}/include'

AC_SUBST([AM_CPPFLAGS],["$localinc $xercesinc $xmlsecinc $boostinc $icuinc $gtestinc $xsdinc $opensslinc $curlinc $saml2inc $spepinc"])
AC_SUBST([AM_LDFLAGS],["$xerceslib $xmlseclib $boostlib $iculib $gtestlib $openssllib $curllib $saml2lib $speplib"])
AC_SUBST([BOOST_SUFFIX],["$boostsuffix"])

AC_CONFIG_FILES([Makefile src/Makefile include/Makefile])
AC_OUTPUT
