Summary: libapreq
Name: libapreq-oracle
Version: 1.34
Release: 1.el4
Source0: libapreq-%{version}.tar.gz
Source1: Apache-oracle-1.3.31.tar.gz
License: Apache 2.0
Group: Development/Libraries
BuildRoot: /var/tmp/%{name}-root
Prefix: /usr/local/spep

%description
Provides an API for parsing all aspects of an Apache request.

%prep
[ $UID -eq 0 ] && echo "rpmbuild as root is bad." >&2 && exit 1
%setup -q -n libapreq-%{version} -a 1

%build
./BUILD.sh
./configure --prefix=%{prefix} --with-apache-includes=$RPM_BUILD_DIR/libapreq-%{version}/Apache-oracle-1.3.31/include
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

/usr/local/spep/lib/libapreq*
/usr/local/spep/include/libapreq

