Summary: Xerces-C++
Name: xerces-ESOE
Version: 3.1.0
Release: 1.5
Source0: http://archive.apache.org/dist/xml/xerces-c/Xerces-C_3_1_0/sources/xerces-c-3.1.1.tar.gz
License: Apache 2.0
Group: Development/Libraries
Prefix: /usr/local/spep
BuildRoot: /var/tmp/%{name}-root

%description
The Xerces-C++ library from the Apache Software Foundation
at http://xerces.apache.org

%prep
#[ $UID -eq 0 ] && echo "rpmbuild as root is bad." >&2 && exit 1
%setup -q -n xerces-c-3.1.1

%build
export XERCESCROOT=`pwd`
export CFLAGS="$RPM_OPT_FLAGS"
export CXXFLAGS="$RPM_OPT_FLAGS"
#cd include/xercesc
./configure --prefix=%{prefix}
make

%install
rm -rf "$RPM_BUILD_ROOT"
export XERCESCROOT=`pwd`
#cd include/xercesc
make install DESTDIR="$RPM_BUILD_ROOT"

%post
/sbin/ldconfig $RPM_INSTALL_PREFIX0/lib

%postun
/sbin/ldconfig

%files
%defattr(-,root,root)
/usr/local/spep/lib/libxerces-c-3.1.so
/usr/local/spep/lib/libxerces-c.so


%package devel
Summary: Xerces-C++
Group: Development/Libraries

%description devel
Headers for the Xerces-C++ library

%files devel
%defattr(-,root,root)

/usr/local/spep/include/xercesc

