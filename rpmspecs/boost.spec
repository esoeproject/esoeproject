Summary: Boost
Name: boost-ESOE
Version: 1.34.1
Release: 1
Source0: http://optusnet.dl.sourceforge.net/sourceforge/boost/boost_1_34_1.tar.bz2
License: Boost
Group: Development/Libraries
Prefix: /usr/local/spep
BuildRoot: /var/tmp/%{name}-root

%description
The Boost collection of libraries available from http://www.boost.org

%prep
[ $UID -eq 0 ] && echo "rpmbuild as root is bad." >&2 && exit 1
%setup -q -n boost_1_34_1

%build
./configure

%install
sed -i -e "s!^.*using\s*gcc.*\$!using gcc : : : <cxxflags>\"$RPM_OPT_FLAGS\" ;!" user-config.jam
tools/jam/src/bin.linuxx86/bjam --toolset=gcc --layout=system --buildid=esoe --with-regex --with-thread --with-program_options --with-date_time --prefix=$RPM_BUILD_ROOT%{prefix} install

%post
/sbin/ldconfig $RPM_INSTALL_PREFIX0/lib

%postun
/sbin/ldconfig

%files
%defattr(-,root,root)

/usr/local/spep/lib/libboost_date_time-mt-esoe.a
/usr/local/spep/lib/libboost_program_options-mt-d-esoe.so
/usr/local/spep/lib/libboost_program_options-d-esoe.a
/usr/local/spep/lib/libboost_date_time-d-esoe.a
/usr/local/spep/lib/libboost_date_time-mt-esoe.so
/usr/local/spep/lib/libboost_date_time-esoe.a
/usr/local/spep/lib/libboost_thread-mt-d-esoe.a
/usr/local/spep/lib/libboost_regex-mt-d-esoe.so
/usr/local/spep/lib/libboost_regex-esoe.a
/usr/local/spep/lib/libboost_program_options-mt-esoe.so
/usr/local/spep/lib/libboost_date_time-mt-d-esoe.so
/usr/local/spep/lib/libboost_regex-d-esoe.a
/usr/local/spep/lib/libboost_program_options-esoe.so
/usr/local/spep/lib/libboost_program_options-mt-d-esoe.a
/usr/local/spep/lib/libboost_date_time-esoe.so
/usr/local/spep/lib/libboost_regex-mt-esoe.so
/usr/local/spep/lib/libboost_date_time-d-esoe.so
/usr/local/spep/lib/libboost_program_options-d-esoe.so
/usr/local/spep/lib/libboost_program_options-esoe.a
/usr/local/spep/lib/libboost_thread-mt-esoe.a
/usr/local/spep/lib/libboost_thread-mt-d-esoe.so
/usr/local/spep/lib/libboost_regex-d-esoe.so
/usr/local/spep/lib/libboost_program_options-mt-esoe.a
/usr/local/spep/lib/libboost_regex-mt-esoe.a
/usr/local/spep/lib/libboost_regex-mt-d-esoe.a
/usr/local/spep/lib/libboost_regex-esoe.so
/usr/local/spep/lib/libboost_date_time-mt-d-esoe.a
/usr/local/spep/lib/libboost_thread-mt-esoe.so


%package devel
Summary: Boost Headers
Group: Development/Libraries

%description devel
Headers for the Boost libraries

%files devel
%defattr(-,root,root)

/usr/local/spep/include/boost
