Summary: XML-Security-C library
Name: xml-security-c-ESOE
Version: 1.7.2
Release: 2.el5
Source0: http://archive.apache.org/dist/santuario/c-library/xml-security-c-%{version}.tar.gz
License: Apache 2.0
Group: Development/Libraries
BuildRoot: /var/tmp/%{name}-root
Prefix: /usr/local/spep
Requires: openssl > 0.9.7
BuildRequires: openssl-devel > 0.9.7

%package devel
Summary: XML-Security-C library headers
Group: Development/Libraries

%description
An implementation of the XML Digital Signature specification.

%description devel
Header files for XML-Security-C

%prep
[ $UID -eq 0 ] && echo "rpmbuild as root is bad." >&2 && exit 1
%setup -q -n xml-security-c-%{version}

%build
export CFLAGS="$RPM_OPT_FLAGS"
export CXXFLAGS="$RPM_OPT_FLAGS"
./configure --prefix=%{prefix} --with-xerces=%{prefix}
make

%install
make install DESTDIR="$RPM_BUILD_ROOT"

%post
/sbin/ldconfig $RPM_INSTALL_PREFIX0/lib

%postun
/sbin/ldconfig

%files
%defattr(-,root,root)

/usr/local/spep/lib/libxml-security-c*
/usr/local/spep/bin

%files devel
%defattr(-,root,root)

/usr/local/spep/include/xsec

