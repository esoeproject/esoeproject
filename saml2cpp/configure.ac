#                                               -*- Autoconf -*-
# Process this file with autoconf to produce a configure script.

AC_PREREQ(2.59)
AC_INIT([SAML2], 0.8.5)
AC_CONFIG_SRCDIR([include/saml2/SAML2Defs.h])

#AC_SUBST([VERSION_INFO],[1:0:0])

AM_INIT_AUTOMAKE
AM_DISABLE_STATIC

# Checks for programs.
AC_PROG_CXX
AC_PROG_CC
AC_PROG_LIBTOOL

# Checks for libraries.

# Checks for header files.

# Checks for typedefs, structures, and compiler characteristics.
AC_HEADER_STDBOOL
AC_C_CONST
AC_C_INLINE
AC_TYPE_SIZE_T
AC_STRUCT_TM

# Checks for library functions.
AC_HEADER_STDC
AC_FUNC_STRFTIME
AC_CHECK_FUNCS([localtime_r])

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
	[boost],
	[AS_HELP_STRING(
		[--with-boost=DIR],
		[path to look for boost ./include and ./lib])
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
	boostlib=-L$boostpath/lib
fi
if test -n "$xsdpath"; then
	xsdinc=-I$xsdpath/libxsd
fi
if test -n "$opensslpath"; then
	opensslinc=-I$opensslpath/include
	openssllib=-I$opensslpath/lib
fi

localinc='-I${top_srcdir}/include'

AC_SUBST([AM_CPPFLAGS],["$localinc $xercesinc $xmlsecinc $boostinc $xsdinc $opensslinc"])

AC_CONFIG_FILES([Makefile include/Makefile src/Makefile schema/Makefile])
AC_OUTPUT
