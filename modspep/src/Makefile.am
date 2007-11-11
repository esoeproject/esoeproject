
lib_LTLIBRARIES = modspep.la

# stop libtool giving it a soname - there's just no point when it's going
# to be used with dlopen(3) et al
modspep_la_LDFLAGS = -module -avoid-version $(AM_LDFLAGS)
modspep_la_LIBADD = -lboost_program_options -lcurl -lboost_regex -lsaml2 -lspep -lboost_thread -lboost_date_time -licuio -licule -liculx -licuuc -licui18n -licudata -licutu -lxerces-c -lxerces-depdom -lxml-security-c
modspep_la_SOURCES = APRFileLogger.cpp \
Cookies.cpp \
SSOHandler.cpp \
WSHandler.cpp \
RequestParameters.cpp \
Common.cpp \
RequestHandler.cpp

if BUILDING_APACHE1
modspep_la_SOURCES += versions/apache-13.cpp
modspep_la_CPPFLAGS = $(AM_CPPFLAGS) -DAPACHE1=1 -DEAPI=1
modspep_la_LIBADD += -lapreq
endif
if BUILDING_APACHE2
modspep_la_SOURCES += versions/apache-2x.cpp
modspep_la_CPPFLAGS = $(AM_CPPFLAGS)
modspep_la_LIBADD += -lapreq2
endif
