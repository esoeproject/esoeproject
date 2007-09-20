#                                               -*- Autoconf -*-
# Process this file with autoconf to produce a configure script.

AC_PREREQ(2.61)
AC_INIT([C++ SPEP Library], 0.1)
AC_CONFIG_SRCDIR([includes/Util.h])
#AC_CONFIG_HEADER([config.h])
AC_SUBST([BINARY_VERSION],[0])

# Checks for programs.
AC_PROG_CXX
AC_PROG_CC
AC_PROG_INSTALL
AC_PROG_MKDIR_P
AC_PROG_LN_S

# Checks for libraries.

# Checks for header files.
AC_HEADER_STDC
AC_CHECK_HEADERS([netdb.h netinet/in.h sys/socket.h])

# Checks for typedefs, structures, and compiler characteristics.
AC_HEADER_STDBOOL
AC_C_CONST
AC_C_INLINE
AC_TYPE_INT32_T
AC_TYPE_SIZE_T
AC_TYPE_SSIZE_T

# Checks for library functions.
AC_FUNC_MALLOC
AC_FUNC_REALLOC
AC_FUNC_STRTOD
AC_CHECK_FUNCS([gethostbyname memmove memset socket strerror])

AC_ARG_WITH(
	[xerces],
	[AS_HELP_STRING(
		[--with-xerces=DIR],
		[path to look for xerces ./include and ./lib])
	],
	[AC_SUBST([XERCESPATH],[$withval])],
	[AC_SUBST([XERCESPATH],[[]])]
)

AC_ARG_WITH(
	[xmlsec],
	[AS_HELP_STRING(
		[--with-xmlsec=DIR],
		[path to look for xml-security-c ./include and ./lib])
	],
	[AC_SUBST([XMLSECPATH],[$withval])],
	[AC_SUBST([XMLSECPATH],[[]])]
)

AC_ARG_WITH(
	[saml2cpp],
	[AS_HELP_STRING(
		[--with-saml2cpp=DIR],
		[path to look for saml2cpp ./includes and ./src-gen])
	],
	[AC_SUBST([SAML2CPPPATH],[$withval])],
	[AC_SUBST([SAML2CPPPATH],[[]])]
)

AC_ARG_WITH(
	[icu],
	[AS_HELP_STRING(
		[--with-icu=DIR],
		[path to look for ICU ./include and ./lib])
	],
	[AC_SUBST([ICUPATH],[$withval])],
	[AC_SUBST([ICUPATH],[[]])]
)

AC_ARG_WITH(
	[axis2],
	[AS_HELP_STRING(
		[--with-axis2=DIR],
		[path to look for Axis2/C ./include/axis2-1.0 and ./lib])
	],
	[AC_SUBST([AXIS2PATH],[$withval])],
	[AC_SUBST([AXIS2PATH],[[]])]
)

AC_CONFIG_FILES([Makefile])
AC_OUTPUT
