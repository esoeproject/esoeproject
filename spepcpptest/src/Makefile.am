
bin_PROGRAMS = spepcpptest
spepcpptest_SOURCES = main.cpp \
speptest/GlobalFixtures.cpp \
speptest/MockWeb.cpp \
speptest/MockWebTest.cpp \
ipc/SocketArchiveTest.cpp \
ipc/SocketTest.cpp \
authn/AuthnProcessorTest.cpp

spepcpptest_LDADD = -lboost_program_options$(BOOST_SUFFIX) -lcurl -lboost_regex$(BOOST_SUFFIX) -lesoesaml2 -lspep -lboost_thread$(BOOST_SUFFIX) -lboost_date_time$(BOOST_SUFFIX) -licuio -licule -liculx -licuuc -licui18n -licudata -licutu -lxerces-c -lxerces-depdom -lxml-security-c -lmicrohttpd
