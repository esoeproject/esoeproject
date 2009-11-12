Summary: libapreq2
Name: libapreq2-ESOE
Version: 2.08
Release: 1.el5
Source0: libapreq2-%{version}.tar.gz
License: Apache 2.0
Group: Development/Libraries
BuildRoot: /var/tmp/%{name}-root
Requires: httpd >= 2.0
BuildRequires: httpd-devel >= 2.0
Prefix: /usr/local/spep

%package module
Summary: libapreq2
Group: Development/Libraries
Requires: libapreq2-ESOE

%package devel
Summary: libapreq2
Group: Development/Libraries
Requires: libapreq2-ESOE

%description
Provides an API for parsing all aspects of an Apache request.

%description module
Module for libapreq2

%description devel
Headers for libapreq2

%prep
[ $UID -eq 0 ] && echo "rpmbuild as root is bad." >&2 && exit 1
%setup -q -n libapreq2-%{version}

%build
./configure --prefix=%{prefix} --with-apache2-apxs=/usr/sbin/apxs
make

%install
rm -rf "$RPM_BUILD_ROOT"
make install DESTDIR="$RPM_BUILD_ROOT"

%post
/sbin/ldconfig $RPM_INSTALL_PREFIX0/lib

%postun
/sbin/ldconfig

%files
%defattr(-,root,root)

/usr/local/spep/bin/apreq2-config
/usr/local/spep/lib/libapreq2*

%ifnarch x86_64

%files module
%defattr(-,root,root)

/usr/lib/httpd/modules/mod_apreq2*

%else

%files module
%defattr(-,root,root)

/usr/lib64/httpd/modules/mod_apreq2*

%endif

%files devel
%defattr(-,root,root)

/usr/local/spep/include/apreq2
/usr/include/httpd/apreq2

