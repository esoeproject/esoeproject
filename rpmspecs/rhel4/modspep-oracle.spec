Summary: SPEP for Oracle HTTPD
Name: modspep-oracle
Version: 0.7
Release: 2.el4
Source0: modspep-%{version}.tar.gz
Source1: Apache-oracle-1.3.31.tar.gz
License: Apache 2.0
Group: Development/Libraries
BuildRoot: /var/tmp/%{name}-root
Prefix: /usr/local/spep
Requires: boost-ESOE >= 1.33
Requires: xml-security-c-ESOE >= 1.3.0
Requires: xerces-ESOE >= 2.7.0
Requires: icu-ESOE >= 3.6
Requires: spep = %{version}
Requires: libapreq-oracle > 1.30
BuildRequires: boost-ESOE-devel >= 1.33
BuildRequires: xml-security-c-ESOE-devel >= 1.3.0
BuildRequires: xerces-ESOE-devel >= 2.7.0
BuildRequires: icu-ESOE-devel >= 3.6
BuildRequires: xsd = 3.2.0
BuildRequires: spep-devel = %{version}
BuildRequires: libapreq-oracle > 1.30

%description
SPEP module for Oracle HTTPD (Apache 1.3.31)

%prep
[ $UID -eq 0 ] && echo "rpmbuild as root is bad." >&2 && exit 1
%setup -q -n src -c -a 1

%build
cd $RPM_BUILD_DIR/src/modspep-%{version}
export CXXFLAGS="$RPM_OPT_FLAGS"

export APXS=`which /usr/bin/apxs /usr/bin/apxs2 /usr/sbin/apxs /usr/sbin/apxs2`
[ -z "$APXS" ] && echo "couldn't find apxs" && exit 1
export APRCONFIG=`which /usr/bin/apr-config`
[ -z "$APRCONFIG" ] && echo "couldn't find apr-config" && exit 1
export CXXFLAGS="$CXXFLAGS -I`$APXS -q INCLUDEDIR` `$APXS -q CFLAGS` `$APRCONFIG --includes`"
./configure --prefix=%{prefix} --with-boost-suffix=-mt-esoe --with-apache1=$RPM_BUILD_DIR/src/Apache-oracle-1.3.31 --with-xerces=/usr/local/spep --with-spepcpp=/usr/local/spep --with-saml2cpp=/usr/local/spep
make

%install
rm -rf $RPM_BUILD_ROOT
cd $RPM_BUILD_DIR/src/modspep-%{version} || exit 1
make install DESTDIR=$RPM_BUILD_ROOT

%post
ldconfig

%postun
ldconfig

%files
%defattr(-,root,root)

/usr/local/spep/lib/modspep.*

