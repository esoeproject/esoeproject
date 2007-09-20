#                                               -*- Autoconf -*-
# Process this file with autoconf to produce a configure script.

AC_PREREQ(2.61)
AC_INIT([SPEP Apache Module], 0.1)
AC_CONFIG_SRCDIR([includes/Cookies.h])
#AC_CONFIG_HEADER([config.h])

# Checks for programs.
AC_PROG_CXX
AC_PROG_CC
AC_PROG_INSTALL
AC_PROG_MKDIR_P

# Checks for libraries.

# Checks for header files.

# Checks for typedefs, structures, and compiler characteristics.
AC_HEADER_STDBOOL
AC_C_CONST
AC_TYPE_SIZE_T

# Checks for library functions.
AC_HEADER_STDC

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

AC_ARG_WITH(
	[spepcpp],
	[AS_HELP_STRING(
		[--with-spepcpp=DIR],
		[path to look for spepcpp ./includes])
	],
	[AC_SUBST([SPEPCPPPATH],[$withval])],
	[AC_SUBST([SPEPCPPPATH],[[]])]
)

apache=0
AC_ARG_WITH(
	[apache1],
	[AS_HELP_STRING(
		[--with-apache1=DIR],
		[path to look for apache 1.3 ./include and ./lib])
	],
	[
		AC_SUBST([MODULE_VERSION],[apache-13]) 
		AC_SUBST([APACHEFLAGS],["-DAPACHE1=1 -DEAPI=1"]) 
		AC_SUBST([APACHEPATH],[$withval])
		AC_SUBST([APREQ_LIB],[-lapreq])
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
		AC_SUBST([MODULE_VERSION],[apache-20]) 
		AC_SUBST([APACHEFLAGS],[]) 
		AC_SUBST([APACHEPATH],[$withval])
		AC_SUBST([APREQ_LIB],[-lapreq2])
		apache=2
	]
)

if test $apache -eq 0
then
	AC_MSG_FAILURE([Please provide --with-apache1=DIR or --with-apache2=DIR])
fi

AC_ARG_WITH(
	[apreq],
	[AS_HELP_STRING(
		[--with-apreq=DIR],
		[path to look for apreq (for apache 1.3) or apreq2 (for apache 2.x) ./include and ./lib])
	],
	[AC_SUBST([APREQPATH],[$withval])],
	[AC_SUBST([APREQPATH],[[]])]
)



AC_CONFIG_FILES([Makefile])
AC_OUTPUT
