Summary: Xerces-C++
Name: xerces-ESOE
Version: 2.8
Release: 2.el4
Source0: xerces-c-src_2_8_0.tar.gz
License: Apache 2.0
Group: Development/Libraries
Prefix: /usr/local/spep
BuildRoot: /var/tmp/%{name}-root

%description
The Xerces-C++ library from the Apache Software Foundation
at http://xerces.apache.org

%prep
[ $UID -eq 0 ] && echo "rpmbuild as root is bad." >&2 && exit 1
%setup -q -n xerces-c-src_2_8_0

%build
export XERCESCROOT=`pwd`
export CFLAGS="$RPM_OPT_FLAGS"
export CXXFLAGS="$RPM_OPT_FLAGS"
cd src/xercesc
./runConfigure -p linux -P %{prefix}
make

%install
rm -rf "$RPM_BUILD_ROOT"
export XERCESCROOT=`pwd`
cd src/xercesc
make install DESTDIR="$RPM_BUILD_ROOT"

%post
/sbin/ldconfig $RPM_INSTALL_PREFIX0/lib

%postun
/sbin/ldconfig

%files
%defattr(-,root,root)

/usr/local/spep/lib/libxerces-depdom.so.28.0
/usr/local/spep/lib/libxerces-c.so.28.0
/usr/local/spep/lib/libxerces-c.so
/usr/local/spep/lib/libxerces-c.so.28
/usr/local/spep/lib/libxerces-depdom.so
/usr/local/spep/lib/libxerces-depdom.so.28


%package devel
Summary: Xerces-C++
Group: Development/Libraries

%description devel
Headers for the Xerces-C++ library

%files devel
%defattr(-,root,root)

/usr/local/spep/include/xercesc

