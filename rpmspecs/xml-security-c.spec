Summary: XML-Security-C library
Name: xml-security-c-ESOE
Version: 1.4.0
Release: 1
Source0: xml-security-c-%{version}.tar.gz
License: Apache 2.0
Group: Development/Libraries
BuildRoot: /var/tmp/%{name}-root
Prefix: /usr/local/spep

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

/usr/local/spep/lib/libxml-security-c.so.14.0.0
/usr/local/spep/lib/libxml-security-c.a
/usr/local/spep/lib/libxml-security-c.la
/usr/local/spep/lib/libxml-security-c.so
/usr/local/spep/lib/libxml-security-c.so.14
/usr/local/spep/bin/c14n
/usr/local/spep/bin/cipher
/usr/local/spep/bin/xklient
/usr/local/spep/bin/templatesign
/usr/local/spep/bin/xtest
/usr/local/spep/bin/siginf
/usr/local/spep/bin/txfmout
/usr/local/spep/bin/checksig

%files devel
%defattr(-,root,root)

/usr/local/spep/include/xsec

