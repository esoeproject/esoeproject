#                                               -*- Autoconf -*-
# Process this file with autoconf to produce a configure script.

AC_PREREQ(2.59)
AC_INIT([SPEP], 0.9.0)
AM_INIT_AUTOMAKE([subdir-objects])
AC_CONFIG_SRCDIR([include/spep/Util.h])
#AC_CONFIG_HEADER([config.h])

#AC_SUBST([VERSION_INFO],[1:0:0])

AM_INIT_AUTOMAKE
AM_DISABLE_STATIC

# Checks for programs.
AC_PROG_CXX
AX_CXX_COMPILE_STDCXX_11([noext],[mandatory])
AM_PROG_LIBTOOL

# Checks for libraries.


dnl These were recommended by autoscan, but there's no sense checking if we ignore
dnl the result anyway.. so for now these are commented out.

# Checks for header files.
dnl AC_HEADER_STDC
dnl AC_CHECK_HEADERS([netdb.h netinet/in.h sys/socket.h])

# Checks for typedefs, structures, and compiler characteristics.
dnl AC_HEADER_STDBOOL
dnl AC_C_CONST
dnl AC_C_INLINE
dnl AC_TYPE_INT32_T
dnl AC_TYPE_SIZE_T
dnl AC_TYPE_SSIZE_T

# Checks for library functions.
dnl AC_FUNC_MALLOC
dnl AC_FUNC_REALLOC
dnl AC_FUNC_STRTOD
dnl AC_CHECK_FUNCS([gethostbyname memmove memset socket strerror])


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

if test -n "$xercespath"; then
	xercesinc=-I$xercespath/include
	xerceslib=-L$xercespath/lib64
fi
if test -n "$xmlsecpath"; then
	xmlsecinc=-I$xmlsecpath/include
	xmlseclib=-L$xmlsecpath/lib64
fi
if test -n "$boostpath"; then
	boostinc=-I$boostpath/include
	boostlib=-I$boostpath/lib64
fi
if test -n "$icupath"; then
	icuinc=-I$icupath/include
	iculib=-L$icupath/lib64
fi
if test -n "$xsdpath"; then
	xsdinc=-I$xsdpath/libxsd
fi
if test -n "$opensslpath"; then
	opensslinc=-I$opensslpath/include
	openssllib=-I$opensslpath/lib64
fi
if test -n "$curlpath"; then
	curlinc=-I$curlpath/include
	curllib=-L$curlpath/lib64
fi
if test -n "$saml2path"; then
	saml2inc="-I$saml2path/include/saml2-\$(VERSION)"
	saml2lib=-L$saml2path/lib
fi

localinc='-I${top_srcdir}/include'

AC_SUBST([AM_CPPFLAGS],["$localinc $xercesinc $xmlsecinc $boostinc $icuinc $xsdinc $opensslinc $curlinc $saml2inc"])

AC_CONFIG_FILES([Makefile include/Makefile src/Makefile])
AC_OUTPUT
