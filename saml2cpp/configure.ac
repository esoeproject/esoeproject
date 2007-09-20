#                                               -*- Autoconf -*-
# Process this file with autoconf to produce a configure script.

AC_PREREQ(2.61)
AC_INIT([SAML2 C++], 0.1)
AC_CONFIG_SRCDIR([includes/SAML2Defs.h])
AC_SUBST([BINARY_VERSION],[0])

# Checks for programs.
AC_PROG_CXX
AC_PROG_CC
AC_PROG_INSTALL
AC_PROG_MKDIR_P
AC_PROG_LN_S

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

AC_CONFIG_FILES([Makefile])
AC_OUTPUT
